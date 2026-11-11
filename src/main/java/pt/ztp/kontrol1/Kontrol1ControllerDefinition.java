// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1;

import de.mossgrabers.framework.controller.DefaultControllerDefinition;
import de.mossgrabers.framework.usb.UsbMatcher;
import de.mossgrabers.framework.utils.OperatingSystem;
import de.mossgrabers.framework.utils.Pair;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


/**
 * Definition class for the Native Instruments Kontrol MkI controller extension.
 *
 * @author Jürgen Moßgraber
 * @author akaztp
 */
public class Kontrol1ControllerDefinition extends DefaultControllerDefinition
{
    /** The name of this project, appended to the hardware model names. */
    public static final String     PRODUCT_NAME       = "KK1 Splits";

    /** The author shown in Bitwig. */
    public static final String     AUTHOR             = "akaztp, based on DrivenByMoss by J. Moßgraber";

    // New IDs, so that this extension can be installed next to DrivenByMoss
    private static final UUID []   EXTENSION_ID       =
    {
        UUID.fromString ("72e918f3-9f0e-44ed-a1f8-cec9c795fc52"),
        UUID.fromString ("3ca43fb2-7cd5-47d1-8552-5733d9cffc6e"),
        UUID.fromString ("2c04ff9a-b46f-49b9-98b4-4fc13ee287a2"),
        UUID.fromString ("16cc3f73-845b-484c-8ed3-28f9af4236bb")
    };

    private static final String [] HARDWARE_MODEL     =
    {
        "Komplete Kontrol S25 mk I (" + PRODUCT_NAME + ")",
        "Komplete Kontrol S49 mk I (" + PRODUCT_NAME + ")",
        "Komplete Kontrol S61 mk I (" + PRODUCT_NAME + ")",
        "Komplete Kontrol S88 mk I (" + PRODUCT_NAME + ")"
    };

    private static final int []    NUM_KEYS           =
    {
        25,
        49,
        61,
        88
    };

    /**
     * The note of the left-most key when the keyboard is not octave shifted: C2, C1, C1 and A-1
     * (with C3 = MIDI note 60, the convention used by Bitwig and NI).
     */
    private static final int []    DEFAULT_FIRST_NOTE =
    {
        48,
        36,
        36,
        21
    };

    private static final short     VENDOR_ID          = 0x17cc;
    private static final short []  PRODUCT_ID         =
    {
        0x1340,
        0x1350,
        0x1360,
        0x1410
    };

    private final int              modelIndex;


    /**
     * Constructor.
     *
     * @param modelIndex The index of the specific model (S25, S49, S61, S88)
     */
    public Kontrol1ControllerDefinition (final int modelIndex)
    {
        super (EXTENSION_ID[modelIndex], HARDWARE_MODEL[modelIndex], "Native Instruments", 1, 0);
        this.modelIndex = modelIndex;
    }


    /**
     * Get the number of keys of a model.
     *
     * @param modelIndex The index of the specific model (S25, S49, S61, S88)
     * @return The number of keys
     */
    public static int getNumKeys (final int modelIndex)
    {
        return NUM_KEYS[modelIndex];
    }


    /**
     * Get the note of the left-most key of a model when the keyboard is not octave shifted.
     *
     * @param modelIndex The index of the specific model (S25, S49, S61, S88)
     * @return The MIDI note
     */
    public static int getDefaultFirstNote (final int modelIndex)
    {
        return DEFAULT_FIRST_NOTE[modelIndex];
    }


    /** {@inheritDoc} */
    @Override
    public String getAuthor ()
    {
        return AUTHOR;
    }


    /** {@inheritDoc} */
    @Override
    public String getVersion (final Package pckg)
    {
        // Read the version from this project's JAR manifest
        final String version = Kontrol1ControllerDefinition.class.getPackage ().getImplementationVersion ();
        return version == null ? "1.0" : version;
    }


    /** {@inheritDoc} */
    @Override
    public List<Pair<String [], String []>> getMidiDiscoveryPairs (final OperatingSystem os)
    {
        return Collections.singletonList (this.addDeviceDiscoveryPair ("Komplete Kontrol - 1", null));
    }


    /** {@inheritDoc} */
    @Override
    public UsbMatcher claimUSBDevice ()
    {
        return new UsbMatcher (VENDOR_ID, PRODUCT_ID[this.modelIndex]);
    }
}
