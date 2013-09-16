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

package net.decix.bgpstack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import net.decix.bgpstack.BGPConstants;
import net.decix.bgpstack.BGPRoute;
import net.decix.bgpstack.types.IPv4Prefix;
import net.decix.bgpstack.types.IPv6Prefix;

/**
 * Utility class for conventient access to raw data.
 * 
 * @author sspies
 * 
 */
public class Utility implements BGPConstants
{
	private static Logger logger = Logger.getLogger("net.decix.bgpstack.util.Utility");

	/**
	 * Converts an integer to one byte
	 * 
	 * @param value the integer value to convert
	 * @return the computed byte
	 * @throws UtilityException if Integer value is larger than 2^15
	 */
	public static final byte integerToOneByte(int value) throws UtilityException
	{
		if ((value > Math.pow(2, 15)) || (value < 0))
		{
			throw new UtilityException("Integer value " + value + " is larger than 2^15");
		}
		return (byte) (value & 0xFF);
	}

	/**
	 * Converts an integer to two bytes
	 * 
	 * @param value the integer value to convert
	 * @return the computed byte-array
	 * @throws UtilityException if Intger value is larger than 2^31
	 */
	public static final byte[] integerToTwoBytes(int value) throws UtilityException
	{
		byte[] result = new byte[2];
		if ((value > Math.pow(2, 31)) || (value < 0))
		{
			throw new UtilityException("Integer value " + value + " is larger than 2^31");
		}
		result[0] = (byte) ((value >>> 8) & 0xFF);
		result[1] = (byte) (value & 0xFF);
		return result;
	}

	/**
	 * Converts a long to four bytes
	 * 
	 * @param value the long value to convert
	 * @return the computed byte-array
	 * @throws UtilityException just for completeness
	 */
	public static final byte[] longToFourBytes(long value) throws UtilityException
	{
		byte[] result = new byte[4];
		result[0] = (byte) ((value >>> 24) & 0xFF);
		result[1] = (byte) ((value >>> 16) & 0xFF);
		result[2] = (byte) ((value >>> 8) & 0xFF);
		result[3] = (byte) (value & 0xFF);
		return result;
	}

	/**
	 * Converts a long to six bytes
	 * 
	 * @param value the long value to convert
	 * @return the computed byte-array
	 * @throws UtilityException just for completeness
	 */
	public static final byte[] longToSixBytes(long value) throws UtilityException
	{
		byte[] result = new byte[6];
		result[0] = (byte) ((value >>> 40) & 0xFF);
		result[1] = (byte) ((value >>> 32) & 0xFF);
		result[2] = (byte) ((value >>> 24) & 0xFF);
		result[3] = (byte) ((value >>> 16) & 0xFF);
		result[4] = (byte) ((value >>> 8) & 0xFF);
		result[5] = (byte) (value & 0xFF);
		return result;
	}

	/**
	 * Converts one byte to an integer
	 * 
	 * @param value the byte to convert to an integer
	 * @return the computed integer value
	 * @throws UtilityException just for completeness
	 */
	public static final int oneByteToInteger(byte value) throws UtilityException
	{
		return (int) value & 0xFF;
	}

	/**
	 * Converts two bytes to an integer
	 * 
	 * @param value the byte-array to convert to an integer (MSB left)
	 * @return the computed integer value
	 * @throws UtilityException if byte array is too short
	 */
	public static final int twoBytesToInteger(byte[] value) throws UtilityException
	{
		if (value.length < 2)
		{
			throw new UtilityException("Byte array too short!");
		}
		int temp0 = value[0] & 0xFF;
		int temp1 = value[1] & 0xFF;
		return ((temp0 << 8) + temp1);
	}

	/**
	 * Converts four bytes to a long
	 * 
	 * @param value the byte-array to convert to an long (MSB left)
	 * @return the computed long value
	 * @throws UtilityException if byte array is too short
	 */
	public static final long fourBytesToLong(byte[] value) throws UtilityException
	{
		if (value.length < 4)
		{
			throw new UtilityException("Byte array too short!");
		}
		int temp0 = value[0] & 0xFF;
		int temp1 = value[1] & 0xFF;
		int temp2 = value[2] & 0xFF;
		int temp3 = value[3] & 0xFF;
		return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
	}

	/**
	 * Converts six bytes to a long
	 * 
	 * @param value the byte-array to convert to a long (MSB left)
	 * @return the computer long value
	 * @throws UtilityException if byte array is too short
	 */
	public static final long sixBytesToLong(byte[] value) throws UtilityException
	{
		if (value.length < 6)
		{
			throw new UtilityException("Byte array too short!");
		}
		int temp0 = value[0] & 0xFF;
		int temp1 = value[1] & 0xFF;
		int temp2 = value[2] & 0xFF;
		int temp3 = value[3] & 0xFF;
		int temp4 = value[4] & 0xFF;
		int temp5 = value[5] & 0xFF;
		return ((((long) temp0) << 40) + (((long) temp1) << 32) + (((long) temp2) << 24) + (temp3 << 16) + (temp4 << 8) + temp5);
	}

	/**
	 * Dump bytes from byte-array
	 * 
	 * @param data the byte-array
	 * @return the byte-array in 0x.. notation
	 */
	public static final String dumpBytes(byte[] data)
	{
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (byte b : data)
		{
			i++;
			sb.append(String.valueOf(b));
			if (i < data.length)
				sb.append(", ");
			if ((i % 15) == 0)
				sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Truncate number of bytes from the left of a byte-array
	 * 
	 * @param b1 the byte-array
	 * @param bytes the number of bytes to truncate
	 * @return the resulting new byte-array (new length is b1.length - bytes)
	 */
	public static byte[] truncateLeft(byte[] b1, int bytes)
	{
		int restBytesLength = b1.length - bytes;
		byte[] b2 = new byte[restBytesLength];
		System.arraycopy(b1, bytes, b2, 0, restBytesLength);
		return b2;
	}

	/**
	 * Shifts a byte-array a number of bytes to the left
	 * 
	 * @param b1 the byte-array
	 * @param bytes the number of shifts (shifts 0 from right)
	 * @return the resulting new byte-array
	 */
	public static byte[] shiftLeft(byte[] b1, int bytes)
	{
		byte[] result = new byte[b1.length];
		System.arraycopy(b1, bytes, result, 0, bytes);
		return result;
	}

	/**
	 * Converts a hex String to a byte-array "440f0ac0a8ffef01" -> new byte[] {
	 * 0x44, 0x0F, 0xAC, 0x0A, 0x8F, 0xEF, 0x01 }
	 * 
	 * @param s the hex string
	 * @return the resulting byte-array
	 */
	public static byte[] hexStringToByteArray(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	
	public static String byteArrayToHexString(byte[] bytes)
	{
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	
	
	/**
	 * Concatenates two byte arrays
	 * 
	 * @param b1 byte-array one
	 * @param b2 byte-array two
	 * @return the resulting new byte array (new length is b1.length +
	 *         b2.length)
	 */
	public static byte[] concatenateTwoByteArrays(byte[] b1, byte[] b2)
	{
		int length = b1.length + b2.length;
		byte[] sum = new byte[length];
		System.arraycopy(b1, 0, sum, 0, b1.length);
		System.arraycopy(b2, 0, sum, b1.length, b2.length);

		return sum;
	}

	/**
	 * Generates number of random bytes
	 * 
	 * @param num the number of random bytes to generate
	 * @return an array containing random bytes
	 */
	public static byte[] generateRandomBytes(int num)
	{
		Random rand = new Random();
		byte randomBytes[] = new byte[num];
		rand.nextBytes(randomBytes);
		return randomBytes;
	}

	/**
	 * Generates a random {@link IPv4Prefix} of random size
	 * 
	 * @param rand Random seed
	 * @return random {@link IPv4Prefix}
	 */
	public static IPv4Prefix generateRandomPrefix(Random rand)
	{
		if (rand == null)
			rand = new Random();

		// min prefix-length 8
		return generateRandomPrefix(rand.nextInt(25) + 8);
	}

	/**
	 * Generates a random {@link IPv4Prefix} with a specific netmask
	 * 
	 * @param networkBits amount of network bits
	 * @return random valid {@link IPv4Prefix}
	 */
	public static IPv4Prefix generateRandomPrefix(int networkBits)
	{
		byte[] randomBytes;
		try
		{
			do
			{
				randomBytes = generateRandomBytes(4);
				// filter multicast
			}
			while (((oneByteToInteger(randomBytes[0]) >> 5) == 0x07));
		}
		catch (UtilityException e1)
		{
			e1.printStackTrace();
			return null;
		}

		try
		{
			long addressFull = fourBytesToLong(randomBytes);
			long mask = 0L;
			for (int i = 0; i < networkBits; i++)
				mask |= (1 << (31 - i));
			addressFull &= mask;
			return new IPv4Prefix(longToFourBytes(addressFull), networkBits);
		}
		catch (UtilityException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Generates a random {@link IPv6Prefix} of random size
	 * 
	 * @param rand Random seed
	 * @return random {@link IPv6Prefix}
	 */
	public static IPv6Prefix generateRandomPrefix6(Random rand)
	{
		if (rand == null)
			rand = new Random();

		// min prefix-length 16
		return generateRandomPrefix6(rand.nextInt(32) + 16);
	}

	/**
	 * Generates a random {@link IPv6Prefix} with a specific netmask
	 * 
	 * @param networkBits amount of network bits
	 * @return random valid {@link IPv6Prefix}
	 */
	public static IPv6Prefix generateRandomPrefix6(int networkBits)
	{
		byte[] randomBytes;
		randomBytes = generateRandomBytes(17);

		BigInteger addressFull = new BigInteger(1, randomBytes);
		BigInteger mask = BigInteger.ZERO;

		for (int i = 0; i < networkBits; i++)
			mask = mask.or(BigInteger.ONE.shiftLeft(127 - i));
		addressFull = addressFull.and(mask);
		return new IPv6Prefix(bigIntegerToUnsignedByteArray(addressFull, 16), networkBits);
	}

	public static final byte[] bigIntegerToUnsignedByteArray(BigInteger a, int len) throws NumberFormatException
	{
		byte[] buf = new byte[len];
		
		if (a.bitLength() > len * 8)
			throw new NumberFormatException("BigInteger doesn't fit into array");
		
		byte[] b = a.toByteArray();
		
		if (b.length == len + 1)
			if (b[0] == 0)
				for (int i = 0; i < buf.length; i++)
					buf[i] = b[i + 1];
			else
				throw new NumberFormatException("Internal Error");
		else if (b.length <= len)
			for (int i = 0; i < b.length; i++)
				buf[buf.length - 1 - i] = b[b.length - 1 - i];
		else
			throw new NumberFormatException("Internal Error");
		
		return buf;
	}

	/**
	 * Read an integer from a file (e.g., for pid-files)
	 * 
	 * @param file the file to read the integer from
	 * @return the integer read from the file
	 * @throws IOException if there's some {@link IOException} talking to the
	 *             file
	 * @throws NumberFormatException if the file does not contain only a single
	 *             integer
	 */
	public static int readIntFromFile(File file) throws IOException, NumberFormatException
	{
		StringBuffer sb = new StringBuffer();
		FileInputStream fis = new FileInputStream(file);

		for (;;)
		{
			byte[] buf = new byte[10];

			if (fis.read(buf) == -1)
				break;

			sb.append(new String(buf));
		}

		return Integer.parseInt(sb.toString().trim());
	}

	/**
	 * Read an integer from a script
	 * 
	 * @param file the script to run and expect one integer from
	 * @return the integer value the script sent to stdout
	 * @throws IOException if there's some {@link IOException} talking to the
	 *             script
	 * @throws NumberFormatException if the script didn't output only a single
	 *             integer
	 */
	public static int readIntFromScript(File file, String... params) throws IOException, NumberFormatException
	{
		String param = "";
		for (String p : params)
			param += p + " ";

		StringBuffer buffer = new StringBuffer();

		Runtime rt = Runtime.getRuntime();

		Process proc;
		try
		{
			proc = rt.exec(file.getAbsolutePath() + " " + param);
		}
		catch (IOException e)
		{
			return -1;
		}

		InputStream is = proc.getInputStream();

		byte[] readBuffer = new byte[5];

		for (;;)
		{
			int bytesRead;
			try
			{
				if ((bytesRead = is.read(readBuffer)) == -1)
					break;
			}
			catch (IOException e)
			{
				return -1;
			}
			buffer.append(new String(readBuffer, 0, bytesRead));
		}

		return (int) Float.parseFloat(buffer.toString().trim());
	}

	/**
	 * Read a boolean from a script
	 * 
	 * @param file the script to run and expect one boolean from
	 * @return the boolean value the script sent to stdout (1 is true, 0 is
	 *         false)
	 * @throws IOException if there's some {@link IOException} talking to the
	 *             script
	 */
	public static boolean readBooleanFromScript(File file, String... params) throws IOException
	{
		String param = "";
		for (String p : params)
			param += p + " ";

		Runtime rt = Runtime.getRuntime();

		Process proc = rt.exec(file.getAbsolutePath() + " " + param);

		InputStream is = proc.getInputStream();

		return is.read() == 49;
	}

	public static void runScript(File file, String... params) throws IOException
	{
		String param = "";
		for (String p : params)
			param += p + " ";

		String command = file.getAbsolutePath() + " " + param;
		logger.fine("Running command: " + command);

		Process proc = Runtime.getRuntime().exec(command);
		try
		{
			proc.waitFor();
		}
		catch (InterruptedException e)
		{
		}

	}

	/**
	 * Checks for equality of a list of {@link BGPRoute}s and a list of
	 * {@link IPv4Prefix}es.
	 * 
	 * @param routeList the list of routes to check against the list of prefixes
	 * @param prefixList the list of prefixes to check against the list of
	 *            routes
	 * @return true if they are equal. false if they are not equal
	 */
	public static boolean containsAllPrefixes(List<BGPRoute> routeList, List<IPv4Prefix> prefixList)
	{
		if (routeList.size() != prefixList.size())
			return false;

		for (IPv4Prefix p : prefixList)
			if (!routeListContainsPrefix(routeList, p))
				return false;

		return true;
	}

	/**
	 * Checks if a {@link IPv4Prefix} is contained in a list of {@link BGPRoute}
	 * s.
	 * 
	 * @param haystack the list of {@link BGPRoute}s to check against
	 * @param needle the needle to search
	 * @return true if needle is contained in haystack. false if it is not
	 *         contained.
	 */
	public static boolean routeListContainsPrefix(List<BGPRoute> haystack, IPv4Prefix needle)
	{
		for (BGPRoute r : haystack)
			if (r.getPrefix().equals(needle))
				return true;
		return false;
	}

	/**
	 * Enumeration of network classes (see <a
	 * href="http://tools.ietf.org/html/rfc791">RFC791</a>)
	 * 
	 * @author sspies
	 * 
	 */
	public enum IP_CLASS
	{
		A(8), B(16), C(24), D(0), E(0);

		private int prefixLength;

		private IP_CLASS(int prefixLength)
		{
			this.prefixLength = prefixLength;
		}

		public int getPrefixLength()
		{
			return prefixLength;
		}
	}

	/**
	 * Gets the class of an address
	 * 
	 * @param address the address to find the class for
	 * @return the resulting {@link IP_CLASS} of the address
	 */
	public static IP_CLASS getRangeClassOfAddress(InetAddress address)
	{
		byte interestingByte = address.getAddress()[0];
		if ((interestingByte & 0x80) == 0)
			return IP_CLASS.A;
		else if ((interestingByte & 0xC0) == 0x80)
			return IP_CLASS.B;
		else if ((interestingByte & 0xE0) == 0xC0)
			return IP_CLASS.C;
		else if ((interestingByte & 0xF0) == 0xE0)
			return IP_CLASS.D;
		else if ((interestingByte & 0xF0) == 0xF0)
			return IP_CLASS.E;

		// not reached
		return null;
	}

	public static List<Class> getClassesForPackage(String pckgname) throws ClassNotFoundException
	{
		// This will hold a list of directories matching the pckgname.
		// There may be more than one if a package is split over multiple
		// jars/paths
		List<Class> classes = new ArrayList<Class>();
		ArrayList<File> directories = new ArrayList<File>();
		try
		{
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null)
			{
				throw new ClassNotFoundException("Can't get class loader.");
			}
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
			while (resources.hasMoreElements())
			{
				URL res = resources.nextElement();
				if (res.getProtocol().equalsIgnoreCase("jar"))
				{
					JarURLConnection conn = (JarURLConnection) res.openConnection();
					JarFile jar = conn.getJarFile();
					for (JarEntry e : Collections.list(jar.entries()))
					{

						if (e.getName().startsWith(pckgname.replace('.', '/')) && e.getName().endsWith(".class") && !e.getName().contains("$"))
						{
							String className = e.getName().replace("/", ".").substring(0, e.getName().length() - 6);
							classes.add(Class.forName(className));
						}
					}
				}
				else
					directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
			}
		}
		catch (NullPointerException x)
		{
			throw new ClassNotFoundException(pckgname + " does not appear to be " + "a valid package (Null pointer exception)");
		}
		catch (UnsupportedEncodingException encex)
		{
			throw new ClassNotFoundException(pckgname + " does not appear to be " + "a valid package (Unsupported encoding)");
		}
		catch (IOException ioex)
		{
			throw new ClassNotFoundException("IOException was thrown when trying " + "to get all resources for " + pckgname);
		}

		// For every directory identified capture all the .class files
		for (File directory : directories)
		{
			if (directory.exists())
			{
				// Get the list of the files contained in the package
				String[] files = directory.list();
				for (String file : files)
					// we are only interested in .class files
					if (file.endsWith(".class"))
						// removes the .class extension
						classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
			}
			else
				throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
		}
		return classes;
	}

	public static List<Class> getClassessOfInterface(String thePackage, Class theInterface)
	{
		List<Class> classList = new ArrayList<Class>();
		try
		{
			for (Class discovered : getClassesForPackage(thePackage))
				if (Arrays.asList(discovered.getInterfaces()).contains(theInterface))
					classList.add(discovered);
		}
		catch (ClassNotFoundException ex)
		{

		}

		return classList;
	}

	public static boolean isProcessRunning(String name, int pid) throws UtilityException
	{
		try
		{
			Process proc = Runtime.getRuntime().exec("ps -p " + pid);
			return proc.waitFor() == 0;
		}
		catch (Exception e)
		{
			throw new UtilityException("Don't know if process " + name + " is running");
		}
	}

	public static long inetAddressStringToLong(String inetAddress) throws UtilityException
	{
		String[] addressArray = inetAddress.split("\\.");
		try
		{
			// enforce unsigned bytes by casts ;-)
			return fourBytesToLong(new byte[] { (byte) Integer.parseInt(addressArray[0]), (byte) Integer.parseInt(addressArray[1]), (byte) Integer.parseInt(addressArray[2]), (byte) Integer.parseInt(addressArray[3]) });
		}
		catch (Exception e)
		{
			throw new UtilityException("Invalid address format: " + inetAddress + " " + e.getMessage());
		}
	}

	public static InetAddress inetAddressLongToString(long inetAddress) throws UtilityException
	{
		try
		{
			return InetAddress.getByAddress(longToFourBytes(inetAddress));
		}
		catch (UnknownHostException e)
		{
			throw new UtilityException(e.getMessage());
		}
	}

	public static int getSizeOfNextHopByAfi(int afi) throws UtilityException
	{
		switch (afi)
		{
			case 1:
				return AFI_IPV4_NH_SIZE;
			case 2:
				return AFI_IPV6_NH_SIZE;
			default:
				throw new UtilityException("Unknown AFI: " + afi);
		}
	}

}
