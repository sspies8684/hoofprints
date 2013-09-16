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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.decix.bgpstack.types.IPv6Prefix;
import net.decix.bgpstack.util.Utility;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPPathAttributeMPRreachNLRITest
{
	byte[] wellFormed1 = Utility.hexStringToByteArray("00020120200107f80000000000003c5400000001fe800000000000000218b9fffe340401002020011a90");

	BGPPathAttributeMPReachNLRI mpReachNlri1;
	BGPPathAttributeMPReachNLRI mpReachNlriFixture;

	@Before
	public void setUp() throws Exception
	{
		mpReachNlri1 = new BGPPathAttributeMPReachNLRI();
		mpReachNlri1.parse(wellFormed1);

		mpReachNlriFixture = new BGPPathAttributeMPReachNLRI();
		mpReachNlriFixture.addReachableNLRI(new IPv6Prefix(InetAddress.getByName("2001:1a90::"), 32));
		mpReachNlriFixture.addNextHop(InetAddress.getByName("2001:7f8::3c54:0:1"));
		mpReachNlriFixture.addNextHop(InetAddress.getByName("fe80::218:b9ff:fe34:401"));
	}

	@Test
	public void testParse() throws UtilityException
	{
		mpReachNlri1.parse(wellFormed1);
	}

	@Test
	public void testToBytes() throws UtilityException
	{
		assertEquals(mpReachNlri1.toBytes().length, mpReachNlriFixture.toBytes().length);

		for (int i = 0; i < mpReachNlriFixture.toBytes().length; i++)
		{
			assertEquals(mpReachNlri1.toBytes()[i], mpReachNlriFixture.toBytes()[i]);
			assertEquals(mpReachNlri1.toBytes()[i], wellFormed1[i]);
		}

	}

	@Test
	public void testGetReachableNLRI() throws UnknownHostException
	{
		assertEquals(mpReachNlri1.getReachableNLRI().size(), mpReachNlriFixture.getReachableNLRI().size());

		assertEquals(mpReachNlri1.getReachableNLRI().get(0).getAddress(), InetAddress.getByName("2001:1a90::"));
		assertEquals(mpReachNlri1.getReachableNLRI().get(0).getPrefixLength(), 32);

		assertEquals(2, mpReachNlri1.getNextHops().size());
		assertEquals(InetAddress.getByName("2001:7f8::3c54:0:1"), mpReachNlri1.getNextHops().get(0));
		assertEquals(InetAddress.getByName("fe80::218:b9ff:fe34:401"), mpReachNlri1.getNextHops().get(1));

	}

}
