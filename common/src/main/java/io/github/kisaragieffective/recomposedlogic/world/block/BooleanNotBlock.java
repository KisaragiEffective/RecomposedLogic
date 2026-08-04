package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class BooleanNotBlock extends Block implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = List.of(new PortSpec("value", Direction.WEST, false), new PortSpec("value", Direction.EAST, true));
    public static final MapCodec<BooleanNotBlock> CODEC = simpleCodec(BooleanNotBlock::new);

    public BooleanNotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.BOOLEAN_NOT;
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
