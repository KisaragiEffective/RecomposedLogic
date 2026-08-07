package io.github.kisaragieffective.recomposedlogic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlocks;
import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import io.github.kisaragieffective.recomposedlogic.world.entity.CableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.ColoredCableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PinCableBlockEntityRenderer implements BlockEntityRenderer<CableBlockEntity> {
    private static final double SLOT_START = 16.0 / 64.0;
    private static final double SLOT_SIZE = 8.0 / 64.0;
    private static final double FRAME = 2.0 / 64.0;
    private static final double CONDUCTOR = 4.0 / 64.0;
    private static final double CENTER = 7.0 / 16.0;
    private static final double CENTER_SIZE = 2.0 / 16.0;
    private static final int ADAPTER_ELEMENTS_PER_COLOR = 5;
    private static final ResourceLocation COLORED_TO_UNDYED_MODEL = ResourceLocation.fromNamespaceAndPath("recomposedlogic", "models/block/colored_to_undyed_pin_cable.json");
    private static List<AdapterElement> adapterElements;
    private static final Block[] WOOL_BLOCKS = {
            Blocks.WHITE_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.MAGENTA_WOOL,
            Blocks.LIGHT_BLUE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.LIME_WOOL,
            Blocks.PINK_WOOL,
            Blocks.GRAY_WOOL,
            Blocks.LIGHT_GRAY_WOOL,
            Blocks.CYAN_WOOL,
            Blocks.PURPLE_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.BROWN_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.RED_WOOL,
            Blocks.BLACK_WOOL
    };

    public PinCableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBlockEntity cable, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = cable.getBlockState();
        if (state.getBlock() == ModBlocks.UNDYED_INTERNAL_JUNCTION) {
            renderUndyedJunction(cable, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        if (state.getBlock() == ModBlocks.DYED_INTERNAL_JUNCTION && cable instanceof ColoredCableBlockEntity coloredJunction) {
            renderDyedJunction(coloredJunction, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        Direction.Axis axis = state.hasProperty(PinCableBlock.AXIS) ? state.getValue(PinCableBlock.AXIS) : Direction.Axis.Y;
        if (!(cable instanceof ColoredCableBlockEntity coloredCable)) {
            renderBox(axis, CENTER, CENTER, CENTER_SIZE, CENTER_SIZE, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }
        boolean hasUndyedAdapterConnection = false;
        if (coloredCable.singleColor() && cable.getLevel() != null) {
            DyeColor color = coloredCable.colors().colors().getFirst();
            for (Direction direction : PinCableBlock.connectionDirections(cable.getLevel(), cable.getBlockPos(), color)) {
                Block neighbor = cable.getLevel().getBlockState(cable.getBlockPos().relative(direction)).getBlock();
                if (neighbor == ModBlocks.PIN_CABLE || neighbor == ModBlocks.UNDYED_INTERNAL_JUNCTION) {
                    hasUndyedAdapterConnection = true;
                    renderUndyedAdapter(direction, color, poseStack, bufferSource, packedLight, packedOverlay);
                }
            }
        }
        if (!hasUndyedAdapterConnection) {
            for (DyeColor color : coloredCable.colors().colors()) {
                renderColor(axis, color, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }
    }

    private static void renderDyedJunction(ColoredCableBlockEntity cable, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (cable.getLevel() == null) {
            return;
        }
        for (DyeColor color : cable.colors().colors()) {
            for (Direction direction : PinCableBlock.connectionDirections(cable.getLevel(), cable.getBlockPos(), color)) {
                renderJunctionPlug(direction, color, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }
    }

    private static void renderJunctionPlug(Direction direction, DyeColor color, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        double offsetA = slotOffsetA(direction.getAxis(), color.getId());
        double offsetB = slotOffsetB(direction.getAxis(), color.getId());
        Block wool = WOOL_BLOCKS[color.getId()];
        renderJunctionSegment(direction, offsetA, offsetB, SLOT_SIZE, wool, poseStack, bufferSource, packedLight, packedOverlay);
        renderJunctionSegment(direction, offsetA + FRAME, offsetB + FRAME, CONDUCTOR, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private static void renderJunctionSegment(Direction direction, double offsetA, double offsetB, double size, Block block, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        double depth = 4.0 / 16.0;
        switch (direction) {
            case EAST -> renderBlockBox(1.0 - depth, offsetA, offsetB, depth, size, size, block, poseStack, bufferSource, packedLight, packedOverlay);
            case WEST -> renderBlockBox(0.0, offsetA, offsetB, depth, size, size, block, poseStack, bufferSource, packedLight, packedOverlay);
            case UP -> renderBlockBox(offsetA, 1.0 - depth, offsetB, size, depth, size, block, poseStack, bufferSource, packedLight, packedOverlay);
            case DOWN -> renderBlockBox(offsetA, 0.0, offsetB, size, depth, size, block, poseStack, bufferSource, packedLight, packedOverlay);
            case SOUTH -> renderBlockBox(offsetA, offsetB, 1.0 - depth, size, size, depth, block, poseStack, bufferSource, packedLight, packedOverlay);
            case NORTH -> renderBlockBox(offsetA, offsetB, 0.0, size, size, depth, block, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static void renderUndyedAdapter(Direction direction, DyeColor color, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        rotateAdapterFromNorth(direction, poseStack);
        renderAdapterPath(color, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void rotateAdapterFromNorth(Direction direction, PoseStack poseStack) {
        switch (direction) {
            case NORTH -> {
            }
            case SOUTH -> poseStack.rotateAround(Axis.YP.rotationDegrees(180), 0.5F, 0.5F, 0.5F);
            case EAST -> poseStack.rotateAround(Axis.YP.rotationDegrees(-90), 0.5F, 0.5F, 0.5F);
            case WEST -> poseStack.rotateAround(Axis.YP.rotationDegrees(90), 0.5F, 0.5F, 0.5F);
            case UP -> poseStack.rotateAround(Axis.XP.rotationDegrees(90), 0.5F, 0.5F, 0.5F);
            case DOWN -> poseStack.rotateAround(Axis.XP.rotationDegrees(-90), 0.5F, 0.5F, 0.5F);
        }
    }

    private static void renderAdapterPath(DyeColor color, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        List<AdapterElement> elements = adapterElements();
        int start = color.getId() * ADAPTER_ELEMENTS_PER_COLOR;
        if (elements.size() < start + ADAPTER_ELEMENTS_PER_COLOR) {
            return;
        }
        Block wool = WOOL_BLOCKS[color.getId()];
        for (int index = 0; index < ADAPTER_ELEMENTS_PER_COLOR; index++) {
            renderAdapterElement(elements.get(start + index), index == 0 ? Blocks.COPPER_BLOCK : wool, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static void renderAdapterElement(AdapterElement element, Block block, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(element.originX() / 16.0, element.originY() / 16.0, element.originZ() / 16.0);
        poseStack.mulPose(element.axis().rotationDegrees(element.angle()));
        renderBlockBox(
                (element.fromX() - element.originX()) / 16.0,
                (element.fromY() - element.originY()) / 16.0,
                (element.fromZ() - element.originZ()) / 16.0,
                (element.toX() - element.fromX()) / 16.0,
                (element.toY() - element.fromY()) / 16.0,
                (element.toZ() - element.fromZ()) / 16.0,
                block, poseStack, bufferSource, packedLight, packedOverlay
        );
        poseStack.popPose();
    }

    private static List<AdapterElement> adapterElements() {
        if (adapterElements != null) {
            return adapterElements;
        }
        List<AdapterElement> elements = new ArrayList<>();
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(COLORED_TO_UNDYED_MODEL);
        if (resource.isPresent()) {
            try (Reader reader = resource.get().openAsReader()) {
                JsonArray jsonElements = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("elements");
                for (var element : jsonElements) {
                    JsonObject json = element.getAsJsonObject();
                    JsonArray from = json.getAsJsonArray("from");
                    JsonArray to = json.getAsJsonArray("to");
                    JsonObject rotation = json.getAsJsonObject("rotation");
                    JsonArray origin = rotation.getAsJsonArray("origin");
                    elements.add(new AdapterElement(
                            from.get(0).getAsDouble(), from.get(1).getAsDouble(), from.get(2).getAsDouble(),
                            to.get(0).getAsDouble(), to.get(1).getAsDouble(), to.get(2).getAsDouble(),
                            origin.get(0).getAsDouble(), origin.get(1).getAsDouble(), origin.get(2).getAsDouble(),
                            axis(rotation.get("axis").getAsString()), rotation.get("angle").getAsFloat()
                    ));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load colored-to-undyed pin cable model", exception);
            }
        }
        adapterElements = List.copyOf(elements);
        return adapterElements;
    }

    private static Axis axis(String axis) {
        return switch (axis) {
            case "x" -> Axis.XP;
            case "y" -> Axis.YP;
            case "z" -> Axis.ZP;
            default -> throw new IllegalArgumentException("Unsupported model rotation axis: " + axis);
        };
    }

    private record AdapterElement(
            double fromX, double fromY, double fromZ,
            double toX, double toY, double toZ,
            double originX, double originY, double originZ,
            Axis axis, float angle
    ) {
    }

    private static void renderUndyedJunction(CableBlockEntity cable, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderBlockBox(CENTER, CENTER, CENTER, CENTER_SIZE, CENTER_SIZE, CENTER_SIZE, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
        if (cable.getLevel() == null) {
            return;
        }
        for (Direction direction : PinCableBlock.connectionDirections(cable.getLevel(), cable.getBlockPos())) {
            renderSegment(direction, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static void renderSegment(Direction direction, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        switch (direction) {
            case EAST -> renderBlockBox(0.5, CENTER, CENTER, 0.5, CENTER_SIZE, CENTER_SIZE, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
            case WEST -> renderBlockBox(0.0, CENTER, CENTER, 0.5, CENTER_SIZE, CENTER_SIZE, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
            case UP -> renderBlockBox(CENTER, 0.5, CENTER, CENTER_SIZE, 0.5, CENTER_SIZE, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
            case DOWN -> renderBlockBox(CENTER, 0.0, CENTER, CENTER_SIZE, 0.5, CENTER_SIZE, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
            case SOUTH -> renderBlockBox(CENTER, CENTER, 0.5, CENTER_SIZE, CENTER_SIZE, 0.5, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
            case NORTH -> renderBlockBox(CENTER, CENTER, 0.0, CENTER_SIZE, CENTER_SIZE, 0.5, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static void renderColor(Direction.Axis axis, DyeColor color, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int id = color.getId();
        double offsetA = slotOffsetA(axis, id);
        double offsetB = slotOffsetB(axis, id);
        Block wool = WOOL_BLOCKS[id];
        renderBox(axis, offsetA, offsetB, FRAME, SLOT_SIZE, wool, poseStack, bufferSource, packedLight, packedOverlay);
        renderBox(axis, offsetA + SLOT_SIZE - FRAME, offsetB, FRAME, SLOT_SIZE, wool, poseStack, bufferSource, packedLight, packedOverlay);
        renderBox(axis, offsetA + FRAME, offsetB, CONDUCTOR, FRAME, wool, poseStack, bufferSource, packedLight, packedOverlay);
        renderBox(axis, offsetA + FRAME, offsetB + SLOT_SIZE - FRAME, CONDUCTOR, FRAME, wool, poseStack, bufferSource, packedLight, packedOverlay);
        renderBox(axis, offsetA + FRAME, offsetB + FRAME, CONDUCTOR, CONDUCTOR, Blocks.COPPER_BLOCK, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private static double slotOffsetA(Direction.Axis axis, int colorId) {
        int column = colorId & 3;
        int row = colorId >> 2;
        return switch (axis) {
            case X, Y -> slotOffset(3 - row);
            case Z -> slotOffset(3 - column);
        };
    }

    private static double slotOffsetB(Direction.Axis axis, int colorId) {
        int column = colorId & 3;
        int row = colorId >> 2;
        return switch (axis) {
            case X -> slotOffset(column);
            case Y -> slotOffset(3 - column);
            case Z -> slotOffset(3 - row);
        };
    }

    private static double slotOffset(int slot) {
        return SLOT_START + slot * SLOT_SIZE;
    }

    private static void renderBox(Direction.Axis axis, double offsetA, double offsetB, double sizeA, double sizeB, Block block, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        switch (axis) {
            case X -> renderBlockBox(0.0, offsetA, offsetB, 1.0, sizeA, sizeB, block, poseStack, bufferSource, packedLight, packedOverlay);
            case Y -> renderBlockBox(offsetA, 0.0, offsetB, sizeA, 1.0, sizeB, block, poseStack, bufferSource, packedLight, packedOverlay);
            case Z -> renderBlockBox(offsetA, offsetB, 0.0, sizeA, sizeB, 1.0, block, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static void renderBlockBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, Block block, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale((float) sizeX, (float) sizeY, (float) sizeZ);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
