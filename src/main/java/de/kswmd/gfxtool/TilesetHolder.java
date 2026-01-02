/*
 * Copyright 2025 kai
 */
package de.kswmd.gfxtool;

import de.kswmd.gfxtool.tiles.DmgTile;
import de.kswmd.gfxtool.tiles.TileExtractingMethod;
import de.kswmd.gfxtool.utils.GfxUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;

/**
 *
 * @author kai
 */
public class TilesetHolder {

    //16 x 24 = 384 tiles
    public static int TILESET_HEIGHT_IN_TILES = 24;
    public static int TILESET_WIDTH_IN_TILES = 16;
    public static int TILESET_SPRITES_SIZE = 128;
    public static int TILES_AMOUNT = TILESET_HEIGHT_IN_TILES * TILESET_WIDTH_IN_TILES;

    private final Path[] tilesetImagePaths;
    private final BufferedImage tilesetImage;
    private Collection<DmgTile> dmgTiles = new ArrayList<>();
    private String[] colorPal;

    /**
     * creates a new Tileset from the given paths
     *
     * @param tilesetImagePaths
     * @param unique
     * @throws IOException
     */
    public TilesetHolder(Path[] tilesetImagePaths, boolean unique, boolean fill) throws IOException {
        this.tilesetImagePaths = tilesetImagePaths;
        tilesetImage = new BufferedImage(TILESET_WIDTH_IN_TILES * DmgTile.TILE_DIMENSION, TILESET_HEIGHT_IN_TILES * DmgTile.TILE_DIMENSION, BufferedImage.TYPE_INT_ARGB);
        List<BufferedImage> images = new ArrayList();
        int pixelAmount = 0;
        //validate
        for (Path imgP : tilesetImagePaths) {
            var tmpImg = ImageIO.read(imgP.toFile());
            if (!isDimensionMultipleOf8(tmpImg)) {
                throw new IIOException("Wrong format of image. Width and height must be multiple of 8 pixels.");
            }
            images.add(tmpImg);
            pixelAmount = tmpImg.getWidth() * tmpImg.getHeight() + pixelAmount;
        }

        if (pixelAmount > TILESET_HEIGHT_IN_TILES * TILESET_WIDTH_IN_TILES * DmgTile.TILE_DIMENSION) {
            if (unique) {
                System.out.println("Warning, the range could exceed.");
            } else {
                throw new IIOException("Image exceeds the maximum tile size.");
            }
        }

        if (unique) {
            dmgTiles = new LinkedHashSet<>();
        }

        String[] lastColorPal = null;
        //collect
        for (BufferedImage tmpImg : images) {
            lastColorPal = getColorPalArrayFromImage(tmpImg);
            List<DmgTile> tmpTileList = new ArrayList<>();
            for (int y = 0; y < tmpImg.getHeight(); y += DmgTile.TILE_DIMENSION) {
                for (int x = 0; x < tmpImg.getWidth(); x += DmgTile.TILE_DIMENSION) {
                    var subImg = tmpImg.getSubimage(x, y, DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION);
                    var tmpDmgTile = new DmgTile(subImg);
                    tmpTileList.add(tmpDmgTile);
                }
            }
            //fill sprite region if fill is set
            if (fill && images.indexOf(tmpImg) == 0) {
                while (tmpTileList.size() < TILESET_SPRITES_SIZE) {
                    tmpTileList.add(createRandomDitstinctTileFromColorPal(tmpTileList, lastColorPal));
                }
            }
            dmgTiles.addAll(tmpTileList);
        }
        if (fill) {
            while (dmgTiles.size() < TILES_AMOUNT) {
                dmgTiles.add(createRandomDitstinctTileFromColorPal(dmgTiles, lastColorPal));
            }
        }

        if (dmgTiles.size() > TILESET_HEIGHT_IN_TILES * TILESET_WIDTH_IN_TILES) {
            throw new IIOException("Image exceeds the maximum tile size.");
        }

    }

    /**
     * reads tileset from single image
     *
     * @param tilesetImagePath
     * @throws IOException
     */
    public TilesetHolder(Path tilesetImagePath) throws IOException {
        this.tilesetImagePaths = new Path[]{tilesetImagePath};
        this.tilesetImage = ImageIO.read(tilesetImagePath.toFile());
    }

    public void initialize(TileExtractingMethod method, String[] colorPal) {
        this.colorPal = colorPal;
        initialize(method);
    }

    public void initialize(TileExtractingMethod method) {
        dmgTiles.clear();
        for (int y = 0; y < tilesetImage.getHeight(); y += DmgTile.TILE_DIMENSION) {
            for (int x = 0; x < tilesetImage.getWidth(); x += DmgTile.TILE_DIMENSION) {
                var subImg = tilesetImage.getSubimage(x, y, DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION);
                dmgTiles.add(new DmgTile(subImg, method, colorPal));
            }
        }
    }

    public Set<String> getColorPalSetFromImage(BufferedImage image) {
        Set<String> colorPalSet = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                var rgb = image.getRGB(x, y);
                colorPalSet.add(Integer.toHexString(rgb));
            }
        }
        return colorPalSet;
    }

    public String[] getColorPalArrayFromImage(BufferedImage image) {
        return getColorPalSetFromImage(image).toArray(String[]::new);
    }

    public String[] getColorPalArrayFromImage() {
        return getColorPalSetFromImage(tilesetImage).toArray(String[]::new);
    }

    public Path[] getTilesetImagePath() {
        return tilesetImagePaths;
    }

    public BufferedImage getTilesetImage() {
        return tilesetImage;
    }

    public boolean isDimensionMultipleOf8() {
        return isDimensionMultipleOf8(tilesetImage);
    }

    public boolean isDimensionMultipleOf8(BufferedImage image) {
        return image.getWidth() % DmgTile.TILE_DIMENSION == 0
                && image.getHeight() % DmgTile.TILE_DIMENSION == 0;
    }

    /**
     * makes all Tiles unique
     */
    public void uniqueTilesOnly() {
        dmgTiles = new LinkedHashSet<>(dmgTiles);
    }

    public Collection<DmgTile> getDmgTiles() {
        return dmgTiles;
    }

    public void writeAllTilesTo2BppBinary(String outputPath) throws FileNotFoundException, IOException {
        writeAllTiles(outputPath);
    }

    private DmgTile createRandomDitstinctTile(Collection<DmgTile> tiles) {
        DmgTile dmgTile = null;
        do {
            var bf = new BufferedImage(DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION, BufferedImage.TYPE_INT_ARGB);
            // create random values pixel by pixel
            for (int y = 0; y < bf.getHeight(); y++) {
                for (int x = 0; x < bf.getWidth(); x++) {
                    // generating values less than 256
                    int a = (int) (Math.random() * 256);
                    int r = (int) (Math.random() * 256);
                    int g = (int) (Math.random() * 256);
                    int b = (int) (Math.random() * 256);

                    //pixel
                    int p = (a << 24) | (r << 16) | (g << 8) | b;

                    bf.setRGB(x, y, p);
                }
                dmgTile = new DmgTile(bf);
            }
        } while (tiles.contains(dmgTile));
        return dmgTile;
    }

    private DmgTile createRandomDitstinctTileFromColorPal(Collection<DmgTile> tiles, String[] colorPal) {
        DmgTile dmgTile = null;
        do {
            var bf = new BufferedImage(DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION, BufferedImage.TYPE_INT_ARGB);
            // create random values pixel by pixel
            for (int y = 0; y < bf.getHeight(); y++) {
                for (int x = 0; x < bf.getWidth(); x++) {
                    Color c = GfxUtils.getColor(colorPal[(int) (Math.random() * colorPal.length)]);
                    // generating values less than 256
                    int a = c.getAlpha();
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();

                    //pixel
                    int p = (a << 24) | (r << 16) | (g << 8) | b;

                    bf.setRGB(x, y, p);
                }
                dmgTile = new DmgTile(bf);
            }
        } while (tiles.contains(dmgTile));
        return dmgTile;
    }

    private void writeAllTiles(String outputPath) throws FileNotFoundException, IOException {
        OutputStream fos = new FileOutputStream(outputPath);
        for (DmgTile t : dmgTiles) {
            byte[] b = t.get2BppArrayFromTile();
            fos.write(b);
        }
        fos.flush();
        fos.close();
    }

    public void createIndices() {
        int i = 0;
        for (DmgTile dt : dmgTiles) {
            //important modulo cause of casting from int to signed byte
            //only need values from 0 - 255
            //cause of different memory allocation in gameboy it is no problem
            //to restart at 0. It's needed
            dt.setIndex(i % 256);
            i++;
        }
    }

    public void createDmgTileMaps(String[] paths) throws IOException {
        for (String fp : paths) {
            Path p = Path.of(fp);
            BufferedImage bfimg = ImageIO.read(p.toFile());
            if (isDimensionMultipleOf8(bfimg)) {
                OutputStream fos = new FileOutputStream(new File(fp.replaceAll("\\.png$", ".tlm")));
                int bs = bfimg.getWidth() / DmgTile.TILE_DIMENSION;
                for (int y = 0; y < bfimg.getHeight(); y += DmgTile.TILE_DIMENSION) {
                    byte[] buffer = new byte[bs];
                    int i = 0;
                    for (int x = 0; x < bfimg.getWidth(); x += DmgTile.TILE_DIMENSION) {
                        BufferedImage sub = bfimg.getSubimage(x, y, DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION);
                        int index = Integer.MAX_VALUE;
                        for (DmgTile t : dmgTiles) {
                            if (t.matches(sub)) {
                                index = t.getIndex();
                                //Do not break here cause if tile 0 is blank or tile in sprite region does match randomly 
                                //iterate to the last matching tile so we do not reference to a tile in sprite region
                                //break;
                            }
                        }

                        buffer[i] = (byte) (index & 0xFF);
                        i++;
                        if (index == Integer.MAX_VALUE) {
                            //TODO: LOG WARN OR STH ELSE
                            throw new IOException("No tile found...");
                        }
                    }
                    fos.write(buffer);
                }
                fos.flush();
                fos.close();
            }
        }
    }

    public void recreatePictureFromDmgTiles(String path) throws IOException {
        BufferedImage nbi = new BufferedImage(tilesetImage.getWidth(), tilesetImage.getHeight(), tilesetImage.getType());
        Graphics2D g = (Graphics2D) nbi.getGraphics();

        int x = 0;
        int y = 0;
        Collection<DmgTile> tiles = getDmgTiles();
        for (DmgTile t : tiles) {
            var img = t.getTileImage();
            g.drawImage(t.getTileImage(), x, y, null);
            x = (x + DmgTile.TILE_DIMENSION);
            if (x % tilesetImage.getWidth() == 0) {
                y += DmgTile.TILE_DIMENSION;
                x = 0;
            }
        }
        ImageIO.write(nbi, "png", new File(path));
    }

    public int getDmgTileWidth() {
        return tilesetImage.getWidth() / DmgTile.TILE_DIMENSION;
    }

    public int getDmgTileHeight() {
        return tilesetImage.getHeight() / DmgTile.TILE_DIMENSION;
    }
}
