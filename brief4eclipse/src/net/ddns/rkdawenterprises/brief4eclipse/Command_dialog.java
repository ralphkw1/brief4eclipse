
/***************************************************************************//**
 * Copyright (c) 2021 RKDAW Enterprises and Ralph Williamson,
 * <rkdawenterprises.ddns.net, rkdawenterprises@gmail.com>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0 (the "License"). You may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at "https://www.eclipse.org/legal/epl-2.0".
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions, warranties,
 * and limitations under the License.
 ******************************************************************************/

package net.ddns.rkdawenterprises.brief4eclipse;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

/**
 * Dialog that obtains a command string or a text string to repeat a requested number of times.
 */
public class Command_dialog extends Abstract_text_dialog
{
    /**
     * Limit the repeat count.
     */
    private static final int MAX_COMMAND_COUNT = 1024;

    /**
     * Creates the command dialog.
     *
     * @param parent_shell  The parent shell.
     * @param editor        The active editor.
     */
    protected Command_dialog( Shell parent_shell,
                              IEditorPart editor )
    {
        super( parent_shell,
               editor,
               String.format( m_command_prompt_format, 1, m_command_prompt_initial_instructions ) );

        m_count = 1;
        m_command = new StringBuilder( "" ); //$NON-NLS-1$
    }

    /**
     * The repeat count parsed from the text string from the dialog.
     */
    private int m_count = 1;

    /**
     * Getter.
     * @return  The repeat count parsed from the text string from the dialog.
     */
    public int get_count()
    {
        return m_count;
    }

    /**
     * The command string parsed from the dialog's text box.
     * There will either be a valid command string or a valid text string to repeat.
     */
    private StringBuilder m_command = null; //$NON-NLS-1$

    /**
     * Getter.
     * @return  The command string parsed from the text string from the dialog.
     */
    public String get_command()
    {
        return m_command.toString();
    }

    /**
     * The SWT modifier bits, if any, are index 0. The SWT key code is index 1.
     */
    private int[] m_command_codes = new int[2];

    /**
     * Getter.
     * @return  The SWT modifier bits, if any, are index 0. The SWT key code is index 1.
     */
    public int[] get_command_codes()
    {
        return m_command_codes;
    }

    /**
     * The text string parsed from the dialog's text box.
     * There will either be a valid command string or a valid text string to repeat.
     */
    private String m_repeat_string = ""; //$NON-NLS-1$

    public String get_repeat_string()
    {
        return new String( m_repeat_string );
    }

    /**
     * The initial format of the text that makes up the dialog's text box.
     */
    private static final String m_command_prompt_format =
            Messages.command_dialog_command_prompt_format;

    /**
     * The text box in the dialog starts with instructions.
     */
    private static final String m_command_prompt_initial_instructions =
            Messages.command_dialog_command_prompt_initial_instructions;

    @Override
    protected void create_contents( Composite parent )
    {
        super.create_contents( parent );

        setup_key_filter( parent.getShell() );
    }

    /**
     * Sets up the key filter so we can monitor keystrokes so we can actively
     * replace the text with requested command count and repeated command or string.
     *
     * @param shell     The shell to attach the key filter.
     */
    private void setup_key_filter( Shell shell )
    {
        shell.getDisplay().addFilter( SWT.KeyDown, m_key_filter );
        m_command_state = COMMAND_STATE.IDLE;

        // Highlight the command count.
        int start = m_command_prompt_format.indexOf( "%d" );  //$NON-NLS-1$
        int end = start + String.format( "%d", m_count ).length(); //$NON-NLS-1$
        get_text().setSelection( start, end );
    }

    @Override
    public boolean close()
    {
        if( ( getShell() != null ) && ( !getShell().isDisposed() ) )
        {
            getShell().getDisplay()
                      .removeFilter( SWT.KeyDown,
                                     m_key_filter );
            get_text().removeModifyListener( m_text_listener );
        }

        return super.close();
    }

    /**
     * Keystroke listener for the command processor.
     */
    private Listener m_key_filter = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof Text )
            {
                if( ( get_text().isFocusControl() ) && ( get_text() == event.widget ) )
                {
                    boolean consumed = key_event_processor( event.type,
                                                            event.stateMask,
                                                            event.keyCode,
                                                            event.character );
                    if( consumed )
                    {
                        // Not passing this key event to the Text widget.
                        event.type = SWT.None;
                        event.doit = false;
                    }
                }
            }
        }
    };

    /**
     * Once the repeat count is set and the command processor moves to gathering the repeat
     * string, this listener captures the actual text.
     */
    private ModifyListener m_text_listener = new ModifyListener()
    {
        @Override
        public void modifyText( ModifyEvent event )
        {
            // Extract the text from the whole string.
            String string = ( (Text)( event.getSource() ) ).getText();
            String prefix = String.format( m_command_prompt_format, m_count, "" );
            m_repeat_string = string.replace( prefix, "" );
        }
    };

    /**
     * The key event processor states.
     */
    private enum COMMAND_STATE
    {
        IDLE,
        FORM_COMMAND_STRING,
        WAIT_FOR_COMMAND_OR_STRING_START,
        ACCUMULATE_STRING
    }

    /**
     * The key event processor current state.
     */
    private COMMAND_STATE m_command_state = COMMAND_STATE.IDLE;

    /**
     * State machine that processes the key events.
     *
     * @param type          The Key event type.
     * @param state_mask    The key event state mask.
     * @param key_code      The key event key code.
     * @param character     The key event character.
     *
     * @return True if the key event should be consumed, i.e. not passed on.
     */
    private boolean key_event_processor( int type,
                                         int state_mask,
                                         int key_code,
                                         char character )
    {
        String key = KeyLookupFactory.getDefault().formalNameLookup( key_code );
        boolean key_is_modifier = KeyLookupFactory.getDefault().isModifierKey( key_code );
        boolean is_printable = Activator.is_printable( key_code, state_mask );

        // The state machine can advance itself if need be.
        boolean loop = false;

        do
        {
            // Only allow one recursive loop.
            loop = false;

            switch( m_command_state )
            {
                /**
                 * When idle, looking for either of the following.
                 * 1) A number to modify the count.
                 * 2) A [Ctrl], [Alt], or [Shift] plus another key, or other non-printable character,
                 *    indicating a command to repeat.
                 * 3) A non-numerical character that will begin a string to repeat.
                 * 4) A right arrow key indicating acceptance of the current count with a string
                 *    or command following. This is to allow the repeat of a numerical value at
                 *    the beginning of the string, but that is not required.
                 * 5) Another [Ctrl]+R, according to the spec, doubles the command count.
                 *
                 * A command will start repeating immediately after typing it, while the string
                 * will require clicking the "OK" button to indicate the repeating string is complete.
                 */
                case IDLE:
                default:
                {
                    // Check for key combination with a modifier, a modified key sequence. But ignore multiple modifiers.
                    if( ( ( state_mask & SWT.MODIFIER_MASK ) != 0 ) && ( !key_is_modifier ) )
                    {
                        // Look for the [Ctrl]+R sequence.
                        /**
                         * TODO:
                         * Wish I did not have to hardcode the key bindings for the marking modes,
                         * but not sure any way around it.
                         * Could query the bindings, but what if there are multiple bindings to watch for.
                         * Seems like more work that it is worth...
                         */
                        if( ( ( state_mask & SWT.MOD1) == SWT.MOD1 ) &&
                                key.equalsIgnoreCase( Messages.command_dialog_repeat_command_key_binding ) )
                        {
                            // Double the count.
                            m_count *= 2;
                            if( m_count > MAX_COMMAND_COUNT ) m_count = MAX_COMMAND_COUNT;

                            String message = String.format( m_command_prompt_format, m_count, m_command_prompt_initial_instructions );
                            get_text().setText( message );

                            // Highlight the command count.
                            int start = m_command_prompt_format.indexOf( "%d" );  //$NON-NLS-1$
                            int end = start + String.format( "%d", m_count ).length(); //$NON-NLS-1$
                            get_text().setSelection( start, end );

                            return true;
                        }
                        // Otherwise, assuming this is the command to repeat.
                        else
                        {
                            get_text().setSelection( m_command_prompt_format.indexOf( "%d" ) + //$NON-NLS-1$
                                                         String.format( "%d", m_count ).length() ); //$NON-NLS-1$
                            m_command_state = COMMAND_STATE.FORM_COMMAND_STRING;
                            loop = true;

                            break;
                        }
                    }

                    // Otherwise, not a modified key combo, so it is either a single modifier key,
                    // a single digit, a single command, or other single non-digit character.

                    // Ignore the single modifiers, num-lock and caps-lock.
                    if( ( !key_is_modifier ) &&
                            !( key_code == SWT.NUM_LOCK ) && !( key_code == SWT.CAPS_LOCK ) )
                    {
                        if( key_code == SWT.ARROW_RIGHT )
                        {
                            // The current command count has been accepted by the user.
                            m_command_state = COMMAND_STATE.WAIT_FOR_COMMAND_OR_STRING_START;
                            String message = String.format( m_command_prompt_format, m_count, "" ); //$NON-NLS-1$
                            get_text().setText( message );
                            get_text().setSelection( m_command_prompt_format.indexOf( "%s" ) - 2 + //$NON-NLS-1$
                                                         String.format( "%d", m_count ).length() ); //$NON-NLS-1$
                            return true;
                        }

                        if( Character.isDigit( character ) )
                        {
                            // This is a digit so increase the command count.
                            if( m_count == 1 )
                            {
                                m_count = Integer.parseInt( Character.toString( character ) );
                            }
                            else
                            {
                                m_count *= 10;
                                m_count += Integer.parseInt( Character.toString( character ) );
                            }

                            if( m_count > MAX_COMMAND_COUNT )
                            {
                                m_count = MAX_COMMAND_COUNT;
                            }

                            String message = String.format( m_command_prompt_format, m_count, m_command_prompt_initial_instructions );
                            get_text().setText( message );
                            get_text().setSelection( m_command_prompt_format.indexOf( "%d" ) + //$NON-NLS-1$
                                                         String.format( "%d", m_count ).length() ); //$NON-NLS-1$

                            return true;
                        }

                        if( !is_printable )
                        {
                            get_text().setSelection( m_command_prompt_format.indexOf( "%d" ) + //$NON-NLS-1$
                                                 String.format( "%d", m_count ).length() ); //$NON-NLS-1$
                            m_command_state = COMMAND_STATE.FORM_COMMAND_STRING;
                            loop = true;

                            break;
                        }

                        m_command_state = COMMAND_STATE.ACCUMULATE_STRING;
                        get_text().addModifyListener( m_text_listener );

                        loop = true;
                        break;
                    }

                    return false;
                }

                case FORM_COMMAND_STRING:
                {
                    // Ignore num-lock and caps-lock.
                    if( ( key_code == SWT.NUM_LOCK ) || ( key_code == SWT.CAPS_LOCK ) ) return false;

                    if( ( state_mask & SWT.MOD1 ) == SWT.MOD1 )
                    {
                        m_command.append( Messages.command_dialog_mod1_plus_key );
                    }

                    if( ( state_mask & SWT.MOD3 ) == SWT.MOD3 )
                    {
                        m_command.append( Messages.command_dialog_mod3_plus_key );
                    }

                    if( ( state_mask & SWT.MOD2 ) == SWT.MOD2 )
                    {
                        m_command.append( Messages.command_dialog_mod2_plus_key );
                    }

                    m_command.append( key.toUpperCase() );

                    m_command_codes = new int[]{ state_mask, key_code };

                    String message = String.format( m_command_prompt_format,
                                                    m_count,
                                                    m_command );
                    get_text().setText( message );
                    get_text().setSelection( message.length() );

                    // A modifier plus key is assumed to be a complete command.
                    setReturnCode( OK );
                    m_command_state = COMMAND_STATE.IDLE;

                    // Set close timer so user is able to see the command.
                    set_close_timer( 350 );

                    return true;
                }

                case WAIT_FOR_COMMAND_OR_STRING_START:
                {
                    // Ignore num-lock and caps-lock.
                    if( ( key_code == SWT.NUM_LOCK ) || ( key_code == SWT.CAPS_LOCK ) ) return false;

                    // Check for key combination with a modifier, a modified key sequence.
                    if( ( state_mask & SWT.MODIFIER_MASK ) != 0 )
                    {
                        get_text().setSelection( m_command_prompt_format.indexOf( "%d" ) + //$NON-NLS-1$
                                                     String.format( "%d", m_count ).length() ); //$NON-NLS-1$
                        m_command_state = COMMAND_STATE.FORM_COMMAND_STRING;
                        loop = true;

                        break;
                    }
                    // Otherwise, not a modified key combo, so it is either a single modifier key,
                    // a single digit, or other single non-digit character.
                    else
                    {
                        // Ignore the single modifiers.
                        if( !key_is_modifier )
                        {
                            m_command_state = COMMAND_STATE.ACCUMULATE_STRING;
                            get_text().addModifyListener( m_text_listener );

                            loop = true;
                            break;
                        }

                        return true;
                    }
                }

                case ACCUMULATE_STRING:
                {
                    String prefix = String.format( m_command_prompt_format, m_count, "" );

                    // Don't allow backspace beyond the prompt.
                    if( key.equalsIgnoreCase( IKeyLookup.BS_NAME ) ||
                          key.equalsIgnoreCase( IKeyLookup.BACKSPACE_NAME ) )
                    {
                        if( get_text().getText().length() <= prefix.length() )
                        {
                            get_text().setSelection( prefix.length() );
                            return true;
                        }
                    }

                    // Don't allow cursor ahead of prompt.
                    if( get_text().getCaretPosition() < prefix.length() )
                    {
                        get_text().setSelection( prefix.length() );
                    }

                    return false;
                }
            }
        }
        while( loop );

        return true;
    }

    /**
     * Calls close method after the requested delay, so the user can see the command.
     *
     * @param delay_ms  The number of milliseconds to before closing.
     */
    private void set_close_timer( int delay_ms )
    {
        getShell().getDisplay().timerExec( delay_ms, new Runnable()
        {
            @Override
            public void run()
            {
                close();
            }
        } );
    }
}
