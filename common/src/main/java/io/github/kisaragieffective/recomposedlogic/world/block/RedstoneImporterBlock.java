package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import io.github.kisaragieffective.recomposedlogic.world.PortSpec;
import io.github.kisaragieffective.recomposedlogic.world.Ports;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class RedstoneImporterBlock extends Block implements LogicBlockKindProvider {
    public static final List<PortSpec> PORTS = Ports.allOutputs("value");
    public static final MapCodec<RedstoneImporterBlock> CODEC = simpleCodec(RedstoneImporterBlock::new);

    public RedstoneImporterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.REDSTONE_IMPORTER;
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
