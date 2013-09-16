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
import net.decix.bgpstack.types.IPv4Prefix;

public class PrefixFilter implements BGPConstants
{
	private IPv4Prefix prefix;
	private int upperBound;
	private int lowerBound;
	private boolean allowed;
	
	public PrefixFilter(IPv4Prefix prefix, int upperBound, int lowerBound, boolean allowed)
	{
		this.prefix = prefix;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.allowed = allowed;
	}

	/**
	 * @return the prefix
	 */
	public IPv4Prefix getPrefix()
	{
		return prefix;
	}

	/**
	 * @return the upperBound
	 */
	public int getUpperBound()
	{
		return upperBound;
	}

	/**
	 * @return the lowerBound
	 */
	public int getLowerBound()
	{
		return lowerBound;
	}

	/**
	 * @return the allowed
	 */
	public boolean isAllowed()
	{
		return allowed;
	}

	public void setAllowed(boolean allow)
	{
		this.allowed = allow;
	}
	
	
	
}
