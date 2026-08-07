package io.github.kisaragieffective.recomposedlogic.world;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public record ColorSet16(int mask) {
    public static final Codec<ColorSet16> CODEC = Codec.INT.xmap(ColorSet16::new, ColorSet16::mask);
    public static final StreamCodec<ByteBuf, ColorSet16> STREAM_CODEC = ByteBufCodecs.INT.map(ColorSet16::new, ColorSet16::mask);
    public static final ColorSet16 EMPTY = new ColorSet16(0);

    public ColorSet16 {
        mask &= 0xffff;
    }

    public static ColorSet16 of(DyeColor color) {
        return EMPTY.with(color);
    }

    public boolean contains(DyeColor color) {
        return (mask & bit(color)) != 0;
    }

    public ColorSet16 with(DyeColor color) {
        return new ColorSet16(mask | bit(color));
    }

    public ColorSet16 without(DyeColor color) {
        return new ColorSet16(mask & ~bit(color));
    }

    public boolean intersects(ColorSet16 other) {
        return (mask & other.mask) != 0;
    }

    public int size() {
        return Integer.bitCount(mask);
    }

    public boolean isEmpty() {
        return mask == 0;
    }

    public boolean isSingle() {
        return size() == 1;
    }

    public List<DyeColor> colors() {
        List<DyeColor> colors = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            if (contains(color)) {
                colors.add(color);
            }
        }
        return colors;
    }

    private static int bit(DyeColor color) {
        return 1 << color.getId();
    }
}
