package io.github.kisaragieffective.recomposedlogic.world.block;

import com.mojang.serialization.MapCodec;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKind;
import io.github.kisaragieffective.recomposedlogic.world.LogicBlockKindProvider;
import net.minecraft.world.level.block.Block;

public class PinCableBlock extends Block implements LogicBlockKindProvider {
    public static final MapCodec<PinCableBlock> CODEC = simpleCodec(PinCableBlock::new);

    public PinCableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public LogicBlockKind kind() {
        return LogicBlockKind.PIN_CABLE;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
