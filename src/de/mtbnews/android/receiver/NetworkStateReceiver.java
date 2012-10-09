package de.mtbnews.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import de.mtbnews.android.service.SubscriptionService;

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
		final boolean connectionAvailable = !intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

		if (connectionAvailable)
		{
			// Datenverbindung vorhanden
			Log.d("IBC", "Connection established, (re-)starting service");
			context
					.startService(new Intent(context, SubscriptionService.class));
		}
		else
		{
			// Service wird nicht beendet, sondern der Timer soll weiterlaufen.
			;
		}
	}
}