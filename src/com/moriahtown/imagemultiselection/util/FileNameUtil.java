package com.moriahtown.imagemultiselection.util;
/**
 * @author "Hanson Kim"
 * @date 2012. 1. 27.占쏙옙占쏙옙 2:31:40
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author "Hanson Kim"
 * 
 */
public class FileNameUtil
{

	private static final String pattern = "(\\.[a-zA-Z0-9]{2,6}$)";

	/**
	 * append string to file name
	 * 
	 * @param fileName
	 * @return
	 */
	public static String appendFileName(String fileName, String appenderString)
	{
		Pattern p_ext = Pattern.compile(pattern);
		Matcher m_ext = p_ext.matcher(fileName);

		if (m_ext.find())
		{
			String name = fileName
			        .substring(0, fileName.indexOf(m_ext.group()))
			        + appenderString;

			fileName = name + m_ext.group();
		}
		else
		{
			fileName += appenderString;
		}

		return fileName;
	}

	/**
	 * form file extention to lower case
	 * 
	 * @param fileName
	 * @return
	 */
	public static String lowerExtension(String fileName)
	{

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(fileName);
		if (m.find())
		{
			String extName = m.group().toLowerCase();
			fileName = fileName.replaceAll("(.[a-zA-Z0-9]+)$", extName);
		}

		return fileName;
	}

	public static String getFileName(String fileName)
	{

		Pattern p_ext = Pattern.compile(pattern);
		Matcher m_ext = p_ext.matcher(fileName);

		if (m_ext.find())
		{
			return fileName.substring(0, fileName.indexOf(m_ext.group()));
		}
		else
		{
			return fileName;
		}
	}

	/**
	 * @param orgName
	 * @return
	 */
	public static String getExtension(String fileName)
	{

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(fileName);
		if (m.find())
		{
			return m.group().toLowerCase();
		}
		else
		{
			return "";
		}
	}

}
