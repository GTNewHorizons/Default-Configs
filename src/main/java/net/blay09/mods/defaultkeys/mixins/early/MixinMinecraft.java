package net.blay09.mods.defaultkeys.mixins.early;

import net.blay09.mods.defaultkeys.DefaultKeys;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "startGame", at = @At("HEAD"))
    private void injectStartGame(CallbackInfo ci) {
        DefaultKeys.preStartGame();
    }
}
