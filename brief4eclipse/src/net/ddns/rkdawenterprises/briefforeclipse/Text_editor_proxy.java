
/***************************************************************************//**
 * Copyright (c) 2020 Ralph and Donna Williamson and RKDAW Enterprises
 * <rkdawenterprises.ddns.net>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0.
 ******************************************************************************/

 package net.ddns.rkdawenterprises.briefforeclipse;

import java.awt.AWTException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.ITextEditorExtension5;

import net.ddns.rkdawenterprises.briefforeclipse.Scrap_support.Scrap_item;
import net.ddns.rkdawenterprises.briefforeclipse.Scrap_support.i_editor_copy;
import net.ddns.rkdawenterprises.briefforeclipse.Scrap_support.i_editor_cut;
import net.ddns.rkdawenterprises.briefforeclipse.Scrap_support.i_editor_paste;

/**
 * Performs command operations on the active text editor.
 */
public class Text_editor_proxy
{
    /**
     * The current display.
     */
    private Display m_current_display = null;

    /**
     * Getter.
     *
     * @return  The current display.
     */
    public Display get_current_display()
    {
        return m_current_display;
    }

    /**
     * The current workbench window for the active editor.
     */
    private IWorkbenchWindow m_workbench_window = null;

    /**
     * The currently active editor. If multi-part, then the currently active part.
     */
    private IEditorPart m_active_editor = null;

    /**
     * Getter.
     *
     * @return  The currently active editor.
     */
    public IEditorPart get_active_editor()
    {
        return m_active_editor;
    }

    /**
     * The styled text for the currently active editor.
     */
    private StyledText m_styled_text = null;

    /**
     * Getter.
     *
     * @return  The styled text for the currently active editor.
     */
    public StyledText get_styled_text()
    {
        return m_styled_text;
    }

    /**
     * The key mapper for command insertion.
     */
    private SWT_to_AWT_key_helper m_swt_vk_key_mapper = null;

    /**
     * Handles bookmark functionality.
     */
    private Bookmarks_support m_bookmarks_support = null;

    /**
     * Handles scrap buffer functionality.
     */
    Scrap_support m_scrap_buffer_support = null;

    /**
     * Save the main editor of a multi-part editor.
     */
    IEditorPart m_active_editor_parent = null;

    /**
     * Initializes the proxy with the current text editor and workbench parts.
     *
     * @param active_editor     The currently active editor.
     * @param workbench_window  The current workbench window.
     *
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws AWTException
     */
    protected Text_editor_proxy( IWorkbenchWindow workbench_window,
                                 IEditorPart active_editor )
        throws IllegalArgumentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, AWTException
    {
        if( active_editor != null )
        {
            set_editor( workbench_window, active_editor );
        }

        m_swt_vk_key_mapper = new SWT_to_AWT_key_helper();

        m_bookmarks_support = new Bookmarks_support( this );
        m_scrap_buffer_support = new Scrap_support( this );
    }

    /**
     * Sets the active editor and associated styled text.
     *
     * @param active_editor     The active editor.
     */
    private void set_active_editor( IEditorPart active_editor ) throws IllegalArgumentException
    {
        if( active_editor instanceof ITextEditor )
        {
            m_active_editor = active_editor;
            m_styled_text = (StyledText)( ( (ITextEditor)m_active_editor ).getAdapter( Control.class ) );
            if( m_styled_text != null ) return;
        }

        Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                "Unsupported editor (" + active_editor.toString() + ")" ); //$NON-NLS-1$ //$NON-NLS-2$
        Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_48 );
        throw new IllegalArgumentException( "Unsupported editor" ); //$NON-NLS-1$
    }

    /**
     * Updates the proxy with the current text editor and workbench window.
     * Must be called for every new key event in case there is a change of the active editor.
     *
     * Unfortunately, this accesses a protected method because otherwise it is quite
     * difficult to get the current text editor for all the varying editors in the
     * eclipse UI. Wish they would normalize that. Fortunately this works for all of the
     * editors I have tested so far.
     *
     * @param active_editor     The currently active editor.
     * @param workbench_window  The current workbench window.
     *
     * @return Error status, true if error occurs, false otherwise.
     *
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    protected void set_editor( IWorkbenchWindow workbench_window,
                               IEditorPart active_editor )
        throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        m_workbench_window = workbench_window;

        if( ( active_editor == null ) || ( workbench_window == null ) ||
                ( ( m_current_display = workbench_window.getWorkbench().getDisplay() ) == null ) )
        {
            throw new IllegalArgumentException( "Invalid or null parameters" ); //$NON-NLS-1$
        }

        m_active_editor_parent = active_editor;

        if( active_editor instanceof ITextEditor )
        {
            set_active_editor( active_editor );
            return;
        }

        if( active_editor instanceof FormEditor )
        {
            FormEditor form_editor = (FormEditor)active_editor;
            IEditorPart editor_part = form_editor.getActiveEditor();
            set_active_editor( editor_part );

            return;
        }

        if( active_editor instanceof MultiPageEditorPart )
        {
            MultiPageEditorPart multi_page_editor_part = (MultiPageEditorPart)active_editor;
            IEditorPart editor_part = getActiveEditor( multi_page_editor_part );
            set_active_editor( editor_part );

            return;
        }

        Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                "Unsupported editor (" + active_editor.toString() + ")" ); //$NON-NLS-1$ //$NON-NLS-2$
        Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_49 );
        throw new IllegalArgumentException( "Unsupported editor" ); //$NON-NLS-1$
    }

    /**
     * Informs the bookmark and scrap functions to save their data.
     */
    public void dispose()
    {
        if( m_bookmarks_support != null ) m_bookmarks_support.dispose();
        if( m_scrap_buffer_support != null ) m_scrap_buffer_support.dispose();
    }

    /**
     * Obtains the active sub-editor from a multi-page editor.
     * Unfortunately, this uses reflection to obtain a protected method.
     *
     * @param multi_page_editor_part    The multi-page editor.
     *
     * @return  The active editor.
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private IEditorPart getActiveEditor( MultiPageEditorPart multi_page_editor_part )
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method m = MultiPageEditorPart.class.getDeclaredMethod( "getActiveEditor" ); //$NON-NLS-1$
        m.setAccessible( true );
        return (IEditorPart)m.invoke( multi_page_editor_part );
    }

    /**
     * Returns the caret offset of the end of the line at the given caret offset.
     * Does not include the line delimiter.
     *
     * @param caret_offset  The caret offset.
     * @return
     */
    private int get_line_end_offset( int caret_offset )
    {
        int line_index = m_styled_text.getLineAtOffset( caret_offset );
        int line_end_offset = m_styled_text.getOffsetAtLine( line_index ) +
                m_styled_text.getLine( line_index ).length();
        return line_end_offset;
    }

    /**
     * Returns the delimiter of the line at the given caret offset.
     *
     * @param caret_offset  The caret offset.
     *
     * @return  The line's delimiter.
     */
    private String get_lines_delimiter( int caret_offset )
    {
        int line_end_offset = get_line_end_offset( caret_offset );
        int content_length = m_styled_text.getCharCount();
        if( line_end_offset == content_length )
        {
            // End of file.
            return ""; //$NON-NLS-1$
        }

        String line_end = System.getProperty( "line.separator" ); //$NON-NLS-1$

        if( line_end_offset + 1 == content_length )
        {
            /**
             * Empty line at the end and caret on the second to the last line.
             * The method StyledText.getText has an issue for this case.
             * It won't let you get just the delimiter but it will let you
             * get the last character and the delimiter.
             */
            line_end = m_styled_text.getText( line_end_offset - 1, line_end_offset );
        }
        else
        {
            /*
             * Far enough away from the end to grab the first character of the delimiter
             * and the next character after that.
             */
            line_end = m_styled_text.getText( line_end_offset, line_end_offset + 1 );
        }

        line_end = line_end.replaceAll("[^\\r\\n]", ""); //$NON-NLS-1$ //$NON-NLS-2$

        return line_end;
    }

    /**
     * Selects the current line at the current caret offset.
     * The selection includes the line delimiter so it can be cut and pasted as a whole line.
     */
    private void select_current_line()
    {
        int caret_offset = m_styled_text.getCaretOffset();
        int line_index = m_styled_text.getLineAtOffset( caret_offset );
        int start_of_line = m_styled_text.getOffsetAtLine( line_index );

        int line_length = m_styled_text.getLine( line_index ).length() +
                get_lines_delimiter( caret_offset ).length();
        if( line_length == 0 ) return;

        m_styled_text.setSelection( start_of_line, start_of_line + line_length );
    }

    /**
     * Flag indicating active normal marking mode.
     */
    private boolean m_in_marking_mode = false;

    /**
     * Stops normal marking mode and resets all associated variables.
     */
    private void stop_marking_mode()
    {
        m_current_display.removeFilter( SWT.KeyDown, m_marking_mode_key_filter );
        m_current_display.removeFilter( SWT.KeyUp, m_marking_mode_key_filter );
        m_current_display.removeFilter( SWT.MouseDown, m_marking_mode_mouse_down_filter );
        m_current_display.removeFilter( SWT.MouseDoubleClick, m_marking_mode_mouse_double_click_listener );

        m_in_marking_mode = false;

        Brief_for_eclipse.set_status_line( null );
    }

    /**
     * Starts the normal marking mode. It initializes all of the associated tracking variables
     * and adds the required listeners.
     */
    private void start_marking_mode()
    {
        m_in_marking_mode = true;

        // Register listeners.
        m_current_display.addFilter( SWT.KeyDown, m_marking_mode_key_filter );
        m_current_display.addFilter( SWT.KeyUp, m_marking_mode_key_filter );
        m_current_display.addFilter( SWT.MouseDown, m_marking_mode_mouse_down_filter );
        m_current_display.addFilter( SWT.MouseDoubleClick, m_marking_mode_mouse_double_click_listener );

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_12 );
    }

    /**
     * Flag indicating active line marking mode.
     */
    private boolean m_in_line_marking_mode = false;

    /**
     * Stops line marking mode and resets all associated variables.
     */
    private void stop_line_marking_mode()
    {
        m_current_display.removeFilter( SWT.KeyDown, m_line_marking_mode_key_filter );
        m_current_display.removeFilter( SWT.KeyUp, m_line_marking_mode_key_filter );
        m_current_display.removeFilter( SWT.MouseDown, m_marking_mode_mouse_down_filter );
        m_styled_text.removeListener( SWT.MouseUp, m_line_marking_mode_mouse_up_listener );
        m_current_display.removeFilter( SWT.MouseDoubleClick, m_marking_mode_mouse_double_click_listener );

        m_in_line_marking_mode = false;

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( null );
    }

    /**
     * Starts the line marking mode. It initializes all of the associated tracking variables
     * and adds the required listeners.
     */
    private void start_line_marking_mode()
    {
        m_in_line_marking_mode = true;

        // Line marking mode starts with a full line selection.
        m_styled_text.invokeAction( ST.LINE_START );
        m_styled_text.invokeAction( ST.SELECT_LINE_DOWN );

        // Register listeners.
        m_current_display.addFilter( SWT.KeyDown, m_line_marking_mode_key_filter );
        m_current_display.addFilter( SWT.KeyUp, m_line_marking_mode_key_filter );
        m_current_display.addFilter( SWT.MouseDown, m_marking_mode_mouse_down_filter );
        m_styled_text.addListener( SWT.MouseUp, m_line_marking_mode_mouse_up_listener );
        m_current_display.addFilter( SWT.MouseDoubleClick, m_marking_mode_mouse_double_click_listener );

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_13 );
    }

    /**
     * Stops column marking mode and resets all associated variables.
     * Disables block selection mode.
     */
    private void stop_column_marking_mode()
    {
        stop_column_marking_mode( true );
    }

    /**
     * Flag indicating active column marking mode.
     */
    private boolean m_in_column_marking_mode = false;

    /**
     * Stops column marking mode and resets all associated variables.
     * Disables block selection mode if requested.
     *
     * @param disable_block_selection_mode  Turns off block selection mode when true.
     */
    private void stop_column_marking_mode( boolean disable_block_selection_mode )
    {
        m_current_display.removeFilter( SWT.KeyDown, m_marking_mode_key_filter );
        m_current_display.removeFilter( SWT.KeyUp, m_marking_mode_key_filter );
        m_current_display.removeFilter( SWT.MouseDown, m_marking_mode_mouse_down_filter );
        m_current_display.removeFilter( SWT.MouseDoubleClick, m_marking_mode_mouse_double_click_listener );

        if( disable_block_selection_mode )
        {
            ( (ITextEditorExtension5)m_active_editor ).setBlockSelectionMode( false );
        }

        m_in_column_marking_mode = false;

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( null );
    }

    /**
     * Starts the column marking mode. It initializes all of the associated tracking variables
     * and adds the required listeners.
     */
    private void start_column_marking_mode()
    {
        m_in_column_marking_mode = true;
        ( (ITextEditorExtension5)m_active_editor ).setBlockSelectionMode( true );

        // Register listeners.
        m_current_display.addFilter( SWT.KeyDown, m_marking_mode_key_filter );
        m_current_display.addFilter( SWT.KeyUp, m_marking_mode_key_filter );
        m_current_display.addFilter( SWT.MouseDown, m_marking_mode_mouse_down_filter );
        m_current_display.addFilter( SWT.MouseDoubleClick, m_marking_mode_mouse_double_click_listener );

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_14 );
    }

    /**
     * Flag indicating active virtual caret mode.
     */
    private boolean m_in_virtual_caret_mode = false;

    /**
     * Stops column marking mode and resets all associated variables.
     * Disables block selection mode if requested.
     *
     * @param disable_block_selection_mode  Turns off block selection mode when true.
     */
    private void stop_virtual_caret_mode( boolean disable_block_selection_mode )
    {
        m_styled_text.removeListener( SWT.MouseDown, m_virtual_caret_mode_mouse_listener );
        m_styled_text.removeListener( SWT.MouseUp, m_virtual_caret_mode_mouse_listener );
        m_styled_text.removeListener( SWT.KeyDown, m_virtual_caret_mode_key_listener );
        m_styled_text.removeListener( SWT.KeyUp, m_virtual_caret_mode_key_listener );

        m_styled_text.setCursor( null );

        if( disable_block_selection_mode )
        {
            ( (ITextEditorExtension5)m_active_editor ).setBlockSelectionMode( false );
        }

        m_in_virtual_caret_mode = false;

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( null );
    }

    /**
     * Start the virtual caret mode, which can place cursor in location that is out of
     * bounds of normal text.
     *
     * TODO: Unfortunately, this uses a protected method in StyledText.class since
     * the ability to move the cursor to virtual space is not exposed publicly.
     *
     * @param x     Virtual caret horizontal position.
     * @param y     Virtual caret vertical position.
     *
     * @return
     */
    private void start_virtual_caret_mode( int x, int y )
    {
        ( (ITextEditorExtension5)m_active_editor ).setBlockSelectionMode( true );

        setBlockSelectionLocation( x, y, true );

        m_styled_text.setCursor( m_current_display.getSystemCursor( SWT.CURSOR_IBEAM ) );

        m_in_virtual_caret_mode = true;

        // Create listeners.
        m_styled_text.addListener( SWT.MouseDown, m_virtual_caret_mode_mouse_listener );
        m_styled_text.addListener( SWT.MouseUp, m_virtual_caret_mode_mouse_listener );
        m_styled_text.addListener( SWT.KeyDown, m_virtual_caret_mode_key_listener );
        m_styled_text.addListener( SWT.KeyUp, m_virtual_caret_mode_key_listener );

        m_got_alt = false;
        m_got_marking_mode_key = false;

        Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_15 );
    }

    /**
     * Stops all of the marking modes and removes the existing selection and disables block mode,
     * if requested.
     *
     * @param remove_selection      Removes the current selection.
     * @param reset_block_mode      Turns of the block selection mode.
     */
    private void stop_all_marking_modes( boolean remove_selection,
                                         boolean reset_block_mode  )
    {
        if( m_in_marking_mode )
        {
            stop_marking_mode();
        }

        if( m_in_column_marking_mode )
        {
            stop_column_marking_mode( reset_block_mode );
        }

        if( m_in_line_marking_mode )
        {
            stop_line_marking_mode();
        }

        if( m_in_virtual_caret_mode )
        {
            stop_virtual_caret_mode( true );
        }

        if( remove_selection )
        {
            m_styled_text.setSelection( m_styled_text.getCaretOffset() );
        }
    }

    /**
     * Determines the total number of hidden rows of pixels from the start of the text to the
     * beginning of the visible widget client area.
     *
     * @return  The hidden rows of pixels.
     */
    private int get_vertical_scroll_offset()
    {
        int hidden_top_line_pixels = JFaceTextUtil.getHiddenTopLinePixels( m_styled_text );
        int partial_top_index = JFaceTextUtil.getPartialTopIndex( m_styled_text );
        int vertical_scroll_offset = 0;
        for( int i = 0; i < partial_top_index; i++ )
        {
            vertical_scroll_offset += JFaceTextUtil.computeLineHeight( m_styled_text, i );
        }

        vertical_scroll_offset += hidden_top_line_pixels;

        return vertical_scroll_offset;
    }

    /**
     * Filter to modify mouse down with the shift key in all marking modes.
     */
    private Listener m_marking_mode_mouse_down_filter = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                m_styled_text = (StyledText)event.widget;
                if( ( m_styled_text.isFocusControl() ) && ( m_styled_text == event.widget ) )
                {
                    // Modify mouse down to be a selection event.
                    event.stateMask |= SWT.MOD2;
                }
            }
        }
    };

    /**
     * Filter to make sure mouse movement in line marking mode results in the
     * selection of whole lines.
     */
    private Listener m_line_marking_mode_mouse_up_listener = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                m_styled_text = (StyledText)event.widget;
                if( ( m_styled_text.isFocusControl() ) && ( m_styled_text == event.widget ) )
                {
                    // Just make sure a whole line is selected at current location.
                    m_styled_text.invokeAction( ST.SELECT_LINE_END );
                    m_styled_text.invokeAction( ST.SELECT_COLUMN_NEXT );
                }
            }
        }
    };

    /**
     * Filter to monitor mouse down when in virtual caret mode.
     */
    private Listener m_virtual_caret_mode_mouse_listener = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                m_styled_text = (StyledText)event.widget;
                if( ( m_styled_text.isFocusControl() ) && ( m_styled_text == event.widget ) )
                {
                    // Any click outside of virtual caret space will stop the mode.
                    Point right_side_of_window_point = new Point( event.x, event.y );
                    int offset = m_styled_text.getOffsetAtPoint( right_side_of_window_point );
                    if( offset != -1 )
                    {
                        stop_virtual_caret_mode( true );
                    }
                }
            }
        }
    };

    /**
     * Listener to disable all marking modes and virtual caret mode if mouse is double clicked.
     */
    private Listener m_marking_mode_mouse_double_click_listener = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                // Don't know what to do with a double click, so just get out of column mode.
                m_styled_text = (StyledText)event.widget;
                stop_all_marking_modes( false, true );
            }
        }
    };

    /**
     * TODO: Investigate alternatives for the following members.
     * Wish I did not have to hardcode the key bindings for the marking modes,
     * but not sure any way around it.
     * Could query the bindings, but what if there are multiple bindings to watch for.
     * Seems like more work than it is worth...
     */

    /**
     * The ALT key was pressed in the key event handler.
     */
    private boolean m_got_alt = false;

    /**
     * The marking mode key (M, L, or C) was pressed in the key event handler.
     */
    private boolean m_got_marking_mode_key = false;

    /**
     * Keystroke filter for monitoring key event while the marking mode
     * and column marking modes are active.
     */
    private Listener m_marking_mode_key_filter = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                m_styled_text = (StyledText)event.widget;
                if( ( m_styled_text.isFocusControl() ) && ( m_styled_text == event.widget ) )
                {
                    int key_code = event.keyCode;
                    int state_mask = event.stateMask;

                    /*
                     * Need to ignore the end of the key sequence that enabled this marking mode
                     * and don't do anything until we get it.
                     */
                    if( !m_got_alt || !m_got_marking_mode_key )
                    {
                        if( !m_got_alt && ( key_code == SWT.MOD3 ) && ( event.type == SWT.KeyUp ) )
                        {
                            m_got_alt = true;
                        }

                        String key = ( KeyLookupFactory.getDefault().formalNameLookup( key_code ) );

                        String marking_mode_key_test = ""; //$NON-NLS-1$
                        if( m_in_marking_mode ) marking_mode_key_test = Messages.text_editor_proxy_17;
                        else if( m_in_column_marking_mode ) marking_mode_key_test = Messages.text_editor_proxy_18;

                        if( !m_got_marking_mode_key && key.equalsIgnoreCase( marking_mode_key_test ) && ( event.type == SWT.KeyUp ) )
                        {
                            m_got_marking_mode_key = true;
                        }

                        if( !m_got_alt || !m_got_marking_mode_key )
                        {
                            return;
                        }

                        if( m_got_alt || m_got_marking_mode_key )
                        {
                            return;
                        }
                    }

                    /*
                     * Looking for keys that can adjust the selection within the normal marking mode.
                     */
                    boolean adjust_selection = ( key_code == SWT.ARROW_DOWN ) ||
                                               ( key_code == SWT.ARROW_UP ) ||
                                               ( key_code == SWT.PAGE_DOWN ) ||
                                               ( key_code == SWT.PAGE_UP ) ||
                                               ( key_code == SWT.ARROW_RIGHT ) ||
                                               ( key_code == SWT.ARROW_LEFT );
                    if( adjust_selection )
                    {
                        // Modify keystroke to be a selection event.
                        event.stateMask |= SWT.MOD2;

                        return;
                    }

                    /*
                     * Look for keys that should stop/cancel the marking modes,
                     * Which is pretty much any printable character, plus BS, and DEL, and ENTER.
                     * TODO: modified keys.
                     */
                    if( Brief_for_eclipse.is_printable( key_code, state_mask ) ||
                            ( ( key_code == SWT.BS ) || ( key_code == SWT.DEL ) || ( key_code == SWT.CR ) ) )
                    {
                        stop_all_marking_modes( false, true );
                        return;
                    }

                    return;
                }

                return;
            }

            return;
        }
    };

    /**
     * Keystroke filter for monitoring key event while the line marking mode is active.
     */
    private Listener m_line_marking_mode_key_filter = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                m_styled_text = (StyledText)event.widget;
                if( ( m_styled_text.isFocusControl() ) && ( m_styled_text == event.widget ) )
                {
                    int key_code = event.keyCode;
                    int state_mask = event.stateMask;

                    /*
                     * Need to ignore the end of the key sequence that enabled this marking mode
                     * and don't do anything until we get it.
                     */
                    if( !m_got_alt || !m_got_marking_mode_key )
                    {
                        if( !m_got_alt && ( key_code == SWT.MOD3 ) && ( event.type == SWT.KeyUp ) )
                        {
                            m_got_alt = true;
                        }

                        String key = ( KeyLookupFactory.getDefault().formalNameLookup( key_code ) );

                        if( !m_got_marking_mode_key && key.equalsIgnoreCase( Messages.text_editor_proxy_19 ) && ( event.type == SWT.KeyUp ) )
                        {
                            m_got_marking_mode_key = true;
                        }

                        if( !m_got_alt || !m_got_marking_mode_key )
                        {
                            return;
                        }

                        if( m_got_alt || m_got_marking_mode_key )
                        {
                            return;
                        }
                    }

                    /*
                     * Looking for keys that can adjust the selection within the normal marking mode.
                     */
                    boolean adjust_selection = ( key_code == SWT.ARROW_DOWN ) ||
                                               ( key_code == SWT.ARROW_UP ) ||
                                               ( key_code == SWT.PAGE_DOWN ) ||
                                               ( key_code == SWT.PAGE_UP ) ||
                                               ( key_code == SWT.ARROW_RIGHT ) ||
                                               ( key_code == SWT.ARROW_LEFT );
                    if( adjust_selection )
                    {
                        // Modify keystroke to be a selection event.
                        event.stateMask |= SWT.MOD2;

                        // Arrow right or left is translated to up or down instead.
                        if( key_code == SWT.ARROW_RIGHT )
                        {
                            event.keyCode = SWT.ARROW_DOWN;
                        }
                        else if( key_code == SWT.ARROW_LEFT )
                        {
                            event.keyCode = SWT.ARROW_UP;
                        }

                        if( event.type == SWT.KeyDown )
                        {
                            int current_caret_offset = m_styled_text.getCaretOffset();
                            int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
                            int last_line_index = m_styled_text.getLineCount() - 1;
                            int first_char_in_last_line = m_styled_text.getOffsetAtLine( last_line_index );

                            /*
                             * Make sure we are at the beginning of the line,
                             * unless it is the last line where it gets tricky.
                             */
                            if( current_line_index != last_line_index )
                            {
                                m_styled_text.invokeAction( ST.SELECT_LINE_START );
                            }
                            else
                            {
                                /*
                                 * We are currently on the last line. If the cursor is not at the
                                 * beginning of the last line, then an up arrow should only move the
                                 * cursor to the beginning of the line.
                                 */
                                if( ( current_caret_offset != first_char_in_last_line ) && ( key_code == SWT.ARROW_UP ) )
                                {
                                    m_styled_text.invokeAction( ST.SELECT_LINE_START );
                                    event.keyCode = SWT.NONE;
                                }
                            }
                        }

                        return;
                    }

                    /*
                     * Look for keys that should stop/cancel the line marking mode,
                     * Which is pretty much any printable character, plus BS, and DEL, and ENTER.
                     * TODO: modified keys.
                     */
                    if( Brief_for_eclipse.is_printable( key_code, state_mask ) ||
                            ( ( key_code == SWT.BS ) || ( key_code == SWT.DEL ) || ( key_code == SWT.CR ) ) )
                    {
                        stop_all_marking_modes( false, false );
                        return;
                    }

                    return;
                }

                return;
            }

            return;
        }
    };

    /**
     * Keystroke filter for monitoring key event while in virtual caret mode.
     */
    private Listener m_virtual_caret_mode_key_listener = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            if( event.widget instanceof StyledText )
            {
                m_styled_text = (StyledText)event.widget;
                if( ( m_styled_text.isFocusControl() ) && ( m_styled_text == event.widget ) )
                {
                    int key_code = event.keyCode;

                    /*
                     * Need to ignore the end of the key sequence that enabled this marking mode
                     * and don't do anything until we get it.
                     */
                    if( !m_got_alt || !m_got_marking_mode_key )
                    {
                        if( !m_got_alt && ( key_code == SWT.MOD3 ) && ( event.type == SWT.KeyUp ) )
                        {
                            m_got_alt = true;
                        }

                        String key = ( KeyLookupFactory.getDefault().formalNameLookup( key_code ) );

                        if( !m_got_marking_mode_key && key.equalsIgnoreCase( Messages.text_editor_proxy_20 ) && ( event.type == SWT.KeyUp ) )
                        {
                            m_got_marking_mode_key = true;
                        }

                        if( !m_got_alt || !m_got_marking_mode_key )
                        {
                            return;
                        }

                        if( m_got_alt || m_got_marking_mode_key )
                        {
                            return;
                        }
                    }

                    boolean key_is_modifier = KeyLookupFactory.getDefault().isModifierKey( key_code );

                    /*
                     * Any keystroke will stop the mode, but ignore the modifiers.
                     */
                    if( !key_is_modifier && m_in_virtual_caret_mode )
                    {
                        stop_virtual_caret_mode( true );
                    }
                }
            }
        }
    };

    /**
     * Inserts the given string into the active editor a given number of times
     * according to the passed count.
     *
     * @param string    The string to insert.
     * @param count     The number of times to insert.
     */
    private void insert_string_at_caret( String string,
                                         int count )
    {
        if( string.length() == 0 ) return;

        // The user can insert a newline into the string using "\\n".
        string = string.replace( Messages.text_editor_proxy_0, System.getProperty( "line.separator" ) ); //$NON-NLS-1$

        // The user can insert a tab into the string using "\\t".
        string = string.replace( Messages.text_editor_proxy_1, Messages.text_editor_proxy_24 );

        StringBuilder complete_string = new StringBuilder();
        for( int i = 0; i < count; i++ )
        {
            complete_string.append( string );
        }

        m_styled_text.insert( complete_string.toString() );
    }

    /**
     * Repeat a command a given number of times.
     *
     * @param command           The command to repeat.
     * @param command_codes     The command key codes. The SWT modifier bits, if any, are index 0.
     *                          The SWT key code is index 1.
     * @param count             The number of times to repeat.
     *
     * @return  Error status. True if an error occurs.
     */
    private boolean repeat_command( String command,
                                    int[] command_codes,
                                    int count )
    {
        for( int i = 0; i < count; i++ )
        {
            try
            {
                if( ( command_codes[0] & SWT.CTRL ) != 0 )
                {
                    m_swt_vk_key_mapper.press_key( SWT.CTRL );
                }

                if( ( command_codes[0] & SWT.ALT ) != 0 )
                {
                    m_swt_vk_key_mapper.press_key( SWT.ALT );
                }

                if( ( command_codes[0] & SWT.SHIFT ) != 0 )
                {
                    m_swt_vk_key_mapper.press_key( SWT.SHIFT );
                }

                m_swt_vk_key_mapper.press_key( command_codes[1] );
                m_swt_vk_key_mapper.release_key( command_codes[1] );

                if( ( command_codes[0] & SWT.SHIFT ) != 0 )
                {
                    m_swt_vk_key_mapper.release_key( SWT.SHIFT );
                }

                if( ( command_codes[0] & SWT.ALT ) != 0 )
                {
                    m_swt_vk_key_mapper.release_key( SWT.ALT );
                }

                if( ( command_codes[0] & SWT.CTRL ) != 0 )
                {
                    m_swt_vk_key_mapper.release_key( SWT.CTRL );
                }
            }
            catch( IllegalArgumentException | ParseException exception )
            {
                Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                        "Command <" + command + "> not found.", exception ); //$NON-NLS-1$ //$NON-NLS-2$
                return true;
            }
        }

        return false;
    }

    /**
     * Execute command with the given ID in dot notation using the Handler Service
     * for the active editor.
     * For instance, "org.eclipse.ui.edit.delete".
     *
     * @param command_id The identifier of the command to execute
     */
    private boolean execute_command_id( String command_id )
    {
        IHandlerService handler_service =
                (IHandlerService)m_active_editor.getSite().getService( IHandlerService.class );

        try
        {
            handler_service.executeCommand( command_id, null );
            return false;
        }
        catch( Exception exception )
        {
            Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                    "Command <" + command_id + "> not found.", exception ); //$NON-NLS-1$ //$NON-NLS-2$
            return true;
        }
    }

    /**
     * Moves the cursor to the x/y coordinate. Must be in block selection mode.
     * Unfortunately, this uses reflection to obtain a protected method.
     *
     * @param x             The horizontal pixel location.
     * @param y             The vertical pixel location.
     * @param sendEvent     Sends event when true.
     */
    private void setBlockSelectionLocation( int x, int y, boolean sendEvent )
    {
        try
        {
            Method m = m_styled_text.getClass().getDeclaredMethod( "setBlockSelectionLocation", //$NON-NLS-1$
                                                                   int.class,
                                                                   int.class,
                                                                   boolean.class );
            m.setAccessible( true );
            m.invoke( m_styled_text,
                      x,
                      y,
                      sendEvent );
        }
        catch( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception )
        {
            Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                    "Could not execute protected methods in StyledText.class", exception ); //$NON-NLS-1$
            return;
        }
    }

    /**
     * Copies the current selection or the current line if there is no selection.
     */
    protected void numpad_copy()
    {
        // If no text selected, select the whole line.
        if( !m_styled_text.isTextSelected() )
        {
            select_current_line();

            m_scrap_buffer_support.copy( m_current_display,
                                         (i_editor_copy)() ->
                {
                    m_styled_text.copy();
                    return null;
                } );

            stop_all_marking_modes( true, true );

            return;
        }

        if( !m_in_column_marking_mode )
        {
            m_scrap_buffer_support.copy( m_current_display,
                                         (i_editor_copy)() ->
                {
                    m_styled_text.copy();
                    return null;
                } );

            stop_all_marking_modes( true, true );

            return;
        }
        else
        {
            // Determine the geometry of the block for use below.
            Rectangle block_rectangle = m_styled_text.getBlockSelectionBounds();

            m_scrap_buffer_support.copy( m_current_display,
                                         (i_editor_copy)() ->
                {
                    m_styled_text.copy();
                    return( new Point( block_rectangle.width, block_rectangle.height ) );
                } );

            stop_column_marking_mode();

            // Move the caret to the lower left of the block to make multiple pastes more convenient.
            // This also clears the selection.
            Point lower_left = new Point( block_rectangle.x, block_rectangle.y + block_rectangle.height );
            lower_left.x -= m_styled_text.getHorizontalPixel();
            lower_left.y -= get_vertical_scroll_offset();
            int new_caret_offset = m_styled_text.getOffsetAtPoint( lower_left );
            if( new_caret_offset != -1 )
            {
                m_styled_text.setSelection( new_caret_offset );
            }
            else
            {
                // Can't place the caret there, so just clear the selection.
                m_styled_text.setSelection( m_styled_text.getCaretOffset() );
            }

            return;
        }
    }

    /**
     * Cuts the current selection or the current line if there is no selection.
     */
    protected void numpad_cut()
    {
        if( validate( m_workbench_window, m_active_editor ) ) return;

        // If no text selected, select the whole line.
        if( !m_styled_text.isTextSelected() )
        {
            select_current_line();

            m_scrap_buffer_support.cut( m_current_display,
                                        (i_editor_cut)() ->
            {
                m_styled_text.cut();
                return null;
            } );

            stop_all_marking_modes( true, true );

            return;
        }

        if( !m_in_column_marking_mode )
        {
            m_scrap_buffer_support.cut( m_current_display,
                                        (i_editor_cut)() ->
            {
                m_styled_text.cut();
                return null;
            } );

            stop_all_marking_modes( true, true );

            return;
        }
        else
        {
            // Determine the geometry of the block for use later.
            Rectangle block_rectangle = m_styled_text.getBlockSelectionBounds();

            m_scrap_buffer_support.cut( m_current_display,
                                        (i_editor_cut)() ->
            {
                m_styled_text.cut();
                return( new Point( block_rectangle.width, block_rectangle.height ) );
            } );

            stop_column_marking_mode();

            // Clear the selection.
            m_styled_text.setSelection( m_styled_text.getCaretOffset() );

            return;
        }
    }

    /**
     * Pastes the contents of the clipboard in either normal or column mode.
     */
    protected void insert_paste()
    {
        insert_paste( null );
    }

    /**
     * Pastes the given scrap item in either normal or block mode.
     */
    protected void insert_paste( Scrap_item scrap_item )
    {

        if( validate( m_workbench_window, m_active_editor ) ) return;

        if( !m_scrap_buffer_support.clipboard_is_column_content() )
        {
            if( scrap_item == null )
            {
                m_styled_text.paste();
            }
            else
            {
                m_styled_text.insert( scrap_item.m_text );
            }

            stop_all_marking_modes( false, true );

            return;
        }
        else
        {
            // Determine the virtual starting point of the block to be pasted for use below.
            int caret_offset = m_styled_text.getCaretOffset();
            Point block_start_point = m_styled_text.getLocationAtOffset( caret_offset );
            int x = block_start_point.x + m_styled_text.getHorizontalPixel();
            int y = block_start_point.y + get_vertical_scroll_offset();

            Point size = m_scrap_buffer_support.clipboard_column_mode_size();
            Rectangle block_rectangle = new Rectangle( x, y, size.x, size.y );

            ( (ITextEditorExtension5)m_active_editor ).setBlockSelectionMode( true );

            if( scrap_item == null )
            {
                m_styled_text.paste();
            }
            else
            {
                m_styled_text.insert( scrap_item.m_text );
            }

            ( (ITextEditorExtension5)m_active_editor ).setBlockSelectionMode( false );

            // Move the caret to the lower left of the block to make multiple pastes more convenient.
            Point lower_left = new Point( block_rectangle.x, block_rectangle.y + block_rectangle.height );
            lower_left.x -= m_styled_text.getHorizontalPixel();
            lower_left.y -= get_vertical_scroll_offset();
            int new_caret_offset = m_styled_text.getOffsetAtPoint( lower_left );
            if( new_caret_offset != -1 )
            {
                m_styled_text.setSelection( new_caret_offset );
            }
            else
            {
                // Can't place the caret there, so just clear the selection.
                m_styled_text.setSelection( m_styled_text.getCaretOffset() );
            }

            stop_all_marking_modes( false, false );

            return;
        }
    }

    /**
     * Swaps the current selection with the contents of the clipboard.
     */
    protected void insert_swap()
    {
        // Feature not for column select mode.
        if( m_in_column_marking_mode || ( m_scrap_buffer_support.clipboard_is_column_content() ) ) return;

        if( validate( m_workbench_window, m_active_editor ) ) return;

        // If no current selection, don't assume whole line.
        if( !m_styled_text.isTextSelected() ) return;

        // Save the current contents of clipboard, so we can "cut" the current selection.
        Clipboard clipboard = new Clipboard( m_current_display );
        TextTransfer text_transfer = TextTransfer.getInstance();
        String text_data = (String)clipboard.getContents( text_transfer );
        clipboard.dispose();

        numpad_cut();

        // Now insert the saved clipboard contents.
        m_styled_text.insert( text_data );
    }

    /**
     * Starts or stops the column marking mode.
     */
    protected void column_marking_mode_toggle()
    {
        stop_marking_mode();
        stop_line_marking_mode();

        if( m_in_virtual_caret_mode )
        {
            stop_virtual_caret_mode( false );
        }
        else
        {
            // Clear the selection.
            m_styled_text.setSelection( m_styled_text.getCaretOffset() );
        }

        if( !m_in_column_marking_mode )
        {
            start_column_marking_mode();
        }
        else
        {
            stop_column_marking_mode();

            // Clear the selection.
            m_styled_text.setSelection( m_styled_text.getCaretOffset() );
        }
    }

    /**
     * Starts or stops the normal marking mode.
     */
    protected void marking_mode_toggle()
    {
        stop_column_marking_mode();
        stop_line_marking_mode();
        stop_virtual_caret_mode( true );

        // Clear the selection.
        m_styled_text.setSelection( m_styled_text.getCaretOffset() );

        if( !m_in_marking_mode )
        {
            start_marking_mode();
        }
        else
        {
            stop_marking_mode();

            // Clear the selection.
            m_styled_text.setSelection( m_styled_text.getCaretOffset() );
        }
    }

    /**
     * Starts or stops the line marking mode.
     */
    protected void line_marking_mode_toggle()
    {
        stop_column_marking_mode();
        stop_marking_mode();
        stop_virtual_caret_mode( true );

        // Clear the selection.
        m_styled_text.setSelection( m_styled_text.getCaretOffset() );

        if( !m_in_line_marking_mode )
        {
            start_line_marking_mode();
        }
        else
        {
            stop_line_marking_mode();

            // Clear the selection.
            m_styled_text.setSelection( m_styled_text.getCaretOffset() );
        }
    }

    protected void virtual_caret_mode_toggle()
    {
        if( m_in_virtual_caret_mode )
        {
            stop_virtual_caret_mode( true );
        }
        else
        {
            Point cursor_point = m_current_display.getCursorLocation();
            cursor_point = m_current_display.map( null, m_styled_text, cursor_point );

            // Any click outside of virtual caret space with stop the mode.
            int offset = m_styled_text.getOffsetAtPoint( cursor_point );
            if( offset == -1 )
            {
                start_virtual_caret_mode( cursor_point.x, cursor_point.y );
            }
        }
    }

    /**
     * Opens a dialog that obtains a command or a string, from the user, that is to be
     * repeated a number of times requested by the user.
     */
    protected void repeat_command_or_string()
    {
        if( validate( m_workbench_window, m_active_editor ) ) return;

        Command_dialog dialog = new Command_dialog( m_workbench_window.getShell(),
                                                    m_active_editor );
        if( dialog.open() == Window.OK )
        {
            if( dialog.get_command().length() > 0 )
            {
                repeat_command( dialog.get_command(),
                                dialog.get_command_codes(),
                                dialog.get_count() );
                return;
            }

            if( dialog.get_repeat_string().length() > 0 )
            {
                insert_string_at_caret( dialog.get_repeat_string(), dialog.get_count() );
                return;
            }
        }

        IWorkbenchPage workbench_page = m_workbench_window.getActivePage();
        if( workbench_page == null ) return;
        workbench_page.activate( m_active_editor );
    }

    /**
     * Scrolls the editor a given number of lines.
     *
     * @param number_of_lines   The number of lines to scroll and the direction. If it is &lt; 0,
     *                          it's scrolling up, if &gt; 0 it's scrolling down.
     */
    protected void scroll_buffer( int number_of_lines )
    {
        int top_index = m_styled_text.getTopIndex();
        top_index = Math.max( 0, top_index + number_of_lines );
        m_styled_text.setTopIndex( top_index );
    }

    /**
     * Scrolls the editor window to move the current line to the top of the window.
     */
    protected void line_to_top_of_window()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
        m_styled_text.setTopIndex( current_line_index );
    }

    /**
     * Scrolls the editor window to move the current line to the center of the window.
     */
    protected void center_line_in_window()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
        int top_index = m_styled_text.getTopIndex();
        int bottom_index = JFaceTextUtil.getBottomIndex( m_styled_text );
        int middle_index = top_index + ( ( bottom_index - top_index ) / 2 );
        top_index = Math.max( 0, top_index + ( current_line_index - middle_index ) );
        m_styled_text.setTopIndex( top_index );
    }

    /**
     * Scrolls the editor window to move the current line to the bottom of the window.
     */
    protected void line_to_bottom_of_window()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
        int top_index = m_styled_text.getTopIndex();
        int bottom_index = JFaceTextUtil.getBottomIndex( m_styled_text );
        top_index = Math.max( 0, top_index - ( bottom_index - current_line_index ) );
        m_styled_text.setTopIndex( top_index );
    }

    /**
     * Moves the caret to the beginning of the editor.
     */
    protected void top_of_buffer()
    {
        m_styled_text.setCaretOffset( 0 );
        m_styled_text.setTopIndex( 0 );
    }

    /**
     * Moves the caret to the end of the editor.
     */
    protected void bottom_of_buffer()
    {
        m_styled_text.setCaretOffset( m_styled_text.getCharCount() );
        m_styled_text.setTopIndex( m_styled_text.getLineCount() - 1 );
    }

    /**
     * Moves the caret to the top of the window.
     */
    protected void top_of_window()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
        int current_line_start_offset = m_styled_text.getOffsetAtLine( current_line_index );
        int current_caret_column = current_caret_offset - current_line_start_offset;
        int top_index = m_styled_text.getTopIndex();
        int top_line_start_offset = m_styled_text.getOffsetAtLine( top_index );
        m_styled_text.setCaretOffset( top_line_start_offset + current_caret_column );
    }

    /**
     * Moves the caret to the top of the window.
     */
    protected void end_of_window()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
        int current_line_start_offset = m_styled_text.getOffsetAtLine( current_line_index );
        int current_caret_column = current_caret_offset - current_line_start_offset;
        int bottom_index = JFaceTextUtil.getBottomIndex( m_styled_text );
        int bottom_line_start_offset = m_styled_text.getOffsetAtLine( bottom_index );
        m_styled_text.setCaretOffset( bottom_line_start_offset + current_caret_column );
    }

    /**
     * Places the caret at the farthest character position on the left side of the window.
     */
    protected void left_side_of_window()
    {
        Point cursor_point = m_current_display.getCursorLocation();
        cursor_point = m_current_display.map( null, m_styled_text, cursor_point );

        /*
         * Get the x/y location of the the current line at the left side of window,
         * as well as the caret position, which may not be the same location.
         */
        int desired_x = m_styled_text.getLeftMargin();
        int current_y = m_styled_text.getLinePixel( m_styled_text.getLineAtOffset( m_styled_text.getCaretOffset() ) );
        Point left_side_of_window_point = new Point( desired_x, current_y );
        int offset = m_styled_text.getOffsetAtPoint( left_side_of_window_point );

        /*
         * Check if the new location is a normal character location where we can place
         * the caret.
         */
        if( offset != -1 )
        {
            // We're not moving to virtual space, so just set the caret to the offset.
            m_styled_text.setCaretOffset( offset );
            return;
        }
        // Otherwise, the new location is virtual space.
        else
        {
            /*
             * If we are in column marking mode, then we just need to adjust the selection to
             * the window width, regardless of whether it is virtual space or not.
             */
            if( m_in_column_marking_mode )
            {
                Rectangle block_selection_bounds = m_styled_text.getBlockSelectionBounds();
                int current_x = block_selection_bounds.x + block_selection_bounds.width;
                block_selection_bounds.width += desired_x - current_x;
                m_styled_text.setBlockSelectionBounds( block_selection_bounds );
            }
            // Otherwise, turn on block selection mode then move to the virtual space.
            else
            {
                if( m_in_virtual_caret_mode )
                {
                    stop_virtual_caret_mode( true );
                }

                start_virtual_caret_mode( desired_x, current_y );
            }
        }
    }

    /**
     * Places the caret at the farthest character position on the right side of the window,
     * minus a bit for the scroll bars, margin, and such.
     * This may be in "virtual" character space, so unfortunately due to limitations in StyledText,
     * this only works in block/column selection mode.
     */
    protected void right_side_of_window()
    {
        /*
         * Get the x/y location of the the current line at the right side of window,
         * as well as the caret position, which may not be the same location.
         */
        int average_character_width = JFaceTextUtil.getAverageCharWidth( m_styled_text );
        int desired_x = m_styled_text.getClientArea().width - m_styled_text.getRightMargin() -
                ( 2 * average_character_width ) + ( average_character_width / 2 );
        int current_y = m_styled_text.getLinePixel( m_styled_text.getLineAtOffset( m_styled_text.getCaretOffset() ) );
        Point right_side_of_window_point = new Point( desired_x, current_y );
        int offset = m_styled_text.getOffsetAtPoint( right_side_of_window_point );

        /*
         * Check if the new location is a normal character location where we can place
         * the caret.
         */
        if( offset != -1 )
        {
            // We're not moving to virtual space, so just set the caret to the offset.
            m_styled_text.setCaretOffset( offset );
            return;
        }
        // Otherwise, the new location is virtual space.
        else
        {
            /*
             * If we are in column marking mode, then we just need to adjust the selection to
             * the window width, regardless of whether it is virtual space or not.
             */
            if( m_in_column_marking_mode )
            {
                Rectangle block_selection_bounds = m_styled_text.getBlockSelectionBounds();
                int current_x = block_selection_bounds.x + block_selection_bounds.width;
                block_selection_bounds.width += desired_x - current_x;
                m_styled_text.setBlockSelectionBounds( block_selection_bounds );
            }
            // Otherwise, turn on block selection mode then move to the virtual space.
            else
            {
                if( m_in_virtual_caret_mode )
                {
                    stop_virtual_caret_mode( true );
                }

                start_virtual_caret_mode( desired_x, current_y );
            }
        }
    }

    /**
     * Moves the caret to the beginning of the line, or window, or file depending on
     * the current caret location.
     */
    protected void home()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
        int current_line_start_offset = m_styled_text.getOffsetAtLine( current_line_index );

        int visible_top_index = m_styled_text.getTopIndex();
        int visible_top_line_start_offset = m_styled_text.getOffsetAtLine( visible_top_index );

        boolean at_file_start = ( current_caret_offset == 0 );
        boolean at_window_start = ( current_caret_offset == visible_top_line_start_offset );
        boolean at_line_start = ( current_caret_offset == current_line_start_offset );

        if( at_file_start ) return;

        // If at window start, then move to file start.
        if( at_window_start )
        {
            if( ( m_in_marking_mode ) || ( m_in_line_marking_mode ) || ( m_in_column_marking_mode ) )
            {
                m_styled_text.invokeAction( ST.SELECT_TEXT_START );
                return;
            }

            m_styled_text.invokeAction( ST.TEXT_START );

            return;
        }

        // If at line start, then move to window start.
        if( at_line_start )
        {
            if( ( m_in_marking_mode ) || ( m_in_line_marking_mode ) || ( m_in_column_marking_mode ))
            {
                /**
                 * TODO: Action SELECT_WINDOW_END not working for block selection mode
                 * which is weird because SELECT_TEXT_END does work.
                 */
                m_styled_text.invokeAction( ST.SELECT_WINDOW_START );
                return;
            }

            m_styled_text.invokeAction( ST.WINDOW_START );

            return;
        }

        // Otherwise, move to start of line.
        if( ( m_in_marking_mode ) || ( m_in_line_marking_mode ) || ( m_in_column_marking_mode ) )
        {
            m_styled_text.invokeAction( ST.SELECT_LINE_START );
            return;
        }

        m_styled_text.invokeAction( ST.LINE_START );

        return;
    }

    /**
     * Moves the caret to the end of the line, or window, or file depending on
     * the current caret location.
     */
    protected void end()
    {
        int current_caret_offset = m_styled_text.getCaretOffset();
        int current_line_end_offset = get_line_end_offset( current_caret_offset );

        int visible_bottom_index = JFaceTextUtil.getBottomIndex( m_styled_text );
        int visible_bottom_line_end_offset = m_styled_text.getOffsetAtLine( visible_bottom_index ) +
                m_styled_text.getLine( visible_bottom_index ).length();

        boolean at_file_end = ( current_caret_offset == m_styled_text.getCharCount() );
        boolean at_window_end = ( current_caret_offset == visible_bottom_line_end_offset );
        boolean at_line_end = ( current_caret_offset == current_line_end_offset );

        if( at_file_end ) return;

        // If at window end, then move to file end.
        if( at_window_end )
        {
            if( ( m_in_marking_mode ) || ( m_in_line_marking_mode ) || ( m_in_column_marking_mode ) )
            {
                m_styled_text.invokeAction( ST.SELECT_TEXT_END );
                return;
            }

            m_styled_text.invokeAction( ST.TEXT_END );

            return;
        }

        // If at line end, then move to window end.
        if( at_line_end )
        {
            if( ( m_in_marking_mode ) || ( m_in_line_marking_mode ) ||( m_in_column_marking_mode ) )
            {
                /**
                 * TODO: Action SELECT_WINDOW_END not working for block selection mode
                 * which is weird because SELECT_TEXT_END does work.
                 */
                m_styled_text.invokeAction( ST.SELECT_WINDOW_END );
                return;
            }

            m_styled_text.invokeAction( ST.WINDOW_END );

            return;
        }

        // Otherwise, move to end of line.
        if( ( m_in_marking_mode ) || ( m_in_line_marking_mode ) || ( m_in_column_marking_mode ) )
        {
            m_styled_text.invokeAction( ST.SELECT_LINE_END );
            return;
        }

        m_styled_text.invokeAction( ST.LINE_END );

        return;
    }

    /**
     * Opens a dialog to request a line number to go to.
     * Then moves the caret to the beginning of the requested line number.
     */
    protected void go_to_line()
    {
        int current_line_index = get_model_current_line_index();
        int last_line_index = get_model_last_line_index();
        if( ( current_line_index == -1 ) || ( last_line_index == -1 ) ) return;

        Get_a_number_dialog dialog = new Get_a_number_dialog( m_workbench_window.getShell(),
                                                              m_active_editor,
                                                              current_line_index + 1,
                                                              1,
                                                              999999,
                                                              Messages.goto_line_number_dialog_prompt_format );
        if( dialog.open() == Window.OK )
        {
            int requested_line_index = dialog.get_number() - 1;
            if( requested_line_index < 0 )
            {
                requested_line_index = 0;
            }

            if( requested_line_index > last_line_index )
            {
                requested_line_index = last_line_index;
            }

            set_model_current_caret_offset( get_model_caret_offset_at_line( requested_line_index ) );
        }

        IWorkbenchPage workbench_page = m_workbench_window.getActivePage();
        if( workbench_page == null ) return;
        workbench_page.activate( m_active_editor );
    }

    /**
     * Opens the bookmarks dialog window.
     */
    protected void open_bookmarks_dialog()
    {
        m_bookmarks_support.open_bookmarks_dialog( m_workbench_window, width_in_chars() );
    }

    /**
     * Gets the current caret offset in the document model.
     * This may be different than the caret offset in the editor if folding is enabled.
     *
     * @return  The current caret offset in the document model.
     */
    protected int get_model_current_caret_offset()
    {
        ITextViewer text_viewer = (ITextViewer)m_active_editor.getAdapter( ITextOperationTarget.class );
        if( text_viewer == null ) return( -1 );

        if( text_viewer instanceof ITextViewerExtension5 )
        {
            return( ( (ITextViewerExtension5)text_viewer ).widgetOffset2ModelOffset( m_styled_text.getCaretOffset() ) );
        }
        else
        {
            return m_styled_text.getCaretOffset();
        }
    }

    /**
     * Gets the line index where the caret is located in the document model.
     * This may be different than the index in the editor if folding is enabled.
     *
     * @return  The line index where the caret is located in the document model.
     */
    protected int get_model_current_line_index()
    {
        ITextViewer text_viewer = (ITextViewer)m_active_editor.getAdapter( ITextOperationTarget.class );
        if( text_viewer == null ) return( -1 );

        if( text_viewer instanceof ITextViewerExtension5 )
        {
            return( ( (ITextViewerExtension5)text_viewer ).widgetLine2ModelLine( m_styled_text.getLineAtOffset( m_styled_text.getCaretOffset() ) ) );
        }
        else
        {
            int current_caret_offset = m_styled_text.getCaretOffset();
            int current_line_index = m_styled_text.getLineAtOffset( current_caret_offset );
            return current_line_index;
        }
    }

    /**
     * Gets the line index of the last line in the document model.
     * This may be different than the index in the editor if folding is enabled.
     *
     * @return  The line index of the last line in the document model.
     */
    protected int get_model_last_line_index()
    {
        ITextViewer text_viewer = (ITextViewer)m_active_editor.getAdapter( ITextOperationTarget.class );
        if( text_viewer == null ) return( -1 );

        if( text_viewer instanceof ITextViewerExtension5 )
        {
            return( ( (ITextViewerExtension5)text_viewer ).widgetLine2ModelLine( m_styled_text.getLineCount() - 1 ) );
        }
        else
        {
            int last_line_index = m_styled_text.getLineCount() - 1;
            return last_line_index;
        }
    }

    /**
     * Gets the caret offset of the first character of the given line in the document model.
     * This may be different than the offset in the editor if folding is enabled.
     *
     * @param line The line index of the document model.
     *
     * @return  The caret offset of the first character of the given line in the document model.
     */
    protected int get_model_caret_offset_at_line( int line )
    {
        IDocumentProvider provider = ( (ITextEditor)m_active_editor ).getDocumentProvider();
        IDocument document = provider.getDocument( ( (ITextEditor)m_active_editor ).getEditorInput() );
        try
        {
            return document.getLineOffset( line );
        }
        catch( BadLocationException e )
        {
            return -1;
        }
    }

    /**
     * Gets the caret offset of the current line in the document model.
     * This may be different than the offset in the editor if folding is enabled.
     *
     * @return  The caret offset of the current line in the document model.
     */
    protected int get_model_caret_offset_at_current_line()
    {
        int current_line_index = get_model_current_line_index();
        if( current_line_index == -1 ) return( -1 );
        return( get_model_caret_offset_at_line( current_line_index ) );
    }

    /**
     * Sets the current caret offset in the document model.
     *
     * @param offset    The caret offset.
     */
    protected void set_model_current_caret_offset( int offset )
    {
        ( (ITextEditor)m_active_editor ).selectAndReveal( offset, 0 );
    }

    /**
     * Gets the find/replace dialog settings for use in repeating search/replace commands.
     * Unfortunately, this uses methods that are discouraged because they are not an official API.
     *
     * @return  The find/replace dialog settings.
     */
    @SuppressWarnings( "rawtypes" )
    private IDialogSettings getDialogSettings()
    {
        Class FindReplaceDialog_class = null;
        try
        {
            FindReplaceDialog_class = Class.forName( "org.eclipse.ui.texteditor.FindReplaceDialog" ); //$NON-NLS-1$
        }
        catch( ClassNotFoundException exception )
        {
            Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                    "Could not get name for FindReplaceDialog.class", exception ); //$NON-NLS-1$
            return null;
        }

        if( FindReplaceDialog_class == null ) return null;

        String name = FindReplaceDialog_class.getName();

        IDialogSettings settings = TextEditorPlugin.getDefault().getDialogSettings();
        IDialogSettings dialog_settings= settings.getSection( name );
        if( dialog_settings == null )
        {
            dialog_settings= settings.addNewSection( name );
        }

        return dialog_settings;
    }

    /**
     * The search dialog "wrap search" flag.
     */
    private boolean m_search_dialog_is_wrap_search = true;

    /**
     * The search dialog "case sensitive" flag.
     */
    private boolean m_search_dialog_is_case_sensitive = false;

    /**
     * The search dialog "whole word" flag.
     */
    private boolean m_search_dialog_is_whole_word = false;

    /**
     * The search dialog "regular_expression" flag.
     */
    private boolean m_search_dialog_is_regular_expression = false;

    /**
     * The search dialog "incremental" flag.
     */
    @SuppressWarnings( "unused" )
    private boolean m_search_dialog_is_incremental = false;

    /**
     * The search dialog "last selection".
     */
    @SuppressWarnings( "unused" )
    private String m_search_dialog_last_selection = null;

    /**
     * The search dialog find history.
     */
    private List<String> m_find_history = new ArrayList<>();

    /**
     * The search dialog replace history.
     */
    private List<String> m_replace_history = new ArrayList<>();

    /**
     * The current search target.
     */
    private IFindReplaceTarget m_search_target = null;

    /**
     * The current search direction, based on the command, not the search dialog.
     */
    private boolean m_is_forward_search = false;

    /**
     * Reads the configuration parameters of the find/replace dialog.
     *
     * @return  Error status. True if an error occurs.
     */
    private boolean read_find_replace_configuration()
    {
        IDialogSettings dialog_settings = getDialogSettings();
        if( dialog_settings == null ) return true;

        m_search_dialog_is_wrap_search = dialog_settings.get( "wrap" ) == null || dialog_settings.getBoolean( "wrap" ); //$NON-NLS-1$ //$NON-NLS-2$
        m_search_dialog_is_case_sensitive = dialog_settings.getBoolean( "casesensitive" ); //$NON-NLS-1$
        m_search_dialog_is_whole_word = dialog_settings.getBoolean( "wholeword" ); //$NON-NLS-1$
        m_search_dialog_is_regular_expression = dialog_settings.getBoolean( "isRegEx" ); //$NON-NLS-1$
        m_search_dialog_is_incremental = dialog_settings.getBoolean( "incremental" ); //$NON-NLS-1$
        m_search_dialog_last_selection = dialog_settings.get( "selection" ); //$NON-NLS-1$

        String[] find_history= dialog_settings.getArray( "findhistory" ); //$NON-NLS-1$
        if( find_history != null )
        {
            m_find_history.clear();
            for( int i = 0; i < find_history.length; i++ )
            {
                m_find_history.add(find_history[i]);
            }
        }

        String[] replace_history= dialog_settings.getArray( "replacehistory" ); //$NON-NLS-1$
        if( replace_history != null )
        {
            m_replace_history.clear();
            for( int i = 0; i < replace_history.length; i++ )
            {
                m_replace_history.add(replace_history[i]);
            }
        }

        return false;
    }

    /**
     * Performs a find of the most recent find history, based on the current parameters,
     * and selects the string in the target if found.
     * Supports regular_expression if the search target supports it.
     * The search target and find history must be tested for validity before calling.
     *
     * @param offset    The starting offset of the search in the target.
     *
     * @return  The position of the history string if found, or -1 if not found.
     */
    private int find_and_select_regex( int offset )
    {
        if( m_search_target instanceof IFindReplaceTargetExtension3 )
        {
            try
            {
                return( ( (IFindReplaceTargetExtension3)m_search_target ).
                    findAndSelect( offset,
                                   m_find_history.get( 0 ),
                                   m_is_forward_search,
                                   m_search_dialog_is_case_sensitive && !m_search_dialog_is_regular_expression,
                                   m_search_dialog_is_whole_word && !m_search_dialog_is_regular_expression,
                                   m_search_dialog_is_regular_expression ) );
            }
            catch( PatternSyntaxException ex )
            {
                return( -1 );
            }
        }

        return m_search_target.
            findAndSelect( offset,
                           m_find_history.get( 0 ),
                           m_is_forward_search,
                           m_search_dialog_is_case_sensitive && !m_search_dialog_is_regular_expression,
                           m_search_dialog_is_whole_word && !m_search_dialog_is_regular_expression );
    }

    /**
     * Performs a search based on the current parameters. The search target must
     * be tested for validity before calling.
     *
     * @return  Error status. True if search was not successful, false otherwise.
     */
    private boolean search()
    {
        if( m_find_history.isEmpty() || ( m_find_history.get( 0 ).equals( "" ) ) ) //$NON-NLS-1$
        {
            Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_42 );
            Brief_for_eclipse.beep();
            return true;
        }

        Point selection = m_search_target.getSelection();
        int selection_start_position = selection.x;
        int selection_end_position = selection_start_position + selection.y;

        int index = find_and_select_regex( m_is_forward_search ?
                selection_end_position : selection_start_position );

        if( ( index == -1 ) && m_search_dialog_is_wrap_search )
        {
            index = find_and_select_regex( -1 );
            Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_43 );
            Brief_for_eclipse.beep();
        }

        if( index == -1 )
        {
            Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_44 );
            Brief_for_eclipse.beep();
            return true;
        }

        return false;
    }

    /**
     * Replaces the currently selected text in the search target.
     * Supports regular_expression if the search target supports it.
     * The search target and replace history must be tested for validity
     * before calling.
     *
     * @return  Error status. True if search was not successful, false otherwise.
     */
    private boolean replace_selection_regex()
    {
        String replace_string = ""; //$NON-NLS-1$

        if( !m_replace_history.isEmpty() )
        {
            replace_string = m_replace_history.get( 0 );
        }

        if( m_search_target instanceof IFindReplaceTargetExtension3 )
        {
            try
            {
                ( (IFindReplaceTargetExtension3)m_search_target ).
                    replaceSelection( replace_string,
                                      m_search_dialog_is_regular_expression );
            }
            catch( PatternSyntaxException | IllegalStateException exception )
            {
                return true;
            }
        }
        else
        {
            m_search_target.replaceSelection( replace_string );
        }

        // Update the last selection.
        IDialogSettings s= getDialogSettings();
        s.put( "selection", m_search_target.getSelectionText() ); //$NON-NLS-1$

        return false;
    }

    /**
     * Performs a "repeat" replace, in either direction, for the translate again command.
     * The repeat parameters are obtained from the FindReplaceDialog, so it must have
     * been used previously for this to work as expected.
     *
     * @param is_forward_search     The replace direction.
     */
    protected void replace_next_previous( boolean is_forward_search )
    {
        if( validate( m_workbench_window, m_active_editor ) ) return;

        IWorkbenchPart workbench_part = m_workbench_window.getPartService().getActivePart();
        m_search_target = workbench_part.getAdapter( IFindReplaceTarget.class );
        if( ( m_search_target == null ) || !m_search_target.canPerformFind() ) return;

        // Get the search dialog's last configuration.
        if( read_find_replace_configuration() ) return;

        m_is_forward_search = is_forward_search;
        if( search() ) return;

        replace_selection_regex();
    }

    /**
     * Calls the scrap buffer support to open the scrap dialog.
     */
    public void open_scrap_dialog()
    {
        m_scrap_buffer_support.open_scrap_dialog( m_workbench_window,
                                                  width_in_chars(),
                                                  (i_editor_paste)( Scrap_item selected_scrap_item ) ->
        {
            insert_paste( selected_scrap_item );
        } );
    }

    /**
     * Get the width, in characters, of the currently active editor.
     *
     * @return  The width, in characters, of the currently active editor.
     */
    protected int width_in_chars()
    {
        int average_character_width = JFaceTextUtil.getAverageCharWidth( m_styled_text );
        int width = m_styled_text.getClientArea().width - m_styled_text.getRightMargin() -
                ( 2 * average_character_width ) + ( average_character_width / 2 );
        int width_in_chars = width / average_character_width;

        return width_in_chars;
    }

    /**
     * Determines if the given editor can be modified, i.e. it is not read-only or
     * some other restriction.
     *
     * @param workbench_window  The workbench window of the editor.
     * @param editor            The editor.
     *
     * @return  True if modifiable, false otherwise.
     */
    private boolean is_modifiable( IWorkbenchWindow workbench_window,
                                   IEditorPart editor )
    {
        boolean is_modifiable = true;

        if( editor instanceof ITextEditorExtension2 )
        {
            is_modifiable &= ( ( (ITextEditorExtension2)editor ).isEditorInputModifiable() );
        }

        IWorkbenchPart workbench_part = workbench_window.getPartService().getActivePart();
        IFindReplaceTarget target = workbench_part.getAdapter( IFindReplaceTarget.class );
        if( target != null )
        {
            is_modifiable &= target.isEditable();

            if( target instanceof IFindReplaceTargetExtension2 )
            {
                is_modifiable &= ( ( (IFindReplaceTargetExtension2)target ).validateTargetState() );
            }
        }

        return is_modifiable;
    }

    /**
     * Determines if the given editor can be modified, i.e. it is not read-only or
     * some other restriction. It displays a message to the user if not.
     *
     * @param workbench_window  The workbench window of the editor.
     * @param editor            The editor.
     *
     * @return  True if modifiable, false otherwise.
     */
    private boolean validate( IWorkbenchWindow workbench_window,
                              IEditorPart active_editor )
    {
        if( !is_modifiable( workbench_window, active_editor ) )
        {
            Brief_for_eclipse.set_status_line( Messages.text_editor_proxy_46 );
            Brief_for_eclipse.beep();
            return true;
        }

        return false;
    }

    /**
     * Updates a given numbered bookmark with the current location in the active editor.
     *
     * @param bookmark_number   The bookmark number to use, 1-10.
     */
    protected void drop_bookmark( int bookmark_number )
    {
        m_bookmarks_support.drop_bookmark( bookmark_number, m_active_editor );
    }

    /**
     * Opens the jump bookmark dialog.
     */
    protected void jump_bookmark()
    {
        m_bookmarks_support.jump_bookmark( m_workbench_window );
    }

    /**
     * Listener to look for dialog activation so it can find the rename dialog.
     */
    private Listener m_rename_dialog_activate_listener = null;

    /**
     * Listener to return focus to the active editor upon completion of a rename resource.
     */
    private DisposeListener m_rename_dialog_dispose_listener = null;

    /**
     * The rename dialog's shell for use in the handler.
     */
    private Shell m_rename_dialogs_shell = null;

    /**
     * Listeners that return focus to the active editor after the rename command,
     * in Project Explorer, is complete.
     * Otherwise upon the dialog closing, the Project Explorer would still have focus.
     */
    private void add_rename_dialog_close_listener()
    {
        if( m_rename_dialog_dispose_listener != null )
        {
            m_rename_dialogs_shell.removeDisposeListener( m_rename_dialog_dispose_listener );
            m_rename_dialog_dispose_listener = null;
            m_rename_dialogs_shell = null;
        }

        m_rename_dialog_dispose_listener = new DisposeListener()
        {
            @Override
            public void widgetDisposed( DisposeEvent event )
            {
                m_rename_dialogs_shell.removeDisposeListener( m_rename_dialog_dispose_listener );
                m_rename_dialogs_shell = null;
                m_rename_dialog_dispose_listener = null;

                activate_editor();
            }
        };

        if( m_rename_dialog_activate_listener != null )
        {
            Display.getDefault().removeFilter( SWT.Activate, m_rename_dialog_activate_listener );
            m_rename_dialog_activate_listener = null;
        }

        m_rename_dialog_activate_listener = new Listener()
        {
            @Override
            public void handleEvent( final Event event )
            {
                // Looking for a rename dialog shell being activated.
                if( event.widget instanceof Shell )
                {
                    final Shell shell = (Shell)event.widget;
                    m_rename_dialogs_shell = shell;
                    shell.addDisposeListener( m_rename_dialog_dispose_listener );
                    Display.getDefault().removeFilter( SWT.Activate, m_rename_dialog_activate_listener );
                    m_rename_dialog_activate_listener = null;
                }
            }
        };

        Display.getDefault().addFilter( SWT.Activate, m_rename_dialog_activate_listener );
    }

    /**
     * Renames the active editor. It uses Project Explorer's "rename" if it is a resource there.
     * Otherwise, it just does a "save-as" within the editor.
     * Once the dialog closes, it tries to return focus to the active editor.
     */
    protected void change_output_file_name()
    {
        IEditorInput editor_input = m_active_editor.getEditorInput();
        if( editor_input == null ) return;

        IResource resource = editor_input.getAdapter( IResource.class );
        if( resource != null )
        {
            IWorkbenchPage workbench_page = m_workbench_window.getActivePage();
            if( workbench_page == null ) return;

            IViewPart view_part  = workbench_page.findView( "org.eclipse.ui.navigator.ProjectExplorer" ); //$NON-NLS-1$
            if( ( view_part == null ) || !( view_part instanceof ProjectExplorer ) ) return;
            workbench_page.activate( view_part );

            StructuredSelection structured_selection = new StructuredSelection( resource );
            Object object = structured_selection.getFirstElement();
            if( object == null ) return;
            ( (ProjectExplorer)view_part ).selectReveal( structured_selection );

            add_rename_dialog_close_listener();

            IHandlerService handler_service =
                    (IHandlerService)view_part.getSite().getService( IHandlerService.class );
            try
            {
                handler_service.executeCommand( IWorkbenchCommandConstants.FILE_RENAME, null );
                return;
            }
            catch( Exception exception )
            {
                Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                        "Command <" + IWorkbenchCommandConstants.FILE_RENAME + "> not found.", exception ); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
        }
        // Must not be a resource in the Project Explorer. Rename within the editor.
        else
        {
            if( execute_command_id( IWorkbenchCommandConstants.FILE_SAVE_AS ) ) return;
        }
    }

    /**
     * Returns focus to the active editor.
     */
    private void activate_editor()
    {
        if( m_active_editor_parent instanceof ITextEditor )
        {
            m_workbench_window.getActivePage().activate( m_active_editor_parent );
            return;
        }

        if( m_active_editor_parent instanceof MultiPageEditorPart )
        {
            MultiPageEditorPart multi_page_editor_part = (MultiPageEditorPart)m_active_editor_parent;
            m_workbench_window.getActivePage().bringToTop( multi_page_editor_part );
            m_workbench_window.getActivePage().activate( multi_page_editor_part );
            multi_page_editor_part.setActiveEditor( m_active_editor );

            return;
        }
    }
}
