
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Used as the superclass functionality of the table and text subclass dialogs.
 * Creates a simple dialog that positions itself at the bottom of the editor.
 */
public abstract class Abstract_simple_dialog extends Window
{
    /**
     * The active editor that the dialog will operate on.
     */
    private IEditorPart m_active_editor = null;

    /**
     * Constructs the text dialog instance with the given parent shell.
     *
     * @param parent_shell  The parent shell.
     * @param editor    The currently active editor.
     */
    protected Abstract_simple_dialog( Shell parent_shell,
                                     IEditorPart editor  )
    {
        this( new SameShellProvider( parent_shell ),
              editor );
    }

    /**
     * Creates the text dialog with the given parent shell provider.
     *
     * @param shell_provider    Provider that returns the shell for this dialog.
     * @param editor    The currently active editor.
     */
    protected Abstract_simple_dialog( IShellProvider shell_provider,
                                     IEditorPart editor  )
    {
        super( shell_provider );
        setShellStyle( SWT.NO_TRIM | getDefaultOrientation() );
        setBlockOnOpen( true );
        m_active_editor = editor;
    }

    /**
     * The current font information.
     */
    protected FontMetrics m_font_metrics = null;

    /**
     * Initializes the font and the font measurement information,
     * which is used when creating the dialog components.
     *
     * @param control   The widget to use to obtain the GC for setting the default font and obtaining the font metrics.
     */
    private void initialize_font_metrics( Control control )
    {
        GC gc = new GC( control );
        gc.setFont( JFaceResources.getDialogFont() );
        m_font_metrics = gc.getFontMetrics();
        gc.dispose();
    }

    /**
     * Changes the location of the button bar.
     * Empty method; Meant to be overridden by the subclass.
     *
     * @return True if the subclass wants the buttons below the additional content,
     *         i.e. the composite will be a single column, or false if subclass wants
     *         the buttons to the right of the additional content, i.e. the composite
     *         will be two columns.
     */
    protected boolean buttons_below() { return true; }

    @Override
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent,
                                             SWT.NONE );

        Shell shell = composite.getShell();

        Dialog.applyDialogFont( composite );

        initialize_font_metrics( composite );

        GridLayout layout = new GridLayout( buttons_below() ? 1 : 2, false );
        layout.marginHeight = Dialog.convertVerticalDLUsToPixels( m_font_metrics, IDialogConstants.VERTICAL_MARGIN ) / 2;
        layout.marginWidth = Dialog.convertHorizontalDLUsToPixels( m_font_metrics, IDialogConstants.HORIZONTAL_MARGIN ) / 2;
        layout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(m_font_metrics, IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(m_font_metrics, IDialogConstants.HORIZONTAL_SPACING);

        composite.setLayout(layout);

        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        composite.setBackground( shell.getDisplay().getSystemColor( SWT.COLOR_WIDGET_BACKGROUND ) );

        // Call the subclass for the remaining content.
        create_contents( composite );

        create_button_bar( composite );

        add_mouse_drag_listeners( shell, composite );

        StyledText styled_text =
                (StyledText)( ( (ITextEditor)m_active_editor ).getAdapter( Control.class ) );
        styled_text.addListener( SWT.Resize, m_reposition_listener );
        shell.addListener( SWT.Activate, m_reposition_listener );

        return composite;
    }

    /**
     * Creates the additional content above or beside the buttons.
     *
     * @param parent
     */
    protected abstract void create_contents( Composite parent );

    /**
     * Used to tell when the dialog is opened or editor moved so it can be positioned properly.
     */
    private Listener m_reposition_listener = new Listener()
    {
        @Override
        public void handleEvent( Event event )
        {
            dialog_default_position();
        }
    };

    /**
     * Places the dialog at the bottom left corner of the active editor.
     */
    private void dialog_default_position()
    {
        // Find the positional origin of the active editor.
        StyledText styled_text =
                (StyledText)( ( (ITextEditor)m_active_editor ).getAdapter( Control.class ) );
        Point active_editor_origin = styled_text.toDisplay( 0, 0 );
        Point active_editor_size = styled_text.getSize();
        Point dialog_size = getShell().getSize();
        getShell().setLocation( active_editor_origin.x,
                                active_editor_origin.y + active_editor_size.y - dialog_size.y );
    }

    /**
     * Creates the component that contains the buttons.
     *
     * @param parent    The parent that the button bar is contained in.
     */
    private void create_button_bar( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;  // Incremented by the subclass when additional buttons are created.
        layout.makeColumnsEqualWidth = false;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels( m_font_metrics, IDialogConstants.HORIZONTAL_SPACING );
        composite.setLayout( layout );

        GridData data = new GridData( GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER );
        composite.setLayoutData( data );
        composite.setFont( parent.getFont() );

        create_buttons( composite, get_buttons_height() );

        create_default_buttons( composite, get_buttons_height() );
    }

    /**
     * Empty method; Meant to be overridden by subclass if the default is not acceptable.
     *
     * @return  The default height the buttons will be created with.
     */
    protected int get_buttons_height()
    {
        return( (int)( m_font_metrics.getHeight() * 1.6 ) );   // Just a WAG...
    }

    /**
     * Adds additional buttons to the button bar.
     * Empty method; Meant to be overridden by subclass to add additional buttons if desired.
     *
     * @param parent    The parent the buttons will be contained in.
     * @param height    The requested height of the buttons.
     */
    protected void create_buttons( Composite parent, int height ) {}

    /**
     * Creates the "Ok" and "Cancel" buttons.
     *
     * @param parent    The parent container the buttons are created in.
     * @param height    The desired height of the buttons.
     */
    protected void create_default_buttons( Composite parent, int height )
    {
        create_button( parent,
                       IDialogConstants.OK_ID,
                       IDialogConstants.OK_LABEL,
                       null,
                       SWT.DEFAULT,
                       height,
                       true );

        create_button( parent,
                       IDialogConstants.CANCEL_ID,
                       null,
                       "icons/cancel_24px.svg", //$NON-NLS-1$
                       SWT.DEFAULT,
                       height,
                       false );
    }

    /**
     * Creates a button for the button bar.
     *
     * @param parent            The parent that the button is contained in.
     * @param id                The IDialogConstants ID for this button.
     * @param label             The button's label or null. Optional.
     * @param image_path        The path to the button's image or null. Optional.
     * @param width_hint        The requested width or SWT.DEFAULT. Optional.
     * @param height_hint       The requested width or SWT.DEFAULT. Optional.
     * @param default_button    Indicates that this is the default button for the window.
     *
     * @return The newly created button.
     */
    protected Button create_button( Composite parent,
                                    int id,
                                    String label,
                                    String image_path,
                                    int width_hint,
                                    int height_hint,
                                    boolean default_button )
    {
        ((GridLayout) parent.getLayout()).numColumns++;

        Button button = new Button( parent, SWT.PUSH );

        if( label != null ) button.setText( label );

        button.setFont( JFaceResources.getDialogFont() );

        button.setData( Integer.valueOf( id ) );

        if( image_path != null )
        {
            Bundle bundle = FrameworkUtil.getBundle( getClass() );
            URL url = FileLocator.find( bundle, new Path( image_path ), null );
            ImageDescriptor imageDesc = ImageDescriptor.createFromURL( url );
            Image image = imageDesc.createImage();
            button.setImage( image );

            button.addDisposeListener( e ->
            {
                image.dispose();
            } );
        }

        button.addSelectionListener( widgetSelectedAdapter( event ->
        {
            button_pressed_default( ( (Integer) event.widget.getData() ).intValue() );
        }));

        if( default_button )
        {
            Shell shell = parent.getShell();
            if( shell != null )
            {
                shell.setDefaultButton( button );
            }
        }

        GridData gd = new GridData( SWT.FILL,
                                    SWT.FILL,
                                    true,
                                    true );

        gd.widthHint = width_hint;
        gd.heightHint = height_hint;
        button.setLayoutData( gd );

        return button;
    }

    /**
     * Handler for the default buttons.
     *
     * @param id    The button ID.
     */
    private void button_pressed_default( int id )
    {
        button_pressed( id );

        if( IDialogConstants.OK_ID == id )
        {
            setReturnCode( OK );
            close();
            return;
        }

        if( IDialogConstants.CANCEL_ID == id )
        {
            setReturnCode( CANCEL );
            close();
            return;
        }
    }

    /**
     * Handles button presses for any additional buttons.
     * Empty method; Meant to be overridden by the subclass for additional buttons handling.
     *
     * @param id    The buttons ID that was pressed.
     */
    protected void button_pressed( int id ) {}

    @Override
    public boolean close()
    {
        return super.close();
    }

    /**
     * The starting point of a mouse drag. Used to translate coordinates from the
     * shell to the dialog during a mouse drag of the dialog.
     */
    private Point m_mouse_drag_start = null;

    /**
     * Having a shell with "SWT.NO_TRIM" removes ability to reposition the dialog
     * with the mouse.
     *
     * @param shell     The dialogs shell.
     * @param composite The object to add the listeners to.
     */
    private void add_mouse_drag_listeners( Shell shell,
                                           Composite composite )
    {
        composite.addListener( SWT.MouseDown, event ->
        {
            m_mouse_drag_start = new Point( event.x,
                                            event.y );
        } );

        composite.addListener( SWT.MouseUp, event ->
        {
            m_mouse_drag_start = null;
        } );

        composite.addListener( SWT.MouseMove, event ->
        {
            if( m_mouse_drag_start != null )
            {
                Point p = shell.getDisplay().map( shell, null, event.x, event.y );
                int x = p.x - m_mouse_drag_start.x;
                int y = p.y - m_mouse_drag_start.y;
                shell.setLocation( x, y );
                if( m_reposition_listener != null )
                {
                    shell.removeListener( SWT.Activate, m_reposition_listener );
                    m_reposition_listener = null;
                }
            }
        } );
    }
}
