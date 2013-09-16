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

import static net.decix.bgpstack.util.Utility.twoBytesToInteger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads complete BGP packets from an InputStream. Copes with truncated
 * messages.
 * 
 * @author sspies
 * 
 */
public class BGPPacketInputStream extends InputStream
{
	BufferedInputStream is;
	private final int MAX_SIZE = 163840;

	byte[] readBuffer = new byte[MAX_SIZE];

	/**
	 * Instantiates a new {@link BGPPacketInputStream} object.
	 * 
	 * @param is InputStream to read the BGP packets from
	 */
	public BGPPacketInputStream(InputStream is)
	{
		this.is = new BufferedInputStream(is);
	}

	/**
	 * Read a packet from {@link InputStream}. Blocks until there's a complete
	 * BGP packet available.
	 * 
	 * @return BGP packet containing multiple BGP messages as a byte-array
	 * @throws IOException if there's an error with the {@link InputStream} to
	 *             read from
	 * @throws UtilityException if the length-field of a BGP message is not
	 *             well-formed
	 * @throws InterruptedException
	 */
	public byte[] readBGPPacket() throws IOException
	{
		long delay = 0;
		int bytesRead = 0;
		for (;;)
		{
			bytesRead = waitForBytes();

			if (bytesRead < 0) break;

			if (bytesRead > 18)
			{
				byte[] lengthArray = new byte[2];
				System.arraycopy(readBuffer, 16, lengthArray, 0, 2);
				int length;
				try
				{
					length = twoBytesToInteger(lengthArray);
				}
				catch (UtilityException e)
				{
					throw new IOException(e);
				}

				if (bytesRead >= length)
				{
					byte[] packet = new byte[length];
					System.arraycopy(readBuffer, 0, packet, 0, length);

					if (bytesRead > length)
					{
						// cut off first message
						
						// go back to the beginning
						is.reset();
						// reread first message
						is.read(readBuffer, 0, length);
						// mark after first message
						is.mark(MAX_SIZE);
					}

					return packet;
				}
				else
				{
					delay = 200;
					is.reset();
				}
			}
			else
			{
				delay = 1000;
				is.reset();
			}

			if (delay != 0)
			{
				try
				{
					Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{/* Intentionally nothing */
				}
				finally
				{
					delay = 0;
				}
			}

		}

		return null;

	}

	@Override
	public int read() throws IOException
	{
		return is.read();
	}

	/**
	 * Wait for bytes to arrive. Public for profiling. Do not use!
	 */
	public int waitForBytes() throws IOException
	{
		is.mark(MAX_SIZE);
		return is.read(readBuffer, 0, MAX_SIZE);
	}

}
