package io.github.kisaragieffective.recomposedlogic.registry;

import io.github.kisaragieffective.recomposedlogic.Constants;
import io.github.kisaragieffective.recomposedlogic.world.WrenchItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final Map<String, Item> ALL = new LinkedHashMap<>();

    public static final Item WRENCH = item("wrench", new WrenchItem(new Item.Properties().stacksTo(1)));
    public static final Item TINY_MEMORY = item("tiny_memory", new Item(new Item.Properties()));
    public static final Item BLUEPRINT = item("blueprint", new Item(new Item.Properties().stacksTo(1)));
    public static final Item PROGRAMMED_ROM = item("programmed_rom", new Item(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }

    public static void register() {
        ModBlocks.ALL.forEach((name, block) -> item(name, new BlockItem(block, new Item.Properties())));
        ALL.forEach((name, item) -> Registry.register(BuiltInRegistries.ITEM, id(name), item));
    }

    private static Item item(String name, Item item) {
        ALL.put(name, item);
        return item;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
