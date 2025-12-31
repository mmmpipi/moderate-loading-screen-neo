package dev.enjarai.mls.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.enjarai.mls.DrawContextWrapper;
import dev.enjarai.mls.config.ModConfig;
import dev.enjarai.mls.screens.LoadingScreen;
import dev.enjarai.mls.screens.SnowFlakesScreen;
import dev.enjarai.mls.screens.StackingScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.earlydisplay.ColourScheme;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(NeoForgeLoadingOverlay.class)
public abstract class ForgeLoadingOverlayMixin extends Overlay {
    ///*
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private DisplayWindow displayWindow;
    @Shadow private long fadeOutStart;
    @Unique
    private LoadingScreen moderateLoadingScreen$loadingScreen;
    @Unique
    private final float moderateLoadingScreen$loadingScreenAlpha = ModConfig.iconOpacity.get()/100F;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void moderateLoadingScreen$constructor(Minecraft mc, ReloadInstance reloader, Consumer errorConsumer, DisplayWindow displayWindow, CallbackInfo ci) {
        moderateLoadingScreen$loadingScreen = switch (ModConfig.screenType.get()) {
            case SNOWFLAKES -> new SnowFlakesScreen(this.minecraft,true);
            case STACKING -> new StackingScreen(this.minecraft,true);
        };
//        moderateLoadingScreen$loadingScreen.setWidthSupplier(()->this.minecraft.getWindow().getWidth());
//        moderateLoadingScreen$loadingScreen.setHeightSupplier(()->this.minecraft.getWindow().getHeight());
        moderateLoadingScreen$loadingScreen.setWidthSupplier(()->this.displayWindow.context().width());
        moderateLoadingScreen$loadingScreen.setHeightSupplier(()->this.displayWindow.context().height());
        moderateLoadingScreen$loadingScreen.setScaleFix(()-> (float) this.minecraft.getWindow().getHeight()/(float) this.displayWindow.context().height());
    }

//    @ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/neoforged/fml/earlydisplay/ColourScheme;background()Lnet/neoforged/fml/earlydisplay/ColourScheme$Colour;"))
//    private ColourScheme.Colour moderateLoadingScreen$changeColorGl(ColourScheme.Colour constant) {
//        Color color = Color.getColor("logo", ModConfig.backgroundColor.get());
//        return new ColourScheme.Colour(color.getRed(), color.getGreen(), color.getBlue());
//    }

//    @ModifyVariable(method = "render", at = @At(value = "STORE",ordinal = 0))
//    private ColourScheme.Colour moderateLoadingScreen$changeColorGl(ColourScheme.Colour constant) {
//        Color color = Color.getColor("logo", ModConfig.backgroundColor.get());
//        return new ColourScheme.Colour(color.getRed(), color.getGreen(), color.getBlue());
//    }

//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", ordinal = 1, shift = At.Shift.AFTER))
//    private void moderateLoadingScreen$renderPatches(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
//        long k = Util.getMillis();
//        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
//        moderateLoadingScreen$loadingScreen.renderPatches(new DrawContextWrapper(graphics), partialTick, f >= 1.0f);
//    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void moderateLoadingScreen$renderPatches(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        long k = Util.getMillis();
        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, moderateLoadingScreen$loadingScreenAlpha);
        moderateLoadingScreen$loadingScreen.renderPatches(new DrawContextWrapper(graphics), partialTick, f >= 1.0f);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }


//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V"), locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void moderateLoadingScreen$resetTransparency(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci, long millis, float fadeouttimer, float fade, ColourScheme.Colour colour, int fbWidth, int fbHeight, int twidth, int theight, float wscale, float hscale, float scale, float wleft, float wtop, float wright, float wbottom, BufferBuilder bufferbuilder) {
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fade);
//    }

//    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0), index = 3)
//    private float moderateLoadingScreen$modifyLogoTransparency(float fade) {
//        return fade * ModConfig.logoOpacity.get() / 100f;
//    }

//    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", ordinal = 1), index = 3)
//    private float moderateLoadingScreen$modifyLogoTransparency(float original) {
//        return original * ModConfig.logoOpacity.get() / 100f;
//    }
}
