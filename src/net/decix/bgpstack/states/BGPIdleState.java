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
 * This class represents the BGP IDLE state
 * 
 * @author sspies
 */
public class BGPIdleState implements BGPConstants, BGPState
{

	private BGPPeerFSM fsm;
	
	private static Logger logger = Logger.getLogger(BGPIdleState.class.getCanonicalName());


	/**
	 * instantiates a new BGPIdleState object
	 * 
	 * @param fsm the finite state machine
	 */
	public BGPIdleState(BGPPeerFSM fsm)
	{
		this.fsm = fsm;

	}

	public void handleEvent(BGPEvent event)
	{
		switch (event.getEventType())
		{
			case TcpConnected:
				fsm.shutdown();
			break;

			case ManualStart:
			case AutomaticStart:
				// int connectRetryCounter = 0;

				// while ((!fsm.isConnected()) && connectRetryCounter++ <
				// BGP_CONNECT_RETRIES)
				// fsm.connect();
				//
				// if (fsm.isConnected())
				// {
				// fsm.getSession().start();
				// fsm.sendPacket(fsm.generateOpenMessage());
				// fsm.setCurrentState(fsm.getOpenSentState());
				// }
				// else
				fsm.setCurrentState(fsm.getActiveState());
			break;

			case ManualStart_with_PassiveTcpEstablishment:
			case AutomaticStart_with_PassiveTcpEstablishment:
				fsm.setCurrentState(fsm.getActiveState());
			break;

			default:
				logger.warning("Unhandled event: " + event);
		}

	}

	public String getName()
	{
		return "Idle";
	}

	public BGPPeerFSM getFsm()
	{
		return fsm;
	}

}
