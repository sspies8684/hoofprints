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

import static net.decix.bgpstack.util.Utility.concatenateTwoByteArrays;
import static net.decix.bgpstack.util.Utility.integerToOneByte;
import static net.decix.bgpstack.util.Utility.integerToTwoBytes;
import static net.decix.bgpstack.util.Utility.oneByteToInteger;
import static net.decix.bgpstack.util.Utility.twoBytesToInteger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.types.NLRI;
import net.decix.bgpstack.util.Utility;
import net.decix.bgpstack.util.UtilityException;

/**
 * @author sspies
 * 
 */
@PathAttributeAnnotation(typeCode = 14, name = "MP_REACH_NLRI", optional = true, transitive = false, extendedLength = true)
public class BGPPathAttributeMPReachNLRI implements BGPConstants, BGPPathAttributeContent
{
	
	private List<NLRI> nlri = new LinkedList<NLRI>();
	private List<InetAddress> nextHops = new LinkedList<InetAddress>();
	private byte reserved;

	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_MP_REACH_NLRI;
	}

	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	/**
	 * Supported: AFI 1 (IPv4), AFI 2 (IPv6)
	 */
	public void parse(byte[] data, Object param) throws UtilityException
	{ 
		
		byte[] afi = new byte[2];
		System.arraycopy(data, 0, afi, 0, 2);
		int afiValue = twoBytesToInteger(afi);
		
		int nextHopSize = Utility.getSizeOfNextHopByAfi(afiValue);
			
		int safiValue = oneByteToInteger(data[2]);
		
		if(safiValue != 1)
			throw new UtilityException("unsupported subsequent address family identifier " + safiValue);
		
		int nextHopLengthValue = oneByteToInteger(data[3]);
		int offset = 4;
		while(offset < (4 + nextHopLengthValue))
		{
			byte[] nextHop = new byte[nextHopSize];
			System.arraycopy(data, offset, nextHop, 0, nextHopSize);
			try
			{
				nextHops.add(InetAddress.getByAddress(nextHop));
			}
			catch (UnknownHostException e)
			{
				throw new UtilityException("address is not well-formed");
			}
			offset += nextHopSize;
		}
		
		reserved = data[offset++];
		
		if(reserved != 0x00)
			throw new UtilityException("reserved field is not set to 0");
		
		while (offset < data.length)
		{
			int prefixLengthInBits = oneByteToInteger(data[offset]);
			int prefixLength = (int) Math.ceil(((double) prefixLengthInBits) / 8);
			byte[] prefix = new byte[prefixLength];
			System.arraycopy(data, offset + 1, prefix, 0, prefixLength);
			
			NLRI parsedNlri = null;
			switch(afiValue)
			{
				case 1:
					parsedNlri = new IPv4Prefix(prefix, prefixLengthInBits);
				break;
				case 2:
					parsedNlri = new IPv6Prefix(prefix, prefixLengthInBits);
				break;
			}
			nlri.add(parsedNlri);
			offset += 1 + prefixLength;
		}

	}

	public int getByteLength()
	{
		int byteLength = 5;
		
		for(InetAddress a : nextHops)
			byteLength += a.getAddress().length;
		
		for(NLRI n : nlri)
			byteLength += n.getByteLength();
			
		return byteLength;
	}
	
	public byte[] toBytes() throws UtilityException
	{	
		byte[] result = integerToTwoBytes(nlri.get(0).getAfi());
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(nlri.get(0).getSafi()) });
		
		int nextHopLen = 0;
		for(InetAddress a : nextHops)
			nextHopLen += a.getAddress().length;
		
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(nextHopLen) });
		
		for(InetAddress a : nextHops)
			result = concatenateTwoByteArrays(result, a.getAddress());
		
		result = concatenateTwoByteArrays(result, new byte[] { 0x00 });
		
		for(NLRI n : nlri)
			result = concatenateTwoByteArrays(result, n.toBytes());
		
		return result;
	}
	
	public void addReachableNLRI(NLRI n)
	{
		nlri.add(n);
	}
	
	public void addNextHop(InetAddress nextHop)
	{
		nextHops.add(nextHop);
	}

	public List<NLRI> getReachableNLRI()
	{
		return nlri;
	}
	
	public List<InetAddress> getNextHops()
	{
		return nextHops;
	}

}
