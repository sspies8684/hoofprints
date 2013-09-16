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

import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.NLRI;
import net.decix.bgpstack.types.pathattributes.BGPPathAttributeSequence;

/**
 * Represents a BGP Route
 * 
 * @author sspies
 * 
 */
public class BGPRoute
{
	private BGPPathAttributeSequence sequence;
	private NLRI prefix;

	/**
	 * Instantiates a new {@link BGPRoute} object
	 * @param prefix the contained prefix
	 * @param sequence a {@link BGPPathAttributeSequence} which describes the NLRI
	 */
	public BGPRoute(NLRI prefix, BGPPathAttributeSequence sequence)
	{
		this.prefix = prefix;
		this.sequence = sequence;
	}

	protected BGPRoute()
	{
	}

	/**
	 * Gets the {@link BGPPathAttributeSequence} which describes the NLRI
	 * 
	 * @return the attribute sequence
	 */
	public BGPPathAttributeSequence getSequence()
	{
		return sequence;
	}

	/**
	 * Gets the contained prefix
	 * 
	 * @return the contained prefix
	 */
	public NLRI getPrefix()
	{
		return prefix;
	}

	protected void setSequence(BGPPathAttributeSequence sequence)
	{
		this.sequence = sequence;
	}

	protected void setPrefix(IPv4Prefix prefix)
	{
		this.prefix = prefix;
	}

	@Override
	public String toString()
	{
		return "Route: " + prefix.toString();
	}

}
