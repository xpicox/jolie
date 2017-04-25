/***************************************************************************
 *   Copyright (C) 2014 by Claudio Guidi <cguidi@italianasoftware.com>          *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/

package jolie.monitoring.events;

import jolie.StatefulContext;
import jolie.monitoring.MonitoringEvent;
import jolie.runtime.Value;

/**
 *
 * @author claudio
 */
public class OperationCallEvent extends MonitoringEvent
{
	public static final int SUCCESS = 0;
	public static final int FAULT = 1;
	
	
	public OperationCallEvent( StatefulContext sessionContext, String operationName, String messageId, int status, String details, String outputPort, Value message ) {
		super( "OperationCall", Value.create(), sessionContext );
		
		data().getFirstChild( "operationName" ).setValue( operationName );
		data().getFirstChild( "processId" ).setValue( sessionContext.getSessionId() );
		data().getFirstChild( "messageId" ).setValue( messageId );
		data().getFirstChild( "status" ).setValue( status );
		data().getFirstChild( "details" ).setValue( details );
		data().getFirstChild( "outputPort" ).setValue( outputPort );
        data().getFirstChild( "message" ).deepCopy( message );
	}
}
