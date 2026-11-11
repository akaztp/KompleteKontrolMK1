// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

import java.util.List;

import de.mossgrabers.framework.scale.Scales;


/**
 * A template of the keyboard in NI's Controller Editor, with its key zones.
 *
 * @param name The name of the template
 * @param zones The key zones
 *
 * @author akaztp
 */
public record NiTemplate (String name, List<NiZone> zones)
{
    /**
     * Constructor.
     *
     * @param name The name of the template
     * @param zones The key zones
     */
    public NiTemplate
    {
        zones = List.copyOf (zones);
    }


    /**
     * Get the zone of a key. If zones overlap, the first one wins.
     *
     * @param keyNote The key, as the note it plays when the keyboard is not octave shifted
     * @return The zone or null if the key is not part of a zone
     */
    public NiZone getZoneForKey (final int keyNote)
    {
        for (final NiZone zone: this.zones)
        {
            if (zone.contains (keyNote))
                return zone;
        }
        return null;
    }


    /**
     * Describe the template for diagnostics.
     *
     * @return The description, e.g. "Split: Left Ch 2 A-1..E2 +24 Turquoise, Right Ch 3 F2..G8 -24
     *         Orange"
     */
    public String describe ()
    {
        final StringBuilder sb = new StringBuilder (this.name).append (':');
        if (this.zones.isEmpty ())
            sb.append (" no key zones");
        for (int i = 0; i < this.zones.size (); i++)
        {
            final NiZone zone = this.zones.get (i);
            sb.append (i == 0 ? " " : ", ");
            if (!zone.label ().isBlank ())
                sb.append (zone.label ().trim ()).append (' ');
            sb.append ("Ch ").append (zone.channel ()).append (' ');
            sb.append (Scales.formatDrumNote (Math.min (zone.lowKey (), zone.highKey ()))).append ("..").append (Scales.formatDrumNote (Math.max (zone.lowKey (), zone.highKey ())));
            if (zone.transpose () != 0)
                sb.append (zone.transpose () > 0 ? " +" : " ").append (zone.transpose ());
            sb.append (' ').append (NiColors.getName (zone.color ()));
        }
        return sb.toString ();
    }
}
