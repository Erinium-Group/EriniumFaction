package fr.eriniumgroup.erinium_faction.client.waypoint;

import com.google.gson.annotations.SerializedName;

/**
 * Représente un waypoint créé par le joueur
 */
public class Waypoint {
    @SerializedName("name")
    private String name;

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    @SerializedName("z")
    private int z;

    @SerializedName("dimension")
    private String dimension; // ID de dimension (ex: "minecraft:overworld")

    @SerializedName("colorR")
    private int colorR;

    @SerializedName("colorG")
    private int colorG;

    @SerializedName("colorB")
    private int colorB;

    @SerializedName("visible")
    private boolean visible; // true = visible sur map et overlay, false = caché

    public Waypoint(String name, int x, int y, int z, String dimension, int colorR, int colorG, int colorB) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.colorR = Math.max(0, Math.min(255, colorR));
        this.colorG = Math.max(0, Math.min(255, colorG));
        this.colorB = Math.max(0, Math.min(255, colorB));
        this.visible = true; // Visible par défaut
    }

    // Getters
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getDimension() { return dimension; }
    public int getColorR() { return colorR; }
    public int getColorG() { return colorG; }
    public int getColorB() { return colorB; }
    public boolean isVisible() { return visible; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }
    public void setDimension(String dimension) { this.dimension = dimension; }

    public void setColor(int r, int g, int b) {
        this.colorR = Math.max(0, Math.min(255, r));
        this.colorG = Math.max(0, Math.min(255, g));
        this.colorB = Math.max(0, Math.min(255, b));
    }

    public void setVisible(boolean visible) { this.visible = visible; }
    public void toggleVisible() { this.visible = !this.visible; }

    // Obtenir la couleur en format ARGB
    public int getColorARGB() {
        return 0xFF000000 | (colorR << 16) | (colorG << 8) | colorB;
    }

    // Distance 3D au point donné
    public double distanceTo(double x, double y, double z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // Distance 2D (horizontale) au point donné
    public double distanceToXZ(double x, double z) {
        double dx = this.x - x;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}