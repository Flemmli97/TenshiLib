package io.github.flemmli97.tenshilib.client.model;

import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.mixin.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

public class MolangQueries {

    // Queries according to https://learn.microsoft.com/en-us/minecraft/creator/reference/content/molangreference/examples/molangconcepts/queryfunctions?view=minecraft-bedrock-stable
    // At least all sensible ones in this context...
    public static void applyEntityQueriesTo(VariableMap variables, Set<String> used, @Nullable Entity entity, float partialTicks) {
        QueryContext ctx = new QueryContext(variables, used, Minecraft.getInstance(), partialTicks);
        applyEntityQueriesTo(ctx, entity);
    }

    private static void applyEntityQueriesTo(QueryContext context, @Nullable Entity entity) {
        setQuery(context, "query.actor_count", ctx -> ((LevelRendererAccessor) ctx.mc().levelRenderer).getRenderedEntities());
        setQuery(context, "query.cardinal_facing", ctx -> ctx.mc().player.getDirection().ordinal());
        setQuery(context, "query.cardinal_player_facing", ctx -> ctx.mc().player.getDirection().get2DDataValue() == -1 ? 6 : ctx.mc().player.getDirection().ordinal());
        setQuery(context, "query.cardinal_facing_2d", ctx -> ctx.mc().player.getDirection().ordinal());
        setQuery(context, "query.client_max_render_distance", ctx -> ctx.mc().options.renderDistance().get());
        setQuery(context, "query.day", ctx -> ctx.mc().level.getDayTime() / 24000L);
        setQuery(context, "query.delta_time", ctx -> ctx.partialTicks());
        setQuery(context, "query.has_cape", ctx -> ctx.mc().player != null && ctx.mc().player.getSkin().capeTexture() != null ? 1 : 0);
        setQuery(context, "query.is_first_person", ctx -> ctx.mc().options.getCameraType().isFirstPerson() ? 1 : 0);
        setQuery(context, "query.moon_brightness", ctx -> ctx.mc().level.getMoonBrightness());
        setQuery(context, "query.moon_phase", ctx -> ctx.mc().level.getMoonPhase());
        setQuery(context, "query.time_stamp", ctx -> ctx.mc().level.getGameTime());

        if (entity != null) {
            setQuery(context, "query.body_x_rotation", entity, (e, ctx) -> e.getViewXRot(ctx.partialTicks()));
            setQuery(context, "query.body_y_rotation", entity, (e, ctx) -> e instanceof LivingEntity living ? Mth.lerp(ctx.partialTicks(), living.yBodyRotO, living.yBodyRot) : entity.getViewYRot(ctx.partialTicks()));
            setQuery(context, "query.distance_from_camera", entity, (e, ctx) -> ctx.mc.gameRenderer.getMainCamera().getPosition().distanceTo(e.position()));
            setQuery(context, "query.get_e_info_id", entity, Entity::getId);
            setQuery(context, "query.ground_speed", entity, e -> e.getDeltaMovement().horizontalDistance());
            setQuery(context, "query.has_collision", entity, e -> e.noPhysics ? 0 : 1);
            setQuery(context, "query.has_gravity", entity, e -> e.isNoGravity() ? 0 : 1);
            setQuery(context, "query.has_player_rider", entity, e -> e.countPlayerPassengers() > 0 ? 1 : 0);
            setQuery(context, "query.has_rider", entity, e -> e.getPassengers().isEmpty() ? 0 : 1);
            setQuery(context, "query.invulnerable_ticks", entity, (e, ctx) -> e.invulnerableTime == 0 ? 0 : Math.max(0, e.invulnerableTime - ctx.partialTicks()));
            setQuery(context, "query.is_alive", entity, e -> e.isAlive() ? 1 : 0);
            setQuery(context, "query.is_breathing", entity, e -> e.getAirSupply() >= e.getMaxAirSupply() ? 1 : 0);
            setQuery(context, "query.is_croaking", entity, e -> e.getPose() == Pose.CROAKING ? 1 : 0);
            setQuery(context, "query.is_digging", entity, e -> e.getPose() == Pose.DIGGING ? 1 : 0);
            setQuery(context, "query.is_emerging", entity, e -> e.getPose() == Pose.EMERGING ? 1 : 0);
            setQuery(context, "query.is_fire_immune", entity, e -> e.fireImmune() ? 1 : 0);
            setQuery(context, "query.is_ignited", entity, e -> e.isOnFire() ? 1 : 0);
            setQuery(context, "query.is_invisible", entity, e -> e.isInvisible() ? 1 : 0);
            setQuery(context, "query.is_in_contact_with_water", entity, e -> e.isInWaterOrRain() ? 1 : 0);
            setQuery(context, "query.is_in_lava", entity, e -> e.isInLava() ? 1 : 0);
            setQuery(context, "query.is_in_water", entity, e -> e.isInWater() ? 1 : 0);
            setQuery(context, "query.is_in_water_or_rain", entity, e -> e.isInWaterOrRain() ? 1 : 0);
            setQuery(context, "query.is_levitating", entity, e -> e.isNoGravity() && !e.onGround() ? 1 : 0);
            setQuery(context, "query.is_moving", entity, e -> e.getDeltaMovement().lengthSqr() > 0.001 ? 1 : 0);
            setQuery(context, "query.is_on_fire", entity, e -> e.isInvisible() ? 1 : 0);
            setQuery(context, "query.is_invisible", entity, e -> e.isInvisible() ? 1 : 0);
            setQuery(context, "query.is_on_ground", entity, e -> e.onGround() ? 1 : 0);
            setQuery(context, "query.is_riding", entity, e -> e.isPassenger() ? 1 : 0);
            setQuery(context, "query.is_roaring", entity, e -> e.getPose() == Pose.ROARING ? 1 : 0);
            setQuery(context, "query.is_shaking", entity, e -> e.isFullyFrozen() ? 1 : 0);
            setQuery(context, "query.is_silent", entity, e -> e.isSilent() ? 1 : 0);
            setQuery(context, "query.is_sleeping", entity, e -> e.getPose() == Pose.SLEEPING ? 1 : 0);
            setQuery(context, "query.is_sneaking", entity, e -> e.isCrouching() ? 1 : 0);
            setQuery(context, "query.is_sniffing", entity, e -> e.getPose() == Pose.SNIFFING ? 1 : 0);
            setQuery(context, "query.is_sprinting", entity, e -> e.isSprinting() ? 1 : 0);
            setQuery(context, "query.is_swimming", entity, e -> e.isSwimming() ? 1 : 0);
            setQuery(context, "query.life_time", entity, e -> e.tickCount);
            setQuery(context, "query.on_fire_time", entity, Entity::getRemainingFireTicks);

            setQuery(context, "query.rider_body_x_rotation", entity, (e, ctx) -> e.getFirstPassenger() == null ? 0 : e.getFirstPassenger().getViewXRot(ctx.partialTicks()));
            setQuery(context, "query.rider_body_y_rotation", entity, (e, ctx) -> {
                Entity passenger = e.getFirstPassenger();
                if (passenger == null)
                    return 0;
                return passenger instanceof LivingEntity living ? Mth.lerp(ctx.partialTicks(), living.yBodyRotO, living.yBodyRot) : e.getFirstPassenger().getViewYRot(ctx.partialTicks());
            });
            setQuery(context, "query.rider_head_x_rotation", entity, (e, ctx) -> e.getFirstPassenger() instanceof LivingEntity living ? living.getViewXRot(ctx.partialTicks()) : 0);
            setQuery(context, "query.rider_head_y_rotation", entity, (e, ctx) -> e.getFirstPassenger() instanceof LivingEntity living ? living.getViewYRot(ctx.partialTicks()) : 0);

            setQuery(context, "query.vertical_speed", entity, e -> e.getDeltaMovement().y);
            setQuery(context, "query.yaw_speed", entity, e -> e.getYRot() - e.yRotO);

            if (entity instanceof LivingEntity living) {
                setQuery(context, "query.base_swing_duration", living, e -> e.swingTime);
                setQuery(context, "query.blocking", living, e -> e.isBlocking() ? 1 : 0);
                setQuery(context, "query.death_ticks", living, (e, ctx) -> e.deathTime == 0 ? 0 : e.deathTime + ctx.partialTicks());
                setQuery(context, "query.equipment_count", living, e -> Arrays.stream(EquipmentSlot.values()).filter(EquipmentSlot::isArmor).filter(slot -> e.getItemBySlot(slot).isEmpty()).count());
                setQuery(context, "query.eye_target_x_rotation", living, (e, ctx) -> e.getViewXRot(ctx.partialTicks()));
                setQuery(context, "query.eye_target_y_rotation", living, (e, ctx) -> Mth.lerp(ctx.partialTicks(), e.yHeadRotO, e.yHeadRot));
                setQuery(context, "query.has_head_gear", living, e -> e.hasItemInSlot(EquipmentSlot.HEAD) ? 1 : 0);
                setQuery(context, "query.head_x_rotation", living, (e, ctx) -> e.getViewXRot(ctx.partialTicks()));
                setQuery(context, "query.head_y_rotation", living, (e, ctx) -> Mth.lerp(ctx.partialTicks(), e.yHeadRotO, e.yHeadRot));
                setQuery(context, "query.health", living, LivingEntity::getHealth);
                setQuery(context, "query.hurt_time", living, (e, ctx) -> e.hurtTime == 0 ? 0 : Math.max(0, e.hurtTime - ctx.partialTicks()));
                setQuery(context, "query.is_baby", living, e -> e.isBaby() ? 1 : 0);
                setQuery(context, "query.is_critical", living, e -> e.getHealth() <= e.getMaxHealth() * 0.2 ? 1 : 0);
                setQuery(context, "query.is_eating", living, e -> e.getUseItem().has(DataComponents.FOOD) ? 1 : 0);
                setQuery(context, "query.is_eating", living, e -> e.getPose() == Pose.SLEEPING ? 1 : 0);
                setQuery(context, "query.is_using_item", living, e -> e.getUseItem().isEmpty() ? 0 : 1);
                setQuery(context, "query.is_wall_climbing", living, e -> e.onClimbable() ? 1 : 0);
                setQuery(context, "query.item_in_use_duration", living, LivingEntity::getTicksUsingItem);
                setQuery(context, "query.item_max_use_duration", living, e -> e.getUseItem().getUseDuration(e));
                setQuery(context, "query.item_remaining_use_duration", living, LivingEntity::getUseItemRemainingTicks);
                setQuery(context, "query.main_hand_item_max_duration", living, e -> e.getMainHandItem().getUseDuration(e));
                setQuery(context, "query.main_hand_item_use_duration", living, e -> e.getMainHandItem().getUseDuration(e));
                setQuery(context, "query.max_health", living, LivingEntity::getMaxHealth);
            }
            if (entity instanceof Mob mob) {
                setQuery(context, "query.can_climb", mob, e -> !e.isNoAi() && e.getNavigation() instanceof WallClimberNavigation ? 1 : 0);
                setQuery(context, "query.can_fly", mob, e -> !e.isNoAi() && e.getNavigation() instanceof FlyingPathNavigation ? 1 : 0);
                setQuery(context, "query.can_swim", mob, e -> !e.isNoAi() && (e.getNavigation() instanceof WaterBoundPathNavigation || e.getNavigation() instanceof AmphibiousPathNavigation) ? 1 : 0);
                setQuery(context, "query.can_walk", mob, e -> !e.isNoAi() && (e.getNavigation() instanceof GroundPathNavigation || e.getNavigation() instanceof AmphibiousPathNavigation) ? 1 : 0);
            }
            if (entity instanceof OwnableEntity ownable) {
                setQuery(context, "query.has_owner", ownable, e -> e.getOwnerUUID() != null ? 1 : 0);
                setQuery(context, "query.is_tamed", ownable, e -> e.getOwnerUUID() != null ? 1 : 0);
            }
            if (entity instanceof NeutralMob neutral) {
                setQuery(context, "query.is_angry", neutral, e -> e.isAngry() ? 1 : 0);
            }
            if (entity instanceof Leashable leashable) {
                setQuery(context, "query.is_leashed", leashable, e -> leashable.isLeashed() ? 1 : 0);
            }
        }
    }

    private static void setQuery(QueryContext context, String query, ToDoubleFunction<QueryContext> value) {
        if (context.used().contains(query)) {
            context.variables().setVariable(query, value.applyAsDouble(context));
        }
    }

    private static <T> void setQuery(QueryContext context, String query, T holder, ToDoubleFunction<T> value) {
        if (context.used().contains(query)) {
            context.variables().setVariable(query, value.applyAsDouble(holder));
        }
    }

    private static <T> void setQuery(QueryContext context, String query, T holder, ToDoubleBiFunction<T, QueryContext> value) {
        if (context.used().contains(query)) {
            context.variables().setVariable(query, value.applyAsDouble(holder, context));
        }
    }

    private record QueryContext(VariableMap variables, Set<String> used, Minecraft mc, float partialTicks) {
    }
}
