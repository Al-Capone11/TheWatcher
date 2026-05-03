package com.josem.echoofthevoid.game;

import com.mojang.brigadier.CommandDispatcher;
import com.josem.echoofthevoid.entity.ModEntities;
import com.josem.echoofthevoid.entity.ShadowStalkerEntity;
import com.josem.echoofthevoid.network.ClientHorrorPacket;
import com.josem.echoofthevoid.network.ModNetwork;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FearSystem {
    private static final String FEAR = "EchoFear";
    private static final String STILL_TICKS = "EchoStillTicks";
    private static final String MOVE_TICKS = "EchoMoveTicks";
    private static final String LAST_X = "EchoLastX";
    private static final String LAST_Y = "EchoLastY";
    private static final String LAST_Z = "EchoLastZ";
    private static final String LAST_STEP = "EchoLastStep";
    private static final String SHADOW_ID = "EchoShadowId";
    private static final String SHADOW_COOLDOWN = "EchoShadowCooldown";
    private static final String SHADOW_MOVE = "EchoShadowMove";
    private static final String SHADOW_SPAWN_X = "EchoShadowSpawnX";
    private static final String SHADOW_SPAWN_Y = "EchoShadowSpawnY";
    private static final String SHADOW_SPAWN_Z = "EchoShadowSpawnZ";
    private static final String LAST_DOOR_X = "EchoDoorX";
    private static final String LAST_DOOR_Y = "EchoDoorY";
    private static final String LAST_DOOR_Z = "EchoDoorZ";
    private static final String LAST_DOOR_ACTIVE = "EchoDoorActive";
    private static final String CLIMAX_LOCK = "EchoClimaxLock";
    private static final String CLIMAX_COOLDOWN = "EchoClimaxCooldown";
    private static final String FEAR_BAR_VISIBLE = "EchoFearBarVisible";
    private static final Component[] WHISPERS = new Component[] {
        Component.literal("Me ves?"),
        Component.literal("Detras"),
        Component.literal("No estas solo")
    };

    private FearSystem() {
    }

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SHADOW_STALKER.get(), ShadowStalkerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        updateMovementState(player, data);
        updateFear(player, data);
        syncFearHud(player, data);
        handleAuditoryHallucinations(player, data);
        handleEnvironment(player, data);
        handleInventory(player, data);
        handleAnimals(player);
        handleShadow(player, data);
        handleClimax(player, data);
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof DoorBlock)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        data.putInt(LAST_DOOR_X, event.getPos().getX());
        data.putInt(LAST_DOOR_Y, event.getPos().getY());
        data.putInt(LAST_DOOR_Z, event.getPos().getZ());
        data.putBoolean(LAST_DOOR_ACTIVE, true);
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack stack = event.getItem();
        if (stack.is(Items.BREAD) || stack.is(Items.MUSHROOM_STEW) || stack.is(Items.RABBIT_STEW) || stack.is(Items.BEETROOT_SOUP)) {
            setFear(player, Mth.clamp(getFear(player) - 12, 0, 100));
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendFearState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        CompoundTag copy = event.getEntity().getPersistentData();
        if (original.contains(FEAR_BAR_VISIBLE)) {
            copy.putBoolean(FEAR_BAR_VISIBLE, original.getBoolean(FEAR_BAR_VISIBLE));
        }
    }

    private static void updateMovementState(ServerPlayer player, CompoundTag data) {
        Vec3 pos = player.position();
        double lastX = data.getDouble(LAST_X);
        double lastY = data.getDouble(LAST_Y);
        double lastZ = data.getDouble(LAST_Z);
        boolean moved = pos.distanceToSqr(lastX, lastY, lastZ) > 0.0025D;

        if (moved) {
            data.putInt(STILL_TICKS, 0);
            data.putInt(MOVE_TICKS, data.getInt(MOVE_TICKS) + 1);
        } else {
            data.putInt(STILL_TICKS, data.getInt(STILL_TICKS) + 1);
            data.putInt(MOVE_TICKS, 0);
        }

        data.putDouble(LAST_X, pos.x);
        data.putDouble(LAST_Y, pos.y);
        data.putDouble(LAST_Z, pos.z);
    }

    private static void updateFear(ServerPlayer player, CompoundTag data) {
        if (player.tickCount % 20 != 0) {
            return;
        }

        int fear = getFear(player);
        int ambientLight = player.level().getMaxLocalRawBrightness(player.blockPosition());

        if (ambientLight <= 3) {
            fear += 1;
        }

        if (data.getInt(STILL_TICKS) > 100) {
            fear += 1;
        }

        if (isNearLitCampfire(player) || player.level().canSeeSky(player.blockPosition())) {
            fear -= 2;
        }

        setFear(player, Mth.clamp(fear, 0, 100));
    }

    private static void handleAuditoryHallucinations(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        if (fear < 20 || player.tickCount % 20 != 0) {
            return;
        }

        if (data.getInt(STILL_TICKS) == 10 && data.getInt(MOVE_TICKS) == 0 && player.tickCount - data.getInt(LAST_STEP) > 80) {
            sendClientEvent(player, 1);
            data.putInt(LAST_STEP, player.tickCount);
        }

        if (player.getRandom().nextInt(250 - Math.min(180, fear)) == 0) {
            sendClientEvent(player, 2);
        }

        if (player.getRandom().nextInt(220 - Math.min(170, fear)) == 0) {
            sendClientEvent(player, 3);
        }
    }

    private static void handleEnvironment(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        if (player.tickCount % 40 != 0) {
            return;
        }

        if (data.getBoolean(LAST_DOOR_ACTIVE)) {
            BlockPos doorPos = new BlockPos(data.getInt(LAST_DOOR_X), data.getInt(LAST_DOOR_Y), data.getInt(LAST_DOOR_Z));
            if (doorPos.distToCenterSqr(player.position()) > 225.0D && player.getRandom().nextInt(10) == 0) {
                BlockState state = player.level().getBlockState(doorPos);
                if (state.getBlock() instanceof DoorBlock && state.hasProperty(BlockStateProperties.OPEN) && state.getValue(BlockStateProperties.OPEN)) {
                    player.level().setBlock(doorPos, state.setValue(BlockStateProperties.OPEN, false), Block.UPDATE_CLIENTS);
                }
                data.putBoolean(LAST_DOOR_ACTIVE, false);
            }
        }

        if (fear >= 45 && player.level().getMaxLocalRawBrightness(player.blockPosition()) <= 6 && player.getRandom().nextInt(12) == 0) {
            breakNearbyTorch(player);
        }
    }

    private static void handleInventory(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        if (fear < 35 || player.tickCount % 100 != 0 || player.containerMenu != player.inventoryMenu) {
            return;
        }

        if (player.getRandom().nextInt(8) == 0) {
            int slot = player.getRandom().nextInt(player.getInventory().items.size());
            ItemStack stack = player.getInventory().items.get(slot);
            if (!stack.isEmpty()) {
                stack.setHoverName(WHISPERS[player.getRandom().nextInt(WHISPERS.length)]);
            }
        }

        if (player.getRandom().nextInt(10) == 0) {
            ItemStack first = player.getInventory().getItem(0).copy();
            ItemStack second = player.getInventory().getItem(1).copy();
            player.getInventory().setItem(0, second);
            player.getInventory().setItem(1, first);
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static void handleAnimals(ServerPlayer player) {
        if (getFear(player) <= 50 || player.tickCount % 10 != 0) {
            return;
        }

        List<Animal> animals = player.level().getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(10.0D), EntitySelector.ENTITY_STILL_ALIVE);
        for (Animal animal : animals) {
            animal.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }

    private static void handleShadow(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        ShadowStalkerEntity shadow = getShadow(player);

        if (shadow == null && fear >= 35 && player.level().getMaxLocalRawBrightness(player.blockPosition()) <= 7 && data.getInt(SHADOW_COOLDOWN) <= 0) {
            if (player.tickCount % 40 == 0 && player.getRandom().nextInt(Math.max(8, 34 - fear / 3)) == 0) {
                spawnShadow(player, data);
            }
        }

        if (shadow != null) {
            if (!shadow.isAlive() || shadow.level() != player.level()) {
                clearShadowData(data);
                return;
            }

            shadow.lookAt(player, 30.0F, 30.0F);

            if (isPlayerLookingAt(player, shadow)) {
                despawnShadow(player, shadow, data);
                return;
            }

            if (player.level().getMaxLocalRawBrightness(shadow.blockPosition()) <= 7 && player.tickCount - data.getInt(SHADOW_MOVE) >= 100) {
                moveShadowCloser(player, shadow);
                data.putInt(SHADOW_MOVE, player.tickCount);
            }
        }

        if (data.getInt(SHADOW_COOLDOWN) > 0) {
            data.putInt(SHADOW_COOLDOWN, data.getInt(SHADOW_COOLDOWN) - 1);
        }
    }

    private static void handleClimax(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        int cooldown = data.getInt(CLIMAX_COOLDOWN);
        if (cooldown > 0) {
            data.putInt(CLIMAX_COOLDOWN, cooldown - 1);
            return;
        }

        if (fear < 90 || data.getBoolean(CLIMAX_LOCK)) {
            return;
        }

        data.putBoolean(CLIMAX_LOCK, true);
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        forceSpawnAhead(player, data);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.8F, 0.5F);
        setFear(player, 0);
        data.putInt(CLIMAX_COOLDOWN, 2400);
        data.putBoolean(CLIMAX_LOCK, false);
    }

    private static void syncFearHud(ServerPlayer player, CompoundTag data) {
        if (player.tickCount % 20 != 0) {
            return;
        }

        sendFearState(player);
        if (!data.contains(FEAR_BAR_VISIBLE)) {
            data.putBoolean(FEAR_BAR_VISIBLE, true);
        }
    }

    private static void forceSpawnAhead(ServerPlayer player, CompoundTag data) {
        ShadowStalkerEntity shadow = getShadow(player);
        Vec3 ahead = player.position().add(player.getLookAngle().scale(2.5D));
        if (shadow == null) {
            shadow = ModEntities.SHADOW_STALKER.get().create(player.level());
            if (shadow == null) {
                return;
            }
            player.level().addFreshEntity(shadow);
            data.putUUID(SHADOW_ID, shadow.getUUID());
        }

        shadow.moveTo(ahead.x, player.getY(), ahead.z, player.getYRot() + 180.0F, 0.0F);
    }

    private static void spawnShadow(ServerPlayer player, CompoundTag data) {
        ShadowStalkerEntity shadow = ModEntities.SHADOW_STALKER.get().create(player.level());
        if (shadow == null) {
            return;
        }

        Vec3 view = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-view.z, 0.0D, view.x).normalize();
        double sideScale = 11.5D + player.getRandom().nextDouble() * 3.5D;
        double forwardScale = 1.5D + player.getRandom().nextDouble() * 1.5D;
        Vec3 offset = side.scale(player.getRandom().nextBoolean() ? sideScale : -sideScale).add(view.scale(forwardScale));
        BlockPos spawnBase = BlockPos.containing(player.position().add(offset));
        BlockPos spawnPos = findShadowSpawn(player, spawnBase);
        if (spawnPos == null) {
            data.putInt(SHADOW_COOLDOWN, 80);
            return;
        }

        shadow.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
        player.level().addFreshEntity(shadow);
        data.putUUID(SHADOW_ID, shadow.getUUID());
        data.putInt(SHADOW_MOVE, player.tickCount);
        data.putDouble(SHADOW_SPAWN_X, shadow.getX());
        data.putDouble(SHADOW_SPAWN_Y, shadow.getY());
        data.putDouble(SHADOW_SPAWN_Z, shadow.getZ());
    }

    private static void moveShadowCloser(ServerPlayer player, ShadowStalkerEntity shadow) {
        Vec3 toPlayer = player.position().subtract(shadow.position()).normalize();
        Vec3 moved = shadow.position().add(toPlayer.scale(2.0D));
        shadow.moveTo(moved.x, shadow.getY(), moved.z, shadow.getYRot(), shadow.getXRot());
    }

    private static void despawnShadow(ServerPlayer player, ShadowStalkerEntity shadow, CompoundTag data) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, shadow.getX(), shadow.getY(0.6D), shadow.getZ(), 12, 0.25D, 0.6D, 0.25D, 0.02D);
        }
        shadow.discard();
        clearShadowData(data);
    }

    private static void clearShadowData(CompoundTag data) {
        data.remove(SHADOW_ID);
        data.putInt(SHADOW_COOLDOWN, 120);
    }

    private static ShadowStalkerEntity getShadow(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(SHADOW_ID) || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(data.getUUID(SHADOW_ID));
        return entity instanceof ShadowStalkerEntity shadow ? shadow : null;
    }

    private static boolean isPlayerLookingAt(Player player, Entity entity) {
        Vec3 start = player.getEyePosition();
        Vec3 target = entity.getBoundingBox().getCenter();
        Vec3 toEntity = target.subtract(start).normalize();
        double dot = player.getLookAngle().normalize().dot(toEntity);
        if (dot < 0.94D) {
            return false;
        }

        BlockHitResult hit = player.level().clip(new ClipContext(start, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    private static BlockPos findShadowSpawn(ServerPlayer player, BlockPos preferred) {
        for (int dy = -2; dy <= 2; dy++) {
            BlockPos candidate = preferred.offset(0, dy, 0);
            if (canSpawnShadowAt(player, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean canSpawnShadowAt(ServerPlayer player, BlockPos pos) {
        BlockState feet = player.level().getBlockState(pos);
        BlockState head = player.level().getBlockState(pos.above());
        BlockState below = player.level().getBlockState(pos.below());
        return feet.canBeReplaced()
            && head.canBeReplaced()
            && !below.canBeReplaced()
            && player.level().getMaxLocalRawBrightness(pos) <= 7;
    }

    private static boolean isNearLitCampfire(ServerPlayer player) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos origin = player.blockPosition();

        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = player.level().getBlockState(mutable);
                    if (state.getBlock() instanceof CampfireBlock && state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void breakNearbyTorch(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, -2, -5), origin.offset(5, 2, 5))) {
            BlockState state = player.level().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof TorchBlock || block instanceof WallTorchBlock) {
                Block.dropResources(state, player.level(), pos);
                player.level().removeBlock(pos, false);
                return;
            }
        }
    }

    private static int getFear(ServerPlayer player) {
        return player.getPersistentData().getInt(FEAR);
    }

    private static void setFear(ServerPlayer player, int fear) {
        player.getPersistentData().putInt(FEAR, fear);
    }

    private static void sendClientEvent(ServerPlayer player, int eventId) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientHorrorPacket(eventId));
    }

    private static void sendFearState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        boolean visible = !data.contains(FEAR_BAR_VISIBLE) || data.getBoolean(FEAR_BAR_VISIBLE);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientHorrorPacket(100, getFear(player)));
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientHorrorPacket(101, visible ? 1 : 0));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("watcherfearbar")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("on").executes(context -> setFearBar(context.getSource(), true)))
                .then(Commands.literal("off").executes(context -> setFearBar(context.getSource(), false)))
        );
    }

    private static int setFearBar(CommandSourceStack source, boolean enabled) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        player.getPersistentData().putBoolean(FEAR_BAR_VISIBLE, enabled);
        sendFearState(player);
        source.sendSuccess(() -> Component.literal("Fear bar " + (enabled ? "enabled" : "disabled")), false);
        return 1;
    }
}
