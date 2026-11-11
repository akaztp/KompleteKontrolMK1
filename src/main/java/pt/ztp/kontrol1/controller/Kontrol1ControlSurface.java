// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import pt.ztp.kontrol1.Kontrol1Configuration;
import pt.ztp.kontrol1.NoteInputs;
import pt.ztp.kontrol1.template.NiTemplateKeyboard;
import pt.ztp.kontrol1.template.NiZone;
import de.mossgrabers.framework.controller.AbstractControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.controller.ContinuousID;
import de.mossgrabers.framework.controller.OutputID;
import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.hardware.BindType;
import de.mossgrabers.framework.controller.hardware.IHwContinuousControl;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.daw.midi.MidiConstants;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * The Kontrol 1 surface.
 *
 * @author Jürgen Moßgraber
 */
public class Kontrol1ControlSurface extends AbstractControlSurface<Kontrol1Configuration> implements UIChangeCallback
{
    /** Clicking the main encoder. */
    public static final int                     BUTTON_MAIN_ENCODER   = 1;
    /** The preset up button. */
    public static final int                     BUTTON_PRESET_UP      = 2;
    /** The enter button. */
    public static final int                     BUTTON_ENTER          = 3;
    /** The preset down button. */
    public static final int                     BUTTON_PRESET_DOWN    = 4;
    /** The browse button. */
    public static final int                     BUTTON_BROWSE         = 5;
    /** The instance button. */
    public static final int                     BUTTON_INSTANCE       = 6;
    /** The octave down button. */
    public static final int                     BUTTON_OCTAVE_DOWN    = 7;
    /** The octave up button. */
    public static final int                     BUTTON_OCTAVE_UP      = 8;

    /** The stop button. */
    public static final int                     BUTTON_STOP           = 9;
    /** The record button. */
    public static final int                     BUTTON_REC            = 10;
    /** The play button. */
    public static final int                     BUTTON_PLAY           = 11;
    /** The navigate right button. */
    public static final int                     BUTTON_NAVIGATE_RIGHT = 12;
    /** The navigate down button. */
    public static final int                     BUTTON_NAVIGATE_DOWN  = 13;
    /** The navigate left button. */
    public static final int                     BUTTON_NAVIGATE_LEFT  = 14;
    /** The back button. */
    public static final int                     BUTTON_BACK           = 15;
    /** The navigate up button. */
    public static final int                     BUTTON_NAVIGATE_UP    = 16;

    /** The shift button. */
    public static final int                     BUTTON_SHIFT          = 17;
    /** The scale button. */
    public static final int                     BUTTON_SCALE          = 18;
    /** The arp button. */
    public static final int                     BUTTON_ARP            = 19;
    /** The loop button. */
    public static final int                     BUTTON_LOOP           = 20;
    /** The page right button. */
    public static final int                     BUTTON_PAGE_RIGHT     = 21;
    /** The page left button. */
    public static final int                     BUTTON_PAGE_LEFT      = 22;
    /** The rewind button. */
    public static final int                     BUTTON_RWD            = 23;
    /** The forward button. */
    public static final int                     BUTTON_FWD            = 24;

    /** Touching encoder 1. */
    public static final int                     TOUCH_ENCODER_1       = 25;
    /** Touching encoder 2. */
    public static final int                     TOUCH_ENCODER_2       = 26;
    /** Touching encoder 3. */
    public static final int                     TOUCH_ENCODER_3       = 27;
    /** Touching encoder 4. */
    public static final int                     TOUCH_ENCODER_4       = 28;
    /** Touching encoder 5. */
    public static final int                     TOUCH_ENCODER_5       = 29;
    /** Touching encoder 6. */
    public static final int                     TOUCH_ENCODER_6       = 30;
    /** Touching encoder 7. */
    public static final int                     TOUCH_ENCODER_7       = 31;
    /** Touching encoder 8. */
    public static final int                     TOUCH_ENCODER_8       = 32;

    /** Touching the main encoder. */
    public static final int                     TOUCH_ENCODER_MAIN    = 33;

    // Continuous

    /** Moving encoder 1. */
    public static final int                     ENCODER_1             = 40;
    /** Moving encoder 2. */
    public static final int                     ENCODER_2             = 41;
    /** Moving encoder 3. */
    public static final int                     ENCODER_3             = 42;
    /** Moving encoder 4. */
    public static final int                     ENCODER_4             = 43;
    /** Moving encoder 5. */
    public static final int                     ENCODER_5             = 44;
    /** Moving encoder 6. */
    public static final int                     ENCODER_6             = 45;
    /** Moving encoder 7. */
    public static final int                     ENCODER_7             = 46;
    /** Moving encoder 8. */
    public static final int                     ENCODER_8             = 47;
    /** Moving the main encoder. */
    public static final int                     MAIN_ENCODER          = 48;

    // The framework has no button IDs for these buttons, therefore use generic ones
    /** The ID for the Preset Up button. */
    public static final ButtonID                ID_PRESET_UP          = ButtonID.F1;
    /** The ID for the Preset Down button. */
    public static final ButtonID                ID_PRESET_DOWN        = ButtonID.F2;
    /** The ID for the Instance button. */
    public static final ButtonID                ID_INSTANCE           = ButtonID.F3;

    /** The prefix of the extension's messages on Bitwig's console. */
    public static final String                  LOG_PREFIX            = "[KK1] ";

    private static final Map<Integer, ButtonID> BUTTON_MAP            = new HashMap<> ();
    static
    {
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_SHIFT), ButtonID.SHIFT);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_SCALE), ButtonID.SCALES);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_ARP), ButtonID.METRONOME);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_PLAY), ButtonID.PLAY);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_REC), ButtonID.RECORD);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_STOP), ButtonID.STOP);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_RWD), ButtonID.REWIND);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_FWD), ButtonID.FORWARD);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_LOOP), ButtonID.LOOP);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_PAGE_LEFT), ButtonID.PAGE_LEFT);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_PAGE_RIGHT), ButtonID.PAGE_RIGHT);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_MAIN_ENCODER), ButtonID.MASTERTRACK);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_NAVIGATE_DOWN), ButtonID.ARROW_DOWN);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_NAVIGATE_UP), ButtonID.ARROW_UP);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_NAVIGATE_LEFT), ButtonID.ARROW_LEFT);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_NAVIGATE_RIGHT), ButtonID.ARROW_RIGHT);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_BACK), ButtonID.MUTE);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_ENTER), ButtonID.SOLO);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_BROWSE), ButtonID.BROWSE);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_PRESET_UP), ID_PRESET_UP);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_PRESET_DOWN), ID_PRESET_DOWN);
        BUTTON_MAP.put (Integer.valueOf (Kontrol1ControlSurface.BUTTON_INSTANCE), ID_INSTANCE);
    }

    private static final Map<Integer, ContinuousID> CONTINUOUS_MAP = new HashMap<> ();
    static
    {
        for (int i = 0; i < 8; i++)
            CONTINUOUS_MAP.put (Integer.valueOf (Kontrol1ControlSurface.TOUCH_ENCODER_1 + i), ContinuousID.get (ContinuousID.KNOB1, i));
        CONTINUOUS_MAP.put (Integer.valueOf (Kontrol1ControlSurface.TOUCH_ENCODER_MAIN), ContinuousID.MASTER_KNOB);
    }

    private final Kontrol1UsbDevice  usbDevice;
    private final NiTemplateKeyboard niTemplateKeyboard;
    private boolean                  isKeyLEDResendNeeded;


    /**
     * Constructor.
     *
     * @param host The host
     * @param colorManager The color manager
     * @param configuration The configuration
     * @param input The MIDI input
     * @param usbDevice The USB device
     * @param niTemplateKeyboard The keys of the NI template which the keyboard plays
     */
    public Kontrol1ControlSurface (final IHost host, final ColorManager colorManager, final Kontrol1Configuration configuration, final IMidiInput input, final Kontrol1UsbDevice usbDevice, final NiTemplateKeyboard niTemplateKeyboard)
    {
        super (0, host, configuration, colorManager, null, input, null, null, 800, 300);

        this.usbDevice = usbDevice;
        this.niTemplateKeyboard = niTemplateKeyboard;

        this.lightGuide = new Kontrol1LightGuide (colorManager, usbDevice);
        this.createLightGuide ();
    }


    /** {@inheritDoc} */
    @Override
    protected void createLightGuide ()
    {
        if (this.lightGuide == null)
            return;

        for (int i = 0; i < this.usbDevice.getNumKeys (); i++)
        {
            final int index = i;

            this.createLight (OutputID.get (OutputID.LIGHT_GUIDE1, i), () -> {

                // The lights on the device are always addressed from 0..N, therefore the currently
                // selected first note of the keyboard (depending on octave transpose) needs to be
                // added
                final int firstNote = this.usbDevice.getFirstNote ();
                final int note = firstNote + index;
                if (note >= 128)
                    return -1;
                return this.lightGuide.getLightInfo (note).getEncoded ();

            }, state -> {

                final int firstNote = this.usbDevice.getFirstNote ();
                final int note = firstNote + index;
                this.lightGuide.sendState (note);

            }, colorIndex -> this.colorManager.getColor (colorIndex, null), null);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void internalShutdown ()
    {
        this.usbDevice.turnOffButtonLEDs ();

        // The keyboard keeps the lights which it has when the extension stops (until it is switched
        // off and on), so leave the zones of the template lit
        final Kontrol1ColorManager kontrol1ColorManager = (Kontrol1ColorManager) this.colorManager;
        for (int i = 0; i < 88; i++)
        {
            final NiZone zone = this.niTemplateKeyboard.getZoneForKeyIndex (i);
            final int [] rgb = zone == null ? new int [3] : kontrol1ColorManager.getZoneColor (zone.color (), false).toIntRGB127 ();
            this.usbDevice.setKeyLED (i, rgb[0], rgb[1], rgb[2]);
        }
        this.updateKeyLEDs ();

        this.getTextDisplay ().clear ().notify (" START  " + this.host.getName ().toUpperCase (Locale.US) + " TO PLAY");

        super.internalShutdown ();
    }


    /** {@inheritDoc} */
    @Override
    protected void updateGrid ()
    {
        super.updateGrid ();

        // The light guide only sends the keys whose color changed, so send all keys after the RGB
        // values of their colors changed. Done after drawing, so that they get their new colors.
        if (this.isKeyLEDResendNeeded)
        {
            this.isKeyLEDResendNeeded = false;
            final int firstNote = this.usbDevice.getFirstNote ();
            for (int i = 0; i < this.usbDevice.getNumKeys (); i++)
            {
                final int note = firstNote + i;
                if (note >= 0 && note < 128)
                    this.lightGuide.sendState (note);
            }
        }

        this.updateKeyLEDs ();
    }


    /**
     * Send the colors of all keys again with the next update, after the RGB values of their colors
     * changed.
     */
    public void resendKeyLEDs ()
    {
        this.isKeyLEDResendNeeded = true;
    }


    /** {@inheritDoc} */
    @Override
    public void setTrigger (final BindType bindType, final int channel, final int cc, final int value)
    {
        this.usbDevice.setButtonLED (cc, value);
    }


    /** {@inheritDoc} */
    @Override
    public void buttonChange (final int usbControlNumber, final boolean isPressed)
    {
        final ButtonID buttonID = BUTTON_MAP.get (Integer.valueOf (usbControlNumber));
        if (buttonID == null)
        {
            // Simulate knob touch via CC
            final ContinuousID continuousID = CONTINUOUS_MAP.get (Integer.valueOf (usbControlNumber));
            if (continuousID == null)
                return;
            final IHwContinuousControl continuous = this.getContinuous (continuousID);
            if (isPressed)
            {
                if (!continuous.isTouched ())
                    continuous.triggerTouch (true);
            }
            else if (continuous.isTouched ())
                continuous.triggerTouch (false);
            return;
        }

        // Simulate button press via CC
        if (isPressed)
        {
            if (!this.isPressed (buttonID))
                this.getButton (buttonID).trigger (ButtonEvent.DOWN);
        }
        else if (this.isPressed (buttonID))
            this.getButton (buttonID).trigger (ButtonEvent.UP);
    }


    /** {@inheritDoc} */
    @Override
    public void mainEncoderChanged (final boolean valueIncreased)
    {
        this.getContinuous (ContinuousID.MASTER_KNOB).getCommand ().execute (valueIncreased ? 3 : 125);
    }


    /** {@inheritDoc} */
    @Override
    public void encoderChanged (final int encIndex, final int change)
    {
        final int v;
        if (this.isShiftPressed ())
            v = change < 0 ? 127 : 1;
        else
            v = change < 0 ? 127 + change : change;
        this.getContinuous (ContinuousID.get (ContinuousID.KNOB1, encIndex)).getCommand ().execute (v);
    }


    /** {@inheritDoc} */
    @Override
    public void octaveChanged (final int firstNote)
    {
        this.niTemplateKeyboard.setFirstNote (firstNote);
        this.log (() -> "Left-most key " + Scales.formatDrumNote (firstNote) + ", octave shift " + this.niTemplateKeyboard.getOctaveShift ());

        final int endNote = firstNote + this.usbDevice.getNumKeys () - 1;
        this.getDisplay ().notify (Scales.formatDrumNote (firstNote) + " to " + Scales.formatDrumNote (endNote));
    }


    /**
     * Send the LED status to the device.
     */
    public void updateButtonLEDs ()
    {
        this.usbDevice.updateButtonLEDs ();
    }


    /**
     * Send the key LED status updates to the device.
     */
    public void updateKeyLEDs ()
    {
        this.usbDevice.updateKeyLEDs ();
    }


    /** {@inheritDoc} */
    @Override
    protected void updateViewControls ()
    {
        super.updateViewControls ();
        this.updateButtonLEDs ();
    }


    /** {@inheritDoc} */
    @Override
    protected void handleMidi (final int status, final int data1, final int data2)
    {
        // The NI template plays the keys, pedals and wheels, their MIDI reaches the note inputs
        // directly. The notes light up their keys
        final int code = status & 0xF0;
        final int channel = (status & 0x0F) + 1;
        if (code == MidiConstants.CMD_NOTE_ON)
            this.niTemplateKeyboard.noteOn (channel, data1, data2);
        else if (code == MidiConstants.CMD_NOTE_OFF)
            this.niTemplateKeyboard.noteOff (channel, data1);
        this.logMidi (code, channel, data1, data2);
    }


    /**
     * Log the notes and CCs which the NI template sends, and the note inputs which receive them.
     *
     * @param code The MIDI status without the channel
     * @param channel The MIDI channel (1-16)
     * @param data1 The first data byte
     * @param data2 The second data byte
     */
    private void logMidi (final int code, final int channel, final int data1, final int data2)
    {
        final String inputs = " on MIDI channel " + channel + " -> " + NoteInputs.MAIN_INPUT_NAME + ", " + NoteInputs.getChannelInputName (channel);
        switch (code)
        {
            case MidiConstants.CMD_NOTE_ON:
                this.log (() -> "Note on " + Scales.formatDrumNote (data1) + " (" + data1 + ") velocity " + data2 + inputs);
                break;
            case MidiConstants.CMD_NOTE_OFF:
                this.log (() -> "Note off " + Scales.formatDrumNote (data1) + " (" + data1 + ")" + inputs);
                break;
            case MidiConstants.CMD_CC:
                this.log (() -> "CC " + data1 + " value " + data2 + inputs);
                break;
            default:
                // Pitch-bend and after-touch would flood the console
                break;
        }
    }


    /**
     * Describe the state of the USB connection for diagnostics.
     *
     * @return The description
     */
    public String describeUsb ()
    {
        final StringBuilder sb = new StringBuilder ("USB connection: ");
        if (!this.usbDevice.isConnected ())
            return sb.append ("not open (display, buttons and lights don't work, see any error above)").toString ();
        sb.append ("open, ").append (this.usbDevice.hasReceivedReport () ? "keyboard status received" : "no keyboard status received yet");
        sb.append (", left-most key ").append (Scales.formatDrumNote (this.usbDevice.getFirstNote ()));
        return sb.toString ();
    }


    /** {@inheritDoc} */
    @Override
    public void statusBytesChanged (final String description)
    {
        this.log (() -> "Keyboard status bytes " + description + " (left-most key " + Scales.formatDrumNote (this.usbDevice.getFirstNote ()) + ")");
    }


    /**
     * Print a message to Bitwig's control script console if enabled in the settings.
     *
     * @param message Creates the message, only called if logging is enabled
     */
    public void log (final Supplier<String> message)
    {
        if (this.configuration.isLogToConsole ())
            this.host.println (LOG_PREFIX + message.get ());
    }


    /**
     * Get the keys of the NI template which the keyboard plays.
     *
     * @return The template's keys
     */
    public NiTemplateKeyboard getNiTemplateKeyboard ()
    {
        return this.niTemplateKeyboard;
    }


    /**
     * Show a message on the display, one text of up to 8 characters per display cell.
     *
     * @param cells The texts of up to 9 cells, from left to right
     */
    public void notifyCells (final String... cells)
    {
        final StringBuilder text = new StringBuilder ();
        for (int i = 0; i < 9; i++)
            text.append (String.format ("%-8.8s", i < cells.length ? cells[i] : ""));
        this.getDisplay ().notify (text.toString ());
    }


    /**
     * Get the note which the left-most key currently plays (depends on the octave shift).
     *
     * @return The note
     */
    public int getFirstNote ()
    {
        return this.usbDevice.getFirstNote ();
    }


    /**
     * Get the number of keys of the keyboard.
     *
     * @return The number of keys
     */
    public int getNumKeys ()
    {
        return this.usbDevice.getNumKeys ();
    }
}