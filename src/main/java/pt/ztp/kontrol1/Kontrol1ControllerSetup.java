// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1;

import java.util.List;
import java.util.function.BooleanSupplier;

import pt.ztp.kontrol1.command.continuous.MainEncoderCommand;
import pt.ztp.kontrol1.command.trigger.BackButtonCommand;
import pt.ztp.kontrol1.command.trigger.EnterButtonCommand;
import pt.ztp.kontrol1.command.trigger.Kontrol1CursorCommand;
import pt.ztp.kontrol1.command.trigger.Kontrol1PlayCommand;
import pt.ztp.kontrol1.command.trigger.MainEncoderButtonCommand;
import pt.ztp.kontrol1.command.trigger.NiTemplateCommand;
import pt.ztp.kontrol1.controller.Kontrol1ColorManager;
import pt.ztp.kontrol1.controller.Kontrol1ControlSurface;
import pt.ztp.kontrol1.controller.Kontrol1Display;
import pt.ztp.kontrol1.controller.Kontrol1UsbDevice;
import pt.ztp.kontrol1.mode.device.BrowseMode;
import pt.ztp.kontrol1.mode.device.ParamsMode;
import pt.ztp.kontrol1.mode.track.TrackMode;
import pt.ztp.kontrol1.mode.track.VolumeMode;
import pt.ztp.kontrol1.template.NiTemplate;
import pt.ztp.kontrol1.template.NiTemplateKeyboard;
import pt.ztp.kontrol1.template.NiZone;
import pt.ztp.kontrol1.view.ControlView;
import de.mossgrabers.framework.command.continuous.KnobRowModeCommand;
import de.mossgrabers.framework.command.core.NopCommand;
import de.mossgrabers.framework.command.trigger.BrowserCommand;
import de.mossgrabers.framework.command.trigger.Direction;
import de.mossgrabers.framework.command.trigger.mode.KnobRowTouchModeCommand;
import de.mossgrabers.framework.command.trigger.mode.ModeMultiSelectCommand;
import de.mossgrabers.framework.command.trigger.transport.MetronomeCommand;
import de.mossgrabers.framework.command.trigger.transport.RecordCommand;
import de.mossgrabers.framework.command.trigger.transport.StopCommand;
import de.mossgrabers.framework.command.trigger.transport.ToggleLoopCommand;
import de.mossgrabers.framework.command.trigger.transport.WindCommand;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.controller.OutputID;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwRelativeKnob;
import de.mossgrabers.framework.controller.valuechanger.TwosComplementValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.ModelSetup;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.featuregroup.ViewManager;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.view.Views;
import purejavahidapi.PureJavaHidApi;


/**
 * Setup to support the Native Instruments Komplete Kontrol 1 Sxx controllers.
 *
 * @author Jürgen Moßgraber
 */
public class Kontrol1ControllerSetup extends AbstractControllerSetup<Kontrol1ControlSurface, Kontrol1Configuration>
{
    // Put this on channel 16 to not conflict with real MIDI messages like the modulation wheel,
    // expression, etc.
    private static final int CHANNEL            = 15;

    private final int        modelIndex;
    private NiTemplate       lastActiveTemplate;


    /**
     * Constructor.
     *
     * @param modelIndex The index of the model (S25, S49, S61, S88)
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     */
    public Kontrol1ControllerSetup (final int modelIndex, final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        super (factory, host, globalSettings, documentSettings);
        this.modelIndex = modelIndex;
        this.valueChanger = new TwosComplementValueChanger (1024, 10);
        this.colorManager = new Kontrol1ColorManager ();
        this.configuration = new Kontrol1Configuration (host, this.valueChanger, factory.getArpeggiatorModes ());
    }


    /** {@inheritDoc} */
    @Override
    protected void createScales ()
    {
        this.scales = new Scales (this.valueChanger, 0, 127, 128, 1);
        this.scales.setChromatic (true);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        this.model = this.factory.createModel (this.configuration, this.colorManager, this.valueChanger, this.scales, new ModelSetup ());
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final Kontrol1UsbDevice usbDevice = new Kontrol1UsbDevice (this.modelIndex, this.host);
        // The start-up message hands the key lights and the display over to the extension. The
        // keyboard then stops drawing the lights of its template until it is switched off and on,
        // or Controller Editor takes it back, so the extension draws them from the template file.
        // If the file could not be read, it leaves the lights alone
        usbDevice.setKeyLightsEnabled (!this.configuration.getNiTemplates ().isEmpty ());
        usbDevice.setLogger (message -> {
            if (this.configuration.isLogToConsole ())
                this.host.println (Kontrol1ControlSurface.LOG_PREFIX + message);
        });
        usbDevice.init ();

        // The NI template plays the keys, pedals and wheels on its MIDI channels. The channel note
        // inputs only reach tracks which select them explicitly
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiInput input = midiAccess.createInput (NoteInputs.MAIN_INPUT_NAME, NoteInputs.getMidiFilters (0));
        for (int channel = 1; channel <= NoteInputs.NUM_CHANNELS; channel++)
            input.createNoteInput (NoteInputs.getChannelInputName (channel), NoteInputs.getMidiFilters (channel)).setIncludeInAllInputs (false);

        final NiTemplateKeyboard niTemplateKeyboard = new NiTemplateKeyboard (Kontrol1ControllerDefinition.getDefaultFirstNote (this.modelIndex));
        niTemplateKeyboard.setFirstNote (usbDevice.getFirstNote ());

        final Kontrol1ControlSurface surface = new Kontrol1ControlSurface (this.host, this.colorManager, this.configuration, input, usbDevice, niTemplateKeyboard);
        usbDevice.setCallback (surface);
        this.surfaces.add (surface);
        final Kontrol1Display display = new Kontrol1Display (this.host, this.valueChanger.getUpperBound (), this.configuration, usbDevice);
        surface.addTextDisplay (display);

        surface.getModeManager ().setDefaultID (Modes.TRACK);
    }


    /** {@inheritDoc} */
    @Override
    protected void createModes ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        final ModeManager modeManager = surface.getModeManager ();

        modeManager.register (Modes.TRACK, new TrackMode (surface, this.model));
        modeManager.register (Modes.VOLUME, new VolumeMode (surface, this.model));
        modeManager.register (Modes.DEVICE_PARAMS, new ParamsMode (surface, this.model));
        modeManager.register (Modes.BROWSER, new BrowseMode (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createViews ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        final ViewManager viewManager = surface.getViewManager ();
        viewManager.register (Views.CONTROL, new ControlView (surface, this.model));
    }


    /** {@inheritDoc} */
    @Override
    protected void createObservers ()
    {
        super.createObservers ();

        final ITrackBank trackBank = this.model.getTrackBank ();
        trackBank.addSelectionObserver ((index, isSelected) -> this.handleTrackChange (isSelected));
        final ITrackBank effectTrackBank = this.model.getEffectTrackBank ();
        if (effectTrackBank != null)
            effectTrackBank.addSelectionObserver ((index, isSelected) -> this.handleTrackChange (isSelected));

        this.configuration.registerDeactivatedItemsHandler (this.model);

        this.activateBrowserObserver (Modes.BROWSER);

        // The NI template which the keyboard plays
        this.configuration.addSettingObserver (Kontrol1Configuration.ACTIVE_TEMPLATE, this::updateNiTemplate);
        this.updateNiTemplate ();

        // Turning the log on prints the state of the keyboard and its templates
        this.configuration.addSettingObserver (Kontrol1Configuration.LOG_TO_CONSOLE, this::logState);
        this.logState ();

        // The brightness of the key lights which are not held
        this.configuration.addSettingObserver (Kontrol1Configuration.UNHELD_KEY_BRIGHTNESS, this::updateUnheldKeyBrightness);
        this.updateUnheldKeyBrightness ();
    }


    /**
     * Set the brightness of the key lights which are not held, and send the keys again with it.
     */
    private void updateUnheldKeyBrightness ()
    {
        ((Kontrol1ColorManager) this.colorManager).setZoneDimFactor (this.configuration.getUnheldKeyBrightness ());
        this.getSurface ().resendKeyLEDs ();
    }


    /**
     * Set the NI template which the keyboard plays for the key lights, and show its name and zones on
     * the display if it changed.
     */
    private void updateNiTemplate ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        final NiTemplate template = this.configuration.getActiveTemplate ();
        surface.getNiTemplateKeyboard ().setTemplate (template);
        if (template == null || template == this.lastActiveTemplate)
            return;

        // Don't show a notification on startup, logState logs the template then
        if (this.lastActiveTemplate != null)
        {
            surface.log (this::describeActiveTemplate);

            final List<NiZone> zones = template.zones ();
            final String [] cells = new String [1 + Math.min (8, zones.size ())];
            cells[0] = template.name ();
            for (int i = 1; i < cells.length; i++)
                cells[i] = zones.get (i - 1).getDisplayName ();
            surface.notifyCells (cells);
        }
        this.lastActiveTemplate = template;
    }


    /**
     * Log the state of the USB connection, the note inputs and the NI templates, if logging is
     * enabled.
     */
    private void logState ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        surface.log (surface::describeUsb);
        surface.log (NoteInputs::describe);
        surface.log (this.configuration::getNiTemplateFileStatus);
        if (this.configuration.getActiveTemplate () != null)
            surface.log (this::describeActiveTemplate);
    }


    private String describeActiveTemplate ()
    {
        return "NI template " + (this.configuration.getActiveTemplateIndex () + 1) + " of " + this.configuration.getNiTemplates ().size () + ": " + this.configuration.getActiveTemplate ().describe ();
    }


    /** {@inheritDoc} */
    @Override
    protected void registerTriggerCommands ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        final ITransport t = this.model.getTransport ();

        // The NI template plays the keys, so the scale of the light guide is not used
        this.addButton (ButtonID.SCALES, "Scales", NopCommand.INSTANCE, CHANNEL, Kontrol1ControlSurface.BUTTON_SCALE, () -> false);

        // The keyboard itself switches its templates with Preset Up/Down: Preset Down selects the
        // next one in Controller Editor's list. The extension follows it
        final BooleanSupplier hasTemplates = () -> this.configuration.getNiTemplates ().size () > 1;
        this.addButton (Kontrol1ControlSurface.ID_PRESET_UP, "Preset Up", new NiTemplateCommand (this.model, surface, -1), CHANNEL, Kontrol1ControlSurface.BUTTON_PRESET_UP, hasTemplates);
        this.addButton (Kontrol1ControlSurface.ID_PRESET_DOWN, "Preset Down", new NiTemplateCommand (this.model, surface, 1), CHANNEL, Kontrol1ControlSurface.BUTTON_PRESET_DOWN, hasTemplates);
        this.addButton (Kontrol1ControlSurface.ID_INSTANCE, "Instance", NopCommand.INSTANCE, CHANNEL, Kontrol1ControlSurface.BUTTON_INSTANCE, () -> false);
        this.addButton (ButtonID.METRONOME, "Metronome", new MetronomeCommand<> (this.model, surface, false), CHANNEL, Kontrol1ControlSurface.BUTTON_ARP, () -> surface.isShiftPressed () && t.isMetronomeTicksOn () || !surface.isShiftPressed () && t.isMetronomeOn ());

        this.addButton (ButtonID.PLAY, "PLAY", new Kontrol1PlayCommand (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_PLAY, t::isPlaying);
        this.addButton (ButtonID.RECORD, "REC", new RecordCommand<> (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_REC, t::isRecording);
        this.addButton (ButtonID.STOP, "STOP", new StopCommand<> (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_STOP, () -> !t.isPlaying ());
        this.addButton (ButtonID.REWIND, "RWD", new WindCommand<> (this.model, surface, false), CHANNEL, Kontrol1ControlSurface.BUTTON_RWD, () -> surface.isPressed (ButtonID.REWIND));
        this.addButton (ButtonID.FORWARD, "FFW", new WindCommand<> (this.model, surface, true), CHANNEL, Kontrol1ControlSurface.BUTTON_FWD, () -> surface.isPressed (ButtonID.FORWARD));
        this.addButton (ButtonID.LOOP, "LOOP", new ToggleLoopCommand<> (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_LOOP, t::isLoop);

        this.addButton (ButtonID.PAGE_LEFT, "Left", new ModeMultiSelectCommand<> (this.model, surface, Modes.DEVICE_PARAMS, Modes.VOLUME, Modes.TRACK), CHANNEL, Kontrol1ControlSurface.BUTTON_PAGE_LEFT);
        this.addButton (ButtonID.PAGE_RIGHT, "Right", new ModeMultiSelectCommand<> (this.model, surface, Modes.TRACK, Modes.VOLUME, Modes.DEVICE_PARAMS), CHANNEL, Kontrol1ControlSurface.BUTTON_PAGE_RIGHT);

        this.addButton (ButtonID.MASTERTRACK, "Encoder", new MainEncoderButtonCommand (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_MAIN_ENCODER);

        final Kontrol1CursorCommand commandDown = new Kontrol1CursorCommand (Direction.DOWN, this.model, surface);
        this.addButton (ButtonID.ARROW_DOWN, "Down", commandDown, CHANNEL, Kontrol1ControlSurface.BUTTON_NAVIGATE_DOWN, commandDown::canScroll);
        final Kontrol1CursorCommand commandUp = new Kontrol1CursorCommand (Direction.UP, this.model, surface);
        this.addButton (ButtonID.ARROW_UP, "Up", commandUp, CHANNEL, Kontrol1ControlSurface.BUTTON_NAVIGATE_UP, commandUp::canScroll);
        final Kontrol1CursorCommand commandLeft = new Kontrol1CursorCommand (Direction.LEFT, this.model, surface);
        this.addButton (ButtonID.ARROW_LEFT, "Left", commandLeft, CHANNEL, Kontrol1ControlSurface.BUTTON_NAVIGATE_LEFT, commandLeft::canScroll);
        final Kontrol1CursorCommand commandRight = new Kontrol1CursorCommand (Direction.RIGHT, this.model, surface);
        this.addButton (ButtonID.ARROW_RIGHT, "Right", commandRight, CHANNEL, Kontrol1ControlSurface.BUTTON_NAVIGATE_RIGHT, commandRight::canScroll);

        this.addButton (ButtonID.MUTE, "Back", new BackButtonCommand (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_BACK, () -> this.getModeColor (ButtonID.MUTE));
        this.addButton (ButtonID.SOLO, "Enter", new EnterButtonCommand (this.model, surface), CHANNEL, Kontrol1ControlSurface.BUTTON_ENTER, () -> this.getModeColor (ButtonID.SOLO));

        this.addButton (ButtonID.BROWSE, "Browse", new BrowserCommand<> (this.model, surface, ButtonID.SHIFT, null), CHANNEL, Kontrol1ControlSurface.BUTTON_BROWSE, () -> this.getModeColor (ButtonID.BROWSE));
        this.addButton (ButtonID.SHIFT, "Shift", NopCommand.INSTANCE, CHANNEL, Kontrol1ControlSurface.BUTTON_SHIFT);
    }


    /** {@inheritDoc} */
    @Override
    protected void registerContinuousCommands ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        final IMidiInput input = surface.getMidiInput ();

        for (int i = 0; i < 8; i++)
        {
            final IHwRelativeKnob knob = this.addRelativeKnob (ContinuousID.get (ContinuousID.KNOB1, i), "Knob " + (i + 1), new KnobRowModeCommand<> (i, this.model, surface), BindType.CC, CHANNEL, Kontrol1ControlSurface.ENCODER_1 + i);
            knob.bindTouch (new KnobRowTouchModeCommand<> (i, this.model, surface), input, BindType.CC, CHANNEL, Kontrol1ControlSurface.TOUCH_ENCODER_1 + i);
            knob.setIndexInGroup (i);
        }

        this.addRelativeKnob (ContinuousID.MASTER_KNOB, "Master", new MainEncoderCommand (this.model, surface), BindType.CC, CHANNEL, Kontrol1ControlSurface.MAIN_ENCODER);

        surface.addPianoKeyboard (25, input, true);
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();
        surface.getViewManager ().setActive (Views.CONTROL);
        surface.getModeManager ().setActive (Modes.TRACK);

        // Without the templates the key lights don't work, tell why
        if (this.configuration.getNiTemplates ().isEmpty ())
        {
            final String status = this.configuration.getNiTemplateFileStatus ();
            this.host.showNotification ("Komplete Kontrol: " + status);
            this.host.println (Kontrol1ControlSurface.LOG_PREFIX + status);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void layoutControls ()
    {
        final Kontrol1ControlSurface surface = this.getSurface ();

        surface.getButton (ButtonID.PLAY).setBounds (7.25, 135.25, 42.25, 23.5);
        surface.getButton (ButtonID.RECORD).setBounds (68.0, 135.25, 42.25, 23.5);
        surface.getButton (ButtonID.STOP).setBounds (128.75, 135.25, 42.25, 23.5);
        surface.getButton (ButtonID.REWIND).setBounds (68.0, 100.75, 42.25, 23.5);
        surface.getButton (ButtonID.FORWARD).setBounds (128.75, 100.75, 42.25, 23.5);
        surface.getButton (ButtonID.LOOP).setBounds (7.25, 100.75, 42.25, 23.5);
        surface.getButton (ButtonID.METRONOME).setBounds (128.75, 31.5, 42.25, 23.5);
        surface.getButton (ButtonID.SHIFT).setBounds (7.25, 31.5, 42.25, 23.5);
        surface.getButton (ButtonID.SCALES).setBounds (68.0, 31.5, 42.25, 23.5);
        surface.getButton (ButtonID.PAGE_LEFT).setBounds (182.25, 65.5, 19.25, 23.5);
        surface.getButton (ButtonID.PAGE_RIGHT).setBounds (209.5, 65.5, 19.25, 23.5);
        surface.getButton (ButtonID.MUTE).setBounds (639.0, 105.25, 42.25, 23.5);
        surface.getButton (ButtonID.SOLO).setBounds (747.5, 105.25, 42.25, 23.5);
        surface.getButton (ButtonID.ARROW_UP).setBounds (693.25, 105.25, 42.25, 23.5);
        surface.getButton (ButtonID.ARROW_DOWN).setBounds (693.25, 133.25, 42.25, 23.5);
        surface.getButton (ButtonID.ARROW_LEFT).setBounds (639.0, 133.25, 42.25, 23.5);
        surface.getButton (ButtonID.ARROW_RIGHT).setBounds (747.5, 133.25, 42.25, 23.5);
        surface.getButton (ButtonID.BROWSE).setBounds (639.0, 46.75, 42.25, 23.5);
        surface.getButton (Kontrol1ControlSurface.ID_INSTANCE).setBounds (747.5, 46.75, 42.25, 23.5);
        surface.getButton (Kontrol1ControlSurface.ID_PRESET_UP).setBounds (747.5, 75.5, 20.0, 23.5);
        surface.getButton (Kontrol1ControlSurface.ID_PRESET_DOWN).setBounds (769.75, 75.5, 20.0, 23.5);
        surface.getButton (ButtonID.MASTERTRACK).setBounds (686.25, 24.75, 49.5, 21.5);

        surface.getContinuous (ContinuousID.MASTER_KNOB).setBounds (693.25, 44.75, 43.0, 53.75);
        surface.getContinuous (ContinuousID.KNOB1).setBounds (241.0, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB2).setBounds (288.25, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB3).setBounds (335.5, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB4).setBounds (382.75, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB5).setBounds (430.0, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB6).setBounds (477.25, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB7).setBounds (524.5, 59.5, 35.0, 30.0);
        surface.getContinuous (ContinuousID.KNOB8).setBounds (571.75, 59.5, 35.0, 30.0);

        surface.getLight (OutputID.LIGHT_GUIDE1).setBounds (203.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE2).setBounds (225.75, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE3).setBounds (244.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE4).setBounds (263.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE5).setBounds (285.5, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE6).setBounds (311.5, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE7).setBounds (336.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE8).setBounds (355.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE9).setBounds (376.5, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE10).setBounds (394.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE11).setBounds (413.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE12).setBounds (436.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE13).setBounds (463.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE14).setBounds (487.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE15).setBounds (505.75, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE16).setBounds (522.75, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE17).setBounds (547.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE18).setBounds (573.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE19).setBounds (596.0, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE20).setBounds (617.5, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE21).setBounds (634.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE22).setBounds (653.5, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE23).setBounds (671.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE24).setBounds (696.25, 172.5, 15.5, 29.5);
        surface.getLight (OutputID.LIGHT_GUIDE25).setBounds (726.75, 172.5, 15.5, 29.5);

        surface.getTextDisplay ().getHardwareDisplay ().setBounds (190.75, 100.25, 422.75, 33.5);

        surface.getContinuous (ContinuousID.PITCHBEND_WHEEL).setBounds (45.0, 219.5, 37.75, 68.5);
        surface.getContinuous (ContinuousID.MODULATION_WHEEL).setBounds (96.0, 219.5, 37.75, 68.5);

        surface.getPianoKeyboard ().setBounds (196.5, 211.0, 558.75, 88.75);
    }


    /** {@inheritDoc} */
    @Override
    public void exit ()
    {
        super.exit ();

        PureJavaHidApi.cleanup ();
    }
}
