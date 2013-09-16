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

import static net.decix.bgpstack.util.Utility.*;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a BGP ORIGIN path attribute<br />
 * see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * 
 * @author sspies
 * 
 */
@PathAttributeAnnotation(typeCode = 1, name = "ORIGIN")
public class BGPPathAttributeOrigin implements BGPConstants, BGPPathAttributeContent
{
	private int origin;

	/**
	 * Instantiates a new BGPPathAttributeOrigin object
	 * 
	 * @param origin the type value
	 * @see BGPConstants#BGP_PATH_ORIGIN_EGP
	 * @see BGPConstants#BGP_PATH_ORIGIN_IGP
	 * @see BGPConstants#BGP_PATH_ORIGIN_INCOMPLETE
	 */
	public BGPPathAttributeOrigin(int origin)
	{
		this.origin = origin;
	}

	protected BGPPathAttributeOrigin()
	{
		super();
	}

	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	
	
	public void parse(byte[] data, Object param) throws UtilityException
	{
		origin = oneByteToInteger(data[0]);
	}

	/**
	 * Gets the origin type value
	 * 
	 * @return the origin type value
	 * @see BGPConstants#BGP_PATH_ORIGIN_EGP
	 * @see BGPConstants#BGP_PATH_ORIGIN_IGP
	 * @see BGPConstants#BGP_PATH_ORIGIN_INCOMPLETE
	 */
	public int getOrigin()
	{
		return origin;
	}

	protected void setOrigin(int origin)
	{
		this.origin = origin;
	}

	public byte[] toBytes() throws UtilityException
	{
		return new byte[] { integerToOneByte(origin) };
	}

	/**
	 * Gets the type code of the path attribute content
	 * 
	 * @return 1
	 */
	public int getByteLength()
	{
		return 1;
	}

	@Override
	public String toString()
	{
		return "ORIGIN: " + origin;
	}

	/**
	 * Gets the type code of the path attribute content
	 * 
	 * @return BGP_PATH_ATTRIBUTE_ORIGIN
	 */
	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_ORIGIN;
	}

}
