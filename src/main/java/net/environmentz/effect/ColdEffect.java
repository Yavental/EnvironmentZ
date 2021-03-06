package net.environmentz.effect;

import java.util.UUID;

import net.environmentz.init.ConfigInit;
import net.environmentz.init.TagInit;
import net.environmentz.mixin.DamageSourceAccessor;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ColdEffect extends StatusEffect implements DamageSourceAccessor {
  private static final UUID COLDNESS = UUID.fromString("a8287185-47ef-4b9f-a6d3-643b5833181d");

  public ColdEffect(StatusEffectType type, int color) {
    super(type, color);
  }

  @Override
  public void applyUpdateEffect(LivingEntity entity, int amplifier) {
    if (!isWarmBlockNearBy(entity)) {
      DamageSource damageSource = createDamageSource();
      entity.damage(damageSource, ConfigInit.CONFIG.cold_damage);
      ((PlayerEntity) entity).addExhaustion(0.005F);
    }
  }

  @Override
  public boolean canApplyUpdateEffect(int duration, int amplifier) {
    return duration % ConfigInit.CONFIG.cold_damage_interval == 0;
  }

  @Override
  public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
    EntityAttributeInstance entityAttributeInstance = attributes
        .getCustomInstance((EntityAttributes.GENERIC_MOVEMENT_SPEED));
    if (entityAttributeInstance != null) {
      EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(this.getTranslationKey(), -0.15D,
          EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
      entityAttributeInstance.removeModifier(entityAttributeModifier);
      entityAttributeInstance.addPersistentModifier(
          new EntityAttributeModifier(COLDNESS, this.getTranslationKey() + " " + entityAttributeModifier.getValue(),
              this.adjustModifierAmount(amplifier, entityAttributeModifier), entityAttributeModifier.getOperation()));
    }

  }

  @Override
  public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
    EntityAttributeInstance entityAttributeInstance = attributes
        .getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    if (entityAttributeInstance != null) {
      entityAttributeInstance.removeModifier(COLDNESS);
    }

  }

  public DamageSource createDamageSource() {
    return ((DamageSourceAccessor) ((DamageSourceAccessor) new DamageSource("cold"))
        .setBypassesArmorEnvironmentZAccess()).setUnblockableEnvironmentZAccess();
  }

  public static int warmClothingModifier(LivingEntity livingEntity) {
    int warmingModifier = 0;
    int configAddition = ConfigInit.CONFIG.warm_armor_tick_modifier;
    boolean allowAllArmor = ConfigInit.CONFIG.allow_all_armor;

    ItemStack headStack = livingEntity.getEquippedStack(EquipmentSlot.HEAD);
    ItemStack chestStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
    ItemStack legStack = livingEntity.getEquippedStack(EquipmentSlot.LEGS);
    ItemStack feetStack = livingEntity.getEquippedStack(EquipmentSlot.FEET);

    if (headStack.getItem().isIn(TagInit.WARM_ARMOR) || (allowAllArmor && !headStack.isEmpty())
        || (headStack.hasTag() && headStack.getTag().contains("environmentz"))) {
      warmingModifier = warmingModifier + configAddition;
    }
    if (chestStack.getItem().isIn(TagInit.WARM_ARMOR) || (allowAllArmor && !chestStack.isEmpty())
        || (chestStack.hasTag() && chestStack.getTag().contains("environmentz"))) {
      warmingModifier = warmingModifier + configAddition;
    }
    if (legStack.getItem().isIn(TagInit.WARM_ARMOR) || (allowAllArmor && !legStack.isEmpty())
        || (legStack.hasTag() && legStack.getTag().contains("environmentz"))) {
      warmingModifier = warmingModifier + configAddition;
    }
    if (feetStack.getItem().isIn(TagInit.WARM_ARMOR) || (allowAllArmor && !feetStack.isEmpty())
        || (feetStack.hasTag() && feetStack.getTag().contains("environmentz"))) {
      warmingModifier = warmingModifier + configAddition;
    }
    return warmingModifier;
  }

  public static boolean isWarmBlockNearBy(LivingEntity livingEntity) {
    int heatingRange = ConfigInit.CONFIG.heating_up_block_range;
    for (int i = -heatingRange; i < heatingRange + 1; i++) {
      for (int u = -heatingRange; u < heatingRange + 1; u++) {
        BlockPos pos = new BlockPos(livingEntity.getBlockPos().getX() + i, livingEntity.getBlockPos().getY(),
            livingEntity.getBlockPos().getZ() + u);
        if (livingEntity.world.getBlockState(pos).isIn(TagInit.WARMING_BLOCKS)
            || (livingEntity.world.getBlockState(pos).isOf(Blocks.FURNACE)
                && livingEntity.world.getBlockState(pos).get(AbstractFurnaceBlock.LIT))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public DamageSource setBypassesArmorEnvironmentZAccess() {
    throw new AssertionError("Access Error");
  }

  @Override
  public DamageSource setUnblockableEnvironmentZAccess() {
    throw new AssertionError("Access Error");
  }

}
