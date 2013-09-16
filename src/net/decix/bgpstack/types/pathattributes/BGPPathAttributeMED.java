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

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

/*
 * TODO implement
 */

/**
 * Represents a BGP MULTI_EXIT_DISC path attribute<br />
 * see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 */
// @PathAttributeAnnotation(name = "MULTI_EXIT_DISC", typeCode = 4)
public class BGPPathAttributeMED implements BGPConstants, BGPPathAttributeContent
{
	public void parse(byte[] data) throws UtilityException
	{
		parse(data, null);
	}
	public void parse(byte[] data, Object param) throws UtilityException
	{

	}

	public int getByteLength()
	{
		return 0;
	}

	public byte[] toBytes() throws UtilityException
	{
		return new byte[] {};
	}

	public int getTypeCode()
	{
		return BGP_PATH_ATTRIBUTE_MED;
	}

}
