package betterwithmods.module.gameplay.miniblocks.blocks;

import betterwithmods.module.gameplay.miniblocks.orientations.BaseOrientation;
import betterwithmods.module.gameplay.miniblocks.orientations.MouldingOrientation;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

public class BlockMoulding extends BlockMini {


    public BlockMoulding(Material material, Function<Material, Collection<IBlockState>> subtypes) {
        super(material, subtypes);
    }

    @Override
    public BaseOrientation getOrientationFromPlacement(EntityLivingBase placer, @Nullable EnumFacing facing, ItemStack stack, float hitX, float hitY, float hitZ) {
        if (facing != null)
            return MouldingOrientation.getFromVec(new Vec3d(hitX, hitY, hitZ), facing);
        return BaseOrientation.DEFAULT;
    }

    @Override
    public BaseOrientation deserializeOrientation(int ordinal) {
        return MouldingOrientation.VALUES[ordinal];
    }

}
