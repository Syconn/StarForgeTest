package mod.stf.syconn.api.util.data;

import mod.stf.syconn.init.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorData { // TODO MAKE VECTOR DATA SAVABLE TO NBT

    private final Map<Direction, Boolean> fluidValues = new HashMap<>();
    private final List<Direction> blockFaces = new ArrayList<>();
    private BlockPos pos;
    private Level level;
    private final float r, g, b;
    private final float blockHeight;
    public VectorData(Level level, BlockPos pos, boolean posx, boolean negx, boolean posz, boolean negz, int lowestY) { // TODO CREATE BLOCK MODELS HERE
        this.level = level;
        this.pos = pos;
        for (Direction d : Direction.values()) {
            // FLUIDS
            if (!level.getBlockState(pos).getFluidState().isEmpty()) fluidValues.put(d, shouldRenderWithinChunk(level.getChunkAt(pos).getPos(), pos, d, posx, negx, posz, negz)
                        || (!Block.isShapeFullBlock(level.getBlockState(pos.relative(d)).getShape(level, pos.relative(d))) && level.getBlockState(pos.relative(d)).getFluidState().isEmpty()) ||
                        (!level.getBlockState(pos.relative(d)).canOcclude() && level.getBlockState(pos.relative(d)).getFluidState().isEmpty()));
            if (pos.getY() == lowestY && d != Direction.UP) fluidValues.put(d, false);
            // BLOCKS
            else if (shouldRenderWithinChunk(level.getChunkAt(pos).getPos(), pos, d, posx, negx, posz, negz) || handleNonFullBlocks(d) || shouldRenderGlassFace(d)) {
                if (pos.getY() == lowestY && d == Direction.UP) blockFaces.add(d);
                else if (pos.getY() != lowestY) blockFaces.add(d);
            }
        }
        blockHeight = 1.0F;
        fluidValues.put(Direction.DOWN, level.getBlockState(pos.below()).getFluidState().isEmpty());
        FluidState fluid = level.getBlockState(pos).getFluidState();
        int color;
        if (!level.getBlockState(pos).getFluidState().isEmpty()) color = IClientFluidTypeExtensions.of(fluid).getTintColor(fluid, level, pos);
        else color = Minecraft.getInstance().getBlockColors().getColor(level.getBlockState(pos), level, pos, 0);
        this.r = (color >> 16 & 0xFF) / 255.0F;
        this.g = (color >> 8 & 0xFF) / 255.0F;
        this.b = (color & 0xFF) / 255.0F;
    }

    public VectorData(CompoundTag tag) { // ALWAYS CLIENT SIDE
        this.r = tag.getFloat("r");
        this.g = tag.getFloat("g");
        this.b = tag.getFloat("b");
        this.blockHeight = tag.getFloat("height");
        if (tag.contains("faces", Tag.TAG_LIST)){
            ListTag map = tag.getList("faces", Tag.TAG_COMPOUND);
            map.forEach(nbt -> {
                CompoundTag data = (CompoundTag) nbt;
                blockFaces.add(Direction.from3DDataValue(data.getInt("dir")));
            });
        }
        if (tag.contains("fluids", Tag.TAG_LIST)){
            ListTag map = tag.getList("fluids", Tag.TAG_COMPOUND);
            map.forEach(nbt -> {
                CompoundTag data = (CompoundTag) nbt;
                fluidValues.put(Direction.from3DDataValue(data.getInt("dir")), data.getBoolean("bool"));
            });
        }
    }

    public static boolean shouldRenderWithinChunk(ChunkPos block, BlockPos pos, Direction d, boolean posx, boolean negx, boolean posz, boolean negz) {
        if (negz && d == Direction.NORTH && block.getBlockZ(0) == pos.getZ()) return true;
        if (posz && d == Direction.SOUTH && block.getBlockZ(15) == pos.getZ()) return true;
        if (negx && d == Direction.WEST && block.getBlockX(0) == pos.getX()) return true;
        return posx && d == Direction.EAST && block.getBlockX(15) == pos.getX();
    }

    public boolean handleNonFullBlocks(Direction d) {
        if (level.getBlockState(pos.relative(d)).is(ModTags.Blocks.HIDE_FACE_BLOCK) && level.getBlockState(pos).is(ModTags.Blocks.HIDE_FACE_BLOCK)) return false;
        return (!Block.isShapeFullBlock(level.getBlockState(pos.relative(d)).getShape(level, pos.relative(d))) ||
                (level.getBlockState(pos.relative(d)).is(ModTags.Blocks.HIDE_FACE_BLOCK) && level.getBlockState(pos).is(ModTags.Blocks.HIDE_FACE_BLOCK)));
    }

    public boolean shouldRenderGlassFace(Direction d) {
        if (level.getBlockState(pos).getBlock() instanceof HalfTransparentBlock) if (level.getBlockState(pos.relative(d)).getBlock() instanceof HalfTransparentBlock) return false;
        return !level.getBlockState(pos.relative(d)).canOcclude() && (!(level.getBlockState(pos.relative(d)).getBlock() instanceof LeavesBlock) || !(level.getBlockState(pos).getBlock() instanceof LeavesBlock));
    }

    public float getBlockHeight() {
        return blockHeight;
    }

    public boolean getVectorData(Direction d) {
        if (fluidValues.get(d) == null) return false;
        return fluidValues.get(d);
    }

    public List<Direction> getBlockFaces() {
        return blockFaces;
    }

    public float[] getRGB() {
        return new float[] {r, g, b};
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("r", r);
        tag.putFloat("g", g);
        tag.putFloat("b", b);
        tag.putFloat("height", blockHeight);
        ListTag listTag = new ListTag();
        blockFaces.forEach(face -> {
            CompoundTag d = new CompoundTag();
            d.putInt("dir", face.get3DDataValue());
            listTag.add(d);
        });
        tag.put("faces", listTag);
        ListTag listTag2 = new ListTag();
        System.out.println(fluidValues.size());
        fluidValues.forEach((dir, b) -> {
            CompoundTag d = new CompoundTag();
            d.putInt("dir", dir.get3DDataValue());
            d.putBoolean("bool", b);
            listTag2.add(d);
        });
        tag.put("fluids", listTag2);
        return tag;
    }
}
