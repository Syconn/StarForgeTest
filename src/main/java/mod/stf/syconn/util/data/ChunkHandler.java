package mod.stf.syconn.util.data;

import com.google.common.primitives.Ints;
import mod.stf.syconn.Config;
import mod.stf.syconn.api.util.ChunkUtil;
import mod.stf.syconn.api.util.Mths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkHandler {

    private final List<ChunkInfo> chunks;
    private final List<ChunkInfo> processed_chunks;
    private final int renderY;

    public ChunkHandler() {
        chunks = new ArrayList<>();
        processed_chunks = new ArrayList<>();
        renderY = 0;
    }

    public ChunkHandler(Level level, ChunkPos center) {
        List<ChunkInfo> chunks = new ArrayList<>();
        List<ChunkInfo> processed_chunks = new ArrayList<>();
        List<Integer> sideYs = new ArrayList<>();
        int max = 2;
        for (int x = -max; x <= max; x++) {
            for (int z = -max; z <= max; z++) {
                LevelChunk chunk = level.getChunk(center.x + x, center.z + z);
//                level.getServer().getLevel(Level.OVERWORLD).setChunkForced(chunk.getPos().x, chunk.getPos().z, true);
                chunks.add(new ChunkInfo(chunk.getPos().x - center.x, chunk.getPos().z - center.z, chunk.getPos(), x == max, x == -max, z == max, z == -max, x == 1, x == -1, z == 1, z == -1));
                if (x <= 1 && x >= -1 && z <= 1 && z >= -1) processed_chunks.add(new ChunkInfo(chunk.getPos().x - center.x, chunk.getPos().z - center.z, chunk.getPos(), x == max, x == -max, z == max, z == -max, x == 1, x == -1, z == 1, z == -1));
                sideYs.addAll(ChunkUtil.getEdgeSurfaceBlock(chunk, x == max, x == -max, z == max, z == -max));
            }
        }
        int lowestY = ChunkUtil.getLowestSurfaceBlock(level, chunks).getY();
        this.renderY = Mths.mode(Ints.toArray(sideYs));
        if (lowestY < Config.minYRenderHeight.get()) lowestY = Config.minYRenderHeight.get();
        for (ChunkInfo chunk : chunks) chunk.createChunkRenderer(lowestY, this.renderY, level);
        this.chunks = chunks;
        this.processed_chunks = processed_chunks;
    }

    public ChunkHandler(CompoundTag tag) {
        List<ChunkInfo> chunks = new ArrayList<>();
        ListTag infoList = tag.getList("chunks", Tag.TAG_COMPOUND);
        infoList.forEach(tag1 -> chunks.add(new ChunkInfo(((CompoundTag) tag1).getCompound("data"))));
        this.chunks = chunks;
        List<ChunkInfo> processed_chunks = new ArrayList<>();
        infoList = tag.getList("processed_chunks", Tag.TAG_COMPOUND);
        infoList.forEach(tag1 -> processed_chunks.add(new ChunkInfo(((CompoundTag) tag1).getCompound("data"))));
        this.processed_chunks = processed_chunks;
        this.renderY = tag.getInt("renderY");
    }

    public List<ChunkInfo> getChunks(boolean fastRender) {
        if (fastRender) return processed_chunks;
        return chunks;
    }

    public int getRenderY() {
        return renderY;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag infoList = new ListTag();
        for (ChunkInfo chunk : chunks) {
            CompoundTag tag2 = new CompoundTag();
            tag2.put("data", chunk.save());
            infoList.add(tag2);
        }
        tag.put("chunks", infoList);
        infoList = new ListTag();
        for (ChunkInfo chunk : processed_chunks) {
            CompoundTag tag2 = new CompoundTag();
            tag2.put("data", chunk.save());
            infoList.add(tag2);
        }
        tag.put("processed_chunks", infoList);
        tag.putInt("renderY", renderY);
        return tag;
    }
}
