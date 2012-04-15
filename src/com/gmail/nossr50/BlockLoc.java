package com.gmail.nossr50;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockLoc implements Serializable {
	private static final long serialVersionUID = 5843412966390363475L;
	public int x, y, z;

	public BlockLoc(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockLoc(Location loc) {
		this.x = (int) loc.getX();
		this.y = (int) loc.getY();
		this.z = (int) loc.getZ();
	}

	public BlockLoc(Block block) {
		this(block.getLocation());
	}

	public Location toLocation(World w) {
		return new Location(w, x, y, z);
	}

	public Block getBlock(World w) {
		return w.getBlockAt(x, y, z);
	}

	@Override
	public int hashCode() {
		int hash = 7;

		hash = 31 * hash + x;
		hash = 31 * hash + y;
		hash = 31 * hash + z;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockLoc other = (BlockLoc) obj;
		return x == other.x && y == other.y && z == other.z;
	}
}