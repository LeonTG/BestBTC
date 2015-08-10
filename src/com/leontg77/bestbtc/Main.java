package com.leontg77.bestbtc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
	private final Logger logger = Bukkit.getServer().getLogger();
	private HashSet<String> list = new HashSet<String>();
	private boolean enabled = false;
	private BukkitRunnable task;
	public static Main plugin;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " is now disabled.");
		plugin = null;
	}
	
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " is now enabled.");
		
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
	}
	
	/**
	 * Get the UHC prefix with an ending color.
	 * @param endcolor the ending color.
	 * @return The UHC prefix.
	 */
	public static String prefix() {
		String prefix = "§a§lBestBTC §8§l>> §7";
		return prefix;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!enabled) {
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (!list.contains(player.getName()) && block.getType() == Material.DIAMOND_ORE) {
			Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " mined a diamond! He is back on the Best BTC List!");
			list.add(player.getName());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!enabled) {
			return;
		}
		
		Player player = event.getPlayer();

		if (list.contains(player.getName()) && event.getTo().getBlockY() > 50) {
			Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " moved above y:50!");
			list.remove(player.getName());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("blist")) {
			if (!enabled) {
				sender.sendMessage(ChatColor.RED + "BestBTC is not enabled.");
				return true;
			}
			
			StringBuilder pvelist = new StringBuilder("");
			ArrayList<String> pve = new ArrayList<String>(list);
			
			for (int i = 0; i < pve.size(); i++) {
				if (pvelist.length() > 0 && i == pve.size() - 1) {
					pvelist.append(" §7and §6");
				}
				else if (pvelist.length() > 0 && pvelist.length() != pve.size()) {
					pvelist.append("§7, §6");
				}
				
				pvelist.append(ChatColor.GOLD + pve.get(i));
			}
			
			sender.sendMessage(Main.prefix() + "People still on the best pve list: §6" + (pvelist.length() > 0 ? pvelist.toString().trim() : "None") + "§7.");
		}
		
		if (cmd.getName().equalsIgnoreCase("btc")) {
			if (sender.hasPermission("btc.manage")) {
				if (args.length == 0) {
					sender.sendMessage(Main.prefix() + "Help for best btc:");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc enable - Enables the scenario.");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc disable - Disables the scenario.");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc add <player> - Adds an player manually to the list.");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc remove <player> - Removes an player manually to the list.");
					return true;
				}
				
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("enable")) {
						if (enabled) {
							sender.sendMessage(ChatColor.RED + "BestBTC is already enabled.");
							return true;
						}
						
						for (Player online : Bukkit.getServer().getOnlinePlayers()) {
							list.add(online.getName());
						}
						
						this.task = new BukkitRunnable() {
							public void run() {
								for (Player online : Bukkit.getOnlinePlayers()) {
									if (list.contains(online.getName())) {
										online.setMaxHealth(online.getMaxHealth() + 2);
										online.setHealth(online.getHealth() + 2);
										online.sendMessage(ChatColor.GREEN + "You were rewarded for your BTC skills!");
									} else {
										online.sendMessage(ChatColor.GREEN + "BestBTC players gained a heart!");
									}
								}
							}
						};
						
						task.runTaskTimer(Main.plugin, 12000, 12000);
						enabled = true;
						
						Bukkit.broadcastMessage(prefix() + "BestBTC has been enabled.");
					} else if (args[0].equalsIgnoreCase("disable")) {
						if (!enabled) {
							sender.sendMessage(ChatColor.RED + "BestBTC is already disabled.");
							return true;
						}
						
						list.clear();
						task.cancel();
						enabled = false;
						Bukkit.broadcastMessage(prefix() + "BestBTC has been disabled.");
					} else {
						sender.sendMessage(Main.prefix() + "Help for best btc:");
						sender.sendMessage(ChatColor.GRAY + "- §f/btc enable - Enables the scenario.");
						sender.sendMessage(ChatColor.GRAY + "- §f/btc disable - Disables the scenario.");
						sender.sendMessage(ChatColor.GRAY + "- §f/btc add <player> - Adds an player manually to the list.");
						sender.sendMessage(ChatColor.GRAY + "- §f/btc remove <player> - Removes an player manually to the list.");
					}
					return true;
				}
				
				if (args[0].equalsIgnoreCase("add")) {
					if (!enabled) {
						sender.sendMessage(ChatColor.RED + "BestBTC is not enabled.");
						return true;
					}
					
					if (list.contains(args[1])) {
						sender.sendMessage(ChatColor.RED + args[1] + " already on the best btc list.");
						return true;
					}
					
					list.add(args[1]);
					sender.sendMessage(Main.prefix() + args[1] + " added to the best btc list.");
				} else if (args[0].equalsIgnoreCase("remove")) {
					if (!enabled) {
						sender.sendMessage(ChatColor.RED + "BestBTC is not enabled.");
						return true;
					}
					
					if (!list.contains(args[1])) {
						sender.sendMessage(ChatColor.RED + args[1] + " is not on the best btc list.");
						return true;
					}
					
					list.remove(args[1]);
					sender.sendMessage(Main.prefix() + args[1] + " removed from the best btc list.");
				} else if (args[0].equalsIgnoreCase("enable")) {
					if (enabled) {
						sender.sendMessage(ChatColor.RED + "BestBTC is already enabled.");
						return true;
					}
					
					for (Player online : Bukkit.getServer().getOnlinePlayers()) {
						list.add(online.getName());
					}
					
					this.task = new BukkitRunnable() {
						public void run() {
							for (Player online : Bukkit.getOnlinePlayers()) {
								if (list.contains(online.getName())) {
									online.setMaxHealth(online.getMaxHealth() + 2);
									online.setHealth(online.getHealth() + 2);
									online.sendMessage(ChatColor.GREEN + "You were rewarded for your BTC skills!");
								} else {
									online.sendMessage(ChatColor.GREEN + "BestBTC players gained a heart!");
								}
							}
						}
					};
					
					task.runTaskTimer(Main.plugin, 12000, 12000);
					enabled = true;
					
					Bukkit.broadcastMessage(prefix() + "BestBTC has been enabled.");
				} else if (args[0].equalsIgnoreCase("disable")) {
					if (!enabled) {
						sender.sendMessage(ChatColor.RED + "BestBTC is already disabled.");
						return true;
					}
					
					list.clear();
					task.cancel();
					enabled = false;
					Bukkit.broadcastMessage(prefix() + "BestBTC has been disabled.");
				} else {
					sender.sendMessage(Main.prefix() + "Help for best btc:");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc enable - Enables the scenario.");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc disable - Disables the scenario.");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc add <player> - Adds an player manually to the list.");
					sender.sendMessage(ChatColor.GRAY + "- §f/btc remove <player> - Removes an player manually to the list.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}
		return true;
	}
}