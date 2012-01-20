package de.mtbnews.android.util;

import org.mcsoxford.rss.RSSFeed;
import org.xmlrpc.android.XMLRPCClient;

/**
 * Speichert Anwendungsdaten.
 * 
 * @author dankert
 * 
 */
public class AppData
{
	public static RSSFeed newsFeed;

	public static RSSFeed photoFeed;

	public static XMLRPCClient client;
}
