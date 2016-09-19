/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
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
package joliex.rmi;

import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import jolie.StatefulContext;
import jolie.net.CommChannel;
import jolie.net.CommCore;
import jolie.net.ext.CommChannelFactory;
import jolie.net.ports.OutputPort;

public class RMICommChannelFactory extends CommChannelFactory
{
	public RMICommChannelFactory( CommCore commCore )
	{
		super( commCore );
	}

	public CommChannel createChannel( URI location, OutputPort port, StatefulContext ctx )
		throws IOException
	{
		try {
			Registry registry = LocateRegistry.getRegistry( location.getHost(), location.getPort() );
			JolieRemote remote = (JolieRemote)registry.lookup( location.getPath() );
			return new RMICommChannel( remote.createRemoteBasicChannel() );
		} catch( NotBoundException e ) {
			throw new IOException( e );
		}
	}
}
