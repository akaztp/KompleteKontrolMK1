// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

/**
 * A key zone of a template in NI's Controller Editor.
 *
 * @param label The label of the zone, empty if it has none
 * @param channel The MIDI channel 1-16 which the zone sends on
 * @param lowKey The lowest key of the zone, as the note it plays when the keyboard is not octave
 *            shifted (0-127)
 * @param highKey The highest key of the zone, see lowKey
 * @param transpose The transposition of the zone's notes in semitones
 * @param color The index of the color in Controller Editor's color list, see {@link NiColors}
 *
 * @author akaztp
 */
public record NiZone (String label, int channel, int lowKey, int highKey, int transpose, int color)
{
    /**
     * Does the zone contain the given key? If the low key is above the high key the two are
     * swapped.
     *
     * @param keyNote The key, as the note it plays when the keyboard is not octave shifted
     * @return True if the zone contains the key
     */
    public boolean contains (final int keyNote)
    {
        return keyNote >= Math.min (this.lowKey, this.highKey) && keyNote <= Math.max (this.lowKey, this.highKey);
    }


    /**
     * Get the name of the zone for the display: its label, or its MIDI channel if it has none.
     *
     * @return The name
     */
    public String getDisplayName ()
    {
        return this.label.isBlank () ? "Ch " + this.channel : this.label.trim ();
    }
}
