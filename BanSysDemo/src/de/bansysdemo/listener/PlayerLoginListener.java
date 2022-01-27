package de.bansysdemo.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import de.bansysdemo.Bansysdemo;
import de.bansysdemo.banhandler.BanHandler;
import de.bansysdemo.database.Mysql;
import de.bansysdemo.language.Languages;

public class PlayerLoginListener implements Listener {
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (Mysql.connection == null) {
			Languages languages = new Languages();
			e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	        e.setKickMessage(languages.getMessage(Bansysdemo.defaultlanguage, "login_connection_error"));
	        return;
		}
		
		BanHandler bh = new BanHandler(e.getPlayer().getName(), "Loginevent");
		bh.setIP(e.getAddress().getHostAddress());
		
		if (bh.isPermBanned()) {
			e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	        e.setKickMessage(bh.getPermBanInfoScreen());
	        return;
		}
		
		if (bh.isTempBanned()) {
			e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
	        e.setKickMessage(bh.getTempBanInfoScreen());
	        return;
		}
	}
}
