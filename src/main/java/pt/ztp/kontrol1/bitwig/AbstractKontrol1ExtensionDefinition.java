// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.bitwig;

import de.mossgrabers.bitwig.framework.BitwigSetupFactory;
import de.mossgrabers.bitwig.framework.configuration.SettingsUIImpl;
import de.mossgrabers.bitwig.framework.daw.HostImpl;
import de.mossgrabers.bitwig.framework.extension.AbstractControllerExtensionDefinition;
import pt.ztp.kontrol1.Kontrol1Configuration;
import pt.ztp.kontrol1.Kontrol1ControllerDefinition;
import pt.ztp.kontrol1.Kontrol1ControllerSetup;
import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import de.mossgrabers.framework.controller.IControllerSetup;

import com.bitwig.extension.controller.api.ControllerHost;


/**
 * Definition class for Native Instruments Komplete Kontrol 1 S-series controllers.
 *
 * @author Jürgen Moßgraber
 */
public abstract class AbstractKontrol1ExtensionDefinition extends AbstractControllerExtensionDefinition<Kontrol1ControlSurface, Kontrol1Configuration>
{
    private final int modelIndex;


    /**
     * Constructor.
     *
     * @param modelIndex The index of the specific model
     */
    protected AbstractKontrol1ExtensionDefinition (final int modelIndex)
    {
        super (new Kontrol1ControllerDefinition (modelIndex));
        this.modelIndex = modelIndex;
    }


    /** {@inheritDoc} */
    @Override
    public String getHelpFilePath ()
    {
        return "https://github.com/akaztp/KompleteKontrolMK1#readme";
    }


    /** {@inheritDoc} */
    @Override
    public boolean isUsingBetaAPI ()
    {
        return true;
    }


    /** {@inheritDoc} */
    @Override
    protected IControllerSetup<Kontrol1ControlSurface, Kontrol1Configuration> getControllerSetup (final ControllerHost host)
    {
        return new Kontrol1ControllerSetup (this.modelIndex, new HostImpl (host), new BitwigSetupFactory (host), new SettingsUIImpl (host, host.getPreferences ()), new SettingsUIImpl (host, host.getDocumentState ()));
    }
}
