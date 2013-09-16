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
package net.decix.rs.conf;

import net.decix.bgpstack.BGPConstants;

public class ASPathFilter implements BGPConstants
{
	private boolean allowed;
	private int sourceAs;

	public ASPathFilter(int sourceAs, boolean allowed)
	{
		this.sourceAs = sourceAs;
		this.allowed = allowed;
	}

	/**
	 * @return the allowed
	 */
	public boolean isAllowed()
	{
		return allowed;
	}

	/**
	 * @return the sourceAs
	 */
	public int getSourceAs()
	{
		return sourceAs;
	}

}
