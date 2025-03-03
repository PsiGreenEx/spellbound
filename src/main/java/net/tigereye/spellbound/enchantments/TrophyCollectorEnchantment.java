package net.tigereye.spellbound.enchantments;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.tigereye.spellbound.Spellbound;
import net.tigereye.spellbound.registration.SBConfig;
import net.tigereye.spellbound.registration.SBStatusEffects;

import java.util.*;

public class TrophyCollectorEnchantment extends SBEnchantment implements CustomConditionsEnchantment{

    private static final String TROPHY_COLLECTOR_KEY = Spellbound.MODID+"TrophyCollector";
    private static final String UNIQUE_TROPHY_COUNT_KEY = Spellbound.MODID+"UniqueTrophyCount";
    public TrophyCollectorEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.VANISHABLE, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    public int getMinPower(int level) {
        return 1;
    }

    public int getMaxPower(int level) {
        return 50;
    }

    public int getMaxLevel() {
        return 1;
    }

    public boolean isAcceptableItem(ItemStack stack) {
        return isAcceptableAtTable(stack);
    }

    public float getAttackDamage(int level, ItemStack stack, LivingEntity attacker, Entity defender) {
        float UniqueTrophyDamage = getUniqueDamageBonus(getUniqueTrophyCount(stack));
        int EntityTrophyDamage = 0;
        if(defender instanceof LivingEntity) {
            EntityTrophyDamage = getEntityDamageBonus(getEntityTrophyCount((LivingEntity)defender, stack));
        }
        return UniqueTrophyDamage + EntityTrophyDamage;
    }

    public float getProjectileDamage(int level, ItemStack stack, PersistentProjectileEntity projectile, Entity attacker, Entity defender, float damage) {
        float UniqueTrophyDamage = getRangedUniqueDamageMultiple(getUniqueTrophyCount(stack));
        float EntityTrophyDamage = 0;
        if(defender instanceof LivingEntity) {
            EntityTrophyDamage = getRangedEntityDamageMultiple(getEntityTrophyCount((LivingEntity)defender, stack));
        }
        return damage*(1+(UniqueTrophyDamage + EntityTrophyDamage));
    }

    public void onKill(int level, ItemStack stack, LivingEntity killer, LivingEntity victim){
        addTrophy(victim, killer, stack,stack.getItem() instanceof RangedWeaponItem);
    }

    public void onActivate(int level, PlayerEntity player, ItemStack stack, Entity target) {
        if(!player.world.isClient && player.getPose() == EntityPose.CROUCHING){
            CompoundTag tag = stack.getOrCreateSubTag(TROPHY_COLLECTOR_KEY);
            Set<String> keys = tag.getKeys();

            player.sendMessage(new LiteralText(""),false);
            player.sendMessage(new LiteralText("----------------------------"),false);
            keys.forEach((trophyKey) -> {
                if(!trophyKey.equals(UNIQUE_TROPHY_COUNT_KEY)) {
                    player.sendMessage(new LiteralText(
                            tag.getInt(trophyKey) + " ")
                                    .append(new TranslatableText(trophyKey))
                                    .append(" (+"+(int)(Math.sqrt(tag.getInt(trophyKey))/4)+")")
                    , false);
                }
            });

            player.sendMessage(new LiteralText(
                    "--" + tag.getInt(UNIQUE_TROPHY_COUNT_KEY) + " Unique Trophies (+"+String.format("%.1f", Math.sqrt(getUniqueTrophyCount(stack))/2)+")--"
            ), false);
        }
    }

    public List<Text> addTooltip(int level, ItemStack stack, PlayerEntity player, TooltipContext context) {
        boolean isRanged = stack.getItem() instanceof RangedWeaponItem;
        List<Text> output = new ArrayList<>();
        CompoundTag tag = stack.getOrCreateSubTag(TROPHY_COLLECTOR_KEY);
        Set<String> keys = tag.getKeys();
        Map<String,Integer> keyIntMap = new HashMap<>();
        keys.forEach((trophyKey) -> {
            if(!trophyKey.equals(UNIQUE_TROPHY_COUNT_KEY)) {
                keyIntMap.put(trophyKey,tag.getInt(trophyKey));
            }
        });
        if(isRanged) {
            output.add(new LiteralText(
                    "--" + getUniqueTrophyCount(stack) + " Unique Trophies (+"
                            + String.format("%.2f", getRangedUniqueDamageMultiple(getUniqueTrophyCount(stack))) + "x)--"));
        }
        else{
            output.add(new LiteralText(
                    "--" + tag.getInt(UNIQUE_TROPHY_COUNT_KEY) + " Unique Trophies (+"
                            + String.format("%.1f", getUniqueDamageBonus(getUniqueTrophyCount(stack))) + ")--"));
        }
        keyIntMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(10)
                .forEach((entry) -> {
                    if(isRanged) {
                        output.add(new LiteralText(
                                entry.getValue() + " ")
                                .append(new TranslatableText(entry.getKey()))
                                .append(" (+" + String.format("%.1f", getRangedEntityDamageMultiple(entry.getValue())) + "x)"));
                    }
                    else{
                        output.add(new LiteralText(
                                entry.getValue() + " ")
                                .append(new TranslatableText(entry.getKey()))
                                .append(" (+" + getEntityDamageBonus(entry.getValue()) + ")"));
                    }
                });
        output.add(new LiteralText("--------------------------"));
        return output;
    }

    public boolean isTreasure() {
        return false;
    }

    //I want to disallow damageEnchantments and anything else that disallows damageEnchantments
    //as typically the later is trying to be another form of damage enchantment
    public boolean canAccept(Enchantment other) {
        return super.canAccept(other)
                && other.canCombine(Enchantments.SHARPNESS)
                && other.canCombine(Enchantments.POWER);
    }

    public boolean isAcceptableAtTable(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof RangedWeaponItem
                || stack.getItem() == Items.BOOK;
    }

    private boolean hasTrophy(LivingEntity victim, ItemStack stack){
        CompoundTag tag = stack.getOrCreateSubTag(TROPHY_COLLECTOR_KEY);
        return tag.contains(victim.getType().toString());
    }

    private boolean addTrophy(LivingEntity victim, LivingEntity killer, ItemStack stack,boolean isRanged){
        CompoundTag tag = stack.getOrCreateSubTag(TROPHY_COLLECTOR_KEY);
        if(!hasTrophy(victim,stack)
                && (victim instanceof HostileEntity || victim instanceof Angerable || victim instanceof PlayerEntity)){
            tag.putInt(UNIQUE_TROPHY_COUNT_KEY,tag.getInt(UNIQUE_TROPHY_COUNT_KEY)+1);
            tag.putInt(victim.getType().toString(),1);
            if(killer instanceof PlayerEntity){
                String message = stack.getName().getString()
                        + " acquired a "
                        + new TranslatableText(victim.getType().toString()).getString()
                        + " trophy";
                ((PlayerEntity)killer).sendMessage(new LiteralText(message)
                        , true);
            }
            return true;
        }
        else{
            int newValue = tag.getInt(victim.getType().toString())+1;
            tag.putInt(victim.getType().toString(),newValue);
            if(isRanged){
                if (getRangedEntityDamageMultiple(newValue - 1) < (getRangedEntityDamageMultiple(newValue))) {
                    String message = stack.getName().getString()
                            + "'s "
                            + new TranslatableText(victim.getType().toString()).getString()
                            + " trophy improved";
                    ((PlayerEntity) killer).sendMessage(new LiteralText(message)
                            , true);
                }
            }
            else {
                if (getEntityDamageBonus(newValue - 1) < (getEntityDamageBonus(newValue))) {
                    String message = stack.getName().getString()
                            + "'s "
                            + new TranslatableText(victim.getType().toString()).getString()
                            + " trophy improved";
                    ((PlayerEntity) killer).sendMessage(new LiteralText(message)
                            , true);
                }
            }
            return false;
        }
    }

    private int getUniqueTrophyCount(ItemStack stack){
        CompoundTag tag = stack.getOrCreateSubTag(TROPHY_COLLECTOR_KEY);
        return tag.getInt(UNIQUE_TROPHY_COUNT_KEY);
    }

    private int getEntityTrophyCount(LivingEntity victim, ItemStack stack){
        CompoundTag tag = stack.getOrCreateSubTag(TROPHY_COLLECTOR_KEY);
        return tag.getInt(victim.getType().toString());
    }

    private float getUniqueDamageBonus(int uniques){
        return (float)Math.sqrt(uniques)/2;
    }

    private int getEntityDamageBonus(int kills){
        return (int) (Math.sqrt(kills) / 4);
    }

    private float getRangedUniqueDamageMultiple(int uniques){
        return (float)Math.sqrt(uniques)*0.2f;
    }

    private float getRangedEntityDamageMultiple(int kills){
        return ((int)(Math.sqrt(kills)/4))*0.2f;
    }
}
