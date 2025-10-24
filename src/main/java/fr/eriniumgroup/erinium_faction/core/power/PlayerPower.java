package fr.eriniumgroup.erinium_faction.core.power;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerPower implements INBTSerializable<CompoundTag> {
    private double power;
    private double maxPower;

    public PlayerPower() {
        this.power = 0;
        this.maxPower = 0;
    }

    public double getPower() { return power; }
    public double getMaxPower() { return maxPower; }

    public void setMaxPower(double v) {
        maxPower = Math.max(0, v);
        if (power > maxPower) power = maxPower;
    }

    public void setPower(double v) {
        power = Math.max(0, Math.min(v, maxPower));
    }

    public void addPower(double v) {
        if (v == 0) return;
        setPower(power + v);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("p", power);
        nbt.putDouble("m", maxPower);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        power = nbt.getDouble("p");
        maxPower = nbt.getDouble("m");
    }
}