package de.mtbnews.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.mtbnews.android.service.SubscriptionService;

public class Configuration extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setTheme(R.style.Default);

		OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
			{
				if (key.equals("autostart_subscription_service"))
				{
					if (prefs.getBoolean("autostart_subscription_service", false))
					{

						startService(new Intent(getApplicationContext(), SubscriptionService.class));
						Toast.makeText(Configuration.this, R.string.subscription_service_started, Toast.LENGTH_SHORT)
								.show();
					}
					else
					{
						stopService(new Intent(getApplicationContext(), SubscriptionService.class));
						Toast.makeText(Configuration.this, R.string.subscription_service_stopped, Toast.LENGTH_SHORT)
								.show();
					}
				}

				// Wenn Intervall für Abodienst geändert, dann den Service neu
				// starten.
				if (key.equals("subscription_service_interval"))
				{
					if (prefs.getBoolean("autostart_subscription_service", false))
					{
						// Restart
						stopService(new Intent(getApplicationContext(), SubscriptionService.class));
						startService(new Intent(getApplicationContext(), SubscriptionService.class));
						Toast.makeText(Configuration.this, R.string.subscription_service_started, Toast.LENGTH_SHORT)
								.show();
					}
				}

				// Theme geändert
				if (key.equals("ibc_theme"))
				{
					final IBCApplication application = (IBCApplication) getApplication();
					if (prefs.getBoolean("ibc_theme", false))
						application.setTheme(R.style.IBC);
					else
						application.setTheme(android.R.style.Theme);

					Toast.makeText(Configuration.this, R.string.theme_changed, Toast.LENGTH_SHORT).show();
				}
			}
		};

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Den Listener setzen.
		prefs.registerOnSharedPreferenceChangeListener(listener);

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
