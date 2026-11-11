// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


class Kontrol1DisplayTest
{
    @Test
    void namesWhichFitKeepTheirSpaces ()
    {
        assertEquals ("BASS 2", Kontrol1Display.formatName ("Bass 2", 8));
        assertEquals ("PIANO", Kontrol1Display.formatName ("Piano", 8));
    }


    @Test
    void longNamesLoseTheirSpacesAndAreCutOff ()
    {
        assertEquals ("LEADSYNT", Kontrol1Display.formatName ("Lead Synth", 8));
        assertEquals ("DRUMSGRO", Kontrol1Display.formatName ("Drums  Group", 8));
        assertEquals ("LEADSYN", Kontrol1Display.formatName ("Lead Synth", 7));
        assertEquals ("STRINGS", Kontrol1Display.formatName ("Strings 1", 7));
    }


    @Test
    void namesAreWrittenInAscii ()
    {
        assertEquals ("BAESSELI", Kontrol1Display.formatName ("Bässe links", 8));
        assertEquals ("", Kontrol1Display.formatName (null, 8));
    }
}
