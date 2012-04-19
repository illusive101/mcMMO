package com.gmail.nossr50.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.nossr50.Combat;
import com.gmail.nossr50.Users;
import com.gmail.nossr50.m;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.mcPermissions;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.locale.mcLocale;

public class Archery {

	private static Random random = new Random();

	/**
	 * Track arrows fired for later retrieval.
	 * 
	 * @param plugin
	 *            mcMMO plugin instance
	 * @param entity
	 *            Entity damaged by the arrow
	 * @param PPa
	 *            PlayerProfile of the player firing the arrow
	 */
	public static void trackArrows(mcMMO plugin, Entity entity,
			PlayerProfile PPa) {
		final int MAX_BONUS_LEVEL = 1000;
		int skillLevel = PPa.getSkillLevel(SkillType.ARCHERY);

		if (!plugin.arrowTracker.containsKey(entity)) {
			plugin.arrowTracker.put(entity, 0);
		}

		if (skillLevel > MAX_BONUS_LEVEL
				|| (random.nextInt(1000) <= skillLevel)) {
			plugin.arrowTracker.put(entity, 1);
		}
	}

	/**
	 * Check for Daze.
	 * 
	 * @param defender
	 *            Defending player
	 * @param attacker
	 *            Attacking player
	 */
	public static void dazeCheck(Player defender, Player attacker) {
		final int MAX_BONUS_LEVEL = 1000;

		int skillLevel = Users.getProfile(attacker).getSkillLevel(
				SkillType.ARCHERY);
		Location loc = defender.getLocation();
		int skillCheck = m.skillCheck(skillLevel, MAX_BONUS_LEVEL);

		if (random.nextInt(10) > 5) {
			loc.setPitch(90);
		} else {
			loc.setPitch(-90);
		}

		if (random.nextInt(2000) <= skillCheck
				&& mcPermissions.getInstance().daze(attacker)) {
			Bukkit.getLogger().info("Inside this function!");
			defender.teleport(loc);
			Combat.dealDamage(defender, 4);
			defender.sendMessage(mcLocale.getString("Combat.TouchedFuzzy"));
			attacker.sendMessage(mcLocale.getString("Combat.TargetDazed"));

		}
	}

	/**
	 * Check for arrow retrieval.
	 * 
	 * @param entity
	 *            The entity hit by the arrows
	 * @param plugin
	 *            mcMMO plugin instance
	 */
	public static void arrowRetrievalCheck(Entity entity, mcMMO plugin) {
		if (plugin.arrowTracker.containsKey(entity)) {
			m.mcDropItems(entity.getLocation(), new ItemStack(Material.ARROW),
					plugin.arrowTracker.get(entity));
		}

		plugin.arrowTracker.remove(entity);
	}

	public static Set<String> frozenPlayers = Collections
			.synchronizedSet(new HashSet<String>());

	public static void freezeCheck(final mcMMO plugin, final Player defender,
			Player attacker) {
		final int MAX_BONUS_LEVEL = 1000;

		int skillLevel = Users.getProfile(attacker).getSkillLevel(
				SkillType.ARCHERY);
		int skillCheck = m.skillCheck(skillLevel, MAX_BONUS_LEVEL);

		if (random.nextInt(2000) <= skillCheck) {

			defender.sendMessage(mcLocale.getString("Combat.Frozen"));
			attacker.sendMessage(mcLocale.getString("Combat.TargetFrozen"));
			
			frozenPlayers.add(defender.getName());

			final Location playerLoc = defender.getLocation();
			addIce(plugin, playerLoc, +1, 0, 0);
			addIce(plugin, playerLoc, -1, 0, 0);
			addIce(plugin, playerLoc, 0, 0, -1);
			addIce(plugin, playerLoc, 0, 0, +1);

			Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin,
					new Runnable() {
						@Override
						public void run() {
							frozenPlayers.remove(defender.getName());
							removeIce(plugin, playerLoc, +1, 0, 0);
							removeIce(plugin, playerLoc, -1, 0, 0);
							removeIce(plugin, playerLoc, 0, 0, -1);
							removeIce(plugin, playerLoc, 0, 0, +1);
						}
					}, 100);
		}
	}

	private static void addIce(mcMMO plugin, Location orig, int dx, int dy,
			int dz) {
		Location loc = getLocationRelativeTo(orig, dx, dy, dz);
		Block block = loc.getBlock();
		if (!block.isEmpty()) {
			return;
		}

		block.setType(Material.ICE);
	}

	private static void removeIce(mcMMO plugin, Location orig, int dx, int dy,
			int dz) {
		Location loc = getLocationRelativeTo(orig, dx, dy, dz);
		Block block = loc.getBlock();
		if (block.getType() != Material.ICE && block.getType() != Material.WATER) {
			return;
		}

		block.setType(Material.AIR);
		block.removeMetadata("mcMMOFrozenIce", plugin);
	}

	private static Location getLocationRelativeTo(Location orig, int dx,
			int dy, int dz) {
		return new Location(orig.getWorld(), orig.getX() + dx,
				orig.getY() + dy, orig.getZ() + dz);
	}

}