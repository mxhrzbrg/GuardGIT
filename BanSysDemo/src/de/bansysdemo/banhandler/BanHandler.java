package de.bansysdemo.banhandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.bansysdemo.Bansysdemo;
import de.bansysdemo.database.Mysql;
import de.bansysdemo.language.Languages;

public class BanHandler {

	private Player player = null;
	private String playerUUID;
	private String playerName;
	private String playerIP;
	private String banstate;
	private Long banexpiration;
	private String reason;
	private String log;
	private String cause;

	private Languages languages = new Languages();

	public BanHandler(String playerName, String cause) {
		validateParameters(playerName, cause);
		this.playerName = playerName;

		Player player = Bukkit.getPlayer(this.playerName);
		if (player != null) {
			this.player = player;
			this.playerUUID = player.getUniqueId().toString();
			this.playerIP = player.getAddress().getHostString();
		} else {
			@SuppressWarnings("deprecation")
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
			this.playerUUID = offlinePlayer.getUniqueId().toString();
			this.playerIP = "unknown";
		}

		this.cause = cause;

		if (!playerExists()) {
			createPlayer();
		} else {
			updateName();
		}
	}

	public void createPlayer() {
		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("INSERT INTO playerDB (uuid,name,ip,banstate,banexpiration,reason,log) VALUES (?,?,?,?,?,?,?)");
			ppst.setString(1, this.playerUUID);
			ppst.setString(2, this.playerName);
			ppst.setString(3, this.playerIP);
			ppst.setString(4, "unbanned");
			ppst.setDate(5, null);
			ppst.setString(6, "");
			ppst.setString(7, "");
			ppst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.banstate = "unbanned";
		this.banexpiration = null;
		this.reason = "";
		this.log = "";
	}

	public void updateName() {
		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("UPDATE playerDB SET name = ? WHERE UUID = ?");
			ppst.setString(1, this.playerName);
			ppst.setString(2, this.playerUUID);
			ppst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean playerExists() {
		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("SELECT * FROM playerDB WHERE uuid = ?");
			ppst.setString(1, this.playerUUID);
			ResultSet results = ppst.executeQuery();
			if (results.next()) {
				this.banstate = results.getString("banstate");
				this.banexpiration = results.getLong("banexpiration");
				this.reason = results.getString("reason");
				this.log = results.getString("log");
				results.close();
				return true;
			}
			results.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public boolean setPermBanned(String reason) {
		if (this.playerUUID == null) {
			return false;
		}
		String log = buildBanCause(this.cause);

		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("UPDATE playerDB SET banstate = ?, reason = ?, log = ? WHERE UUID = ?");
			ppst.setString(1, "perm");
			ppst.setString(2, reason);
			ppst.setString(3, log);
			ppst.setString(4, this.playerUUID);
			ppst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		if (this.player != null) {
			if (reason == null) {
				this.player.kickPlayer(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + "\n\n" + languages.getMessage(Bansysdemo.defaultlanguage, "ban_perm_reason_standard"));
				return true;
			}
			this.player.kickPlayer(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + "\n\n" + languages.getMessage(Bansysdemo.defaultlanguage, "ban_perm_reason").replace("%reason%", reason));
			return true;
		}
		this.banstate = "perm";
		return true;
	}

	public boolean setTempBanned(String reason, Long duration) {
		if (this.playerUUID == null) {
			return false;
		}
		String log = buildBanCause(this.cause);

		this.banexpiration = (System.currentTimeMillis() + duration);

		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("UPDATE playerDB SET banstate = ?, banexpiration = ?, reason = ?, log = ? WHERE UUID = ?");
			ppst.setString(1, "temp");
			ppst.setLong(2, this.banexpiration);
			ppst.setString(3, reason);
			ppst.setString(4, log);
			ppst.setString(5, this.playerUUID);
			ppst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		if (this.player != null) {
			if (reason == null) {
				this.player.kickPlayer(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + "\n\n" + languages.getMessage(Bansysdemo.defaultlanguage, "ban_temp_reason_standard").replace("%duration%", getConvertedTime(duration))); // TODO: Standard Bangrund
				return true;
			}
			this.player.kickPlayer(languages.getMessage(Bansysdemo.defaultlanguage, "pluginname") + "\n\n" + languages.getMessage(Bansysdemo.defaultlanguage, "ban_temp_reason").replace("%duration%", getConvertedTime(duration)).replace("%reason%", reason));
			return true;
		}
		this.banstate = "temp";
		return true;
	}

	public boolean setUnbanned() {
		if (this.playerUUID == null) {
			return false;
		}
		String log = buildUnBanCause(this.cause);

		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("UPDATE playerDB SET banstate = ?, banexpiration = ?, reason = ?, log = ? WHERE UUID = ?");
			ppst.setString(1, "unbanned");
			ppst.setDate(2, null);
			ppst.setString(3, "");
			ppst.setString(4, log);
			ppst.setString(5, this.playerUUID);
			ppst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		this.banstate = "unbanned";
		return true;
	}

	public void setIP(String ip) {
		if (ip == null) {
			return;
		}

		try {
			PreparedStatement ppst = Mysql.connection.prepareStatement("UPDATE playerDB SET ip = ? WHERE UUID = ?");
			ppst.setString(1, ip);
			ppst.setString(2, this.playerUUID);
			ppst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		this.playerIP = ip;
	}

	public boolean isBanned() {
		if (!this.banstate.equalsIgnoreCase("perm") && !this.banstate.equalsIgnoreCase("temp")) {
			return false;
		}
		return true;
	}

	public boolean isPermBanned() {
		if (this.banstate.equalsIgnoreCase("perm")) {
			return true;
		}
		return false;
	}

	public boolean isTempBanned() {
		if (this.banstate.equalsIgnoreCase("temp")) {
			if (this.banexpiration <= System.currentTimeMillis()) {
				setUnbanned();
				return false;
			}
			return true;
		}
		return false;
	}

	public Long getBanTimeLeft() {
		if (this.banexpiration != null) {
			return this.banexpiration-System.currentTimeMillis();
		}
		return 0L;
	}

	public String getBanstate() {
		if (this.banstate != null) {
			return this.banstate;
		}
		return "";
	}

	public String getReason() {
		if (this.reason != null) {
			return this.reason;
		}
		return languages.getMessage(Bansysdemo.defaultlanguage, "no_reason");
	}

	public String getLog() {
		if (this.log != null) {
			return this.log;
		}
		return "";
	}

	public String getPermBanInfoScreen() {
		return languages.getMessage(Bansysdemo.defaultlanguage, "login_permban_screen").replace("%reason%", getReason()).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname"));
	}

	public String getTempBanInfoScreen() {
		return languages.getMessage(Bansysdemo.defaultlanguage, "login_tempban_screen").replace("%reason%", getReason()).replaceAll("%duration%", getConvertedTime(this.banexpiration-System.currentTimeMillis())).replace("%pluginname%", languages.getMessage(Bansysdemo.defaultlanguage, "pluginname"));
	}

	public String getConvertedTime(long total) {
		long days = TimeUnit.MILLISECONDS.toDays(total);
		total -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(total);
		total -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(total);
		total -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(total);

		StringBuilder output = new StringBuilder();
		output.append(days);
		output.append(" " + languages.getMessage(Bansysdemo.defaultlanguage, "days") + " ");
		output.append(hours);
		output.append(" " + languages.getMessage(Bansysdemo.defaultlanguage, "hours") + " ");
		output.append(minutes);
		output.append(" " + languages.getMessage(Bansysdemo.defaultlanguage, "minutes") + " ");
		output.append(seconds);
		output.append(" " + languages.getMessage(Bansysdemo.defaultlanguage, "seconds"));
		
		return output.toString();
	}

	public String buildBanCause(String cause) {
		SimpleDateFormat now = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		String output = "Banned by " + cause + " on " + now.format(Calendar.getInstance().getTime());
		return output;
	}

	public String buildUnBanCause(String cause) {
		SimpleDateFormat now = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		now.format(new java.util.Date());
		String output = "Unbanned by " + cause + " on " + now.format(Calendar.getInstance().getTime());
		return output;
	}

	public void validateParameters(String playerName, String cause) {
		if (playerName == null) {
			throw new NullPointerException("Playername cannot be null");
		}
		if (cause == null) {
			throw new NullPointerException("Cause cannot be null");
		}
	}

}
