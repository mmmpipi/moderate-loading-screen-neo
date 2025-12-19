package dev.enjarai.mls.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.MatrixUtil;
import dev.enjarai.mls.DrawContextWrapper;
import dev.enjarai.mls.ModerateLoadingScreen;
import dev.enjarai.mls.config.ModConfig;
import dev.enjarai.mls.config.Orientation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public abstract class LoadingScreen {
    protected final int patchSize = ModConfig.iconSize.get();
    protected final Orientation orientation;
    protected final Minecraft client;
    protected final ArrayList<ResourceLocation> icons;
    protected final Random random = new Random();
    protected final ArrayList<Patch> patches = new ArrayList<>();
    protected double patchTimer = 0f;
    protected Supplier<Integer> widthSupplier;
    protected Supplier<Integer> heightSupplier;
    //protected boolean tater = ModerateLoadingScreen.CONFIG.showTater;
    protected boolean modsOnlyOnce = ModConfig.modsOnlyOnce.get();
    public Optional<Supplier<Float>> scaleFix = Optional.empty();
    protected int offsetX = 0;
    protected int offsetY = 0;

    public LoadingScreen(Minecraft client, boolean isEarlyLoad) {
        this.client = client;
        this.widthSupplier = ()->client.getWindow().getGuiScaledWidth();
        this.heightSupplier = ()->client.getWindow().getGuiScaledHeight();
        if (isEarlyLoad){
            var temp = ModConfig.orientation.get();
            if (temp == Orientation.DOWN){
                this.orientation = Orientation.UP;
            }else if (temp == Orientation.UP){
                this.orientation = Orientation.DOWN;
            }else if (temp == Orientation.LEFT){
                this.orientation = Orientation.RIGHT;
            }else {
                this.orientation = Orientation.LEFT;
            }
        }else{
            this.orientation = ModConfig.orientation.get();
        }
        icons = ModerateLoadingScreen.getIcon();
    }
    public void setOffset(int x, int y){
        this.offsetX = x;
        this.offsetY = y;
    }
    public void setWidthSupplier(Supplier<Integer> supplier){
        this.widthSupplier = supplier;
    }
    public void setHeightSupplier(Supplier<Integer> supplier){
        this.heightSupplier = supplier;
    }

    public abstract void createPatch(ResourceLocation texture);

    protected ResourceLocation getNextTexture() {
        // Summon the holy tater if enabled
        /*
        if (tater) {
            tater = false;
            return ModerateLoadingScreen.id("textures/gui/tiny_potato.png");
        }
         */

        return icons.get(random.nextInt(icons.size()));
    }

    public void updatePatches(float delta, boolean ending) {
        processPhysics(delta, ending);

        if (!icons.isEmpty()) {
            patchTimer -= delta;

            if (patchTimer < 0f && !ending) {
                ResourceLocation icon = getNextTexture();

                if (modsOnlyOnce) {
                    icons.remove(icon);
                }
                createPatch(icon);

                patchTimer = getPatchTimer();
            }
        }
    }

    protected double getPatchTimer() {
        return random.nextFloat();
    }

    protected double getOffsetX() {
        return offsetX;
    }

    protected double getOffsetY() {
        return offsetY;
    }

    protected int getScreenWidth() {
        return orientation.switchAxes ? heightSupplier.get() : widthSupplier.get();
    }

    protected int getScreenHeight() {
        return orientation.switchAxes ? widthSupplier.get() : heightSupplier.get();
    }

    protected void processPhysics(float delta, boolean ending) {
        for (Patch patch : patches) {
            if (ending)
                patch.fallSpeed *= 1.0 + delta / 3;

            patch.update(delta);
        }
    }

    public void renderPatches(DrawContextWrapper wrapper, float delta, boolean ending) {
        // spike prevention
        if (delta < 2.0f)
            updatePatches(delta, ending);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (Patch patch : patches) {
            RenderSystem.setShaderTexture(0, patch.texture);
            patch.render(wrapper, getOffsetX(), getOffsetY());
        }
    }

    protected class Patch {
        protected double x, y, rot;
        protected final ResourceLocation texture;

        protected final double horizontal, rotSpeed;
        protected final double scale;

        public double fallSpeed;

        protected final int patchSize;

        public Patch(double x, double y, double rot, double horizontal, double fallSpeed, double rotSpeed, double scale, ResourceLocation texture, int patchSize) {
            this.x = x;
            this.y = y;
            this.rot = rot;

            this.horizontal = horizontal;
            this.fallSpeed = fallSpeed;
            this.rotSpeed = rotSpeed;

            this.scale = scale;

            this.texture = texture;

            this.patchSize = patchSize;
        }

        public void update(float delta) {
            x += horizontal * delta;
            y += fallSpeed * delta;

            rot += rotSpeed * delta;
        }

        public void render(DrawContextWrapper wrapper, double offsetX, double offsetY) {
            PoseStack matrices = wrapper.matrices();
            matrices.pushPose();
            float fixScale = scaleFix.map(Supplier::get).orElse(1.0F);
            if (orientation.switchAxes) {
                matrices.translate(
                        perhapsInvert((y + offsetY), getScreenHeight())* fixScale,
                        perhapsInvert((x + offsetX), getScreenWidth())* fixScale,
                        0
                );
            } else {
                matrices.translate(
                        perhapsInvert((x + offsetX), getScreenWidth())* fixScale,
                        perhapsInvert((y + offsetY), getScreenHeight())* fixScale,
                        0
                );
            }

            Matrix4f matrix = matrices.last().pose();
            MatrixUtil.mulComponentWise(matrix.rotate((float) rot * 0.017453292F, 0, 0, 1), (float) scale * fixScale);

            double x1 = -patchSize / 2d * fixScale;
            double y1 = -patchSize / 2d * fixScale;
            double x2 = patchSize / 2d * fixScale;
            double y2 = patchSize / 2d * fixScale;

            wrapper.drawTexturedQuad(texture, (int) x1, (int) x2, (int) y1, (int) y2);
            matrices.popPose();
        }

        private double perhapsInvert(double value, int fullSize) {
            return orientation.reverseAxes ? fullSize - value : value;
        }
    }
}
