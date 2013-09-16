package net.decix.bgpstack.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class AntPropertiesTest
{

	AntProperties networkProperties = new AntProperties();
	@Before
	public void setUp() throws Exception
	{
		networkProperties.load(new File("conf/network.properties"));
	}

	@Test
	public void testGetGenericProperty()
	{
		int peers = networkProperties.getProperty(Integer.class, "parseInt", "cross.id");
		assertEquals(1, peers);
		assertEquals(1, networkProperties.getPropertyAsInt("cross.id"));
	}

}
