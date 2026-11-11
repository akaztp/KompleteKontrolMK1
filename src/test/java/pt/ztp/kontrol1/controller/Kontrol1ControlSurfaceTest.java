// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.mossgrabers.framework.controller.valuechanger.TwosComplementValueChanger;


class Kontrol1ControlSurfaceTest
{
    private final TwosComplementValueChanger valueChanger = new TwosComplementValueChanger (1024, 10);


    @Test
    void knobsMoveAsFarDownAsUp ()
    {
        for (int change = 1; change <= 63; change++)
        {
            assertEquals (change, this.decode (change));
            assertEquals (-change, this.decode (-change));
        }
    }


    @Test
    void fastTurnsKeepTheirDirection ()
    {
        assertEquals (63, this.decode (125));
        assertEquals (-63, this.decode (-125));
    }


    @Test
    void noTurnIsNoChange ()
    {
        assertEquals (0, Kontrol1ControlSurface.toRelativeValue (0));
    }


    @Test
    void slowSensitivityMakesStepsSmaller ()
    {
        final int oneStepUp = Kontrol1ControlSurface.toRelativeValue (1);
        final double normal = this.valueChanger.calcKnobChange (oneStepUp, 0);
        // Knob Sensitivity Slow defaults to -90 (see Kontrol1Configuration)
        assertEquals (normal / 10, this.valueChanger.calcKnobChange (oneStepUp, -90), 1e-9);
    }


    private int decode (final int change)
    {
        return this.valueChanger.decode (Kontrol1ControlSurface.toRelativeValue (change));
    }
}
