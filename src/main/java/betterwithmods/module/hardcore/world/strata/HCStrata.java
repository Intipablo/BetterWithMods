package betterwithmods.module.hardcore.world.strata;

import betterwithmods.common.BWMRecipes;
import betterwithmods.common.BWOreDictionary;
import betterwithmods.common.registry.BrokenToolRegistry;
import betterwithmods.module.Feature;
import betterwithmods.module.ModuleLoader;
import betterwithmods.util.item.ToolsManager;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HCStrata extends Feature {
    private static final Pattern PARSE = Pattern.compile("(^\\d{1,255})=(\\d{1,255}),(\\d{1,255}).*");
    public static boolean ENABLED;
    public static float[] STRATA_SPEEDS;
    public static float INCORRECT_STRATA_SCALE;
    public static HashMap<IBlockState, BlockType> STATES = Maps.newHashMap();
    public static HashMap<Integer, StrataConfig> STRATA_CONFIGS = Maps.newHashMap();
    public static StrataConfig DEFAULT = new StrataConfig(-1, -1);

    public HCStrata() {
        enabledByDefault = false;
    }

    public static void addStone(IBlockState state) {
        STATES.put(state, BlockType.STONE);
    }

    public static void addStone(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates())
            addStone(state);
    }

    public static void addOre(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates())
            STATES.put(state, BlockType.ORE);
    }

    public static boolean shouldStratify(World world, BlockPos pos) {
        return shouldStratify(world, world.getBlockState(pos));
    }

    public static boolean shouldStratify(World world, IBlockState state) {
        return world.provider.getDimensionType() == DimensionType.OVERWORLD && STATES.containsKey(state);
    }

    public static Stratification getStratification(int y, int dimension) {
        return STRATA_CONFIGS.getOrDefault(dimension, DEFAULT).getStrata(y);
    }
    private static final Pattern PATTERN = Pattern.compile("^([\\-]?\\d+)=(\\d{1,255}),(\\d{1,255}).*");
    private static void loadStrataConfig(String entry) {

        Matcher matcher = PATTERN.matcher(entry);
        if(matcher.matches()) {
            int dim = Integer.parseInt(matcher.group(1));
            int medium = Integer.parseInt(matcher.group(2));
            int hard = Integer.parseInt(matcher.group(3));
            STRATA_CONFIGS.put(dim, new StrataConfig(medium, hard));
        }
    }

    @Override
    public void setupConfig() {
        STRATA_SPEEDS = new float[]{(float) loadPropDouble("Light Strata", "Speed for Light Strata", 1.0),
                (float) loadPropDouble("Medium Strata", "Speed for Medium Strata", 1.0),
                (float) loadPropDouble("Dark Strata", "Speed for Dark Strata", 1.0)
        };
        INCORRECT_STRATA_SCALE = (float) loadPropDouble("Incorrect Strata", "Speed scale for when the Strata is higher than the tool", 0.35);

        Arrays.stream(loadPropStringList("Strata Configs", "Set the strata levels for a given dimension, <dim>=< medium start y>,<hard start y>", new String[]{
                "0=42,21"
        })).map( s -> s.replaceAll(" ", "")).forEach(HCStrata::loadStrataConfig);
    }

    @Override
    public String getFeatureDescription() {
        return "Divides the underground into three strata. Each strata requires the next tool tier to properly mine";
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {

        if (Loader.isModLoaded("ctm")) {
            try {
                Class clazz = Class.forName("team.chisel.ctm.client.texture.type.TextureTypeRegistry");
                Class clazz1 = Class.forName("team.chisel.ctm.api.texture.ITextureType");
                Class clazz2 = Class.forName("betterwithmods.module.hardcore.world.strata.TextureTypeStrata");
                Method register = ReflectionHelper.findMethod(clazz, "register", "register", String.class, clazz1);
                register.invoke(null, "bwm_strata", clazz2.newInstance());
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ENABLED = ModuleLoader.isFeatureEnabled(HCStrata.class);
        for (BWOreDictionary.Ore ore : BWOreDictionary.oreNames) {
            for (ItemStack stack : ore.getOres()) {
                if (stack.getItem() instanceof ItemBlock) {
                    addOre(((ItemBlock) stack.getItem()).getBlock());
                }
            }
        }
        List<ItemStack> stones = loadItemStackList("Strata Stones", "Blocks that are considered as stone to HCStrata", new ItemStack[]{new ItemStack(Blocks.STONE, 1, OreDictionary.WILDCARD_VALUE)});
        stones.stream().map(BWMRecipes::getStatesFromStack).flatMap(Set::stream).forEach(HCStrata::addStone);
    }

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (shouldStratify(world, event.getState()) && event.getHarvester() != null) {
            ItemStack stack = BrokenToolRegistry.findItem(event.getHarvester(), event.getState());
            int strata = getStratification(pos.getY(), world.provider.getDimension()).ordinal();
            if (STATES.getOrDefault(event.getState(), BlockType.STONE) == BlockType.STONE) {
                int level = Math.max(1, stack.getItem().getHarvestLevel(stack, "pickaxe", event.getHarvester(), event.getState()));
                if (level <= strata) {
                    event.getDrops().clear();
                }
            }
        }
    }

    @SubscribeEvent
    public void getBreakSpeed(PlayerEvent.BreakSpeed event) {
        World world = event.getEntityPlayer().getEntityWorld();
        BlockPos pos = event.getPos();
        if (shouldStratify(world, pos)) {
            ItemStack stack = BrokenToolRegistry.findItem(event.getEntityPlayer(), event.getState());
            float scale = ToolsManager.getSpeed(stack, event.getState());
            int strata = getStratification(pos.getY(), world.provider.getDimension()).ordinal();
            if (STATES.getOrDefault(event.getState(), BlockType.STONE) == BlockType.STONE) {
                int level = Math.max(1, stack.getItem().getHarvestLevel(stack, "pickaxe", event.getEntityPlayer(), event.getState()));
                if (level <= strata) {
                    scale = INCORRECT_STRATA_SCALE;
                }
            }
            event.setNewSpeed(scale * STRATA_SPEEDS[strata] * event.getOriginalSpeed());
        }

    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }


    private enum BlockType {
        STONE(0),
        ORE(1);
        private int level;

        BlockType(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public enum Stratification {
        NORMAL,
        MEDIUM,
        HARD
    }

    private static class StrataConfig {


        private int medium;
        private int hard;


        public StrataConfig(int medium, int hard) {
            this.medium = medium;
            this.hard = hard;
        }

        public Stratification getStrata(int y) {
            return y <= hard ? Stratification.HARD : y <= medium ? Stratification.MEDIUM : Stratification.NORMAL;
        }
    }


}
