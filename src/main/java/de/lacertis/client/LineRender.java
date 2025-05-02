package de.lacertis.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class LineRender {
    public void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::renderLine);
    }

    private void renderLine(WorldRenderContext context) {
        VertexConsumerProvider provider = context.consumers();
        if (provider == null) return;

        MatrixStack ms = context.matrixStack();
        Vec3d camPos = context.camera().getPos();

        Vec3d start = new Vec3d(100.5, 70.5, 100.5).subtract(camPos);
        Vec3d end   = new Vec3d( 90.5, 70.5,  90.5).subtract(camPos);

        VertexConsumer consumer = provider.getBuffer(RenderLayer.getLines());
        Matrix4f mat = ms.peek().getPositionMatrix();

        consumer
                .vertex(mat, (float)start.x, (float)start.y, (float)start.z)
                .color(0, 0, 255, 255)
                .normal(ms.peek(), 0f, 1f, 0f);

        consumer
                .vertex(mat, (float)end.x,   (float)end.y,   (float)end.z)
                .color(0, 0, 255, 255)
                .normal(ms.peek(), 0f, 1f, 0f);
    }
}
