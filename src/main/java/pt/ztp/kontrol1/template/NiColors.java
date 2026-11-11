// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

import de.mossgrabers.framework.controller.color.ColorEx;


/**
 * The key zone colors of NI's Controller Editor, and how the light guide shows them.
 *
 * Controller Editor stores a color as its index in its color list, which is the order of its color
 * picker (four colors per row). The RGB values are the ones which are sent to the keyboard (0-127
 * per channel). They are tuned by eye on an S88, so that the keys look like the picker's colors.
 * Black leaves the keys of a zone dark.
 *
 * @author akaztp
 */
public final class NiColors
{
    private record Entry (String name, int red, int green, int blue)
    {
        // Intentionally empty
    }


    /** The highest value of a color channel, as sent to the keyboard. */
    private static final int      MAX_VALUE = 127;

    // @formatter:off
    private static final Entry [] ENTRIES =
    {
        new Entry ("Red",          110,   0,   0),
        new Entry ("Orange",       127,  19,   0),
        new Entry ("Light Orange", 127,  40,   0),
        new Entry ("Warm Yellow",  127,  74,   0),
        new Entry ("Yellow",       127,  85,   0),
        new Entry ("Lime",          63, 127,   0),
        new Entry ("Green",          0, 127,   0),
        new Entry ("Mint",           0, 127,  31),
        new Entry ("Cyan",           5, 127, 127),
        new Entry ("Turquoise",      0, 127,  91),
        new Entry ("Blue",           0,  10, 127),
        new Entry ("Plum",          33,  25, 127),
        new Entry ("Violet",        61,  24, 105),
        new Entry ("Purple",       105,  25, 122),
        new Entry ("Magenta",      127,   0, 127),
        new Entry ("Fuchsia",      127,   0,  68),
        new Entry ("Black",          0,   0,   0)
    };
    // @formatter:on

    /** The number of colors. */
    public static final int         NUM_COLORS    = ENTRIES.length;
    /** The index of orange, which is used for missing and unknown colors. */
    public static final int         DEFAULT_INDEX = 1;

    private static final ColorEx [] COLORS        = new ColorEx [NUM_COLORS];

    static
    {
        for (int i = 0; i < NUM_COLORS; i++)
            COLORS[i] = new ColorEx (ENTRIES[i].red / (double) MAX_VALUE, ENTRIES[i].green / (double) MAX_VALUE, ENTRIES[i].blue / (double) MAX_VALUE);
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
     * @return The index, or {@link #DEFAULT_INDEX} if the color is unknown
     */
    public static int toKnownIndex (final int index)
    {
        return index >= 0 && index < NUM_COLORS ? index : DEFAULT_INDEX;
    }


    /**
     * Get the name of a color, for diagnostics.
     *
     * @param index The index of the color
     * @return The name, the default color's for unknown colors
     */
    public static String getName (final int index)
    {
        return ENTRIES[toKnownIndex (index)].name;
    }


    /**
     * Get the RGB value of a color, as the keys show it.
     *
     * @param index The index of the color
     * @return The color, the default color for unknown colors
     */
    public static ColorEx getColor (final int index)
    {
        return COLORS[toKnownIndex (index)];
    }
}
