// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import de.mossgrabers.framework.controller.color.ColorEx;


class NiTemplateFileTest
{
    // Shortened from a file which Controller Editor 2.8.2 wrote for an S88
    private static final String FILE = """
        <?xml version="1.0"?>
        <ni-controller-midi-map version="1">
          <wrapmode>0</wrapmode>
          <current_index>1</current_index>
          <midi-map type="KompleteKontrol" name="CH_1" port="internal">
            <footswitchContinuous>0</footswitchContinuous>
            <KnobsPages>
              <current_index>0</current_index>
            </KnobsPages>
            <KeyZones>
              <KeyZone id="Key Zone 1">
                <lowerKey>0</lowerKey>
                <upperKey>127</upperKey>
                <velocityCurve>3</velocityCurve>
                <transpose>0</transpose>
                <color-index>1</color-index>
                <channel>0</channel>
                <ledMode>3</ledMode>
              </KeyZone>
            </KeyZones>
          </midi-map>
          <midi-map type="KompleteKontrol" name="CH_2/3" port="internal">
            <KnobsPages>
              <current_index>3</current_index>
            </KnobsPages>
            <KeyZones>
              <KeyZone id="Key Zone 1">
                <label>Left</label>
                <lowerKey>0</lowerKey>
                <upperKey>52</upperKey>
                <transpose>24</transpose>
                <color-index>9</color-index>
                <channel>1</channel>
              </KeyZone>
              <KeyZone id="Key Zone 1">
                <label>Right</label>
                <lowerKey>53</lowerKey>
                <upperKey>127</upperKey>
                <transpose>-24</transpose>
                <color-index>1</color-index>
                <channel>2</channel>
              </KeyZone>
            </KeyZones>
          </midi-map>
          <midi-map type="Maschine" name="Not a keyboard template" port="internal" />
        </ni-controller-midi-map>
        """;


    @Test
    void readsTheTemplatesAndTheSelectedOne () throws IOException
    {
        final NiTemplateFile.Content content = parse (FILE);
        assertEquals (2, content.templates ().size ());
        assertEquals ("CH_1", content.templates ().get (0).name ());
        assertEquals ("CH_2/3", content.templates ().get (1).name ());
        // The root's current_index, not the one of the knob pages
        assertEquals (1, content.selectedIndex ());
    }


    @Test
    void readsTheKeyZones () throws IOException
    {
        final NiTemplate split = parse (FILE).templates ().get (1);
        assertEquals (2, split.zones ().size ());

        // The file counts the MIDI channels from 0
        final NiZone left = split.zones ().get (0);
        assertEquals (new NiZone ("Left", 2, 0, 52, 24, 9), left);
        final NiZone right = split.zones ().get (1);
        assertEquals (new NiZone ("Right", 3, 53, 127, -24, 1), right);

        assertEquals (left, split.getZoneForKey (52));
        assertEquals (right, split.getZoneForKey (53));
        assertEquals ("CH_2/3: Left Ch 2 C-2..E2 +24 Turquoise, Right Ch 3 F2..G8 -24 Orange", split.describe ());
    }


    @Test
    void zonesWithoutLabelAreNamedByTheirChannel () throws IOException
    {
        final NiZone zone = parse (FILE).templates ().get (0).zones ().get (0);
        assertEquals ("", zone.label ());
        assertEquals ("Ch 1", zone.getDisplayName ());
    }


    @Test
    void selectedIndexIsClampedToTheTemplates () throws IOException
    {
        assertEquals (1, parse (FILE.replace ("<current_index>1</current_index>", "<current_index>7</current_index>")).selectedIndex ());
    }


    @Test
    void fileWithoutTemplatesIsEmpty () throws IOException
    {
        final NiTemplateFile.Content content = parse ("<ni-controller-midi-map version=\"1\"><current_index>2</current_index></ni-controller-midi-map>");
        assertTrue (content.templates ().isEmpty ());
        assertEquals (0, content.selectedIndex ());
    }


    @Test
    void otherContentIsRejected ()
    {
        assertThrows (IOException.class, () -> parse ("not XML"));
        assertThrows (IOException.class, () -> parse ("<!DOCTYPE x [<!ENTITY e \"e\">]><x>&e;</x>"));
    }


    @Test
    void unknownColorsAreTheDefaultColor () throws IOException
    {
        final NiZone zone = parse (FILE.replace ("<color-index>9</color-index>", "<color-index>42</color-index>")).templates ().get (1).zones ().get (0);
        assertEquals (NiColors.DEFAULT_INDEX, zone.color ());
    }


    @Test
    void niColorsHaveControllerEditorsNamesAndTheValuesTunedOnTheKeys ()
    {
        // Confirmed on an S88: 1 is orange, 9 is a blue. The values are the ones sent to the keys.
        assertEquals ("Orange", NiColors.getName (1));
        assertArrayEquals (new int [] { 127, 19, 0 }, NiColors.getColor (1).toIntRGB127 ());
        assertEquals ("Turquoise", NiColors.getName (9));
        assertArrayEquals (new int [] { 0, 127, 91 }, NiColors.getColor (9).toIntRGB127 ());
        // The last color of the picker
        assertEquals (ColorEx.BLACK, NiColors.getColor (NiColors.NUM_COLORS - 1));

        assertEquals ("Orange", NiColors.getName (99));
        assertEquals ("Orange", NiColors.getName (-1));
    }


    private static NiTemplateFile.Content parse (final String content) throws IOException
    {
        return NiTemplateFile.parse (new ByteArrayInputStream (content.getBytes (StandardCharsets.UTF_8)));
    }
}
