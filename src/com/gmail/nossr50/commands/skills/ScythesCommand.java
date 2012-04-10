package com.gmail.nossr50.commands.skills;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.Users;
import com.gmail.nossr50.mcPermissions;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.locale.mcLocale;
import com.gmail.nossr50.util.Page;

public class ScythesCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command does not support console useage."); //TODO: Needs more locale.
			return true;
		}

		Player player = (Player) sender;
		PlayerProfile PP = Users.getProfile(player);

		String percentage, slow;

		float skillvalue = (float) PP.getSkillLevel(SkillType.SCYTHES);
		if (PP.getSkillLevel(SkillType.SCYTHES) < 750)
			percentage = String.valueOf((skillvalue / 1000) * 100);
		else
			percentage = "75";
		
		int ticks = 2;
		int x = PP.getSkillLevel(SkillType.AXES);
		while (x >= 50) {
			x -= 50;
			ticks++;
		}
			
		slow = String.valueOf((skillvalue / 1000) * 100);

		player.sendMessage(mcLocale.getString("m.SkillHeader", new Object[] { mcLocale.getString("m.SkillScythes") }));
		player.sendMessage(mcLocale.getString("m.XPGain", new Object[] { mcLocale.getString("m.XPGainSwords") }));
		
		if (mcPermissions.getInstance().swords(player))
			player.sendMessage(mcLocale.getString("m.LVL", new Object[] { PP.getSkillLevel(SkillType.SCYTHES), PP.getSkillXpLevel(SkillType.SCYTHES), PP.getXpToLevel(SkillType.SCYTHES) }));
		
		player.sendMessage(mcLocale.getString("m.SkillHeader", new Object[] { mcLocale.getString("m.Effects") }));
		player.sendMessage(mcLocale.getString("m.EffectsTemplate", new Object[] { mcLocale.getString("m.EffectsScythes1_0"), mcLocale.getString("m.EffectsScythes1_1") }));
		player.sendMessage(mcLocale.getString("m.EffectsTemplate", new Object[] { mcLocale.getString("m.EffectsScythes2_0"), mcLocale.getString("m.EffectsScythes2_1") }));
		player.sendMessage(mcLocale.getString("m.EffectsTemplate", new Object[] { mcLocale.getString("m.EffectsScythes3_0"), mcLocale.getString("m.EffectsScythes3_1") }));
		player.sendMessage(mcLocale.getString("m.SkillHeader", new Object[] { mcLocale.getString("m.YourStats") }));
		player.sendMessage(mcLocale.getString("m.ScythesCritChance", new Object[] { percentage }));
        player.sendMessage(mcLocale.getString("m.ScythesPlagueLength", new Object[] { ticks }));
        player.sendMessage(mcLocale.getString("m.ScythesSlowChance", new Object[] { slow }));
		
		Page.grabGuidePageForSkill(SkillType.SCYTHES, player, args);

		return true;
	}
}
