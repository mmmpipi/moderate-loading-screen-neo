package dev.enjarai.mls;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import dev.enjarai.mls.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;


@Mod(value = ModerateLoadingScreen.MODID, dist = Dist.CLIENT)
public class ModerateLoadingScreen {
    public static final String MODID = "mls";
    private static final Logger logger = LogUtils.getLogger();
    public ModerateLoadingScreen(IEventBus modEventBus, ModContainer container){
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,ModConfig.SPEC);
        //container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static ArrayList<ResourceLocation> cachedIcons = null;

    public static ArrayList<ResourceLocation> getIcon(){
        if (ModConfig.modsOnlyOnce.get()&&cachedIcons!=null)return cachedIcons;
        ArrayList<ResourceLocation> result = new ArrayList<>();

        for (IModInfo modInfo : ModList.get().getMods()) {
            String modId = modInfo.getModId();

            if(ModConfig.modIdBlacklist.get().stream().anyMatch(Predicate.isEqual(modId))) continue;

            ModContainer modContainer = ModList.get().getModContainerById(modId).orElse(null);
            if (modContainer == null) continue;
            String logoFile = modContainer.getModInfo().getLogoFile().orElse(null);
            if (logoFile == null) continue;

            TextureManager tm = Minecraft.getInstance().getTextureManager();
            final Pack.ResourcesSupplier resourcePack = ResourcePackLoader.getPackFor(modInfo.getModId()).orElse(ResourcePackLoader.getPackFor("neoforge").orElseThrow(()->new RuntimeException("Can't find forge, WHAT!")));

            try (PackResources packResources = resourcePack.openPrimary(new PackLocationInfo("mod/" + modId, Component.empty(), PackSource.BUILT_IN, Optional.empty()))) {
                NativeImage logo;
                IoSupplier<InputStream> logoResource = packResources.getRootResource(logoFile.split("[/\\\\]"));
                if (logoResource != null) {
                    logo = NativeImage.read(logoResource.get());
                    result.add(tm.register("modlogo", new DynamicTexture(logo) {
                        @Override
                        public void upload() {
                            this.bind();
                            NativeImage td = this.getPixels();
                            this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), modInfo.getLogoBlur(), false, false, false);
                        }
                    }));
                }
            } catch (IllegalArgumentException | IOException ignored) {
            }
        }
        cachedIcons = result;
        return result;
    }

    /*
    private static NativeImageBackedTexture getIconTexture(ModContainer iconSource, String iconPath) {
        try {
            Path path = iconSource.getPath(iconPath);
            try (InputStream inputStream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
                Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
                return new NativeImageBackedTexture(image);
            }

        } catch (Throwable t) {
            return null;
        }
    }
     */


    // https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
    private static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder("^");
        for(int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*' -> out.append(".*");
                case '?' -> out.append('.');
                case '.' -> out.append("\\.");
                case '\\' -> out.append("\\\\");
                default -> out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }

    public static ResourceLocation id(String path) {
        /*? if >=1.21 {*//*
        return Identifier.of(MODID, path);
        *//*?} else {*/
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
        /*?} */
    }
}
