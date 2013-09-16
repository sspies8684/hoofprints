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
@PathAttributeAnnotation(typeCode = 15, name = "MP_UNREACH_NLRI", optional = true, transitive = false, extendedLength = true)
public class BGPPathAttributeMPUnreachNLRI implements BGPConstants, BGPPathAttributeContent
{
	private List<NLRI> unreachableNlri = new LinkedList<NLRI>();
	
	protected BGPPathAttributeMPUnreachNLRI()
	{
		
	}

	public BGPPathAttributeMPUnreachNLRI(List<NLRI> unreachableNlri)
	{
		this.unreachableNlri = unreachableNlri;
	}
	
	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_MP_UNREACH_NLRI;
	}

	
	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	
	public void parse(byte[] data, Object param) throws UtilityException
	{

		byte[] afi = new byte[2];
		System.arraycopy(data, 0, afi, 0, 2);
		int afiValue = twoBytesToInteger(afi);
		int safiValue = oneByteToInteger(data[2]);

		if (safiValue != 1)
			throw new UtilityException("subsequent address family identifier " + safiValue + " != Unicast");

		int offset = 3;

		while (offset < data.length)
		{
			int prefixLengthInBits = oneByteToInteger(data[offset]);
			int prefixLength = (int) Math.ceil(((double) prefixLengthInBits) / 8);
			byte[] prefix = new byte[prefixLength];
			System.arraycopy(data, offset + 1, prefix, 0, prefixLength);
			NLRI parsedNlri = null;
			switch (afiValue)
			{
				case 1:
					parsedNlri = new IPv4Prefix(prefix, prefixLengthInBits);
				break;
				case 2:
					parsedNlri = new IPv6Prefix(prefix, prefixLengthInBits);
				break;

			}
			unreachableNlri.add(parsedNlri);
			offset += 1 + prefixLength;
		}

	}

	public int getByteLength()
	{
		int byteLength = 3;

		for (NLRI n : unreachableNlri)
			byteLength += n.getByteLength();

		return byteLength;
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = integerToTwoBytes(2);
		result = concatenateTwoByteArrays(result, new byte[] { integerToOneByte(1) });

		for (NLRI n : unreachableNlri)
			result = concatenateTwoByteArrays(result, n.toBytes());

		return result;
	}

	public void addUnreachableNLRI(NLRI n)
	{
		unreachableNlri.add(n);
	}

	/**
	 * @return the unreachableNlriß
	 */
	public List<NLRI> getUnreachableNLRI()
	{
		return unreachableNlri;
	}

}
