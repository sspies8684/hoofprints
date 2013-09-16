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

import net.decix.bgpstack.BGPEvent;
import net.decix.bgpstack.BGPPeerFSM;

/**
 * Defines the methods a BGP state has to implement
 * 
 * @author sspies
 * 
 */
public interface BGPState
{

	/**
	 * Receives an event and processes it. Should return fast
	 * 
	 * @param event The event
	 */
	public void handleEvent(BGPEvent event);

	/**
	 * Gets the name of the represented state 
	 * @return "Active/Idle/Established..."
	 */
	public String getName();
	
	public BGPPeerFSM getFsm();

}
