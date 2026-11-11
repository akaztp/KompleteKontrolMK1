// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.LightInfo;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.AbstractPlayView;
import pt.ztp.kontrol1.template.NiColors;


class Kontrol1ColorManagerTest
{
    private final Kontrol1ColorManager colorManager = new Kontrol1ColorManager ();


    @Test
    void zoneColorsAreValidLightColors ()
    {
        for (int i = 0; i < NiColors.NUM_COLORS; i++)
        {
            for (final boolean isBright: new boolean []
            {
                true,
                false
            })
            {
                final int index = this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (i, isBright));
                // The light guide rejects (throws on) any index outside of 0..127
                assertDoesNotThrow (() -> new LightInfo (index, -1, false), "color " + i + " bright " + isBright);
            }
        }
    }


    @Test
    void zoneColorsMapBackToTheirRgbValues ()
    {
        for (int i = 0; i < NiColors.NUM_COLORS; i++)
        {
            final int bright = this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (i, true));
            final int dim = this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (i, false));
            assertEquals (NiColors.getColor (i), this.colorManager.getColor (bright, null));
            assertEquals (NiColors.getColor (i).dim (Kontrol1ColorManager.DEFAULT_ZONE_DIM_FACTOR), this.colorManager.getColor (dim, null));
        }
    }


    @Test
    void unheldKeysFollowTheBrightness ()
    {
        final int bright = this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (1, true));
        final int dim = this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (1, false));

        this.colorManager.setZoneDimFactor (1.0);
        assertEquals (this.colorManager.getColor (bright, null), this.colorManager.getColor (dim, null));
        this.colorManager.setZoneDimFactor (0.5);
        assertEquals (NiColors.getColor (1).dim (0.5), this.colorManager.getColor (dim, null));
        this.colorManager.setZoneDimFactor (0.0);
        assertArrayEquals (new int [3], this.colorManager.getColor (dim, null).toIntRGB127 ());
        assertEquals (NiColors.getColor (1), this.colorManager.getColor (bright, null));
    }


    @Test
    void zoneColorsDoNotShareIndicesWithOtherColors ()
    {
        final Set<Integer> zoneIndices = new HashSet<> ();
        for (int i = 0; i < NiColors.NUM_COLORS; i++)
        {
            assertTrue (zoneIndices.add (Integer.valueOf (this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (i, true)))));
            assertTrue (zoneIndices.add (Integer.valueOf (this.colorManager.getColorIndex (Kontrol1ColorManager.getZoneColorID (i, false)))));
        }

        final Set<String> otherIDs = new HashSet<> (Set.of (ColorManager.BUTTON_STATE_OFF, ColorManager.BUTTON_STATE_ON, Scales.SCALE_COLOR_OFF, Scales.SCALE_COLOR_OCTAVE, Scales.SCALE_COLOR_NOTE, Scales.SCALE_COLOR_OUT_OF_SCALE, AbstractPlayView.COLOR_PLAY, AbstractPlayView.COLOR_RECORD));
        for (final DAWColor color: DAWColor.values ())
            otherIDs.add (color.name ());
        for (final String id: otherIDs)
            assertFalse (zoneIndices.contains (Integer.valueOf (this.colorManager.getColorIndex (id))), id);
    }
}
