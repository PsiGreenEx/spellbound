package net.tigereye.spellbound.enchantments;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tigereye.spellbound.registration.SBConfig;

import java.util.ArrayList;
import java.util.List;

public class CaveInEnchantment extends SBEnchantment implements CustomConditionsEnchantment{

    public CaveInEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.VANISHABLE, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    public int getMinPower(int level) {
        if(level <= 3) {
            return 10 + (level * 5);
        }
        else{
            return 5 + (level * 10);
        }
    }

    public int getMaxPower(int level) {
        return getMinPower(level)+20;
    }

    public int getMaxLevel() {
        return 5;
    }

    public boolean isAcceptableItem(ItemStack stack) {
        return isAcceptableAtTable(stack);
    }

    public boolean canAccept(Enchantment other) {
        return super.canAccept(other);
    }

    public boolean isAcceptableAtTable(ItemStack stack) {
        return stack.getItem() instanceof RangedWeaponItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() == Items.BOOK;
    }

    public void onProjectileBlockHit(int level, ItemStack itemStack, ProjectileEntity projectileEntity, BlockHitResult blockHitResult) {
        caveIn(level,projectileEntity.world,blockHitResult.getBlockPos());
    }

    private void caveIn(int level, World world, BlockPos center){
        BlockPos lowerCorner = center.add(1-level,1-level,1-level);
        List<BlockState> fallingBlocks = new ArrayList<>();
        int size = (level*2)-1;
        BlockPos target;
        BlockState targetBlock;
        BlockState blockBelowTarget;
        for(int y = 0; y < size; y++){
            if(lowerCorner.getY()+y >= 0){
                for(int x = 0; x < size; x++){
                    for(int z = 0; z < size; z++){
                        target = lowerCorner.add(x,y,z);
                        targetBlock = world.getBlockState(target);
                        blockBelowTarget = world.getBlockState(target.down());
                        if(!targetBlock.isAir() && targetBlock.getBlock().getBlastResistance() <= SBConfig.CAVE_IN_MAX_BLAST_RES
                                && world.getBlockEntity(target) == null
                                && (fallingBlocks.contains(blockBelowTarget) || FallingBlock.canFallThrough(blockBelowTarget))){
                            FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(world,
                                    target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D,
                                    targetBlock);
                            world.spawnEntity(fallingBlockEntity);
                            fallingBlocks.add(targetBlock);
                        }
                    }
                }
            }
        }
    }
}
