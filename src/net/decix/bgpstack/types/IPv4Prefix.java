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

import net.decix.bgpstack.util.Utility;
import net.decix.bgpstack.util.UtilityException;

/*
 * TODO support v6.
 */
/**
 * Represents an IPv4 Prefix (see <a
 * href="http://tools.ietf.org/html/rfc1817">RFC1817</a>), which can be decoded
 * to a byte-array and added to an NLRI (network layer reachability information)
 */
public class IPv4Prefix extends NLRI
{

	public IPv4Prefix(InetAddress address, int prefixLength)
	{
		super(address, prefixLength);
	}

	public IPv4Prefix(byte[] prefix, int prefixLengthInBits)
	{
		super(prefix, prefixLengthInBits);
	}

	/**
	 * Constructs a new {@link IPv4Prefix} object from CIDR notation (e.g.,
	 * 10.0.0.0/8, 24.29.55.180/29) (see <a
	 * href="http://tools.ietf.org/html/rfc1817">RFC1817</a>)
	 * 
	 * @param prefix the prefix in CIDR-notation
	 * @return a new {@link IPv4Prefix} object
	 * @throws UtilityException if prefix is a well-formed CIDR notation
	 */
	public static IPv4Prefix fromString(String prefix) throws UtilityException
	{
		InetAddress resultAddress = null;
		int resultPrefixLength = 0;

		// check for classful prefix
		if (!prefix.contains("/"))
		{
			try
			{
				resultAddress = InetAddress.getByName(prefix);
			}
			catch (UnknownHostException e)
			{
				throw new UtilityException("prefix is not well-formatted: " + prefix);
			}
			resultPrefixLength = Utility.getRangeClassOfAddress(resultAddress).getPrefixLength();
		}
		else
		{
			String[] prefixSplit = prefix.split("/");

			if (prefixSplit.length != 2) throw new UtilityException("unknown host: " + prefix);

			try
			{
				resultAddress = InetAddress.getByName(prefixSplit[0]);
			}
			catch (UnknownHostException e)
			{
				throw new UtilityException("unknown host: " + prefixSplit[0]);
			}
			resultPrefixLength = Integer.parseInt(prefixSplit[1]);
		}

		return new IPv4Prefix(resultAddress, resultPrefixLength);
	}

	protected void constructInetAddressFromBytes(byte[] address)
	{
		if (address.length > 4) return;

		byte[] addrArray = { 0, 0, 0, 0 };
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

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof IPv4Prefix)
		{
			IPv4Prefix prefix = (IPv4Prefix) obj;
			return prefix.getPrefixLength() == getPrefixLength() && prefix.getAddress().equals(getAddress());
		}

		return false;
	}

	@Override
	public int getAfi()
	{
		return 1;
	}

	@Override
	public int getSafi()
	{
		return 1;
	}

}
