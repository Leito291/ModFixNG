/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package modfixng.main;

import java.util.HashSet;

import modfixng.utils.ModFixNGUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor, Listener {

	private ModFixNG main;
	private Config config;

	public Commands(ModFixNG main, Config config) {
		this.main = main;
		this.config = config;
	}

	private HashSet<String> pleinfoswitch = new HashSet<String>();
	private HashSet<String> plbinfoswitch = new HashSet<String>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] args) {
		Player player = null;

		// check permissions
		if ((sender instanceof Player)) {
			// Player, lets check if player isOp or have permission
			player = (Player) sender;
			if (!player.isOp() && !player.hasPermission("modfix.conf")) {
				sender.sendMessage(ChatColor.BLUE + "Нет прав");
				return true;
			}
		} else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
			// Success, this was from the Console or Remote Console
		} else {
			// Who are you people?
			sender.sendMessage(ChatColor.BLUE + "Ты вообще кто такой? давай до свиданья!");
			return true;
		}

		// now handle commands
		if (args.length == 0) {
			sender.sendMessage(ChatColor.BLUE + "Используйте команд /modfix help для получения списка комманд");
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			config.loadConfig();
			sender.sendMessage(ChatColor.BLUE + "Конфиг перезагружен");
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			displayHelp(sender);
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("iteminfo")) {
			displayItemInfo(sender);
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("blockinfo")) {
			displayBlockInfo(sender);
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("entityinfo")) {
			displayEntityInfo(sender);
			return true;
		} else if (args.length == 1 && args[0].equalsIgnoreCase("inventoryinfo")) {
			displayInventoryNameInfo(sender);
			return true;
		}
		return false;
	}

	private void displayHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + "/modfix reload " + ChatColor.WHITE + "-" + ChatColor.BLUE + " перезагрузить конфиг плагина");
		sender.sendMessage(ChatColor.AQUA + "/modfix iteminfo " + ChatColor.WHITE + "-" + ChatColor.BLUE + " получить id и subid итема в руке");
		sender.sendMessage(ChatColor.AQUA + "/modfix entityinfo " + ChatColor.WHITE + "-" + ChatColor.BLUE + " получить Entity Type ID");
		sender.sendMessage(ChatColor.AQUA + "/modfix blockinfo " + ChatColor.WHITE + "-" + ChatColor.BLUE + " получить id и subid блока");
		sender.sendMessage(ChatColor.AQUA + "/modfix inventoryinfo" + ChatColor.WHITE + "-" + ChatColor.BLUE + "получить имя открытого инвентаря");
	}

	private void displayItemInfo(CommandSender sender) {
		if (sender instanceof Player) {
			Player pl = (Player) sender;
			ItemStack i = pl.getItemInHand();
			String msg = ChatColor.BLUE + "Item id: " + i.getTypeId();
			if (i.getDurability() != 0) {
				msg += ", subid: " + pl.getItemInHand().getDurability();
			}
			pl.sendMessage(msg);
		} else {
			sender.sendMessage(ChatColor.BLUE + "У консоли нет рук");
		}
	}

	private void displayBlockInfo(CommandSender sender) {
		if (sender instanceof Player) {
			Player pl = (Player) sender;
			pl.sendMessage(ChatColor.BLUE + "Кликните правой кнопкой мыши по блоку, для того чтобы узнать его ID и subID");
			plbinfoswitch.add(pl.getName());
		} else {
			sender.sendMessage(ChatColor.BLUE + "У консоли нет рук");
		}
	}

	private void displayEntityInfo(CommandSender sender) {
		if (sender instanceof Player) {
			Player pl = (Player) sender;
			pl.sendMessage(ChatColor.BLUE + "Кликните правой кнопкой мыши по Entity, для того чтобы узнать её Type ID");
			pleinfoswitch.add(pl.getName());
		} else {
			sender.sendMessage(ChatColor.BLUE + "У консоли нет рук");
		}
	}

	private void displayInventoryNameInfo(CommandSender sender) {
		if (sender instanceof Player) {
			final Player pl = (Player) sender;
			pl.sendMessage(ChatColor.BLUE + "Откройте инвентарь и подождите 2 секунды для того чтобы узнать имя открытого инвентаря");
			Bukkit.getScheduler().scheduleSyncDelayedTask(main,
				new Runnable() {
					@Override
					public void run() {
						pl.sendMessage(ChatColor.BLUE + "Open inventory name: "+ModFixNGUtils.getOpenInventoryName(pl));
						pl.closeInventory();
					}
				}
			, 40);
		} else {
			sender.sendMessage(ChatColor.BLUE + "У консоли нет рук");
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerCheckEntityID(PlayerInteractEntityEvent e) {
		Player pl = e.getPlayer();
		if (pleinfoswitch.contains(pl.getName())) {
			pl.sendMessage(ChatColor.BLUE + "Entity Type ID: " + e.getRightClicked().getType().getTypeId());
			pleinfoswitch.remove(pl.getName());
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerCheckBlockID(PlayerInteractEvent e) {
		if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		Player pl = e.getPlayer();
		if (plbinfoswitch.contains(pl.getName())) {
			Block b = e.getClickedBlock();
			String msg = ChatColor.BLUE + "Block id: " + b.getTypeId();
			if (b.getData() != 0) {
				msg += ", subid: " + e.getClickedBlock().getData();
			}
			msg += ", has inventory: " + ModFixNGUtils.hasInventory(b);
			pl.sendMessage(msg);
			plbinfoswitch.remove(pl.getName());
			e.setCancelled(true);
		}

	}

}
