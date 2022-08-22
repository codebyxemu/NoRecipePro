package me.xemu.norecipepro;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public final class NoRecipePro extends JavaPlugin implements CommandExecutor {

	private static NoRecipePro instance;

	public static NoRecipePro getInstance() {
		return instance;
	}

	private File languageFile;
	private FileConfiguration languageConfig;

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

	private String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	private void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		languageFile = new File(getDataFolder(), "language.yml");
		languageConfig = YamlConfiguration.loadConfiguration(languageFile);

		if (!languageFile.exists()) {
			try {
				getLogger().info("Created language file.");
				languageFile.createNewFile();
			} catch (IOException e) {
				getLogger().info("Failed creating language file.");
				throw new RuntimeException(e);
			}
		}

		languageConfig.addDefault("noPermission", "&8[&dNoRecipePro&8] &cYou do not have enough permissions to do this.");
		languageConfig.addDefault("reloadedPlugin", "&8[&dNoRecipePro&8] &aConfiguration and Recipes has been reloaded!");
		languageConfig.addDefault("failedReloadingPlugin", "&8[&dNoRecipePro&8] &cCould not reload the plugin, config and recipes. Read console error.");
		languageConfig.options().copyDefaults(true);
		saveLanguage();
	}

	private void saveLanguage() {
		try {
			languageConfig.save(languageFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
			player.sendMessage(translate(languageConfig.getString("noPermission")));
			return true;
		}

		try {
			reloadConfig();
			removeBlockedRecipes();
			clearRecipes();
			player.sendMessage(translate(languageConfig.getString("reloadedPlugin")));
		} catch (Exception exception) {
			exception.printStackTrace();
			player.sendMessage(translate(languageConfig.getString("failedReloadingPlugin")));
			return true;
		}


		return true;
	}
}
