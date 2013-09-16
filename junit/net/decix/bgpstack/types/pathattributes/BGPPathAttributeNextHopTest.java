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

import java.net.InetAddress;

import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeNextHopTest
{

	private byte[] wellFormed = hexStringToByteArray("c0a89416");
	private BGPPathAttributeNextHop nextHopParsed;
	private BGPPathAttributeNextHop nextHopFixture;
	
	@Before
	public void setUp() throws Exception
	{
		nextHopParsed = new BGPPathAttributeNextHop();
		nextHopParsed.parse(wellFormed);
		nextHopFixture = new BGPPathAttributeNextHop(InetAddress.getByName("192.168.148.22"));
	}


	@Test
	public void testParse() throws UtilityException
	{
		nextHopParsed.parse(wellFormed);
	}

	@Test
	public void testGetAddress()
	{
		assertEquals(nextHopFixture.getAddress(), nextHopParsed.getAddress());
	}

	@Test
	public void testToBytes() throws UtilityException
	{
		assertEquals(nextHopFixture.toBytes().length, nextHopParsed.toBytes().length);
		
		for(int i = 0; i < nextHopFixture.toBytes().length; i++)
		{
			assertEquals(nextHopFixture.toBytes()[i], nextHopParsed.toBytes()[i]);
			assertEquals(nextHopParsed.toBytes()[i], wellFormed[i]);
		}
	}

	@Test
	public void testGetByteLength()
	{
		assertEquals(nextHopFixture.getByteLength(), nextHopParsed.getByteLength());
		assertEquals(nextHopFixture.getByteLength(), wellFormed.length);
	}

	@Test
	public void testToString()
	{
		assertEquals(nextHopFixture.toString(), nextHopParsed.toString());
	}

}
