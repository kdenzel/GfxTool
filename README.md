
# Java GfxTool

A tool that converts png files into a 2bpp-tileset and tilemap files for GameBoy DMG.

## Getting started

The tool is oriented at the gfxconv tool from the book "Gameboy coding adventures".

| Command     | Description |
| ---      | ---       |
| -crt --createTileSet | Generates a tileset png image from 3 specified images in $PWD: </path/to/sprites.png> </path/to/background.png> </path/to/window.png> |
| -f --fill | Fills the sprite region and every following not defined tile with empty random generated pixels based on colorpalet from image before for option -crt |
| -conv --convert | Converts an image to a colorpalette defined (maps every pixel depending on how close the pixel is to one of the values): </path/to/image.png> <hexcolor1,hexcolor2,hex3,hex4> (DMG only uses 4 colors) |
| -u --unique | Generates for option -o a tileset in gameboy format with unique tiles and for -crt with unique tiles inside the $PWD/tileset.png image. | 
| -o --output | Creates the tileset in gameboy format based on the parameters: (/path/to/tileset.png)(/path/to/outputfile.2bpp) |
| -c --colorPal     | defines the colorpalet by sending some hexadecimal color values or if no parameter is given the tool tries to look up the color palete out of the image files. If the -c argument is missing completely the tool generates the tileset depending on the grayscale of the image. |
| -scp --sortColorPalet     | has only effect if -c was specified. It automatically sorts the color values by grayscale. So bright pixels are mapped bright and vice versa. |
| -t --tilemaps     | in combination with -o will map the tiles (8x8 pixel units) to the given tileset and create indices for it. You can pass as many image files as you'd like. The output name and path matches the input file but replaces the ending .png with .tlm |
| -h --help     | prints an help message |

## Authors

- [@Kai](https://www.github.com/kdenzel)

