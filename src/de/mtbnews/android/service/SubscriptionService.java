/**
 * 
 */
package de.mtbnews.android.service;

import java.util.ArrayList;
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
import android.text.TextUtils;
import android.util.Log;
import de.mtbnews.android.IBCActivity;
import de.mtbnews.android.IBCApplication;
import de.mtbnews.android.MailboxActivity;
import de.mtbnews.android.R;
import de.mtbnews.android.SubscriptionForenActivity;
import de.mtbnews.android.SubscriptionTopicsActivity;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.tapatalk.wrapper.ListHolder;
import de.mtbnews.android.tapatalk.wrapper.Mailbox;
import de.mtbnews.android.tapatalk.wrapper.Topic;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.Utils;

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

	private TapatalkClient client;

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
		Log.d(IBC.TAG, "Starting service");
		super.onCreate();
		ibcApp = (IBCApplication) getApplication();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		client = ibcApp.getTapatalkClient();

		// Intervall in Minuten (Default = 3 Stunden)
		int intervalInMinutes = Integer.parseInt(prefs.getString(
				"subscription_service_interval", "180"));

		Log.d(IBC.TAG, "Creating the timer");
		timer = new Timer();
		timer.scheduleAtFixedRate(new SubscriptionTask(), 2000,
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
			Log.d(IBC.TAG, "timer event fired");

			final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			try
			{
				client.login(prefs.getString("username", ""), prefs.getString(
						"password", ""));

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

					final Notification notification = createNotification(
							tickerText, R.string.unread_forum, "("
									+ subscribedForum.size() + ")", TextUtils
									.join(", ", forumNameList), contentIntent);
					nm.notify(NOTIFICATION_FORUM, notification);
				}
				
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

					final Notification notification = createNotification(
							tickerText, R.string.unread_topic, "("
									+ subscribedTopic.getChildren().size()
									+ ")", TextUtils.join(", ", topicNameList),
							contentIntent);

					nm.notify(NOTIFICATION_TOPIC, notification);
				}

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

					final Notification notification = createNotification(
							tickerText, R.string.unread_messages, "("
									+ unreadCount + ")", TextUtils.join(", ",
									unreadBoxNames), contentIntent);
					nm.notify(NOTIFICATION_MESSAGES, notification);
				}
			}
			catch (TapatalkException e)
			{
				// Kann vorkommen, wenn Login fehlschlägt oder Verbindung
				// abbricht
				final Intent notificationIntent = new Intent(
						SubscriptionService.this, IBCActivity.class);
				final PendingIntent contentIntent = PendingIntent.getActivity(
						SubscriptionService.this, 0, notificationIntent, 0);

				final Notification notification = createNotification(e
						.getMessage(), R.string.error, null, getResources()
						.getString(Utils.getResId(e.getErrorCode())),
						contentIntent);

				notification.flags = Notification.FLAG_AUTO_CANCEL;
				nm.notify(NOTIFICATION_ERROR, notification);
			}
			catch (Exception e)
			{
				// Das sollte eigentlich nicht vorkommen. Hier scheint etwas
				// schlimmeres kaputt zu sein, daher werfen wir hier eine
				// RuntimeException weiter. Android wird dann den Service
				// beenden und das Senden eines Berichtes ermöglichen. Das ist
				// das beste, was wir hier noch tun können ;) Würden wir den
				// Fehler nicht weiterwerfen, würde der Service eh nichts mehr
				// machen und niemandem wäre geholfen.
				Log.w(IBC.TAG, e);
				throw new RuntimeException("Unrecoverable error in service", e);
			}
		}

		@Override
		public boolean cancel()
		{
			Log.d(IBC.TAG, "Timer destroyed");
			return super.cancel();
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
		Log.d(IBC.TAG, "Destroying service");

		timer.cancel(); // Alle Timer-Ereignisse stoppen.

		super.onDestroy();
	}

	/**
	 * Notification erzeugen
	 * 
	 * @param tickerText
	 *            Mehrzeiliger Ticker-Text, der in der Notification-Bar
	 *            angezeigt wird.
	 * @param titleResId
	 *            Titel-Resource-Id der Notification
	 * @param titleExtra
	 *            Zusatz-Titeltext, kann <code>null</code> bleiben
	 * @param content
	 *            Inhaltstext der Noification
	 * @param intent
	 *            Auszulösender Intent
	 * @return
	 */
	private Notification createNotification(String tickerText, int titleResId,
			String titleExtra, String content, PendingIntent intent)
	{

		final Notification notification = new Notification(R.drawable.ibc_logo,
				tickerText, System.currentTimeMillis());
		notification
				.setLatestEventInfo(getApplicationContext(), getResources()
						.getString(titleResId)
						+ (titleExtra != null ? " " + titleExtra : ""),
						content, intent);

		notification.defaults = Notification.DEFAULT_LIGHTS
				| Notification.DEFAULT_SOUND;

		// Falls so konfiguriert, den Vibrationsalarm auslösen
		if (prefs.getBoolean("use_vibration", false))
			notification.defaults |= Notification.DEFAULT_VIBRATE;

		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONLY_ALERT_ONCE;

		return notification;
	}
}
