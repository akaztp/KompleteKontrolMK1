// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

import de.mossgrabers.framework.controller.color.ColorEx;


/**
 * The key zone colors of NI's Controller Editor, and how the light guide shows them.
 *
 * Controller Editor stores a color as its index in its color list. Index 1 (orange) and 9
 * (turquoise, which the keyboard shows as a blue) are confirmed on an S88; the others follow the
 * order of the list. The RGB values are chosen to be distinguishable on the light guide.
 *
 * @author akaztp
 */
public final class NiColors
{
    private record Entry (String name, int red, int green, int blue)
    {
        // Intentionally empty
    }


    // @formatter:off
    private static final Entry [] ENTRIES =
    {
        new Entry ("Red",          255,   0,   0),
        new Entry ("Orange",       255, 110,   0),
        new Entry ("Light Orange", 255, 160,   0),
        new Entry ("Warm Yellow",  255, 200,   0),
        new Entry ("Yellow",       255, 255,   0),
        new Entry ("Lime",         170, 255,   0),
        new Entry ("Green",          0, 255,   0),
        new Entry ("Mint",          60, 255, 160),
        new Entry ("Cyan",           0, 255, 255),
        new Entry ("Turquoise",      0, 120, 255),
        new Entry ("Blue",           0,   0, 255),
        new Entry ("Plum",          80,   0, 255),
        new Entry ("Violet",       140,   0, 255),
        new Entry ("Purple",       180,   0, 220),
        new Entry ("Magenta",      255,   0, 255),
        new Entry ("Fuchsia",      255,   0, 170),
        new Entry ("White",        255, 255, 255)
    };
    // @formatter:on

    /** The number of colors. */
    public static final int         NUM_COLORS = ENTRIES.length;
    /** The index of white, which is used for unknown colors. */
    public static final int         WHITE      = NUM_COLORS - 1;

    private static final ColorEx [] COLORS     = new ColorEx [NUM_COLORS];

    static
    {
        for (int i = 0; i < NUM_COLORS; i++)
            COLORS[i] = ColorEx.fromRGB (ENTRIES[i].red, ENTRIES[i].green, ENTRIES[i].blue);
    }


    /**
     * Private due to utility class.
     */
    private NiColors ()
    {
        // Intentionally empty
    }


    /**
     * Check the index of a color.
     *
     * @param index The index of the color in Controller Editor's color list
     * @return The index, or {@link #WHITE} if the color is unknown
     */
    public static int toKnownIndex (final int index)
    {
        return index >= 0 && index < NUM_COLORS ? index : WHITE;
    }


    /**
     * Get the name of a color, as Controller Editor calls it.
     *
     * @param index The index of the color
     * @return The name, white for unknown colors
     */
    public static String getName (final int index)
    {
        return ENTRIES[toKnownIndex (index)].name;
    }


    /**
     * Get the RGB value of a color.
     *
     * @param index The index of the color
     * @return The color, white for unknown colors
     */
    public static ColorEx getColor (final int index)
    {
        return COLORS[toKnownIndex (index)];
    }
}
