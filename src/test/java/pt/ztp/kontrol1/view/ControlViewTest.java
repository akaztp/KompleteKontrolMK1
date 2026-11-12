// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


class ControlViewTest
{
    @Test
    void keysFlashAtTheStartOfEachQuarterNoteBeat ()
    {
        assertTrue (ControlView.isBeatFlash (0.0, 4));
        assertTrue (ControlView.isBeatFlash (3.2, 4));
        assertFalse (ControlView.isBeatFlash (3.3, 4));
        assertFalse (ControlView.isBeatFlash (3.9, 4));
    }


    @Test
    void beatsFollowTheTimeSignature ()
    {
        // In x/8, a beat is an eighth note, half a quarter note
        assertTrue (ControlView.isBeatFlash (0.5, 8));
        assertFalse (ControlView.isBeatFlash (0.25, 8));
        // In x/2, a beat is a half note
        assertFalse (ControlView.isBeatFlash (1.0, 2));
        assertTrue (ControlView.isBeatFlash (2.4, 2));
    }


    @Test
    void preRollFlashesToo ()
    {
        assertTrue (ControlView.isBeatFlash (-1.0, 4));
        assertFalse (ControlView.isBeatFlash (-0.5, 4));
    }
}
