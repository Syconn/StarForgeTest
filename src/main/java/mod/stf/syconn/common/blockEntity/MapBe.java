package mod.stf.syconn.common.blockEntity;

import mod.stf.syconn.api.blockEntity.ClientBlockEntity;
import mod.stf.syconn.api.util.NbtUtil;
import mod.stf.syconn.block.MapProjector;
import mod.stf.syconn.init.ModBlockEntities;
import mod.stf.syconn.util.data.ChunkHandler;
import mod.stf.syconn.world.data.ChunkData;
import mod.stf.syconn.world.data.ChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class MapBe extends ClientBlockEntity {

    private ChunkHandler chunkHandler = new ChunkHandler();
    private BlockPos selectedPos = BlockPos.ZERO;
    private List<BlockPos> selections = new ArrayList<>();
    private Boolean fastRender = false;
    private static int map_update = 0;
    private static int receiving_signal = 0;

    public MapBe(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.MAP_BE.get(), pWorldPosition, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MapBe be) {
        boolean update = false;
        if (level.getCapability(ChunkManager.CHUNKS).isPresent()) {
            ChunkData cap = level.getCapability(ChunkManager.CHUNKS).resolve().get();
            if (map_update < 1000) { map_update++; }
            else if (!be.selectedPos.equals(BlockPos.ZERO)) { map_update = 0; be.chunkHandler = cap.getChunkHandler(be.selectedPos, level); update = true; }
            if (receiving_signal < 350) receiving_signal++;
            else {
                receiving_signal = 0;
                if (!be.selectedPos.equals(BlockPos.ZERO) && !cap.getChunkOptions(pos).contains(be.selectedPos)) {
                    be.selectedPos = BlockPos.ZERO;
                    be.chunkHandler = new ChunkHandler();
                    update = true;
                }
            }
        }
        if (update) {
//            update(level, pos, state);
        }
        if (!be.chunkHandler.getChunks(false).isEmpty() && state.getValue(MapProjector.TOP)) level.setBlock(pos, state.setValue(MapProjector.TOP, false), 2);
        else if (be.chunkHandler.getChunks(false).isEmpty() && !state.getValue(MapProjector.TOP)) level.setBlock(pos, state.setValue(MapProjector.TOP, true), 2);
    }

    public List<BlockPos> setSelections() {
        selections = level.getCapability(ChunkManager.CHUNKS).map(data -> data.getChunkOptions(worldPosition)).orElse(List.of());
        update();
        return selections;
    }

    public void setSelectedPos(BlockPos selectedPos, Level level) {
        this.selectedPos = selectedPos;
        ChunkData cap = level.getCapability(ChunkManager.CHUNKS).resolve().get();
        chunkHandler = cap.getChunkHandler(selectedPos, level);
        update();
    }

    public void setFastRender(Boolean fastRender) {
        this.fastRender = fastRender;
        update();
    }

    public ChunkHandler getChunkHandler() {
        return chunkHandler;
    }

    public Boolean isFastRender() {
        return fastRender;
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("fastrender", fastRender);
        tag.put("selectedpos", NbtUtils.writeBlockPos(selectedPos));
        tag.put("handler", chunkHandler.save());
        tag.put("selections", NbtUtil.writeBlockPosses(selections));
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        fastRender = tag.getBoolean("fastrender");
        selectedPos = NbtUtils.readBlockPos(tag.getCompound("selectedpos"));
        if (tag.contains("selections")) selections = NbtUtil.readBlockPosses(tag.getCompound("selections"));
        if (tag.contains("handler")) chunkHandler = new ChunkHandler(tag.getCompound("handler"));
    }
}