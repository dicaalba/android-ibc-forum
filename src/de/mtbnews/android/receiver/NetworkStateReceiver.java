package de.mtbnews.android.receiver;

import de.mtbnews.android.service.SubscriptionService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		boolean noConnectivity = intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (!noConnectivity)
		{
			Log.d("IBC", "connection established, starting service");
			context.stopService(new Intent(context, SubscriptionService.class));
			context
					.startService(new Intent(context, SubscriptionService.class));
		}
		else
		{
			Log.d("IBC", "connection lost, stopping service");
			context.stopService(new Intent(context, SubscriptionService.class));
		}
	}
}