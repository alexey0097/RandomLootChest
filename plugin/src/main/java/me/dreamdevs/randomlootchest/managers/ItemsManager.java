package me.dreamdevs.randomlootchest.managers;

import lombok.Getter;
import me.dreamdevs.randomlootchest.RandomLootChestMain;
import me.dreamdevs.randomlootchest.api.utils.Util;
import me.dreamdevs.randomlootchest.objects.RandomItem;
import me.dreamdevs.randomlootchest.utils.InventorySerializerUtil;
import me.dreamdevs.randomlootchest.utils.ItemUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ItemsManager {

    private YamlConfiguration config;
    private final @Getter HashMap<String, RandomItem> items;

    public ItemsManager() {
        items = new HashMap<>();
        load(RandomLootChestMain.getInstance());
    }

    public void load(RandomLootChestMain plugin) {
        items.clear();
        config = YamlConfiguration.loadConfiguration(plugin.getItemsFile());

        Optional<ConfigurationSection> section = Optional.ofNullable(config.getConfigurationSection("Items"));
        if (section.isPresent()) {
            section.ifPresent(itemsSection -> itemsSection.getKeys(false).forEach(string -> {
                Map<String, Integer> enchantments = new HashMap<>();
                Optional<ConfigurationSection> enchantmentsSection = Optional.ofNullable(itemsSection.getConfigurationSection("Enchantments"));
                if (enchantmentsSection.isPresent() && !enchantmentsSection.get().getKeys(false).isEmpty()) {
                    for (String key : enchantmentsSection.get().getKeys(false)) {
                        enchantments.put(key.toUpperCase(), enchantmentsSection.get().getInt(key));
                    }
                }

                Map<String, Object> tags = new HashMap<>();
                Optional<ConfigurationSection> tagsSection = Optional.ofNullable(itemsSection.getConfigurationSection(string+".PublicBukkitValues"));


                if (tagsSection.isPresent() && !tagsSection.get().getKeys(false).isEmpty()) {
                    for (String key : tagsSection.get().getKeys(false)) {
                        tags.put(key, tagsSection.get().get(key));
                    }
                }

                String base64 = itemsSection.getString(string+".Model", null);

                ItemStack itemStack = null;

                if (!StringUtils.isEmpty(base64)) {
                    try {
                        itemStack = InventorySerializerUtil.fromBase64Item(base64);
                        Util.sendPluginMessage("itemStack = " + itemStack);
                    } catch (IOException e) {
                        Util.sendPluginMessage("error = " + e.getMessage());
                    }
                }

                itemStack = ItemUtil.parsedItem(itemStack, itemsSection.getString(string+".Material","STONE"),
                        itemsSection.getInt(string+".Amount",1), itemsSection.getString(string+".DisplayName"),
                        itemsSection.getStringList(string+".DisplayLore"), enchantments,
                        itemsSection.getBoolean(string+".Unbreakable", false), itemsSection.getBoolean(string+".Glowing",false));


                ItemMeta meta = itemStack.getItemMeta();

                if (!tags.isEmpty() && Objects.nonNull(meta)) {
                    tags.forEach((key, value) -> {
                        if (key.contains(":")) {
                            String[] array = key.split(":");
                            org.bukkit.NamespacedKey namespace = new org.bukkit.NamespacedKey(array[0], array[1]);
                            if (value instanceof String) {
                                meta.getPersistentDataContainer().set(namespace, org.bukkit.persistence.PersistentDataType.STRING, String.valueOf(value));
                            }
                            if (value instanceof Integer) {
                                meta.getPersistentDataContainer().set(namespace, PersistentDataType.INTEGER, (Integer) value);
                            }
                        }
                    });

                }
                items.put(string, new RandomItem(itemStack, section.get().getDouble(string+".Chance", 1.00)));
            }));
        }

        Util.sendPluginMessage("&aLoaded "+items.size()+" items from 'items.yml'!");
    }

    public void save(RandomLootChestMain plugin) {
        ConfigurationSection section = config.createSection("Items");
        items.forEach(((s, randomItem) -> {
            ConfigurationSection itemSection = section.createSection(s);

            ItemStack itemStack = randomItem.getItemStack();

            File dir = new File(plugin.getDataFolder(), "data");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir,  itemSection.getName());
            try {
                Files.deleteIfExists(file.getAbsoluteFile().toPath());
            } catch (IOException e) {
                Util.sendPluginMessage("ex = " + e.getMessage());
            }

            InventorySerializerUtil.saveBase64Item(itemStack, file);

            itemSection.set("Model", file.getPath());
            itemSection.set("Material", itemStack.getType().name());
            itemSection.set("Amount", itemStack.getAmount());
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta == null) {
                itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            }

            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    itemSection.set("DisplayName", itemMeta.getDisplayName());
                }
                if (itemMeta.hasLore()) {
                    itemSection.set("DisplayLore", itemMeta.getLore());
                }
                itemSection.set("Unbreakable", itemMeta.isUnbreakable());
                itemSection.set("Glowing", itemMeta.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS));
                if (itemMeta.hasEnchants()) {
                    ConfigurationSection enchantmentSection = itemSection.createSection("Enchantments");
                    for (Map.Entry<Enchantment, Integer> entry : itemMeta.getEnchants().entrySet()) {
                        enchantmentSection.set(entry.getKey().toString(), entry.getValue());
                    }
                }

                if (!itemMeta.getPersistentDataContainer().isEmpty()) {
                    ConfigurationSection bukkitValues = itemSection.createSection("PublicBukkitValues");
                    for (org.bukkit.NamespacedKey key : itemMeta.getPersistentDataContainer().getKeys()) {
                        if (key.toString().contains("weaponmechanics")) {
                            bukkitValues.set("weaponmechanics:firearm-action-state", 0);
                        }

                        try {
                            String value = itemMeta.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
                            Util.sendPluginMessage("value = " + value);
                            bukkitValues.set(key.toString(), value);
                        } catch (Exception ex) {
                            try {
                                Integer value = itemMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                                Util.sendPluginMessage("value = " + value);
                                bukkitValues.set(key.toString(), value);
                            } catch (Exception ex2) {
                                Util.sendPluginMessage("ex = " + ex2.getMessage());
                            }
                        }
                    }
                }

            }
        }));
        try {
            String path = plugin.getItemsFile().getPath();
            Util.sendPluginMessage("save items config" + path);
            config.save(path);
        } catch (IOException e) {
            Util.sendPluginMessage("&cSomething went wrong while saving items.yml file!");
        }
    }

}