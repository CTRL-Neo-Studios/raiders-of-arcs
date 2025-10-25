package dev.ctrlneo.roa.foundation.entity;

import dev.ctrlneo.roa.foundation.RoaEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BulletEntity extends Projectile {

    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ARMOR_PENETRATION =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RANGE =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_TRACER =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.BOOLEAN);

    private Vec3 spawnPosition;
    private int ticksInAir = 0;
    private int maxLifeTime = 100;

    private static final float BULLET_VELOCITY = 8.0f;

    public BulletEntity(EntityType<? extends BulletEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BulletEntity(Level level, LivingEntity shooter, float damage, float range, float armorPenetration, boolean isTracer) {
        super(RoaEntityTypes.BULLET.get(), level);
        this.setOwner(shooter);

        Vec3 eyePos = shooter.getEyePosition(1.0f);
        this.setPos(eyePos.x, eyePos.y, eyePos.z);
        this.spawnPosition = eyePos;

        this.entityData.set(DAMAGE, damage);
        this.entityData.set(RANGE, range);
        this.entityData.set(ARMOR_PENETRATION, armorPenetration);
        this.entityData.set(IS_TRACER, isTracer);

        this.maxLifeTime = (int) Math.ceil((range / BULLET_VELOCITY) * 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 10.0f);
        builder.define(RANGE, 50.0f);
        builder.define(ARMOR_PENETRATION, 0.0f);
        builder.define(IS_TRACER, false);
    }

    @Override
    public void tick() {
        super.tick();

        // Initialize spawn position if null (client-side sync)
        if (spawnPosition == null) {
            spawnPosition = this.position();
        }

        ticksInAir++;

        if (ticksInAir > maxLifeTime) {
            this.discard();
            return;
        }

        // Check for hits
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }

        // Calculate distance traveled
        float distanceTraveled = (float) this.position().distanceTo(spawnPosition);
        float effectiveRange = this.entityData.get(RANGE);

        // Move the bullet
        Vec3 motion = this.getDeltaMovement();
        Vec3 newPos = this.position().add(motion);
        this.setPos(newPos);

        // Apply gravity based on distance
        float gravity;
        if (distanceTraveled < effectiveRange) {
            // Within effective range - minimal but noticeable gravity
            gravity = -0.015f; // More noticeable for long range shots
        } else {
            // Beyond effective range - normal projectile gravity
            gravity = -0.05f;
        }

        this.setDeltaMovement(motion.x, motion.y + gravity, motion.z);

        // Spawn tracer particles
        if (this.entityData.get(IS_TRACER) && this.level().isClientSide) {
            spawnTracerParticles();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();

            if (target == owner) {
                return;
            }

            float damage = this.entityData.get(DAMAGE);
            float armorPenetration = this.entityData.get(ARMOR_PENETRATION);

            DamageSource damageSource = this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null);

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.invulnerableTime = 0;

                float armor = livingTarget.getArmorValue();
                float effectiveArmor = armor * (1.0f - armorPenetration);

                float damageReduction = Math.min(20.0f, Math.max(
                        effectiveArmor / 5.0f,
                        effectiveArmor - damage / (2.0f + effectiveArmor / 4.0f)
                ));

                float finalDamage = Math.max(1.0f, damage - damageReduction);
                livingTarget.hurt(damageSource, finalDamage);
                livingTarget.invulnerableTime = 1;
            } else {
                target.hurt(damageSource, damage);
            }

            spawnImpactParticles(result.getLocation());
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            spawnImpactParticles(result.getLocation());
            this.discard();
        }
    }

    private void spawnTracerParticles() {
        this.level().addParticle(
                ParticleTypes.FLAME,
                this.getX(),
                this.getY(),
                this.getZ(),
                0, 0, 0
        );
    }

    private void spawnImpactParticles(Vec3 location) {
        for (int i = 0; i < 8; i++) {
            this.level().addParticle(
                    ParticleTypes.SMOKE,
                    location.x,
                    location.y,
                    location.z,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1
            );
        }
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
    }

    public void shoot(Vec3 direction, float inaccuracy) {
        Vec3 spread = new Vec3(
                direction.x + (this.random.nextGaussian() * 0.007 * inaccuracy),
                direction.y + (this.random.nextGaussian() * 0.007 * inaccuracy),
                direction.z + (this.random.nextGaussian() * 0.007 * inaccuracy)
        ).normalize();

        this.setDeltaMovement(spread.scale(BULLET_VELOCITY));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.entityData.get(DAMAGE));
        compound.putFloat("Range", this.entityData.get(RANGE));
        compound.putFloat("ArmorPenetration", this.entityData.get(ARMOR_PENETRATION));
        compound.putBoolean("IsTracer", this.entityData.get(IS_TRACER));
        compound.putInt("MaxLifeTime", this.maxLifeTime);
        if (spawnPosition != null) {
            compound.putDouble("SpawnX", spawnPosition.x);
            compound.putDouble("SpawnY", spawnPosition.y);
            compound.putDouble("SpawnZ", spawnPosition.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(DAMAGE, compound.getFloat("Damage"));
        this.entityData.set(RANGE, compound.getFloat("Range"));
        this.entityData.set(ARMOR_PENETRATION, compound.getFloat("ArmorPenetration"));
        this.entityData.set(IS_TRACER, compound.getBoolean("IsTracer"));
        this.maxLifeTime = compound.getInt("MaxLifeTime");
        if (compound.contains("SpawnX")) {
            this.spawnPosition = new Vec3(
                    compound.getDouble("SpawnX"),
                    compound.getDouble("SpawnY"),
                    compound.getDouble("SpawnZ")
            );
        }
    }
}