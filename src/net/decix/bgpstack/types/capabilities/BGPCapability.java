package net.decix.bgpstack.types.capabilities;

import static net.decix.bgpstack.util.Utility.*;

import java.util.HashMap;
import java.util.Map;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

public abstract class BGPCapability implements PacketSerializable, BGPConstants
{
	private int typeCode;
	private int length;
	protected static Map<Integer, Class<? extends BGPCapability>> registry = new HashMap<Integer, Class<? extends BGPCapability>>();

	public static void registerStandardCapabilities()
	{
		registerCapabilitiesFromPackage("net.decix.bgpstack.types.capabilities");
	}

	public static void registerCapabilitiesFromPackage(String packageName)
	{
		try
		{
			for (Class<? extends BGPCapability> c : getClassesForPackage(packageName))
			{
				CapabilityAnnotation cap;
				if ((cap = (CapabilityAnnotation) c.getAnnotation(CapabilityAnnotation.class)) != null)
					registry.put(cap.typeCode(), c);
			}
		}
		catch (ClassNotFoundException e)
		{
		}
	}

	protected BGPCapability(int typeCode)
	{
		this.typeCode = typeCode;
	}

	protected abstract void parseTemplate(byte[] data) throws UtilityException;

	protected abstract int getByteLengthTemplate();

	protected abstract byte[] toBytesTemplate() throws UtilityException;

	protected static void loadCapabilities()
	{
		synchronized (BGPCapability.class)
		{
			if (registry.size() == 0)
				registerStandardCapabilities();
		}
	}
	
	public static BGPCapability parse(byte[] data) throws UtilityException
	{
		loadCapabilities();

		int typeCode = oneByteToInteger(data[0]);
		int length = oneByteToInteger(data[1]);
		byte[] value = new byte[length];
		System.arraycopy(data, 2, value, 0, length);

		BGPCapability capability = null;

		try
		{
			// fetch capability class
			capability = registry.get(typeCode).getConstructor().newInstance();
		}
		catch (Exception e)
		{
			capability = new BGPCapabilityUnknown(typeCode);
		}

		capability.parseTemplate(value);

		return capability;

	}

	/**
	 * @return the typeCode
	 */
	public int getTypeCode()
	{
		return typeCode;
	}

	/**
	 * @return the length
	 */
	public int getLength()
	{
		return length;
	}

	public final byte[] toBytes() throws UtilityException
	{
		return concatenateTwoByteArrays(new byte[] { integerToOneByte(typeCode), integerToOneByte(getByteLengthTemplate()) }, toBytesTemplate());
	}

	public final int getByteLength()
	{
		return 2 + getByteLengthTemplate();
	}

}
