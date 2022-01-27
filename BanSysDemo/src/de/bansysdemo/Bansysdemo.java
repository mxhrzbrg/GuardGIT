package de.bansysdemo;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.bansysdemo.commands.CMDguard;
import de.bansysdemo.database.Mysql;
import de.bansysdemo.language.Languages;
import de.bansysdemo.listener.PlayerLoginListener;

public class Bansysdemo extends JavaPlugin {
	public static Bansysdemo instance;
	public static String defaultlanguage = "ger";
	
	public void onEnable() {
		Bansysdemo.instance = this;
		this.loadListeners();
		this.loadCommands();
		
		Languages.loadMessages();
		
		Mysql.connectMysql();
		Mysql.createTables();
	}

	public void onDisable() {
		Mysql.disconnectMysql();
	}
	
	public void loadCommands() {
		this.getCommand("guard").setExecutor((CommandExecutor) new CMDguard());
	}

	public void loadListeners() {
		Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), (Plugin) this);
	}
	
}
