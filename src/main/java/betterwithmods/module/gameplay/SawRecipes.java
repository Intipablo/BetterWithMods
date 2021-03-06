package betterwithmods.module.gameplay;

import betterwithmods.api.util.IWood;
import betterwithmods.common.BWMBlocks;
import betterwithmods.common.BWOreDictionary;
import betterwithmods.common.BWRegistry;
import betterwithmods.common.registry.block.recipe.BlockDropIngredient;
import betterwithmods.common.registry.block.recipe.BlockIngredient;
import betterwithmods.common.registry.block.recipe.SawRecipe;
import betterwithmods.module.Feature;
import betterwithmods.util.InvUtils;
import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import java.util.Random;

/**
 * Created by primetoxinz on 5/16/17.
 */
public class SawRecipes extends Feature {
    private static int plankCount, barkCount, sawDustCount;

    public SawRecipes() {
        canDisable = false;
    }

    @Override
    public void setupConfig() {
        //TODO make the default 4 in 1.13
        plankCount = loadPropInt("Saw Plank Output", "Plank count that is output when a log is chopped by a Saw.", 6);
        barkCount = loadPropInt("Saw Bark Output", "Bark count that is output when a log is chopped by a Saw.", 1);
        sawDustCount = loadPropInt("Saw sawdust Output", "Sawdust count that is output when a log is chopped by a Saw.", 2);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(Blocks.PUMPKIN));
        BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(Blocks.VINE));
        BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(Blocks.YELLOW_FLOWER));
        BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(Blocks.BROWN_MUSHROOM));
        BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(Blocks.RED_MUSHROOM));
        BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(BWMBlocks.ROPE));
        for (int i = 0; i < 9; i++)
            BWRegistry.WOOD_SAW.addSelfdropRecipe(new ItemStack(Blocks.RED_FLOWER, 1, i));
        BWRegistry.WOOD_SAW.addRecipe(new SawRecipe(new BlockIngredient(new ItemStack(Blocks.MELON_BLOCK)), NonNullList.create()) {
            @Override
            public NonNullList<ItemStack> getOutputs() {
                Random random = new Random();
                return InvUtils.asNonnullList(new ItemStack(Items.MELON, 3 + random.nextInt(5)));
            }
        });
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        for (IWood wood : BWOreDictionary.woods) {
            BWRegistry.WOOD_SAW.addRecipe(new BlockDropIngredient(wood.getLog(1)), Lists.newArrayList(wood.getPlank(plankCount), wood.getBark(barkCount), wood.getSawdust(sawDustCount)));
        }
    }


}
