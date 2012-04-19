package com.gmail.nossr50.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.getspout.spoutapi.sound.SoundEffect;

import com.gmail.nossr50.BlockChecks;
import com.gmail.nossr50.ItemChecks;
import com.gmail.nossr50.Users;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.mcPermissions;
import com.gmail.nossr50.config.LoadProperties;
import com.gmail.nossr50.datatypes.AbilityType;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.datatypes.ToolType;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import com.gmail.nossr50.events.fake.FakePlayerAnimationEvent;
import com.gmail.nossr50.skills.Excavation;
import com.gmail.nossr50.skills.Mining;
import com.gmail.nossr50.skills.Repair;
import com.gmail.nossr50.skills.Skills;
import com.gmail.nossr50.skills.Unarmed;
import com.gmail.nossr50.skills.WoodCutting;
import com.gmail.nossr50.spout.SpoutSounds;

public class mcBlockListener implements Listener {
	private final mcMMO plugin;

	public mcBlockListener(final mcMMO plugin) {
		this.plugin = plugin;
	}

	/**
	 * Monitor BlockPistonExtend events.
	 *
	 * @param event The event to monitor
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		List<Block> blocks = event.getBlocks();
		BlockFace direction = event.getDirection();

		for (Block b : blocks) {
			if (mcMMO.isBlockPlaced(b)) {
				b.getRelative(direction).setMetadata("mcmmoNeedsTracking", new FixedMetadataValue(plugin, true));
				mcMMO.removeBlockPlacedMetadata(b, plugin);
			}
		}

		for (Block b : blocks) {
			if (b.getRelative(direction).hasMetadata("mcmmoNeedsTracking")) {
				mcMMO.setBlockPlacedMetadata(b.getRelative(direction), plugin);
				b.getRelative(direction).removeMetadata("mcmmoNeedsTracking", plugin);
			}
		}
	}

	/**
	 * Monitor BlockPistonRetract events.
	 *
	 * @param event The event to monitor
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		Block block = event.getRetractLocation().getBlock();

		if (event.isSticky() && mcMMO.isBlockPlaced(block)) {
			mcMMO.removeBlockPlacedMetadata(block, plugin);
			mcMMO.setBlockPlacedMetadata(event.getBlock().getRelative(event.getDirection()), plugin);
		}
	}

	/**
	 * Monitor BlockPlace events.
	 *
	 * @param event The event to monitor
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		int id = block.getTypeId();
		Material mat = block.getType();

		/* Code to prevent issues with placed falling Sand/Gravel not being tracked */
		if (mat.equals(Material.SAND) || mat.equals(Material.GRAVEL)) {
			for (int y = -1;  y + block.getY() >= 0; y--) {
				if (block.getRelative(0, y, 0).getType().equals(Material.AIR)) {
					continue;
				}
				else {
					Block newLocation = block.getRelative(0, y + 1, 0);
					mcMMO.setBlockPlacedMetadata(newLocation, plugin);
					break;
				}
			}
		}

		/* Check if the blocks placed should be monitored so they do not give out XP in the future */
		if (BlockChecks.shouldBeWatched(mat)) {
			mcMMO.setBlockPlacedMetadata(block, plugin);
		}

		if (id == LoadProperties.anvilID && LoadProperties.anvilmessages) {
			Repair.placedAnvilCheck(player, id);
		}
	}

	/**
	 * Monitor BlockBreak events.
	 *
	 * @param event The event to monitor
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {

		Player player = event.getPlayer();
		PlayerProfile PP = Users.getProfile(player);
		Block block = event.getBlock();
		Material mat = block.getType();
		ItemStack inhand = player.getItemInHand();

		if (event instanceof FakeBlockBreakEvent) {
			return;
		}

		/*
		 * HERBALISM
		 */

		/* Green Terra 
        if (PP.getToolPreparationMode(ToolType.HOE) && mcPermissions.getInstance().greenTerra(player) && ((mat.equals(Material.CROPS) && block.getData() == CropState.RIPE.getData()) || Herbalism.canBeGreenTerra(mat))) {
            Skills.abilityCheck(player, SkillType.HERBALISM);
        }
		 */
		/* Triple drops 
        if (PP.getAbilityMode(AbilityType.GREEN_TERRA) && Herbalism.canBeGreenTerra(mat)) {
            Herbalism.herbalismProcCheck(block, player, event, plugin);
        }

        if (mcPermissions.getInstance().herbalismDoubleDrops(player) && Herbalism.canBeGreenTerra(mat)) {
            Herbalism.herbalismProcCheck(block, player, event, plugin);
        }
		 */
		/*
		 * MINING
		 */

		if (mcPermissions.getInstance().mining(player) && Mining.canBeSuperBroken(mat)) {
			if (LoadProperties.miningrequirespickaxe && ItemChecks.isMiningPick(inhand)) {
				Mining.miningBlockCheck(player, block);
			}
			else if (!LoadProperties.miningrequirespickaxe) {
				Mining.miningBlockCheck(player, block);
			}
		}

		/*
		 * WOOD CUTTING
		 */

		if (mcPermissions.getInstance().woodcutting(player) && mat.equals(Material.LOG)) {
			if (LoadProperties.woodcuttingrequiresaxe && ItemChecks.isAxe(inhand)) {
				WoodCutting.woodcuttingBlockCheck(player, block);
			}
			else if (!LoadProperties.woodcuttingrequiresaxe) {
				WoodCutting.woodcuttingBlockCheck(player, block);
			}
		}

		if (PP.getAbilityMode(AbilityType.TREE_FELLER) && mcPermissions.getInstance().treeFeller(player)) {
			WoodCutting.treeFeller(event);
		}

		/*
		 * EXCAVATION
		 */

		if (Excavation.canBeGigaDrillBroken(mat) && mcPermissions.getInstance().excavation(player) && !mcMMO.isBlockPlaced(block)) {
			if (LoadProperties.excavationRequiresShovel && ItemChecks.isShovel(inhand)) {
				Excavation.excavationProcCheck(block, player);
			}
			else if (!LoadProperties.excavationRequiresShovel) {
				Excavation.excavationProcCheck(block, player);
			}
		}

		//Remove metadata when broken
		if (mcMMO.isBlockPlaced(block) && BlockChecks.shouldBeWatched(mat)) {
			mcMMO.removeBlockPlacedMetadata(block, plugin);
		}
	}

	/**
	 * Monitor BlockDamage events.
	 *
	 * @param event The event to monitor
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event) {
		final int LEAF_BLOWER_LEVEL = 100;

		Player player = event.getPlayer();
		PlayerProfile PP = Users.getProfile(player);
		ItemStack inhand = player.getItemInHand();
		Block block = event.getBlock();
		Material mat = block.getType();

		/*
		 * ABILITY PREPARATION CHECKS
		 */
		if (BlockChecks.abilityBlockCheck(mat)) {
			/*
            if (PP.getToolPreparationMode(ToolType.HOE) && (Herbalism.canBeGreenTerra(mat) || Herbalism.makeMossy(mat))) {
                Skills.abilityCheck(player, SkillType.HERBALISM);
            }
			 */
			if (PP.getToolPreparationMode(ToolType.AXE) && mat.equals(Material.LOG) && mcPermissions.getInstance().treeFeller(player)) {  //Why are we checking the permissions here?
				Skills.abilityCheck(player, SkillType.WOODCUTTING);
			}
			else if (PP.getToolPreparationMode(ToolType.PICKAXE) && Mining.canBeSuperBroken(mat)) {
				Skills.abilityCheck(player, SkillType.MINING);
			}
			else if (PP.getToolPreparationMode(ToolType.SHOVEL) && Excavation.canBeGigaDrillBroken(mat)) {
				Skills.abilityCheck(player, SkillType.EXCAVATION);
			}
			else if (PP.getToolPreparationMode(ToolType.FISTS) && (Excavation.canBeGigaDrillBroken(mat) || mat.equals(Material.SNOW))) {
				Skills.abilityCheck(player, SkillType.UNARMED);
			}
		}
		/*
		 * ABILITY TRIGGER CHECKS

        if (PP.getAbilityMode(AbilityType.GREEN_TERRA) && mcPermissions.getInstance().greenTerra(player) && Herbalism.makeMossy(mat)) {
            Herbalism.greenTerra(player, block);
        }
		 */
		if (PP.getAbilityMode(AbilityType.GIGA_DRILL_BREAKER) && Skills.triggerCheck(player, block, AbilityType.GIGA_DRILL_BREAKER)) {
			if (LoadProperties.excavationRequiresShovel && ItemChecks.isShovel(inhand) && Excavation.canBeGigaDrillBroken(block.getType())) {
				event.setInstaBreak(true);
				Excavation.gigaDrillBreaker(player, block);
			}
			else if (!LoadProperties.excavationRequiresShovel) {
				event.setInstaBreak(true);
				Excavation.gigaDrillBreaker(player, block);
			}
		}
		else if (PP.getAbilityMode(AbilityType.BERSERK) && Skills.triggerCheck(player, block, AbilityType.BERSERK) && Unarmed.canBeBeserked(block.getType())) {
			if (inhand.getType().equals(Material.AIR)) {
				FakePlayerAnimationEvent armswing = new FakePlayerAnimationEvent(player);
				Bukkit.getPluginManager().callEvent(armswing);

				event.setInstaBreak(true);
			}

			if (LoadProperties.spoutEnabled) {
				SpoutSounds.playSoundForPlayer(SoundEffect.POP, player, block.getLocation());
			}
		}
		else if (PP.getAbilityMode(AbilityType.SUPER_BREAKER) && Skills.triggerCheck(player, block, AbilityType.SUPER_BREAKER) && Mining.canBeSuperBroken(block.getType())) {
			if (LoadProperties.miningrequirespickaxe && ItemChecks.isMiningPick(inhand)) {
				event.setInstaBreak(true);
				Mining.SuperBreakerBlockCheck(player, block);
			}
			else if (!LoadProperties.miningrequirespickaxe) {
				event.setInstaBreak(true);
				Mining.SuperBreakerBlockCheck(player, block);
			}
		}
		else if (PP.getSkillLevel(SkillType.WOODCUTTING) >= LEAF_BLOWER_LEVEL && mat.equals(Material.LEAVES)) {
			if (LoadProperties.woodcuttingrequiresaxe && ItemChecks.isAxe(inhand)) {
				if (Skills.triggerCheck(player, block, AbilityType.LEAF_BLOWER)) {
					event.setInstaBreak(true);
					WoodCutting.leafBlower(player, block);
				}
			}
			else if (!LoadProperties.woodcuttingrequiresaxe && !inhand.getType().equals(Material.SHEARS)) {
				if (Skills.triggerCheck(player, block, AbilityType.LEAF_BLOWER)) {
					event.setInstaBreak(true);
					WoodCutting.leafBlower(player, block);
				}
			}
		}
	}
}
