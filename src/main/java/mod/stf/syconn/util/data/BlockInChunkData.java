package mod.stf.syconn.util.data;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.stf.syconn.api.util.NbtUtil;
import mod.stf.syconn.api.util.RenderUtil;
import mod.stf.syconn.api.util.data.VectorData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public class BlockInChunkData {

    private final BlockPos pos;
    private final BlockState state;
    private final VectorData _3x3;
    private final VectorData _5x5;
    private final boolean isBlock;
    private final int x;
    private final int z;

    public BlockInChunkData(BlockState state, BlockPos pos, VectorData _3x3, VectorData _5x5, int x, int z) {
        this.pos = pos;
        this.state = state;
        this._3x3 = _3x3;
        this._5x5 = _5x5;
        this.x = x;
        this.z = z;
        this.isBlock = state.getFluidState().isEmpty();
    }

    public BlockInChunkData(CompoundTag tag) {
        HolderGetter<Block> holdergetter = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
        this.pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
        this.state = NbtUtils.readBlockState(holdergetter, tag.getCompound("state"));
//        this.state = BlockState.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("state")).result().get();
        this._5x5 = new VectorData(tag.getCompound("5x5"));
        this._3x3 = new VectorData(tag.getCompound("3x3"));
        this.isBlock = tag.getBoolean("is_block");
        this.x = tag.getInt("x");
        this.z = tag.getInt("z");
    }

    public void render(PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int lowestY, boolean fastRender) {
        pPoseStack.pushPose();
        pPoseStack.translate(x, pos.getY() - lowestY, z);
        if (isBlock) RenderUtil.renderSingleBlock(state, pPoseStack, pBufferSource, pPackedLight, fastRender ? _3x3 : _5x5);
        else RenderUtil.renderLiquid(pPoseStack, pBufferSource, state, fastRender ? _3x3 : _5x5);
        pPoseStack.popPose();
    }

    public CompoundTag save() { // TODO MAY BE SAVING TO MUCH
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(pos));
        tag.put("state", NbtUtils.writeBlockState(state));
        tag.put("5x5", _5x5.save());
        tag.put("3x3", _3x3.save());
        tag.putBoolean("is_block", isBlock);
        tag.putInt("x", x);
        tag.putInt("z", z);
        return tag;
    }

    public static CompoundTag saveAll(List<BlockInChunkData> data) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        data.forEach(blockInChunkData -> listTag.add(blockInChunkData.save()));
        tag.put("blocks", listTag);
        return tag;
    }

    public static List<BlockInChunkData> readAll(CompoundTag tag) {
        List<BlockInChunkData> blocks = new ArrayList<>();
        if(tag.contains("blocks", Tag.TAG_LIST)) tag.getList("blocks", Tag.TAG_COMPOUND).forEach(data -> blocks.add(new BlockInChunkData((CompoundTag) data)));
        return blocks;
    }
}
