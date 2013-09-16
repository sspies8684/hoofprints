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

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a configuration abstraction of a BGP peer
 * 
 * @author sspies
 * 
 */
public class Configuration implements Cloneable
{
	private long asn;
	private int holdTime;
	private List<Neighbor> neighbors = new LinkedList<Neighbor>();
	private List<PrefixFilter> globalFilters = new LinkedList<PrefixFilter>();

	/**
	 * Gets the configured ASN - autonomous system number
	 * 
	 * @return
	 */
	public long getAsn()
	{
		return asn;
	}

	/**
	 * Sets the ASN - autonomous system number of the configuration
	 * 
	 * @param asn
	 */
	public void setAsn(long asn)
	{
		this.asn = asn;
	}

	/**
	 * Adds a neighbor object to the list of neighbors
	 * 
	 * @param n the neighbor to add
	 */
	public void addNeighbor(Neighbor n)
	{
		this.neighbors.add(n);
	}


	/**
	 * Gets the list of neighbors of the configuration
	 * 
	 * @return the list of configured neighbors
	 */
	public List<Neighbor> getNeighbors()
	{
		return neighbors;
	}

	/**
	 * Gets the hold time of the configuration
	 * 
	 * @return the configured hold time
	 */
	public int getHoldTime()
	{
		return holdTime;
	}

	/**
	 * Sets the hold time of the configuration
	 * 
	 * @param holdTime the new hold time
	 */
	public void setHoldTime(int holdTime)
	{
		this.holdTime = holdTime;
	}

	/**
	 * @return the globalFilters
	 */
	public List<PrefixFilter> getGlobalFilters()
	{
		return globalFilters;
	}
	
	public void addGlobalFilter(PrefixFilter filter)
	{
		globalFilters.add(filter);
	}

	public void addGlobalFilters(List<PrefixFilter> martians)
	{
		this.globalFilters.addAll(martians);
		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Configuration clone = new Configuration();
		clone.setAsn(asn);
		clone.setHoldTime(holdTime);
		clone.globalFilters.addAll(globalFilters);
		clone.neighbors.addAll(neighbors);
		return clone;
	}

}
