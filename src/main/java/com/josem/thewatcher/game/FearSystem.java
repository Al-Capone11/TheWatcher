package com.josem.thewatcher.game;

import com.mojang.brigadier.CommandDispatcher;
import com.josem.thewatcher.entity.ModEntities;
import com.josem.thewatcher.entity.TheWatcherEntity;
import com.josem.thewatcher.network.ClientHorrorPacket;
import com.josem.thewatcher.network.ModNetwork;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FearSystem {
    // ── existing NBT keys ──────────────────────────────────────────────────────
    private static final String FEAR             = "EchoFear";
    private static final String STILL_TICKS      = "EchoStillTicks";
    private static final String MOVE_TICKS       = "EchoMoveTicks";
    private static final String FEAR_BUFFER      = "EchoFearBuffer";
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
    private static final String ECHO_BREAK_TICK  = "EchoBreakTick";   // deferred break echo
    private static final String ECHO_CHEST_TICK  = "EchoChestTick";   // deferred chest echo
    private static final String ECHO_RENAME_SLOT = "EchoRenameSlot";  // hotbar slot being haunted
    private static final String ECHO_RESTORE_TICK = "EchoRestoreTick"; // when to restore name (0 = none)

    private static final Component[] WHISPERS = {
        Component.literal("Me ves?"),
        Component.literal("Detras"),
        Component.literal("No estas solo")
    };

    private FearSystem() {}

    // ── mod-bus ────────────────────────────────────────────────────────────────

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.THE_WATCHER.get(), TheWatcherEntity.createAttributes().build());
    }

    // ── forge event bus ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();
        updateMovementState(player, data);
        updateFear(player, data);
        syncFearHud(player, data);
        handleAuditoryHallucinations(player, data);
        handleFakeCrash(player, data);            // new fake crash event
        handleEchoActions(player, data);          // new in 0.1.8
        handleEnvironment(player, data);
        handleInventory(player, data);
        handleAnimals(player);
        handleShadow(player, data);
        handleClimax(player, data);
    }

    /** Schedule a delayed echo sound when the player breaks a block. */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!TheWatcherConfig.actionEchoesEnabled() || getFear(player) < 20) return;
        CompoundTag data = player.getPersistentData();
        // Only schedule if no echo is already pending (don't overlap)
        if (data.getInt(ECHO_BREAK_TICK) == 0) {
            int delay = 10 + player.getRandom().nextInt(21); // 0.5 – 1.5 s
            data.putInt(ECHO_BREAK_TICK, player.tickCount + delay);
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        CompoundTag data = player.getPersistentData();

        // Track last interacted door (existing behaviour)
        if (state.getBlock() instanceof DoorBlock) {
            data.putInt(LAST_DOOR_X, event.getPos().getX());
            data.putInt(LAST_DOOR_Y, event.getPos().getY());
            data.putInt(LAST_DOOR_Z, event.getPos().getZ());
            data.putBoolean(LAST_DOOR_ACTIVE, true);
        }

        // Schedule a delayed chest echo (new in 0.1.8)
        if (TheWatcherConfig.actionEchoesEnabled() && getFear(player) >= 20 && isContainer(state) && data.getInt(ECHO_CHEST_TICK) == 0) {
            int delay = 10 + player.getRandom().nextInt(21);
            data.putInt(ECHO_CHEST_TICK, player.tickCount + delay);
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();
        if (stack.is(Items.BREAD) || stack.is(Items.MUSHROOM_STEW)
                || stack.is(Items.RABBIT_STEW) || stack.is(Items.BEETROOT_SOUP)) {
            setFear(player, Mth.clamp(getFear(player) - 12, 0, 100));
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // player.tickCount resets each session; clear any stale absolute-tick values
        CompoundTag data = player.getPersistentData();
        data.putInt(ECHO_BREAK_TICK, 0);
        data.putInt(ECHO_CHEST_TICK, 0);
        sendFearState(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        CompoundTag copy     = event.getEntity().getPersistentData();
        if (original.contains(FEAR_BAR_VISIBLE)) {
            copy.putBoolean(FEAR_BAR_VISIBLE, original.getBoolean(FEAR_BAR_VISIBLE));
        }
    }

    // ── per-tick handlers ─────────────────────────────────────────────────────

    private static void updateMovementState(ServerPlayer player, CompoundTag data) {
        Vec3 pos   = player.position();
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
        int fear = getFear(player);
        int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
        int gain = 0;
        int loss = 0;
        if (light <= 3)                       gain += 1;
        if (data.getInt(STILL_TICKS) > 100)   gain += 1;
        if (isNearLitCampfire(player) || player.level().canSeeSky(player.blockPosition())) loss += 2;
        double delta = gain * TheWatcherConfig.fearIncreaseMultiplier()
            - loss * TheWatcherConfig.fearDecreaseMultiplier();
        if (delta > 0 && isHoldingTorch(player)) delta *= 0.5D;
        double buffered = data.getDouble(FEAR_BUFFER) + delta;
        int applied = buffered > 0.0D ? (int) Math.floor(buffered) : (int) Math.ceil(buffered);
        fear += applied;
        data.putDouble(FEAR_BUFFER, buffered - applied);
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
        if (fear >= 30 && player.getRandom().nextInt(280 - Math.min(200, fear)) == 0) sendClientEvent(player, 7);
        if (fear >= 40 && player.getRandom().nextInt(300 - Math.min(220, fear)) == 0) sendClientEvent(player, 8);
    }

    private static void handleFakeCrash(ServerPlayer player, CompoundTag data) {
        if (!TheWatcherConfig.fakeCrashEnabled() || player.tickCount % 20 != 0) return;
        int fear = getFear(player);
        if (fear >= 40 && fear <= 80) {
            if (player.getRandom().nextInt(300) == 0) sendClientEvent(player, 4);
        }
    }

    /** Fire the delayed action echoes once their scheduled tick arrives. */
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
        if (!TheWatcherConfig.environmentalEventsEnabled()) return;
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
        // ECHO_RESTORE_TICK == 0 means no haunted item is currently active
        if (TheWatcherConfig.itemHauntingEnabled() && data.getInt(ECHO_RESTORE_TICK) == 0 && player.getRandom().nextInt(8) == 0) {
            for (int attempt = 0; attempt < 9; attempt++) {
                int slot = player.getRandom().nextInt(9); // hotbar only (0–8)
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && !stack.hasCustomHoverName()) {
                    stack.setHoverName(WHISPERS[player.getRandom().nextInt(WHISPERS.length)]);
                    // Switch the player's hand to that slot so they see the haunted name
                    player.getInventory().selected = slot;
                    player.connection.send(new ClientboundSetCarriedItemPacket(slot));
                    data.putInt(ECHO_RENAME_SLOT, slot);
                    data.putInt(ECHO_RESTORE_TICK, player.tickCount + 200); // 10 s
                    player.inventoryMenu.broadcastChanges();
                    break;
                }
            }
        }

        // ── Improved hotbar drift: swap currently held slot with a non-adjacent one ──
        if (TheWatcherConfig.hotbarDriftEnabled() && data.getInt(ECHO_RESTORE_TICK) == 0 && player.getRandom().nextInt(10) == 0) {
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
        if (!TheWatcherConfig.animalStaringEnabled() || getFear(player) <= 50 || player.tickCount % 10 != 0) return;
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
        TheWatcherEntity shadow = ModEntities.THE_WATCHER.get().create(player.level());
        if (shadow == null) return false;
        Vec3 view = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-view.z, 0.0D, view.x).normalize();
        BlockPos spawnPos = null;
        for (int attempt = 0; attempt < 12 && spawnPos == null; attempt++) {
            double sideScale   = 3.0D + player.getRandom().nextDouble() * 2.0D;
            double forwardScale = 6.0D + player.getRandom().nextDouble() * 3.0D;
            double sideSign    = (attempt & 1) == 0 ? 1.0D : -1.0D;
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
            serverLevel.playSound(null, shadow.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);
            serverLevel.playSound(null, shadow.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.6F, 0.5F);
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
        CompoundTag data = player.getPersistentData();
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

    private static boolean isHoldingTorch(ServerPlayer player) {
        return player.getMainHandItem().is(Items.TORCH) || player.getOffhandItem().is(Items.TORCH)
            || player.getMainHandItem().is(Items.SOUL_TORCH) || player.getOffhandItem().is(Items.SOUL_TORCH);
    }

    // ── NBT / network helpers ─────────────────────────────────────────────────

    private static int getFear(ServerPlayer player)           { return player.getPersistentData().getInt(FEAR); }
    private static void setFear(ServerPlayer player, int f)  { player.getPersistentData().putInt(FEAR, f); }

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
        dispatcher.register(Commands.literal("watcherfearbar")
            .requires(src -> src.hasPermission(0))
            .then(Commands.literal("on").executes(ctx -> setFearBar(ctx.getSource(), true)))
            .then(Commands.literal("off").executes(ctx -> setFearBar(ctx.getSource(), false))));
    }

    private static int setFearBar(CommandSourceStack source, boolean enabled) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        player.getPersistentData().putBoolean(FEAR_BAR_VISIBLE, enabled);
        sendFearState(player);
        source.sendSuccess(() -> Component.literal("Fear bar " + (enabled ? "enabled" : "disabled")), false);
        return 1;
    }
}
