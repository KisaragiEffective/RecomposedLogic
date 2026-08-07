package io.github.kisaragieffective.recomposedlogic.world;

import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorSet16Test {
    @Test
    void colorSetStoresEachDyeColorAsOneBit() {
        ColorSet16 colors = ColorSet16.EMPTY.with(DyeColor.RED).with(DyeColor.BLUE);

        assertTrue(colors.contains(DyeColor.RED));
        assertTrue(colors.contains(DyeColor.BLUE));
        assertFalse(colors.contains(DyeColor.GREEN));
        assertEquals(2, colors.size());
    }

    @Test
    void addingSameColorDoesNotIncreaseSize() {
        ColorSet16 colors = ColorSet16.EMPTY.with(DyeColor.RED).with(DyeColor.RED);

        assertEquals(1, colors.size());
        assertEquals(List.of(DyeColor.RED), colors.colors());
    }

    @Test
    void intersectionsUseSharedColorsOnly() {
        ColorSet16 redBlue = ColorSet16.EMPTY.with(DyeColor.RED).with(DyeColor.BLUE);
        ColorSet16 blueGreen = ColorSet16.EMPTY.with(DyeColor.BLUE).with(DyeColor.GREEN);
        ColorSet16 yellow = ColorSet16.EMPTY.with(DyeColor.YELLOW);

        assertTrue(redBlue.intersects(blueGreen));
        assertFalse(redBlue.intersects(yellow));
    }
}
