// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.bitwig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bitwig.extension.ExtensionDefinition;
import com.bitwig.extension.controller.ControllerExtensionDefinition;


class ExtensionDefinitionTest
{
    @Test
    void allModelsAreRegisteredWithUniqueIdsAndNames ()
    {
        final Set<UUID> ids = new HashSet<> ();
        final Set<String> names = new HashSet<> ();
        for (final ExtensionDefinition extension: ServiceLoader.load (ExtensionDefinition.class))
        {
            final ControllerExtensionDefinition definition = (ControllerExtensionDefinition) extension;
            ids.add (definition.getId ());
            names.add (definition.getName ());
            assertTrue (definition.getName ().endsWith ("(KK1)"), definition.getName ());
            assertEquals ("Native Instruments", definition.getHardwareVendor ());
            assertTrue (definition.getAuthor ().contains ("Moßgraber"), "credits DrivenByMoss");
        }
        assertEquals (4, ids.size ());
        assertEquals (4, names.size ());

        // Must differ from the DrivenByMoss Kontrol MkI extension IDs
        assertTrue (!ids.contains (UUID.fromString ("99ff3646-3a65-47e5-a0e2-58c1c1799e93")));
    }
}
