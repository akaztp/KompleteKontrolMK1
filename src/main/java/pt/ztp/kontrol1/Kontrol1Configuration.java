// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.IEnumSetting;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.configuration.IStringSetting;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.ArpeggiatorMode;
import pt.ztp.kontrol1.template.NiTemplate;
import pt.ztp.kontrol1.template.NiTemplateFile;


/**
 * The configuration settings for Kontrol 1.
 *
 * @author Jürgen Moßgraber
 * @author akaztp
 */
public class Kontrol1Configuration extends AbstractConfiguration
{
    /** ID for the NI template which the keyboard plays. */
    public static final Integer ACTIVE_TEMPLATE       = Integer.valueOf (NEXT_SETTING_ID);
    /** ID for the setting to log to Bitwig's console. */
    public static final Integer LOG_TO_CONSOLE        = Integer.valueOf (NEXT_SETTING_ID + 1);

    private static final String CATEGORY_KEYBOARD     = "Keyboard";
    private static final String CATEGORY_DIAGNOSTICS  = "Diagnostics";

    private List<NiTemplate>    niTemplates           = List.of ();
    private String              niTemplateFileStatus  = "";
    private String []           activeTemplateOptions = new String [0];
    private IEnumSetting        activeTemplateSetting;
    private int                 activeTemplate;
    private boolean             isLogToConsole;


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param valueChanger The value changer
     * @param arpeggiatorModes The available arpeggiator modes
     */
    public Kontrol1Configuration (final IHost host, final IValueChanger valueChanger, final List<ArpeggiatorMode> arpeggiatorModes)
    {
        super (host, valueChanger, arpeggiatorModes);
    }


    /** {@inheritDoc} */
    @Override
    public void init (final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        // ----------------------------------------------------------------
        // NI template (KompleteKontrolMK1)

        this.initTemplateSettings (globalSettings);

        // ----------------------------------------------------------------
        // Play and Sequence

        this.activateShowPlayedChordsSetting (globalSettings);

        // ----------------------------------------------------------------
        // Transport

        this.activateBehaviourOnPauseSetting (globalSettings);
        this.activateBehaviourOnStopSetting (globalSettings);
        this.activateDoubleClickPlayButton (globalSettings);
        this.activateDoubleClickStopButton (globalSettings);
        this.activateFlipRecordSetting (globalSettings);

        // ----------------------------------------------------------------
        // Workflow

        this.activateExcludeDeactivatedItemsSetting (globalSettings);
        this.activateEnableVUMetersSetting (globalSettings);
        this.activateKnobSpeedSetting (globalSettings);

        // ----------------------------------------------------------------
        // Diagnostics (KompleteKontrolMK1)

        final IEnumSetting logSetting = globalSettings.getEnumSetting ("Log to Console", CATEGORY_DIAGNOSTICS, ON_OFF_OPTIONS, ON_OFF_OPTIONS[0]);
        logSetting.addValueObserver (value -> {
            this.isLogToConsole = ON_OFF_OPTIONS[1].equals (value);
            this.notifyObservers (LOG_TO_CONSOLE);
        });
        this.isSettingActive.add (LOG_TO_CONSOLE);
    }


    /**
     * Read the templates from NI's Controller Editor, and create the setting of the template which
     * the keyboard plays. The keyboard switches its templates itself with Preset Up/Down; the
     * extension follows these presses, and the setting corrects it if it is out of step.
     *
     * Controller Editor writes its file when its configuration changes, but not when the template is
     * switched on the keyboard. So the template which the file names is taken over only if the file
     * changed since it was read last; otherwise the template which the extension followed last time
     * is kept.
     *
     * @param globalSettings The global settings
     */
    private void initTemplateSettings (final ISettingsUI globalSettings)
    {
        final Path path = NiTemplateFile.getDefaultPath ();
        final NiTemplateFile.Content content;
        final String fileTime;
        try
        {
            content = NiTemplateFile.read (path);
            fileTime = Long.toString (Files.getLastModifiedTime (path).toMillis ());
        }
        catch (final IOException ex)
        {
            this.niTemplateFileStatus = "Could not read the templates of NI's Controller Editor from " + path + ": " + ex.getMessage ();
            return;
        }
        if (content.templates ().isEmpty ())
        {
            this.niTemplateFileStatus = "No templates of the keyboard in " + path;
            return;
        }

        this.niTemplates = content.templates ();
        this.niTemplateFileStatus = "Read " + this.niTemplates.size () + " templates from " + path;

        // Numbered, since the names don't need to be unique and the order matters for Preset
        // Up/Down
        this.activeTemplateOptions = new String [this.niTemplates.size ()];
        for (int i = 0; i < this.activeTemplateOptions.length; i++)
            this.activeTemplateOptions[i] = (i + 1) + ": " + this.niTemplates.get (i).name ();
        final String selectedOption = this.activeTemplateOptions[content.selectedIndex ()];

        this.activeTemplateSetting = globalSettings.getEnumSetting ("Active Template", CATEGORY_KEYBOARD, this.activeTemplateOptions, selectedOption);
        this.activeTemplateSetting.addValueObserver (value -> {
            this.activeTemplate = Math.max (0, indexOf (this.activeTemplateOptions, value));
            this.notifyObservers (ACTIVE_TEMPLATE);
        });
        this.isSettingActive.add (ACTIVE_TEMPLATE);

        // Not shown: the time of the file when its template was taken over last
        final IStringSetting fileTimeSetting = globalSettings.getStringSetting ("Template File Time", CATEGORY_KEYBOARD, 20, "");
        fileTimeSetting.setVisible (false);
        if (!fileTime.equals (fileTimeSetting.get ()))
        {
            fileTimeSetting.set (fileTime);
            this.activeTemplateSetting.set (selectedOption);
            this.activeTemplate = content.selectedIndex ();
        }
    }


    /**
     * Get the templates of the keyboard in NI's Controller Editor.
     *
     * @return The templates, in the order of Controller Editor's list; empty if they could not be
     *         read
     */
    public List<NiTemplate> getNiTemplates ()
    {
        return this.niTemplates;
    }


    /**
     * Describe the result of reading the templates of NI's Controller Editor, for diagnostics.
     *
     * @return The description
     */
    public String getNiTemplateFileStatus ()
    {
        return this.niTemplateFileStatus;
    }


    /**
     * Get the index of the NI template which the keyboard plays.
     *
     * @return The index into {@link #getNiTemplates()}
     */
    public int getActiveTemplateIndex ()
    {
        return this.activeTemplate;
    }


    /**
     * Get the NI template which the keyboard plays.
     *
     * @return The template or null if the templates could not be read
     */
    public NiTemplate getActiveTemplate ()
    {
        return this.niTemplates.isEmpty () ? null : this.niTemplates.get (Math.clamp (this.activeTemplate, 0, this.niTemplates.size () - 1));
    }


    /**
     * Follow the keyboard to the next or previous NI template. Like the keyboard, it wraps around
     * at the ends of the list.
     *
     * @param delta 1 for the next template, -1 for the previous one
     */
    public void stepActiveTemplate (final int delta)
    {
        if (this.activeTemplateSetting != null)
            this.activeTemplateSetting.set (this.activeTemplateOptions[Math.floorMod (this.activeTemplate + delta, this.activeTemplateOptions.length)]);
    }


    /**
     * Should the state of the keyboard and the incoming MIDI messages be logged to Bitwig's control
     * script console?
     *
     * @return True to log
     */
    public boolean isLogToConsole ()
    {
        return this.isLogToConsole;
    }


    private static int indexOf (final String [] options, final String value)
    {
        for (int i = 0; i < options.length; i++)
            if (options[i].equals (value))
                return i;
        return -1;
    }
}
