package dev.ctrlneo.roa.foundation.animations.controller.core;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehavior;

/**
 * Animation command to be sent to the server/dispatcher.
 */
public record AnimationCommand(String animationName, AzPlayBehavior playBehavior, int durationTicks) {
    public AzCommand createAzureCommand(String controllerName) {
        return AzCommand.create(controllerName, this.animationName(), this.playBehavior());
    }

    public AzCommand createAzureCommand() {
        return this.createAzureCommand("base_controller");
    }
}
