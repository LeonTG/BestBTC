package com.leontg77.bestbtc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {
	private Set<String> list = new HashSet<String>();
	private boolean enabled = false;
	private BukkitRunnable task;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	/**
	 * Get the UHC prefix with an ending color.
	 * @param endcolor the ending color.
	 * @return The UHC prefix.
	 */
	public String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', "&a&lBestBTC &8&l>> &7");
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(!enabled) {
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if(!list.contains(player.getName()) && block.getType() == Material.DIAMOND_ORE) {
			getServer().broadcastMessage(ChatColor.GREEN + player.getName() + " mined a diamond! He is back on the Best BTC List!");
			list.add(player.getName());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!enabled) {
			return;
		}
		
		Player player = event.getPlayer();

		if(list.contains(player.getName()) && event.getTo().getBlockY() > 50) {
			getServer().broadcastMessage(ChatColor.RED + player.getName() + " moved above y:50!");
			list.remove(player.getName());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("blist")) {
			if(!enabled) {
				sender.sendMessage(ChatColor.RED + "BestBTC is not enabled.");
				return true;
			}
			
			StringBuilder pvelist = new StringBuilder();
			List<String> pve = new ArrayList<String>(list);
			
			for (int i = 0; i < pve.size(); i++) {
				if(pvelist.length() > 0 && i == pve.size() - 1) {
					pvelist.append(ChatColor.translateAlternateColorCodes('&', " &7 and &6"));
				}else {
					pvelist.append(ChatColor.translateAlternateColorCodes('&', "&7, &6"));
				}
				
				pvelist.append(ChatColor.GOLD + pve.get(i));
			}
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + "People still on the best pve list: &6" + (pvelist.length() > 0 ? pvelist.toString().trim() : "None") + "&7."));
		}else if(cmd.getName().equalsIgnoreCase("btc")) {
			if(sender.hasPermission("btc.manage")) {
				if(args.length == 0) {
					sendHelp(sender);
					return true;
				}
				
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("enable")) {
						if(enabled) {
							sender.sendMessage(ChatColor.RED + "BestBTC is already enabled.");
							return true;
						}
						
						for(Player online : getServer().getOnlinePlayers()) {
							list.add(online.getName());
						}
						
						task = new BukkitRunnable() {
							@Override
							public void run() {
								for(Player online : Bukkit.getOnlinePlayers()) {
									if(list.contains(online.getName())) {
										online.setMaxHealth(online.getMaxHealth() + 2);
										online.setHealth(online.getHealth() + 2);
										online.sendMessage(ChatColor.GREEN + "You were rewarded for your BTC skills!");
									}else {
										online.sendMessage(ChatColor.GREEN + "BestBTC players gained a heart!");
									}
								}
							}
						};
						
						task.runTaskTimer(this, 12000L, 12000L);
						enabled = true;
						
						getServer().broadcastMessage(getPrefix() + "BestBTC has been enabled.");
					}else if(args[0].equalsIgnoreCase("disable")) {
						if(!enabled) {
							sender.sendMessage(ChatColor.RED + "BestBTC is already disabled.");
							return true;
						}
						
						list.clear();
						task.cancel();
						enabled = false;
						getServer().broadcastMessage(getPrefix() + "BestBTC has been disabled.");
					}else {
						sendHelp(sender);
					}
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("add")) {
					if(!enabled) {
						sender.sendMessage(ChatColor.RED + "BestBTC is not enabled.");
						return true;
					}
					
					if(args.length != 2) {
						sendHelp(sender);
						return true;
					}
					
					if(list.contains(args[1])) {
						sender.sendMessage(ChatColor.RED + args[1] + " is already on the BestBTC list.");
						return true;
					}
					
					list.add(args[1]);
					sender.sendMessage(getPrefix() + args[1] + " added to the BestBTC list.");
				}else if(args[0].equalsIgnoreCase("remove")) {
					if(!enabled) {
						sender.sendMessage(ChatColor.RED + "BestBTC is not enabled.");
						return true;
					}
					
					if(args.length != 2) {
						sendHelp(sender);
						return true;
					}
					
					if(!list.contains(args[1])) {
						sender.sendMessage(ChatColor.RED + args[1] + " is not on the BestBTC list.");
						return true;
					}
					
					list.remove(args[1]);
					sender.sendMessage(getPrefix() + args[1] + " removed from the BestBTC list.");
				}else {
					sendHelp(sender);
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return true;
			}
		}
		
		return false;
	}
	
	private void sendHelp(CommandSender sender) {
		sender.sendMessage(getPrefix() + "Help for BestBTC:");
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &f/btc enable - Enables the scenario."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &f/btc disable - Disables the scenario."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &f/btc add <player> - Adds a player manually to the list."));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &f/btc remove <player> - Removes a player manually from the list."));
	}
}