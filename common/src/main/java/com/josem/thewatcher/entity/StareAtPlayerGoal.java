package com.josem.thewatcher.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

public class StareAtPlayerGoal extends Goal {
    private final Animal animal;
    private final Player target;
    private int ticksLeft;

    public StareAtPlayerGoal(Animal animal, Player target) {
        this.animal = animal;
        this.target = target;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return target != null && target.isAlive() && animal.distanceToSqr(target) < 256.0D;
    }

    @Override
    public void start() {
        this.ticksLeft = 120 + animal.getRandom().nextInt(100);
        this.animal.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksLeft > 0 && target.isAlive() && animal.distanceToSqr(target) < 256.0D;
    }

    @Override
    public void tick() {
        this.animal.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.ticksLeft--;
    }
}
