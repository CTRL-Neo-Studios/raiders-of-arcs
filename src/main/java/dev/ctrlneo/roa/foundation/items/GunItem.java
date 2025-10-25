package dev.ctrlneo.roa.foundation.items;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.AttachmentSlot;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.utils.GunHelper;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class GunItem extends Item {

    public GunItem(Properties properties) {
        super(properties.stacksTo(1).durability(0)); // Guns don't use durability
    }

    // ========== Server-side Methods (Called from Packets) ==========

    /**
     * Called from FireGunPacket
     */
    public void tryFire(Level level, Player player, ItemStack stack) {
        // Get components
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
        GunAttachmentsComponent attachments = stack.getOrDefault(
                RoaDataComponents.GUN_ATTACHMENTS.get(),
                GunAttachmentsComponent.EMPTY
        );
        GunStateComponent state = stack.getOrDefault(
                RoaDataComponents.GUN_STATE.get(),
                GunStateComponent.DEFAULT
        );
        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());

        // Get effective stats (base + attachment modifiers)
        GunStatsComponent stats = GunUtils.getEffectiveStats(stack);

        // Validation
        if (magazine == null || modes == null) {
            return; // Gun not properly initialized
        }

        if (!magazine.canFire()) {
            playEmptySound(level, player);
            return;
        }

        if (state.isReloading()) {
            return;
        }

        // Check fire rate
        long currentTime = level.getGameTime();
        if (!state.canFire(currentTime, stats.getTicksBetweenShots())) {
            return;
        }

        // Handle burst fire
        GunFireMode currentMode = modes.currentMode();
        if (currentMode.isContinuous() && currentMode != GunFireMode.AUTOMATIC_FIRE) {
            // Burst fire logic
            if (state.burstShotsFired() >= currentMode.getBurstCount()) {
                stack.set(RoaDataComponents.GUN_STATE.get(), state.resetBurst());
                return;
            }
        }

        // Fire the gun!
        fireProjectile(level, player, stack, stats);

        // Update components
        stack.set(RoaDataComponents.GUN_MAGAZINE.get(), magazine.consume(1));
        stack.set(RoaDataComponents.GUN_STATE.get(), state.fired(currentTime));

        // Play sound & effects
        playFireSound(level, player, stack);

        // Apply recoil
        applyRecoil(player, stats);
    }

    /**
     * Called from ReloadGunPacket
     */
    public void reload(Player player, ItemStack stack) {
        GunAttachmentsComponent attachments = stack.getOrDefault(
                RoaDataComponents.GUN_ATTACHMENTS.get(),
                GunAttachmentsComponent.EMPTY
        );

        if (!GunHelper.canReload(stack, player, attachments)) {
            player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.0f);
            return;
        }

        GunStateComponent state = stack.getOrDefault(
                RoaDataComponents.GUN_STATE.get(),
                GunStateComponent.DEFAULT
        );

        // Start reload
        stack.set(RoaDataComponents.GUN_STATE.get(), state.withReloading(true));

        // Get effective stats for reload time
        GunStatsComponent stats = GunUtils.getEffectiveStats(stack);

        // TODO: Schedule reload completion after stats.getReloadTicks() ticks
        // For now, instant reload:
        GunHelper.reload(stack, player, attachments);
        stack.set(RoaDataComponents.GUN_STATE.get(), state.withReloading(false));

        if (player instanceof ServerPlayer serverPlayer) {
            GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.roa.reloaded",
                            magazine.currentAmmo(),
                            magazine.getEffectiveCapacity(attachments)),
                    true
            );
        }

        // TODO: Play reload sound based on stats.reloadSpeed()
    }

    /**
     * Called from CycleFireModePacket
     */
    public void cycleFireMode(ItemStack stack, Player player) {
        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        if (modes == null || modes.availableModes().isEmpty()) return;

        int currentIndex = modes.availableModes().indexOf(modes.currentMode());
        int nextIndex = (currentIndex + 1) % modes.availableModes().size();
        GunFireMode nextMode = modes.availableModes().get(nextIndex);

        stack.set(
                RoaDataComponents.GUN_FIRE_MODES.get(),
                new GunFireModesComponent(nextMode, modes.availableModes())
        );

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.roa.fire_mode_changed", nextMode.getDisplayName()),
                    true
            );
        }

        player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.5f, 1.5f);
    }

    // ========== Helper Methods ==========

    private void fireProjectile(Level level, Player player, ItemStack stack, GunStatsComponent stats) {
        // TODO: Implement your projectile/hitscan logic
        // Use stats for damage, accuracy, range, penetration

        // Example placeholder:
        // Vec3 lookVec = player.getLookAngle();
        // float spread = 1.0f - stats.accuracy(); // Convert accuracy to spread
        // BulletEntity bullet = new BulletEntity(level, player, stats.damage());
        // bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0f, spread);
        // level.addFreshEntity(bullet);
    }

    private void applyRecoil(Player player, GunStatsComponent stats) {
        // TODO: Apply recoil to player's view based on stats.recoilVertical() and stats.recoilHorizontal()
        // You'll need to send a packet to the client to adjust camera

        // Example placeholder:
        // float pitchRecoil = stats.recoilVertical() * (random.nextFloat() - 0.5f);
        // float yawRecoil = stats.recoilHorizontal() * (random.nextFloat() - 0.5f);
        // RoaPackets.sendToPlayer((ServerPlayer) player, new ApplyRecoilPacket(pitchRecoil, yawRecoil));
    }

    private void playFireSound(Level level, Player player, ItemStack stack) {
        // TODO: Play gun fire sound
        // Different sounds for suppressed vs unsuppressed (check muzzle attachment)

        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        boolean hasSuppressor = attachments != null && !attachments.muzzle().isEmpty();

        // if (hasSuppressor) {
        //     level.playSound(null, player.blockPosition(), SUPPRESSED_FIRE_SOUND, SoundSource.PLAYERS, 0.5f, 1.0f);
        // } else {
        //     level.playSound(null, player.blockPosition(), FIRE_SOUND, SoundSource.PLAYERS, 1.0f, 1.0f);
        // }
    }

    private void playEmptySound(Level level, Player player) {
        player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.5f, 1.0f);
    }

    // ========== Tooltip ==========

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        GunAttachmentsComponent attachments = stack.getOrDefault(
                RoaDataComponents.GUN_ATTACHMENTS.get(),
                GunAttachmentsComponent.EMPTY
        );

        // Magazine info (with effective capacity)
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (magazine != null) {
            int effectiveCapacity = magazine.getEffectiveCapacity(attachments);
            tooltipComponents.add(Component.translatable(
                    "tooltip.roa.ammo",
                    magazine.currentAmmo(),
                    effectiveCapacity
            ).withStyle(magazine.currentAmmo() == 0 ? ChatFormatting.RED : ChatFormatting.WHITE));

            // Show capacity bonus if extended mag is equipped
            if (effectiveCapacity > magazine.baseCapacity()) {
                int bonus = effectiveCapacity - magazine.baseCapacity();
                tooltipComponents.add(Component.translatable(
                        "tooltip.roa.extended_mag_bonus",
                        bonus
                ).withStyle(ChatFormatting.GREEN));
            }
        }

        // Fire mode info
        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        if (modes != null) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.roa.fire_mode",
                    modes.currentMode().getDisplayName()
            ).withStyle(ChatFormatting.GRAY));
        }

        tooltipComponents.add(Component.empty());

        // Show attachments
        if (attachments.hasAnyAttachments()) {
            tooltipComponents.add(Component.translatable("tooltip.roa.attachments")
                    .withStyle(ChatFormatting.GOLD));

            Map<AttachmentSlot, ItemStack> allAttachments = attachments.getAllAttachments();
            for (Map.Entry<AttachmentSlot, ItemStack> entry : allAttachments.entrySet()) {
                tooltipComponents.add(Component.literal("  ")
                        .append(Component.translatable(
                                "tooltip.roa.attachment_slot." + entry.getKey().getSerializedName()
                        ))
                        .append(": ")
                        .append(entry.getValue().getHoverName())
                        .withStyle(ChatFormatting.GRAY));
            }
            tooltipComponents.add(Component.empty());
        }

        // Stats (when holding Shift)
        if (tooltipFlag.hasShiftDown()) {
            // Get base stats and effective stats
            GunStatsComponent baseStats = stack.get(RoaDataComponents.GUN_STATS.get());
            GunStatsComponent effectiveStats = GunUtils.getEffectiveStats(stack);

            if (effectiveStats != null) {
                tooltipComponents.add(Component.translatable("tooltip.roa.stats")
                        .withStyle(ChatFormatting.AQUA));

                addStatLine(tooltipComponents, "damage", baseStats.damage(), effectiveStats.damage());
                addStatLine(tooltipComponents, "accuracy", baseStats.accuracy() * 100, effectiveStats.accuracy() * 100);
                addStatLine(tooltipComponents, "fire_rate", baseStats.fireRate(), effectiveStats.fireRate());
                addStatLine(tooltipComponents, "range", baseStats.range(), effectiveStats.range());
                addStatLine(tooltipComponents, "recoil", baseStats.recoilVertical(), effectiveStats.recoilVertical());
                addStatLine(tooltipComponents, "ads_speed", baseStats.adsSpeed(), effectiveStats.adsSpeed());
                addStatLine(tooltipComponents, "reload_speed", baseStats.reloadSpeed(), effectiveStats.reloadSpeed());

                if (effectiveStats.armorPenetration() > 0) {
                    tooltipComponents.add(Component.translatable(
                            "tooltip.roa.armor_penetration",
                            String.format("%.0f%%", effectiveStats.armorPenetration() * 100)
                    ).withStyle(ChatFormatting.YELLOW));
                }
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.roa.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("tooltip.roa.controls")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("tooltip.roa.lmb_fire")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.roa.rmb_ads")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.roa.r_reload")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.roa.b_firemode")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.roa.i_attachments")
                .withStyle(ChatFormatting.GRAY));
    }

    private void addStatLine(List<Component> tooltips, String key, float baseValue, float effectiveValue) {
        Component text = Component.translatable("tooltip.roa.stat." + key,
                String.format("%.1f", effectiveValue));

        // Show difference if attachments modify the stat
        if (Math.abs(effectiveValue - baseValue) > 0.01f) {
            float diff = effectiveValue - baseValue;
            ChatFormatting color = diff > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;

            text = text.copy()
                    .append(Component.literal(String.format(" (%+.1f)", diff))
                            .withStyle(color));
        }

        tooltips.add(Component.literal("  ").append(text).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // Don't play animation when:
        // - Ammo count changes
        // - Gun state changes (reloading, etc)
        // - Fire mode changes
        if (slotChanged || !ItemStack.isSameItem(oldStack, newStack)) {
            return true;
        }

        // Check if attachments changed (this should trigger animation)
        GunAttachmentsComponent oldAttachments = oldStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunAttachmentsComponent newAttachments = newStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());

        if (!java.util.Objects.equals(oldAttachments, newAttachments)) {
            return true;
        }

        return false;
    }
}