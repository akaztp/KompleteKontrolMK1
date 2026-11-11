// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.command.trigger;

import pt.ztp.kontrol1.Kontrol1Configuration;
import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import de.mossgrabers.framework.command.trigger.Direction;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.IMode;


/**
 * Command for cursor arrow keys.
 *
 * @author Jürgen Moßgraber
 */
public class Kontrol1CursorCommand extends de.mossgrabers.framework.command.trigger.mode.CursorCommand<Kontrol1ControlSurface, Kontrol1Configuration>
{
    /**
     * Constructor.
     *
     * @param direction The direction of the kontrol1ed cursor arrow
     * @param model The model
     * @param surface The surface
     */
    public Kontrol1CursorCommand (final Direction direction, final IModel model, final Kontrol1ControlSurface surface)
    {
        super (direction, model, surface, false);
    }


    /** {@inheritDoc} */
    @Override
    protected void scrollUp ()
    {
        final IMode mode = this.surface.getModeManager ().getActive ();
        if (mode != null)
            mode.selectNextItemPage ();
    }


    /** {@inheritDoc} */
    @Override
    protected void scrollDown ()
    {
        final IMode mode = this.surface.getModeManager ().getActive ();
        if (mode != null)
            mode.selectPreviousItemPage ();
    }
}
