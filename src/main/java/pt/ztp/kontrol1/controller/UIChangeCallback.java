// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.controller;

/**
 * Callback interface for button and encoder changes.
 *
 * @author Jürgen Moßgraber
 */
public interface UIChangeCallback
{
    /**
     * A button was pressed or released.
     *
     * @param buttonID The ID of the button
     * @param isPressed True if pressed
     */
    void buttonChange (int buttonID, boolean isPressed);


    /**
     * The main encoder was turned.
     *
     * @param valueIncreased The change value
     */
    void mainEncoderChanged (boolean valueIncreased);


    /**
     * An encoder was turned.
     *
     * @param encIndex The index of the encoder (0-7)
     * @param change The change value
     */
    void encoderChanged (int encIndex, int change);


    /**
     * The keyboard was transposed.
     *
     * @param firstNote The left most note that the keyboard sends now
     */
    void octaveChanged (int firstNote);


    /**
     * The status bytes of the keyboard after the encoder values changed, for diagnostics. Added
     * by akaztp (2026).
     *
     * @param description The changed bytes as text
     */
    void statusBytesChanged (String description);
}
