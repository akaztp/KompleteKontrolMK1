// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt
// Modified by akaztp (2026) for the KompleteKontrolMK1 project - see README.md

package pt.ztp.kontrol1.mode;

/**
 * Interface for specific knob and button interactions for modes.
 *
 * @author Jürgen Moßgraber
 */
public interface IKontrol1Mode
{
    /**
     * The main knob was turned.
     *
     * @param value The updated value
     */
    void onMainKnob (final int value);


    /**
     * The main knob was pressed.
     */
    void onMainKnobPressed ();


    /**
     * The back button was pressed.
     */
    void onBack ();


    /**
     * The enter button was pressed.
     */
    void onEnter ();
}
