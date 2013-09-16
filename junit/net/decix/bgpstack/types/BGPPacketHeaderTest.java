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

import static net.decix.bgpstack.util.Utility.*;

import static org.junit.Assert.*;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPacketHeaderTest
{
	
	private byte[] wellFormed = hexStringToByteArray("ffffffffffffffffffffffffffffffff001d0104fe0900b4c0a8000f00");
	BGPMessageHeader phFromWellFormed;

	@Before
	public void setUp() throws UtilityException
	{
		phFromWellFormed = BGPMessageHeader.parse(wellFormed);
	}
	
	@Test
	public void testParse() throws UtilityException
	{
		BGPMessageHeader.parse(wellFormed);
	}
	
	

	@Test
	public void testGetMarker() throws UtilityException
	{
		
		assertEquals(phFromWellFormed.getMarker().length, 16);
		for(byte b : phFromWellFormed.getMarker())
			assertEquals(-1, b);
		
	}

	@Test
	public void testGetLength() throws UtilityException
	{
		assertEquals(phFromWellFormed.getLength(), 29);

	}

	@Test
	public void testGetType()
	{
		assertEquals(phFromWellFormed.getType(), BGPConstants.BGP_MESSAGE_TYPE_OPEN);
	}

}
