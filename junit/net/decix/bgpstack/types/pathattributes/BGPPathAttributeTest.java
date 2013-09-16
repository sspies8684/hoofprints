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

import static junit.framework.Assert.assertEquals;
import static net.decix.bgpstack.util.Utility.hexStringToByteArray;
import static org.junit.Assert.*;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeTest implements BGPConstants
{
	private byte[] wellFormed = hexStringToByteArray("40010100");
	private BGPPathAttribute origin;
	
	@Before
	public void setUp() throws Exception
	{
		origin = new BGPPathAttribute(new BGPPathAttributeOrigin(0));
	}

	@Test
	public void testBGPPathAttributeByteArray() throws UtilityException
	{
		byte[] originBytes = origin.toBytes();
		
		assertEquals(wellFormed.length, originBytes.length);
		
		for(int i = 0; i < wellFormed.length; i++)
			assertEquals(wellFormed[i], originBytes[i]);
		
		assertFalse(origin.isOptional());
		assertTrue(origin.isTransitive());
		assertFalse(origin.isExtendedLength());
		assertFalse(origin.isIncomplete());
	}

}
