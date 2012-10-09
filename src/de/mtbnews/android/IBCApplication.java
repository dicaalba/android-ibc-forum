/**
 * 
 */
package de.mtbnews.android;

import java.lang.ref.SoftReference;
import java.util.List;

import org.mcsoxford.rss.RSSFeed;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.mtbnews.android.service.SubscriptionService;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.wrapper.Forum;
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
	/**
	 * Speichert eine Referenz auf den Tapatalk-Client. Dieser Client kann vom
	 * GC jederzeit entfernt werden, wenn der Speicherverbrauch zu hoch ist.
	 */
	private SoftReference<TapatalkClient> tapatalkClientRef = new SoftReference<TapatalkClient>(
			null);

	/**
	 * Speichert eine Referenz auf den RSSFeed. Dieser Feed kann vom GC
	 * jederzeit entfernt werden, wenn der Speicherverbrauch zu hoch ist.
	 */
	private SoftReference<RSSFeed> newsFeedRef = new SoftReference<RSSFeed>(
			null);
	/**
	 * Speichert eine Referenz auf die Forum-Liste. Diese Liste kann vom GC
	 * jederzeit entfernt werden, wenn der Speicherverbrauch zu hoch ist.
	 */
	private SoftReference<List<Forum>> listForumRef = new SoftReference<List<Forum>>(
			null);;

	public SharedPreferences prefs;

	public int themeResId;

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate()
	{
		Log.d(IBC.TAG, "starting main application");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		themeResId = (prefs.getBoolean("ibc_theme", true)) ? R.style.IBC
				: R.style.Default;

		if (prefs.getBoolean("autostart_subscription_service", false))
		{
			startService(new Intent(getApplicationContext(),
					SubscriptionService.class));
		}

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
		Log.d(IBC.TAG, "Low memory detected...");
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
		Log.d(IBC.TAG, "terminating application");
		super.onTerminate();
	}

	/**
	 * Liefert den Tapatalk-Client. Der Client wird zwar gecacht, kann jedoch,
	 * falls der bisherige durch den GC weggeräumt wurde, auch ein neuer Client
	 * ohne bestehendes Login sein!
	 * 
	 * @return
	 */
	public TapatalkClient getTapatalkClient()
	{
		TapatalkClient client = tapatalkClientRef.get();
		if (client != null)
		{
			return client;
		}
		else
		{
			Log.d(IBC.TAG, "Creating a new tapatalk client");

			client = new TapatalkClient(IBC.IBC_FORUM_CONNECTOR_URL);
			client.setUserAgent(
					"Mozilla/5.0 (compatible; Android)");
			tapatalkClientRef = new SoftReference<TapatalkClient>(client);
			return client;
		}
	}

	public RSSFeed getNewsFeed()
	{
		return newsFeedRef.get();
	}

	public void setNewsFeed(RSSFeed feed)
	{
		newsFeedRef = new SoftReference<RSSFeed>(feed);
	}

	public List<Forum> getForumList()
	{
		return listForumRef.get();
	}

	public void setForumList(List<Forum> feed)
	{
		listForumRef = new SoftReference<List<Forum>>(feed);
	}
}
