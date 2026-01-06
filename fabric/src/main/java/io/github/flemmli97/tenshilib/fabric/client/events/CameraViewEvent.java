package io.github.flemmli97.tenshilib.fabric.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;

public interface CameraViewEvent {

    /**
     * Allows the modification of the camera view angles
     */
    Event<CameraViewEvent> EVENT = EventFactory.createArrayBacked(CameraViewEvent.class,
            (listeners) -> handler -> {
                for (CameraViewEvent event : listeners) {
                    event.handle(handler);
                }
            }
    );

    void handle(Instance angles);

    class Instance {

        private final Camera camera;
        private final float partialTick;
        private float yaw;
        private float pitch;
        private float roll;

        public Instance(Camera camera, float partialTick, float yaw, float pitch, float roll) {
            this.camera = camera;
            this.partialTick = partialTick;
            this.setYaw(yaw);
            this.setPitch(pitch);
            this.setRoll(roll);
        }

        public Camera getCamera() {
            return this.camera;
        }

        public float getPartialTicks() {
            return this.partialTick;
        }

        public float getYaw() {
            return this.yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return this.pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public float getRoll() {
            return this.roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }
    }
}
