package dev.ctrlneo.roa.foundation.utils;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class HitscanUtil {

    /**
     * Perform a hitscan raycast
     */
    public static HitscanResult performHitscan(
            Level level,
            LivingEntity shooter,
            float range,
            float damage,
            float accuracy,
            float armorPenetration
    ) {
        // Calculate spread based on accuracy (lower accuracy = more spread)
        float spread = (1.0f - accuracy) * 5.0f; // Max 5 degrees of spread

        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 lookVec = shooter.getLookAngle();

        // Apply spread
        Vec3 spreadVec = applySpread(lookVec, spread, shooter.getRandom());
        Vec3 end = start.add(spreadVec.scale(range));

        // Raycast for blocks
        ClipContext clipContext = new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
        );
        BlockHitResult blockHit = level.clip(clipContext);

        // Raycast for entities
        AABB searchBox = shooter.getBoundingBox().inflate(range);
        EntityHitResult entityHit = getEntityHit(level, shooter, start, end, searchBox);

        // Determine which hit is closer
        HitResult finalHit = blockHit;
        if (entityHit != null) {
            double blockDist = blockHit.getLocation().distanceToSqr(start);
            double entityDist = entityHit.getLocation().distanceToSqr(start);
            if (entityDist < blockDist) {
                finalHit = entityHit;
            }
        }

        // Apply damage if we hit an entity
        if (finalHit instanceof EntityHitResult entityResult) {
            Entity target = entityResult.getEntity();
            if (target != shooter && level instanceof ServerLevel serverLevel) {
                applyDamage(serverLevel, shooter, target, damage, armorPenetration);
            }
        }

        return new HitscanResult(finalHit, start, finalHit.getLocation());
    }

    private static Vec3 applySpread(Vec3 direction, float spreadDegrees, net.minecraft.util.RandomSource random) {
        if (spreadDegrees <= 0) {
            return direction;
        }

        // Convert spread from degrees to radians
        float spreadRadians = (float) Math.toRadians(spreadDegrees);

        // Random angles for cone spread
        float theta = random.nextFloat() * 2.0f * (float) Math.PI;
        float phi = random.nextFloat() * spreadRadians;

        // Calculate spread offset
        float x = (float) (Math.sin(phi) * Math.cos(theta));
        float y = (float) (Math.sin(phi) * Math.sin(theta));
        float z = (float) Math.cos(phi);

        // Create rotation vectors
        Vec3 up = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = direction.cross(up).normalize();
        up = right.cross(direction).normalize();

        // Apply spread
        return direction.scale(z)
                .add(right.scale(x))
                .add(up.scale(y))
                .normalize();
    }

    private static EntityHitResult getEntityHit(
            Level level,
            Entity shooter,
            Vec3 start,
            Vec3 end,
            AABB searchBox
    ) {
        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;
        Vec3 closestHitPos = null;

        for (Entity entity : level.getEntities(shooter, searchBox, e -> e != shooter && e.isPickable())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hitPos = entityBox.clip(start, end);

            if (hitPos.isPresent()) {
                double distance = start.distanceToSqr(hitPos.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                    closestHitPos = hitPos.get();
                }
            }
        }

        return closestEntity != null ? new EntityHitResult(closestEntity, closestHitPos) : null;
    }

    private static void applyDamage(
            ServerLevel level,
            LivingEntity shooter,
            Entity target,
            float damage,
            float armorPenetration
    ) {
        DamageSource damageSource = level.damageSources().playerAttack((Player) shooter);

        if (target instanceof LivingEntity livingTarget) {
            // Store and reset invulnerability
            int oldInvulnerableTime = livingTarget.invulnerableTime;
            livingTarget.invulnerableTime = 0;

            // Calculate effective damage with armor penetration
            float armor = livingTarget.getArmorValue();
            float effectiveArmor = armor * (1.0f - armorPenetration);

            // Minecraft armor damage reduction formula
            float damageReduction = Math.min(20.0f, Math.max(
                    effectiveArmor / 5.0f,
                    effectiveArmor - damage / (2.0f + effectiveArmor / 4.0f)
            ));

            float finalDamage = Math.max(1.0f, damage - damageReduction);
            livingTarget.hurt(damageSource, finalDamage);

            // Set minimal invulnerability (1 tick) to prevent same-frame duplicates
            livingTarget.invulnerableTime = 1;
        } else {
            target.hurt(damageSource, damage);
        }
    }

    /**
     * Spawn tracer particles from shooter to hit location
     */
    public static void spawnTracerParticles(ServerLevel level, Vec3 start, Vec3 end, boolean isTracer) {
        if (!isTracer) return;

        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        direction = direction.normalize();

        // Spawn particles along the line
        int particleCount = (int) (distance * 2); // 2 particles per block
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 particlePos = start.add(direction.scale(distance * progress));

            level.sendParticles(
                    ParticleTypes.FLAME,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0, 0, 0
            );
        }
    }

    public record HitscanResult(HitResult hitResult, Vec3 start, Vec3 end) {
        public boolean isHit() {
            return hitResult.getType() != HitResult.Type.MISS;
        }

        public boolean hitEntity() {
            return hitResult instanceof EntityHitResult;
        }

        public boolean hitBlock() {
            return hitResult instanceof BlockHitResult;
        }
    }
}