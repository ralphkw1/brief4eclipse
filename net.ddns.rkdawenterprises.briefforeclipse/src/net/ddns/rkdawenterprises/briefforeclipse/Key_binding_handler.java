
/***************************************************************************//**
 * Copyright (c) 2020 Ralph and Donna Williamson and RKDAW Enterprises
 * <rkdawenterprises.ddns.net>. All rights reserved.
 * This program, and the accompanying materials, are provided under the terms
 * of the Eclipse Public License v2.0.
 ******************************************************************************/

package net.ddns.rkdawenterprises.briefforeclipse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * All of the command/key assignments in plugin.xml come here.
 */
public class Key_binding_handler extends AbstractHandler
{
    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException
    {
        if( !isEnabled() )
        {
            Brief_for_eclipse.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                    "Handler not enabled." ); //$NON-NLS-1$

            return null;
        }

        // Send all events back to plug-in main class.
        return Brief_for_eclipse.getDefault().execute( event );
    }
}
