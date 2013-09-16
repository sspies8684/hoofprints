package net.decix.bgpstack.types.capabilities;

import static net.decix.bgpstack.util.Utility.hexStringToByteArray;
import static org.junit.Assert.*;
import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.UtilityException;

import org.junit.Before;
import org.junit.Test;

public class BGPCapabilityTest implements BGPConstants
{

	byte[] multiprotocolCapWellformed = hexStringToByteArray("010400020001");
	byte[] routeRefreshCapWellformed = hexStringToByteArray("0200");
	byte[] gracefulRestartCapWellformed = hexStringToByteArray("4006005a00010100");
	byte[] fourByteAsnCapWellformed = hexStringToByteArray("4104121d0064");
	
	BGPCapability mpCap;
	BGPCapability rrCap;
	BGPCapability grCap;
	BGPCapability fbAsnCap;
	
	
	@Before
	public void setUp() throws Exception
	{
		mpCap = BGPCapability.parse(multiprotocolCapWellformed);
		rrCap = BGPCapability.parse(routeRefreshCapWellformed);
		grCap = BGPCapability.parse(gracefulRestartCapWellformed);
		fbAsnCap = BGPCapability.parse(fourByteAsnCapWellformed);
	}

	@Test
	public void testRuntimeClass()
	{
		assertEquals(BGPCapabilityMultiprotocol.class, mpCap.getClass());
		assertEquals(BGPCapabilityRouteRefresh.class, rrCap.getClass());
		assertEquals(BGPCapabilityGracefulRestart.class, grCap.getClass());
		assertEquals(BGPCapabilityFourByteASN.class, fbAsnCap.getClass());
	}

	@Test
	public void testAfiSafi()
	{
		assertEquals(AFI_INET6, ((BGPCapabilityMultiprotocol) mpCap).getAddressFamilyIdentifier());
		assertEquals(SAFI_UNICAST, ((BGPCapabilityMultiprotocol) mpCap).getSubsequentAddressFamilyIdentifier());
	}
	
	@Test
	public void testGr()
	{
		assertEquals(90, ((BGPCapabilityGracefulRestart) grCap).getRestartTime());
		assertFalse(((BGPCapabilityGracefulRestart) grCap).isRestartState());
		assertEquals(1, ((BGPCapabilityGracefulRestart) grCap).getStates().size());
		assertEquals(AFI_INET, ((BGPCapabilityGracefulRestart) grCap).getStates().get(0).getAfi());
		assertEquals(SAFI_UNICAST, ((BGPCapabilityGracefulRestart) grCap).getStates().get(0).getSafi());
		assertFalse(((BGPCapabilityGracefulRestart) grCap).getStates().get(0).isForwardingState());
	}

	@Test
	public void testCreateGr() throws UtilityException
	{
		BGPCapabilityGracefulRestart grCapGenerated = new BGPCapabilityGracefulRestart(false, 90);
		grCapGenerated.addState(new BGPCapabilityGracefulRestartForwardingState(AFI_INET, SAFI_UNICAST, false));
		assertEquals(gracefulRestartCapWellformed.length, grCapGenerated.toBytes().length);
		assertEquals(grCapGenerated.getByteLength(), gracefulRestartCapWellformed.length);
		for(int i = 0; i < gracefulRestartCapWellformed.length; i++)
			assertEquals(gracefulRestartCapWellformed[i], grCapGenerated.toBytes()[i]);
	}
	
	@Test
	public void test4BAsn() throws UtilityException
	{
		assertEquals(((BGPCapabilityFourByteASN) fbAsnCap).getAsn(), 303890532L);
	}
	
	@Test
	public void testCreate4BAsn() throws UtilityException
	{
		BGPCapabilityFourByteASN fbAsnGenerated = new BGPCapabilityFourByteASN(303890532L);
		for(int i = 0; i < fourByteAsnCapWellformed.length; i++)
			assertEquals(fourByteAsnCapWellformed[i], fbAsnGenerated.toBytes()[i]);
	}
}
