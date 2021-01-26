
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

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;

/**
 * Used as the superclass functionality of the bookmark and scrap subclass dialogs.
 * Contains a table of items defined by the subclass, and four buttons,
 * the buttons being clear, clear all, ok, and cancel.
 *
 * Subclasses implement the Table_info class defined here to create a specific dialog.
 */
public abstract class Abstract_table_dialog extends Abstract_simple_dialog
{
    /**
     * Creates the text dialog with the given parent shell provider.
     *
     * @param parent_shell    The parent shell.
     * @param editor    The currently active editor.
     */
    protected Abstract_table_dialog( Shell parent_shell,
                                     IEditorPart editor  )
    {
        super( parent_shell, editor );
    }

    /**
     * The table info from the subclass.
     */
    Abstract_table_info m_table_info = null;

    /**
     * Storage for the model data as generic objects.
     * Supplied by the subclass so we don't know the actual type.
     */
    private List< Object > m_model = null;

    @Override
    protected void create_contents( Composite parent )
    {
        m_table_info = get_table_info();
        create_table( parent );
    }

    /**
     * Interface to facilitate customization of the dialog's table by the subclass.
     */
    private interface I_table_info
    {
        /**
         * Gets the cell text for a given element for a given column.
         *
         * @param element   The element (row) of the table.
         * @param column_index  The column's index.
         * @return  The cell's text.
         */
        public String get_text( Object element, int column_index );

        /**
         * Gets the title text for a given column.
         *
         * @param column_index  The column's index.
         *
         * @return The given column's title text.
         */
        public String get_title( int column_index );

        /**
         * Gets the number of columns in the table.
         *
         * @return  The number of columns in the table.
         */
        public int get_number_of_columns();

        /**
         * Gets the list of table elements/items/rows.
         *
         * @return  The list of table elements/items/rows.
         */
        public List<Object> get_table_items();

        /**
         * Gets the justification for a given column.
         *
         * @param column_index  The column's index.
         *
         * @return  The justification for a given column.
         */

        public int get_justification( int column_index );

        /**
         * Gets the view comparator to use in sorting the table.
         * Optional. Only implement if sorting.
         *
         * @return  The view comparator to use in sorting the table.
         */
        public ViewerComparator get_view_comparator();

        /**
         * Gets the sorting direction.
         * Optional. Only needed if get_view_comparator is implemented.
         *
         * @return  The sorting direction.
         */
        public int get_view_comparator_sort_direction();

        /**
         * Gets the column to base the sort on.
         * Optional. Only needed if get_view_comparator is implemented.
         *
         * @return  The column to base the sort on.
         */
        public int get_view_comparator_sort_column();
    }

    /**
     * Stub class allows the subclass to partially implement the i_table_info interface.
     * Must implement all except the optional methods.
     */
    public static abstract class Abstract_table_info extends Abstract_table_dialog implements I_table_info
    {
        protected Abstract_table_info( Shell parent_shell,
                              IEditorPart editor )
        {
            super( parent_shell,
                   editor );
        }

        @Override
        public String get_text( Object element,
                                int column_index )
        {
            return null;
        }

        @Override
        public String get_title( int column_index )
        {
            return null;
        }

        @Override
        public int get_number_of_columns()
        {
            return -1;
        }

        @Override
        public List< Object > get_table_items()
        {
            return null;
        }

        @Override
        public int get_justification( int column_index )
        {
            return -1;
        }

        @Override
        public ViewerComparator get_view_comparator()
        {
            return null;
        }

        @Override
        public int get_view_comparator_sort_direction()
        {
            return -1;
        }

        @Override
        public int get_view_comparator_sort_column()
        {
            return -1;
        }
    }

    /**
     * The subclass implements this to give this class the custom table information.
     *
     * @return  The subclass customized table information.
     */
    public abstract Abstract_table_info get_table_info();

    /**
     * Provider that keeps track of column index so the subclass can provide the
     * appropriate label for the element based on the column index. Used by the
     * TableViewerColumn.
     */
    static class column_label_provider extends ColumnLabelProvider
    {
        protected int m_column_index = -1;

        public column_label_provider( int column_index )
        {
            super();
            m_column_index = column_index;
        }
    }

    /**
     * The table viewer used for the table functionality in the dialog.
     */
    private TableViewer m_table_viewer;

    /**
     * The currently selected item, i.e. row in the table. Only a single row can be selected currently.
     */
    private Object m_selected_item = null;

    /**
     * Getter.
     * @return  The currently selected table item.
     */
    public Object get_selected_item()
    {
        return m_selected_item;
    }

    /**
     * The index of the currently selected table item in the data model storage.
     */
    private int m_selected_item_index = -1;

    /**
     * Creates the table viewer and sets up the tables layout and listeners.
     *
     * @param parent    The parent that the table viewers contained in.
     */
    private void create_table( Composite parent )
    {
        // Single row selection only.
        m_table_viewer = new TableViewer( parent,
                                          SWT.H_SCROLL | SWT.V_SCROLL |
                                          SWT.FULL_SELECTION | SWT.BORDER );
        create_columns();

        final Table table = m_table_viewer.getTable();
        table.setHeaderVisible( true );
        table.setLinesVisible( true );

        m_table_viewer.setContentProvider( new ArrayContentProvider() );

        // Get the items for the table viewer and load them.
        m_model = m_table_info.get_table_items();
        m_table_viewer.setInput( m_model );

        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        m_table_viewer.getControl().setLayoutData( gridData );

        // Resize the columns to match the data.
        for( int i = 0, n = table.getColumnCount(); i < n; i++ )
        {
            table.getColumn(i).pack();
        }

        // Listener for the button actions.
        m_table_viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged( SelectionChangedEvent event )
            {
                IStructuredSelection selection = m_table_viewer.getStructuredSelection();
                m_selected_item = selection.getFirstElement();
                m_selected_item_index = m_table_viewer.getTable().getSelectionIndex();
                if( ( ( m_selected_item == null ) || ( m_selected_item_index == -1 ) ) &&
                        ( m_model.size() > 0 ) )
                {
                    m_table_viewer.getTable().setSelection( 0 );
                }
            }
        } );

        // Set column to sort.
        if( m_table_info.get_view_comparator() != null )
        {
            set_sorting( m_table_info.get_view_comparator(),
                         m_table_info.get_view_comparator_sort_direction(),
                         m_table_info.get_view_comparator_sort_column() );
        }
    }

    /**
     * Sets up sorting.
     *
     * @param viewer_comparator     The view comparator used for the sort.
     * @param dir   The sort direction. Either SWT.UP, SWT.DOWN, SWT.NONE.
     * @param column    The column index to be used for sorting.
     */
    protected void set_sorting( ViewerComparator viewer_comparator,
                                int dir,
                                int column )
    {
        m_table_viewer.setComparator( viewer_comparator );

        m_table_viewer.getTable().setSortDirection( dir );
        m_table_viewer.getTable().setSortColumn( m_table_viewer.getTable().getColumn( column ) );

        refresh_table();
    }

    /**
     * Creates the columns based on the table info from the subclass.
     */
    private void create_columns()
    {
        for( int i = 0, n = m_table_info.get_number_of_columns(); i < n; i++ )
        {
            TableViewerColumn table_viewer_column =
                    create_table_viewer_column( m_table_info.get_title( i ),
                                                m_table_info.get_justification( i ) );

            table_viewer_column.setLabelProvider( new column_label_provider( i )
            {
                @Override
                public String getText( Object element )
                {
                    String text = m_table_info.get_text( element, m_column_index );
                    if( text == null ) return ""; //$NON-NLS-1$
                    return( text );
                }
            } );
        }
    }

    /**
     * Creates a column based on the table info from the subclass.
     *
     * @param title     The title of the table column.
     * @param justification The justification of the column, i.e. SWT.CENTER, SWT.LEFT, etc.
     *
     * @return The newly created column for the table viewer.
     */
    private TableViewerColumn create_table_viewer_column( String title,
                                                       int justification )
    {
        final TableViewerColumn viewerColumn = new TableViewerColumn( m_table_viewer,
                                                                      justification );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( title );
        return viewerColumn;
    }

    public final static int BUTTON_ID_CLEAR = IDialogConstants.CLIENT_ID + 1;
    public final static int BUTTON_ID_CLEAR_ALL = IDialogConstants.CLIENT_ID + 2;

    @Override
    protected void create_buttons( Composite parent,
                                   int height )
    {
        create_button( parent,
                       BUTTON_ID_CLEAR,
                       Messages.abstract_table_dialog_1,
                       null,
                       SWT.DEFAULT,
                       height,
                       false );

        create_button( parent,
                       BUTTON_ID_CLEAR_ALL,
                       Messages.abstract_table_dialog_2,
                       null,
                       SWT.DEFAULT,
                       height,
                       false );
    }

    /**
     * Refreshes the table based on any changes in the model data.
     * Typically when the clear button is pressed. Returns the selection
     * to the most recent item.
     */
    public void refresh_table()
    {
        m_table_viewer.refresh();
    }

    @Override
    protected void button_pressed( int id )
    {
        // Clear button.
        if( BUTTON_ID_CLEAR == id )
        {
            // Call the subclass to delete the item in the database.
            clear_button_pressed( m_selected_item );

            m_model.remove( m_selected_item );

            refresh_table();

            return;
        }

        // Clear all button.
        if( BUTTON_ID_CLEAR_ALL == id )
        {
            // Call the subclass to clear the database.
            clear_all_button_pressed();

            m_model.clear();
            m_selected_item = null;

            setReturnCode( CANCEL );
            close();

            return;
        }
    }

    /**
     * Empty method. The subclass overrides this to act upon the clear button being pressed.
     * The subclass would usually just delete the selected item in the database
     * that originally populated the model. This class deletes the row/item
     * in the model.
     *
     * @param selected_item     The table item that will be cleared from the model.
     */
    public void clear_button_pressed( Object selected_item ) {}

    /**
     * Empty method. The subclass overrides this to act upon the clear all button being pressed.
     * The subclass would usually just delete all of the items in the database
     * that originally populated the model. This class clears the model
     * and closes the dialog.
     */
    public void clear_all_button_pressed() {}
}
