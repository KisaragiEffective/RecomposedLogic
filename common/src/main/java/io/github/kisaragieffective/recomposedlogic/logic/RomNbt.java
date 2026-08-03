package io.github.kisaragieffective.recomposedlogic.logic;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public final class RomNbt {
    public static final String KEY = "recomposedlogic_program";

    private RomNbt() {
    }

    public static void write(ItemStack stack, RomProgram program) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        root.put(KEY, write(program));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    public static RomProgram read(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag root = data.copyTag();
        if (!root.contains(KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        return read(root.getCompound(KEY));
    }

    public static CompoundTag write(RomProgram program) {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("wasm", program.wasm());
        tag.putInt("scannedVolume", program.scannedVolume());
        ListTag inputs = new ListTag();
        program.inputs().forEach(input -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", input);
            inputs.add(entry);
        });
        tag.put("inputs", inputs);
        ListTag outputs = new ListTag();
        program.outputs().forEach(output -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", output);
            outputs.add(entry);
        });
        tag.put("outputs", outputs);
        ListTag instructions = new ListTag();
        for (IrInstruction instruction : program.instructions()) {
            instructions.add(write(instruction));
        }
        tag.put("instructions", instructions);
        return tag;
    }

    public static RomProgram read(CompoundTag tag) {
        byte[] wasm = tag.getByteArray("wasm");
        int scannedVolume = tag.getInt("scannedVolume");
        List<String> inputs = strings(tag.getList("inputs", Tag.TAG_COMPOUND));
        List<String> outputs = strings(tag.getList("outputs", Tag.TAG_COMPOUND));
        List<IrInstruction> instructions = new ArrayList<>();
        ListTag instructionTags = tag.getList("instructions", Tag.TAG_COMPOUND);
        for (int i = 0; i < instructionTags.size(); i++) {
            instructions.add(readInstruction(instructionTags.getCompound(i)));
        }
        return new RomProgram(wasm, instructions, inputs, outputs, scannedVolume);
    }

    private static CompoundTag write(IrInstruction instruction) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", instruction.id());
        tag.putString("kind", instruction.kind().name());
        tag.putString("outputType", instruction.outputType().name());
        tag.putDouble("scalarValue", instruction.scalarValue());
        tag.putString("operator", instruction.comparatorOperator().name());
        ListTag inputs = new ListTag();
        instruction.inputs().forEach(input -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", input);
            inputs.add(entry);
        });
        tag.put("inputs", inputs);
        if (instruction.romProgram() != null) {
            tag.put("rom", write(instruction.romProgram()));
        }
        return tag;
    }

    private static IrInstruction readInstruction(CompoundTag tag) {
        RomProgram nested = tag.contains("rom", Tag.TAG_COMPOUND) ? read(tag.getCompound("rom")) : null;
        return new IrInstruction(
                tag.getString("id"),
                NodeKind.valueOf(tag.getString("kind")),
                SignalType.valueOf(tag.getString("outputType")),
                strings(tag.getList("inputs", Tag.TAG_COMPOUND)),
                tag.getDouble("scalarValue"),
                ComparatorOperator.valueOf(tag.getString("operator")),
                nested
        );
    }

    private static List<String> strings(ListTag tags) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            values.add(tags.getCompound(i).getString("id"));
        }
        return values;
    }
}
