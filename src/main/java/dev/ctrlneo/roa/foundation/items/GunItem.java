package dev.ctrlneo.roa.foundation.items;

import com.mojang.logging.LogUtils;
import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.animations.controller.AnimatorController;
import dev.ctrlneo.roa.foundation.animations.controller.core.AnimationCommand;
import dev.ctrlneo.roa.foundation.animations.controller.core.AnimationState;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.AttachmentSlot;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.data.structures.GunLevelConfig;
import dev.ctrlneo.roa.foundation.data.structures.ReloadType;
import dev.ctrlneo.roa.foundation.entity.BulletEntity;
import dev.ctrlneo.roa.foundation.network.packets.ApplyRecoilPacket;
import dev.ctrlneo.roa.foundation.utils.GunHelper;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class GunItem extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG = true; // Set to false to disable debug logs

    // Store default components in the class
    private final GunStatsComponent defaultStats;
    private final GunMagazineComponent defaultMagazine;
    private final GunFireModesComponent defaultFireModes;
    private final GunReloadComponent defaultReload;
    private final GunLevelConfig levelConfig;
    public final AnimatorController animatorController;

    public GunItem(Properties properties,
            GunStatsComponent stats,
            GunMagazineComponent magazine,
            GunFireModesComponent fireModes,
            GunReloadComponent reload,
            GunLevelConfig levelConfig,
            AnimatorController animatorController) {
        super(properties.stacksTo(1));
        this.defaultStats = stats;
        this.defaultMagazine = magazine;
        this.defaultFireModes = fireModes;
        this.defaultReload = reload;
        this.levelConfig = levelConfig;
        this.animatorController = animatorController;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        // Ensure ALL components are present
        stack.set(RoaDataComponents.GUN_STATS.get(), defaultStats);
        stack.set(RoaDataComponents.GUN_MAGAZINE.get(), defaultMagazine);
        stack.set(RoaDataComponents.GUN_FIRE_MODES.get(), defaultFireModes);
        stack.set(RoaDataComponents.GUN_RELOAD.get(), defaultReload);
        stack.set(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT);
        stack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);
        stack.set(RoaDataComponents.GUN_LEVEL.get(), GunLevelComponent.DEFAULT);
        return stack;
    }

    // Getters for default values
    public GunStatsComponent getDefaultStats() {
        return defaultStats;
    }

    public GunLevelConfig getLevelConfig() {
        return levelConfig;
    }

    public GunMagazineComponent getDefaultMagazine() {
        return defaultMagazine;
    }

    public GunFireModesComponent getDefaultFireModes() {
        return defaultFireModes;
    }

    public GunReloadComponent getDefaultReload() {
        return defaultReload;
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
        if (magazine == null || modes == null || stats == null || state == null) {
            return;
        }

        // Cancel reload if trying to fire
        if (state.isReloading()) {
            stack.set(RoaDataComponents.GUN_STATE.get(), state.cancelReload());
            player.getCooldowns().removeCooldown(this);
            player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.2f);
            // Animation is now handled by AnimatorController on client-side
            return;
        }

        if (!magazine.canFire()) {
            playEmptySound(level, player);
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

        // Fire animation - dispatch from server for efficiency (no extra packet needed!)
        // Get animation from AnimatorController with proper play behavior
        if (!level.isClientSide) {
            boolean isAiming = AdsStateManager.isPlayerAiming(); // This works on server too - synced via AimDownSightsPacket
            AnimationState fireState = 
                ((dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController)animatorController)
                    .getFireState(isAiming);
            AnimationCommand command = fireState.getAnimationCommand();
            
            // Create and dispatch AzCommand using the helper method
            command.createAzureCommand().sendForItem(player, stack);
        }

        // Update components
        stack.set(RoaDataComponents.GUN_MAGAZINE.get(), magazine.consume(1));

        if (currentMode.isContinuous() && currentMode != GunFireMode.AUTOMATIC_FIRE) {
            stack.set(RoaDataComponents.GUN_STATE.get(), state.fired(currentTime));
        } else {
            stack.set(RoaDataComponents.GUN_STATE.get(), new GunStateComponent(
                    false,      // isReloading
                    0,          // reloadStartTime
                    0,          // currentSequenceRounds
                    false,      // isUnholstering
                    0,          // unholsterStartTime
                    currentTime,// lastFireTime
                    0           // burstShotsFired
            ));
        }

        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        playFireSound(level, player, stack);

        // Apply recoil (CLIENT-SIDE via packet)
        if (player instanceof ServerPlayer serverPlayer) {
            applyRecoil(serverPlayer, stats);
        }
    }

    private void applyRecoil(ServerPlayer player, GunStatsComponent stats) {
        // Calculate recoil based on gun stats
        // The recoil values are now directly used without excessive multipliers
        // This makes stat changes much more noticeable!
        float basePitchRecoil = stats.recoilVertical();
        float baseYawRecoil = stats.recoilHorizontal();

        // Add subtle randomness for realistic feel (±10%)
        // Much less random variation so you can actually feel stat differences
        float randomVariation = 0.9f + player.getRandom().nextFloat() * 0.2f; // 0.9 to 1.1
        float pitchRecoil = basePitchRecoil * randomVariation;
        
        // Horizontal recoil: random direction but consistent magnitude
        float yawRecoil = baseYawRecoil * randomVariation * (player.getRandom().nextBoolean() ? -1 : 1);

        // Note: ADS recoil reduction is now handled client-side in RecoilManager
        // This allows for smoother, FOV-aware recoil application

        // Send recoil packet to client
        RoaPackets.sendToPlayer(player, new ApplyRecoilPacket(pitchRecoil, yawRecoil));
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
                true);

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
                1.0f + (player.getRandom().nextFloat() - 0.5f) * 0.2f);
    }

    private void playEmptySound(Level level, Player player) {
        player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.5f, 1.0f);
    }

    public void reload(Player player, ItemStack stack) {
        ensureComponents(stack);

        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
        GunStateComponent state = stack.get(RoaDataComponents.GUN_STATE.get());
        GunReloadComponent reloadConfig = stack.get(RoaDataComponents.GUN_RELOAD.get());

        if (magazine == null || state == null || reloadConfig == null) {
            return;
        }

        // If already reloading, ignore (button press won't interrupt)
        if (state.isReloading()) {
            return;
        }

        if (!GunHelper.canReload(stack, player, attachments)) {
            player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.0f);
            return;
        }

        // Start reload - don't actually reload yet!
        long currentTime = player.level().getGameTime();
        
        // Transition time for RELOAD_IN (0.2 seconds = 4 ticks)
        int transitionInTicks = Math.round(0.2f * 20.0f);
        
        // Determine reload behavior based on type
        if (reloadConfig.reloadType() == ReloadType.ONE_SHOT) {
            // One-shot reload: single animation, full magazine refill at the end
            // Adjust start time to account for RELOAD_IN transition
            long adjustedStartTime = currentTime + transitionInTicks;
            stack.set(RoaDataComponents.GUN_STATE.get(), state.withReloading(true, adjustedStartTime, 0));

            // Get reload duration from reload config
            int reloadTicks = reloadConfig.getReloadDurationTicks();
            
            // Set item cooldown for visual feedback (includes RELOAD_IN transition)
            player.getCooldowns().addCooldown(this, reloadTicks + transitionInTicks);
        } else {
            // Sequential reload: multiple animations, per-sequence refill
            int maxCapacity = magazine.getEffectiveCapacity(stack, attachments);
            int currentAmmo = magazine.currentAmmo();
            int roundsNeeded = maxCapacity - currentAmmo;
            
            // Calculate rounds for this sequence (min of sequence size and remaining)
            int roundsThisSequence = Math.min(reloadConfig.sequenceSize(), roundsNeeded);
            
            // Adjust start time to account for RELOAD_IN transition
            long adjustedStartTime = currentTime + transitionInTicks;
            stack.set(RoaDataComponents.GUN_STATE.get(), state.withReloading(true, adjustedStartTime, roundsThisSequence));

            // Cooldown is based on SEQUENCE duration + RELOAD_IN transition
            int sequenceTicks = reloadConfig.getReloadDurationTicks();
            player.getCooldowns().addCooldown(this, sequenceTicks + transitionInTicks);
        }

        // Play reload start sound
        player.playSound(SoundEvents.PISTON_EXTEND, 0.8f, 1.0f);

        // Reload animation - dispatch RELOAD_IN transition from server
        if (!player.level().isClientSide) {
            // Dispatch reload entrance transition
            AnimationCommand inCommand = ((dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController)animatorController)
                .getReloadInState().getAnimationCommand();
            inCommand.createAzureCommand().sendForItem(player, stack);
        }
    }
    
    /**
     * Determines the appropriate reload animation based on reload type and current state.
     * For sequential reloads, picks the animation matching the sequence size (e.g., weapon.reload.3round).
     */
    private AnimationState getReloadAnimationState(ItemStack stack, GunReloadComponent reloadConfig) {
        if (!(animatorController instanceof dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController gunController)) {
            return ((dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController)animatorController).getReloadState();
        }
        
        if (reloadConfig.reloadType() == ReloadType.ONE_SHOT) {
            return gunController.getReloadState();
        } else {
            // Sequential reload - get state for specific round count
            GunStateComponent state = stack.get(RoaDataComponents.GUN_STATE.get());
            int roundsThisSequence = state != null ? state.currentSequenceRounds() : reloadConfig.sequenceSize();
            return gunController.getReloadState(roundsThisSequence);
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
                new GunFireModesComponent(nextMode, List.copyOf(availableModes)));

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.roa.fire_mode_changed", nextMode.getDisplayName()),
                    true);
        }

        player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.5f, 1.5f);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId,
            boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!(entity instanceof Player player)) {
            return;
        }

        ensureComponents(stack);

        GunStateComponent state = stack.get(RoaDataComponents.GUN_STATE.get());
        if (state == null) {
            return;
        }

        // Cancel reload if item was switched away
        if (!isSelected && state.isReloading()) {
            stack.set(RoaDataComponents.GUN_STATE.get(), state.cancelReload());
            player.getCooldowns().removeCooldown(this);
            if (!level.isClientSide) {
                player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.2f);
            }
            return;
        }

        // Check reload completion (server-side only)
        if (isSelected && state.isReloading() && !level.isClientSide) {
            GunReloadComponent reloadConfig = stack.get(RoaDataComponents.GUN_RELOAD.get());
            if (reloadConfig == null) return;
            
            long currentTime = level.getGameTime();
            
            // Dispatch the main reload animation after RELOAD_IN transition completes
            // reloadStartTime is when the main reload should start (after RELOAD_IN)
            if (currentTime == state.reloadStartTime()) {
                AnimationState reloadState = getReloadAnimationState(stack, reloadConfig);
                AnimationCommand command = reloadState.getAnimationCommand();
                command.createAzureCommand().sendForItem(player, stack);
                
                if (DEBUG) {
                    LOGGER.info("[GunItem] Dispatching main reload animation: {}", reloadState.getAnimationName());
                }
            }
            
            if (reloadConfig.reloadType() == ReloadType.ONE_SHOT) {
                // One-shot reload: check if full reload is complete
                if (state.isReloadComplete(currentTime, reloadConfig.getReloadDurationTicks())) {
                    // Complete one-shot reload - refill entire magazine
                    GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
                    GunHelper.reload(stack, player, attachments);
                    stack.set(RoaDataComponents.GUN_STATE.get(), state.completeReload());
                    
                    // Dispatch RELOAD_OUT animation
                    if (animatorController instanceof dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController gunController) {
                        AnimationCommand command = gunController.getReloadOutState().getAnimationCommand();
                        command.createAzureCommand().sendForItem(player, stack);
                    }

                    player.playSound(SoundEvents.PISTON_CONTRACT, 0.8f, 1.0f);

                    GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
                    if (player instanceof ServerPlayer serverPlayer && magazine != null) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("gui.roa.reloaded",
                                        magazine.currentAmmo(),
                                        magazine.getEffectiveCapacity(stack, attachments)),
                                true);
                    }
                }
            } else {
                // Sequential reload: check if current sequence is complete
                int sequenceTicks = reloadConfig.getReloadDurationTicks();
                
                if (state.isReloadComplete(currentTime, sequenceTicks)) {
                    // Complete this sequence - add the rounds
                    GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
                    GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
                    
                    if (magazine == null) return;
                    
                    int roundsToAdd = state.currentSequenceRounds();
                    int maxCapacity = magazine.getEffectiveCapacity(stack, attachments);
                    int newAmmo = Math.min(magazine.currentAmmo() + roundsToAdd, maxCapacity);
                    
                    // Update magazine
                    stack.set(RoaDataComponents.GUN_MAGAZINE.get(), 
                        new GunMagazineComponent(newAmmo, magazine.baseCapacity(), magazine.ammoType()));
                    
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.2f);
                    
                    // Check if more rounds are needed
                    if (newAmmo < maxCapacity) {
                        // Continue sequential reload - calculate next sequence
                        int roundsNeeded = maxCapacity - newAmmo;
                        int roundsNextSequence = Math.min(reloadConfig.sequenceSize(), roundsNeeded);
                        
                        // Start next sequence
                        stack.set(RoaDataComponents.GUN_STATE.get(), state.startNextSequence(currentTime, roundsNextSequence));
                        
                        // Set cooldown for next sequence
                        player.getCooldowns().addCooldown(this, sequenceTicks);
                        
                        // Dispatch next reload animation
                        AnimationState reloadState = getReloadAnimationState(stack, reloadConfig);
                        AnimationCommand command = reloadState.getAnimationCommand();
                        command.createAzureCommand().sendForItem(player, stack);
                    } else {
                        // Fully reloaded!
                        stack.set(RoaDataComponents.GUN_STATE.get(), state.completeReload());
                        
                        // Dispatch RELOAD_OUT animation
                        if (animatorController instanceof dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController gunController) {
                            AnimationCommand command = gunController.getReloadOutState().getAnimationCommand();
                            command.createAzureCommand().sendForItem(player, stack);
                        }
                        
                        player.playSound(SoundEvents.PISTON_CONTRACT, 0.8f, 1.0f);
                        
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.displayClientMessage(
                                    Component.translatable("gui.roa.reloaded",
                                            newAmmo,
                                            maxCapacity),
                                    true);
                        }
                    }
                }
            }
        }
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
        if (!stack.has(RoaDataComponents.GUN_RELOAD.get())) {
            stack.set(RoaDataComponents.GUN_RELOAD.get(), defaultReload);
        }
        if (!stack.has(RoaDataComponents.GUN_STATE.get())) {
            stack.set(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT);
        }
        if (!stack.has(RoaDataComponents.GUN_LEVEL.get())) {
            stack.set(RoaDataComponents.GUN_LEVEL.get(), GunLevelComponent.DEFAULT);
        }
        if (!stack.has(RoaDataComponents.GUN_ATTACHMENTS.get())) {
            stack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // Ensure components exist before reading
        ensureComponents(stack);

        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunMagazineComponent magazine = stack.get(RoaDataComponents.GUN_MAGAZINE.get());
        GunFireModesComponent modes = stack.get(RoaDataComponents.GUN_FIRE_MODES.get());

        // Durability info
        if (stack.isDamageableItem()) {
            int durability = stack.getMaxDamage() - stack.getDamageValue();
            int maxDurability = stack.getMaxDamage();
            float durabilityPercent = (float) durability / maxDurability * 100;

            ChatFormatting durabilityColor = durabilityPercent > 50 ? ChatFormatting.GREEN
                    : durabilityPercent > 25 ? ChatFormatting.YELLOW : ChatFormatting.RED;

            tooltipComponents.add(Component.translatable(
                    "item.durability",
                    durability,
                    maxDurability).withStyle(durabilityColor));
        }

        // Level info
        GunLevelComponent level = stack.get(RoaDataComponents.GUN_LEVEL.get());
        if (level != null && levelConfig.maxLevel() > 1) {
            ChatFormatting levelColor = level.currentLevel() == levelConfig.maxLevel() 
                    ? ChatFormatting.GOLD 
                    : ChatFormatting.AQUA;
            tooltipComponents.add(Component.translatable(
                    "tooltip.roa.level",
                    level.currentLevel(),
                    levelConfig.maxLevel()).withStyle(levelColor));
        }

        // Magazine info
        int effectiveCapacity = magazine.getEffectiveCapacity(stack, attachments);
        tooltipComponents.add(Component.translatable(
                "tooltip.roa.ammo",
                magazine.currentAmmo(),
                effectiveCapacity).withStyle(magazine.currentAmmo() == 0 ? ChatFormatting.RED : ChatFormatting.GRAY));

        if (effectiveCapacity > magazine.baseCapacity()) {
            int bonus = effectiveCapacity - magazine.baseCapacity();
            int levelBonus = GunUtils.getLevelMagazineBonus(stack);
            int attachmentBonus = bonus - levelBonus;
            
            if (levelBonus > 0) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.roa.level_mag_bonus",
                        levelBonus).withStyle(ChatFormatting.AQUA));
            }
            if (attachmentBonus > 0) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.roa.extended_mag_bonus",
                        attachmentBonus).withStyle(ChatFormatting.GREEN));
            }
        }

        // Fire mode info
        tooltipComponents.add(Component.translatable(
                "tooltip.roa.fire_mode",
                modes.currentMode().getDisplayName()).withStyle(ChatFormatting.GRAY));

        // Show reload status
        GunStateComponent state = stack.get(RoaDataComponents.GUN_STATE.get());
        if (state != null && state.isReloading()) {
            GunReloadComponent reloadConfig = stack.get(RoaDataComponents.GUN_RELOAD.get());
            if (reloadConfig != null) {
                long currentTime = net.minecraft.client.Minecraft.getInstance().level != null
                        ? net.minecraft.client.Minecraft.getInstance().level.getGameTime()
                        : 0;
                float progress = state.getReloadProgress(currentTime, reloadConfig.getReloadDurationTicks());

                tooltipComponents.add(Component.translatable(
                        "tooltip.roa.reloading",
                        String.format("%.0f%%", progress * 100)).withStyle(ChatFormatting.YELLOW));
            }
        }

        tooltipComponents.add(Component.empty());

        // Attachments
        if (attachments.hasAnyAttachments()) {
            tooltipComponents.add(Component.translatable("tooltip.roa.attachments")
                    .withStyle(ChatFormatting.GOLD));

            Map<AttachmentSlot, ItemStack> allAttachments = attachments.getAllAttachments();
            for (Map.Entry<AttachmentSlot, ItemStack> entry : allAttachments.entrySet()) {
                tooltipComponents.add(Component.literal("  ")
                        .append(Component.translatable(
                                "tooltip.roa.attachment_slot." + entry.getKey().getSerializedName()))
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
            addStatLine(tooltipComponents, "fire_rate", (float) baseStats.fireRate(),
                    (float) effectiveStats.fireRate());
            addStatLine(tooltipComponents, "range", baseStats.range(), effectiveStats.range());
            addStatLine(tooltipComponents, "recoil", baseStats.recoilVertical(), effectiveStats.recoilVertical());
            addStatLine(tooltipComponents, "ads_speed", baseStats.adsSpeed(), effectiveStats.adsSpeed());
            
            // Show reload speed from GunReloadComponent
            GunReloadComponent reloadConfig = stack.get(RoaDataComponents.GUN_RELOAD.get());
            if (reloadConfig != null) {
                tooltipComponents.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.roa.stat.reload_speed",
                                String.format("%.1f", reloadConfig.reloadDuration())))
                        .withStyle(ChatFormatting.GRAY));
            }

            if (effectiveStats.armorPenetration() > 0) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.roa.armor_penetration",
                        String.format("%.0f%%", effectiveStats.armorPenetration() * 100))
                        .withStyle(ChatFormatting.YELLOW));
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

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        // Get attachments before the item is destroyed
        ItemStack stack = itemEntity.getItem();
        GunAttachmentsComponent attachments = stack.get(RoaDataComponents.GUN_ATTACHMENTS.get());

        if (attachments != null && attachments.hasAnyAttachments()) {
            Level level = itemEntity.level();
            Vec3 pos = itemEntity.position();

            // Drop each attachment as a separate item entity
            Map<AttachmentSlot, ItemStack> allAttachments = attachments.getAllAttachments();
            for (ItemStack attachmentStack : allAttachments.values()) {
                if (!attachmentStack.isEmpty()) {
                    ItemEntity droppedAttachment = new ItemEntity(
                            level,
                            pos.x,
                            pos.y,
                            pos.z,
                            attachmentStack.copy());

                    // Add some random velocity for scatter effect
                    droppedAttachment.setDeltaMovement(
                            (level.random.nextFloat() - 0.5) * 0.2,
                            0.2,
                            (level.random.nextFloat() - 0.5) * 0.2);

                    level.addFreshEntity(droppedAttachment);
                }
            }

            // Play sound
            level.playSound(
                    null,
                    pos.x, pos.y, pos.z,
                    SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f);
        }
    }
}