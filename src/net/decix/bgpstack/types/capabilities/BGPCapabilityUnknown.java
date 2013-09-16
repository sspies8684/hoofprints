package net.decix.bgpstack.types.capabilities;

import net.decix.bgpstack.util.UtilityException;

public class BGPCapabilityUnknown extends BGPCapability
{
	protected BGPCapabilityUnknown(int typeCode)
	{
		super(typeCode);
	}

	private byte[] data;

	@Override
	protected void parseTemplate(byte[] data)
	{
		this.data = data;
	}

	public int getByteLengthTemplate()
	{
		return data.length;
	}

	public byte[] toBytesTemplate() throws UtilityException
	{
		return data;
	}

}
