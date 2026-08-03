package io.github.kisaragieffective.recomposedlogic.world;

import io.github.kisaragieffective.recomposedlogic.logic.ComparatorOperator;
import io.github.kisaragieffective.recomposedlogic.logic.RomNbt;
import io.github.kisaragieffective.recomposedlogic.logic.RomProgram;
import io.github.kisaragieffective.recomposedlogic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LogicBlockEntity extends BlockEntity {
    private LogicBlockKind kind = LogicBlockKind.PIN_CABLE;
    private double scalarValue = 1.0;
    private ComparatorOperator comparatorOperator = ComparatorOperator.GREATER_OR_EQUAL;
    private BlockPos pairedOrigin;
    private BlockPos originMaxOffset = new BlockPos(2, 2, 2);
    private boolean externalOutput;
    private ItemStack slotA = ItemStack.EMPTY;
    private ItemStack slotB = ItemStack.EMPTY;
    private String status = "Idle";
    private RomProgram romProgram;
    private int redstonePower;

    public LogicBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LOGIC, pos, blockState);
        if (blockState.getBlock() instanceof LogicBlock logicBlock) {
            kind = logicBlock.kind();
        }
    }

    public LogicBlockKind kind() {
        return kind;
    }

    public double scalarValue() {
        return scalarValue;
    }

    public void scalarValue(double scalarValue) {
        this.scalarValue = scalarValue;
        changed();
    }

    public ComparatorOperator comparatorOperator() {
        return comparatorOperator;
    }

    public void comparatorOperator(ComparatorOperator comparatorOperator) {
        this.comparatorOperator = comparatorOperator;
        changed();
    }

    public BlockPos pairedOrigin() {
        return pairedOrigin;
    }

    public void pairedOrigin(BlockPos pairedOrigin) {
        this.pairedOrigin = pairedOrigin;
        changed();
    }

    public BlockPos originMaxOffset() {
        return originMaxOffset;
    }

    public void growOrigin() {
        originMaxOffset = originMaxOffset.offset(1, 1, 1);
        changed();
    }

    public boolean externalOutput() {
        return externalOutput;
    }

    public void toggleExternalDirection() {
        externalOutput = !externalOutput;
        changed();
    }

    public ItemStack slotA() {
        return slotA;
    }

    public void slotA(ItemStack slotA) {
        this.slotA = slotA;
        changed();
    }

    public ItemStack slotB() {
        return slotB;
    }

    public void slotB(ItemStack slotB) {
        this.slotB = slotB;
        changed();
    }

    public String status() {
        return status;
    }

    public void status(String status) {
        this.status = status;
        changed();
    }

    public RomProgram romProgram() {
        return romProgram;
    }

    public void romProgram(RomProgram romProgram) {
        this.romProgram = romProgram;
        changed();
    }

    public int redstonePower() {
        return redstonePower;
    }

    public void redstonePower(int redstonePower) {
        this.redstonePower = Math.max(0, Math.min(15, redstonePower));
        changed();
    }

    public void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("kind", kind.name());
        tag.putDouble("scalarValue", scalarValue);
        tag.putString("operator", comparatorOperator.name());
        if (pairedOrigin != null) {
            tag.putLong("pairedOrigin", pairedOrigin.asLong());
        }
        tag.putLong("originMaxOffset", originMaxOffset.asLong());
        tag.putBoolean("externalOutput", externalOutput);
        tag.putString("status", status);
        tag.putInt("redstonePower", redstonePower);
        if (!slotA.isEmpty()) {
            tag.put("slotA", slotA.save(registries));
        }
        if (!slotB.isEmpty()) {
            tag.put("slotB", slotB.save(registries));
        }
        if (romProgram != null) {
            tag.put("romProgram", RomNbt.write(romProgram));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("kind")) {
            kind = LogicBlockKind.valueOf(tag.getString("kind"));
        }
        scalarValue = tag.getDouble("scalarValue");
        if (tag.contains("operator")) {
            comparatorOperator = ComparatorOperator.valueOf(tag.getString("operator"));
        }
        pairedOrigin = tag.contains("pairedOrigin") ? BlockPos.of(tag.getLong("pairedOrigin")) : null;
        originMaxOffset = tag.contains("originMaxOffset") ? BlockPos.of(tag.getLong("originMaxOffset")) : new BlockPos(2, 2, 2);
        externalOutput = tag.getBoolean("externalOutput");
        status = tag.getString("status");
        redstonePower = tag.getInt("redstonePower");
        slotA = tag.contains("slotA", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound("slotA")) : ItemStack.EMPTY;
        slotB = tag.contains("slotB", Tag.TAG_COMPOUND) ? ItemStack.parseOptional(registries, tag.getCompound("slotB")) : ItemStack.EMPTY;
        romProgram = tag.contains("romProgram", Tag.TAG_COMPOUND) ? RomNbt.read(tag.getCompound("romProgram")) : null;
    }
}
