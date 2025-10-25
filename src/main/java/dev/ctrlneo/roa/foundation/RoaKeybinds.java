package dev.ctrlneo.roa.foundation;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ctrlneo.roa.Roa;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Roa.MODID, value = Dist.CLIENT)
public class RoaKeybinds {

    public static final String CATEGORY = "key.categories.roa";

    // Keybindings
    public static KeyMapping RELOAD;
    public static KeyMapping FIRE_MODE_CYCLE;
    public static KeyMapping AIM_DOWN_SIGHTS;
    public static KeyMapping OPEN_ATTACHMENTS;  // NEW!

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        RELOAD = new KeyMapping(
                "key.roa.reload",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        );

        FIRE_MODE_CYCLE = new KeyMapping(
                "key.roa.fire_mode_cycle",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        );

        AIM_DOWN_SIGHTS = new KeyMapping(
                "key.roa.aim_down_sights",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.MOUSE,
                InputConstants.MOUSE_BUTTON_RIGHT,
                CATEGORY
        );

        OPEN_ATTACHMENTS = new KeyMapping(
                "key.roa.open_attachments",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                CATEGORY
        );

        event.register(RELOAD);
        event.register(FIRE_MODE_CYCLE);
        event.register(AIM_DOWN_SIGHTS);
        event.register(OPEN_ATTACHMENTS);
    }
}