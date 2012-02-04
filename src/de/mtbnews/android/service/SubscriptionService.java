/**
 * 
 */
package de.mtbnews.android.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint.Join;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import de.mtbnews.android.ForumActivity;
import de.mtbnews.android.IBCApplication;
import de.mtbnews.android.MailboxActivity;
import de.mtbnews.android.R;
import de.mtbnews.android.SubscriptionForenActivity;
import de.mtbnews.android.SubscriptionTopicsActivity;
import de.mtbnews.android.TopicActivity;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.tapatalk.wrapper.ListHolder;
import de.mtbnews.android.tapatalk.wrapper.Mailbox;
import de.mtbnews.android.tapatalk.wrapper.Topic;

/**
 * @author dankert
 * 
 */
public class SubscriptionService extends Service
{
	private IBCApplication ibcApp;
	private static Timer timer;
	private SharedPreferences prefs;

	private static final int NOTIFICATION_ERROR = 1;
	private static final int NOTIFICATION_TOPIC = 2;
	private static final int NOTIFICATION_FORUM = 3;
	private static final int NOTIFICATION_MESSAGES = 4;

	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	/**
	 * Erzeugt für diese Serviceinstanz einen Timer, der in regelmäßigen
	 * Abständen auf neue Themen und Nachrichten prüft.
	 * 
	 * @see android.app.Service#onCreate()
	 */
	public void onCreate()
	{
		Log.d(this.getClass().getSimpleName(), "Starting service");
		super.onCreate();
		ibcApp = (IBCApplication) getApplication();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Intervall in Minuten (Default = 3 Stunden)
		int intervalInMinutes = Integer.parseInt(prefs.getString(
				"subscription_service_interval", "180"));

		timer = new Timer();
		timer.scheduleAtFixedRate(new SubscriptionTask(), 0,
				intervalInMinutes * 60 * 1000);
	}

	/**
	 * Der Timer, der auf ungelesene Nachrichten und ungelesene Themen prüft.
	 * Falls gefunden, wird dies über den NotificationService gemeldet.
	 * 
	 * @author dankert
	 * 
	 */
	private class SubscriptionTask extends TimerTask
	{
		public void run()
		{
			Log.d(this.getClass().getSimpleName(),
					"now testing for unread topics and messages");

			final long actualTime = System.currentTimeMillis();
			final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			final TapatalkClient client = ibcApp.getTapatalkClient();

			if (!client.loggedIn)
			{
				try
				{
					client.login(prefs.getString("username", ""), prefs
							.getString("password", ""));
				}
				catch (TapatalkException e)
				{
					final Notification notification = new Notification(
							R.drawable.ibc_logo, e.getMessage(), actualTime);
					notification.setLatestEventInfo(getApplicationContext(),
							getResources().getString(R.string.login_failed),
							"", null);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					nm.notify(NOTIFICATION_ERROR, notification);

					return;
				}
			}

			if (!client.loggedIn)
			{
				final Notification notification = new Notification(
						R.drawable.ibc_logo, "", actualTime);
				notification.setLatestEventInfo(getApplicationContext(),
						getResources().getString(R.string.login_failed), "",
						null);
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				nm.notify(NOTIFICATION_ERROR, notification);

				return;
			}

			try
			{
				List<Forum> subscribedForum = client.getSubscribedForum(true);

				final List<String> forumNameList = new ArrayList<String>();
				for (Forum forum : subscribedForum)
				{
					forumNameList.add(forum.getTitle());
				}
				if (!forumNameList.isEmpty())
				{
					final Intent notificationIntent = new Intent(
							SubscriptionService.this,
							SubscriptionForenActivity.class);
					final PendingIntent contentIntent = PendingIntent
							.getActivity(SubscriptionService.this, 0,
									notificationIntent, 0);

					final String tickerText = getResources().getString(
							R.string.unread_forum)
							+ "\n" + TextUtils.join("\n", forumNameList);

					final Notification notification = new Notification(
							R.drawable.ibc_logo, tickerText, actualTime);
					notification.setLatestEventInfo(getApplicationContext(),
							getResources().getString(R.string.unread_forum)
									+ " (" + subscribedForum.size() + ")",
							TextUtils.join(", ", forumNameList), contentIntent);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					// notification.flags = Notification.FLAG_ONGOING_EVENT
					// | Notification.FLAG_NO_CLEAR;
					nm.notify(NOTIFICATION_FORUM, notification);
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
				ListHolder<Topic> subscribedTopic = client.getSubscribedTopics(
						0, 10, true);
				final List<String> topicNameList = new ArrayList<String>();
				for (Topic topic : subscribedTopic.getChildren())
				{
					topicNameList.add(topic.getTitle());
				}
				if (!topicNameList.isEmpty())
				{

					final Intent notificationIntent = new Intent(
							SubscriptionService.this,
							SubscriptionTopicsActivity.class);
					final PendingIntent contentIntent = PendingIntent
							.getActivity(SubscriptionService.this, 0,
									notificationIntent, 0);

					final String tickerText = getResources().getString(
							R.string.unread_topic)
							+ "\n" + TextUtils.join("\n", topicNameList);

					final Notification notification = new Notification(
							R.drawable.ibc_logo, tickerText, actualTime);
					final String title = getResources().getString(
							R.string.unread_topic)
							+ " (" + subscribedTopic.getChildren().size() + ")";
					final String content = TextUtils.join(", ", topicNameList);
					
					notification.setLatestEventInfo(getApplicationContext(),
							title, content, contentIntent);
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					// notification.flags = Notification.FLAG_ONGOING_EVENT
					// | Notification.FLAG_NO_CLEAR;
					nm.notify(NOTIFICATION_TOPIC, notification);
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
				List<Mailbox> mailboxList = client.getMailbox();
				int unreadCount = 0;

				final List<String> unreadBoxNames = new ArrayList<String>();

				for (Mailbox mailbox : mailboxList)
				{
					if (mailbox.countUnread > 0)
					{

						unreadBoxNames.add(mailbox.getTitle());
						unreadCount += mailbox.countUnread;
					}
				}

				if (unreadCount > 0)
				{
					final Intent notificationIntent = new Intent(
							SubscriptionService.this, MailboxActivity.class);
					final PendingIntent contentIntent = PendingIntent
							.getActivity(SubscriptionService.this, 0,
									notificationIntent, 0);

					final String tickerText = getResources().getString(
							R.string.unread_messages)
							+ "\n" + TextUtils.join("\n", unreadBoxNames);

					final Notification notification = new Notification(
							R.drawable.ibc_logo, tickerText, actualTime);
					notification.setLatestEventInfo(getApplicationContext(),
							getResources().getString(R.string.unread_messages)
									+ " (" + unreadCount + ")", TextUtils.join(
									", ", unreadBoxNames), contentIntent);

					notification.flags = Notification.FLAG_AUTO_CANCEL;
					// notification.flags = Notification.FLAG_ONGOING_EVENT
					// | Notification.FLAG_NO_CLEAR;
					nm.notify(NOTIFICATION_MESSAGES, notification);
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

	/**
	 * Wird vom System automatisch aufgerufen bevor der Service entfernt wird.
	 * Hier räumen wir vor allem den Timer weg, damit keine weiteren Ereignisse
	 * ausgelöst werden.
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	public void onDestroy()
	{

		timer.cancel(); // Alle Timer-Ereignisse stoppen.
		Log.d(this.getClass().getSimpleName(), "Destroying service");

		super.onDestroy();
	}

}
