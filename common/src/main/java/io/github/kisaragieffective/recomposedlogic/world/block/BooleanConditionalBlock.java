package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BooleanConditionalBlock extends Block implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("condition", Direction.WEST, false), new PortSpec("ifTrue", Direction.NORTH, false), new PortSpec("ifFalse", Direction.SOUTH, false), new PortSpec("value", Direction.EAST, true));
    public static final MapCodec<BooleanConditionalBlock> CODEC = simpleCodec(BooleanConditionalBlock::new);

    public BooleanConditionalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.BOOLEAN_CONDITIONAL;
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
