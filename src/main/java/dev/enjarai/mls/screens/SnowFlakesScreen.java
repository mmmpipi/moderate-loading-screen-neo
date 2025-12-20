package dev.enjarai.mls.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class SnowFlakesScreen extends LoadingScreen {


    public SnowFlakesScreen(Minecraft client, boolean isEarlyLoad) {
        super(client, isEarlyLoad);
    }

    @Override
  public void createPatch(ResourceLocation texture) {
      patches.add(new Patch(
              random.nextDouble() * (getScreenWidth() + patchSize),
              -patchSize, 0,
              (random.nextDouble() - 0.5) * 0.6,
              random.nextDouble() * 3.0 + 1.0,
              (random.nextDouble() - 0.5) * 6.0,
              random.nextDouble() / 2 + 0.5,
              texture, patchSize
      ));
  }
}
