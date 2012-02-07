/**
 * 
 */
package de.mtbnews.android;

import org.mcsoxford.rss.RSSFeed;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import de.mtbnews.android.service.SubscriptionService;
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

	public SharedPreferences prefs;

	public int themeResId;

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
		Log.i("IBC","starting main application");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		themeResId = (prefs.getBoolean("ibc_theme", true)) ? R.style.IBC
				: android.R.style.Theme;

		client = new TapatalkClient(IBC.IBC_FORUM_CONNECTOR_URL);

		if (prefs.getBoolean("autostart_subscription_service", false))
		{
			startService(new Intent(getApplicationContext(),
					SubscriptionService.class));

			Toast.makeText(this, R.string.subscription_service_started,
					Toast.LENGTH_SHORT).show();
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
