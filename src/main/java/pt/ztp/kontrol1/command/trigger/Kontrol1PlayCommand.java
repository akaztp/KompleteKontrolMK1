// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.command.trigger;

import pt.ztp.kontrol1.Kontrol1Configuration;
import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import de.mossgrabers.framework.command.trigger.clip.NewCommand;
import de.mossgrabers.framework.command.trigger.transport.PlayCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command to handle the play button.
 *
 * @author Jürgen Moßgraber
 */
public class Kontrol1PlayCommand extends PlayCommand<Kontrol1ControlSurface, Kontrol1Configuration>
{
    private final NewCommand<Kontrol1ControlSurface, Kontrol1Configuration> newCommand;


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     */
    public Kontrol1PlayCommand (final IModel model, final Kontrol1ControlSurface surface)
    {
        super (model, surface);
        this.newCommand = new NewCommand<> (model, surface);
    }


    /** {@inheritDoc} */
    @Override
    public void executeShifted (final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN)
            this.newCommand.handleExecute (true);
    }
}
