package net.decix.bgpstack.types.capabilities;

import static net.decix.bgpstack.util.Utility.fourBytesToLong;
import static net.decix.bgpstack.util.Utility.longToFourBytes;
import net.decix.bgpstack.util.UtilityException;

@CapabilityAnnotation(name="4 bytes ASN", typeCode = 65)
public class BGPCapabilityFourByteASN extends BGPCapability
{
	private long asn;
	
	public BGPCapabilityFourByteASN(long asn)
	{
		this();
		this.asn = asn;
	}

	public BGPCapabilityFourByteASN()
	{
		this(BGP_CAPABILITY_4_BYTE_ASN);
	}

	protected BGPCapabilityFourByteASN(int typeCode)
	{
		super(typeCode);
	}

	@Override
	protected int getByteLengthTemplate()
	{
		return 4;
	}

	@Override
	protected void parseTemplate(byte[] data) throws UtilityException
	{
		if(data.length != 4)
			throw new UtilityException("4 bytes ASNs should be 4 byte long!");
		
		this.asn = fourBytesToLong(data);
	}

	@Override
	protected byte[] toBytesTemplate() throws UtilityException
	{	
		return longToFourBytes(asn);
	}

	/**
	 * @return the asn
	 */
	public long getAsn()
	{
		return asn;
	}

}
