// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.mode.track;

import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import pt.ztp.kontrol1.controller.Kontrol1Display;
import pt.ztp.kontrol1.mode.AbstractKontrol1Mode;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ISend;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISendBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.parameterprovider.track.SelectedTrackParameterProvider;

import java.util.Optional;


/**
 * Edit track parameters mode.
 *
 * @author Jürgen Moßgraber
 */
public class TrackMode extends AbstractKontrol1Mode<ITrack>
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public TrackMode (final Kontrol1ControlSurface surface, final IModel model)
    {
        super ("TRACK", surface, model, model.getCurrentTrackBank ());

        model.addTrackBankObserver (this::switchBanks);

        this.setParameterProvider (new SelectedTrackParameterProvider (model));
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay ()
    {
        final ITrackBank currentTrackBank = this.model.getCurrentTrackBank ();
        final Optional<ITrack> t = currentTrackBank.getSelectedItem ();
        final Kontrol1Display d = (Kontrol1Display) this.surface.getDisplay ();

        d.clear ();

        if (t.isEmpty ())
        {
            d.setCell (0, 3, "  PLEASE").setCell (0, 4, "SELECT A").setCell (0, 5, "TRACK").allDone ();
            return;
        }

        final boolean isEffectTrackBankActive = this.model.isEffectTrackBankActive ();

        final ITrack track = t.get ();
        d.setCell (0, 0, (isEffectTrackBankActive ? "TR-FX " : "TRACK ") + (track.getPosition () + 1)).setCell (1, 0, Kontrol1Display.formatName (track.getName (), 8));

        d.setCell (0, 1, "VOLUME").setCell (1, 1, getSecondLineText (track)).setCell (0, 2, "PAN").setCell (1, 2, track.getPanStr (8));
        d.setBar (1, this.surface.getContinuous (ContinuousID.KNOB1).isTouched (), track.getVolume ());
        d.setPanBar (2, this.surface.getContinuous (ContinuousID.KNOB2).isTouched (), track.getPan ());

        final ISendBank sendBank = track.getSendBank ();
        for (int i = 0; i < 6; i++)
        {
            final int pos = 3 + i;
            final ISend sendData = sendBank.getItem (i);
            // Sends are named after their effect tracks
            d.setCell (0, pos, Kontrol1Display.formatName (sendData.getName (), 8)).setCell (1, pos, sendData.getDisplayedValue (8));
            d.setBar (pos, this.surface.getContinuous (ContinuousID.get (ContinuousID.KNOB3, i)).isTouched () && sendData.doesExist (), sendData.getValue ());
        }
        d.allDone ();
    }
}
