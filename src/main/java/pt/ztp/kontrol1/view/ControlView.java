// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.view;

import pt.ztp.kontrol1.Kontrol1Configuration;
import pt.ztp.kontrol1.controller.Kontrol1ColorManager;
import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import pt.ztp.kontrol1.template.NiTemplateKeyboard;
import pt.ztp.kontrol1.template.NiZone;
import de.mossgrabers.framework.controller.grid.ILightGuide;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.AbstractPlayView;


/**
 * The view for controlling the DAW. The keys play the template in NI's Controller Editor, and the
 * light guide shows its key zones.
 *
 * @author Jürgen Moßgraber
 * @author akaztp
 */
public class ControlView extends AbstractPlayView<Kontrol1ControlSurface, Kontrol1Configuration>
{
    /** How long the keys flash on each beat, as a part of the beat. */
    private static final double BEAT_FLASH_LENGTH = 0.25;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public ControlView (final Kontrol1ControlSurface surface, final IModel model)
    {
        super (surface, model, true);
    }


    /** {@inheritDoc} */
    @Override
    public void updateNoteMapping ()
    {
        // The key manager tracks the notes which the selected track plays, to show the played
        // chords. Only set the key manager not the translation matrix, since the NI template decides
        // which notes the keys play!
        final int [] matrix = this.model.canSelectedTrackHoldNotes () ? Scales.getIdentityMatrix () : EMPTY_TABLE;
        this.surface.scheduleTask (() -> this.keyManager.setNoteMatrix (matrix), 6);
    }


    /**
     * Is the playhead at the start of a beat, where the keys flash?
     *
     * @param position The position of the playhead in quarter notes
     * @param denominator The denominator of the time signature, which sets the length of a beat
     * @return True while in the first part of a beat
     */
    static boolean isBeatFlash (final double position, final int denominator)
    {
        final double beat = position * Math.max (1, denominator) / 4.0;
        return beat - Math.floor (beat) < BEAT_FLASH_LENGTH;
    }


    /**
     * Flash the keys which are not held to full brightness at the start of each beat while playing.
     */
    private void updateBeatFlash ()
    {
        final ITransport transport = this.model.getTransport ();
        final boolean isFlash = this.surface.getConfiguration ().isBeatFlash () && transport.isPlaying () && isBeatFlash (transport.getPlayPosition (), transport.getDenominator ());
        if (((Kontrol1ColorManager) this.model.getColorManager ()).setBeatFlash (isFlash))
            this.surface.resendKeyLEDs ();
    }


    /**
     * Light the keys in the color of their zone in the NI template which the keyboard plays; held
     * keys are lit brighter. Keys which are not part of a zone are off.
     */
    @Override
    public void drawGrid ()
    {
        this.updateBeatFlash ();

        final ILightGuide lightGuide = this.surface.getLightGuide ();
        final NiTemplateKeyboard templateKeyboard = this.surface.getNiTemplateKeyboard ();
        final int firstNote = this.surface.getFirstNote ();
        final int numKeys = this.surface.getNumKeys ();
        for (int keyIndex = 0; keyIndex < numKeys; keyIndex++)
        {
            // The light guide addresses a key by the note of the left-most key plus its index
            final int note = firstNote + keyIndex;
            if (note < 0 || note > 127)
                continue;
            final NiZone zone = templateKeyboard.getZoneForKeyIndex (keyIndex);
            if (zone == null)
                lightGuide.light (note, Scales.SCALE_COLOR_OFF);
            else
                lightGuide.light (note, Kontrol1ColorManager.getZoneColorID (zone.color (), templateKeyboard.isKeyHeld (keyIndex)));
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        // Intentionally empty
    }
}
