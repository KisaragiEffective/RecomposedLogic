package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.world.block.PinCableBlock;
import io.github.kisaragieffective.recomposedlogic.world.entity.ColoredCableBlockEntity;
import io.github.kisaragieffective.recomposedlogic.world.entity.PinCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DyedPinCableItem extends BlockItem {
    private final DyeColor color;

    public DyedPinCableItem(Block block, DyeColor color, Properties properties) {
        super(block, properties);
        this.color = color;
    }

    public DyeColor color() {
        return color;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity clickedEntity = level.getBlockEntity(clickedPos);
        if (clickedEntity instanceof ColoredCableBlockEntity cable) {
            if (cable.colors().contains(color)) {
                if (cable.multipleColors()) {
                    return InteractionResult.SUCCESS;
                }
                BlockPos placePos = clickedPos.relative(context.getClickedFace());
                if (level.getBlockState(placePos).canBeReplaced()) {
                    return placeNewCable(context, placePos);
                }
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide) {
                cable.addColor(color);
                shrinkOne(context.getPlayer());
                PinCableBlock.updateCableAndNeighbors(level, clickedPos);
            }
            return InteractionResult.SUCCESS;
        } else if (clickedEntity instanceof PinCableBlockEntity) {
            return InteractionResult.FAIL;
        }

        BlockPos placePos = level.getBlockState(clickedPos).canBeReplaced()
                ? clickedPos
                : clickedPos.relative(context.getClickedFace());
        return placeNewCable(context, placePos);
    }

    @Override
    public String getDescriptionId() {
        return "item.recomposedlogic." + color.getSerializedName() + "_pin_cable";
    }

    private InteractionResult placeNewCable(UseOnContext context, BlockPos placePos) {
        Level level = context.getLevel();
        InteractionResult result = super.useOn(context);
        if (result.consumesAction() && !level.isClientSide && level.getBlockEntity(placePos) instanceof ColoredCableBlockEntity cable) {
            cable.singleColor(color);
            PinCableBlock.updateCableAndNeighbors(level, placePos);
        }
        return result;
    }

    private void shrinkOne(Player player) {
        if (player == null || player.getAbilities().instabuild) {
            return;
        }
        player.getItemInHand(player.getUsedItemHand()).shrink(1);
    }
}
