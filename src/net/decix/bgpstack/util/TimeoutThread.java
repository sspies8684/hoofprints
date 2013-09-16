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

import java.util.logging.Logger;

/**
 * Wait for a timeout to occur. Is resetable to T0.
 * 
 * @author sspies
 * 
 */
public class TimeoutThread extends Thread
{
	private int timeout;
	private boolean shouldISleep = true;
	private Object mutex = new Object();
	private Logger logger = Logger.getLogger(TimeoutThread.class.getCanonicalName());

	/**
	 * Instantiates a new {@link TimeoutThread} object.
	 * 
	 * @param timeout the time to wait in milliseconds
	 */
	public TimeoutThread(int timeout)
	{
		this.timeout = timeout;

	}

	@Override
	/**
	 * Mainloop. Wait for the {@link TimeoutThread} to expire with {@link TimeoutThread#join}
	 */
	public void run()
	{
		setName("TimeoutThread");

		do
		{
			try
			{
				synchronized (mutex)
				{
					logger.fine("waiting for timeout or notify");
					shouldISleep = false;
					mutex.wait(timeout);
				}
			}
			catch (InterruptedException e)
			{
			}
		}
		while (shouldISleep);
		logger.fine("timed out");
	}

	/**
	 * Reset the timer, so it will start counting from zero.
	 */
	public void reset()
	{
		synchronized (mutex)
		{
			logger.entering("TimeoutThread", "reset");
			shouldISleep = true;
			mutex.notify();
		}

	}

}
