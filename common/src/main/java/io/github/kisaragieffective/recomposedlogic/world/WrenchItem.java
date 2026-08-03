package io.github.kisaragieffective.recomposedlogic.world;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WrenchItem extends Item {
    private static final Map<UUID, BlockPos> ORIGIN_SELECTIONS = new HashMap<>();

    public WrenchItem(Properties properties) {
        super(properties);
    }

    public static void useOnLogic(Player player, BlockPos pos, LogicBlockEntity logic) {
        if (logic.kind() == LogicBlockKind.CIRCUIT_ORIGIN) {
            ORIGIN_SELECTIONS.put(player.getUUID(), pos.immutable());
            message(player, "Origin selected");
            return;
        }
        if (logic.kind() == LogicBlockKind.CIRCUIT_SCANNER) {
            BlockPos origin = ORIGIN_SELECTIONS.get(player.getUUID());
            if (origin == null) {
                message(player, "Select a Circuit Origin first");
                return;
            }
            logic.pairedOrigin(origin);
            message(player, "Scanner paired to origin " + origin.toShortString());
            return;
        }
        message(player, "No wrench action for " + logic.kind());
    }

    private static void message(Player player, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW), true);
    }
}
