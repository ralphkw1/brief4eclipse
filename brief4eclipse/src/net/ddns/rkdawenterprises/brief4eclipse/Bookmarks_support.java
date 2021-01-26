
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.service.prefs.Preferences;

/**
 * Bookmark functionality.
 */
public class Bookmarks_support
{
    private Text_editor_proxy m_text_editor_proxy = null;

    /**
     * Must call dispose() to store bookmarks to non-volatile preferences storage.
     *
     * @param text_editor_proxy
     */
    protected Bookmarks_support( Text_editor_proxy text_editor_proxy )
    {
        m_text_editor_proxy = text_editor_proxy;
        get_stored_bookmarks();
    }

    /**
     * Must be called to store bookmarks in non-volatile storage.
     */
    protected void dispose()
    {
        store_bookmarks();
    }

    /**
     * Obtains the file path of the given editor.
     *
     * @param editor    The editor to obtain the path from.
     *
     * @return  The file path of the given editor.
     */
    private String get_editor_file_path( IEditorPart editor )
    {
        IEditorInput editor_input = editor.getEditorInput();
        if( editor_input == null ) return null;

        return get_editor_file_path( editor_input );
    }

    /**
     * Obtains the file path of the editor input.
     *
     * @param editor_input  The editor input to obtain the path from.
     *
     * @return  The file path of the editor input.
     */
    private String get_editor_file_path( IEditorInput editor_input )
    {
        if( editor_input instanceof FileEditorInput )
        {
            IPath path = ( (FileEditorInput) editor_input ).getPath();
            return( path.toPortableString() );
        }

        if( editor_input instanceof FileStoreEditorInput )
        {
            // Editor is not part of a project.
            URI uri = ( (FileStoreEditorInput)editor_input ).getURI();
            URL url = null;
            try
            {
                url = uri.toURL();
            }
            catch( MalformedURLException | IllegalArgumentException exception )
            {
                Activator.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": ", exception ); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }

            String path = url.getPath();
            File file = new File( path );
            return( file.getAbsolutePath() );
        }

        return null;
    }

    /**
     * The first bookmark number.
     */
    public static final int MIN_BOOKMARK_NUMBER = 1;

    /**
     * The last bookmark number.
     */
    public static final int MAX_BOOKMARK_NUMBER = 10;

    /**
     * The list of bookmarks.
     */
    private LinkedList<Bookmark_info> m_bookmarks = null;

    /**
     * Represents a bookmark.
     */
    public static class Bookmark_info implements java.io.Serializable
    {
        private static final long serialVersionUID = 7082009511901719744L;

        /**
         * The bookmark's number (1-10).
         */
        protected int m_number = -1;

        /**
         * The bookmark's caret/character offset in the editor.
         */
        protected int m_offset = -1;

        /**
         * The bookmark's location as a line and column position. Used for display purposes only.
         */
        protected String m_line_column = null;

        /**
         * Identifies the editor. Usually it is the fully qualified path.
         */
        protected String m_resource = null;

        /**
         * Create a bookmark.
         *
         * @param location  The cursor location/offset in the file.
         * @param resource  The file path.
         * @param line_column   The line index and column index of the bookmark.
         * @param number    The bookmark number. Currently 1-10.
         *
         * @throws IllegalArgumentException
         */
        public Bookmark_info( int location,
                              String resource,
                              String line_column,
                              int number ) throws IllegalArgumentException
        {
            m_offset = location;
            m_resource = resource;
            m_line_column = line_column;

            if( ( number < MIN_BOOKMARK_NUMBER ) || ( number > MAX_BOOKMARK_NUMBER ) )
            {
                throw( new IllegalArgumentException( " Bookmark number " + number + " is out of range." ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }

            m_number = number;
        }

        /**
         * De-serializes a bookmark.
         *
         * @param serialized_object     The serialized bookmark.
         *
         * @return  The bookmark de-serialized.
         */
        public static Bookmark_info deserialize( String serialized_object )
        {
            byte bytes[] = Base64.getDecoder().decode( serialized_object.getBytes() );
            ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
            ObjectInputStream ois = null;
            try
            {
                ois = new ObjectInputStream( bais );
                Bookmark_info bmi = (Bookmark_info)ois.readObject();

                if( ( bmi.m_number < MIN_BOOKMARK_NUMBER ) || ( bmi.m_number > MAX_BOOKMARK_NUMBER ) ) return null;

                return( bmi );
            }
            catch( IOException | ClassNotFoundException e )
            {
                return null;
            }
        }

        /**
         * Serializes a bookmark.
         *
         * @param object_to_serialize   The bookmark to serialize.
         *
         * @return  The serialized bookmark.
         */
        public static String serialize( Bookmark_info object_to_serialize )
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
     * Gets the stored bookmarks from preferences.
     */
    private void get_stored_bookmarks()
    {
        m_bookmarks = new LinkedList<Bookmark_info>();

        Preferences preferences = InstanceScope.INSTANCE.getNode( Activator.PLUGIN_ID );
        if( preferences == null ) return;

        Preferences bookmarks = preferences.node( "bookmarks" ); //$NON-NLS-1$

        for( int number = MIN_BOOKMARK_NUMBER; number <= MAX_BOOKMARK_NUMBER; number++ )
        {
            String bookmark_serialized;
            bookmark_serialized = bookmarks.get( String.valueOf( number ), "" ); //$NON-NLS-1$
            if( bookmark_serialized.equals( "" ) ) continue; //$NON-NLS-1$

            Bookmark_info bmi = Bookmark_info.deserialize( bookmark_serialized );
            if( bmi == null )
            {
                bookmarks.remove( String.valueOf( number ) );
                continue;
            }

            if( ( bmi.m_resource == null ) || ( bmi.m_line_column == null ) ||
                    ( bmi.m_offset == -1 ) || ( bmi.m_number == -1 ) )
            {
                bookmarks.remove( String.valueOf( number ) );
                continue;
            }

            if( m_bookmarks.indexOf( bmi ) == -1 )
            {
                m_bookmarks.add( bmi );
            }
        }
    }

    @SuppressWarnings( "unused" )
    private void print_bookmarks()
    {
        StringBuilder message = new StringBuilder();
        boolean no_bookmarks = true;
        for( Bookmark_info bmi : m_bookmarks )
        {
            no_bookmarks = false;
            message.append( "\tBookmark " ); //$NON-NLS-1$
            message.append( bmi.m_number );
            message.append( " = [" + bmi.m_resource + //$NON-NLS-1$
                            ", " + bmi.m_offset + //$NON-NLS-1$
                            ", " + bmi.m_line_column + "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Activator.system_message( "get_stored_bookmarks:\n" + ( no_bookmarks ? //$NON-NLS-1$
                "\tno stored bookmarks" : message.toString() ) ); //$NON-NLS-1$
    }

    /**
     * Gets a bookmark for the given bookmark number.
     *
     * @param number    The bookmark number.
     *
     * @return  The bookmark with the given number or null if it does not exist.
     */
    private Bookmark_info get_bookmark( int number )
    {
        if( ( number < MIN_BOOKMARK_NUMBER ) || ( number > MAX_BOOKMARK_NUMBER ) ) return null;

        for( Bookmark_info bmi : m_bookmarks )
        {
            if( bmi.m_number == number ) return bmi;
        }

        return null;
    }

    /**
     * Creates a new bookmark for the given bookmark number, or modifies the existing one.
     * Places it in the bookmark list.
     *
     * @param number        The bookmark number.
     * @param location      The bookmarks location/offset in the file.
     * @param resource      The file path of the bookmark.
     * @param line_column   The line/column of the bookmark.
     */
    private void store_bookmark( int number,
                                 int location,
                                 String resource,
                                 String line_column )
    {
        Bookmark_info bmi = get_bookmark( number );
        if( bmi != null )
        {
            bmi.m_line_column = line_column;
            bmi.m_offset = location;
            bmi.m_resource = resource;

            return;
        }

        bmi = new Bookmark_info( location, resource, line_column, number );
        m_bookmarks.add( bmi );
    }

    /**
     * Stores the bookmarks in preferences non-volatile storage.
     */
    private void store_bookmarks()
    {
        Preferences preferences = InstanceScope.INSTANCE.getNode( Activator.PLUGIN_ID );
        if( preferences == null ) return;

        Preferences bookmarks = preferences.node( "bookmarks" ); //$NON-NLS-1$

        for( int number = MIN_BOOKMARK_NUMBER; number <= MAX_BOOKMARK_NUMBER; number++ )
        {
            Bookmark_info bmi = get_bookmark( number );
            if( bmi != null )
            {
                String bookmark_serialized = Bookmark_info.serialize( bmi );

                // Make sure the value has changed before committing it to storage.
                String stored_bookmark_serialized = bookmarks.get( String.valueOf( number ), "" ); //$NON-NLS-1$

                if( !bookmark_serialized.equals( stored_bookmark_serialized ) )
                {
                    bookmarks.put( String.valueOf( number ), bookmark_serialized );
                }
            }
            else
            {
                // No bookmark for this number, so clear the key.
                bookmarks.remove( String.valueOf( number ) );
            }
        }
    }

    /**
     * Creates a bookmark in the given editor at the current cursor/caret location.
     *
     * @param number    The bookmark number.
     * @param editor     The editor.
     *
     * @throws IllegalArgumentException
     */
    protected void drop_bookmark( int number,
                                  IEditorPart editor ) throws IllegalArgumentException
    {
        if( ( number < MIN_BOOKMARK_NUMBER ) || ( number > MAX_BOOKMARK_NUMBER ) )
        {
            throw( new IllegalArgumentException( " Bookmark number " + number + " is out of range." ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Using the path to identify the editor later. Probably a better way but not sure what.
        String path = get_editor_file_path( editor );
        if( path == null ) return;

        int current_caret_offset = m_text_editor_proxy.get_model_current_caret_offset();
        if( current_caret_offset == -1 ) return;
        int current_line_index = m_text_editor_proxy.get_model_current_line_index();
        if( current_line_index == -1 ) return;
        int caret_offset_at_current_line = m_text_editor_proxy.get_model_caret_offset_at_current_line();
        if( caret_offset_at_current_line == -1 ) return;

        int current_column = current_caret_offset - caret_offset_at_current_line;

        StringBuilder line_column = new StringBuilder();
        line_column.append( current_line_index + 1 );
        line_column.append( Messages.bookmarks_support_18 );
        line_column.append( current_column + 1 );

        store_bookmark( number,
                        current_caret_offset,
                        path,
                        line_column.toString() );
    }

    /**
     * Opens a dialog to obtain a bookmark number, then jumps to it.
     *
     * @param workbench_window
     */
    protected void jump_bookmark( IWorkbenchWindow workbench_window )
    {
        if( m_bookmarks.size() == 0 )
        {
            Activator.set_status_line( Messages.bookmarks_support_19 );
            return;
        }

        Get_a_number_dialog dialog = new Get_a_number_dialog( Activator.get_shell(),
                                                              m_text_editor_proxy.get_active_editor(),
                                                              MIN_BOOKMARK_NUMBER,
                                                              MIN_BOOKMARK_NUMBER,
                                                              MAX_BOOKMARK_NUMBER,
                                                              Messages.jump_bookmark_number_dialog_prompt_format );
        if( dialog.open() == Window.OK )
        {
            int number = dialog.get_number();

            Bookmark_info bmi = get_bookmark( number );
            if( bmi != null )
            {
                find_bookmark_and_goto_it( workbench_window, bmi );
            }
        }
    }

    /**
     * Based on the given bookmark, finds the matching editor, activates it,
     * and jumps to the model offset.
     *
     * @param workbench_window  The current workbench window.
     * @param bmi   The bookmark to locate and go to.
     */
    private void find_bookmark_and_goto_it( IWorkbenchWindow workbench_window,
                                            Bookmark_info bmi )
    {
        IWorkbench workbench = workbench_window.getWorkbench();

        IEditorReference[] editor_references =
                workbench.getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        for( IEditorReference editor_reference : editor_references )
        {
            try
            {
                IEditorInput editor_input = editor_reference.getEditorInput();

                String path = get_editor_file_path( editor_input );
                if( path == null ) continue;

                if( path.equals( bmi.m_resource ) )
                {
                    IEditorPart editor_part = editor_reference.getEditor( true );
                    editor_reference.getPage().activate( editor_part );

                    IEditorPart active_editor = Activator.get_active_editor( workbench_window );
                    m_text_editor_proxy.set_editor( workbench_window, active_editor );

                    m_text_editor_proxy.set_model_current_caret_offset( bmi.m_offset );
                }
            }
            catch( Exception exception )
            {
                Activator.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": ", exception ); //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            }
        }
    }

    /**
     * Opens a dialog of existing bookmarks and jumps to the selected bookmark if requested.
     *
     * @param workbench_window  The current workbench window.
     * @param max_width_in_chars    The maximum width of the dialog.
     */
    protected void open_bookmarks_dialog( IWorkbenchWindow workbench_window,
                                          int max_width_in_chars )
    {
        if( m_bookmarks.size() == 0 )
        {
            Activator.set_status_line( Messages.bookmarks_support_22 );
            return;
        }

        IEditorPart active_editor = m_text_editor_proxy.get_active_editor();
        if( active_editor == null ) return;

        Bookmark_dialog dialog = new Bookmark_dialog( workbench_window.getShell(),
                                                      active_editor,
                                                      m_bookmarks,
                                                      max_width_in_chars );

        int selected_bookmark_number = -1;
        if( dialog.open() == Window.OK )
        {
            selected_bookmark_number = dialog.get_selected_bookmark_number();
        }

        IWorkbenchPage workbench_page = workbench_window.getActivePage();
        if( workbench_page == null ) return;
        workbench_page.activate( active_editor );

        if( selected_bookmark_number == -1 ) return;

        Bookmark_info bmi = get_bookmark( selected_bookmark_number );
        if( bmi != null )
        {
            find_bookmark_and_goto_it( workbench_window, bmi );
        }
    }
}
