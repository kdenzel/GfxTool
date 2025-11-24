/*
 * Copyright 2025 kai
 */
package de.kswmd.gfxtool.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author kai
 */
public class GfxUtils {

    private GfxUtils() {
    }

    public static String[] getSortedColorPalet(String[] colorPal) throws ParseException {
        Map<String, Float> map = new HashMap<>();
        for (var c : colorPal) {
            String nc = c.strip();
            try {
                int argb = Long.decode("0x" + nc).intValue();
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
        String colors = list.reversed().stream().map(e -> e.getKey()).collect(Collectors.joining(","));
        return colors.split(",");
    }

}
