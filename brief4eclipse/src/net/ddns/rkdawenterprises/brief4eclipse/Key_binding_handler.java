
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
            Activator.log_error( this.getClass().getName() + "." + new Throwable().getStackTrace()[0].getMethodName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                    "Handler not enabled." ); //$NON-NLS-1$

            return null;
        }

        // Send all events back to plug-in main class.
        return Activator.getDefault().execute( event );
    }
}
