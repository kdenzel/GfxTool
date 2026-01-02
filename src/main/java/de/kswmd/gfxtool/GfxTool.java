/*
 * Copyright 2025 kai
 */
package de.kswmd.gfxtool;

import de.kswmd.gfxtool.tiles.TileExtractingMethod;
import de.kswmd.gfxtool.utils.GfxUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author kai
 */
public class GfxTool {

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("crt")
                .longOpt("createTileSet")
                .desc("Generates a tileset from specified images")
                .numberOfArgs(3)
                .argName("/path/to/sprites.png> </path/to/background.png> </path/to/window.png")
                .get()
        );

        options.addOption(Option.builder("conv")
                .longOpt("convert")
                .desc("Converts an image to colorpal defined")
                .numberOfArgs(3)
                .argName("/path/to/image.png> </path/to/output.png> <hexcolor1,hexcolor2")
                .get()
        );

        options.addOption(Option.builder("f")
                .longOpt("fill")
                .desc("Fills spaces from sprites if missing from <crt, createTileSet> when creating Tileset.")
                .argName("/path/to/sprites.png> </path/to/background.png> </path/to/window.png")
                .get()
        );

        options.addOption(Option.builder("u")
                .longOpt("unique")
                .desc("Generates a png file from <-crt, --createTileSet> with distinct tiles or reads distinct tiles with option <-o, --output>")
                .get()
        );

        options.addOption(Option.builder("o")
                .longOpt("output")
                .desc("Generates a gameboy 2bpp file from png")
                .numberOfArgs(2)
                .argName("/path/to/tileset> </output/file/name")
                .get()
        );

        options.addOption(Option.builder("c")
                .longOpt("colorPal")
                .desc("Adds a colorpalet to the tileset to filter for. If none it tries to identify the colorPalet of the image.")
                .optionalArg(true)
                .argName("List of colors. First 4 values are used.")
                .get()
        );

        options.addOption(Option.builder("scp")
                .longOpt("sortColorPalet")
                .desc("Sorts the color palet by grayscale. Bright first Dark last.")
                .optionalArg(true)
                .argName("if \"1\" is set reverse the colorpalette")
                .get()
        );

        options.addOption(Option.builder("t")
                .longOpt("tilemaps")
                .desc("Generates a tiledmap based on the following files.")
                .hasArgs()
                .argName("file1.png file2.png ...")
                .get()
        );

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Prints this message")
                .get()
        );

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                printHelpMessage(options);
            }

            if (cmd.hasOption("crt")) {
                String[] values = cmd.getOptionValues("crt");
                Path[] paths = new Path[values.length];
                int i = 0;
                for (String v : values) {
                    paths[i] = Path.of(v);
                    i++;
                }
                TilesetHolder th = new TilesetHolder(paths, cmd.hasOption("u"), cmd.hasOption("fill"));
                String outputPath = System.getProperty("user.dir") + "/tileset.png";
                th.recreatePictureFromDmgTiles(outputPath);
                String[] colorPal;
                if (cmd.hasOption("c")) {
                    String colors = cmd.getOptionValue("c");
                    Path p = Path.of(outputPath);
                    BufferedImage img = ImageIO.read(p.toFile());
                    if (colors != null) {
                        colorPal = colors.split(",");
                    } else {
                        colorPal = th.getColorPalArrayFromImage(img);
                        colorPal = Arrays.copyOf(colorPal, Math.min(4, colorPal.length));
                    }

                    img = GfxUtils.convertImagePixelsToColorPal(img, colorPal);
                    ImageIO.write(img, "png", new File(outputPath));
                }
            }

            if (cmd.hasOption("conv")) {
                String[] values = cmd.getOptionValues("conv");
                Path s = Path.of(values[0]);
                Path o = Path.of(values[1]);
                BufferedImage img = ImageIO.read(s.toFile());
                img = GfxUtils.convertImagePixelsToColorPal(img, values[2].split(","));
                ImageIO.write(img, "png", o.toFile());
            }
            
            if (cmd.hasOption("o")) {
                String[] values = cmd.getOptionValues("o");
                if (values == null || values.length != 2) {
                    throw new ParseException("Option '-o' requires exactly 2 arguments");
                }
                String pngPath = values[0];
                String outputPath = values[1];

                Path tilesetImagePath = Path.of(pngPath);
                TilesetHolder th = new TilesetHolder(tilesetImagePath);

                if (!th.isDimensionMultipleOf8()) {
                    throw new ParseException("Image-files dimensions (width and height) must be a multiple of 8");
                }

                String[] colorPal;
                if (cmd.hasOption("c")) {
                    String colors = cmd.getOptionValue("c");
                    if (colors != null) {
                        colorPal = colors.split(",");
                    } else {
                        colorPal = th.getColorPalArrayFromImage();
                    }

                    if (cmd.hasOption("scp")) {
                        String optionValue = cmd.getOptionValue("scp");
                        //only reverse if the value equals 1
                        boolean reverseColorValue = optionValue != null && optionValue.equals("1");
                        colorPal = GfxUtils.getSortedColorPalet(colorPal, reverseColorValue);
                    }

                    th.initialize(TileExtractingMethod.PIXEL_PERFECT, colorPal);

                } else {
                    th.initialize(TileExtractingMethod.GRAY_SCALE);
                }

                if (cmd.hasOption("u")) {
                    th.uniqueTilesOnly();
                }
                
                //uncomment for debug purpose
                //th.recreatePictureFromDmgTiles(tilesetImagePath.toString().replaceAll("\\.png$", "wtf.png"));

                //{"9BBC0F", "8BAC0F", "306230", "0F380F"}
                th.writeAllTilesTo2BppBinary(outputPath);

                if (cmd.hasOption("t")) {
                    String[] paths = cmd.getOptionValues("t");
                    th.createIndices();
                    th.createDmgTileMaps(paths);
                }

            }

        } catch (ParseException ex) {
            System.getLogger(GfxTool.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            printHelpMessage(options);
        } catch (IOException ex) {
            System.getLogger(GfxTool.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            printHelpMessage(options);
        }
    }

    private static void printHelpMessage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Converts .png files to 2bpp files for the DMG-Gameboy.", options);
    }
}
