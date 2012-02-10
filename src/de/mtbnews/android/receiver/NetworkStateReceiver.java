package de.mtbnews.android.receiver;

import de.mtbnews.android.service.SubscriptionService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Ein Receiver für die Erkennung von Änderungen am Verbindungsstatus. Wenn die
 * Datenverbindung unterbrochen oder wieder hergestellt wird, wird dieser
 * Receiver informiert.
 * <ul>
 * <li>Wird die Datenverbindung unterbrochen, wird der Hintergrundservice
 * gestoppt.</li>
 * <li>Wird die Datenverbindung hergestellt, wird der Hintergrundservice
 * gestartet.</li>
 * </ul>
 * 
 * @author dankert
 * 
 */
public class NetworkStateReceiver extends BroadcastReceiver
{

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 *      android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Feststellen, ob die Verbindung besteht.
		final boolean noConnectivity = intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

		if (!noConnectivity)
		{
			// Datenverbindung vorhanden
			Log.d("IBC", "connection established, starting service");
			context.stopService(new Intent(context, SubscriptionService.class));
			context
					.startService(new Intent(context, SubscriptionService.class));
		}
		else
		{
			// Datenverbindung unterbrochen
			Log.d("IBC", "connection lost, stopping service");
			context.stopService(new Intent(context, SubscriptionService.class));
		}
	}
}