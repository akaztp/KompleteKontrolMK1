// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

/**
 * The keys of the NI template which the keyboard plays, for the key lights: the zone of each key
 * and which keys are held.
 *
 * The keyboard applies the template itself, so the extension only receives the notes which the
 * template sends. Each key is traced through the zones of the template: it is held if the note
 * which it sends on its zone's MIDI channel is held.
 *
 * The keyboard applies its octave shift before the zones: a key belongs to the zone which contains
 * the note it plays after the shift. So when the keyboard is octave shifted, the zones move along
 * the keys.
 *
 * All methods must be called from the controller thread.
 *
 * @author akaztp
 */
public class NiTemplateKeyboard
{
    private final int             defaultFirstNote;
    private NiTemplate            template;
    private int                   octaveShift;
    private final boolean [] []   heldNotes = new boolean [16] [128];


    /**
     * Constructor.
     *
     * @param defaultFirstNote The note of the left-most key when the keyboard is not octave shifted
     */
    public NiTemplateKeyboard (final int defaultFirstNote)
    {
        this.defaultFirstNote = defaultFirstNote;
    }


    /**
     * Set the template which the keyboard plays.
     *
     * @param template The template or null if unknown
     */
    public void setTemplate (final NiTemplate template)
    {
        this.template = template;
    }


    /**
     * The keyboard was octave shifted.
     *
     * @param firstNote The note of the left-most key, as reported by the keyboard
     */
    public void setFirstNote (final int firstNote)
    {
        this.octaveShift = firstNote - this.defaultFirstNote;
    }


    /**
     * Get the octave shift of the keyboard.
     *
     * @return The shift in semitones
     */
    public int getOctaveShift ()
    {
        return this.octaveShift;
    }


    /**
     * Get the zone of a physical key.
     *
     * @param keyIndex The index of the key, 0 is the left-most key
     * @return The zone or null if the key is not part of a zone or the template is unknown
     */
    public NiZone getZoneForKeyIndex (final int keyIndex)
    {
        return this.template == null ? null : this.template.getZoneForKey (this.getShiftedNote (keyIndex));
    }


    /**
     * Is a physical key held down?
     *
     * @param keyIndex The index of the key, 0 is the left-most key
     * @return True if the note which the key sends is held on any of its zones
     */
    public boolean isKeyHeld (final int keyIndex)
    {
        if (this.template == null)
            return false;

        final int keyNote = this.getShiftedNote (keyIndex);
        for (final NiZone zone: this.template.zones ())
        {
            if (!zone.contains (keyNote))
                continue;
            final int note = keyNote + zone.transpose ();
            if (note >= 0 && note < 128 && this.heldNotes[zone.channel () - 1][note])
                return true;
        }
        return false;
    }


    /**
     * Get the note which a key plays with the octave shift, before the zones transpose it.
     *
     * @param keyIndex The index of the key, 0 is the left-most key
     * @return The note
     */
    private int getShiftedNote (final int keyIndex)
    {
        return this.defaultFirstNote + keyIndex + this.octaveShift;
    }


    /**
     * Handle a note-on received from the keyboard.
     *
     * @param channel The MIDI channel 1-16
     * @param note The note
     * @param velocity The velocity, 0 is handled as note-off
     */
    public void noteOn (final int channel, final int note, final int velocity)
    {
        this.setHeld (channel, note, velocity > 0);
    }


    /**
     * Handle a note-off received from the keyboard.
     *
     * @param channel The MIDI channel 1-16
     * @param note The note
     */
    public void noteOff (final int channel, final int note)
    {
        this.setHeld (channel, note, false);
    }


    private void setHeld (final int channel, final int note, final boolean isHeld)
    {
        if (channel >= 1 && channel <= 16 && note >= 0 && note < 128)
            this.heldNotes[channel - 1][note] = isHeld;
    }
}
