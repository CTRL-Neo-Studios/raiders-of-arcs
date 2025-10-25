package dev.ctrlneo.roa.foundation.items;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.AttachmentSlot;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.entity.BulletEntity;
import dev.ctrlneo.roa.foundation.utils.GunHelper;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class GunItem extends Item {

    // Store default components in the class
    private final GunStatsComponent defaultStats;
    private final GunMagazineComponent defaultMagazine;
    private final GunFireModesComponent defaultFireModes;

    public GunItem(Properties properties,
                   GunStatsComponent stats,
                   GunMagazineComponent magazine,
                   GunFireModesComponent fireModes) {
        super(properties.stacksTo(1).durability(0));
        this.defaultStats = stats;
        this.defaultMagazine = magazine;
        this.defaultFireModes = fireModes;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // Ensure ALL components are present
        stack.set(RoaDataComponents.GUN_STATS.get(), defaultStats);
        stack.set(RoaDataComponents.GUN_MAGAZINE.get(), defaultMagazine);
        stack.set(RoaDataComponents.GUN_FIRE_MODES.get(), defaultFireModes);
        stack.set(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT);
        stack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);
        return stack;
    }

    // Getters for default values
    public GunStatsComponent getDefaultStats() {
        return defaultStats;
    }

    public GunMagazineComponent getDefaultMagazine() {
        return defaultMagazine;
    }

    public GunFireModesComponent getDefaultFireModes() {
        return defaultFireModes;
    }

    public void tryFire(Level level, Player player, ItemStack stack) {
        // Ensure components exist
        ensureComponents(stack);

        // Get components
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunStateComponent state = stack.get(RoaDataComponents.GUN_STATE.get());
        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        GunStatsComponent stats = GunUtils.getEffectiveStats(stack);

        // Validation
        if (magazine == null || modes == null || stats == null) {
            return;
        }

        if (!magazine.canFire()) {
            playEmptySound(level, player);
            return;
        }

        if (state.isReloading()) {
            return;
        }

        long currentTime = level.getGameTime();
        if (!state.canFire(currentTime, stats.getTicksBetweenShots())) {
            return;
        }

        // Handle burst fire
        GunFireMode currentMode = modes.currentMode();
        if (currentMode.isContinuous() && currentMode != GunFireMode.AUTOMATIC_FIRE) {
            if (state.burstShotsFired() >= currentMode.getBurstCount()) {
                stack.set(RoaDataComponents.GUN_STATE.get(), state.resetBurst());
                return;
            }
        }

        // Fire the gun!
        fireProjectile(level, player, stack, stats);

        // Update components
        stack.set(RoaDataComponents.GUN_MAGAZINE.get(), magazine.consume(1));

        if (currentMode.isContinuous() && currentMode != GunFireMode.AUTOMATIC_FIRE) {
            stack.set(RoaDataComponents.GUN_STATE.get(), state.fired(currentTime));
        } else {
            stack.set(RoaDataComponents.GUN_STATE.get(), new GunStateComponent(
                    false,
                    currentTime,
                    0
            ));
        }

        playFireSound(level, player, stack);
    }

    private void fireProjectile(Level level, Player player, ItemStack stack, GunStatsComponent stats) {
        if (level.isClientSide) {
            return;
        }

        BulletEntity bullet = new BulletEntity(
                level,
                player,
                stats.damage(),
                stats.range(),
                stats.armorPenetration(),
                true
        );

        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getLookAngle();
        bullet.setPos(eyePos.add(lookVec.scale(0.5)));

        float inaccuracy = 1.0f - stats.accuracy();
        bullet.shoot(lookVec, inaccuracy);

        level.addFreshEntity(bullet);
    }

    private void playFireSound(Level level, Player player, ItemStack stack) {
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                0.5f,
                1.0f + (player.getRandom().nextFloat() - 0.5f) * 0.2f
        );
    }

    private void playEmptySound(Level level, Player player) {
        player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.5f, 1.0f);
    }

    public void reload(Player player, ItemStack stack) {
        ensureComponents(stack);

        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());

        if (magazine == null || !GunHelper.canReload(stack, player, attachments)) {
            player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.0f);
            return;
        }

        GunStateComponent state = stack.get(RoaDataComponents.GUN_STATE.get());

        stack.set(RoaDataComponents.GUN_STATE.get(), state.withReloading(true));
        GunHelper.reload(stack, player, attachments);
        stack.set(RoaDataComponents.GUN_STATE.get(), state.withReloading(false));

        GunMagazineComponent updatedMag = stack.get(RoaDataComponents.GUN_MAGAZINE.get());

        if (player instanceof ServerPlayer serverPlayer && updatedMag != null) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.roa.reloaded",
                            updatedMag.currentAmmo(),
                            updatedMag.getEffectiveCapacity(attachments)),
                    true
            );
        }
    }

    public void cycleFireMode(ItemStack stack, Player player) {
        ensureComponents(stack);

        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        if (modes == null || modes.availableModes().isEmpty()) {
            return;
        }

        List<GunFireMode> availableModes = modes.availableModes();
        GunFireMode currentMode = modes.currentMode();

        int currentIndex = availableModes.indexOf(currentMode);
        if (currentIndex < 0) {
            currentIndex = 0;
        }

        int nextIndex = (currentIndex + 1) % availableModes.size();
        GunFireMode nextMode = availableModes.get(nextIndex);

        stack.set(
                RoaDataComponents.GUN_FIRE_MODES.get(),
                new GunFireModesComponent(nextMode, List.copyOf(availableModes))
        );

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.roa.fire_mode_changed", nextMode.getDisplayName()),
                    true
            );
        }

        player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.5f, 1.5f);
    }

    /**
     * Ensures all components exist using stored defaults
     */
    private void ensureComponents(ItemStack stack) {
        if (!stack.has(RoaDataComponents.GUN_STATS.get())) {
            stack.set(RoaDataComponents.GUN_STATS.get(), defaultStats);
        }
        if (!stack.has(RoaDataComponents.GUN_MAGAZINE.get())) {
            stack.set(RoaDataComponents.GUN_MAGAZINE.get(), defaultMagazine);
        }
        if (!stack.has(RoaDataComponents.GUN_FIRE_MODES.get())) {
            stack.set(RoaDataComponents.GUN_FIRE_MODES.get(), defaultFireModes);
        }
        if (!stack.has(RoaDataComponents.GUN_STATE.get())) {
            stack.set(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT);
        }
        if (!stack.has(RoaDataComponents.GUN_ATTACHMENTS.get())) {
            stack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // Ensure components exist before reading
        ensureComponents(stack);

        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());

        // Magazine info
        int effectiveCapacity = magazine.getEffectiveCapacity(attachments);
        tooltipComponents.add(Component.translatable(
                "tooltip.roa.ammo",
                magazine.currentAmmo(),
                effectiveCapacity
        ).withStyle(magazine.currentAmmo() == 0 ? ChatFormatting.RED : ChatFormatting.WHITE));

        if (effectiveCapacity > magazine.baseCapacity()) {
            int bonus = effectiveCapacity - magazine.baseCapacity();
            tooltipComponents.add(Component.translatable(
                    "tooltip.roa.extended_mag_bonus",
                    bonus
            ).withStyle(ChatFormatting.GREEN));
        }

        // Fire mode info
        tooltipComponents.add(Component.translatable(
                "tooltip.roa.fire_mode",
                modes.currentMode().getDisplayName()
        ).withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.empty());

        // Attachments
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
            GunStatsComponent baseStats = stack.get(RoaDataComponents.GUN_STATS.get());
            GunStatsComponent effectiveStats = GunUtils.getEffectiveStats(stack);

            tooltipComponents.add(Component.translatable("tooltip.roa.stats")
                    .withStyle(ChatFormatting.AQUA));

            addStatLine(tooltipComponents, "damage", baseStats.damage(), effectiveStats.damage());
            addStatLine(tooltipComponents, "accuracy", baseStats.accuracy() * 100, effectiveStats.accuracy() * 100);
            addStatLine(tooltipComponents, "fire_rate", (float)baseStats.fireRate(), (float)effectiveStats.fireRate());
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
        } else {
            tooltipComponents.add(Component.translatable("tooltip.roa.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private void addStatLine(List<Component> tooltips, String key, float baseValue, float effectiveValue) {
        Component text = Component.translatable("tooltip.roa.stat." + key,
                String.format("%.1f", effectiveValue));

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
        if (slotChanged || !ItemStack.isSameItem(oldStack, newStack)) {
            return true;
        }

        GunAttachmentsComponent oldAttachments = oldStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunAttachmentsComponent newAttachments = newStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());

        return !java.util.Objects.equals(oldAttachments, newAttachments);
    }
}