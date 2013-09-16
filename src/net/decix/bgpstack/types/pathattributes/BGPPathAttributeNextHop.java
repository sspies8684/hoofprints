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

package net.decix.bgpstack.types.pathattributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a BGP NEXT_HOP path attribute<br />
 * see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * @author sspies
 *
 */
@PathAttributeAnnotation(typeCode = 3, name = "NEXT_HOP")
public class BGPPathAttributeNextHop implements BGPConstants, BGPPathAttributeContent
{

	private InetAddress address;
	
	protected BGPPathAttributeNextHop()
	{
	}

	/**
	 * Instantiates a new BGPPathAttributeNextHop object
	 * @param address the NEXT_HOP address
	 */
	public BGPPathAttributeNextHop(InetAddress address)
	{
		this.address = address;
	}
	
	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	
	public void parse(byte[] data, Object param) throws UtilityException
	{
		try
		{
			address = InetAddress.getByAddress(data);
		}
		catch(UnknownHostException e)
		{
			throw new UtilityException(e.getMessage());
		}
	}

	/**
	 * Gets the address of the NEXT_HOP attribute 
	 * @return the address
	 */
	public InetAddress getAddress()
	{
		return address;
	}

	protected void setAddress(InetAddress address)
	{
		this.address = address;
	}

	public byte[] toBytes() throws UtilityException
	{
		return address.getAddress();
	}
	
	/**
	 * Gets the type code of the path attribute content
	 * 
	 * @return 4
	 */
	public int getByteLength()
	{
		return 4;
	}

	@Override
	public String toString()
	{
		return "NEXT_HOP: " + address.getHostAddress();
	}

	/**
	 * Gets the type code of the path attribute content
	 * 
	 * @return BGP_PATH_ATTRIBUTE_NEXT_HOP
	 */
	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_NEXT_HOP;
	}
}
