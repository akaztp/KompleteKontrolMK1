// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;


class NiTemplateKeyboardTest
{
    /** The left-most key of an S88 plays A-1 (21) when it is not octave shifted. */
    private static final int        FIRST_NOTE = 21;

    private static final NiZone     LEFT       = new NiZone ("Left", 2, 0, 52, 24, 0);
    private static final NiZone     RIGHT      = new NiZone ("Right", 3, 53, 127, -24, 1);
    private static final NiTemplate SPLIT      = new NiTemplate ("Split", List.of (LEFT, RIGHT));


    @Test
    void keysBelongToTheirZones ()
    {
        final NiTemplateKeyboard keyboard = createKeyboard ();
        assertEquals (LEFT, keyboard.getZoneForKeyIndex (keyIndex (52)));
        assertEquals (RIGHT, keyboard.getZoneForKeyIndex (keyIndex (53)));
    }


    @Test
    void noteIsTracedBackToItsKey ()
    {
        final NiTemplateKeyboard keyboard = createKeyboard ();

        // Key 52 sends 76 on channel 2, key 53 sends 29 on channel 3
        keyboard.noteOn (2, 76, 100);
        assertTrue (keyboard.isKeyHeld (keyIndex (52)));
        keyboard.noteOn (3, 29, 100);
        assertTrue (keyboard.isKeyHeld (keyIndex (53)));

        keyboard.noteOff (2, 76);
        assertFalse (keyboard.isKeyHeld (keyIndex (52)));
        // Note-on with velocity 0 is a note-off
        keyboard.noteOn (3, 29, 0);
        assertFalse (keyboard.isKeyHeld (keyIndex (53)));
    }


    @Test
    void noteOnAnotherChannelLightsNoKey ()
    {
        final NiTemplateKeyboard keyboard = createKeyboard ();
        keyboard.noteOn (1, 76, 100);
        for (int i = 0; i < 88; i++)
            assertFalse (keyboard.isKeyHeld (i));
    }


    @Test
    void zonesMoveAlongTheKeysWhenOctaveShifted ()
    {
        final NiTemplateKeyboard keyboard = createKeyboard ();
        keyboard.setFirstNote (FIRST_NOTE + 12);
        assertEquals (12, keyboard.getOctaveShift ());

        // The split is now an octave lower on the keys: key 40 plays 52, key 41 plays 53
        assertEquals (LEFT, keyboard.getZoneForKeyIndex (keyIndex (40)));
        assertEquals (RIGHT, keyboard.getZoneForKeyIndex (keyIndex (41)));
        assertEquals (RIGHT, keyboard.getZoneForKeyIndex (keyIndex (52)));

        // Key 40 plays 52, which the left zone sends as 76 on channel 2
        keyboard.noteOn (2, 76, 100);
        assertTrue (keyboard.isKeyHeld (keyIndex (40)));
        assertFalse (keyboard.isKeyHeld (keyIndex (52)));
    }


    @Test
    void unknownTemplateLightsNothing ()
    {
        final NiTemplateKeyboard keyboard = new NiTemplateKeyboard (FIRST_NOTE);
        keyboard.noteOn (2, 76, 100);
        assertNull (keyboard.getZoneForKeyIndex (0));
        assertFalse (keyboard.isKeyHeld (keyIndex (52)));
    }


    private static NiTemplateKeyboard createKeyboard ()
    {
        final NiTemplateKeyboard keyboard = new NiTemplateKeyboard (FIRST_NOTE);
        keyboard.setTemplate (SPLIT);
        return keyboard;
    }


    private static int keyIndex (final int keyNote)
    {
        return keyNote - FIRST_NOTE;
    }
}
