package de.bansysdemo.language;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Languages {
	
	public static Map<String, FileConfiguration> languages = new HashMap<>();
	
	private String messagetranslated;
	
	public static void loadMessages() {
		File path_languages = new File("plugins/guard/languages");
		for (File language : path_languages.listFiles()) {
			FileConfiguration config_language = YamlConfiguration.loadConfiguration(language);
			String filename = language.getName();
			languages.put(filename.substring(0, filename.lastIndexOf('.')), config_language);
		}
	}

	public String getMessage(String language, String message) {
		this.messagetranslated = "§cerror loading language";
		if (!languages.containsKey(language)) {
			return messagetranslated;
		}
		this.messagetranslated = languages.get(language).getString(message, "§cerror loading " + message);
		return messagetranslated;
	}
}
