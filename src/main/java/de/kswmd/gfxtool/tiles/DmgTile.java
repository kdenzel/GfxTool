/*
 * Copyright 2025 kai
 */
package de.kswmd.gfxtool.tiles;

import static de.kswmd.gfxtool.tiles.TileExtractingMethod.GRAY_SCALE;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 *
 * @author kai
 */
public class DmgTile {

    public static final int TILE_DIMENSION = 8;

    private final BufferedImage tileImage;

    private final byte[] _2bppArray = new byte[16];
    private String[] colorPal;

    private TileExtractingMethod extractingMethod = GRAY_SCALE;

    private int index;

    public DmgTile(BufferedImage tileImage) {
        this.tileImage = tileImage;
    }

    public DmgTile(BufferedImage tileImage, TileExtractingMethod method, String[] colorPal) {
        this.tileImage = tileImage;
        this.extractingMethod = method;
        this.colorPal = colorPal;
    }

    public DmgTile(BufferedImage tileImage, String[] colorPalette) {
        this.tileImage = tileImage;
        this.colorPal = colorPalette;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public BufferedImage getTileImage() {
        return tileImage;
    }

    public void setExtractingMethod(TileExtractingMethod extractingMethod) {
        this.extractingMethod = extractingMethod;
    }

    public void setColorPal(String[] colorPal) {
        this.colorPal = colorPal;
    }

    public boolean matches(BufferedImage tile) {
        if (tile.getWidth() != tileImage.getWidth() || tileImage.getHeight() != tile.getHeight()) {
            return false;
        }
        int width = tile.getWidth();
        int height = tile.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tile.getRGB(x, y) != tileImage.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public byte[] get2BppArrayFromTile() {
        int aIndex = 0;
        for (int y = 0; y < tileImage.getHeight(); y++) {
            byte lb = 0b000000000, hb = 0b000000000;
            for (int x = 0; x < tileImage.getWidth(); x++) {
                int pixelColor = tileImage.getRGB(x, y);
                int a = (pixelColor >> 24) & 0xff;
                int r = (pixelColor >> 16) & 0xff;
                int g = (pixelColor >> 8) & 0xff;
                int b = pixelColor & 0xff;

                int byteValue = -1;
                switch (extractingMethod) {
                    case PIXEL_PERFECT:
                        String hex = Integer.toHexString(pixelColor);
                        for (int i = 0; i < colorPal.length; i++) {
                            if (colorPal[i].toLowerCase().endsWith(hex)) {
                                byteValue = i % 4;
                                break;
                            }
                        }
                        break;
                    case GRAY_SCALE:
                    default:
                        float grayScale = ((0.299f * r) + (0.587f * g) + (0.114f * b)) * (a / 255f);
                        byteValue = (int) ((4 * grayScale) / 256f);
                        break;
                }

                if (byteValue < 0) {
                    throw new IllegalStateException("The value " + byteValue + " is no possible value.");
                }

                switch (byteValue) {
                    case 0://0 0                        
                        break;
                    case 1://0 1
                        hb |= (1 << x);
                        break;
                    case 2://1 0
                        lb |= (1 << x);
                        break;
                    case 3://1 1
                        lb |= (1 << x);
                        hb |= (1 << x);
                        break;
                }
            }
            _2bppArray[aIndex] = (byte) (Integer.reverse(hb & 0xFF) >>> 24);
            _2bppArray[aIndex + 1] = (byte) (Integer.reverse(lb & 0xFF) >>> 24);
            aIndex += 2;
        }
        return _2bppArray;
    }

    private String hashCodeOfPixels() {
        int width = tileImage.getWidth();
        int height = tileImage.getHeight();
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(tileImage.getRGB(x, y));
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.hashCodeOfPixels());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DmgTile other = (DmgTile) obj;
        return matches(other.tileImage);
    }

}
