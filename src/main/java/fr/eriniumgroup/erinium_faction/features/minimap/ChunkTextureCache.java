package fr.eriniumgroup.erinium_faction.features.minimap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache de la minimap sous forme d'UNE SEULE grande image dynamique
 * L'image grandit selon les besoins et se déplace avec le joueur
 */
public class ChunkTextureCache {
    private static final int CHUNK_SIZE = 16;
    private static final int PIXELS_PER_CHUNK = 16; // 1 chunk = 16x16 pixels

    // UNE SEULE grande image pour la surface
    private NativeImage surfaceImage;
    private DynamicTexture surfaceTexture;
    private ResourceLocation surfaceTextureId;

    // UNE SEULE grande image pour underground
    private NativeImage undergroundImage;
    private DynamicTexture undergroundTexture;
    private ResourceLocation undergroundTextureId;

    // Dimensions de l'image (en chunks)
    private int imageWidthChunks = 128; // Taille initiale: 128x128 chunks = 2048x2048 pixels
    private int imageHeightChunks = 128;

    // Offset de l'origine de l'image par rapport aux coordonnées monde (en chunks)
    private int originChunkX = 0;
    private int originChunkZ = 0;
    private boolean initialized = false;

    // Tracker des chunks déjà dessinés dans l'image
    private final Map<ChunkPos, Boolean> renderedChunks = new HashMap<>();

    public ChunkTextureCache(int maxSize) {
        // Les images seront initialisées lors du premier rendu (quand on connaît la position du joueur)
    }

    /**
     * Initialiser l'origine de l'image centrée sur la position du joueur
     */
    public void initializeIfNeeded(ChunkPos playerChunk) {
        if (!initialized) {
            // Centrer l'origine sur le joueur - 64 chunks de rayon
            originChunkX = playerChunk.x - 64;
            originChunkZ = playerChunk.z - 64;
            initImages();
            initialized = true;
        }
    }

    private void initImages() {
        int imageWidth = imageWidthChunks * PIXELS_PER_CHUNK;
        int imageHeight = imageHeightChunks * PIXELS_PER_CHUNK;

        // Surface
        surfaceImage = new NativeImage(imageWidth, imageHeight, false);
        surfaceTexture = new DynamicTexture(surfaceImage);
        surfaceTextureId = Minecraft.getInstance().getTextureManager()
            .register("minimap_surface", surfaceTexture);

        // Underground
        undergroundImage = new NativeImage(imageWidth, imageHeight, false);
        undergroundTexture = new DynamicTexture(undergroundImage);
        undergroundTextureId = Minecraft.getInstance().getTextureManager()
            .register("minimap_underground", undergroundTexture);
    }

    /**
     * Initialiser (juste pour compatibilité avec l'ancien code)
     */
    public void initializeDiskCache(String worldName, String dimensionName) {
        // Plus besoin de cache disque
    }

    /**
     * Obtenir la texture de la minimap
     */
    public ResourceLocation getTextureId(boolean underground) {
        return underground ? undergroundTextureId : surfaceTextureId;
    }

    /**
     * Getters pour l'origine de l'image
     */
    public int getOriginChunkX() {
        return originChunkX;
    }

    public int getOriginChunkZ() {
        return originChunkZ;
    }

    /**
     * Dessiner un chunk dans la grande image
     */
    public void renderChunkAt(ChunkPos chunkPos, Level level, boolean underground) {
        // Vérifier si le chunk est déjà dessiné
        if (renderedChunks.containsKey(chunkPos)) {
            return; // Déjà dessiné
        }

        // Vérifier si le chunk est dans les limites de l'image
        int relativeX = chunkPos.x - originChunkX;
        int relativeZ = chunkPos.z - originChunkZ;

        // Si hors limites, agrandir l'image
        if (relativeX < 0 || relativeZ < 0 ||
            relativeX >= imageWidthChunks || relativeZ >= imageHeightChunks) {
            expandImage(chunkPos);
            // Recalculer après expansion
            relativeX = chunkPos.x - originChunkX;
            relativeZ = chunkPos.z - originChunkZ;
        }

        // Dessiner le chunk dans l'image
        NativeImage image = underground ? undergroundImage : surfaceImage;
        DynamicTexture texture = underground ? undergroundTexture : surfaceTexture;

        try {
            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
            renderChunkToImage(chunk, level, image, relativeX * PIXELS_PER_CHUNK, relativeZ * PIXELS_PER_CHUNK, underground);

            // Upload la texture au GPU
            texture.upload();

            // Marquer comme dessiné
            renderedChunks.put(chunkPos, true);
        } catch (Exception e) {
            // Chunk pas chargé, on skip
        }
    }

    /**
     * Agrandir l'image pour inclure un nouveau chunk
     */
    private void expandImage(ChunkPos newChunk) {
        // Calculer les nouvelles dimensions nécessaires
        int minChunkX = Math.min(originChunkX, newChunk.x);
        int minChunkZ = Math.min(originChunkZ, newChunk.z);
        int maxChunkX = Math.max(originChunkX + imageWidthChunks - 1, newChunk.x);
        int maxChunkZ = Math.max(originChunkZ + imageHeightChunks - 1, newChunk.z);

        int newWidthChunks = maxChunkX - minChunkX + 1;
        int newHeightChunks = maxChunkZ - minChunkZ + 1;

        // Limiter la taille max (pour éviter de manquer de mémoire)
        if (newWidthChunks > 256 || newHeightChunks > 256) {
            // Trop grand, on reset tout
            clearAll();
            originChunkX = newChunk.x - 64;
            originChunkZ = newChunk.z - 64;
            imageWidthChunks = 128;
            imageHeightChunks = 128;
            return;
        }

        int newWidth = newWidthChunks * PIXELS_PER_CHUNK;
        int newHeight = newHeightChunks * PIXELS_PER_CHUNK;

        // Créer nouvelles images plus grandes
        NativeImage newSurfaceImage = new NativeImage(newWidth, newHeight, false);
        NativeImage newUndergroundImage = new NativeImage(newWidth, newHeight, false);

        // Copier l'ancien contenu au bon offset
        int offsetX = (originChunkX - minChunkX) * PIXELS_PER_CHUNK;
        int offsetZ = (originChunkZ - minChunkZ) * PIXELS_PER_CHUNK;

        copyImage(surfaceImage, newSurfaceImage, offsetX, offsetZ);
        copyImage(undergroundImage, newUndergroundImage, offsetX, offsetZ);

        // Libérer les anciennes images
        surfaceImage.close();
        undergroundImage.close();
        surfaceTexture.close();
        undergroundTexture.close();

        // Remplacer par les nouvelles
        surfaceImage = newSurfaceImage;
        undergroundImage = newUndergroundImage;
        surfaceTexture = new DynamicTexture(surfaceImage);
        undergroundTexture = new DynamicTexture(undergroundImage);

        // Enregistrer les nouvelles textures
        Minecraft.getInstance().getTextureManager().release(surfaceTextureId);
        Minecraft.getInstance().getTextureManager().release(undergroundTextureId);
        surfaceTextureId = Minecraft.getInstance().getTextureManager().register("minimap_surface_" + System.currentTimeMillis(), surfaceTexture);
        undergroundTextureId = Minecraft.getInstance().getTextureManager().register("minimap_underground_" + System.currentTimeMillis(), undergroundTexture);

        // Mettre à jour l'origine
        originChunkX = minChunkX;
        originChunkZ = minChunkZ;
        imageWidthChunks = newWidthChunks;
        imageHeightChunks = newHeightChunks;
    }

    /**
     * Copier une image dans une autre
     */
    private void copyImage(NativeImage source, NativeImage dest, int offsetX, int offsetZ) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int z = 0; z < source.getHeight(); z++) {
                int pixel = source.getPixelRGBA(x, z);
                dest.setPixelRGBA(offsetX + x, offsetZ + z, pixel);
            }
        }
    }

    /**
     * Dessiner un chunk dans l'image à une position donnée
     */
    private void renderChunkToImage(ChunkAccess chunk, Level level, NativeImage image, int startX, int startZ, boolean underground) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = chunk.getPos().getMinBlockX() + x;
                int worldZ = chunk.getPos().getMinBlockZ() + z;

                int y;
                try {
                    if (underground) {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) {
                            y = findSolidBlockBelow(level, worldX, (int) mc.player.getY(), worldZ);
                        } else {
                            y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
                        }
                    } else {
                        y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
                    }

                    mutablePos.set(worldX, y, worldZ);
                    BlockState state = level.getBlockState(mutablePos);

                    if (underground && state.isAir()) {
                        int color = 0xFF000000;
                        int abgr = convertARGBtoABGR(color);
                        image.setPixelRGBA(startX + x, startZ + z, abgr);
                        continue;
                    } else if (!underground) {
                        while (state.isAir() && y > level.getMinBuildHeight()) {
                            y--;
                            mutablePos.setY(y);
                            state = level.getBlockState(mutablePos);
                        }
                    }

                    int color = TerrainColorProvider.getBlockColor(state, level, mutablePos);

                    if (color == 0 || color == 0x00000000) {
                        continue;
                    }

                    if (x > 0) {
                        int neighborY = underground ?
                            findSolidBlockBelow(level, worldX - 1, y, worldZ) :
                            level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX - 1, worldZ);
                        color = TerrainColorProvider.applyHeightShading(color, y, neighborY);
                    }

                    if (underground && Minecraft.getInstance().player != null) {
                        int playerY = (int) Minecraft.getInstance().player.getY();
                        int depth = Math.max(0, playerY - y);
                        color = TerrainColorProvider.applyCaveShading(color, depth / 10);
                    }

                    int abgr = convertARGBtoABGR(color);
                    image.setPixelRGBA(startX + x, startZ + z, abgr);
                } catch (Exception e) {
                    image.setPixelRGBA(startX + x, startZ + z, convertARGBtoABGR(0xFF808080));
                }
            }
        }
    }

    private int findSolidBlockBelow(Level level, int x, int startY, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.isSolidRender(level, pos)) {
                return y;
            }
        }
        return level.getMinBuildHeight();
    }

    private int convertARGBtoABGR(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    /**
     * Obtenir la position de texture pour un chunk donné (pour le rendu)
     */
    public TextureCoords getTextureCoords(ChunkPos chunkPos) {
        int relativeX = chunkPos.x - originChunkX;
        int relativeZ = chunkPos.z - originChunkZ;

        int pixelX = relativeX * PIXELS_PER_CHUNK;
        int pixelZ = relativeZ * PIXELS_PER_CHUNK;

        int totalWidth = imageWidthChunks * PIXELS_PER_CHUNK;
        int totalHeight = imageHeightChunks * PIXELS_PER_CHUNK;

        // UV coords (0.0 - 1.0)
        float u0 = (float) pixelX / totalWidth;
        float v0 = (float) pixelZ / totalHeight;
        float u1 = (float) (pixelX + PIXELS_PER_CHUNK) / totalWidth;
        float v1 = (float) (pixelZ + PIXELS_PER_CHUNK) / totalHeight;

        return new TextureCoords(u0, v0, u1, v1, totalWidth, totalHeight);
    }

    public void markDirty(ChunkPos chunkPos, boolean underground) {
        // Retirer du tracker pour forcer le redessin
        renderedChunks.remove(chunkPos);
    }

    public void markDirtyBoth(ChunkPos chunkPos) {
        renderedChunks.remove(chunkPos);
    }

    public void removeFromCache(ChunkPos chunkPos) {
        renderedChunks.remove(chunkPos);
    }

    public void clear() {
        clearAll();
    }

    private void clearAll() {
        renderedChunks.clear();
        if (surfaceImage != null) surfaceImage.close();
        if (undergroundImage != null) undergroundImage.close();
        if (surfaceTexture != null) surfaceTexture.close();
        if (undergroundTexture != null) undergroundTexture.close();
        initImages();
    }

    public record TextureCoords(float u0, float v0, float u1, float v1, int totalWidth, int totalHeight) {}

    // Méthodes de compatibilité pour l'ancien code
    public CachedChunkTexture getIfCached(ChunkPos chunkPos, boolean underground) {
        return null; // Plus utilisé
    }

    public CachedChunkTexture getOrCreate(ChunkPos chunkPos, Level level, boolean underground) {
        renderChunkAt(chunkPos, level, underground);
        return null; // Plus utilisé
    }

    public Map<ChunkPos, CachedChunkTexture> getAllCached(boolean underground) {
        return new HashMap<>(); // Plus utilisé
    }

    public static class CachedChunkTexture {
        private final ResourceLocation textureId;
        private boolean dirty;

        public CachedChunkTexture(ResourceLocation textureId) {
            this.textureId = textureId;
        }

        public ResourceLocation getTextureId() {
            return textureId;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public void release() {}
    }
}
