package dev.enjarai.mls.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.enjarai.mls.DrawContextWrapper;
import dev.enjarai.mls.config.ModConfig;
import dev.enjarai.mls.screens.LoadingScreen;
import dev.enjarai.mls.screens.SnowFlakesScreen;
import dev.enjarai.mls.screens.StackingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public abstract class SplashOverlayMixin extends Overlay {
    @Shadow @Final private Minecraft minecraft;
    @Unique
    private LoadingScreen moderateLoadingScreen$loadingScreen;

    @Shadow
    private static int replaceAlpha(int color, int alpha) {
        throw new UnsupportedOperationException("Shadowed method somehow called outside mixin. Exorcise your computer.");
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void moderateLoadingScreen$constructor(Minecraft client, ReloadInstance monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
        moderateLoadingScreen$loadingScreen = switch (ModConfig.screenType.get()) {
            case SNOWFLAKES -> new SnowFlakesScreen(this.minecraft,false);
            case STACKING -> new StackingScreen(this.minecraft,false);
        };
    }

    // 背景颜色填充
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"), index = 5)
    private int moderateLoadingScreen$changeColor(int in) {
        if (this.minecraft.options.darkMojangStudiosBackground().get()) {
            return in;
        }
        return replaceAlpha(Color.getColor("background", ModConfig.backgroundColor.get()).getRGB(), in >> 24); // Use existing transparency
    }

    // 背景颜色变量填充
    @ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/function/IntSupplier;getAsInt()I", ordinal = 2), ordinal = 4)
    private int moderateLoadingScreen$changeColorGl(int in) {
        return this.minecraft.options.darkMojangStudiosBackground().get() ? in : Color.getColor("logo", ModConfig.backgroundColor.get()).getRGB();
    }

    // logo前注入图标
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void moderateLoadingScreen$renderPatches(net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, long l, float f) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        moderateLoadingScreen$loadingScreen.renderPatches(new DrawContextWrapper(context), delta, f >= 1.0f);
    }

    // logo透明度
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setColor(FFFF)V"), index = 3)
    private float moderateLoadingScreen$modifyLogoTransparency(float original) {
        return original * ModConfig.logoOpacity.get() / 100f;
    }

    // 渲染后重置透明度
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"), locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
    private void moderateLoadingScreen$resetTransparency(net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci,
                                                         int i, int j, long l, float f, float g, float h) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, h);
    }

    // 加载条透明度修改
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"), index = 5)
    private float moderateLoadingScreen$modifyBarTransparency(float original) {
        return original * ModConfig.barOpacity.get() / 100f;
    }
}
