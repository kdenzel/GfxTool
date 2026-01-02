/*
 * Copyright 2025 kai
 */
package de.kswmd.gfxtool.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author kai
 */
public class GfxUtils {

    private GfxUtils() {
    }

    public static String[] getSortedColorPalet(String[] colorPal, boolean reverseColors) throws ParseException {
        Map<String, Float> map = new HashMap<>();
        for (var c : colorPal) {
            String nc = c.strip();
            try {
                Color co = getColor(nc);
                int argb = co.getRGB();
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = (argb) & 0xFF;
                float gs = 0.299f * r + 0.587f * g + 0.114f * b;
                map.put(nc, gs);
            } catch (NumberFormatException ex) {
                throw new ParseException("Invalid color value for " + c);
            }
        }
        List<Map.Entry<String, Float>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        //do the reverse when reversed flag is not set cause it is vice versa
        //the dmg gameboy interprets it this way so we have to reverse the list for
        //original colors and for inversed colors we skip the reverse list
        if (!reverseColors) {
            list = list.reversed();
        }
        String[] colors = list.stream().map(e -> e.getKey()).toArray(String[]::new);
        return colors;
    }

    public static double colorDistance(Color a, Color b) {
        double dr = a.getRed() - b.getRed();
        double dg = a.getGreen() - b.getGreen();
        double db = a.getBlue() - b.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db); // RGB Euclidean (simple/fast)
    }

    public static double colorDistance(int pixelColorA, int pixelColorB) {
        int aA = (pixelColorA >> 24) & 0xff;
        int rA = (pixelColorA >> 16) & 0xff;
        int gA = (pixelColorA >> 8) & 0xff;
        int bA = pixelColorA & 0xff;

        int aB = (pixelColorB >> 24) & 0xff;
        int rB = (pixelColorB >> 16) & 0xff;
        int gB = (pixelColorB >> 8) & 0xff;
        int bB = pixelColorB & 0xff;

        double dr = rA - rB;
        double dg = gA - gB;
        double db = bA - bB;
        return Math.sqrt(dr * dr + dg * dg + db * db); // RGB Euclidean (simple/fast)
    }

    public static BufferedImage convertImagePixelsToColorPal(BufferedImage img, String[] colorPal) {
        BufferedImage nImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getRGB(x, y);
                Color a = new Color(color);
                double distance = Double.MAX_VALUE;
                Color nC = a;
                for (String colorString : colorPal) {
                    Color b = getColor(colorString);
                    double cDistance = GfxUtils.colorDistance(a, b);
                    if (cDistance < distance) {
                        nC = b;
                        distance = cDistance;
                    }
                }
                nImg.setRGB(x, y, nC.getRGB());
            }
        }
        return nImg;
    }

    public static Color getColor(String hexString) {
        hexString = hexString.replaceAll("[^A-Fa-f0-9]", "");
        hexString = hexString.length() > 6 ? hexString.substring(hexString.length() - 6) : hexString;
        return Color.decode("#" + hexString);
    }

}
