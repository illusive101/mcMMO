package com.gmail.nossr50.skills;

import java.util.Random;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.nossr50.Users;
import com.gmail.nossr50.m;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.locale.mcLocale;
import com.gmail.nossr50.party.Party;

public class Scythes {

	// Generator
	private static Random random = new Random();

	/**
	 * Scythe slow ability!
	 */

	public static void slowCheck(Player attack, EntityDamageByEntityEvent event) {

		Entity target = event.getEntity();

		if (target instanceof Player) {
			Player defender = (Player) target;
			PlayerProfile PPd = Users.getProfile(attack);

			final int MAX_BONUS_LEVEL = 1000;
			int skillLevel = PPd.getSkillLevel(SkillType.SCYTHES);
			int skillCheck = m.skillCheck(skillLevel, MAX_BONUS_LEVEL);

			if (random.nextInt(5000) <= skillCheck) {
				defender.addPotionEffect(new PotionEffect(
						PotionEffectType.SLOW, 200, 2));
				defender.sendMessage(mcLocale.getString("Scythes.HitSlowEffect"));

				if (attack instanceof Player)
					attack.sendMessage(mcLocale.getString("Scythes.SlowEffect"));
			}
		}
	}

	/**
	 * Check for critical chances on scythe damage.
	 * 
	 * @param attacker
	 *            The attacking player
	 * @param event
	 *            The event to modify
	 */
	public static void scytheCriticalCheck(Player attacker,
			EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();

		if (entity instanceof Wolf) {
			Wolf wolf = (Wolf) entity;

			if (wolf.isTamed()) {
				AnimalTamer tamer = wolf.getOwner();

				if (tamer instanceof Player) {
					Player owner = (Player) tamer;

					if (owner == attacker
							|| Party.getInstance().inSameParty(attacker, owner)) {
						return;
					}
				}
			}
		}

		final int MAX_BONUS_LEVEL = 750;
		final double PVP_MODIFIER = 1.5;
		final int PVE_MODIFIER = 2;

		PlayerProfile PPa = Users.getProfile(attacker);
		int skillLevel = PPa.getSkillLevel(SkillType.SCYTHES);
		int skillCheck = m.skillCheck(skillLevel, MAX_BONUS_LEVEL);

		if (random.nextInt(1000) <= skillCheck && !entity.isDead()) {
			int damage = event.getDamage();

			if (entity instanceof Player) {
				event.setDamage((int) (damage * PVP_MODIFIER));
				Player player = (Player) entity;
				player.sendMessage(mcLocale.getString("Scythes.HitCritically"));
			} else {
				event.setDamage(damage * PVE_MODIFIER);
			}
			attacker.sendMessage(mcLocale.getString("Scythes.CriticalHit"));
		}
	}

}
