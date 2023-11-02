package me.dreamdevs.randomlootchest.managers;

import lombok.Getter;
import me.dreamdevs.randomlootchest.RandomLootChestMain;
import me.dreamdevs.randomlootchest.api.utils.Util;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public class LocationManager {

    private final Map<String, String> locations;
    private static final String FILENAME = "locations.yml";
    private final YamlConfiguration locationsConfig;

    public LocationManager() {
        locations = new HashMap<>();
        locationsConfig = YamlConfiguration.loadConfiguration(RandomLootChestMain.getInstance().getLocationsFile());
        for(String string : locationsConfig.getStringList("locations")) {
            String[] splits = string.split(";");
            locations.put(splits[0], splits[1]);
        }
    }

    public void addLocation(@NotNull String type, @NotNull Location location) {
        locations.put(Util.getLocationString(location), type);
    }

    public void removeLocation(@NotNull Location location) {
        locations.remove(Util.getLocationString(location));
    }

    public void save(RandomLootChestMain plugin) {
        if(getLocations().isEmpty())
            return;
        ArrayList<String> arrayList = new ArrayList<>();
        for(Map.Entry<String, String> map : getLocations().entrySet()) {
            arrayList.add(map.getKey()+";"+map.getValue());
        }

        locationsConfig.set("locations", arrayList);
        try {
            String path = plugin.getLocationsFile().getPath();
            Util.sendPluginMessage("save locations config" + path);
            locationsConfig.save(path);
            locationsConfig.load(path);
        } catch (Exception e) {
            Util.sendPluginMessage("&cSomething went wrong while saving and loading locations.yml");
        }
    }

}