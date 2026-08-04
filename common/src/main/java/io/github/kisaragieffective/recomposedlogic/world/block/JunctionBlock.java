package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import net.minecraft.world.level.block.Block;

public class JunctionBlock extends Block implements LogicBlockKindProvider {
    public static final MapCodec<JunctionBlock> CODEC = simpleCodec(JunctionBlock::new);

    public JunctionBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.JUNCTION;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
