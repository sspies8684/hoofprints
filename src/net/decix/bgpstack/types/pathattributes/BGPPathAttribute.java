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

import static net.decix.bgpstack.util.Utility.*;

import java.util.HashMap;
import java.util.Map;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.util.PacketSerializable;
import net.decix.bgpstack.util.UtilityException;

/**
 * Represents a BGP path attribute.
 * 
 * see <a href="http://tools.ietf.org/html/rfc4271">RFC4271</a>
 * 
 * @author sspies
 * 
 */
public class BGPPathAttribute implements BGPConstants, PacketSerializable
{
	private boolean optional = false;
	private boolean transitive = true;
	private boolean incomplete = false;
	private boolean extendedLength = false;

	private int length;

	private int typeCode;

	private BGPPathAttributeContent content;

	protected static Map<Integer, Class<? extends BGPPathAttributeContent>> registry = new HashMap<Integer, Class<? extends BGPPathAttributeContent>>();

	// search for BGP capability plugins at DE-CIX locations
	static
	{
		registerPathAttributesFromPackage("net.decix.bgpstack.types.pathattributes");
	}

	public static void registerPathAttributesFromPackage(String packageName)
	{
		try
		{
			for (Class<? extends BGPPathAttributeContent> c : getClassesForPackage(packageName))
			{
				PathAttributeAnnotation pa;
				if ((pa = (PathAttributeAnnotation) c.getAnnotation(PathAttributeAnnotation.class)) != null)
					registry.put(pa.typeCode(), c);

			}
		}
		catch (ClassNotFoundException e)
		{
		}
	}

	/**
	 * Instantiates a new BGPPathAttribute object
	 * 
	 * @param content the content part of this path attribute
	 */
	public BGPPathAttribute(BGPPathAttributeContent content)
	{
		super();
		this.content = content;
		this.typeCode = content.getTypeCode();
		this.length = content.getByteLength();
		PathAttributeAnnotation annotation = content.getClass().getAnnotation(PathAttributeAnnotation.class);
		this.incomplete = annotation.incomplete();
		this.extendedLength = annotation.extendedLength();
		this.optional = annotation.optional();
		this.transitive = annotation.transitive();
	}

	/**
	 * <p>
	 * Instantiates a new BGPPathAttribute from raw byte data as defined in
	 * RFC4271<br />
	 * <br />
	 * 
	 * If type is unknown, the data will be saved to BGPPathAttributeUnknown
	 * 
	 * </p>
	 * 
	 * @param data the data
	 * @throws UtilityException if byte data is malformed
	 */
	public BGPPathAttribute(byte[] data, Object param) throws UtilityException
	{
		super();
		parseHeader(data);

		byte[] value = new byte[length];

		System.arraycopy(data, (extendedLength ? 4 : 3), value, 0, length);

		try
		{
			// fetch path attribute class
			content = registry.get(typeCode).getConstructor().newInstance();
		}
		catch (Exception e)
		{
			BGPPathAttributeUnknown au = new BGPPathAttributeUnknown();
			au.setTypeCode(typeCode);
			content = au;
		}

		content.parse(value, param);
	}

	public BGPPathAttribute(byte[] data) throws UtilityException
	{
		this(data, null);
	}

	private void parseHeader(byte[] data) throws UtilityException
	{
		optional = (data[0] & 0x80) == 0x80;
		transitive = (data[0] & 0x40) == 0x40;
		incomplete = (data[0] & 0x20) == 0x20;
		extendedLength = (data[0] & 0x10) == 0x10;

		typeCode = oneByteToInteger(data[1]);

		if (extendedLength)
			length = twoBytesToInteger(new byte[] { data[2], data[3] });
		else
			length = oneByteToInteger(data[2]);

	}

	/**
	 * Gets the transitive flag
	 * 
	 * @return true if the well-known bit is set
	 */
	public boolean isTransitive()
	{
		return transitive;
	}

	/**
	 * Gets the length of the path attribute
	 * 
	 * @return the length of the path attribute as the length field says
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Gets the type code of the content part of the attribute
	 * 
	 * @return the type code as as the type code field says
	 */
	public int getTypeCode()
	{
		return typeCode;
	}

	protected void setLength(int length)
	{
		this.length = length;
	}

	protected void setTypeCode(int typeCode)
	{
		this.typeCode = typeCode;
	}

	@Override
	public String toString()
	{
		return "** optional:" + optional + " transitive:" + transitive + " incomplete:" + incomplete + " extendedLength:" + extendedLength + " length:" + length + " type-code:" + typeCode + "\n** content: " + content;

	}

	/**
	 * Decodes the path attribute to bytes
	 * 
	 * @return the path attribute decoded to an byte-array
	 * @throws UtilityException if the path attribute one of its subclasses is
	 *             semantically incorrect (e.g., incomplete)
	 */
	public byte[] toBytes() throws UtilityException
	{
		int flags = 0;
		flags |= (optional ? 1 : 0) << 7;
		flags |= (transitive ? 1 : 0) << 6;
		flags |= (incomplete ? 1 : 0) << 5;
		flags |= (extendedLength ? 1 : 0) << 4;

		byte[] result = new byte[] { integerToOneByte(flags), integerToOneByte(typeCode) };
		result = concatenateTwoByteArrays(result, extendedLength ? integerToTwoBytes(length) : new byte[] { integerToOneByte(length) });
		result = concatenateTwoByteArrays(result, content.toBytes());
		return result;

	}

	public int getByteLength()
	{
		return (extendedLength ? 4 : 3) + content.getByteLength();
	}

	/**
	 * Gets the content part of this path attribute
	 * 
	 * @return the content object
	 */
	public BGPPathAttributeContent getContent()
	{
		return content;
	}

	protected void setContent(BGPPathAttributeContent content)
	{
		this.content = content;
	}

	/**
	 * Sets the option flag of this path attribute
	 * 
	 * @param optional the value of the optional bit
	 */
	public void setOptional(boolean optional)
	{
		this.optional = optional;
	}

	/**
	 * Sets the incomplete flag of this path attribute
	 * 
	 * @param incomplete the value of the incomplete bit
	 */
	public void setIncomplete(boolean incomplete)
	{
		this.incomplete = incomplete;
	}

	/**
	 * Sets the extended-length flag of this path attribute
	 * 
	 * @param extendedLength the value of the extended-length bit
	 */
	public void setExtendedLength(boolean extendedLength)
	{
		this.extendedLength = extendedLength;
	}

	/**
	 * Sets the transitive bit of the path attribute
	 * 
	 * @param transitive the transitive to set
	 */
	protected void setTransitive(boolean transitive)
	{
		this.transitive = transitive;
	}

	/**
	 * True if the optional bit is set
	 * 
	 * @return the optional
	 */
	public boolean isOptional()
	{
		return optional;
	}

	/**
	 * true if the incomplete bit is set
	 * 
	 * @return the incomplete
	 */
	public boolean isIncomplete()
	{
		return incomplete;
	}

	/**
	 * true if the extended length bit is set
	 * 
	 * @return the extendedLength
	 */
	public boolean isExtendedLength()
	{
		return extendedLength;
	}

}
