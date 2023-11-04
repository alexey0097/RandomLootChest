package me.dreamdevs.randomlootchest.utils;

import lombok.experimental.UtilityClass;
import me.dreamdevs.randomlootchest.api.utils.ColourUtil;
import me.dreamdevs.randomlootchest.api.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class ItemUtil {

    private final String parseError = "&cCannot parse item with type: %MATERIAL%";

    public static ItemStack parsedBasicItem(ItemStack itemStack, String material, int amount) {
        try {
            if (Objects.isNull(itemStack)) return new ItemStack(Objects.requireNonNull(Material.getMaterial(material.toUpperCase())), amount);
            itemStack.setType(Objects.requireNonNull(Material.getMaterial(material.toUpperCase())));
            itemStack.setAmount(amount);
            return itemStack;
        } catch (NullPointerException e) {
            Util.sendPluginMessage(parseError.replace("%MATERIAL%", material));
            return null;
        }
    }

    public static ItemStack parsedItem(ItemStack item, @NotNull String material, int amount, String displayName, List<String> lore, Map<String, Integer> enchantments, boolean unbreakable, boolean glowing) {
        try {
            Optional<ItemStack> optionalItemStack = Optional.ofNullable(parsedBasicItem(item, material, amount));

            if (optionalItemStack.isEmpty()) {
                Util.sendPluginMessage(parseError.replace("%MATERIAL%", material));
                return null;
            }

            ItemStack itemStack = optionalItemStack.get();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta == null) {
                itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            }

            if (itemMeta != null) {
                if (displayName != null) {
                    itemMeta.setDisplayName(ColourUtil.colorize(displayName));
                }

                if (lore != null) {
                    itemMeta.setLore(ColourUtil.colouredLore(lore));
                }

                itemMeta.setUnbreakable(unbreakable);
                if (glowing) {
                    itemMeta.addEnchant(Enchantment.LUCK, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                if (material.equalsIgnoreCase("ENCHANTED_BOOK")) {
                    EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) itemMeta;
                    enchantments.forEach((key, value) -> storageMeta.addStoredEnchant(Enchantment.getByName(key), value, true));
                    itemStack.setItemMeta(storageMeta);
                }

                try {
                    if (!material.equalsIgnoreCase("ENCHANTED_BOOK")) {
                        enchantments.forEach((key, value) -> itemStack.addUnsafeEnchantment(Enchantment.getByName(key), value));
                        itemStack.setItemMeta(itemMeta);
                    }
                } catch (Exception exx) {
                    Util.sendPluginMessage("error = " + exx.getMessage());
                }
            }
            return itemStack;
        } catch (Exception e) {
            Util.sendPluginMessage(parseError.replace("%MATERIAL%", material));
            return null;
        }
    }

    public static ItemStack getPotion(ItemStack item, String material, int amount, String displayName, List<String> lore, Map<String, Integer> enchantments, boolean unbreakable, boolean glowing, String potionType, boolean extended, boolean upgraded) {
        try {
            ItemStack itemStack = parsedItem(item, material, amount, displayName, lore, enchantments, unbreakable, glowing);
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            PotionType potionTypeObj = PotionType.valueOf(potionType.toUpperCase());
            PotionData potionData = new PotionData(potionTypeObj, extended, upgraded);
            potionMeta.setBasePotionData(potionData);
            itemStack.setItemMeta(potionMeta);
            return itemStack;
        } catch (NullPointerException | IllegalArgumentException e) {
            Util.sendPluginMessage("&cCannot get potion with type: "+material+" and type "+potionType);
            return null;
        }
    }

}