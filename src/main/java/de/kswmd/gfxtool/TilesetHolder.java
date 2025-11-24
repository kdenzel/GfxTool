/*
 * Copyright 2025 kai
 */
package de.kswmd.gfxtool;

import de.kswmd.gfxtool.tiles.DmgTile;
import de.kswmd.gfxtool.tiles.TileExtractingMethod;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 *
 * @author kai
 */
public class TilesetHolder {

    private final Path tilesetImagePath;
    private final BufferedImage tilesetImage;
    private final List<DmgTile> dmgTileList = new ArrayList<>();
    private String[] colorPal;

    public TilesetHolder(Path tilesetImagePath) throws IOException {
        this.tilesetImagePath = tilesetImagePath;
        this.tilesetImage = ImageIO.read(tilesetImagePath.toFile());
    }

    public void initialize(TileExtractingMethod method, String[] colorPal) {
        this.colorPal = colorPal;
        initialize(method);
    }

    public void initialize(TileExtractingMethod method) {
        dmgTileList.clear();
        for (int y = 0; y < tilesetImage.getHeight(); y += DmgTile.TILE_DIMENSION) {
            for (int x = 0; x < tilesetImage.getWidth(); x += DmgTile.TILE_DIMENSION) {
                var subImg = tilesetImage.getSubimage(x, y, DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION);
                dmgTileList.add(new DmgTile(subImg, method, colorPal));
            }
        }
    }

    public String[] getColorPalFromTilesetImage() {
        Set<String> colorPalSet = new HashSet<>();
        for (int y = 0; y < tilesetImage.getHeight(); y++) {
            for (int x = 0; x < tilesetImage.getWidth(); x++) {
                var rgb = tilesetImage.getRGB(x, y);
                colorPalSet.add(Integer.toHexString(rgb));
            }
        }
        return colorPalSet.toArray(String[]::new);
    }

    public Path getTilesetImagePath() {
        return tilesetImagePath;
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

    public List<DmgTile> getDmgTiles() {
        return dmgTileList;
    }

    public void writeAllTilesTo2BppBinary(String outputPath) throws FileNotFoundException, IOException {
        OutputStream fos = new FileOutputStream(outputPath);
        for (DmgTile t : getDmgTiles()) {
            byte[] b = t.get2BppArrayFromTile();
            fos.write(b);
        }
        fos.flush();
        fos.close();
    }

    public void createIndices() {
        int i = 0;
        for (DmgTile dt : dmgTileList) {
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
                byte[] buffer = new byte[bfimg.getWidth() / DmgTile.TILE_DIMENSION];
                for (int y = 0; y < bfimg.getHeight(); y += DmgTile.TILE_DIMENSION) {
                    int i = 0;
                    for (int x = 0; x < bfimg.getWidth(); x += DmgTile.TILE_DIMENSION) {
                        BufferedImage sub = bfimg.getSubimage(x, y, DmgTile.TILE_DIMENSION, DmgTile.TILE_DIMENSION);
                        boolean matchFound = false;
                        int index = 0;
                        for (DmgTile t : dmgTileList) {
                            if (t.matches(sub)) {
                                matchFound = true;
                                index = t.getIndex();
                                break;
                            }
                        }

                        buffer[i] = (byte) (index & 0xFF);
                        i++;
                        if (!matchFound) {
                            //TODO: LOG WARN OR STH ELSE
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
        List<DmgTile> tiles = getDmgTiles();
        for (DmgTile t : tiles) {
            g.drawImage(t.getTileImage(), x, y, null);
            x = (x + DmgTile.TILE_DIMENSION) % tilesetImage.getWidth();
            if (x == 0) {
                y += DmgTile.TILE_DIMENSION;
            }
        }
        ImageIO.write(nbi, "png", new File(path + ".png"));
    }

    public int getDmgTileWidth() {
        return tilesetImage.getWidth() / DmgTile.TILE_DIMENSION;
    }

    public int getDmgTileHeight() {
        return tilesetImage.getHeight() / DmgTile.TILE_DIMENSION;
    }
}
