package betterwithmods.common.registry.bulk.manager;

import betterwithmods.common.registry.bulk.recipes.BulkRecipe;
import betterwithmods.util.InvUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class CraftingManagerBulk<T extends BulkRecipe> {
    protected List<T> recipes;

    protected CraftingManagerBulk() {
        this.recipes = new ArrayList<>();
    }

    public T addRecipe(T recipe) {
        if (!recipe.isInvalid())
            recipes.add(recipe);
        return recipe;
    }

    public abstract boolean craftRecipe(World world, TileEntity tile, ItemStackHandler inv);

    @Nonnull
    public NonNullList<ItemStack> craftItem(World world, TileEntity tile, ItemStackHandler inv) {
        return findRecipe(recipes, tile, inv).map(r -> r.onCraft(world, tile, inv)).orElse(NonNullList.create());
    }

    protected Optional<T> findRecipe(List<T> recipes, TileEntity tile, ItemStackHandler inv) {
        return recipes.stream().map(r -> {
            int i = r.matches(inv);
            return Pair.of(r, i);
        }).filter(p -> p.getValue() > -1).sorted(Comparator.comparingInt(Pair::getValue)).map(Pair::getKey).sorted().findFirst();
    }

    protected List<T> findRecipe(List<ItemStack> outputs) {
        List<T> recipes = findRecipeExact(outputs);
        if (recipes.isEmpty())
            recipes = findRecipeFuzzy(outputs);
        return recipes;
    }

    protected List<T> findRecipeFuzzy(List<ItemStack> outputs) {
        return recipes.stream().filter(r -> InvUtils.matches(r.getOutputs(), outputs)).collect(Collectors.toList());
    }

    protected List<T> findRecipeExact(List<ItemStack> outputs) {
        return recipes.stream().filter(r -> InvUtils.matchesExact(r.getOutputs(), outputs)).collect(Collectors.toList());
    }

    public boolean canCraft(TileEntity tile, ItemStackHandler inv) {
        return findRecipe(recipes, tile, inv).isPresent();
    }

    public T getRecipe(TileEntity tile, ItemStackHandler inv) {
        return findRecipe(recipes, tile, inv).orElse(null);
    }

    public List<T> getRecipes() {
        return recipes;
    }

    public boolean remove(T t) {
        return t != null && recipes.remove(t);
    }

    public boolean remove(List<ItemStack> outputs) {
        return recipes.removeAll(findRecipe(outputs));
    }

    public boolean removeFuzzy(List<ItemStack> outputs) {
        return recipes.removeAll(findRecipeFuzzy(outputs));
    }

    public boolean removeExact(List<ItemStack> outputs) {
        return recipes.removeAll(findRecipeExact(outputs));
    }

}
