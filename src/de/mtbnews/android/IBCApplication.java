/**
 * 
 */
package de.mtbnews.android;

import org.mcsoxford.rss.RSSFeed;

import android.app.Application;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.util.IBC;

/**
 * IBC-Application. Diese Klasse dient als anwendungsweite (und damit
 * Activity-übergreifende) Ablage für Informationen.
 * 
 * @author dankert
 * 
 */
public class IBCApplication extends Application
{
	public RSSFeed newsFeed;

	public RSSFeed photoFeed;

	public TapatalkClient client;

	public TapatalkClient getTapatalkClient()
	{
		return client;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate()
	{
		client = new TapatalkClient(IBC.IBC_FORUM_CONNECTOR_URL);

		super.onCreate();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Application#onLowMemory()
	 */
	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate()
	{
		super.onTerminate();
	}
}
