
/***************************************************************************//**
 * Copyright (c) 2020 Ralph and Donna Williamson and RKDAW Enterprises
 * <rkdawenterprises.ddns.net>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0.
 ******************************************************************************/

package net.ddns.rkdawenterprises.briefforeclipse;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Brief_for_eclipse extends AbstractUIPlugin
{
    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "net.ddns.rkdawenterprises.briefforeclipse"; //$NON-NLS-1$

    /**
     * The shared instance.
     */
    private static Brief_for_eclipse m_plugin = null;

    /**
     *  Handles the operations for the different editors.
     */
    Text_editor_proxy m_text_editor_proxy = null;

    /**
     * For displaying status messages to the user.
     */
    private IStatusLineManager m_status_line_manager = null;

    @Override
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        m_plugin = this;

        m_status_line_manager =
                get_active_editor( getWorkbench().getActiveWorkbenchWindow() ).
                getEditorSite().getActionBars().getStatusLineManager();

        try
        {
            m_text_editor_proxy = new Text_editor_proxy( getWorkbench().getActiveWorkbenchWindow(),
                                                         null );
        }
        catch( Exception exception ) {}
    }

    @Override
    public void stop( BundleContext context ) throws Exception
    {
        m_text_editor_proxy.dispose();
        m_text_editor_proxy = null;
        m_plugin = null;
        super.stop( context );
    }

    /**
     * Returns the shared instance.
     *
     * @return The shared instance.
     */
    public static Brief_for_eclipse getDefault()
    {
        return m_plugin;
    }

    /**
     * Returns this workbench window's shell.
     */
    public static Shell get_shell()
    {
        return( getDefault().getWorkbench().getActiveWorkbenchWindow().getShell() );
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative
     * path.
     *
     * @param path The path.
     * @return The image descriptor.
     */
    public static ImageDescriptor getImageDescriptor( String path )
    {
        return imageDescriptorFromPlugin( PLUGIN_ID,
                                          path );
    }

    /**
     * For debugging back to developer console.
     *
     * @param message The message to log.
     */
    public static void log_info( String message )
    {
        getDefault().getLog().log(
                new Status( IStatus.INFO,
                            getDefault().getBundle().getSymbolicName(),
                            IStatus.INFO,
                            message,
                            null ) );
    }

    /**
     * For debugging back to developer console.
     *
     * @param message The message to log.
     */
    public static void log_error( String message )
    {
        getDefault().getLog().log(
                new Status( IStatus.ERROR,
                            getDefault().getBundle().getSymbolicName(),
                            IStatus.ERROR,
                            message,
                            null ) );
    }

    /**
     * For debugging back to developer console.
     *
     * @param message The message to log.
     * @param e The exception to log.
     */
    public static void log_error( String message, Throwable e )
    {
        getDefault().getLog().log( new Status( IStatus.ERROR,
                                               getDefault().getBundle().getSymbolicName(),
                                               IStatus.ERROR,
                                               message,
                                               e ) );
    }

    /**
     * Sets the status line with a message.
     *
     * @param message   The message to display on the status line.
     */
    public static void set_status_line( String message )
    {
        getDefault().m_status_line_manager.setMessage( message );
    }

    /**
     * For debugging back to developer console.
     *
     * @param message   The message to show in the console.
     */
    public static void system_message( String message )
    {
        System.out.println( message );
    }

    /**
     * Sounds a beep to the user.
     */
    public static void beep()
    {
        getDefault().getWorkbench().getDisplay().beep();
    }

    /**
     * Extract the first line of the given string using the standard delimiters.
     * If the given string is only one line, then just returns the string.
     *
     * @param string    The given string.
     *
     * @return  The first line of the string.
     */
    public static String get_first_line( String string )
    {
        if( string.length() <= 0 ) return string;

        int[] match = TextUtilities.indexOf( TextUtilities.DELIMITERS, string, 0 );

        if ( match[0] == -1 ) return string;

        if( match[0] > 0 ) return string.substring( 0, match[0] );

        return string;
    }

    public static void print_key_event( String key,
                                        boolean is_printable,
                                        int state_mask,
                                        int key_code,
                                        char character )
    {
        Brief_for_eclipse.system_message( ">>>>: " + key + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                ( is_printable ? "printable" : "non-printable" ) + ", " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "state_mask=" + Integer.toHexString( state_mask )  + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                "key_code=" + Integer.toHexString( key_code ) + ", " + //$NON-NLS-1$ //$NON-NLS-2$
                "character=" + character ); //$NON-NLS-1$
    }

    /**
     * Execute command with the given ID in dot notation using the Handler Service
     * for the workbench.
     *
     * @param command_id The identifier of the command to execute
     */
    private boolean execute_command_id( String command_id )
    {
        IHandlerService handler_service =
                (IHandlerService)PlatformUI.getWorkbench().getService( IHandlerService.class );

        try
        {
            handler_service.executeCommand( command_id, null );
            return false;
        }
        catch( Exception e )
        {
            Brief_for_eclipse.log_error( "brief_for_eclipse.execute_command_id(): Command <" + command_id + "> not found.", e ); //$NON-NLS-1$ //$NON-NLS-2$
            return true;
        }
    }

    /**
     * Saves all unsaved files and then exits the IDE.
     */
    private void write_all_and_exit()
    {
        execute_command_id( IWorkbenchCommandConstants.FILE_SAVE_ALL );
        execute_command_id( IWorkbenchCommandConstants.FILE_EXIT );
    }

    /**
     * Sends commands to the text editor proxy.
     *
     * @param cmd               The command event. These are all the commands defined in plugin.xml.
     * @param workbench_window  The event's current workbench window.
     */
    private void text_editor_proxy_operations( Command cmd,
                                               IWorkbenchWindow workbench_window )
    {
        if( m_text_editor_proxy == null )
        {
            try
            {
                m_text_editor_proxy = new Text_editor_proxy( getWorkbench().getActiveWorkbenchWindow(),
                                                             get_active_editor( workbench_window ) );
            }
            catch( Exception exception ) {}
        }

        if( m_text_editor_proxy == null ) return;

        try
        {
            m_text_editor_proxy.set_editor( workbench_window, get_active_editor( workbench_window ) );
        }
        catch( Exception exception )
        {
            Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + exception ); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        m_status_line_manager =
                get_active_editor( getWorkbench().getActiveWorkbenchWindow() ).
                getEditorSite().getActionBars().getStatusLineManager();

        // Process the command.
        switch( cmd.getId() )
        {
            case "net.ddns.rkdawenterprises.briefforeclipse.commands.line_marking_mode_toggle": //$NON-NLS-1$
            {
                m_text_editor_proxy.line_marking_mode_toggle();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.numpad_copy": //$NON-NLS-1$
            {
                m_text_editor_proxy.numpad_copy();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.numpad_cut": //$NON-NLS-1$
            {
                m_text_editor_proxy.numpad_cut();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.insert_paste": //$NON-NLS-1$
            {
                m_text_editor_proxy.insert_paste();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.insert_swap": //$NON-NLS-1$
            {
                m_text_editor_proxy.insert_swap();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.column_marking_mode_toggle": //$NON-NLS-1$
            {
                m_text_editor_proxy.column_marking_mode_toggle();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.marking_mode_toggle": //$NON-NLS-1$
            {
                m_text_editor_proxy.marking_mode_toggle();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.repeat": //$NON-NLS-1$
            {
                m_text_editor_proxy.repeat_command_or_string();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.scroll_buffer_up": //$NON-NLS-1$
            {
                m_text_editor_proxy.scroll_buffer( 1 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.scroll_buffer_down": //$NON-NLS-1$
            {
                m_text_editor_proxy.scroll_buffer( -1 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.line_to_top_of_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.line_to_top_of_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.center_line_in_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.center_line_in_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.line_to_bottom_of_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.line_to_bottom_of_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.top_of_buffer": //$NON-NLS-1$
            {
                m_text_editor_proxy.top_of_buffer();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.bottom_of_buffer": //$NON-NLS-1$
            {
                m_text_editor_proxy.bottom_of_buffer();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.top_of_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.top_of_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.end_of_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.end_of_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.left_side_of_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.left_side_of_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.right_side_of_window": //$NON-NLS-1$
            {
                m_text_editor_proxy.right_side_of_window();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.home": //$NON-NLS-1$
            {
                m_text_editor_proxy.home();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.end": //$NON-NLS-1$
            {
                m_text_editor_proxy.end();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.virtual_caret_mode_toggle": //$NON-NLS-1$
            {
                m_text_editor_proxy.virtual_caret_mode_toggle();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.go_to_line": //$NON-NLS-1$
            {
                m_text_editor_proxy.go_to_line();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark1": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 1 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark2": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 2 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark3": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 3 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark4": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 4 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark5": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 5 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark6": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 6 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark7": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 7 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark8": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 8 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark9": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 9 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.drop_bookmark10": //$NON-NLS-1$
            {
                m_text_editor_proxy.drop_bookmark( 10 );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.jump_bookmark": //$NON-NLS-1$
            {
                m_text_editor_proxy.jump_bookmark();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.open_bookmarks_dialog": //$NON-NLS-1$
            {
                m_text_editor_proxy.open_bookmarks_dialog();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.replace_next": //$NON-NLS-1$
            {
                m_text_editor_proxy.replace_next_previous( true );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.replace_previous": //$NON-NLS-1$
            {
                m_text_editor_proxy.replace_next_previous( false );
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.open_scrap_dialog": //$NON-NLS-1$
            {
                m_text_editor_proxy.open_scrap_dialog();
                return;
            }

            case "net.ddns.rkdawenterprises.briefforeclipse.commands.rename": //$NON-NLS-1$
            {
                m_text_editor_proxy.change_output_file_name();
                return;
            }

            default:
            {
                return;
            }
        }
    }

    /**
     * Handles the key events from the Key Binding Handler.
     * Most of them are handled by the text editor proxy, otherwise handled here.
     *
     * @param event     The key event.
     *
     * @return          Always returns null.
     *
     * @throws ExecutionException
     */
    Object execute( ExecutionEvent event ) throws ExecutionException
    {
        // Initialize the proxy for the editor associated with this event.
        IWorkbenchWindow workbench_window = HandlerUtil.getActiveWorkbenchWindowChecked( event );
        if( workbench_window == null ) return null;

        // Process the command.
        Command cmd = event.getCommand();
        switch( cmd.getId() )
        {
            case "net.ddns.rkdawenterprises.briefforeclipse.commands.write_all_and_exit": //$NON-NLS-1$
            {
                write_all_and_exit();
                return null;
            }

            default:
            {
                text_editor_proxy_operations( cmd,
                                              workbench_window );
                return null;
            }
        }
    }

    /**
     * Gets the given workbench window's currently active editor.
     *
     * @param window    The workbench window.
     *
     * @return  The currently active editor.
     */
    public static IEditorPart get_active_editor( IWorkbenchWindow window )
    {
        if( window != null )
        {
            IWorkbenchPage page = window.getActivePage();
            if( page != null )
            {
                return( page.getActiveEditor() );
            }
        }

        return null;
    }

    /**
     * Determines if the given key is not a control type key, i.e. printable.
     *
     * @param key_code      The key code from an SWT key event.
     * @param state_mask    The state mask from an SWT key event.
     *
     * @return True if printable, false otherwise.
     */
    public static boolean is_printable( int key_code, int state_mask )
    {
        if( ( state_mask & SWT.MODIFIER_MASK ) != 0 ) return false;

        String key = KeyLookupFactory.getDefault().formalNameLookup( key_code );
        boolean key_is_modifier = KeyLookupFactory.getDefault().isModifierKey( key_code );

        if( key_is_modifier ) return false;

        // Check if this is a number pad key. They are printable.
        if( key.startsWith( "NUMPAD_" ) ) return true; //$NON-NLS-1$

        // Check if this is declared a "keycode". Keycodes are generally not printable.
        if( ( key_code & SWT.KEYCODE_BIT ) != 0 ) return false;

        int type = Character.getType( key_code & SWT.KEY_MASK );
        switch( type )
        {
            case Character.CONTROL:     // \p{Cc}
            case Character.FORMAT:      // \p{Cf}
            case Character.PRIVATE_USE: // \p{Co}
            case Character.SURROGATE:   // \p{Cs}
            case Character.UNASSIGNED:  // \p{Cn}
            {
                return false;
            }

            default:
            {
                return true;
            }
        }
    }

    /**
     * Converts a multi-line string to single line by replacing line separator with the string representation,
     * i.e. [ SWT.CR / CR ('\r') ] , [ SWT.LF / LF ('\n') ], or [ SWT.CR + SWT.LF / CR+LF ("\r\n") ].
     * Then truncates it to the given max length.
     *
     * @param text                  The string to convert and truncate.
     * @param column_max_chars      The length to truncate to.
     * @param end_not_beginning     Truncate the end of the string if <i>true</i>, otherwise the beginning.
     *
     * @return  The modified string.
     */
    public static String truncate_elipsis( String text,
                                           int column_max_chars,
                                           boolean beginning_not_end )
    {
        //  Replace line delimiter with a literal line delimiter string.
        text = text.replace( Messages.brief_for_eclipse_13, Messages.brief_for_eclipse_14 );
        text = text.replace( Messages.brief_for_eclipse_15, Messages.brief_for_eclipse_16 );

        // Set the string length to less than the editor width in characters.
        if( text.length() > column_max_chars  )
        {
            String elipsis = Messages.brief_for_eclipse_17;
            column_max_chars -= elipsis.length();
            int corrected_max_width =
                    ( ( Character.isLowSurrogate( text.charAt( column_max_chars ) ) ) &&
                    ( column_max_chars > 0 ) ) ? column_max_chars - 1 : column_max_chars;
            corrected_max_width = Math.min( text.length(), corrected_max_width );

            if( beginning_not_end )
            {
                int offset = text.length() - corrected_max_width;
                text = elipsis + text.substring( offset, text.length() );
            }
            else
            {
                text = text.substring( 0, corrected_max_width ) + elipsis;
            }
        }

        return text;
    }
}
