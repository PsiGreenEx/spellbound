package net.tigereye.spellbound.enchantments;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.tigereye.spellbound.registration.SBStatusEffects;
import net.tigereye.spellbound.util.SBEnchantmentHelper;

public class PolygamousEnchantment extends SBEnchantment{

    public PolygamousEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentTarget.VANISHABLE, new EquipmentSlot[] {EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND});
    }

    public int getMinPower(int level) {
        return 5;
    }

    public int getMaxPower(int level) {
        return 51;
    }

    public int getMaxLevel() {
        return 1;
    }

    public boolean isAcceptableItem(ItemStack stack) {
        return super.isAcceptableItem(stack);
    }

    public float getProtectionAmount(int level, DamageSource source, ItemStack stack, LivingEntity target) {
        SBEnchantmentHelper.testOwnerFaithfulness(stack,target);
        if(target.hasStatusEffect(SBStatusEffects.POLYGAMY)){
            return .5f;
        }
        return -1;
    }

    public float getAttackDamage(int level, ItemStack stack, LivingEntity attacker, Entity defender) {
        SBEnchantmentHelper.testOwnerFaithfulness(stack,attacker);
        if(attacker.hasStatusEffect(SBStatusEffects.POLYGAMY)){
            return 2;
        }
        return -4;
    }

    public float getProjectileDamage(int level, ItemStack stack, PersistentProjectileEntity projectile, Entity attacker, Entity defender, float damage) {
        if(attacker instanceof LivingEntity) {
            SBEnchantmentHelper.testOwnerFaithfulness(stack, (LivingEntity)attacker);
            if (((LivingEntity)attacker).hasStatusEffect(SBStatusEffects.POLYGAMY)) {
                return damage*1.1f;
            }
            return damage*.8f;
        }
        return damage;
    }

    public float getMiningSpeed(int level, PlayerEntity playerEntity, ItemStack itemStack, BlockState block, float miningSpeed) {
        SBEnchantmentHelper.testOwnerFaithfulness(itemStack,playerEntity);
        if(playerEntity.hasStatusEffect(SBStatusEffects.POLYGAMY)){
            return miningSpeed*1.2f;
        }
        return miningSpeed*.7f;
    }

    public boolean isTreasure() {
        return false;
    }

    public boolean canAccept(Enchantment other) {
        return super.canAccept(other);
    }



}
