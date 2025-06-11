package io.github.flemmli97.tenshilib.mixinhelper;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Map;

public interface EntityRenderDispatcherAccess {

    static Matrix4f toWorldMatrix(Matrix4f localMatrix) {
        Matrix4f preMatrix = ((EntityRenderDispatcherAccess) Minecraft.getInstance().getEntityRenderDispatcher())
                .tenshilib$getPreRenderMatrix();
        if (preMatrix == null) {
            TenshiLib.LOGGER.error("Trying to get world position from wrong/non render context!");
            return null;
        }
        preMatrix.invert();
        preMatrix.mul(localMatrix);
        return preMatrix;
    }

    static Vec3 toWorldPosition(Entity entity, Matrix4f localMatrix) {
        Matrix4f preMatrix = toWorldMatrix(localMatrix);
        if (preMatrix == null) {
            return entity.position();
        }
        Vector4f offset = preMatrix.transform(new Vector4f(0, 0, 0, 1));
        return entity.position().add(offset.x(), offset.y(), offset.z());
    }

    static Map<EntityType<?>, EntityRenderer<?>> getAllRenders() {
        return ((EntityRenderDispatcherAccess) Minecraft.getInstance().getEntityRenderDispatcher()).tenshilib$getRenderers();
    }

    Map<EntityType<?>, EntityRenderer<?>> tenshilib$getRenderers();

    Matrix4f tenshilib$getPreRenderMatrix();
}
