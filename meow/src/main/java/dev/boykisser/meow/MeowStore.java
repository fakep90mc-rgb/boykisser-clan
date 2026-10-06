// language: Java, file: MeowStore.java, target: Paper 1.21.11, Java 21
// yaml-backed store for saved warp commands: name -> world + xyz + rotation
package dev.boykisser.meow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MeowStore {

    public record Warp(String world, double x, double y, double z, float yaw, float pitch) {

        static Warp of(Location loc) {
            return new Warp(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                    loc.getYaw(), loc.getPitch());
        }

        /** Resolves the world at teleport time — returns null if the world is unloaded. */
        public Location toLocation() {
            World w = Bukkit.getWorld(world);
            if (w == null) return null;
            return new Location(w, x, y, z, yaw, pitch);
        }

        public String format() {
            return String.format("%s  %.1f / %.1f / %.1f", world, x, y, z);
        }
    }

    private final MeowPlugin plugin;
    private final File file;
    private final Map<String, Warp> warps = new LinkedHashMap<>();

    public MeowStore(MeowPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
    }

    public void load() {
        warps.clear();
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("warps");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection w = section.getConfigurationSection(key);
            if (w == null) continue;
            warps.put(key.toLowerCase(Locale.ROOT), new Warp(
                    w.getString("world", "world"),
                    w.getDouble("x"),
                    w.getDouble("y"),
                    w.getDouble("z"),
                    (float) w.getDouble("yaw"),
                    (float) w.getDouble("pitch")
            ));
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Warp> e : warps.entrySet()) {
            String path = "warps." + e.getKey();
            Warp w = e.getValue();
            yaml.set(path + ".world", w.world());
            yaml.set(path + ".x", w.x());
            yaml.set(path + ".y", w.y());
            yaml.set(path + ".z", w.z());
            yaml.set(path + ".yaw", w.yaw());
            yaml.set(path + ".pitch", w.pitch());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save warps.yml: " + ex.getMessage());
        }
    }

    public void put(String name, Location loc) {
        warps.put(name, Warp.of(loc));
    }

    public void remove(String name) {
        warps.remove(name);
    }

    public Warp get(String name) {
        return warps.get(name);
    }

    public boolean contains(String name) {
        return warps.containsKey(name);
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(warps.keySet());
    }

    public int size() {
        return warps.size();
    }
}
