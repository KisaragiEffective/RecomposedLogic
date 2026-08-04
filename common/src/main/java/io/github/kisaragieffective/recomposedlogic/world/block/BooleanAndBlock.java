package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BooleanAndBlock extends Block implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("left", Direction.WEST, false), new PortSpec("right", Direction.NORTH, false), new PortSpec("value", Direction.EAST, true));
    public static final MapCodec<BooleanAndBlock> CODEC = simpleCodec(BooleanAndBlock::new);

    public BooleanAndBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.BOOLEAN_AND;
    }

    @Override
    public List<PortSpec> ports() {
        return PORTS;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
