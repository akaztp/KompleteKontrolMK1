// Written by akaztp (2026) for the KompleteKontrolMK1 project - see README.md
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package pt.ztp.kontrol1.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Reads the templates of the keyboard from the file in which NI's Controller Editor keeps them.
 *
 * The file is XML. Its root contains the index of the selected template (current_index) and one
 * midi-map element per template, with the key zones of the template. Controller Editor writes the
 * file when its configuration changes, but not when the template is switched on the keyboard, so
 * the selected template in the file is only correct right after Controller Editor was used.
 *
 * @author akaztp
 */
public final class NiTemplateFile
{
    /** The file of the first Komplete Kontrol S-series MK1 keyboard. */
    private static final String FILE_NAME     = "KompleteKontrol.ncs";
    private static final String TEMPLATE_TYPE = "KompleteKontrol";


    /**
     * The content of the file.
     *
     * @param templates The templates of the keyboard, in the order of Controller Editor's list
     * @param selectedIndex The index of the template which was selected when the file was written
     */
    public record Content (List<NiTemplate> templates, int selectedIndex)
    {
        /**
         * Constructor.
         *
         * @param templates The templates
         * @param selectedIndex The index of the selected template
         */
        public Content
        {
            templates = List.copyOf (templates);
        }
    }


    /**
     * Private due to utility class.
     */
    private NiTemplateFile ()
    {
        // Intentionally empty
    }


    /**
     * Get the path of the file.
     *
     * @return The path
     */
    public static Path getDefaultPath ()
    {
        final String os = System.getProperty ("os.name", "").toLowerCase (Locale.US);
        if (os.contains ("win"))
        {
            final String programData = System.getenv ("ProgramData");
            final String folder = programData == null || programData.isBlank () ? "C:\\ProgramData" : programData;
            return Path.of (folder, "Native Instruments", "Controller Editor", FILE_NAME);
        }
        // The macOS location is not verified
        return Path.of ("/Library/Application Support/Native Instruments/Controller Editor", FILE_NAME);
    }


    /**
     * Read the file.
     *
     * @param path The path of the file
     * @return The content
     * @throws IOException The file could not be read or is not a template file
     */
    public static Content read (final Path path) throws IOException
    {
        try (final InputStream in = Files.newInputStream (path))
        {
            return parse (in);
        }
    }


    /**
     * Parse the content of the file.
     *
     * @param in The content
     * @return The content
     * @throws IOException The content could not be read or is not a template file
     */
    public static Content parse (final InputStream in) throws IOException
    {
        final Element root;
        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
            // The file needs no document type, entities or external resources
            factory.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature ("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware (false);
            factory.setExpandEntityReferences (false);
            final DocumentBuilder builder = factory.newDocumentBuilder ();
            // Report errors only with the exception, not on the console
            builder.setErrorHandler (new DefaultHandler ());
            root = builder.parse (in).getDocumentElement ();
        }
        catch (final ParserConfigurationException | SAXException ex)
        {
            throw new IOException ("Not a template file of NI's Controller Editor: " + ex.getMessage (), ex);
        }

        final List<NiTemplate> templates = new ArrayList<> ();
        for (final Element midiMap: getChildren (root, "midi-map"))
        {
            if (TEMPLATE_TYPE.equals (midiMap.getAttribute ("type")))
                templates.add (parseTemplate (midiMap, templates.size ()));
        }

        final int selectedIndex = templates.isEmpty () ? 0 : Math.clamp (getInt (root, "current_index", 0), 0, templates.size () - 1);
        return new Content (templates, selectedIndex);
    }


    private static NiTemplate parseTemplate (final Element midiMap, final int index)
    {
        final List<NiZone> zones = new ArrayList<> ();
        for (final Element keyZones: getChildren (midiMap, "KeyZones"))
        {
            for (final Element keyZone: getChildren (keyZones, "KeyZone"))
            {
                // The file counts the MIDI channels from 0
                final int channel = Math.clamp (getInt (keyZone, "channel", 0), 0, 15) + 1;
                final int lowKey = Math.clamp (getInt (keyZone, "lowerKey", 0), 0, 127);
                final int highKey = Math.clamp (getInt (keyZone, "upperKey", 127), 0, 127);
                final int transpose = getInt (keyZone, "transpose", 0);
                final int color = NiColors.toKnownIndex (getInt (keyZone, "color-index", NiColors.DEFAULT_INDEX));
                zones.add (new NiZone (getText (keyZone, "label", ""), channel, lowKey, highKey, transpose, color));
            }
        }

        final String name = midiMap.getAttribute ("name").trim ();
        return new NiTemplate (name.isEmpty () ? "Template " + (index + 1) : name, zones);
    }


    /**
     * Get the child elements with a name. Only direct children, since e.g. current_index exists
     * on several levels.
     *
     * @param parent The parent element
     * @param name The name of the children
     * @return The children
     */
    private static List<Element> getChildren (final Element parent, final String name)
    {
        final List<Element> children = new ArrayList<> ();
        for (Node node = parent.getFirstChild (); node != null; node = node.getNextSibling ())
        {
            if (node instanceof final Element element && name.equals (element.getTagName ()))
                children.add (element);
        }
        return children;
    }


    private static String getText (final Element parent, final String name, final String defaultValue)
    {
        final List<Element> children = getChildren (parent, name);
        return children.isEmpty () ? defaultValue : children.get (0).getTextContent ().trim ();
    }


    private static int getInt (final Element parent, final String name, final int defaultValue)
    {
        try
        {
            return Integer.parseInt (getText (parent, name, Integer.toString (defaultValue)));
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }
}
