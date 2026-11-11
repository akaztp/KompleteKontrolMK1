// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.command.trigger;

import pt.ztp.kontrol1.Kontrol1Configuration;
import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * The keyboard itself switches its NI templates with Preset Up/Down. This command follows it, so
 * the extension knows which template the keyboard plays.
 *
 * @author akaztp
 */
public class NiTemplateCommand extends AbstractTriggerCommand<Kontrol1ControlSurface, Kontrol1Configuration>
{
    private final int direction;


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     * @param direction 1 for the next template (Preset Down), -1 for the previous one (Preset Up)
     */
    public NiTemplateCommand (final IModel model, final Kontrol1ControlSurface surface, final int direction)
    {
        super (model, surface);

        this.direction = direction;
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final ButtonEvent event, final int velocity)
    {
        if (event == ButtonEvent.DOWN)
            this.surface.getConfiguration ().stepActiveTemplate (this.direction);
    }
}
