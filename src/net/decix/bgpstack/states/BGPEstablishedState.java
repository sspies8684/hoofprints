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

package net.decix.bgpstack.states;

import java.util.logging.Logger;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPPeerFSM;

/**
 * This class represents the BGP ESTABLISHED state
 * 
 * @author sspies
 * 
 */
public class BGPEstablishedState implements BGPState, BGPConstants
{

	private BGPPeerFSM fsm;
	
	private static Logger logger = Logger.getLogger(BGPEstablishedState.class.getCanonicalName());


	/**
	 * instantiates a new BGPEstablishedState object
	 * 
	 * @param fsm the finite state machine
	 */
	public BGPEstablishedState(BGPPeerFSM fsm)
	{
		this.fsm = fsm;

	}

	public void handleEvent(BGPEvent event)
	{
		switch (event.getEventType())
		{
			case KeepAliveMsg:
				fsm.restartHoldTimer();
			break;

			case KeepaliveTimer_Expires:
				fsm.sendPacket(fsm.generateKeepaliveMessage());
				fsm.restartKeepaliveTimer();
			break;

			case UpdateMsg:
				fsm.learnUpdate(event.getMessage());
				fsm.restartHoldTimer();
			break;

			case TcpConnectionFails:
				fsm.sendPacket(fsm.generateShutdownMessage());
				fsm.shutdown();
				fsm.setCurrentState(fsm.getActiveState());
			break;
			case AutomaticStop:
			case ManualStop:
			case HoldTimer_Expires:
				fsm.sendPacket(fsm.generateShutdownMessage());
				fsm.shutdown();
				fsm.setCurrentState(fsm.getIdleState());
			break;

			default:
				logger.warning("Unhandled event: " + event);

		}

	}

	public String getName()
	{
		return "Established";
	}

	public BGPPeerFSM getFsm()
	{
		return fsm;
	}

}
