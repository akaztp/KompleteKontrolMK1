// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1;

import java.util.Locale;


/**
 * The note inputs through which the keyboard plays Bitwig's tracks.
 *
 * The template in NI's Controller Editor decides on which MIDI channels the keys, pedals and wheels
 * play, and the note inputs receive them unchanged. A track which selects a note input receives all
 * MIDI channels which the input lets through, therefore each MIDI channel has its own note input
 * ("KK1 Ch 1" to "KK1 Ch 16"), so that a track can play a single key zone of the template.
 *
 * @author akaztp
 */
public final class NoteInputs
{
    /** The number of MIDI channels, each has its own note input. */
    public static final int         NUM_CHANNELS         = 16;

    /** The name of the note input which receives all MIDI channels. */
    public static final String      MAIN_INPUT_NAME      = "KK1 Keyboard";

    private static final String     CHANNEL_INPUT_PREFIX = "KK1 Ch ";

    // @formatter:off
    private static final String []  STATUS_CODES         =
    {
        "8", // Note off
        "9", // Note on
        "A", // Polyphonic after-touch
        "B", // Control change: modulation, pedals, ...
        "C", // Program change
        "D", // Channel after-touch
        "E"  // Pitch-bend
    };
    // @formatter:on


    /**
     * Private due to utility class.
     */
    private NoteInputs ()
    {
        // Intentionally empty
    }


    /**
     * Get the name of the note input of a MIDI channel.
     *
     * @param channel The MIDI channel 1-16
     * @return The name
     */
    public static String getChannelInputName (final int channel)
    {
        return CHANNEL_INPUT_PREFIX + channel;
    }


    /**
     * Get the MIDI messages which a note input receives: all channel messages, on all MIDI channels
     * for the main input, or on its MIDI channel for the input of a channel.
     *
     * @param channel 0 for the main input, 1-16 for the input of a MIDI channel
     * @return The filters for Bitwig's note input
     */
    public static String [] getMidiFilters (final int channel)
    {
        final String channelDigit = channel == 0 ? "?" : Integer.toHexString (channel - 1).toUpperCase (Locale.US);
        final String [] filters = new String [STATUS_CODES.length];
        for (int i = 0; i < STATUS_CODES.length; i++)
            filters[i] = STATUS_CODES[i] + channelDigit + "????";
        return filters;
    }


    /**
     * Describe the note inputs for diagnostics.
     *
     * @return The description
     */
    public static String describe ()
    {
        return "Note inputs: " + MAIN_INPUT_NAME + " receives all MIDI channels, " + CHANNEL_INPUT_PREFIX + "1-" + NUM_CHANNELS + " receive their MIDI channel";
    }
}
