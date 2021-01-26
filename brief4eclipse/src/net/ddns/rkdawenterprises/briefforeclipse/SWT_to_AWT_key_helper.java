
/***************************************************************************//**
 * Copyright (c) 2020 Ralph and Donna Williamson and RKDAW Enterprises
 * <rkdawenterprises.ddns.net>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0.
 ******************************************************************************/

package net.ddns.rkdawenterprises.briefforeclipse;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;

/**
 * Converts SWT key codes to AWT key codes and facilitates key presses
 * to simulate commands.
 */
public class SWT_to_AWT_key_helper
{
    /**
     * The robot used to simulate key presses.
     */
    private final Robot m_robot;

    /**
     * The map to convert key codes.
     */
    private final Map<Integer, Integer> m_swt_to_awt_mapping;

    /**
     * Creates the helper. Creates the robot and populates the key conversion map.
     *
     * @throws AWTException
     */
    protected SWT_to_AWT_key_helper() throws AWTException
    {
        m_robot = new Robot();
        m_swt_to_awt_mapping = new HashMap<Integer, Integer>();

        /*
         * All the keys currently defined in "org.eclipse.swt.SWT.class".
         * Duplicates, like "MOD1", are removed.
         */
        add_key_mapping( SWT.BS,              KeyEvent.VK_BACK_SPACE );
        add_key_mapping( SWT.CR,              KeyEvent.VK_ENTER );
        add_key_mapping( SWT.DEL,             KeyEvent.VK_DELETE );
        add_key_mapping( SWT.ESC,             KeyEvent.VK_ESCAPE );
        add_key_mapping( SWT.LF,              '\n' );
        add_key_mapping( SWT.TAB,             KeyEvent.VK_TAB );
        add_key_mapping( SWT.SPACE,           KeyEvent.VK_SPACE );
        add_key_mapping( SWT.ALT_GR,          KeyEvent.VK_ALT_GRAPH );
        add_key_mapping( SWT.ALT,             KeyEvent.VK_ALT );
        add_key_mapping( SWT.SHIFT,           KeyEvent.VK_SHIFT );
        add_key_mapping( SWT.CONTROL,         KeyEvent.VK_CONTROL );
        add_key_mapping( SWT.COMMAND,         KeyEvent.VK_META );
        add_key_mapping( SWT.ARROW_UP,        KeyEvent.VK_UP );
        add_key_mapping( SWT.ARROW_DOWN,      KeyEvent.VK_DOWN );
        add_key_mapping( SWT.ARROW_LEFT,      KeyEvent.VK_LEFT );
        add_key_mapping( SWT.ARROW_RIGHT,     KeyEvent.VK_RIGHT );
        add_key_mapping( SWT.PAGE_UP,         KeyEvent.VK_PAGE_UP );
        add_key_mapping( SWT.PAGE_DOWN,       KeyEvent.VK_PAGE_DOWN );
        add_key_mapping( SWT.HOME,            KeyEvent.VK_HOME );
        add_key_mapping( SWT.END,             KeyEvent.VK_END );
        add_key_mapping( SWT.INSERT,          KeyEvent.VK_INSERT );
        add_key_mapping( SWT.F1,              KeyEvent.VK_F1 );
        add_key_mapping( SWT.F2,              KeyEvent.VK_F2 );
        add_key_mapping( SWT.F3,              KeyEvent.VK_F3 );
        add_key_mapping( SWT.F4,              KeyEvent.VK_F4 );
        add_key_mapping( SWT.F5,              KeyEvent.VK_F5 );
        add_key_mapping( SWT.F6,              KeyEvent.VK_F6 );
        add_key_mapping( SWT.F7,              KeyEvent.VK_F7 );
        add_key_mapping( SWT.F8,              KeyEvent.VK_F8 );
        add_key_mapping( SWT.F9,              KeyEvent.VK_F9 );
        add_key_mapping( SWT.F10,             KeyEvent.VK_F10 );
        add_key_mapping( SWT.F12,             KeyEvent.VK_F12 );
        add_key_mapping( SWT.F11,             KeyEvent.VK_F11 );
        add_key_mapping( SWT.F13,             KeyEvent.VK_F13 );
        add_key_mapping( SWT.F14,             KeyEvent.VK_F14 );
        add_key_mapping( SWT.F15,             KeyEvent.VK_F15 );
        add_key_mapping( SWT.F16,             KeyEvent.VK_F16 );
        add_key_mapping( SWT.F17,             KeyEvent.VK_F17 );
        add_key_mapping( SWT.F18,             KeyEvent.VK_F18 );
        add_key_mapping( SWT.F19,             KeyEvent.VK_F19 );
        add_key_mapping( SWT.F20,             KeyEvent.VK_F20 );
        add_key_mapping( SWT.KEYPAD_MULTIPLY, KeyEvent.VK_MULTIPLY );
        add_key_mapping( SWT.KEYPAD_ADD,      KeyEvent.VK_ADD );
        add_key_mapping( SWT.KEYPAD_SUBTRACT, KeyEvent.VK_SUBTRACT );
        add_key_mapping( SWT.KEYPAD_DECIMAL,  KeyEvent.VK_DECIMAL );
        add_key_mapping( SWT.KEYPAD_DIVIDE,   KeyEvent.VK_DIVIDE );
        add_key_mapping( SWT.KEYPAD_0,        KeyEvent.VK_NUMPAD0 );
        add_key_mapping( SWT.KEYPAD_1,        KeyEvent.VK_NUMPAD1 );
        add_key_mapping( SWT.KEYPAD_2,        KeyEvent.VK_NUMPAD2 );
        add_key_mapping( SWT.KEYPAD_3,        KeyEvent.VK_NUMPAD3 );
        add_key_mapping( SWT.KEYPAD_4,        KeyEvent.VK_NUMPAD4 );
        add_key_mapping( SWT.KEYPAD_5,        KeyEvent.VK_NUMPAD5 );
        add_key_mapping( SWT.KEYPAD_6,        KeyEvent.VK_NUMPAD6 );
        add_key_mapping( SWT.KEYPAD_7,        KeyEvent.VK_NUMPAD7 );
        add_key_mapping( SWT.KEYPAD_8,        KeyEvent.VK_NUMPAD8 );
        add_key_mapping( SWT.KEYPAD_9,        KeyEvent.VK_NUMPAD9 );
        add_key_mapping( SWT.KEYPAD_EQUAL,    KeyEvent.VK_EQUALS );
        add_key_mapping( SWT.KEYPAD_CR,       KeyEvent.VK_ENTER );
        add_key_mapping( SWT.HELP,            KeyEvent.VK_HELP );
        add_key_mapping( SWT.CAPS_LOCK,       KeyEvent.VK_CAPS_LOCK );
        add_key_mapping( SWT.NUM_LOCK,        KeyEvent.VK_NUM_LOCK );
        add_key_mapping( SWT.SCROLL_LOCK,     KeyEvent.VK_SCROLL_LOCK );
        add_key_mapping( SWT.PAUSE,           KeyEvent.VK_PAUSE );
        add_key_mapping( SWT.BREAK,           KeyEvent.VK_UNDEFINED );
        add_key_mapping( SWT.PRINT_SCREEN,    KeyEvent.VK_PRINTSCREEN );
    }

    /**
     * Simulates a key press.
     *
     * @param swt_key   The SWT key code.
     *
     * @throws IllegalArgumentException
     * @throws ParseException
     */
    public void press_key( int swt_key ) throws IllegalArgumentException, ParseException
    {
        int vk_key = get_vk_key( swt_key );
        if( vk_key != KeyStroke.NO_KEY )
        {
            m_robot.keyPress( vk_key );
            return;
        }

        throw new IllegalArgumentException( "Key not recognized/supported: " + Integer.toHexString( swt_key ) ); //$NON-NLS-1$
    }

    /**
     * Simulates a key release.
     *
     * @param swt_key   The SWT key code.
     *
     * @throws IllegalArgumentException
     * @throws ParseException
     */
    public void release_key( int swt_key ) throws IllegalArgumentException, ParseException
    {
        int vk_key = get_vk_key( swt_key );
        if( vk_key != KeyStroke.NO_KEY )
        {
            m_robot.keyRelease( vk_key );
            return;
        }

        throw new IllegalArgumentException( "Key not recognized/supported: " + Integer.toHexString( swt_key ) ); //$NON-NLS-1$
    }

    /**
     * Simulates multiple key presses.
     *
     * @param keys  The SWT key codes.
     *
     * @throws IllegalArgumentException
     * @throws ParseException
     */
    public void press_keys( int... keys ) throws IllegalArgumentException, ParseException
    {
        for( int key : keys)
        {
            press_key( key );
        }
    }

    /**
     * Simulates multiple key releases.
     *
     * @param keys  The SWT key codes.
     *
     * @throws IllegalArgumentException
     * @throws ParseException
     */
    public void release_keys( int... keys ) throws IllegalArgumentException, ParseException
    {
        for( int key : keys)
        {
            release_key( key );
        }
    }

    /**
     * Converts an SWT key code to an AWT key code.
     *
     * @param swt_key  The SWT key code.
     *
     * @return  The AWT key code.
     *
     * @throws ParseException
     */
    public int get_vk_key( int swt_key ) throws ParseException
    {
        Integer awt_key = m_swt_to_awt_mapping.get( swt_key );

        if( awt_key != null )
        {
            return awt_key;
        }

        // Unicode should not have keycode bit set, but couldn't hurt to clear it.
        swt_key &= ~( SWT.KEYCODE_BIT );

        awt_key = java.awt.event.KeyEvent.getExtendedKeyCodeForChar( swt_key );

        return( awt_key );
    }

    /**
     * Add the SWT to AWT key mapping to the map.
     *
     * @param swt_key  The SWT key code.
     * @param awt_key  The AWT key code.
     */
    public void add_key_mapping( int swt_key, int awt_key )
    {
        m_swt_to_awt_mapping.put( swt_key, awt_key );
    }
}
