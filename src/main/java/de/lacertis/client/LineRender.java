package de.lacertis.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LineRender {
    private static final List<Line> LINES = new CopyOnWriteArrayList<>();

    public void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onRender);
    }

    public static void uninit() {
        LINES.clear();
    }

    public static void registerLine(Vec3d start, Vec3d end, int r, int g, int b, int a) {
        LINES.add(new Line(start, end, r, g, b, a));
    }

    public static void unregisterLine(Line line) {
        LINES.remove(line);
    }

    public static void unregisterAllLines() {
        LINES.clear();
    }

    private void onRender(WorldRenderContext context) {
        VertexConsumerProvider provider = context.consumers();
        if (provider == null) return;

        MatrixStack ms = context.matrixStack();
        Vec3d camPos = context.camera().getPos();
        VertexConsumer consumer = provider.getBuffer(RenderLayer.getLines());
        Matrix4f mat = ms.peek().getPositionMatrix();

        for (Line line : LINES) {
            Vec3d s = line.start.subtract(camPos);
            Vec3d e = line.end.subtract(camPos);

            consumer
                    .vertex(mat, (float) s.x, (float) s.y, (float) s.z)
                    .color(line.r, line.g, line.b, line.a)
                    .normal(ms.peek(), 0f, 1f, 0f);

            consumer
                    .vertex(mat, (float) e.x, (float) e.y, (float) e.z)
                    .color(line.r, line.g, line.b, line.a)
                    .normal(ms.peek(), 0f, 1f, 0f);
        }
    }

    public static class Line {
        public final Vec3d start, end;
        public final int r, g, b, a;

        public Line(Vec3d start, Vec3d end, int r, int g, int b, int a) {
            this.start = start;
            this.end = end;
            this.r = r; this.g = g; this.b = b; this.a = a;
        }
    }
}