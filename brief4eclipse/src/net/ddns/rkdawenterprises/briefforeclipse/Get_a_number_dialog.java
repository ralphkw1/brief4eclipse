
/***************************************************************************//**
 * Copyright (c) 2020 Ralph and Donna Williamson and RKDAW Enterprises
 * <rkdawenterprises.ddns.net>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0.
 ******************************************************************************/

package net.ddns.rkdawenterprises.briefforeclipse;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import com.google.common.base.CharMatcher;

/**
 * Dialog which obtains a number within a given range. Used by jump to bookmark and goto line number.
 */
public class Get_a_number_dialog extends Abstract_text_dialog
{
    /**
     * Creates the dialog.
     *
     * @param parent_shell      The parent shell.
     * @param editor            The active editor.
     * @param initial_number    The initial number to display. The text is selected.
     * @param min_number        The minimum number allowed to be entered.
     * @param max_number        The maximum number allowed to be entered.
     * @param prompt_format     The format of the prompt to the user. Used by String.format(), so it
     *                          needs to conform to that specification.
     */
    protected Get_a_number_dialog( Shell parent_shell,
                                   IEditorPart editor,
                                   int initial_number,
                                   int min_number,
                                   int max_number,
                                   String prompt_format )
    {
        super( parent_shell,
               editor,
               String.format( prompt_format, initial_number ) + "     " ); //$NON-NLS-1$

        m_number = initial_number;
        m_number_prompt_format = prompt_format;
        m_min_number = min_number;
        m_max_number = max_number;
    }

    /**
     * The number that is obtained from the dialog.
     */
    private int m_number = 1;

    /**
     * Getter.
     * @return  The number that is obtained from the dialog.
     */
    public int get_number()
    {
        return m_number;
    }

    /**
     * The user prompt format. Used by String.format(), so it needs to conform to that specification.
     */
    private final String m_number_prompt_format;

    @Override
    protected void create_contents( Composite parent )
    {
        super.create_contents( parent );

        // Select the current number so it can be overwritten by the user.
        int start = m_number_prompt_format.indexOf( "%d" ); //$NON-NLS-1$
        int end = start + String.format( "%d", m_number ).length(); //$NON-NLS-1$
        get_text().setSelection( start, end );
    }

    /**
     * The minimum number allowed to be entered.
     */
    private final int m_min_number;

    /**
     * The maximum number allowed to be entered.
     */
    private final int m_max_number;

    @Override
    protected void button_pressed( int id )
    {
        if( IDialogConstants.OK_ID == id )
        {
            String text = get_text_string();
            int start = m_number_prompt_format.indexOf( "%d" ); //$NON-NLS-1$

            m_number = Integer.parseInt( CharMatcher.inRange( '0', '9' ).retainFrom( text.substring( start ) ) );

            if( m_number < m_min_number ) m_number = m_min_number;

            if( m_number > m_max_number ) m_number = m_max_number;
        }
    }
}
