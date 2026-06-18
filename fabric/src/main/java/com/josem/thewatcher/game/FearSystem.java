package com.josem.thewatcher.game;

import com.mojang.brigadier.CommandDispatcher;
import com.josem.thewatcher.entity.ModEntities;
import com.josem.thewatcher.entity.TheWatcherEntity;
import com.josem.thewatcher.bridge.EntityPersistentDataHolder;
import com.josem.thewatcher.network.ModNetworkIds;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FearSystem {
    // ── existing NBT keys ──────────────────────────────────────────────────────
    private static final String FEAR             = "EchoFear";
    private static final String STILL_TICKS      = "EchoStillTicks";
    private static final String MOVE_TICKS       = "EchoMoveTicks";
    private static final String LAST_X           = "EchoLastX";
    private static final String LAST_Y           = "EchoLastY";
    private static final String LAST_Z           = "EchoLastZ";
    private static final String LAST_STEP        = "EchoLastStep";
    private static final String SHADOW_ID        = "EchoShadowId";
    private static final String SHADOW_COOLDOWN  = "EchoShadowCooldown";
    private static final String SHADOW_MOVE      = "EchoShadowMove";
    private static final String SHADOW_CLIMAX    = "EchoShadowClimax";
    private static final String SHADOW_SPAWN_X   = "EchoShadowSpawnX";
    private static final String SHADOW_SPAWN_Y   = "EchoShadowSpawnY";
    private static final String SHADOW_SPAWN_Z   = "EchoShadowSpawnZ";
    private static final String LAST_DOOR_X      = "EchoDoorX";
    private static final String LAST_DOOR_Y      = "EchoDoorY";
    private static final String LAST_DOOR_Z      = "EchoDoorZ";
    private static final String LAST_DOOR_ACTIVE = "EchoDoorActive";
    private static final String CLIMAX_LOCK      = "EchoClimaxLock";
    private static final String CLIMAX_COOLDOWN  = "EchoClimaxCooldown";
    private static final String FEAR_BAR_VISIBLE = "EchoFearBarVisible";
    // ── new NBT keys (0.1.8) ──────────────────────────────────────────────────
    private static final String ECHO_BREAK_TICK   = "EchoBreakTick";
    private static final String ECHO_CHEST_TICK   = "EchoChestTick";
    private static final String ECHO_RENAME_SLOT  = "EchoRenameSlot";
    private static final String ECHO_RESTORE_TICK = "EchoRestoreTick";

    private static final Component[] WHISPERS = {
        Component.literal("Me ves?"),
        Component.literal("Detras"),
        Component.literal("No estas solo")
    };

    private FearSystem() {}

    public static void onComfortFoodConsumed(ServerPlayer player, ItemStack stack) {
        if (stack.is(Items.BREAD) || stack.is(Items.MUSHROOM_STEW)
                || stack.is(Items.RABBIT_STEW) || stack.is(Items.BEETROOT_SOUP)) {
            setFear(player, Mth.clamp(getFear(player) - 12, 0, 100));
        }
    }

    public static void init() {
        // ── server tick ───────────────────────────────────────────────────────
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) tickPlayer(player);
        });

        // ── door + container interaction ──────────────────────────────────────
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                BlockState state = world.getBlockState(hitResult.getBlockPos());
                CompoundTag data = ((EntityPersistentDataHolder) serverPlayer).thewatcher$getPersistentData();

                if (state.getBlock() instanceof DoorBlock) {
                    data.putInt(LAST_DOOR_X, hitResult.getBlockPos().getX());
                    data.putInt(LAST_DOOR_Y, hitResult.getBlockPos().getY());
                    data.putInt(LAST_DOOR_Z, hitResult.getBlockPos().getZ());
                    data.putBoolean(LAST_DOOR_ACTIVE, true);
                }

                if (getFear(serverPlayer) >= 20 && isContainer(state) && data.getInt(ECHO_CHEST_TICK) == 0) {
                    int delay = 10 + serverPlayer.getRandom().nextInt(21);
                    data.putInt(ECHO_CHEST_TICK, serverPlayer.tickCount + delay);
                }
            }
            return InteractionResult.PASS;
        });

        // ── block break: schedule echo ─────────────────────────────────────
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer
                    && getFear(serverPlayer) >= 20) {
                CompoundTag data = ((EntityPersistentDataHolder) serverPlayer).thewatcher$getPersistentData();
                if (data.getInt(ECHO_BREAK_TICK) == 0) {
                    int delay = 10 + serverPlayer.getRandom().nextInt(21);
                    data.putInt(ECHO_BREAK_TICK, serverPlayer.tickCount + delay);
                }
            }
            return true; // never cancel the break
        });

        // ── commands ──────────────────────────────────────────────────────────
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

        // ── on login: clear stale echo ticks and sync HUD ────────────────────
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer p = handler.getPlayer();
            CompoundTag d  = ((EntityPersistentDataHolder) p).thewatcher$getPersistentData();
            d.putInt(ECHO_BREAK_TICK, 0);
            d.putInt(ECHO_CHEST_TICK, 0);
            sendFearState(p);
        });

        // ── persist fear bar preference across respawn ────────────────────────
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CompoundTag original = ((EntityPersistentDataHolder) oldPlayer).thewatcher$getPersistentData();
            CompoundTag copy     = ((EntityPersistentDataHolder) newPlayer).thewatcher$getPersistentData();
            if (original.contains(FEAR_BAR_VISIBLE)) {
                copy.putBoolean(FEAR_BAR_VISIBLE, original.getBoolean(FEAR_BAR_VISIBLE));
            }
        });
    }

    // ── per-tick ──────────────────────────────────────────────────────────────

    private static void tickPlayer(ServerPlayer player) {
        CompoundTag data = ((EntityPersistentDataHolder) player).thewatcher$getPersistentData();
        updateMovementState(player, data);
        updateFear(player, data);
        syncFearHud(player, data);
        handleAuditoryHallucinations(player, data);
        handleEchoActions(player, data);          // new in 0.1.8
        handleEnvironment(player, data);
        handleInventory(player, data);
        handleAnimals(player);
        handleShadow(player, data);
        handleClimax(player, data);
    }

    private static void updateMovementState(ServerPlayer player, CompoundTag data) {
        Vec3 pos = player.position();
        boolean moved = pos.distanceToSqr(data.getDouble(LAST_X), data.getDouble(LAST_Y), data.getDouble(LAST_Z)) > 0.0025D;
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
        if (player.tickCount % 20 != 0) return;
        int fear  = getFear(player);
        int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
        if (light <= 3)                       fear += 1;
        if (data.getInt(STILL_TICKS) > 100)   fear += 1;
        if (isNearLitCampfire(player) || player.level().canSeeSky(player.blockPosition())) fear -= 2;
        setFear(player, Mth.clamp(fear, 0, 100));
    }

    private static void handleAuditoryHallucinations(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        if (fear < 20 || player.tickCount % 20 != 0) return;
        if (data.getInt(STILL_TICKS) == 10 && data.getInt(MOVE_TICKS) == 0
                && player.tickCount - data.getInt(LAST_STEP) > 80) {
            sendClientEvent(player, 1);
            data.putInt(LAST_STEP, player.tickCount);
        }
        if (player.getRandom().nextInt(250 - Math.min(180, fear)) == 0) sendClientEvent(player, 2);
        if (player.getRandom().nextInt(220 - Math.min(170, fear)) == 0) sendClientEvent(player, 3);
    }

    /** Fire deferred action echoes when their scheduled tick arrives. */
    private static void handleEchoActions(ServerPlayer player, CompoundTag data) {
        int breakTick = data.getInt(ECHO_BREAK_TICK);
        if (breakTick > 0 && player.tickCount >= breakTick) {
            sendClientEvent(player, 5);
            data.putInt(ECHO_BREAK_TICK, 0);
        }
        int chestTick = data.getInt(ECHO_CHEST_TICK);
        if (chestTick > 0 && player.tickCount >= chestTick) {
            sendClientEvent(player, 6);
            data.putInt(ECHO_CHEST_TICK, 0);
        }
    }

    private static void handleEnvironment(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        if (player.tickCount % 40 != 0) return;
        if (data.getBoolean(LAST_DOOR_ACTIVE)) {
            BlockPos doorPos = new BlockPos(data.getInt(LAST_DOOR_X), data.getInt(LAST_DOOR_Y), data.getInt(LAST_DOOR_Z));
            if (doorPos.distToCenterSqr(player.position()) > 225.0D && player.getRandom().nextInt(10) == 0) {
                BlockState st = player.level().getBlockState(doorPos);
                if (st.getBlock() instanceof DoorBlock && st.hasProperty(BlockStateProperties.OPEN) && st.getValue(BlockStateProperties.OPEN)) {
                    player.level().setBlock(doorPos, st.setValue(BlockStateProperties.OPEN, false), Block.UPDATE_CLIENTS);
                }
                data.putBoolean(LAST_DOOR_ACTIVE, false);
            }
        }
        if (fear >= 45 && player.level().getMaxLocalRawBrightness(player.blockPosition()) <= 6
                && player.getRandom().nextInt(12) == 0) {
            breakNearbyTorch(player);
        }
    }

    private static void handleInventory(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);

        // ── Restore renamed item when timer expires (checked every tick) ──────
        int restoreTick = data.getInt(ECHO_RESTORE_TICK);
        if (restoreTick > 0 && player.tickCount >= restoreTick) {
            int rSlot = data.getInt(ECHO_RENAME_SLOT);
            if (rSlot >= 0 && rSlot <= 8) {
                ItemStack rStack = player.getInventory().getItem(rSlot);
                if (!rStack.isEmpty()) rStack.resetHoverName();
            }
            data.putInt(ECHO_RESTORE_TICK, 0);
            data.putInt(ECHO_RENAME_SLOT, -1);
            player.inventoryMenu.broadcastChanges();
        }

        if (fear < 35 || player.tickCount % 100 != 0 || player.containerMenu != player.inventoryMenu) return;

        // ── Temp rename + force-equip (new in 0.1.8) ──────────────────────────
        if (data.getInt(ECHO_RESTORE_TICK) == 0 && player.getRandom().nextInt(8) == 0) {
            for (int attempt = 0; attempt < 9; attempt++) {
                int slot = player.getRandom().nextInt(9);
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && !stack.hasCustomHoverName()) {
                    stack.setHoverName(WHISPERS[player.getRandom().nextInt(WHISPERS.length)]);
                    player.getInventory().selected = slot;
                    player.connection.send(new ClientboundSetCarriedItemPacket(slot));
                    data.putInt(ECHO_RENAME_SLOT, slot);
                    data.putInt(ECHO_RESTORE_TICK, player.tickCount + 200);
                    player.inventoryMenu.broadcastChanges();
                    break;
                }
            }
        }

        // ── Improved hotbar drift: swap held slot with a non-adjacent one ─────
        if (data.getInt(ECHO_RESTORE_TICK) == 0 && player.getRandom().nextInt(10) == 0) {
            int current = player.getInventory().selected;
            int target  = -1;
            for (int t = 0; t < 20; t++) {
                int candidate = player.getRandom().nextInt(9);
                if (Math.abs(candidate - current) >= 3) { target = candidate; break; }
            }
            if (target != -1) {
                ItemStack a = player.getInventory().getItem(current).copy();
                ItemStack b = player.getInventory().getItem(target).copy();
                player.getInventory().setItem(current, b);
                player.getInventory().setItem(target, a);
                player.inventoryMenu.broadcastChanges();
            }
        }
    }

    private static void handleAnimals(ServerPlayer player) {
        if (getFear(player) <= 50 || player.tickCount % 10 != 0) return;
        List<Animal> animals = player.level().getEntitiesOfClass(
            Animal.class, player.getBoundingBox().inflate(10.0D), EntitySelector.ENTITY_STILL_ALIVE);
        for (Animal animal : animals) animal.getLookControl().setLookAt(player, 30.0F, 30.0F);
    }

    private static void handleShadow(ServerPlayer player, CompoundTag data) {
        int fear = getFear(player);
        TheWatcherEntity shadow = getShadow(player);
        if (shadow == null && fear >= 100 && data.getInt(SHADOW_COOLDOWN) <= 0 && player.tickCount % 10 == 0) {
            spawnShadow(player, data, false);
        }
        if (shadow != null) {
            if (!shadow.isAlive() || shadow.level() != player.level()) {
                finishClimaxShadow(player, data); clearShadowData(data); return;
            }
            facePlayer(player, shadow);
            if (isPlayerLookingAt(player, shadow)) { despawnShadow(player, shadow, data); return; }
            if (player.level().getMaxLocalRawBrightness(shadow.blockPosition()) <= 7
                    && player.tickCount - data.getInt(SHADOW_MOVE) >= 100) {
                moveShadowCloser(player, shadow);
                data.putInt(SHADOW_MOVE, player.tickCount);
            }
        }
        if (data.getInt(SHADOW_COOLDOWN) > 0) data.putInt(SHADOW_COOLDOWN, data.getInt(SHADOW_COOLDOWN) - 1);
    }

    private static void handleClimax(ServerPlayer player, CompoundTag data) {
        int cooldown = data.getInt(CLIMAX_COOLDOWN);
        if (cooldown > 0) { data.putInt(CLIMAX_COOLDOWN, cooldown - 1); return; }
        if (getFear(player) < 100 || data.getBoolean(CLIMAX_LOCK)) return;
        data.putBoolean(CLIMAX_LOCK, true);
        TheWatcherEntity shadow = getShadow(player);
        if (shadow != null || spawnShadow(player, data, true)) {
            data.putBoolean(SHADOW_CLIMAX, true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.8F, 0.5F);
            data.putInt(CLIMAX_COOLDOWN, 2400);
        }
        data.putBoolean(CLIMAX_LOCK, false);
    }

    private static void syncFearHud(ServerPlayer player, CompoundTag data) {
        if (player.tickCount % 20 != 0) return;
        sendFearState(player);
        if (!data.contains(FEAR_BAR_VISIBLE)) data.putBoolean(FEAR_BAR_VISIBLE, true);
    }

    // ── shadow helpers ────────────────────────────────────────────────────────

    private static boolean spawnShadow(ServerPlayer player, CompoundTag data, boolean ignoreLight) {
        TheWatcherEntity shadow = ModEntities.THE_WATCHER.create(player.level());
        if (shadow == null) return false;
        Vec3 view = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-view.z, 0.0D, view.x).normalize();
        BlockPos spawnPos = null;
        for (int attempt = 0; attempt < 12 && spawnPos == null; attempt++) {
            double sideScale    = 3.0D + player.getRandom().nextDouble() * 2.0D;
            double forwardScale = 6.0D + player.getRandom().nextDouble() * 3.0D;
            double sideSign     = (attempt & 1) == 0 ? 1.0D : -1.0D;
            Vec3 offset = side.scale(sideScale * sideSign).add(view.scale(forwardScale));
            spawnPos = findShadowSpawn(player, BlockPos.containing(player.position().add(offset)), ignoreLight);
        }
        if (spawnPos == null) { data.putInt(SHADOW_COOLDOWN, 20); return false; }
        shadow.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yawToPlayer(player, spawnPos), 0.0F);
        facePlayer(player, shadow);
        player.level().addFreshEntity(shadow);
        data.putUUID(SHADOW_ID, shadow.getUUID());
        data.putInt(SHADOW_MOVE, player.tickCount);
        data.putDouble(SHADOW_SPAWN_X, shadow.getX());
        data.putDouble(SHADOW_SPAWN_Y, shadow.getY());
        data.putDouble(SHADOW_SPAWN_Z, shadow.getZ());
        return true;
    }

    private static void moveShadowCloser(ServerPlayer player, TheWatcherEntity shadow) {
        Vec3 toPlayer = player.position().subtract(shadow.position()).normalize();
        BlockPos movedPos = findShadowSpawn(player, BlockPos.containing(shadow.position().add(toPlayer.scale(2.0D))), true);
        if (movedPos != null) {
            shadow.moveTo(movedPos.getX() + 0.5D, movedPos.getY(), movedPos.getZ() + 0.5D, yawToPlayer(player, movedPos), 0.0F);
            facePlayer(player, shadow);
        }
    }

    private static void facePlayer(ServerPlayer player, TheWatcherEntity shadow) {
        float yaw = yawToPlayer(player, shadow.blockPosition());
        shadow.setYRot(yaw); shadow.yBodyRot = yaw; shadow.yHeadRot = yaw;
        shadow.yRotO = yaw; shadow.yBodyRotO = yaw; shadow.yHeadRotO = yaw;
        shadow.setYHeadRot(yaw);
    }

    private static float yawToPlayer(ServerPlayer player, BlockPos from) {
        double dx = player.getX() - (from.getX() + 0.5D);
        double dz = player.getZ() - (from.getZ() + 0.5D);
        return (float) Math.toDegrees(Mth.atan2(dz, dx)) - 90.0F;
    }

    private static void despawnShadow(ServerPlayer player, TheWatcherEntity shadow, CompoundTag data) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, shadow.getX(), shadow.getY(0.6D), shadow.getZ(), 12, 0.25D, 0.6D, 0.25D, 0.02D);
        }
        shadow.discard();
        setFear(player, 90);
        finishClimaxShadow(player, data);
        clearShadowData(data);
    }

    private static void clearShadowData(CompoundTag data) {
        data.remove(SHADOW_ID); data.remove(SHADOW_CLIMAX);
        data.putInt(SHADOW_COOLDOWN, 120);
    }

    private static void finishClimaxShadow(ServerPlayer player, CompoundTag data) {
        if (data.getBoolean(SHADOW_CLIMAX)) setFear(player, 90);
    }

    private static TheWatcherEntity getShadow(ServerPlayer player) {
        CompoundTag data = ((EntityPersistentDataHolder) player).thewatcher$getPersistentData();
        if (!data.hasUUID(SHADOW_ID) || !(player.level() instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(data.getUUID(SHADOW_ID));
        return entity instanceof TheWatcherEntity shadow ? shadow : null;
    }

    // ── misc helpers ──────────────────────────────────────────────────────────

    private static boolean isPlayerLookingAt(Player player, Entity entity) {
        Vec3 start = player.getEyePosition();
        Vec3 end   = start.add(player.getLookAngle().normalize().scale(64.0D));
        if (entity.getBoundingBox().inflate(0.1D).clip(start, end).isEmpty()) return false;
        Vec3 target = entity.getBoundingBox().getCenter();
        BlockHitResult hit = player.level().clip(new ClipContext(start, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() != HitResult.Type.BLOCK;
    }

    private static BlockPos findShadowSpawn(ServerPlayer player, BlockPos preferred, boolean ignoreLight) {
        for (int dy = -4; dy <= 4; dy++) {
            BlockPos candidate = preferred.offset(0, dy, 0);
            if (canSpawnShadowAt(player, candidate, ignoreLight)) return candidate;
        }
        return null;
    }

    private static boolean canSpawnShadowAt(ServerPlayer player, BlockPos pos, boolean ignoreLight) {
        BlockState below = player.level().getBlockState(pos.below());
        return !below.getCollisionShape(player.level(), pos.below()).isEmpty()
            && hasClearWatcherSpace(player, pos)
            && (ignoreLight || player.level().getMaxLocalRawBrightness(pos) <= 7);
    }

    private static boolean hasClearWatcherSpace(ServerPlayer player, BlockPos pos) {
        for (int y = 0; y <= 2; y++) {
            BlockPos check = pos.above(y);
            if (!player.level().getBlockState(check).getCollisionShape(player.level(), check).isEmpty()) return false;
        }
        return true;
    }

    private static boolean isNearLitCampfire(ServerPlayer player) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos origin = player.blockPosition();
        for (int x = -4; x <= 4; x++) for (int y = -2; y <= 2; y++) for (int z = -4; z <= 4; z++) {
            mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
            BlockState st = player.level().getBlockState(mutable);
            if (st.getBlock() instanceof CampfireBlock && st.hasProperty(BlockStateProperties.LIT) && st.getValue(BlockStateProperties.LIT)) return true;
        }
        return false;
    }

    private static void breakNearbyTorch(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, -2, -5), origin.offset(5, 2, 5))) {
            BlockState st = player.level().getBlockState(pos);
            Block block = st.getBlock();
            if (block instanceof TorchBlock || block instanceof WallTorchBlock) {
                Block.dropResources(st, player.level(), pos);
                player.level().removeBlock(pos, false);
                return;
            }
        }
    }

    private static boolean isContainer(BlockState state) {
        Block b = state.getBlock();
        return b instanceof ChestBlock || b instanceof TrappedChestBlock
            || b instanceof BarrelBlock || b instanceof EnderChestBlock;
    }

    // ── NBT / network helpers ─────────────────────────────────────────────────

    private static int getFear(ServerPlayer player) {
        return ((EntityPersistentDataHolder) player).thewatcher$getPersistentData().getInt(FEAR);
    }

    private static void setFear(ServerPlayer player, int fear) {
        ((EntityPersistentDataHolder) player).thewatcher$getPersistentData().putInt(FEAR, fear);
    }

    private static void sendClientEvent(ServerPlayer player, int eventId) {
        var buffer = PacketByteBufs.create();
        buffer.writeVarInt(eventId);
        buffer.writeVarInt(0);
        ServerPlayNetworking.send(player, ModNetworkIds.MAIN, buffer);
    }

    private static void sendFearState(ServerPlayer player) {
        CompoundTag data = ((EntityPersistentDataHolder) player).thewatcher$getPersistentData();
        boolean visible = !data.contains(FEAR_BAR_VISIBLE) || data.getBoolean(FEAR_BAR_VISIBLE);

        var fearBuf = PacketByteBufs.create();
        fearBuf.writeVarInt(100); fearBuf.writeVarInt(getFear(player));
        ServerPlayNetworking.send(player, ModNetworkIds.MAIN, fearBuf);

        var toggleBuf = PacketByteBufs.create();
        toggleBuf.writeVarInt(101); toggleBuf.writeVarInt(visible ? 1 : 0);
        ServerPlayNetworking.send(player, ModNetworkIds.MAIN, toggleBuf);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("watcherfearbar")
            .requires(src -> src.hasPermission(0))
            .then(Commands.literal("on").executes(ctx -> setFearBar(ctx.getSource(), true)))
            .then(Commands.literal("off").executes(ctx -> setFearBar(ctx.getSource(), false))));
    }

    private static int setFearBar(CommandSourceStack source, boolean enabled) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        ((EntityPersistentDataHolder) player).thewatcher$getPersistentData().putBoolean(FEAR_BAR_VISIBLE, enabled);
        sendFearState(player);
        source.sendSuccess(() -> Component.literal("Fear bar " + (enabled ? "enabled" : "disabled")), false);
        return 1;
    }
}
