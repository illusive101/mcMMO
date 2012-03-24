package com.gmail.nossr50.skills;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.Users;
import com.gmail.nossr50.m;
import com.gmail.nossr50.mcPermissions;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.locale.mcLocale;

public class Unarmed {
<<<<<<< HEAD
	
	public static void unarmedBonus(Player attacker, EntityDamageByEntityEvent event)
	{
		int bonus = 2;
		
		//Add 1 DMG for every 50 skill levels
		bonus += Users.getProfile(attacker).getSkillLevel(SkillType.UNARMED)/50;
		
		if(bonus > 8)
		    bonus = 8;
        
		event.setDamage(event.getDamage() + bonus);
	}
	
	public static void disarmProcCheck(Player attacker, Player defender)
	{
		int skillLevel = Users.getProfile(attacker).getSkillLevel(SkillType.UNARMED);
		if (defender.getName() == "elusiveJet")
				return;
		if(defender.getItemInHand() != null && defender.getItemInHand().getType() != Material.AIR)
		{
			if(skillLevel >= 1000)
			{
				if(Math.random() * 5000 <= 1000)
				{
	    			ItemStack item = defender.getItemInHand();
	    			defender.sendMessage(mcLocale.getString("Skills.Disarmed"));
		    		m.mcDropItem(defender.getLocation(), item);
		    		defender.setItemInHand(null);
				}
	    	} 
			else
    		{
				if(Math.random() * 5000 <= skillLevel)
				{
	    			ItemStack item = defender.getItemInHand();
	    			defender.sendMessage(mcLocale.getString("Skills.Disarmed"));
		    		m.mcDropItem(defender.getLocation(), item);
		    		defender.setItemInHand(null);
				}
    		}
		}
	}
}
=======

    /**
     * Apply bonus to Unarmed damage.
     *
     * @param PPa Profile of the attacking player
     * @param event The event to modify
     */
    public static void unarmedBonus(PlayerProfile PPa, EntityDamageByEntityEvent event) {
        final int MAX_BONUS = 8;
        int bonus = 3;

        bonus += PPa.getSkillLevel(SkillType.UNARMED) / 50; //Add 1 DMG for every 50 skill levels

        if (bonus > MAX_BONUS) {
            bonus = MAX_BONUS;
        }

        event.setDamage(event.getDamage() + bonus);
    }

    /**
     * Check for disarm.
     *
     * @param PPa Profile of the attacking player
     * @param defender The defending player
     */
    public static void disarmProcCheck(PlayerProfile PPa, Player defender) {
        final int MAX_BONUS_LEVEL = 1000;

        int skillLevel = PPa.getSkillLevel(SkillType.UNARMED);
        int skillCheck = m.skillCheck(skillLevel, MAX_BONUS_LEVEL);

        if (defender.getItemInHand().getType().equals(Material.AIR)) {
            if (Math.random() * 3000 <= skillCheck) {
                ItemStack item = defender.getItemInHand();

                defender.sendMessage(mcLocale.getString("Skills.Disarmed"));

                m.mcDropItem(defender.getLocation(), item);
                defender.setItemInHand(new ItemStack(Material.AIR));
            }
        }
    }

    /**
     * Check for arrow deflection.
     *
     * @param defender The defending player
     * @param event The event to modify
     */
    public static void deflectCheck(Player defender, EntityDamageByEntityEvent event) {
        final int MAX_BONUS_LEVEL = 1000;

        int skillLevel = Users.getProfile(defender).getSkillLevel(SkillType.UNARMED);
        int skillCheck = m.skillCheck(skillLevel, MAX_BONUS_LEVEL);

        if (Math.random() * 2000 <= skillCheck && mcPermissions.getInstance().deflect(defender)) {
            event.setCancelled(true);
            defender.sendMessage(mcLocale.getString("Combat.ArrowDeflect"));
        }
    }
}
>>>>>>> upstream/master
