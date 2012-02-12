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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
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

/**
 * Hintergrund-Service, der ungelesene Nachrichten, Themen und Beiträge
 * ermittelt und im Erfolgsfall eine Notification erzeugt.
 * 
 * @author dankert
 * 
 */
public class SubscriptionService extends Service
{
	/**
	 * Timer, der das zeitgesteuerte Abholen von neuen Nachrichten steuert.
	 */
	// Damit der Timer nicht nur erzeugt, sondern auch gestoppt werden kann,
	// behalten wir hier eine Referenz.
	private Timer timer;

	/**
	 * App-Einstellungen.
	 */
	// Notwendig für die Benutzeranmeldung.
	private SharedPreferences prefs;

	// Notification-Kategorien:
	private static final int NOTIFICATION_TOPIC = 2;
	private static final int NOTIFICATION_FORUM = 3;
	private static final int NOTIFICATION_MESSAGES = 4;

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	public IBinder onBind(Intent arg0)
	{
		// Dieser Service läuft stets alleine und wird nicht gebunden.
		return null;
	}

	/**
	 * Service-Start. Erzeugt für diese Serviceinstanz einen Timer, der in
	 * regelmäßigen Abständen auf neue Themen und Nachrichten prüft.
	 * 
	 * @see android.app.Service#onCreate()
	 */
	public void onCreate()
	{
		Log.d(IBC.TAG, "Starting service");
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Intervall in Minuten (Default = 3 Stunden)
		int intervalInMinutes = Integer.parseInt(prefs.getString(
				"subscription_service_interval", "180"));

		// Prüfen, ob Service laufen soll und ein Benutzername vorhanden ist
		if (prefs.getBoolean("autostart_subscription_service", false)
				&& !TextUtils.isEmpty(prefs.getString("username", "")))
		{
			Log.d(IBC.TAG, "Creating the timer");
			timer = new Timer();
			timer.scheduleAtFixedRate(new SubscriptionTask(), 2000,
					intervalInMinutes * 60 * 1000);
		}
		else
		{
			// Service soll nicht laufen, also sofort wieder stoppen
			Log.d(IBC.TAG, "Stopping service");
			stopSelf();
		}

	}

	/**
	 * Der Timer, der auf ungelesene Nachrichten und ungelesene Themen prüft.
	 * Falls gefunden, wird dies über den NotificationService gemeldet. <em>Dies
	 * darf eine interne Klasse sein, denn solange der Timer besteht, muss auch
	 * der Service dazu laufen.</em>
	 * 
	 * @author dankert
	 * 
	 */
	private class SubscriptionTask extends TimerTask
	{
		public void run()
		{
			Log.d(IBC.TAG, "timer event fired");

			// Für diesen Timer-Event erzeugen wir einen eigene Instanz des
			// Tapatalk-Client. Die Laufzeit ist hier unkritisch, dafür belastet
			// der Client nicht den HEAP, da nach dem Timeevent der
			// Tapatalk-Client durch den GC weggeräum werden kann. Dieser
			// Hintergrundprozess wird dadurch deutlich weniger
			// speicherintensiv.
			final TapatalkClient client = new TapatalkClient(
					IBC.IBC_FORUM_CONNECTOR_URL);

			final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			try
			{
				// Zuerst Login
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
				Log.w(IBC.TAG, e);

				// Ganz bewusst wird hier keine Fehlermeldung erzeugt:
				// Oft kann es passieren, dass der Empfang schlecht wird und die
				// Verbindung einen Timeout bekommt. In diesem Fall möchten wir
				// den Benutzer aber nicht mit Fehlermeldungen nerven. Beim
				// nächsten Timerevent wird der Abruf sowieso wieder probiert.
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
			Log.d(IBC.TAG, "Timer canceled");
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

		if (timer != null)
			timer.cancel(); // Alle Timer-Ereignisse stoppen.

		super.onDestroy();
	}

	/**
	 * Notification erzeugen.
	 * 
	 * @param tickerText
	 *            Mehrzeiliger Ticker-Text, der in der Notification-Bar
	 *            angezeigt wird.
	 * @param titleResId
	 *            Titel-Resource-Id der Notification
	 * @param titleExtra
	 *            Zusatz-Titeltext, kann <code>null</code> sein
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

		notification.defaults = Notification.DEFAULT_LIGHTS;

		final String ringtone = prefs.getString("ringtone", "");

		if (!TextUtils.isEmpty(ringtone))
			notification.sound = Uri.parse(ringtone);
		else
			notification.defaults |= Notification.DEFAULT_SOUND;

		// Falls so konfiguriert, den Vibrationsalarm auslösen
		if (prefs.getBoolean("use_vibration", false))
			notification.defaults |= Notification.DEFAULT_VIBRATE;

		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONLY_ALERT_ONCE;

		return notification;
	}
}
