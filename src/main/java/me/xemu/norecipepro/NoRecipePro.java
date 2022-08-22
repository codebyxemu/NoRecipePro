package me.xemu.norecipepro;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.List;

public final class NoRecipePro extends JavaPlugin implements CommandExecutor {

	private static NoRecipePro instance;

	public static NoRecipePro getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		// Plugin startup logic

		instance = this;

		loadConfig();

		if (getConfig().getBoolean("stop-all")) {
			clearRecipes();
			return;
		} else if (!getConfig().getStringList("blocked-recipes").isEmpty()) {
			removeBlockedRecipes();
			return;
		}

		getCommand("norecipepro-reload").setExecutor(this);

		getLogger().info("NoRecipePro started!");

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic

		instance = null;

	}

	private void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	private void clearRecipes() {
		if (getConfig().getBoolean("stop-all")) {
			getServer().clearRecipes();
		}
	}

	private void removeBlockedRecipes() {

		List<String> stoppedRecipes = getConfig().getStringList("blocked-recipes");

		for (String s : stoppedRecipes) {
			Iterator<Recipe> it = getServer().recipeIterator();
			Recipe recipe;
			while (it.hasNext()) {
				recipe = it.next();
				if (recipe != null && recipe.getResult().getType() == Material.valueOf(s)) {
					it.remove();
				}
			}
		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (!player.hasPermission(getConfig().getString("admin-perm"))) {
			player.sendMessage(ChatColor.RED + "You don't have enough permissions to do this.");
			return true;
		}

		try {
			reloadConfig();
			removeBlockedRecipes();
			clearRecipes();
			player.sendMessage(ChatColor.GREEN + "Reloaded Recipes & Configs!");
		} catch (Exception exception) {
			exception.printStackTrace();
			player.sendMessage(ChatColor.RED + "Failed Reloaded Recipes & Configs!");
			return true;
		}


		return true;
	}
}
