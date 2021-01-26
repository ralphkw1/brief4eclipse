
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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import net.ddns.rkdawenterprises.brief4eclipse.Abstract_table_dialog.Abstract_table_info;
import net.ddns.rkdawenterprises.brief4eclipse.Scrap_support.Scrap_buffer;
import net.ddns.rkdawenterprises.brief4eclipse.Scrap_support.Scrap_item;

public class Scrap_dialog extends Abstract_table_info
{
    /**
     * Reference to the scrap buffer.
     */
    private Scrap_buffer m_scrap_buffer = null;

    /**
     * The maximum width of the dialog, in characters, to fit within the editor window.
     */
    private int m_max_width_in_chars;

    /**
     * The columns.
     */
    private static final String[] m_titles = { Messages.scrap_dialog_0, Messages.scrap_dialog_1 };

    /**
     * The justification of the columns.
     */
    private static final int[] m_justification = { SWT.CENTER, SWT.NONE };

    /**
     * Constructs the text dialog instance with the given parent shell.
     *
     * @param parent_shell          The parent shell.
     * @param editor                The active editor.
     * @param scrap_buffer          The scrap buffer.
     * @param max_width_in_chars    The maximum width of the dialog, in characters.
     */
    protected Scrap_dialog( Shell parent_shell,
                            IEditorPart editor,
                            Scrap_buffer scrap_buffer,
                            int max_width_in_chars  )
    {
        super( parent_shell,
               editor );
        m_scrap_buffer = scrap_buffer;
        m_max_width_in_chars = max_width_in_chars;
    }

    @Override
    public String get_text( Object element,
                            int column_index )
    {
        Scrap_item si = (Scrap_item)element;

        switch( column_index )
        {
            case 0:
            {
                return( si.is_column_content() ? Messages.scrap_dialog_2 : Messages.scrap_dialog_3 );
            }

            case 1:
            {
                /// TODO: Should do this in pixels for the complete dialog...
                int modified_max_width =
                        ( ( m_max_width_in_chars ) * 4 ) / 5;
                String text = Activator.truncate_elipsis( si.m_text,
                                                                  modified_max_width,
                                                                  false );
                return text;
            }

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
        return( new ArrayList<Object>( m_scrap_buffer.m_item_list ) );
    }

    @Override
    public int get_justification( int column_index )
    {
        return( m_justification[column_index] );
    }

    @Override
    public Abstract_table_info get_table_info()
    {
        return this;
    }

    /**
     * Gets the currently selected scrap item.
     *
     * @return  The currently selected scrap item.
     */
    public Scrap_item get_selected_scrap_item()
    {
        return( (Scrap_item)get_selected_item() );
    }

    @Override
    public void clear_button_pressed( Object selected_item )
    {
        Scrap_item si = (Scrap_item)get_selected_item();
        if( si == null ) return;

        m_scrap_buffer.m_item_list.remove( si );
    }

    @Override
    public void clear_all_button_pressed()
    {
        m_scrap_buffer.m_item_list.clear();
    }
}
