package net.decix.bgpstack.types.capabilities;

import net.decix.bgpstack.util.UtilityException;

@CapabilityAnnotation(name = "Route Refresh RFC2918", typeCode = 2)
public class BGPCapabilityRouteRefresh extends BGPCapability
{
	public BGPCapabilityRouteRefresh()
	{
		super(BGP_CAPABILITY_ROUTE_REFRESH);
	}
	
	
	protected BGPCapabilityRouteRefresh(int typeCode)
	{
		super(typeCode);
	}

	@Override
	protected void parseTemplate(byte[] data) throws UtilityException
	{
		// should be zero ;-)
		if(data.length != 0)
			throw new UtilityException("Route Refresh Capability is not well-formed");

	}

	public int getByteLengthTemplate()
	{
		return 0;
	}

	public byte[] toBytesTemplate() throws UtilityException
	{
		return new byte[0];
	}

}
