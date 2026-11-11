// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


class NoteInputsTest
{
    @Test
    void mainInputReceivesAllChannels ()
    {
        assertArrayEquals (new String []
        {
            "8?????",
            "9?????",
            "A?????",
            "B?????",
            "C?????",
            "D?????",
            "E?????"
        }, NoteInputs.getMidiFilters (0));
    }


    @Test
    void channelInputsReceiveTheirChannel ()
    {
        assertEquals ("90????", NoteInputs.getMidiFilters (1)[1]);
        assertEquals ("BA????", NoteInputs.getMidiFilters (11)[3]);
        assertEquals ("EF????", NoteInputs.getMidiFilters (16)[6]);
        assertEquals ("KK1 Ch 16", NoteInputs.getChannelInputName (16));
    }
}
