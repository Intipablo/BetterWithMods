package betterwithmods.module.compat.jei;

import betterwithmods.client.container.anvil.ContainerSteelAnvil;
import betterwithmods.client.container.anvil.GuiSteelAnvil;
import betterwithmods.client.container.bulk.GuiCauldron;
import betterwithmods.client.container.bulk.GuiCrucible;
import betterwithmods.client.container.bulk.GuiMill;
import betterwithmods.common.BWMBlocks;
import betterwithmods.common.BWMItems;
import betterwithmods.common.blocks.mechanical.BlockCookingPot;
import betterwithmods.common.blocks.mechanical.BlockMechMachines;
import betterwithmods.common.items.ItemMaterial;
import betterwithmods.common.registry.HopperInteractions;
import betterwithmods.common.registry.anvil.AnvilCraftingManager;
import betterwithmods.common.registry.anvil.ShapedAnvilRecipe;
import betterwithmods.common.registry.anvil.ShapelessAnvilRecipe;
import betterwithmods.common.registry.blockmeta.managers.KilnManager;
import betterwithmods.common.registry.blockmeta.managers.SawManager;
import betterwithmods.common.registry.blockmeta.managers.TurntableManager;
import betterwithmods.common.registry.blockmeta.recipe.KilnRecipe;
import betterwithmods.common.registry.blockmeta.recipe.SawRecipe;
import betterwithmods.common.registry.blockmeta.recipe.TurntableRecipe;
import betterwithmods.common.registry.bulk.manager.*;
import betterwithmods.common.registry.bulk.recipes.*;
import betterwithmods.common.registry.crafting.ToolBaseRecipe;
import betterwithmods.module.compat.jei.category.*;
import betterwithmods.module.compat.jei.wrapper.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.gui.Focus;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static betterwithmods.common.blocks.mechanical.BlockCookingPot.EnumType.CAULDRON;
import static betterwithmods.common.blocks.mechanical.BlockCookingPot.EnumType.CRUCIBLE;
import static betterwithmods.common.blocks.mechanical.BlockMechMachines.EnumType.*;

@mezz.jei.api.JEIPlugin
public class JEI implements IModPlugin {
    public static IJeiHelpers HELPER;

    public static IJeiRuntime JEI_RUNTIME;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (JEI_RUNTIME == null) {
            JEI_RUNTIME = jeiRuntime;
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {

        final IJeiHelpers helpers = reg.getJeiHelpers();
        final IGuiHelper guiHelper = helpers.getGuiHelper();
        reg.addRecipeCategories(new CauldronRecipeCategory(guiHelper), new StokedCauldronRecipeCategory(guiHelper), new CrucibleRecipeCategory(guiHelper),
                new StokedCrucibleRecipeCategory(guiHelper), new MillRecipeCategory(guiHelper), new SawRecipeCategory(guiHelper), new SteelSawRecipeCategory(guiHelper), new KilnRecipeCategory(guiHelper),
                new TurntableRecipeCategory(guiHelper), new HopperRecipeCategory(guiHelper), new SteelAnvilRecipeCategory(guiHelper));
    }

    @Override
    public void register(@Nonnull IModRegistry reg) {
        HELPER = reg.getJeiHelpers();

        reg.handleRecipes(CauldronRecipe.class, recipe -> new BulkRecipeWrapper(HELPER, recipe), CauldronRecipeCategory.UID);
        reg.handleRecipes(StokedCauldronRecipe.class, recipe -> new BulkRecipeWrapper(HELPER, recipe), StokedCauldronRecipeCategory.UID);
        reg.handleRecipes(CrucibleRecipe.class, recipe -> new BulkRecipeWrapper(HELPER, recipe), CrucibleRecipeCategory.UID);
        reg.handleRecipes(StokedCrucibleRecipe.class, recipe -> new BulkRecipeWrapper(HELPER, recipe), StokedCrucibleRecipeCategory.UID);
        reg.handleRecipes(MillRecipe.class, recipe -> new BulkRecipeWrapper(HELPER, recipe), MillRecipeCategory.UID);
        reg.handleRecipes(KilnRecipe.class, BlockMetaWrapper::new, KilnRecipeCategory.UID);
        reg.handleRecipes(SawRecipe.class, BlockMetaWrapper::new, SawRecipeCategory.UID);
        reg.handleRecipes(SawRecipe.class, BlockMetaWrapper::new, SteelSawRecipeCategory.UID);
        reg.handleRecipes(TurntableRecipe.class, TurntableRecipeWrapper::new, TurntableRecipeCategory.UID);
        reg.handleRecipes(HopperInteractions.HopperRecipe.class, recipe -> recipe instanceof HopperInteractions.SoulUrnRecipe ? new HopperRecipeWrapper.SoulUrn((HopperInteractions.SoulUrnRecipe) recipe) : new HopperRecipeWrapper(recipe), HopperRecipeCategory.UID);
        reg.handleRecipes(ShapedAnvilRecipe.class, recipe -> new ShapedAnvilRecipeWrapper(HELPER, recipe), SteelCraftingCategory.UID);
        reg.handleRecipes(ShapelessAnvilRecipe.class, recipe -> new ShapelessRecipeWrapper<>(HELPER, recipe), SteelCraftingCategory.UID);
        reg.handleRecipes(ShapelessOreRecipe.class, recipe -> new ShapelessRecipeWrapper<>(HELPER, recipe), SteelCraftingCategory.UID);
        reg.handleRecipes(ShapelessRecipes.class, recipe -> new ShapelessRecipeWrapper<>(HELPER, recipe), SteelCraftingCategory.UID);
        reg.handleRecipes(ToolBaseRecipe.class, recipe -> new ShapelessRecipeWrapper<>(HELPER, recipe), SteelCraftingCategory.UID);
        reg.handleRecipes(ToolBaseRecipe.class, recipe -> new ShapelessRecipeWrapper<>(HELPER, recipe), VanillaRecipeCategoryUid.CRAFTING);
        reg.addRecipes(CauldronManager.getInstance().getRecipes(), CauldronRecipeCategory.UID);
        reg.addRecipes(StokedCauldronManager.getInstance().getRecipes(), StokedCauldronRecipeCategory.UID);
        reg.addRecipes(CrucibleManager.getInstance().getRecipes(), CrucibleRecipeCategory.UID);
        reg.addRecipes(StokedCrucibleManager.getInstance().getRecipes(), StokedCrucibleRecipeCategory.UID);
        reg.addRecipes(MillManager.getInstance().getRecipes(), MillRecipeCategory.UID);
        reg.addRecipes(SawManager.WOOD_SAW.getRecipes(), SawRecipeCategory.UID);
        reg.addRecipes(Sets.union(Sets.newHashSet(SawManager.WOOD_SAW.getRecipes()), Sets.newHashSet(SawManager.STEEL_SAW.getRecipes())), SteelSawRecipeCategory.UID);
        reg.addRecipes(KilnManager.INSTANCE.getRecipes(), KilnRecipeCategory.UID);
        reg.addRecipes(TurntableManager.INSTANCE.getRecipes(), TurntableRecipeCategory.UID);
        reg.addRecipes(HopperInteractions.RECIPES, HopperRecipeCategory.UID);
        reg.addRecipes(AnvilCraftingManager.ANVIL_CRAFTING, SteelCraftingCategory.UID);

        reg.addRecipeCatalyst(BlockMechMachines.getStack(MILL), MillRecipeCategory.UID);
        reg.addRecipeCatalyst(BlockMechMachines.getStack(HOPPER), HopperRecipeCategory.UID);
        reg.addRecipeCatalyst(BlockMechMachines.getStack(TURNTABLE), TurntableRecipeCategory.UID);
        reg.addRecipeCatalyst(BlockCookingPot.getStack(CAULDRON), CauldronRecipeCategory.UID, StokedCauldronRecipeCategory.UID);
        reg.addRecipeCatalyst(BlockCookingPot.getStack(CRUCIBLE), CrucibleRecipeCategory.UID, StokedCrucibleRecipeCategory.UID);
        reg.addRecipeCatalyst(new ItemStack(BWMBlocks.SAW), SawRecipeCategory.UID);
        reg.addRecipeCatalyst(new ItemStack(BWMBlocks.STEEL_SAW), SteelSawRecipeCategory.UID);
        reg.addRecipeCatalyst(new ItemStack(Blocks.BRICK_BLOCK), KilnRecipeCategory.UID);
        reg.addRecipeCatalyst(new ItemStack(BWMBlocks.STEEL_ANVIL), SteelCraftingCategory.UID);

        reg.addRecipeClickArea(GuiCauldron.class, 81, 19, 14, 14, CauldronRecipeCategory.UID, StokedCauldronRecipeCategory.UID);
        reg.addRecipeClickArea(GuiCrucible.class, 81, 19, 14, 14, CrucibleRecipeCategory.UID, StokedCrucibleRecipeCategory.UID);
        reg.addRecipeClickArea(GuiMill.class, 81, 19, 14, 14, MillRecipeCategory.UID);
        reg.addRecipeClickArea(GuiSteelAnvil.class, 88, 41, 28, 23, SteelCraftingCategory.UID);
        registerAnvil(reg);

        IRecipeTransferRegistry recipeTransferRegistry = reg.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(ContainerSteelAnvil.class, SteelCraftingCategory.UID, 1, 16, 17, 36);

    }

    private void registerAnvil(IModRegistry reg) {
        List<ItemStack> tools = Lists.newArrayList(new ItemStack(BWMItems.STEEL_AXE), new ItemStack(BWMItems.STEEL_BATTLEAXE), new ItemStack(BWMItems.STEEL_BOOTS), new ItemStack(BWMItems.STEEL_CHEST), new ItemStack(BWMItems.STEEL_HELMET), new ItemStack(BWMItems.STEEL_HOE), new ItemStack(BWMItems.STEEL_MATTOCK),
                new ItemStack(BWMItems.STEEL_PANTS), new ItemStack(BWMItems.STEEL_PICKAXE), new ItemStack(BWMItems.STEEL_SHOVEL), new ItemStack(BWMItems.STEEL_SWORD));
        for (ItemStack stack : tools) {
            ItemStack dam1 = stack.copy();
            dam1.setItemDamage(dam1.getMaxDamage());
            ItemStack dam2 = stack.copy();
            dam2.setItemDamage(dam2.getMaxDamage() * 3 / 4);
            ItemStack dam3 = stack.copy();
            dam3.setItemDamage(dam3.getMaxDamage() * 2 / 4);
            reg.addAnvilRecipe(dam1, Collections.singletonList(ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.INGOT_STEEL)), Collections.singletonList(dam2));
            reg.addAnvilRecipe(dam2, Collections.singletonList(ItemMaterial.getMaterial(ItemMaterial.EnumMaterial.INGOT_STEEL)), Collections.singletonList(dam3));
        }
    }

    public static void showRecipe(Ingredient ingredient) {
        ItemStack stack = Lists.newArrayList(ingredient.getMatchingStacks()).stream().findFirst().orElse(ItemStack.EMPTY);
        IFocus<?> focus = new Focus<Object>(IFocus.Mode.OUTPUT, stack);
        JEI.JEI_RUNTIME.getRecipesGui().show(focus);
    }

}
