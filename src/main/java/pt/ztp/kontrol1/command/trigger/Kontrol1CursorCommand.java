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
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.Modes;


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
        this.selectItemPage (false);
    }


    /** {@inheritDoc} */
    @Override
    protected void scrollDown ()
    {
        this.selectItemPage (true);
    }


    /**
     * Select the next or previous item page of the active mode. Up selects the next one, except in
     * the Parameters mode, where Down selects the next device (or device page with Shift).
     *
     * @param isDown True if Down was pressed, false for Up
     */
    private void selectItemPage (final boolean isDown)
    {
        final ModeManager modeManager = this.surface.getModeManager ();
        final IMode mode = modeManager.getActive ();
        if (mode == null)
            return;

        final boolean isNext = modeManager.isActive (Modes.DEVICE_PARAMS) ? isDown : !isDown;
        if (isNext)
            mode.selectNextItemPage ();
        else
            mode.selectPreviousItemPage ();
    }
}
