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

    public static void selectOrigin(Player player, BlockPos pos) {
        ORIGIN_SELECTIONS.put(player.getUUID(), pos.immutable());
        message(player, "Origin selected");
    }

    public static BlockPos selectedOrigin(Player player) {
        return ORIGIN_SELECTIONS.get(player.getUUID());
    }

    private static void message(Player player, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW), true);
    }
}
