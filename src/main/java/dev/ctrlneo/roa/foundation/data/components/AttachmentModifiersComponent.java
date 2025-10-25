package dev.ctrlneo.roa.foundation.data.components;

/**
 * Describes stat modifications provided by an attachment.
 * Additive bonuses are added to base values.
 * Multipliers: 1.0 = no change, 1.1 = +10%, 0.9 = -10%
 */
public record AttachmentModifiersComponent(
        // Additive bonuses
        float damageBonus,
        float rangeBonus,
        int magazineCapacityBonus,
        float armorPenetrationBonus,

        // Multiplicative modifiers
        float accuracyMultiplier,
        float recoilMultiplier,
        float adsSpeedMultiplier,
        float reloadSpeedMultiplier
) {
    public static final AttachmentModifiersComponent EMPTY = new AttachmentModifiersComponent(
            0.0f, 0.0f, 0, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    );

    /**
     * Combines multiple modifiers (for stacking multiple attachments)
     */
    public AttachmentModifiersComponent combine(AttachmentModifiersComponent other) {
        return new AttachmentModifiersComponent(
                this.damageBonus + other.damageBonus,
                this.rangeBonus + other.rangeBonus,
                this.magazineCapacityBonus + other.magazineCapacityBonus,
                this.armorPenetrationBonus + other.armorPenetrationBonus,
                this.accuracyMultiplier * other.accuracyMultiplier,
                this.recoilMultiplier * other.recoilMultiplier,
                this.adsSpeedMultiplier * other.adsSpeedMultiplier,
                this.reloadSpeedMultiplier * other.reloadSpeedMultiplier
        );
    }
}