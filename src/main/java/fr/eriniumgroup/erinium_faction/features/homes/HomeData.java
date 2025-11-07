package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Représente un seul home avec son nom et ses coordonnées
 */
public class HomeData {
    private String name;
    private String dimension;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public HomeData(String name, String dimension, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public HomeData() {
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("name", name);
        tag.putString("dimension", dimension);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        return tag;
    }

    public static HomeData load(CompoundTag tag) {
        HomeData data = new HomeData();
        data.name = tag.getString("name");
        data.dimension = tag.getString("dimension");
        data.x = tag.getDouble("x");
        data.y = tag.getDouble("y");
        data.z = tag.getDouble("z");
        data.yaw = tag.getFloat("yaw");
        data.pitch = tag.getFloat("pitch");
        return data;
    }

    // Getters et Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Vec3 getPosition() {
        return new Vec3(x, y, z);
    }
}

