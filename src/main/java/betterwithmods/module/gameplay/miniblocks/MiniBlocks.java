package betterwithmods.module.gameplay.miniblocks;

import betterwithmods.BWMod;
import betterwithmods.client.model.render.RenderUtils;
import betterwithmods.common.BWMBlocks;
import betterwithmods.common.BWMRecipes;
import betterwithmods.common.BWOreDictionary;
import betterwithmods.common.BWRegistry;
import betterwithmods.common.blocks.camo.BlockCamo;
import betterwithmods.common.blocks.camo.TileCamo;
import betterwithmods.common.items.ItemMaterial;
import betterwithmods.module.Feature;
import betterwithmods.module.gameplay.AnvilRecipes;
import betterwithmods.module.gameplay.miniblocks.blocks.*;
import betterwithmods.module.gameplay.miniblocks.client.CamoModel;
import betterwithmods.module.gameplay.miniblocks.client.MiniModel;
import betterwithmods.module.gameplay.miniblocks.tiles.TileMini;
import betterwithmods.util.ReflectionHelperBlock;
import com.google.common.collect.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.*;

public class MiniBlocks extends Feature {
    public static HashMap<MiniType, HashMap<Material, BlockCamo>> MINI_MATERIAL_BLOCKS = Maps.newHashMap();
    public static Multimap<Material, IBlockState> MATERIALS = HashMultimap.create();
    private static Map<Material, String> names = Maps.newHashMap();
    private static boolean autoGeneration;
    private static HashSet<String> whitelist = new HashSet<>();

    static {
        names.put(Material.WOOD, "wood");
        names.put(Material.ROCK, "rock");
        names.put(Material.IRON, "iron");
        for (MiniType type : MiniType.VALUES) {
            MINI_MATERIAL_BLOCKS.put(type, Maps.newHashMap());
        }
    }

    static {
        for (Material material : names.keySet()) {
            String name = names.get(material);
            MINI_MATERIAL_BLOCKS.get(MiniType.SIDING).put(material, (BlockMini) new BlockSiding(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "siding", name)));
            MINI_MATERIAL_BLOCKS.get(MiniType.MOULDING).put(material, (BlockMini) new BlockMoulding(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "moulding", name)));
            MINI_MATERIAL_BLOCKS.get(MiniType.CORNER).put(material, (BlockMini) new BlockCorner(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "corner", name)));
            MINI_MATERIAL_BLOCKS.get(MiniType.COLUMN).put(material, (BlockMini) new BlockColumn(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "column", name)));
            MINI_MATERIAL_BLOCKS.get(MiniType.PEDESTAL).put(material, (BlockMini) new BlockPedestals(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "pedestal", name)));
            MINI_MATERIAL_BLOCKS.get(MiniType.TABLE).put(material, (BlockCamo) new BlockTable(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "table", name)));
            MINI_MATERIAL_BLOCKS.get(MiniType.BENCH).put(material, (BlockCamo) new BlockBench(material, m -> MATERIALS.get(m)).setRegistryName(String.format("%s_%s", "bench", name)));
        }
    }


    public MiniBlocks() {
        enabledByDefault = false;
    }

    public static boolean isValidMini(IBlockState state) {
        Material material = state.getMaterial();
        return names.containsKey(material) && MATERIALS.get(material).contains(state);
    }

    public static boolean isValidMini(IBlockState state, ItemStack stack) {
        ResourceLocation resloc = stack.getItem().getRegistryName();
        if (!autoGeneration && resloc != null && !whitelist.contains(resloc.toString()) && !whitelist.contains(resloc.toString() + ":" + stack.getMetadata()))
            return BWOreDictionary.hasPrefix(stack, "plankWood"); //Specifically planks are a-okay

        Block blk = state.getBlock();
        final ReflectionHelperBlock pb = new ReflectionHelperBlock();
        final Class<? extends Block> blkClass = blk.getClass();

        pb.onBlockActivated(null, null, null, null, null, null, 0, 0, 0);
        boolean noActivation = (getDeclaringClass(blkClass, pb.MethodName, World.class, BlockPos.class, IBlockState.class, EntityPlayer.class, EnumHand.class, EnumFacing.class, float.class, float.class, float.class) == Block.class);

        pb.updateTick(null, null, null, null);
        boolean noUpdate = getDeclaringClass(blkClass, pb.MethodName, World.class, BlockPos.class, IBlockState.class, Random.class) == Block.class;

        // ignore blocks with custom collision.
        pb.onEntityCollidedWithBlock(null, null, null, null);
        boolean noCustomCollision = getDeclaringClass(blkClass, pb.MethodName, World.class, BlockPos.class, IBlockState.class, Entity.class) == Block.class;
        final boolean isFullBlock = state.isFullBlock() || blkClass == BlockStainedGlass.class || blkClass == BlockGlass.class || blk == Blocks.SLIME_BLOCK || blk == Blocks.ICE;
        final boolean hasItem = Item.getItemFromBlock(blk) != Items.AIR;
        final boolean tickingBehavior = blk.getTickRandomly();
        final boolean isOre = BWOreDictionary.hasPrefix(stack, "ore") || BWOreDictionary.isOre(stack, "logWood");

        boolean hasBehavior = (blk.hasTileEntity(state) || tickingBehavior) && blkClass != BlockGrass.class && blkClass != BlockIce.class;

        return noUpdate && noActivation && noCustomCollision && isFullBlock && !hasBehavior && hasItem && !isOre;
    }

    private static Class<?> getDeclaringClass(
            final Class<?> blkClass,
            final String methodName,
            final Class<?>... args) {
        try {
            blkClass.getDeclaredMethod(methodName, args);
            return blkClass;
        } catch (final NoSuchMethodException | SecurityException e) {
            // nothing here...
        } catch (final NoClassDefFoundError e) {
            BWMod.logger.info("Unable to determine blocks eligibility for making a miniblock, " + blkClass.getName() + " attempted to load " + e.getMessage());
            return blkClass;
        } catch (final Throwable t) {
            return blkClass;
        }

        return getDeclaringClass(
                blkClass.getSuperclass(),
                methodName,
                args);
    }

    public static ItemStack fromParent(Block mini, IBlockState state) {
        return fromParent(mini, state, 1);
    }

    public static ItemStack fromParent(Block mini, IBlockState state, int count) {
        ItemStack stack = new ItemStack(mini, count);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound texture = new NBTTagCompound();
        NBTUtil.writeBlockState(texture, state);
        tag.setTag("texture", texture);
        stack.setTagCompound(tag);
        return stack;
    }

    public static void addWhitelistedBlock(ResourceLocation resloc) {
        whitelist.add(resloc.toString());
    }

    public static void addWhitelistedBlock(ResourceLocation resloc, int meta) { //Delete this in 1.13
        whitelist.add(resloc.toString() + ":" + meta);
    }

    @Override
    public void setupConfig() {
        autoGeneration = loadPropBool("Auto Generate Miniblocks", "Automatically add miniblocks for many blocks, based on heuristics and probably planetary alignments. WARNING: Exposure to this config option can kill pack developers.", false);
        whitelist = loadPropStringHashSet("Whitelist", "Whitelist for blocks to generate miniblocks for (aside from the ones required by BWM)", new String[]{});
        whitelist.add("minecraft:stone:0");
        whitelist.add("minecraft:stonebrick");
        whitelist.add("minecraft:sandstone");
        whitelist.add("minecraft:brick_block");
        whitelist.add("minecraft:nether_brick");
        whitelist.add("minecraft:quartz_block");
        whitelist.add("betterwithmods:aesthetic:6");
    }


    @Override
    public String getFeatureDescription() {
        return "Dynamically generate Siding, Mouldings and Corners for many of the blocks in the game.";
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        for (MiniType type : MiniType.VALUES) {
            for (BlockCamo mini : MINI_MATERIAL_BLOCKS.get(type).values()) {
                BWMBlocks.registerBlock(mini, new ItemCamo(mini));
            }
        }
        GameRegistry.registerTileEntity(TileMini.class, "bwm.mini");
        GameRegistry.registerTileEntity(TileCamo.class, "bwm.camo");
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemRegister(RegistryEvent.Register<Item> event) {
        final NonNullList<ItemStack> list = NonNullList.create();
        for (Item item : ForgeRegistries.ITEMS) {
            if (!(item instanceof ItemBlock))
                continue;
            try {

                final CreativeTabs ctab = item.getCreativeTab();
                if (ctab != null) {
                    item.getSubItems(ctab, list);
                }
                for (final ItemStack stack : list) {
                    if (!(stack.getItem() instanceof ItemBlock))
                        continue;
                    final IBlockState state = BWMRecipes.getStateFromStack(stack);
                    if (state != null && isValidMini(state, stack)) {
                        Material material = state.getMaterial();
                        if (names.containsKey(material)) {
                            MATERIALS.put(material, state);
                        }
                    }
                }
                list.clear();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

        for (Material material : names.keySet()) {
            BlockCamo siding = MINI_MATERIAL_BLOCKS.get(MiniType.SIDING).get(material);
            BlockCamo moulding = MINI_MATERIAL_BLOCKS.get(MiniType.MOULDING).get(material);
            BlockCamo corner = MINI_MATERIAL_BLOCKS.get(MiniType.COLUMN).get(material);

            addHardcoreRecipe(new MiniRecipe(siding, null));
            addHardcoreRecipe(new MiniRecipe(moulding, siding));
            addHardcoreRecipe(new MiniRecipe(corner, moulding));
        }

        for (IBlockState wood : MATERIALS.get(Material.WOOD)) {
            ItemStack mini = BWMRecipes.getStackFromState(wood);
            MiniBlockIngredient siding = new MiniBlockIngredient("siding", mini);
            MiniBlockIngredient moulding = new MiniBlockIngredient("moulding", mini);
            MiniBlockIngredient corner = new MiniBlockIngredient("corner", mini);
            ItemStack sidingStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.SIDING).get(Material.WOOD), wood, 2);
            ItemStack mouldingStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.MOULDING).get(Material.WOOD), wood, 2);
            ItemStack cornerStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.CORNER).get(Material.WOOD), wood, 2);
            BWRegistry.WOOD_SAW.addRecipe(mini, sidingStack);
            BWRegistry.WOOD_SAW.addRecipe(siding, mouldingStack);
            BWRegistry.WOOD_SAW.addRecipe(moulding, cornerStack);
            if (BWOreDictionary.isOre(mini, "plankWood"))
                BWRegistry.WOOD_SAW.addRecipe(corner, ItemMaterial.getStack(ItemMaterial.EnumMaterial.WOOD_GEAR, 2));
        }


        for (IBlockState parent : Iterables.concat(MATERIALS.get(Material.ROCK), MATERIALS.get(Material.IRON), MATERIALS.get(Material.WOOD))) {
            ItemStack mini = BWMRecipes.getStackFromState(parent);
            Material material = parent.getMaterial();
            MiniBlockIngredient siding = new MiniBlockIngredient("siding", mini);
            MiniBlockIngredient moulding = new MiniBlockIngredient("moulding", mini);
            ItemStack columnStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.COLUMN).get(material), parent, 8);
            ItemStack pedestalStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.PEDESTAL).get(material), parent, 8);

            AnvilRecipes.addSteelShapedRecipe(columnStack.getItem().getRegistryName(), columnStack, "XX", "XX", "XX", "XX", 'X', moulding);
            AnvilRecipes.addSteelShapedRecipe(pedestalStack.getItem().getRegistryName(), pedestalStack, " XX ", "BBBB", "BBBB", "BBBB", 'X', siding, 'B', mini);
        }

        for (IBlockState parent : Iterables.concat(MATERIALS.get(Material.ROCK), MATERIALS.get(Material.IRON))) {
            ItemStack mini = BWMRecipes.getStackFromState(parent);
            Material material = parent.getMaterial();
            MiniBlockIngredient siding = new MiniBlockIngredient("siding", mini);
            MiniBlockIngredient moulding = new MiniBlockIngredient("moulding", mini);
            ItemStack sidingStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.SIDING).get(material), parent, 8);
            ItemStack mouldingStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.MOULDING).get(material), parent, 8);
            ItemStack cornerStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.CORNER).get(material), parent, 8);
            ItemStack tableStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.TABLE).get(material), parent, 1);
            ItemStack benchStack = MiniBlocks.fromParent(MINI_MATERIAL_BLOCKS.get(MiniType.BENCH).get(material), parent, 1);

            AnvilRecipes.addSteelShapedRecipe(sidingStack.getItem().getRegistryName(), sidingStack, "XXXX", 'X', mini);
            AnvilRecipes.addSteelShapedRecipe(mouldingStack.getItem().getRegistryName(), mouldingStack, "XXXX", 'X', siding);
            AnvilRecipes.addSteelShapedRecipe(cornerStack.getItem().getRegistryName(), cornerStack, "XXXX", 'X', moulding);

            addHardcoreRecipe(new ShapedOreRecipe(tableStack.getItem().getRegistryName(), tableStack, "SSS"," M ", " M ", 'S', siding, 'M', moulding));
            addHardcoreRecipe(new ShapedOreRecipe(benchStack.getItem().getRegistryName(), benchStack, "SSS"," M ", 'S', siding, 'M', moulding));
        }

    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }


    @SideOnly(Side.CLIENT)
    private void registerModel(IRegistry<ModelResourceLocation, IBakedModel> registry, String name, IBakedModel model) {
        registerModel(registry, name, model, Sets.newHashSet("normal", "inventory"));
    }

    @SideOnly(Side.CLIENT)
    private void registerModel(IRegistry<ModelResourceLocation, IBakedModel> registry, String name, IBakedModel model, Set<String> variants) {
        for (String variant : variants) {
            registry.putObject(new ModelResourceLocation(BWMod.MODID + ":" + name, variant), model);
        }
    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPostBake(ModelBakeEvent event) {
        MiniModel.SIDING = new MiniModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/mini/siding")));
        MiniModel.MOULDING = new MiniModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/mini/moulding")));
        MiniModel.CORNER = new MiniModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/mini/corner")));
        MiniModel.COLUMN = new MiniModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/mini/column")));
        MiniModel.PEDESTAL = new MiniModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/mini/pedestal")));
        CamoModel.TABLE_SUPPORTED = new CamoModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/table_supported")));
        CamoModel.TABLE_UNSUPPORTED = new CamoModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/table_unsupported")));

        CamoModel.BENCH_SUPPORTED = new CamoModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/bench_supported")));
        CamoModel.BENCH_UNSUPPORTED = new CamoModel(RenderUtils.getModel(new ResourceLocation(BWMod.MODID, "block/bench_unsupported")));

        for (Material material : names.keySet()) {
            String name = names.get(material);
            registerModel(event.getModelRegistry(), String.format("%s_%s", "siding", name), MiniModel.SIDING);
            registerModel(event.getModelRegistry(), String.format("%s_%s", "moulding", name), MiniModel.MOULDING);
            registerModel(event.getModelRegistry(), String.format("%s_%s", "corner", name), MiniModel.CORNER);
            registerModel(event.getModelRegistry(), String.format("%s_%s", "column", name), MiniModel.COLUMN);
            registerModel(event.getModelRegistry(), String.format("%s_%s", "pedestal", name), MiniModel.PEDESTAL);
            registerModel(event.getModelRegistry(), String.format("%s_%s", "table", name), CamoModel.TABLE_SUPPORTED, Sets.newHashSet("normal", "inventory", "supported=true"));
            registerModel(event.getModelRegistry(), String.format("%s_%s", "table", name), CamoModel.TABLE_UNSUPPORTED, Sets.newHashSet("supported=false"));
            registerModel(event.getModelRegistry(), String.format("%s_%s", "bench", name), CamoModel.BENCH_SUPPORTED, Sets.newHashSet("normal", "inventory", "supported=true"));
            registerModel(event.getModelRegistry(), String.format("%s_%s", "bench", name), CamoModel.BENCH_UNSUPPORTED, Sets.newHashSet("supported=false"));
        }
    }

}
