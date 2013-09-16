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

import java.util.LinkedList;
import java.util.List;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.types.pathattributes.aspath.ASPathSegment;
import net.decix.bgpstack.types.pathattributes.aspath.ASPathSequence;
import net.decix.bgpstack.types.pathattributes.aspath.ASPathSet;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a BGP AS_PATH path attribute<br />
 * see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * @author sspies
 *
 */
//@PathAttributeAnnotation(name = "AS4_PATH", typeCode = 17, optional = true, transitive = true)
public class BGPPathAttributeAs4Path implements BGPConstants, BGPPathAttributeContent
{

	List<ASPathSegment> pathSegments = new LinkedList<ASPathSegment>();
	
	/**
	 * Instantiates a new BGPPathAttributeAsPath object
	 * @param elements the ASPathSegments, that you want to add to this AS_PATH attribute
	 */
	public BGPPathAttributeAs4Path(List<ASPathSegment> elements)
	{
		this.pathSegments = elements;
	}
	
	public BGPPathAttributeAs4Path()
	{
		
	}
	
	
	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	public void parse(byte[] data, Object param) throws UtilityException
	{
		
		int offset = 0;
//		while (offset < data.length)
//		{
//			int numOfAsEntries = oneByteToInteger(data[offset + 1]);
//			byte[] asEntries = new byte[numOfAsEntries * 2];
//			System.arraycopy(data, offset + 2, asEntries, 0, asEntries.length);
//			ASPathSegment element = null;
//			
//			switch (oneByteToInteger(data[offset]))
//			{
//				case BGP_PATH_ATTRIBUTE_AS_PATH_SEQUENCE:
//					element = ASPathSequence.parse(asEntries, true);
//				break;
//				case BGP_PATH_ATTRIBUTE_AS_PATH_SET:
//					element = ASPathSet.parse(asEntries, true);
//				break;
//			}
//			pathSegments.add(element);
//			
//			offset += asEntries.length + 2;
//		}
	}

	public byte[] toBytes() throws UtilityException
	{
		byte[] result = {};
		for(ASPathSegment element : pathSegments)
			result = concatenateTwoByteArrays(result, element.toBytes());
		return result;
	}

	public int getByteLength()
	{
		int sum = 0;
		for(ASPathSegment element : pathSegments)
			sum += element.getByteLength();
		return sum;
	}
	
	@Override
	public String toString()
	{
		String result = "AS_PATH: ";
		for(int i = 0; i < pathSegments.size(); i++)
		{
			result += pathSegments.get(i);
			if(i < pathSegments.size() - 1)
				result += ",";
		}
			
		return result;
	}
	
	/**
	 * Adds a number of ASNs to the AS_PATH as a sequence
	 * @param sequence the ASNs to add to the AS_PATH as a sequence
	 */
	public void addSequence(long...sequence)
	{
		pathSegments.add(new ASPathSequence(sequence, true));
	}
	
	
	/**
	 * Adds a number of ASNs to the AS_PATH as an aggregation-set
	 * @param set the ASNs to add to the AS_PATH as aggregation-set
	 */
	public void addSet(long...set)
	{
		pathSegments.add(new ASPathSet(set, true));
	}

	
	/**
	 * @return BGP_PATH_ATTRIBUTE_AS_PATH
	 */
	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_AS_PATH;
	}
}
