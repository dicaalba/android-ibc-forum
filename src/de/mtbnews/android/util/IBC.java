package de.mtbnews.android.util;

/**
 * Statische Resourcen für IBC-Forum.
 * 
 * @author dankert
 * 
 */
public interface IBC
{

	/**
	 * Tapatalk-API für IBC-Forum.
	 */
	static final String IBC_FORUM_CONNECTOR_URL = "http://www.mtb-news.de/forum/mobiquo/mobiquo.php";

	/**
	 * Feed-URL für Forum.
	 */
	static final String IBC_FORUM_RSS_URL = "http://www.mtb-news.de/forum/external.php?type=RSS2";

	/**
	 * Fee-URL für Nachrichten.
	 */
	static final String IBC_NEWS_RSS_URL = "http://www.mtb-news.de/news/feed/";

	/**
	 * Feed-URL für Fotos.
	 */
	static final String IBC_FOTOS_RSS_URL = "http://fotos.mtb-news.de/photos/recent.rss";

	/**
	 * Logging-Tag.
	 */
	static final String TAG = "IBC";

	/**
	 * Sitzungs-Timeout. Ab wieviel Minuten sollte das Login erneuert werden?
	 */
	static final int LOGIN_TIMEOUT = 10;
}
