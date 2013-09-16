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

import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPPeerFSM;
import net.decix.bgpstack.types.BGPOpenMessage;

/**
 * This class handles the BGP OPEN_SENT state
 * 
 * @author sspies
 * 
 */
public class BGPOpenSentState implements BGPState
{

	private BGPPeerFSM fsm;
	
	private static Logger logger = Logger.getLogger(BGPOpenSentState.class.getCanonicalName());


	/**
	 * Instantiates a new BGPOpenSentState object
	 * 
	 * @param fsm the finite state machine
	 */
	public BGPOpenSentState(BGPPeerFSM fsm)
	{
		this.fsm = fsm;
	}

	public void handleEvent(BGPEvent event)
	{
		switch (event.getEventType())
		{
			case BGPOpen:
				BGPOpenMessage openMessage = (BGPOpenMessage) event.getMessage();

				if (openMessage.getAsNumber() == fsm.getRemoteAsn())
				{
					fsm.setReceivedParameters(openMessage.getParameters());
					fsm.sendPacket(fsm.generateKeepaliveMessage());
					fsm.setHoldTime(Math.min(fsm.getHoldTime(), openMessage.getHoldTime()));
					fsm.setKeepaliveTime(fsm.getHoldTime() / 3);
					fsm.restartKeepaliveTimer();
					fsm.setCurrentState(fsm.getOpenConfirmState());
				}
				else
				{
					fsm.sendPacket(fsm.generateShutdownMessage());
					fsm.shutdown();
					fsm.setCurrentState(fsm.getActiveState());
				}
			break;

			case NotifMsg:
			case TcpConnectionFails:
			{
				fsm.shutdown();
				fsm.setCurrentState(fsm.getActiveState());
			}
			break;

			default:
				logger.warning("Unhandled event: " + event);

		}

	}

	public String getName()
	{
		return "OpenSent";
	}

	public BGPPeerFSM getFsm()
	{
		return fsm;
	}

}
