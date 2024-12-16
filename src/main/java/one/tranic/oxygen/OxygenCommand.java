package one.tranic.oxygen;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OxygenCommand extends Command {
    public OxygenCommand() {
        super("oxygen-reload");
        this.setPermission("tranic.oxygen");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission("tranic.oxygen")) {
            sender.sendMessage(ChatColor.RED + "[OxygenCylinder] Not enough permission to use this command !");
            return true;
        }
        OxygenCylinder.getInstance().reloadConfig();
        Config.reload();
        sender.sendMessage(ChatColor.GREEN + "Config reloaded !");
        return false;
    }
}
