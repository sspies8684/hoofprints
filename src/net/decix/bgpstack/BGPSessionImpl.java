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

import static net.decix.bgpstack.util.Utility.dumpBytes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

import net.decix.bgpstack.types.BGPMessage;
import net.decix.bgpstack.types.BGPPacket;
import net.decix.bgpstack.util.BGPPacketInputStream;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a BGP session. Talks to a socket.
 * 
 * @author sspies
 * 
 */
public class BGPSessionImpl extends Thread implements BGPSession, BGPConstants, Cloneable
{
	private Socket socket;
	private BGPPeerFSM fsm;
	private Logger logger = Logger.getLogger(BGPSessionImpl.class.getCanonicalName());

	private InetAddress remoteAddress;
	private int remotePort;
	private InetAddress myAddress;

	/**
	 * Instantiates a new {@link BGPSessionImpl} object
	 * 
	 * @param remoteAddress the inet address of the remote peer
	 * @param remotePort the tcp port of the remote peer
	 * @param myAddress the inet address of the local peer
	 * @param finiteStateMachine a finite state machine
	 * @throws IOException if something with the socket is wrong
	 */
	public BGPSessionImpl(InetAddress remoteAddress, int remotePort, InetAddress myAddress,
							BGPPeerFSM finiteStateMachine) throws IOException
	{
		this.myAddress = myAddress;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.socket = new Socket();

		if (myAddress != null)
		{
			SocketAddress sa = new InetSocketAddress(myAddress, 0);
			this.socket.bind(sa);
		}

		logger.finest("instantiated " + remoteAddress + ":" + remotePort + " from " + myAddress
						+ " and FSM: " + finiteStateMachine);

		if (finiteStateMachine != null)
			setFsm(finiteStateMachine);
	}

	/*
	 * Events-producing methods
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	/**
	 * Main loop. Reads and parses packets. Notifies the Finite State Machine of the event.
	 */
	public void run()
	{
		setName("session " + socket.getInetAddress() + " " + socket.getLocalAddress());

		try
		{
			// BufferedInputStream bis = new BufferedInputStream();
			BGPPacketInputStream packetInputStream = new BGPPacketInputStream(socket.getInputStream());

			while (true)
			{
				byte[] packet = {};
				packet = packetInputStream.readBGPPacket();

				if (packet == null)
					break;
				BGPPacket p = new BGPPacket(socket.getInetAddress(), packet, this);

				for (BGPMessage message : p.getMessages())
					fsm.receiveEvent(BGPEvent.fromMessage(message));

			}
			logger.fine("socket " + socket + " has been closed");

		}
		catch (IOException e)
		{
			logger.throwing(logger.getName(), "run", e);
		}
		catch (BGPException e)
		{
			logger.throwing(logger.getName(), "run", e);
		}
		finally
		{
			fsm.receiveEvent(new BGPEvent(EVENT_TYPE.TcpConnectionFails));
		}

	}

	/**
	 * Connects the socket to the remote address
	 */
	public void connect()
	{
		try
		{
			logger.finer("connecting to: " + remoteAddress + ":" + remotePort + " from "
							+ this.socket.getLocalAddress() + ":" + this.socket.getLocalPort());
			this.socket.connect(new InetSocketAddress(remoteAddress, remotePort), BGP_CONNECT_TIMEOUT);

		}
		catch (IOException e)
		{
			logger.throwing(getClass().getCanonicalName(), "connect", e);
		}

	}

	/**
	 * Shuts the socket connection down
	 */
	public void shutdown()
	{
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			logger.throwing(BGPSessionImpl.class.getCanonicalName(), "shutdown", e);
		}
	}

	/*
	 * 
	 * 
	 * Methods, that cope with the socket
	 */

	/**
	 * Gets the address of the remote peer
	 * 
	 * @return the address of the remote peer
	 */
	public InetAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	/**
	 * Gets the address of the local peer
	 * 
	 * @return the address of the local peer
	 */
	public InetAddress getMyAddress()
	{
		return myAddress;
	}

	public void setSocket(Socket socket)
	{
		this.socket = socket;
	}

	@Override
	public String toString()
	{
		return getMyAddress() + " " + getRemoteAddress();
	}

	/**
	 * Gets the socket which is to talk to the remote peer
	 * 
	 * @return the socket which is to talk to the remote peer
	 */
	public Socket getSocket()
	{
		return socket;
	}

	/**
	 * Sets the Finite State Machine
	 * 
	 * @param fsm the finite state machine
	 */
	public void setFsm(BGPPeerFSM fsm)
	{
		this.fsm = fsm;
	}

	/**
	 * Gets the Finite State Machine
	 * 
	 * @return
	 */
	public BGPPeerFSM getFsm()
	{
		return fsm;
	}

	@Override
	public BGPSession clone()
	{
		logger.finest("Cloning: " + fsm.getMyAsn());
		try
		{
			return new BGPSessionImpl(remoteAddress, remotePort, myAddress, fsm);
		}
		catch (IOException e)
		{
			logger.severe("Could not clone " + this);
			return null;
		}
	}

}
