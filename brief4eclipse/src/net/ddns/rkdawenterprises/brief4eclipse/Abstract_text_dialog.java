
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

/**
 * Used as the superclass functionality of the command and get_a_number subclass dialogs.
 * Contains a text box and two buttons, the buttons being ok, and cancel.
 */
public abstract class Abstract_text_dialog extends Abstract_simple_dialog
{
    /**
     * The initial string to display in the text box.
     */
    private String m_initial_string = null;

    /**
     * Creates the text dialog with the given parent shell provider.
     *
     * @param shell_provider Provider that returns the shell for this dialog.
     * @param editor    The currently active editor.
     */
    protected Abstract_text_dialog( Shell parent_shell,
                                    IEditorPart editor,
                                    String initial_string )
    {
        super( parent_shell, editor );
        m_initial_string = initial_string;
    }

    /**
     * The text box for the dialog.
     */
    private Text m_text = null;

    /**
     * Getter.
     * @return The text box for the dialog.
     */
    protected Text get_text()
    {
        return m_text;
    }

    @Override
    protected boolean buttons_below() { return false; }

    @Override
    protected void create_contents( Composite parent )
    {
        m_text = create_text( parent,
                              m_initial_string,
                              SWT.DEFAULT,
                              super.get_buttons_height(),
                              ( m_initial_string != null ) ? m_initial_string.length() + 1 : SWT.DEFAULT );

        m_text_string = m_initial_string;
    }

    /**
     * The text in the text box when modified.
     */
    private String m_text_string = null;

    /**
     * Getter.
     * @return  The text box string.
     */
    public String get_text_string()
    {
        return new String( m_text_string );
    }

    /**
     * Create the text box.
     * @param parent            The parent that the button is contained in.
     * @param initial_string    The initial text string.
     * @param width_hint        The requested width or SWT.DEFAULT. Optional.
     * @param height_hint       The requested width or SWT.DEFAULT. Optional.
     * @param starting_width_in_chars
     *                          The initial width of the text box in characters assuming the default font.
     *
     * @return  The newly created text box.
     */
    protected Text create_text( Composite parent,
                                String initial_string,
                                int width_hint,
                                int height_hint,
                                int starting_width_in_chars )
    {
        Text text = new Text( parent, SWT.SINGLE );

        GridData gd = new GridData( SWT.FILL,
                                    SWT.CENTER,
                                    true,
                                    true,
                                    1,
                                    1 );

        if( ( starting_width_in_chars != SWT.DEFAULT ) && ( width_hint == SWT.DEFAULT ) )
        {
            // Specifying width hint in characters.
            gd.widthHint = (int)( m_font_metrics.getAverageCharacterWidth() * starting_width_in_chars );
        }
        else
        {
            gd.widthHint = width_hint;
        }
        gd.heightHint = height_hint;
        text.setLayoutData( gd );

        if( initial_string != null ) text.setText( initial_string );

        text.addModifyListener( e ->
        {
            m_text_string = ( (Text)( e.getSource() ) ).getText();
        } );

        return text;
    }
}
