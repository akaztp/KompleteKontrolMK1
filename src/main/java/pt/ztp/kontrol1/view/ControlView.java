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
     * Light the keys in the color of their zone in the NI template which the keyboard plays; held
     * keys are lit brighter. Keys which are not part of a zone are off.
     */
    @Override
    public void drawGrid ()
    {
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
