package me.dreamdevs.randomlootchest.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;

public class InventorySerializerUtil {

    /**
     * A method to serialize an inventory to Base64 string.
     *
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     */
    public static void saveBase64Item(ItemStack item, File file) throws IllegalStateException {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);

            dataOutput.close();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     *
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     *
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     */
    public static ItemStack fromBase64Item(String path) throws IOException {
        try {
            FileInputStream inputStream = new FileInputStream(path);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack itemStack = (ItemStack) dataInput.readObject();

            dataInput.close();
            return itemStack;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

}
