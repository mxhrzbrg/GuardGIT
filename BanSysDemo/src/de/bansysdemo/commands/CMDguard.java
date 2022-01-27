package de.bansysdemo.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import de.bansysdemo.Bansysdemo;
import de.bansysdemo.banhandler.BanHandler;
import de.bansysdemo.language.Languages;

public class CMDguard implements CommandExecutor, TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
			return false;
		}

		Player p = (Player) sender;
		Languages languages = new Languages();
		
		if (!p.hasPermission("guard.team")) {
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "no_permission").replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
			return false;
		}
		
		if (args.length < 1) {
			sendCommandOverview(p, "overall");
			return false;
		}
		
		BanHandler bh;
		String reason;
		StringBuilder reasonBuilder;
		
		switch (args[0].toLowerCase()) {
		case "ban":
			if (args.length < 2) {
				sendCommandOverview(p, "ban");
				return false;
			}
			
			bh = new BanHandler(args[1], p.getName());
			
			reason = null;
			
			if (args.length > 2) {
				reasonBuilder = new StringBuilder();
				for (int i = 2; i < args.length; i++) {
					reasonBuilder.append(args[i]).append(" ");
				}
				reason = reasonBuilder.toString();
				if (reason.length() > 64) {
					p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "reason_length_error").replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
					return false;
				}
			}
			
			if (bh.isPermBanned()) {
				p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "ban_perm_error_alreadybanned").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
				return false;
			}
			
			if (!bh.setPermBanned(reason)) {
				p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "ban_perm_error").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
				return false;
			} 
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "ban_perm_success").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
			
			return true;
		case "tempban":
			if (args.length < 3) {
				sendCommandOverview(p, "tempban");
				return false;
			}

			bh = new BanHandler(args[1], p.getName());
			
			reason = null;
			
			if (args.length > 3) {
				reasonBuilder = new StringBuilder();
				for (int i = 3; i < args.length; i++) {
					reasonBuilder.append(args[i]).append(" ");
				}
				reason = reasonBuilder.toString();
				if (reason.length() > 64) {
					p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "reason_length_error").replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
					return false;
				}
			}
			
			Long duration;
			try {
				duration = Long.parseLong(args[2].replaceAll("[^0-9]",""));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				sendCommandOverview(p, "tempban");
				return false; 
			}
			
			if (!bh.setTempBanned(reason, convertDurationToLongInMS(args[2], duration))) {
				p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "ban_temp_error").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
				return false;
			} 
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "ban_temp_success").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")).replace("%duration%", args[2]));

			return true;
		case "unban":
			if (args.length != 2) {
				sendCommandOverview(p, "unban");
				return false;
			}

			bh = new BanHandler(args[1], p.getName());
			if (!bh.isBanned()) {
				p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "unban_error_notbanned").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
				return false;
			}
			if (!bh.setUnbanned()) {
				p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "unban_error").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
				return false;
			}
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "unban_success").replace("%name%", args[1]).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname")));
			
			return true;
		default:
			sendCommandOverview(p, "overall");
			break;
		}

		return false;
	}
	
	public void sendCommandOverview(Player p, String part) {
		Languages languages = new Languages();
		switch (part) {
		case "overall":
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + " " + languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_overall"));
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_ban")));
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_tempban")));
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_unban")));
			break;
		case "ban":
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + " " + languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_ban")));
			break;
		case "unban":
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + " " + languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_unban")));
			break;
		case "tempban":
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + " " + languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_tempban")));
			break;
		default:
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + " " + languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_overall"));
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_ban")));
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_tempban")));
			p.sendMessage(languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_usage").replace("%command%", languages.getMessage(Bansysdemo.defaultlanguage, "commandoverview_unban")));
			break;
		}
	}
	
	public Long convertDurationToLongInMS(String rawDuration, Long duration) {
		if (rawDuration.contains("m") || rawDuration.contains("m")) {
			duration = duration*60L;
		} else if (rawDuration.contains("h") || rawDuration.contains("H")) {
			duration = duration*3600L;
		} else if (rawDuration.contains("d") || rawDuration.contains("D")) {
			duration = duration*86400L;
		} else if (rawDuration.contains("w") || rawDuration.contains("W")) {
			duration = duration*604800L;
		}
		
		return duration*1000L;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("guard.team")) {
			return null;
		}
		
        List<String> banfunction = new ArrayList<>();
        banfunction.add("ban");
        banfunction.add("tempban");
        banfunction.add("unban");
        
        List<String> durationunits = new ArrayList<>();
        durationunits.add("s");
        durationunits.add("m");
        durationunits.add("h");
        durationunits.add("d");
        durationunits.add("w");
        
        List<String> output = new ArrayList<>();

        if (args.length == 1){
            StringUtil.copyPartialMatches(args[0], banfunction, output);
        }
        
        if (args.length == 2){
            return null;
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("tempban")) {
        	return durationunits;
        }
        
        if (args.length > 3) {
        	return output;
        }
        
        Collections.sort(output);
        return output;
	}
}
