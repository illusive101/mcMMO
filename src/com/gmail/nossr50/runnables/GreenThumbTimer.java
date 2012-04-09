package com.gmail.nossr50.runnables;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.gmail.nossr50.datatypes.PlayerProfile;

public class GreenThumbTimer implements Runnable {
    private Block block;
    private PlayerProfile PP;

    public GreenThumbTimer(Block block, PlayerProfile PP) {
        this.block = block;
        this.PP = PP;
    }

    @Override
    public void run() {
        block.setType(Material.CROPS);
        
        /*
        //This replants the wheat at a certain stage in development based on Herbalism Skill
        */
            block.setData(CropState.MEDIUM.getData());
    }
}
