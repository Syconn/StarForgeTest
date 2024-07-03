package mod.stf.syconn.util.data;

import mod.stf.syconn.api.util.ChunkUtil;
import mod.stf.syconn.api.util.data.VectorData;
import mod.stf.syconn.init.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkInfo {

    private List<BlockInChunkData> blocks = new ArrayList<>();
    private List<BlockInChunkData> fast_blocks = new ArrayList<>();
    private final ChunkPos chunkPos;
    private final int x;
    private final int z;
    private final boolean posx, negx, posz, negz;
    private final boolean fastPosx, fastNegx, fastPosz, fastNegz;

    public ChunkInfo(int x, int z, ChunkPos chunkPos, boolean posx, boolean negx, boolean posz, boolean negz, boolean fastPosx, boolean fastNegx, boolean fastPosz, boolean fastNegz) {
        this.x = x;
        this.z = z;
        this.posx = posx;
        this.negx = negx;
        this.posz = posz;
        this.negz = negz;
        this.fastPosx = fastPosx;
        this.fastNegx = fastNegx;
        this.fastPosz = fastPosz;
        this.fastNegz = fastNegz;
        this.chunkPos = chunkPos;
    }

    public ChunkInfo(CompoundTag tag) {
        this.x = tag.getInt("x");
        this.z = tag.getInt("z");
        this.posx = tag.getBoolean("posx");
        this.negx = tag.getBoolean("negx");
        this.posz = tag.getBoolean("posz");
        this.negz = tag.getBoolean("negz");
        this.fastPosx = tag.getBoolean("fastPosx");
        this.fastPosz = tag.getBoolean("fastPosz");
        this.fastNegx = tag.getBoolean("fastNegx");
        this.fastNegz = tag.getBoolean("fastNegz");
        this.blocks = BlockInChunkData.readAll(tag.getCompound("blocks"));
        this.fast_blocks = BlockInChunkData.readAll(tag.getCompound("fastblocks"));
        this.chunkPos = new ChunkPos(tag.getInt("chunkx"), tag.getInt("chunkz"));
    }

    public void createChunkRenderer(int lowestY, int renderY, Level level) {
        int highestY = ChunkUtil.getHighestBlock(level, chunkPos).getY();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = lowestY; y < highestY + 1; y++) {
                    BlockPos pos = new BlockPos(chunkPos.getBlockX(x), y, chunkPos.getBlockZ(z));
                    if (renderAble(pos, level)) {
                        blocks.add(new BlockInChunkData(level.getBlockState(pos), pos, new VectorData(level, pos, fastPosx, fastNegx, fastPosz, fastNegz, lowestY), new VectorData(level, pos, posx, negx, posz, negz, lowestY), x, z));
                    }
                    else if (pos.getY() >= renderY + 1 && !level.getBlockState(pos).is(ModTags.Blocks.DONT_RENDER_BLOCK) && !level.getBlockState(pos).is(Blocks.AIR)) {
                        for (Direction d : Direction.values()) {
                            if (VectorData.shouldRenderWithinChunk(level.getChunkAt(pos).getPos(), pos, d, posx, negx, posz, negz)) {
                                blocks.add(new BlockInChunkData(Blocks.STONE.defaultBlockState(), pos, new VectorData(level, pos, fastPosx, fastNegx, fastPosz, fastNegz, lowestY), new VectorData(level, pos, posx, negx, posz, negz, lowestY), x, z));
                                break;
                            }
                        }
                    }
                    if (pos.getY() >= renderY + 1 && !level.getBlockState(pos).is(ModTags.Blocks.DONT_RENDER_BLOCK) && !level.getBlockState(pos).is(Blocks.AIR)) {
                        for (Direction d : Direction.values()) {
                            if (VectorData.shouldRenderWithinChunk(level.getChunkAt(pos).getPos(), pos, d, fastPosx, fastNegx, fastPosz, fastNegz)) {
                                fast_blocks.add(new BlockInChunkData(Blocks.STONE.defaultBlockState(), pos, new VectorData(level, pos, fastPosx, fastNegx, fastPosz, fastNegz, lowestY), new VectorData(level, pos, posx, negx, posz, negz, lowestY), x, z));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean renderAble(BlockPos pos, Level level) {
        if (!level.getBlockState(pos).is(ModTags.Blocks.DONT_RENDER_BLOCK) && !level.getBlockState(pos).is(Blocks.AIR)) {
            for (Direction d : Direction.values()) {
                BlockState state = level.getBlockState(pos.relative(d));
                if (!state.canOcclude() && state.getFluidState().isEmpty())
                    if (level.getBrightness(LightLayer.SKY, pos.relative(d)) > 2)
                        return true;
                if (!Block.isShapeFullBlock(state.getShape(level, pos.relative(d))) && d != Direction.UP && !state.getFluidState().isEmpty() && // WTF DOES THIS DO (IDR)
                        !(!state.getFluidState().isEmpty() && level.getBlockState(pos.relative(d).relative(Direction.UP)).getFluidState().isEmpty())) return true;
            }
        }
        return false;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public List<BlockInChunkData> getBlocks() {
        return blocks;
    }
    public List<BlockInChunkData> getFastBlocks() {
        return fast_blocks;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("z", z);
        tag.putBoolean("posx", posx);
        tag.putBoolean("posz", posz);
        tag.putBoolean("negz", negz);
        tag.putBoolean("negx", negx);
        tag.putBoolean("fastNegx", fastNegx);
        tag.putBoolean("fastPosx", fastPosx);
        tag.putBoolean("fastNegz", fastNegz);
        tag.putBoolean("fastPosz", fastPosz);
        tag.putInt("chunkx", chunkPos.x);
        tag.putInt("chunkz", chunkPos.z);
        tag.put("blocks", BlockInChunkData.saveAll(blocks));
        tag.put("fastblocks", BlockInChunkData.saveAll(fast_blocks));
        return tag;
    }
}
