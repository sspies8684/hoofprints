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

package net.decix.bgpstack.types.pathattributes.aspath;

import java.util.LinkedList;
import java.util.List;
import static net.decix.bgpstack.util.Utility.*;

import net.decix.bgpstack.util.UtilityException;

/**
 * Represents an AS_PATH sequence
 * 
 * @author sspies
 * 
 */
public class ASPathSequence extends ASPathSegment
{
	private List<Long> asEntries = new LinkedList<Long>();
	private boolean fourByteEncoding = false;

	/**
	 * Instantiates a new AS_PATH sequence
	 * 
	 * @param sequence
	 */
	public ASPathSequence(long[] sequence, boolean fourByteEncoding)
	{
		for (long s : sequence)
			asEntries.add(s);
		this.fourByteEncoding = fourByteEncoding;
	}

	protected ASPathSequence()
	{

	}

	/**
	 * Parses AS_PATH sequence from raw byte data as sent by BGP messages
	 * 
	 * @param data the data
	 * @return a new ASPathSequence object
	 * @throws UtilityException if something goes wrong with parsing (e.g.,
	 *             malformed message)
	 */
	public static ASPathSequence parse(byte[] data, boolean fourByteEncoding) throws UtilityException
	{
		ASPathSequence s = new ASPathSequence();

		s.fourByteEncoding = fourByteEncoding;

		for (int i = 0; i < data.length; i += fourByteEncoding ? 4 : 2)
			if (fourByteEncoding)
				s.getAsEntries().add(fourBytesToLong(new byte[] { data[i], data[i + 1], data[i + 2], data[i + 3] }));
			else
				s.getAsEntries().add((long) twoBytesToInteger(new byte[] { data[i], data[i + 1] }));
		return s;

	}

	public int getByteLength()
	{
		return 2 + asEntries.size() * (fourByteEncoding ? 4 : 2);
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = { integerToOneByte(BGP_PATH_ATTRIBUTE_AS_PATH_SEQUENCE), integerToOneByte(asEntries.size()) };

		for (long as : asEntries)
		{
			if (!fourByteEncoding)
				if (as > BGP_AS2_MAX)
					as = BGP_AS_TRANS;

			result = concatenateTwoByteArrays(result, fourByteEncoding ? longToFourBytes(as) : integerToTwoBytes((int) as));
		}

		return result;
	}

	protected List<Long> getAsEntries()
	{
		return asEntries;
	}

	protected void setAsEntries(List<Long> asEntries)
	{
		this.asEntries = asEntries;
	}

	/**
	 * Adds an entry to the AS_PATH
	 * 
	 * @param asNum the ASN to add
	 */
	public void addAsEntry(long asNum)
	{
		asEntries.add(asNum);
	}

	@Override
	public String toString()
	{
		String result = "";
		for (int i = 0; i < asEntries.size(); i++)
		{
			result += asEntries.get(i);
			if (i < asEntries.size() - 1)
				result += ",";
		}
		return result;
	}
}
