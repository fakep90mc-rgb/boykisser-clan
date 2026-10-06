// language: Java, file: MeowCommand.java, target: Paper 1.21.11, Java 21
// /meow create|delete|list — manager command with tab completion
package dev.boykisser.meow.commands;

import dev.boykisser.meow.MeowPlugin;
import dev.boykisser.meow.MeowStore;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MeowCommand implements CommandExecutor, TabCompleter {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]{0,31}$");
    private static final List<String> SUBS = List.of("create", "delete", "list", "help");

    private final MeowPlugin plugin;

    public MeowCommand(MeowPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> create(sender, args);
            case "delete", "remove" -> delete(sender, args);
            case "list" -> list(sender);
            case "help" -> help(sender);
            default -> sender.sendMessage("§d§omeow §f» §cunknown subcommand. §7/meow help");
        }
        return true;
    }

    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can create commands (stand where they should lead).");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§d§omeow §f» §cusage: §f/meow create <name>");
            return;
        }

        String name = args[1].toLowerCase(Locale.ROOT);
        if (!NAME_PATTERN.matcher(name).matches()) {
            sender.sendMessage("§d§omeow §f» §cinvalid name. §7a-z, 0-9, _ or -, max 32 chars");
            return;
        }

        Command existing = plugin.getCommandMap().getCommand(name);
        if (existing != null && !(existing instanceof WarpCommand)) {
            sender.sendMessage("§d§omeow §f» §cthe name §f/" + name + " §cis already taken by another command.");
            return;
        }

        Location loc = player.getLocation();
        plugin.getStore().put(name, loc);
        plugin.getStore().save();

        if (!(existing instanceof WarpCommand)) {
            plugin.registerWarpCommand(name);
        }

        player.sendMessage("§d§omeow §f» §7created §f/" + name + " §7→ §f" + plugin.getStore().get(name).format());
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§d§omeow §f» §cusage: §f/meow delete <name>");
            return;
        }

        String name = args[1].toLowerCase(Locale.ROOT);
        if (!plugin.getStore().contains(name)) {
            sender.sendMessage("§d§omeow §f» §cno created command named §f/" + name);
            return;
        }

        plugin.getStore().remove(name);
        plugin.getStore().save();
        plugin.unregisterWarpCommand(name);
        sender.sendMessage("§d§omeow §f» §7deleted §f/" + name);
    }

    private void list(CommandSender sender) {
        MeowStore store = plugin.getStore();
        if (store.size() == 0) {
            sender.sendMessage("§d§omeow §f» §7no commands created yet. §f/meow create <name>");
            return;
        }
        sender.sendMessage("§d§omeow §f» §7created commands §8(" + store.size() + ")");
        for (String name : store.names()) {
            sender.sendMessage("§7  §f/" + name + " §8→ §7" + store.get(name).format());
        }
    }

    private void help(CommandSender sender) {
        sender.sendMessage("§d§omeow §f» §7custom command manager");
        sender.sendMessage("§7  §f/meow create <name> §8- §7save your position as /<name>");
        sender.sendMessage("§7  §f/meow delete <name> §8- §7remove /<name>");
        sender.sendMessage("§7  §f/meow list §8- §7list every created command");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("delete") || sub.equals("remove")) {
                return filter(new ArrayList<>(plugin.getStore().names()), args[1]);
            }
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String opt : options) {
            if (opt.startsWith(prefix)) out.add(opt);
        }
        return out;
    }
}
