/**
 * 
 */
package de.mtbnews.android.service;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import de.mtbnews.android.ForumActivity;
import de.mtbnews.android.IBCApplication;
import de.mtbnews.android.R;
import de.mtbnews.android.TopicActivity;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.tapatalk.wrapper.Topic;

/**
 * @author dankert
 * 
 */
public class SubscriptionService extends Service
{
	public SubscriptionService()
	{
	}

	private IBCApplication ibcApp;
	private static Timer timer;
	private SharedPreferences prefs;
	private static final int NOTIFICATION_UPLOAD = 1;
	private Date startDate;

	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	public void onCreate()
	{
		Log.d(this.getClass().getSimpleName(), "Starting service");
		super.onCreate();
		ibcApp = (IBCApplication) getApplication();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		startDate = new Date();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TestSubscriptionTask(), 1000, 60000);
	}

	private class TestSubscriptionTask extends TimerTask
	{
		int nId = 0;

		public void run()
		{
			Log.d(this.getClass().getSimpleName(),
					"Testing for unread subscriptions");
			final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			TapatalkClient client = ibcApp.getTapatalkClient();

			try
			{
				List<Forum> subscribedForum = client.getSubscribedForum(true);
				for (Forum forum : subscribedForum)
				{
					final Intent notificationIntent = new Intent(
							SubscriptionService.this, ForumActivity.class);
					notificationIntent.putExtra("forum_id", forum.getId());
					final PendingIntent contentIntent = PendingIntent
							.getActivity(SubscriptionService.this, 0,
									notificationIntent, 0);

					final String tickerText = getResources().getString(
							R.string.unread_forum);

					final Notification notification = new Notification(
							R.drawable.ibc_logo, tickerText, System
									.currentTimeMillis());
					notification.setLatestEventInfo(getApplicationContext(),
							getResources().getString(R.string.unread_forum),
							forum.getTitle(), contentIntent);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					// notification.flags = Notification.FLAG_ONGOING_EVENT
					// | Notification.FLAG_NO_CLEAR;
					nm.notify(++nId, notification);

					Log.d(this.getClass().getName(), "forum unread: "
							+ forum.getName() + forum.getId());
				}
			}
			catch (TapatalkException e)
			{
				Log.w(this.getClass().getSimpleName(), e);
				throw new RuntimeException(e);
			}
			catch (Exception e)
			{
				Log.w("Laufzeitfehler", e);
				throw new RuntimeException(e);
			}

			try
			{
				List<Topic> subscribedTopic = client.getSubscribedTopics(0, 10,
						true);
				for (Topic topic : subscribedTopic)
				{
					final Intent notificationIntent = new Intent(
							SubscriptionService.this, TopicActivity.class);
					notificationIntent.putExtra("topic_id", topic.getId());
					final PendingIntent contentIntent = PendingIntent
							.getActivity(SubscriptionService.this, 0,
									notificationIntent, 0);

					final String tickerText = getResources().getString(
							R.string.unread_topic);

					final Notification notification = new Notification(
							R.drawable.ibc_logo, tickerText, System
									.currentTimeMillis());
					notification.setLatestEventInfo(getApplicationContext(),
							getResources().getString(R.string.unread_topic),
							topic.getTitle(), contentIntent);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					// notification.flags = Notification.FLAG_ONGOING_EVENT
					// | Notification.FLAG_NO_CLEAR;
					nm.notify(++nId, notification);

					Log.d(this.getClass().getSimpleName(), "forum unread: "
							+ topic.getName() + topic.getId());
				}
			}
			catch (TapatalkException e)
			{
				Log.w(this.getClass().getSimpleName(), e);
				throw new RuntimeException(e);
			}
			catch (Exception e)
			{
				Log.w("Laufzeitfehler", e);
				throw new RuntimeException(e);
			}
		}
	}

	public void onDestroy()
	{
		timer.cancel();
		Log.d(this.getClass().getSimpleName(), "Destroying service");

		super.onDestroy();
	}

}
