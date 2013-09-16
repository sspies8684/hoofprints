package net.decix.bgpstack.util;

import static org.junit.Assert.*;
import static net.decix.bgpstack.util.Utility.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

public class UtilityTest
{

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void testInetAddressStringToLong() throws UtilityException
	{
		assertEquals(3232246878L, inetAddressStringToLong("192.168.44.94"));
		
	}

	@Test
	public void testInetAddressLongToString() throws UtilityException, UnknownHostException
	{
		assertEquals(InetAddress.getByName("192.168.44.94"), inetAddressLongToString(3232246878L));
	}

}
