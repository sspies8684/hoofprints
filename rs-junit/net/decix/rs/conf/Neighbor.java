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

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a BGP peer
 * 
 * @author sspies
 * 
 */
public class Neighbor
{

	private InetAddress address;
	private long asn;
	private int weight;
	private String description;
	private boolean rsClient = false;
	private boolean ebgpMultihop = false;
	private boolean nextHopSelf = false;
	private boolean sendCommunity = false;
	private boolean passive = false;
	private boolean attributeUnchanged = false;
	private int port = 179;
	private InetAddress localAddress;

	private List<PrefixFilter> prefixOutFilter = new LinkedList<PrefixFilter>();
	private List<PrefixFilter> prefixInFilter = new LinkedList<PrefixFilter>();
	private List<ASPathFilter> asPathFilters = new LinkedList<ASPathFilter>(); 

	

	/**
	 * Gets the address of the neighbor
	 * 
	 * @return the address of the neighbor
	 */
	public InetAddress getAddress()
	{
		return address;
	}

	/**
	 * Sets the address of the neighbor
	 * 
	 * @param address the address of the neighbor
	 */
	public void setAddress(InetAddress address)
	{
		this.address = address;
	}

	/**
	 * Gets the ASN - autonomous system number of the neighbor
	 * 
	 * @return the ASN of the neighbor
	 */
	public long getAsn()
	{
		return asn;
	}

	/**
	 * Sets the ASN - autonomous system number of the neighbor
	 * 
	 * @param asn the ASN of the neighbor
	 */
	public void setAsn(long asn)
	{
		this.asn = asn;
	}

	/**
	 * Gets the neighbor description
	 * 
	 * @return description of a neighbor
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the neighbor description
	 * 
	 * @param description description of a neighbor
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the rs client feature state
	 * 
	 * @return true if rs client feature is enabled, false if not.
	 */
	public boolean isRsClient()
	{
		return rsClient;
	}

	/**
	 * Sets the rs client feature state
	 * 
	 * @param rsClient true if rs client feature is enabled, false if not
	 */
	public void setRsClient(boolean rsClient)
	{
		this.rsClient = rsClient;
	}

	/**
	 * Gets the configured weight of the neighbor for EBGP
	 * 
	 * @return the weight value
	 */
	public int getWeight()
	{
		return weight;
	}

	/**
	 * Sets the configured weight of the neighbor for EBGP
	 * 
	 * @param weight the weight value
	 */
	public void setWeight(int weight)
	{
		this.weight = weight;
	}

	/**
	 * Gets the state of the EBGP multihop feature
	 * 
	 * @return the state of the EBGP multihop feature
	 */
	public boolean isEbgpMultihop()
	{
		return ebgpMultihop;
	}

	/**
	 * Sets the state of the EBGP multihop feature
	 * 
	 * @param ebgpMultihop the state of the EBGP multihop feature
	 */
	public void setEbgpMultihop(boolean ebgpMultihop)
	{
		this.ebgpMultihop = ebgpMultihop;
	}

	/**
	 * Gets the state of the next-hop self feature
	 * 
	 * @return the state of the next-hop self feature
	 */
	public boolean isNextHopSelf()
	{
		return nextHopSelf;
	}

	/**
	 * Sets the state of the next-hop self feature
	 * 
	 * @param nextHopSelf the state of the next-hop self feature
	 */
	public void setNextHopSelf(boolean nextHopSelf)
	{
		this.nextHopSelf = nextHopSelf;
	}

	/**
	 * Gets the state of the send-community feature
	 * 
	 * @return the state of the send-community feature
	 */
	public boolean isSendCommunity()
	{
		return sendCommunity;
	}

	/**
	 * Sets the state of the send-community feature
	 * 
	 * @param sendCommunity the state of the send-community feature
	 */
	public void setSendCommunity(boolean sendCommunity)
	{
		this.sendCommunity = sendCommunity;
	}

	@Override
	public String toString()
	{
		return "Neighbor: " + asn + " Address: " + getAddress();
	}

	public void setPassive(boolean passive)
	{

	}

	/**
	 * @return the passive
	 */
	public boolean isPassive()
	{
		return passive;
	}

	/**
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	public void setLocalAddress(InetAddress localAddress)
	{
		this.localAddress = localAddress;
	}

	public InetAddress getLocalAddress()
	{
		return localAddress;
	}

	/**
	 * @return the filters
	 */
	public List<PrefixFilter> getOutFilters()
	{
		return prefixOutFilter;
	}

	public void addOutFilter(PrefixFilter filter)
	{
		this.prefixOutFilter.add(filter);
	}
	
	/**
	 * @return the filters
	 */
	public List<PrefixFilter> getInFilters()
	{
		return prefixInFilter;
	}

	public void addInFilter(PrefixFilter filter)
	{
		this.prefixInFilter.add(filter);
	}

	/**
	 * @return the asPathFilters
	 */
	public List<ASPathFilter> getAsPathFilters()
	{
		return asPathFilters;
	}
	
	public void addASPathFilter(ASPathFilter asPathFilter)
	{
		this.asPathFilters.add(asPathFilter);
	}

	/**
	 * @return the attributeUnchanged
	 */
	public boolean isAttributeUnchanged()
	{
		return attributeUnchanged;
	}

	/**
	 * @param attributeUnchanged the attributeUnchanged to set
	 */
	public void setAttributeUnchanged(boolean attributeUnchanged)
	{
		this.attributeUnchanged = attributeUnchanged;
	}
}
