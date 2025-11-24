
# Java GfxTool

A tool that converts png files into a 2bpp-tileset and tilemap files for gameboy dmg.

## Getting started

The tool is oriented at the gfxconv tool from the book "Gameboy coding adventures".

| Command     | Description |
| ---      | ---       |
| -o --output | creates the tileset based on the parameter (/path/to/tileset.png)(/path/to/outputfile.2bpp)         |
| -c --colorPal     | defines the colorpalet by sending some hexadecimal color values or if no parameter is given the tool tries to look up the color palete out of the tileset. If the -c argument is missing completely the tool generates the tileset depending on the grayscale of the image.|
| -scp --sortColorPalet     | has only effect if -c was specified. It automatically sorts the color values by grayscale. So bright pixels are mapped bright and vice versa.|
| -t --tilemaps     | in combination with -o will map the tiles (8x8 pixel units) to the given tileset and create indices for it. You can pass as many image files as you'd like. The output name and path matches the input file but replaces the ending .png with .tlm|
| -h --help     | prints an help message|

## Authors

- [@Kai](https://www.github.com/kdenzel)

