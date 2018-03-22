package betterwithmods.module.gameplay;

import betterwithmods.common.BWMBlocks;
import betterwithmods.module.Feature;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import static betterwithmods.module.gameplay.CauldronRecipes.addCauldronRecipe;

/**
 * Created by primetoxinz on 6/25/17.
 */
public class NetherGrowth extends Feature {

    @Override
    public void init(FMLInitializationEvent event) {
        addCauldronRecipe(new ItemStack(BWMBlocks.NETHER_GROWTH), new ItemStack[]{
                new ItemStack(Blocks.BROWN_MUSHROOM),
                new ItemStack(Blocks.RED_MUSHROOM),
                new ItemStack(Blocks.MYCELIUM),
                new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.ROTTEN_FLESH),
                new ItemStack(BWMBlocks.SOUL_URN)
        });
    }

    @Override
    public String getFeatureDescription() {
        return "Adds Nether Growth, a fungus that will *infest* the Nether and stop all mobs from spawning. Be sure before placing it!";
    }
}
