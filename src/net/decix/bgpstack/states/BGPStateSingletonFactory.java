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

import net.decix.bgpstack.BGPPeerFSM;

/**
 * Instantiates the default set of states lazily
 * 
 * @author sspies
 * 
 */
public class BGPStateSingletonFactory
{
	private BGPIdleState idleState = null;
	private BGPActiveState activeState = null;
	private BGPOpenSentState openSentState = null;
	private BGPOpenConfirmState openConfirmState = null;
	private BGPEstablishedState establishedState = null;

	private BGPPeerFSM fsm;

	/**
	 * Instantiates a new BGPStateSingletonFactory
	 * 
	 * @param fsm the finite state machine
	 */
	public BGPStateSingletonFactory(BGPPeerFSM fsm)
	{
		this.fsm = fsm;
	}

	/**
	 * Creates a new Idle state
	 * 
	 * @return the idle state
	 */
	public BGPIdleState createIdleState()
	{
		if (idleState == null) idleState = new BGPIdleState(fsm);
		return idleState;
	}

	/**
	 * Creates a new ACTIVE state
	 * 
	 * @return the ACTIVE state
	 */
	public BGPActiveState createActiveState()
	{
		if (activeState == null) activeState = new BGPActiveState(fsm);
		return activeState;
	}

	/**
	 * Creates a new OPEN_SENT state
	 * 
	 * @return the OPEN_SENT state
	 */
	public BGPOpenSentState createOpenSentState()
	{
		if (openSentState == null) openSentState = new BGPOpenSentState(fsm);
		return openSentState;
	}

	/**
	 * Creates a new OPEN_CONFIRM state
	 * 
	 * @return the OPEN_CONFIRM state
	 */
	public BGPOpenConfirmState createOpenConfirmState()
	{
		if (openConfirmState == null) openConfirmState = new BGPOpenConfirmState(fsm);
		return openConfirmState;
	}

	/**
	 * Creates a new ESTABLISHED state
	 * 
	 * @return the ESTABLISHED state
	 */
	public BGPEstablishedState createEstablishedState()
	{
		if (establishedState == null) establishedState = new BGPEstablishedState(fsm);
		return establishedState;
	}

}
