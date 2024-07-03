package mod.stf.syconn.client.rendering.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.stf.syconn.common.blockEntity.MapBe;
import mod.stf.syconn.init.ModBlocks;
import mod.stf.syconn.util.data.BlockInChunkData;
import mod.stf.syconn.util.data.ChunkHandler;
import mod.stf.syconn.util.data.ChunkInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapRender implements BlockEntityRenderer<MapBe> {

    private final float Scale3x3 = 0.06f;
    private final float Scale5x5 = 0.036f;

    private final Minecraft mc = Minecraft.getInstance();

    public MapRender(BlockEntityRendererProvider.Context pContext) { } // TODO NEED TO FIX 3x3 Sides

    public void render(MapBe pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ChunkHandler handler = pBlockEntity.getChunkHandler();
        if (mc.level.getBlockState(pBlockEntity.getBlockPos()).getBlock() == ModBlocks.MAP_PROJECTOR.get() && !handler.getChunks(false).isEmpty()) {
            pPoseStack.pushPose();
            if (pBlockEntity.isFastRender()) { pPoseStack.translate(0.025, 0.5015, 0.025); pPoseStack.scale(Scale3x3, Scale3x3, Scale3x3); }
            else { pPoseStack.translate(0.21, 0.5264, 0.214); pPoseStack.scale(Scale5x5, Scale5x5, Scale5x5); }
            for (ChunkInfo data : handler.getChunks(pBlockEntity.isFastRender())) { // TODO Need to add edge faces for 3x3
                pPoseStack.pushPose();
                pPoseStack.translate(data.getX() * 16, 0, data.getZ() * 16);
                if (!pBlockEntity.isFastRender())  for (BlockInChunkData blockInChunkData : data.getBlocks()) blockInChunkData.render(pPoseStack, pBufferSource, pPackedLight, handler.getRenderY(), pBlockEntity.isFastRender());
                // TODO WHY IS THE BOTTOM THING FREEZING SERVER
                else for (BlockInChunkData blockInChunkData : data.getBlocks()) blockInChunkData.render(pPoseStack, pBufferSource, pPackedLight, handler.getRenderY(), pBlockEntity.isFastRender());;
                pPoseStack.popPose();
            }
            pPoseStack.popPose();
        }
    }

    public int getViewDistance() {
        return 16;
    }

    public boolean shouldRender(MapBe pBlockEntity, Vec3 pCameraPos) {
        return Vec3.atCenterOf(pBlockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(pCameraPos.multiply(1.0D, 0.0D, 1.0D), (double)this.getViewDistance());
    }
}
