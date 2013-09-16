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

package net.decix.bgpstack.types;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents an IPv6 Unicast prefix
 * 
 * @author sspies
 * 
 */
public class IPv6Prefix extends NLRI implements PacketSerializable
{

	public IPv6Prefix(InetAddress address, int prefixLength)
	{
		super(address, prefixLength);
	}

	public IPv6Prefix(byte[] address, int prefixLength)
	{
		super(address, prefixLength);
	}

	protected void constructInetAddressFromBytes(byte[] address)
	{
		if (address.length > 16)
			return;

		byte[] addrArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		System.arraycopy(address, 0, addrArray, 0, address.length);

		try
		{
			this.address = InetAddress.getByAddress(addrArray);
		}
		catch (UnknownHostException e)
		{ /*
		 * intentionally nothing API: UnknownHostException - if IP address is of
		 * illegal length
		 */
		}

	}

	/**
	 * Constructs a new {@link IPv6Prefix} object from CIDR notation (e.g.,
	 * 3ffe:c00::/48) (see <a
	 * href="http://tools.ietf.org/html/rfc1817">RFC1817</a>)
	 * 
	 * @param prefix the prefix in CIDR-notation
	 * @return a new {@link IPv6Prefix} object
	 * @throws UtilityException if prefix is a well-formed CIDR notation
	 */
	public static IPv6Prefix fromString(String prefix) throws UtilityException
	{
		InetAddress address = null;
		int prefixLength = 0;

		// check for classful prefix
		if (prefix.contains("/"))
		{
			String[] prefixSplit = prefix.split("/");

			if (prefixSplit.length != 2)
				throw new UtilityException("unknown host: " + prefix);

			try
			{
				address = InetAddress.getByName(prefixSplit[0]);
			}
			catch (UnknownHostException e)
			{
				throw new UtilityException("unknown host: " + prefixSplit[0]);
			}
			prefixLength = Integer.parseInt(prefixSplit[1]);
		}
		else
			throw new UtilityException("prefix is not well-formed: " + prefix);

		return new IPv6Prefix(address, prefixLength);
	}

	@Override
	public int getAfi()
	{
		return 2;
	}

	@Override
	public int getSafi()
	{
		return 1;
	}

}
