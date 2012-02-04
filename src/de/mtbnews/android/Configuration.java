package de.mtbnews.android;

import de.mtbnews.android.service.SubscriptionService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Configuration extends PreferenceActivity
{
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			public void onSharedPreferenceChanged(SharedPreferences prefs,
					String key)
			{
				if (key.equals("autostart_subscription_service"))
				{
					if (prefs.getBoolean("autostart_subscription_service",
							false))
					{

						startService(new Intent(getApplicationContext(),
								SubscriptionService.class));
						Toast.makeText(Configuration.this,
								R.string.subscription_service_started,
								Toast.LENGTH_SHORT).show();
					}
					else
					{
						stopService(new Intent(getApplicationContext(),
								SubscriptionService.class));
						Toast.makeText(Configuration.this,
								R.string.subscription_service_stopped,
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(listener);

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
