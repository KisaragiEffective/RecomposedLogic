package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlocks;
import io.github.kisaragieffective.recomposedlogic.registry.ModItems;
import io.github.kisaragieffective.recomposedlogic.world.ColorSet16;
import io.github.kisaragieffective.recomposedlogic.world.PerLevelNetworkDiagramCache;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.LogicState;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import io.github.kisaragieffective.recomposedlogic.world.entity.ColoredCableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.DyedPinCableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.PinCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumSet;

public class PinCableBlock extends BaseEntityBlock implements LogicBlockKindProvider {
    public static final MapCodec<PinCableBlock> CODEC = simpleCodec(PinCableBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    private static final VoxelShape SHAPE_X = Block.box(0, 7, 7, 16, 9, 9);
    private static final VoxelShape SHAPE_Y = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape SHAPE_Z = Block.box(7, 7, 0, 9, 9, 16);
    private static final VoxelShape PLACE_SHAPE_X = Shapes.or(SHAPE_X, Block.box(0, 0, 0, 1, 16, 16), Block.box(15, 0, 0, 16, 16, 16));
    private static final VoxelShape PLACE_SHAPE_Y = Shapes.or(SHAPE_Y, Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 15, 0, 16, 16, 16));
    private static final VoxelShape PLACE_SHAPE_Z = Shapes.or(SHAPE_Z, Block.box(0, 0, 0, 16, 16, 1), Block.box(0, 0, 15, 16, 16, 16));
    private static final VoxelShape[] DYED_SHAPES_X = dyedShapes(Direction.Axis.X);
    private static final VoxelShape[] DYED_SHAPES_Y = dyedShapes(Direction.Axis.Y);
    private static final VoxelShape[] DYED_SHAPES_Z = dyedShapes(Direction.Axis.Z);
    private static final VoxelShape[] DYED_PLACE_SHAPES_X = dyedPlaceShapes(Direction.Axis.X, DYED_SHAPES_X);
    private static final VoxelShape[] DYED_PLACE_SHAPES_Y = dyedPlaceShapes(Direction.Axis.Y, DYED_SHAPES_Y);
    private static final VoxelShape[] DYED_PLACE_SHAPES_Z = dyedPlaceShapes(Direction.Axis.Z, DYED_SHAPES_Z);

    public PinCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.PIN_CABLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getBlock() == ModBlocks.DYED_PIN_CABLE) {
            return new DyedPinCableBlockEntity(pos, state);
        }
        return new PinCableBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getBlock() == ModBlocks.DYED_PIN_CABLE && level.getBlockEntity(pos) instanceof ColoredCableBlockEntity cable) {
            return dyedShape(level, pos, state.getValue(AXIS), cable.colors(), isHoldingCable(context));
        }
        return pinShape(state.getValue(AXIS), isHoldingCable(context));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getBlock() == ModBlocks.DYED_PIN_CABLE && level.getBlockEntity(pos) instanceof ColoredCableBlockEntity cable) {
            return dyedShape(level, pos, state.getValue(AXIS), cable.colors(), false);
        }
        return pinShape(state.getValue(AXIS), false);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return rotatePillar(state, rotation);
    }

    private static BlockState rotatePillar(BlockState state, Rotation rotation) {
        Direction.Axis axis = state.getValue(AXIS);
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (axis) {
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !isCableSystemBlock(oldState)) {
            updateCableAndNeighbors(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            updateCableAndNeighbors(level, pos);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        dropCableContents(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static void updateCableAndNeighbors(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            PerLevelNetworkDiagramCache.beginTopologyUpdate(level, pos);
        }
        try {
            updateCable(level, pos);
            for (Direction direction : Direction.values()) {
                updateCable(level, pos.relative(direction));
            }
        } finally {
            if (!level.isClientSide) {
                PerLevelNetworkDiagramCache.endTopologyUpdate(level);
            }
        }
    }

    public static boolean isCableSystemBlock(BlockState state) {
        return state.getBlock() instanceof PinCableBlock || state.getBlock() instanceof InternalJunctionBlock;
    }

    public static void updateCable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isCableSystemBlock(state)) {
            return;
        }
        ColorSnapshot previous = colorSnapshot(level.getBlockEntity(pos));
        if (state.getBlock() == ModBlocks.DYED_INTERNAL_JUNCTION && level.getBlockEntity(pos) instanceof ColoredCableBlockEntity junction) {
            ColorSet16 junctionColors = junctionColors(level, pos, previous.colors());
            if (!junctionColors.equals(previous.colors())) {
                junction.colors(junctionColors);
                previous = new ColorSnapshot(true, junctionColors);
            }
        }
        boolean dyed = previous.dyed();
        Direction.Axis straightAxis = straightAxisFor(level, pos, state, previous);
        BlockState nextState;
        if (straightAxis == null) {
            nextState = (dyed ? ModBlocks.DYED_INTERNAL_JUNCTION : ModBlocks.UNDYED_INTERNAL_JUNCTION).defaultBlockState();
        } else {
            nextState = (dyed ? ModBlocks.DYED_PIN_CABLE : ModBlocks.PIN_CABLE).defaultBlockState().setValue(AXIS, straightAxis);
        }
        if (state == nextState || state.equals(nextState)) {
            return;
        }
        level.setBlock(pos, nextState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (previous.dyed() && level.getBlockEntity(pos) instanceof ColoredCableBlockEntity current) {
            current.colors(previous.colors());
            if (nextState.getBlock() == ModBlocks.DYED_INTERNAL_JUNCTION) {
                ColorSet16 junctionColors = junctionColors(level, pos, current.colors());
                if (!junctionColors.equals(current.colors())) {
                    current.colors(junctionColors);
                }
            }
        }
    }

    public static EnumSet<Direction> connectionDirections(Level level, BlockPos pos) {
        EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (connects(level, pos, direction)) {
                directions.add(direction);
            }
        }
        return directions;
    }

    public static EnumSet<Direction> connectionDirections(Level level, BlockPos pos, DyeColor color) {
        EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (connects(level, pos, direction, color)) {
                directions.add(direction);
            }
        }
        return directions;
    }

    public static Direction.Axis straightAxis(EnumSet<Direction> directions) {
        if (directions.isEmpty()) {
            return Direction.Axis.Y;
        }
        if (directions.size() == 1) {
            return directions.iterator().next().getAxis();
        }
        if (directions.size() == 2) {
            Direction first = directions.iterator().next();
            return directions.contains(first.getOpposite()) ? first.getAxis() : null;
        }
        return null;
    }

    private static boolean connects(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        LogicBlockKind neighborKind = LogicState.kind(level, neighborPos);
        if (neighborKind == LogicBlockKind.PIN_CABLE || neighborKind == LogicBlockKind.JUNCTION) {
            return cableCompatible(level, pos, neighborPos);
        }
        for (PortSpec spec : LogicState.ports(level, neighborPos)) {
            if (spec.direction() == direction.getOpposite()) {
                return !multipleDyed(level, pos);
            }
        }
        return false;
    }

    private static boolean connects(Level level, BlockPos pos, Direction direction, DyeColor color) {
        BlockPos neighborPos = pos.relative(direction);
        LogicBlockKind neighborKind = LogicState.kind(level, neighborPos);
        if (neighborKind == LogicBlockKind.PIN_CABLE || neighborKind == LogicBlockKind.JUNCTION) {
            return cableCompatible(level, pos, neighborPos, color);
        }
        for (PortSpec spec : LogicState.ports(level, neighborPos)) {
            if (spec.direction() == direction.getOpposite()) {
                ColorSnapshot current = colorSnapshot(level.getBlockEntity(pos));
                return current.dyed() && current.colors().isSingle() && current.colors().contains(color);
            }
        }
        return false;
    }

    public static boolean cableCompatible(Level level, BlockPos firstPos, BlockPos secondPos) {
        ColorSnapshot first = colorSnapshot(level.getBlockEntity(firstPos));
        ColorSnapshot second = colorSnapshot(level.getBlockEntity(secondPos));
        boolean firstDyed = first.dyed();
        boolean secondDyed = second.dyed();
        if (!firstDyed && !secondDyed) {
            return true;
        }
        if (!firstDyed) {
            return second.colors().isSingle();
        }
        if (!secondDyed) {
            return first.colors().isSingle();
        }
        return first.colors().intersects(second.colors());
    }

    public static TraversalConnection portTraversalStart(Level level, BlockPos cablePos) {
        ColorSnapshot cable = colorSnapshot(level.getBlockEntity(cablePos));
        if (!cable.dyed()) {
            return TraversalConnection.connected(null);
        }
        if (!cable.colors().isSingle()) {
            return TraversalConnection.disconnected();
        }
        return TraversalConnection.connected(cable.colors().colors().getFirst());
    }

    public static TraversalConnection traversalConnection(Level level, BlockPos firstPos, BlockPos secondPos, DyeColor currentColor) {
        ColorSnapshot first = colorSnapshot(level.getBlockEntity(firstPos));
        ColorSnapshot second = colorSnapshot(level.getBlockEntity(secondPos));
        if (!first.dyed() && !second.dyed()) {
            return TraversalConnection.connected(null);
        }
        if (!first.dyed()) {
            if (!second.colors().isSingle()) {
                return TraversalConnection.disconnected();
            }
            DyeColor nextColor = second.colors().colors().getFirst();
            return currentColor == null || currentColor == nextColor
                    ? TraversalConnection.connected(nextColor)
                    : TraversalConnection.disconnected();
        }
        if (!second.dyed()) {
            return first.colors().isSingle() && (currentColor == null || first.colors().contains(currentColor))
                    ? TraversalConnection.connected(null)
                    : TraversalConnection.disconnected();
        }
        ColorSet16 common = new ColorSet16(first.colors().mask() & second.colors().mask());
        if (common.isEmpty()) {
            return TraversalConnection.disconnected();
        }
        if (currentColor != null) {
            return common.contains(currentColor)
                    ? TraversalConnection.connected(currentColor)
                    : TraversalConnection.disconnected();
        }
        return common.isSingle()
                ? TraversalConnection.connected(common.colors().getFirst())
                : TraversalConnection.disconnected();
    }

    private static boolean cableCompatible(Level level, BlockPos firstPos, BlockPos secondPos, DyeColor color) {
        ColorSnapshot first = colorSnapshot(level.getBlockEntity(firstPos));
        ColorSnapshot second = colorSnapshot(level.getBlockEntity(secondPos));
        if (!first.dyed() || !first.colors().contains(color)) {
            return false;
        }
        if (!second.dyed()) {
            return first.colors().isSingle();
        }
        return second.colors().contains(color);
    }

    public record TraversalConnection(boolean connected, DyeColor color) {
        private static TraversalConnection connected(DyeColor color) {
            return new TraversalConnection(true, color);
        }

        private static TraversalConnection disconnected() {
            return new TraversalConnection(false, null);
        }
    }

    public static boolean multipleDyed(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ColoredCableBlockEntity cable && cable.multipleColors();
    }

    public static void dropCableContents(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || player != null && player.getAbilities().instabuild) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof ColoredCableBlockEntity cable)) {
            popResource(level, pos, new ItemStack(ModBlocks.PIN_CABLE));
            return;
        }
        for (var color : cable.colors().colors()) {
            popResource(level, pos, new ItemStack(ModItems.DYED_PIN_CABLES.get(color)));
        }
    }

    private static ColorSnapshot colorSnapshot(BlockEntity blockEntity) {
        if (blockEntity instanceof ColoredCableBlockEntity cable) {
            return new ColorSnapshot(true, cable.colors());
        }
        return ColorSnapshot.UNDYED;
    }

    private static ColorSet16 junctionColors(Level level, BlockPos pos, ColorSet16 colors) {
        int mask = colors.mask();
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof ColoredCableBlockEntity neighbor) {
                mask |= neighbor.colors().mask();
            }
        }
        return new ColorSet16(mask);
    }

    private record ColorSnapshot(boolean dyed, io.github.kisaragieffective.recomposedlogic.world.ColorSet16 colors) {
        private static final ColorSnapshot UNDYED = new ColorSnapshot(false, io.github.kisaragieffective.recomposedlogic.world.ColorSet16.EMPTY);
    }

    public static Direction.Axis straightAxisFor(Level level, BlockPos pos, BlockState state) {
        return straightAxisFor(level, pos, state, colorSnapshot(level.getBlockEntity(pos)));
    }

    private static Direction.Axis straightAxisFor(Level level, BlockPos pos, BlockState state, ColorSnapshot colors) {
        if (!colors.dyed()) {
            return straightAxis(connectionDirections(level, pos));
        }
        Direction.Axis fallback = state.hasProperty(AXIS) ? state.getValue(AXIS) : Direction.Axis.Y;
        if (colors.colors().isEmpty()) {
            return fallback;
        }
        Direction.Axis axis = null;
        for (DyeColor color : colors.colors().colors()) {
            EnumSet<Direction> directions = connectionDirections(level, pos, color);
            Direction.Axis colorAxis = straightAxis(directions);
            if (colorAxis == null) {
                return null;
            }
            if (directions.isEmpty()) {
                colorAxis = fallback;
            }
            if (axis == null) {
                axis = colorAxis;
            } else if (axis != colorAxis) {
                return null;
            }
        }
        return axis == null ? fallback : axis;
    }

    private static VoxelShape pinShape(Direction.Axis axis, boolean placingCable) {
        return switch (axis) {
            case X -> placingCable ? PLACE_SHAPE_X : SHAPE_X;
            case Y -> placingCable ? PLACE_SHAPE_Y : SHAPE_Y;
            case Z -> placingCable ? PLACE_SHAPE_Z : SHAPE_Z;
        };
    }

    private static VoxelShape dyedShape(BlockGetter level, BlockPos pos, Direction.Axis axis, ColorSet16 colors, boolean placingCable) {
        if (colors.isSingle()) {
            DyeColor color = colors.colors().getFirst();
            VoxelShape adapterShape = Shapes.empty();
            for (Direction direction : Direction.values()) {
                if (isUndyedAdapterTarget(level, pos.relative(direction))) {
                    adapterShape = Shapes.or(adapterShape, dyedAdapterShape(direction, color));
                }
            }
            if (!adapterShape.isEmpty()) {
                return adapterShape;
            }
        }
        VoxelShape shape = Shapes.empty();
        VoxelShape[] slots = switch (axis) {
            case X -> placingCable ? DYED_PLACE_SHAPES_X : DYED_SHAPES_X;
            case Y -> placingCable ? DYED_PLACE_SHAPES_Y : DYED_SHAPES_Y;
            case Z -> placingCable ? DYED_PLACE_SHAPES_Z : DYED_SHAPES_Z;
        };
        for (var color : colors.colors()) {
            shape = Shapes.or(shape, slots[color.getId()]);
        }
        return shape;
    }

    private static boolean isUndyedAdapterTarget(BlockGetter level, BlockPos pos) {
        Block neighbor = level.getBlockState(pos).getBlock();
        return neighbor == ModBlocks.PIN_CABLE || neighbor == ModBlocks.UNDYED_INTERNAL_JUNCTION;
    }

    private static VoxelShape dyedAdapterShape(Direction direction, DyeColor color) {
        double slotX = slotStartA(Direction.Axis.Z, color.getId());
        double slotY = slotStartB(Direction.Axis.Z, color.getId());
        return Shapes.or(
                rotateAdapterBox(direction, 7, 7, 0, 9, 9, 4),
                rotateAdapterBox(direction, Math.min(7, slotX), Math.min(7, slotY), 3, Math.max(9, slotX + 2), Math.max(9, slotY + 2), 10),
                rotateAdapterBox(direction, slotX, slotY, 8, slotX + 2, slotY + 2, 16.8)
        );
    }

    private static VoxelShape rotateAdapterBox(Direction direction, double x0, double y0, double z0, double x1, double y1, double z1) {
        return switch (direction) {
            case NORTH -> Block.box(x0, y0, z0, x1, y1, z1);
            case SOUTH -> Block.box(16 - x1, y0, 16 - z1, 16 - x0, y1, 16 - z0);
            case EAST -> Block.box(16 - z1, y0, x0, 16 - z0, y1, x1);
            case WEST -> Block.box(z0, y0, 16 - x1, z1, y1, 16 - x0);
            case UP -> Block.box(x0, 16 - z1, y0, x1, 16 - z0, y1);
            case DOWN -> Block.box(x0, z0, 16 - y1, x1, z1, 16 - y0);
        };
    }

    private static VoxelShape[] dyedShapes(Direction.Axis axis) {
        VoxelShape[] shapes = new VoxelShape[16];
        for (int id = 0; id < shapes.length; id++) {
            double a0 = slotStartA(axis, id);
            double b0 = slotStartB(axis, id);
            double a1 = a0 + 2.0;
            double b1 = b0 + 2.0;
            shapes[id] = switch (axis) {
                case X -> Block.box(0, a0, b0, 16, a1, b1);
                case Y -> Block.box(a0, 0, b0, a1, 16, b1);
                case Z -> Block.box(a0, b0, 0, a1, b1, 16);
            };
        }
        return shapes;
    }

    private static double slotStartA(Direction.Axis axis, int colorId) {
        int column = colorId & 3;
        int row = colorId >> 2;
        return switch (axis) {
            case X, Y -> slotStart(3 - row);
            case Z -> slotStart(3 - column);
        };
    }

    private static double slotStartB(Direction.Axis axis, int colorId) {
        int column = colorId & 3;
        int row = colorId >> 2;
        return switch (axis) {
            case X -> slotStart(column);
            case Y -> slotStart(3 - column);
            case Z -> slotStart(3 - row);
        };
    }

    private static double slotStart(int slot) {
        return 4.0 + slot * 2.0;
    }

    private static VoxelShape[] dyedPlaceShapes(Direction.Axis axis, VoxelShape[] baseShapes) {
        VoxelShape[] shapes = new VoxelShape[baseShapes.length];
        VoxelShape cap = switch (axis) {
            case X -> Shapes.or(Block.box(0, 0, 0, 1, 16, 16), Block.box(15, 0, 0, 16, 16, 16));
            case Y -> Shapes.or(Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 15, 0, 16, 16, 16));
            case Z -> Shapes.or(Block.box(0, 0, 0, 16, 16, 1), Block.box(0, 0, 15, 16, 16, 16));
        };
        for (int i = 0; i < baseShapes.length; i++) {
            shapes[i] = Shapes.or(baseShapes[i], cap);
        }
        return shapes;
    }

    private static boolean isHoldingCable(CollisionContext context) {
        if (context.isHoldingItem(ModBlocks.PIN_CABLE.asItem())) {
            return true;
        }
        for (Item item : ModItems.DYED_PIN_CABLES.values()) {
            if (context.isHoldingItem(item)) {
                return true;
            }
        }
        return false;
    }
}
