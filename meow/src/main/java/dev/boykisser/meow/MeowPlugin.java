// language: Java, file: MeowPlugin.java, target: Paper 1.21.11, Java 21
// plugin entry — resolves the server CommandMap, registers /meow + every saved warp command
package dev.boykisser.meow;

import dev.boykisser.meow.commands.MeowCommand;
import dev.boykisser.meow.commands.WarpCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class MeowPlugin extends JavaPlugin {

    private static final String FALLBACK_PREFIX = "meow";

    private MeowStore store;
    private CommandMap commandMap;

    @Override
    public void onEnable() {
        commandMap = Bukkit.getCommandMap();
        if (commandMap == null) {
            getLogger().severe("Could not resolve the server CommandMap — disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        store = new MeowStore(this);
        store.load();

        PluginCommand meow = getCommand("meow");
        if (meow == null) {
            getLogger().severe("plugin.yml command 'meow' not found — disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        MeowCommand executor = new MeowCommand(this);
        meow.setExecutor(executor);
        meow.setTabCompleter(executor);

        int registered = 0;
        for (String name : store.names()) {
            if (registerWarpCommand(name)) registered++;
        }
        syncCommandTree();

        getLogger().info("Meow enabled — " + registered + " command(s) registered.");
    }

    @Override
    public void onDisable() {
        if (store != null) store.save();
    }

    public MeowStore getStore() {
        return store;
    }

    public CommandMap getCommandMap() {
        return commandMap;
    }

    /**
     * Registers /name as a live server command plus its meow.use.&lt;name&gt; permission node.
     * Returns false if the label is occupied by a foreign command.
     */
    public boolean registerWarpCommand(String name) {
        String key = name.toLowerCase(Locale.ROOT);

        String permNode = "meow.use." + key;
        if (Bukkit.getPluginManager().getPermission(permNode) == null) {
            Bukkit.getPluginManager().addPermission(
                    new Permission(permNode, "Use /" + key, PermissionDefault.TRUE));
        }

        if (commandMap.getCommand(key) != null) return false;

        commandMap.register(FALLBACK_PREFIX, new WarpCommand(this, key));
        syncCommandTree();
        return true;
    }

    /** Unregisters a warp command and its permission node. Never touches foreign commands. */
    public void unregisterWarpCommand(String name) {
        String key = name.toLowerCase(Locale.ROOT);

        org.bukkit.command.Command existing = commandMap.getCommand(key);
        if (existing instanceof WarpCommand warp && warp.getWarpName().equals(key)) {
            // Command#unregister only detaches the object — the map entries must go too
            // ("name" and "meow:name" both point at this instance).
            existing.unregister(commandMap);
            commandMap.getKnownCommands().values().removeIf(cmd -> cmd == existing);
            syncCommandTree();
        }

        Permission perm = Bukkit.getPluginManager().getPermission("meow.use." + key);
        if (perm != null) {
            Bukkit.getPluginManager().removePermission(perm);
        }
    }

    /** Pushes the rebuilt command tree to connected clients so tab-complete matches live state. */
    private void syncCommandTree() {
        try {
            Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException ignored) {
            // non-CraftBukkit server — commands still dispatch, clients refresh on rejoin
        }
    }
}
