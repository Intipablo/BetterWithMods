package betterwithmods.testing;

import betterwithmods.common.registry.bulk.manager.CraftingManagerBulk;
import betterwithmods.common.registry.bulk.recipes.BulkRecipe;
import betterwithmods.testing.base.BaseBulkTest;
import betterwithmods.testing.base.Before;
import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreIngredient;

import java.util.List;

public class BulkRecipeTests extends BaseBulkTest<BulkRecipe> {


    @Before
    public void beforeTest() {
        TEST_MANAGER = new CraftingManagerBulk<>();
        List<Ingredient> inputs = Lists.newArrayList(Ingredient.fromStacks(new ItemStack(Items.BONE)));
        List<Ingredient> inputs2 = Lists.newArrayList(new OreIngredient("bone"));
        List<ItemStack> outputs = Lists.newArrayList(new ItemStack(Blocks.GRAVEL));

        recipe = new BulkRecipe(inputs, outputs);
        oreRecipe = new BulkRecipe(inputs2, outputs);
    }
}
