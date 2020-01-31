
/***************************************************************************//**
 * Copyright (c) 2020 Ralph and Donna Williamson and RKDAW Enterprises
 * <rkdawenterprises.ddns.net>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0.
 ******************************************************************************/

package net.ddns.rkdawenterprises.briefforeclipse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.Preferences;

/**
 * The scrap buffer functionality. Accumulates a number of clipboard cut/copy items
 * and allows the user to paste from a history list.
 */
public class Scrap_support
{
    /**
     * Reference to the text editor proxy, which is the parent. Used to get the display and active editor.
     */
    private Text_editor_proxy m_text_editor_proxy = null;

    /**
     * The scrap buffer.
     */
    private Scrap_buffer m_scrap_buffer = null;

    /**
     * The information need for a scrap item.
     */
    public static class Scrap_item implements java.io.Serializable
    {
        private static final long serialVersionUID = -5076894655479640405L;

        /**
         * The text of the scrap item. May contain line delimiters.
         */
        protected String m_text = null;

        /**
         * The block size of column mode type scrap. Remains null if the scrap item is normal mode.
         */
        protected Point m_column_mode_size = null;

        /**
         * Indicates the scrap item is a column mode type.
         *
         * @return  True if the scrap item is column mode type.
         */
        public boolean is_column_content()
        {
            return ( m_column_mode_size != null );
        }

        /**
         * Creates a new scrap item.
         *
         * @param text              The selection text.
         * @param column_mode_size  The size of the selection if in column selection mode.
         *                          If not column selection mode, then MUST be null to
         *                          indicate normal or line selection mode.
         */
        protected Scrap_item( String text,
                              Point column_mode_size )
        {
            m_text = text;
            m_column_mode_size = column_mode_size;
        }

        /**
         * De-serializes a scrap item.
         *
         * @param serialized_object     The serialized scrap item.
         *
         * @return  The scrap item de-serialized.
         */
        public static Scrap_item deserialize( String serialized_object )
        {
            byte bytes[] = Base64.getDecoder().decode( serialized_object.getBytes() );
            ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
            ObjectInputStream ois = null;
            try
            {
                ois = new ObjectInputStream( bais );
                return( (Scrap_item)ois.readObject() );
            }
            catch( IOException | ClassNotFoundException e )
            {
                return null;
            }
        }

        /**
         * Serializes a scrap item.
         *
         * @param object_to_serialize   The scrap item to serialize.
         *
         * @return  The serialized scrap item.
         */
        public static String serialize( Scrap_item object_to_serialize )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            try
            {
                oos = new ObjectOutputStream( baos );
                oos.writeObject( object_to_serialize );
                oos.flush();
            }
            catch( IOException e )
            {
                return null;
            }

            return( new String( Base64.getEncoder().encode( baos.toByteArray() ) ) );
        }
    }

    /**
     * The buffer that contains the list of scrap items.
     */
    public static class Scrap_buffer implements java.io.Serializable
    {
        private static final long serialVersionUID = 7745123235443436292L;

        /**
         * The maximum number of scrap items in the scrap buffer.
         */
        private static final int MAX_ITEMS = 10;

        /**
         * The scrap buffer.
         */
        protected ArrayList<Scrap_item> m_item_list;

        /**
         * Creates the scrap buffer.
         */
        protected Scrap_buffer()
        {
            m_item_list = new ArrayList<Scrap_item>();
        }

        /**
         * De-serializes a scrap buffer.
         *
         * @param serialized_object     The serialized scrap buffer.
         *
         * @return  The scrap buffer de-serialized.
         */
        public static Scrap_support deserialize( String serialized_object )
        {
            byte bytes[] = Base64.getDecoder().decode( serialized_object.getBytes() );
            ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
            ObjectInputStream ois = null;
            try
            {
                ois = new ObjectInputStream( bais );
                return( (Scrap_support)ois.readObject() );
            }
            catch( IOException | ClassNotFoundException e )
            {
                return null;
            }
        }

        /**
         * Serializes a scrap buffer.
         *
         * @param object_to_serialize   The scrap buffer to serialize.
         *
         * @return  The serialized scrap buffer.
         */
        public static String serialize( Scrap_support object_to_serialize )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            try
            {
                oos = new ObjectOutputStream( baos );
                oos.writeObject( object_to_serialize );
                oos.flush();
            }
            catch( IOException e )
            {
                return null;
            }

            return( new String( Base64.getEncoder().encode( baos.toByteArray() ) ) );
        }
    }

    /**
     * Creates the scrap support functionality.
     * Must call dispose() to store scrap buffer to non-volatile storage.
     *
     * @param text_editor_proxy     The text editor proxy, which is the parent of this object.
     */
    protected Scrap_support( Text_editor_proxy text_editor_proxy )
    {
        m_text_editor_proxy = text_editor_proxy;

        get_stored_scrap();

        Display display = m_text_editor_proxy.get_current_display();
        if( display == null )
        {
            display = PlatformUI.getWorkbench().getDisplay();
        }

        /*
         * Call this first so latest clipboard item can be stored if it came from somewhere
         * outside of the plug-in. Have to assume it is not column mode content.
         */
        syncronize_with_system_clipboard( display, null );
    }

    /**
     * Called to save the scrap items to non-volitile storage.
     */
    protected void dispose()
    {
        put_stored_scrap();
    }

    /**
     * Loads the scrap items from non-volatile preferences storage.
     */
    private void get_stored_scrap()
    {
        m_scrap_buffer = new Scrap_buffer();

        Preferences preferences = InstanceScope.INSTANCE.getNode( Brief_for_eclipse.PLUGIN_ID );
        if( preferences == null ) return;

        Preferences scrap_items = preferences.node( "scrap_items" ); //$NON-NLS-1$

        String number_of_stored_items = scrap_items.get( "number_of_stored_items", "" ); //$NON-NLS-1$ //$NON-NLS-2$
        if( number_of_stored_items.equals( "" ) ) return; //$NON-NLS-1$

        int item_count = Integer.parseInt( number_of_stored_items );

        for( int number = 0; number < item_count; number++ )
        {
            String scrap_items_serialized;
            scrap_items_serialized = scrap_items.get( String.valueOf( number ), "" ); //$NON-NLS-1$
            if( scrap_items_serialized.equals( "" ) ) continue; //$NON-NLS-1$

            Scrap_item si = Scrap_item.deserialize( scrap_items_serialized );
            if( si == null )
            {
                scrap_items.remove( String.valueOf( number ) );
                continue;
            }

            if( si.m_text == null )
            {
                scrap_items.remove( String.valueOf( number ) );
                continue;
            }

            if( m_scrap_buffer.m_item_list.indexOf( si ) == -1 )
            {
                m_scrap_buffer.m_item_list.add( si );
            }
        }
    }

    /**
     * Stores the scrap items to non-volatile preferences storage.
     */
    private void put_stored_scrap()
    {
        Preferences preferences = InstanceScope.INSTANCE.getNode( Brief_for_eclipse.PLUGIN_ID );
        if( preferences == null ) return;

        Preferences scrap_items = preferences.node( "scrap_items" ); //$NON-NLS-1$

        int number_of_items = m_scrap_buffer.m_item_list.size();

        scrap_items.put( "number_of_stored_items", String.valueOf( number_of_items ) ); //$NON-NLS-1$

        for( int number = 0; number < number_of_items; number++ )
        {
            Scrap_item si = m_scrap_buffer.m_item_list.get( number );
            if( si != null )
            {
                String bookmark_serialized = Scrap_item.serialize( si );

                // Make sure the value has changed before committing it to storage.
                String stored_bookmark_serialized = scrap_items.get( String.valueOf( number ), "" ); //$NON-NLS-1$

                if( !bookmark_serialized.equals( stored_bookmark_serialized ) )
                {
                    scrap_items.put( String.valueOf( number ), bookmark_serialized );
                }
            }
            else
            {
                // No bookmark for this number, so clear the key.
                scrap_items.remove( String.valueOf( number ) );
            }
        }
    }

    /**
     * Gets the number of scrap items currently in the scrap buffer.
     *
     * @return  The number of scrap items currently in the scrap buffer.
     */
    protected int get_item_count()
    {
        return m_scrap_buffer.m_item_list.size();
    }

    /**
     * Indicates that the most recent scrap item is a column mode item.
     *
     * @return  True if the most recent scrap item is a column mode item.
     */
    protected boolean clipboard_is_column_content()
    {
        if( m_scrap_buffer.m_item_list.isEmpty() ) return false;
        return( ( m_scrap_buffer.m_item_list.get( 0 ) ).is_column_content() );
    }

    /**
     * The size of the most recent scrap item if it is a column mode item.
     *
     * @return  The size of the most recent scrap item if it is a column mode item.
     *          Otherwise returns null.
     */
    protected Point clipboard_column_mode_size()
    {
        if( m_scrap_buffer.m_item_list.isEmpty() ) return null;
        return( ( m_scrap_buffer.m_item_list.get( 0 ) ).m_column_mode_size );
    }

    /**
     * Simple interface to pass copy method to the scrap buffer so it can be called
     * when saving the clipboard item to the scrap buffer.
     */
    public interface i_editor_copy
    {
        /**
         * Copy the selected scrap item and add it to the scrap buffer.
         *
         * @return  The block size for column mode, or null for line or normal mode.
         */
        public Point copy();
    }

    /**
     * Performs the copy operation which adds the current clipboard to the scrap buffer
     * and calls the given copy method.
     *
     * @param display   The current display.
     * @param copy      The copy method to call.
     */
    protected void copy(  Display display,
                          i_editor_copy copy )
    {
        /*
         * Call this first so latest clipboard item can be stored if it came from somewhere
         * outside of the plug-in. Have to assume it is not column mode content.
         */
        syncronize_with_system_clipboard( display, null );

        // Copy callback.
        Point column_mode_size = copy.copy();

        // Get the copied item.
        syncronize_with_system_clipboard( display,
                                          column_mode_size );
    }

    /**
     * Simple interface to pass cut method to the scrap buffer so it can be called
     * when saving the clipboard item to the scrap buffer.
     */
    public interface i_editor_cut
    {
        /**
         * Cut the selected scrap item and add it to the scrap buffer.
         *
         * @return  The block size for column mode, or null for line or normal mode.
         */
        public Point cut();
    }

    /**
     * Performs the cut operation which adds the current clipboard to the scrap buffer
     * and calls the given cut method.
     *
     * @param display   The current display.
     * @param cut      The cut method to call.
     */
    protected void cut( Display display,
                        i_editor_cut cut )
    {
        /*
         * Call this first so latest clipboard item can be stored if it came from somewhere
         * outside of the plug-in. Have to assume it is not column mode content.
         */
        syncronize_with_system_clipboard( display, null );

        // Cut callback.
        Point column_mode_size = cut.cut();

        // Get the copied item.
        syncronize_with_system_clipboard( display,
                                          column_mode_size );
    }

    /**
     * Obtains the latest system clipboard item, and makes sure the scrap buffer has it.
     *
     * @param display           The current display.
     * @param column_mode_size  The size of the column mode block if column mode, otherwise -1.
     */
    protected void syncronize_with_system_clipboard( Display display,
                                                     Point column_mode_size )
    {
        final Clipboard clipboard = new Clipboard( display );

        String system_string = (String)clipboard.getContents( TextTransfer.getInstance() );
        if( system_string != null )
        {
            if( m_scrap_buffer.m_item_list.isEmpty() )
            {
                m_scrap_buffer.m_item_list.add( new Scrap_item( system_string, column_mode_size ) );
            }
            else
            {
                add_item_checked( system_string, column_mode_size );
            }
        }

        clipboard.dispose();

//        print_scrap_items();
    }

    /**
     * Adds the scrap information as an scrap item to the scrap buffer if it is new, otherwise
     * updates its location to the newest item. List index 0 is always the newest item.
     *
     * It first looks for the string in the scrap buffer. If it finds it, it removes the
     * scrap item from its previous location, then adds it back to index 0. It preserves
     * the column mode size value.
     *
     * If the string is not currently in the buffer, it just adds it with the given column
     * mode size value to index 0.
     *
     * If the buffer gets too many items, the oldest is deleted. The oldest item has index
     * of size() - 1.
     *
     * @param string            The scrap/clipboard item to add to the buffer or update as newest.
     * @param column_mode_size  The block size of the item to add if it is a column mode item,
     *                          or null if a line or normal mode item.
     */
    private void add_item_checked( String string,
                                   Point column_mode_size )
    {
        if( string == null ) return;

        // Remove then add back this item if it is already in the list so it will always be the most recent item.
        Scrap_item add_back_item = null;
        for( Iterator< Scrap_item > iterator = m_scrap_buffer.m_item_list.iterator(); iterator.hasNext(); )
        {
            Scrap_item si = iterator.next();
            if( string.equals( si.m_text ) )
            {
                add_back_item = si;
                iterator.remove();
            }
        }

        if( add_back_item == null )
        {
            m_scrap_buffer.m_item_list.add( 0, new Scrap_item( string,
                                                               column_mode_size ) );
        }
        else
        {
            m_scrap_buffer.m_item_list.add( 0, add_back_item );
        }

        while( m_scrap_buffer.m_item_list.size() > Scrap_buffer.MAX_ITEMS )
        {
            m_scrap_buffer.m_item_list.remove( m_scrap_buffer.m_item_list.size() - 1 );
        }
    }

    /**
     * Simple interface to pass paste method to the scrap buffer so it can be called
     * when pasting the clipboard item from the scrap buffer.
     */
    public interface i_editor_paste
    {
        /**
         * Paste the given scrap item from the scrap buffer.
         */
        public void paste( Scrap_item selected_scrap_item );
    }

    /**
     * Opens the scrap dialog. Shows the list of scrap items in the scrap buffer
     * and allows the user to delete them or paste a particular item.
     *
     * @param workbench_window      The current workbench window.
     * @param max_width_in_chars    The maximum width of the dialog, in characters, so it
     *                              can fit within the editor's window.
     * @param paste                 The interface to call the paste method.
     */
    public void open_scrap_dialog( IWorkbenchWindow workbench_window,
                                   int max_width_in_chars,
                                   i_editor_paste paste )
    {
        if( get_item_count() == 0 )
        {
            Brief_for_eclipse.set_status_line( Messages.scrap_support_9 );
            return;
        }

        IEditorPart active_editor = m_text_editor_proxy.get_active_editor();
        if( active_editor == null ) return;

        Scrap_dialog dialog = new Scrap_dialog( workbench_window.getShell(),
                                                active_editor,
                                                m_scrap_buffer,
                                                max_width_in_chars );

        Scrap_item selected_scrap_item = null;
        if( dialog.open() == Window.OK )
        {
            selected_scrap_item = dialog.get_selected_scrap_item();
        }

        IWorkbenchPage workbench_page = workbench_window.getActivePage();
        if( workbench_page == null ) return;
        workbench_page.activate( active_editor );

        if( selected_scrap_item == null ) return;

        if( ( selected_scrap_item != null ) && ( selected_scrap_item.m_text.length() > 0 ) )
        {
            paste.paste( selected_scrap_item );
        }

        return;
    }

    @SuppressWarnings( "unused" )
    private void print_scrap_items()
    {
        StringBuilder message = new StringBuilder();
        boolean no_scrap_items = true;
        for( Scrap_item si : m_scrap_buffer.m_item_list )
        {
            no_scrap_items = false;
            message.append( "\tScrap " ); //$NON-NLS-1$
            String text = si.m_text;
            text = text.replace( "\r", "\\r" ); //$NON-NLS-1$ //$NON-NLS-2$
            text = text.replace( "\n", "\\n" ); //$NON-NLS-1$ //$NON-NLS-2$
            message.append( " = [" + text + //$NON-NLS-1$
                            ", " + ( si.is_column_content() ? "column" : "normal" ) + "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        Brief_for_eclipse.system_message( "print_scrap_items:\n" + ( no_scrap_items ? //$NON-NLS-1$
                "\tno stored scrap items" : message.toString() ) ); //$NON-NLS-1$
    }
}
