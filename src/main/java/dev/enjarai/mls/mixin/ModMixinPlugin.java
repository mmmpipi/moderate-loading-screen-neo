package dev.enjarai.mls.mixin;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLPaths;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (Dist.CLIENT.isDedicatedServer())return false;
        if (mixinClassName.equals("dev.enjarai.mls.mixin.ForgeLoadingOverlayMixin")){
            boolean enable = true;
            var config = FileConfig.of(FMLPaths.CONFIGDIR.get().resolve("mls-client.toml"));
            try (config){
                config.load();
                Boolean value = config.get(List.of("Moderate Loading Screen","earlyWindowOverlay"));
                if (value!=null){
                    enable = value;
                }
            }catch (ClassCastException e){
                LogUtils.getLogger().warn("Cannot read Config");
            }
            return enable;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
