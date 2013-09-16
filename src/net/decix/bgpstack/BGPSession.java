/**
 * Hoofprints - An Extensible Testbed for Route-Servers
 * Copyright (C) 2009 Sebastian Spies
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package net.decix.bgpstack;

import java.net.InetAddress;
import java.net.Socket;



/**
 * Represents a BGP session. Talks to a socket.
 * 
 * @author sspies
 * 
 */
public interface BGPSession extends Cloneable, BGPConstants
{

	public void connect();
	public void shutdown();
	public void start();
	public Socket getSocket();
	public void setSocket(Socket socket);
	public BGPPeerFSM getFsm();
	public InetAddress getMyAddress();
	
	public BGPSession clone();
}
