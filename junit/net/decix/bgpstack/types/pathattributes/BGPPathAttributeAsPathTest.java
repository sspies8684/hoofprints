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

import static org.junit.Assert.*;
import static net.decix.bgpstack.util.Utility.*;

import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeAsPathTest
{

	BGPPathAttributeAsPath asPathParsed;
	BGPPathAttributeAsPath asPathFixture;
	
	byte[] wellFormed = hexStringToByteArray("010201f401f40201febb");
	
	@Before
	public void setUp() throws Exception
	{
		asPathParsed = new BGPPathAttributeAsPath();
		asPathParsed.parse(wellFormed);
		asPathFixture = new BGPPathAttributeAsPath();
		asPathFixture.addSet(500,500);
		asPathFixture.addSequence(65211);
	}

	@Test
	public void testParse() throws UtilityException
	{
		asPathParsed.parse(wellFormed);
	}

	@Test
	public void testToBytes() throws UtilityException
	{
		assertEquals(asPathParsed.toBytes().length, asPathFixture.toBytes().length);
		
		for(int i = 0; i < asPathFixture.toBytes().length; i++)
		{
			assertEquals(asPathParsed.toBytes()[i], asPathFixture.toBytes()[i]);
			assertEquals(asPathParsed.toBytes()[i], wellFormed[i]);
		}
	}
	
	@Test
	public void testGetByteLength()
	{
		assertEquals(asPathParsed.getByteLength(), wellFormed.length);
		assertEquals(asPathParsed.getByteLength(), asPathFixture.getByteLength());
	}
	
	@Test
	public void testToString()
	{
		assertEquals(asPathFixture.toString(), asPathParsed.toString());
	}

}
