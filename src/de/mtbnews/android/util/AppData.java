package de.mtbnews.android.util;

import org.mcsoxford.rss.RSSFeed;
import org.xmlrpc.android.XMLRPCClient;

import de.mtbnews.android.tapatalk.TapatalkClient;

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

	public static TapatalkClient client;
}
