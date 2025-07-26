package de.lacertis.loreutils;

import com.mojang.blaze3d.systems.RenderSystem;
import de.lacertis.loreutils.config.ModConfig;
import de.lacertis.loreutils.config.ConfigTranslator;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class EspRender {
    private static final List<BlockPos> highlightPositions = new ArrayList<>();
    private static boolean initialized = false;

    public static void registerPosition(BlockPos pos) {
        highlightPositions.add(pos);
    }

    public static void unregisterPosition(BlockPos pos) {
        highlightPositions.remove(pos);
    }

    public static void unregisterAllPositions() {
        highlightPositions.clear();
    }

    public static void init() {
        if (initialized) return;
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            double camX = client.getEntityRenderDispatcher().camera.getPos().x;
            double camY = client.getEntityRenderDispatcher().camera.getPos().y;
            double camZ = client.getEntityRenderDispatcher().camera.getPos().z;

            MatrixStack matrices = ctx.matrixStack();
            if (matrices == null) return;

            VertexConsumerProvider consumers = ctx.consumers();
            RenderSystem.disableDepthTest();
            matrices.push();
            matrices.translate(-camX, -camY, -camZ);

            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            int color = config.primaryColor;
            float[] rgb = ConfigTranslator.translate(color);
            float alpha = ConfigTranslator.translateAlpha(config.alphaPercentage);
            double expand = 0.002;
            for (BlockPos pos : highlightPositions) {
                Box box = new Box(pos).expand(expand, expand, expand);
                DebugRenderer.drawBox(matrices, consumers, box, rgb[0], rgb[1], rgb[2], alpha);
            }

            matrices.pop();
            RenderSystem.enableDepthTest();
        });
        initialized = true;
    }

    public static void uninit() {
        highlightPositions.clear();
        initialized = false;
    }
}