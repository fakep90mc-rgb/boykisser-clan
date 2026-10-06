// language: Java, file: WarpCommand.java, target: Paper 1.21.11, Java 21
// dynamically registered /<name> command — teleports the sender to the saved warp
package dev.boykisser.meow.commands;

import dev.boykisser.meow.MeowPlugin;
import dev.boykisser.meow.MeowStore;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class WarpCommand extends Command {

    private final MeowPlugin plugin;
    private final String warpName;

    public WarpCommand(MeowPlugin plugin, String warpName) {
        super(warpName.toLowerCase(Locale.ROOT));
        this.plugin = plugin;
        this.warpName = warpName.toLowerCase(Locale.ROOT);
        setPermission("meow.use." + this.warpName);
        setDescription("Teleport to the saved location '" + this.warpName + "'");
        setUsage("/" + this.warpName);
    }

    public String getWarpName() {
        return warpName;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) return true;

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        MeowStore.Warp warp = plugin.getStore().get(warpName);
        if (warp == null) {
            player.sendMessage("§cThis command no longer exists.");
            return true;
        }

        Location dest = warp.toLocation();
        if (dest == null) {
            player.sendMessage("§cThe world §f" + warp.world() + " §cis not loaded.");
            return true;
        }

        player.teleport(dest);
        String display = warpName.isEmpty()
                ? warpName
                : Character.toUpperCase(warpName.charAt(0)) + warpName.substring(1);
        player.sendMessage("§d§ouwu §f» §7Send to §f" + display);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
