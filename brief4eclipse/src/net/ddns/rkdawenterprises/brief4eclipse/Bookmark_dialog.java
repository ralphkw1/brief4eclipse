
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import net.ddns.rkdawenterprises.brief4eclipse.Abstract_table_dialog.Abstract_table_info;
import net.ddns.rkdawenterprises.brief4eclipse.Bookmarks_support.Bookmark_info;

public class Bookmark_dialog extends Abstract_table_info
{
    /**
     * Reference to the current list of bookmarks.
     */
    private LinkedList<Bookmark_info> m_bookmarks = null;

    /**
     * The maximum width of the dialog, in characters, to stay within the bounds of the editor.
     */
    private int m_max_width_in_chars;

    /**
     * Constructs the text dialog instance with the given parent shell.
     *
     * @param parent_shell The parent shell.
     * @param editor
     */
    /**
     *
     * @param parent_shell  The parent shell.
     * @param editor        The active editor the dialog should act upon.
     * @param bookmarks     The current list of bookmarks.
     * @param max_width_in_chars    The maximum width of the dialog, in characters, to stay within the bounds of the editor.
     */
    protected Bookmark_dialog( Shell parent_shell,
                               IEditorPart editor,
                               LinkedList<Bookmark_info> bookmarks,
                               int max_width_in_chars  )
    {
        super( parent_shell,
               editor );
        m_bookmarks = bookmarks;
        m_max_width_in_chars = max_width_in_chars;
    }

    @Override
    public Abstract_table_info get_table_info()
    {
        return this;
    }

    /**
     * The column's titles. Also defines the number of columns.
     */
    private static final String[] m_titles = { "", Messages.bookmark_dialog_1, //$NON-NLS-1$
            Messages.bookmark_dialog_2, Messages.bookmark_dialog_3 };

    /**
     * The column's justification.
     */
    private static final int[] m_justification = { SWT.NONE, SWT.NONE, SWT.CENTER, SWT.CENTER };

    @Override
    public String get_text( Object element,
                            int column_index )
    {
        Bookmark_info bookmark = (Bookmark_info)element;

        switch( column_index )
        {
            case 0: return Integer.toString( bookmark.m_number );

            case 1:
            {
                /// TODO: Should do this in pixels for the complete dialog...
                int modified_max_width =
                        ( ( m_max_width_in_chars - m_titles[2].length() - m_titles[3].length()  ) * 4 ) / 5;
                String text = Activator.truncate_elipsis( bookmark.m_resource,
                                                                  modified_max_width,
                                                                  true );
                return text;
            }

            case 2: return bookmark.m_line_column;
            case 3: return Integer.toString( bookmark.m_offset );

            default: return null;
        }
    }

    @Override
    public String get_title( int column_index )
    {
        return( m_titles[column_index] );
    }

    @Override
    public int get_number_of_columns()
    {
        return( m_titles.length );
    }

    @Override
    public List< Object > get_table_items()
    {
        return( new ArrayList<Object>( m_bookmarks ) );
    }

    /**
     * @return  The currently selected bookmark's number, 1-10, or -1 if no bookmark selected.
     */
    public int get_selected_bookmark_number()
    {
        Bookmark_info bmi = (Bookmark_info)get_selected_item();
        if( bmi == null ) return( -1 );

        return( bmi.m_number );
    }

    @Override
    public int get_justification( int column_index )
    {
        return( m_justification[column_index] );
    }

    @Override
    public void clear_button_pressed( Object selected_item )
    {
        Bookmark_info bmi = (Bookmark_info)get_selected_item();
        if( bmi == null ) return;

        m_bookmarks.remove( bmi );
    }

    @Override
    public void clear_all_button_pressed()
    {
        m_bookmarks.clear();
    }

    public static class Viewer_comparator extends ViewerComparator
    {
        @Override
        public int compare( Viewer viewer,
                            Object e1,
                            Object e2 )
        {
            Bookmark_info bmi1 = (Bookmark_info)e1;
            Bookmark_info bmi2 = (Bookmark_info)e2;

            /*
             * This will need to be a switch using "this.propertyIndex" if additional
             * column support is needed. And direction will need to invert the result.
             */
            if( bmi1.m_number > bmi2.m_number )
            {
                return 1;
            }
            if( bmi1.m_number < bmi2.m_number )
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
    }

    @Override
    public ViewerComparator get_view_comparator()
    {
        return new Viewer_comparator();
    }

    public static final int SORT_DIRECTION = SWT.UP;

    @Override
    public int get_view_comparator_sort_direction()
    {
        return SORT_DIRECTION;
    }

    public static final int SORT_COLUMN = 1;

    @Override
    public int get_view_comparator_sort_column()
    {
        return( SORT_COLUMN - 1 );
    }
}
