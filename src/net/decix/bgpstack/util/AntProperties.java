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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper to read out properties from ant-property files, substituting ${prop}
 * properties notation
 * 
 * @author sspies
 * 
 */
public class AntProperties
{
	private Properties properties = new Properties();
	private File file;

	/**
	 * Load properties from file doing variable substitution
	 * 
	 * @param file the file to load properties from
	 */
	public void load(File file) throws FileNotFoundException, IOException
	{
		this.file = file;
		properties.load(new FileInputStream(file.getAbsolutePath()));
		substitute();
	}

	private void substitute()
	{
		boolean variableSubstituted = false;
		do
		{
			variableSubstituted = false;
			for (Object keyObj : properties.keySet())
			{
				String key = (String) keyObj;
				String value = properties.getProperty(key);
				Pattern pattern = Pattern.compile(".*\\$\\{(.*)\\}");
				Matcher matcher = pattern.matcher(value);

				if (matcher.find())
				{
					String matchedKey = matcher.group(1);
					if (properties.containsKey(matchedKey))
					{
						properties.put(key, value.replaceFirst("\\$\\{" + matchedKey + "\\}", Matcher.quoteReplacement(properties.getProperty(matchedKey))));
						variableSubstituted = true;
					}

				}
			}
		}
		while (variableSubstituted);

	}

	/**
	 * Get a property
	 * 
	 * @param key the name of the property
	 * @return the value of the property
	 */
	public String getProperty(String key)
	{
		return (String) properties.get(key);
	}
	
	public int getPropertyAsInt(String key)
	{
		return getProperty(Integer.class, "parseInt", key);
	}
	
	public long getPropertyAsLong(String key)
	{
		return getProperty(Long.class, "parseLong", key);
	}
	

	public <T> T getProperty(Class<T> klass, String parseMethod, String key)
	{
		try
		{
			Method m = klass.getMethod(parseMethod, String.class);
			return (T) m.invoke(this, properties.get(key));
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public Properties getProperties()
	{
		return this.properties;
	}
	
	public boolean containsProperty(String key)
	{
		return properties.containsKey(key);
	}

	public File getFile()
	{
		return file;
	}

}
