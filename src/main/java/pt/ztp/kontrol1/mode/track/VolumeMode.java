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
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.parameterprovider.IParameterProvider;
import de.mossgrabers.framework.parameterprovider.track.PanParameterProvider;
import de.mossgrabers.framework.parameterprovider.track.VolumeParameterProvider;
import de.mossgrabers.framework.utils.StringUtils;

import java.util.Locale;
import java.util.Optional;


/**
 * Volume mode. While Shift is held, the knobs change the panning of the tracks instead.
 *
 * @author Jürgen Moßgraber
 */
public class VolumeMode extends AbstractKontrol1Mode<ITrack>
{
    private final IParameterProvider panParameterProvider;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public VolumeMode (final Kontrol1ControlSurface surface, final IModel model)
    {
        super ("Volume / Pan", surface, model, model.getCurrentTrackBank ());

        model.addTrackBankObserver (this::switchBanks);

        this.setParameterProvider (new VolumeParameterProvider (model));
        this.panParameterProvider = new PanParameterProvider (model);
    }


    /** {@inheritDoc} */
    @Override
    public IParameterProvider getParameterProvider ()
    {
        // The Shift button is created after the modes, so it can't be registered with
        // setParameterProvider (ButtonID, ...)
        return this.surface.isShiftPressed () ? this.panParameterProvider : super.getParameterProvider ();
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay ()
    {
        final Kontrol1Display d = (Kontrol1Display) this.surface.getDisplay ();
        final boolean isPan = this.surface.isShiftPressed ();
        final boolean isEffect = this.model.isEffectTrackBankActive ();

        d.clear ();
        final String title = isPan ? isEffect ? "PAN-FX" : "PAN" : isEffect ? "VOL-FX" : "VOLUME";
        d.setCell (0, 0, title).setCell (1, 0, this.formatPageRange ("%d - %d"));

        final ITrackBank tb = this.model.getCurrentTrackBank ();
        final Optional<ITrack> selTrack = tb.getSelectedItem ();

        final int selIndex = selTrack.isEmpty () ? -1 : selTrack.get ().getIndex ();
        for (int i = 0; i < 8; i++)
        {
            final boolean isSel = i == selIndex;
            final ITrack t = tb.getItem (i);
            final String n = StringUtils.shortenAndFixASCII (t.getName (), isSel ? 7 : 8).toUpperCase (Locale.US);
            d.setCell (0, 1 + i, isSel ? ">" + n : n);

            final boolean isTouched = this.surface.getContinuous (ContinuousID.get (ContinuousID.KNOB1, i)).isTouched () && t.doesExist ();
            if (isPan)
            {
                d.setCell (1, 1 + i, t.getPanStr (8));
                if (t.doesExist ())
                    d.setPanBar (1 + i, isTouched, t.getPan ());
                else
                    d.setBar (1 + i, false, 0);
            }
            else
            {
                d.setCell (1, 1 + i, getSecondLineText (t));
                d.setBar (1 + i, isTouched, t.getVolume ());
            }
        }
        d.allDone ();
    }
}
