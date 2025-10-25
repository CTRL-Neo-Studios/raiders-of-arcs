package dev.ctrlneo.roa.foundation.data.components;

import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import java.util.List;

public record GunFireModesComponent(GunFireMode currentMode, List<GunFireMode> availableModes) {

    public static GunFireModesComponent of(GunFireMode currentMode, GunFireMode... modes) {
        return new GunFireModesComponent(currentMode, List.of(modes));
    }
}