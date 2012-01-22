/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.MapContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.util.AppData;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class ForumActivity extends ListActivity
{
	private Object[] forumList;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (AppData.client == null)
			AppData.client = new TapatalkClient(IBC.IBC_FORUM_CONNECTOR_URL);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (prefs.getBoolean("auto_login", false))
		{
			login();
		}

		if (getIntent().getBooleanExtra("latest", false))
		{
			loadLatest();
		}
		else if (getIntent().getBooleanExtra("participated", false))
		{
			loadParticipated();
		}
		else if (getIntent().getBooleanExtra("unread", false))
		{
			loadUnread();
		}
		else
		{
			String forumId = getIntent().getStringExtra("forum_id");

			loadForum(forumId);
		}
	}

	private void login()
	{
		final TapatalkClient client = AppData.client;
		new ServerAsyncTask(this, R.string.waitingforlogin)
		{

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					Map<String, Object> map = client.login(prefs.getString(
							"username", ""), prefs.getString("password", ""));

				}
				catch (TapatalkException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

		}.execute();
	}

	private void loadUnread()
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final XMLRPCClient client = AppData.client.getXMLRPCClient();

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{
			@Override
			protected void callServer() throws IOException
			{

				// add 2 to 4
				Object[] params = new Object[] {
						prefs.getString("username", "").getBytes(),
						prefs.getString("password", "").getBytes() };

				try
				{
					Object sum = client.callEx("login", params);

					// Object l = client.call("get_inbox_stat");
					// System.out.println(l.toString() );
					Object l = client.call("get_unread_topic");

					Object k = ((Map) l).get("topics");
					forumList = (Object[]) k;

					System.out.println(l.toString());

					// Object i = client.call("get_box_info");
					// System.out.println(i.toString() );

				}
				catch (XMLRPCException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (Object o : forumList)
				{
					list.add((Map) o);
				}
				ListAdapter adapter = new MapContentAdapter(ForumActivity.this,
						list, "post_time", "topic_title", "short_content");
				// IBCActivity.this.setTitle(feed.getTitle());
				setListAdapter(adapter);

			}

		}.execute();
		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				final Intent intent = new Intent(ForumActivity.this,
						TopicActivity.class);
				intent.putExtra("topic_id",
						(String) ((Map) forumList[position]).get("topic_id"));
				startActivity(intent);
			}
		});
	}

	private void loadLatest()
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final XMLRPCClient client = AppData.client.getXMLRPCClient();

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					// Object l = client.call("get_inbox_stat");
					// System.out.println(l.toString() );
					Object l = client.call("get_latest_topic");

					forumList = (Object[]) ((Map) l).get("topics");

					System.out.println(l.toString());

					// Object i = client.call("get_box_info");
					// System.out.println(i.toString() );

				}
				catch (XMLRPCException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (Object o : forumList)
				{
					list.add((Map) o);
				}
				ListAdapter adapter = new MapContentAdapter(ForumActivity.this,
						list, "post_time", "topic_title", "short_content");
				// IBCActivity.this.setTitle(feed.getTitle());
				setListAdapter(adapter);

			}

		}.execute();
		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				// final Intent intent = new Intent(ForumActivity.this,
				// NewsDetailActivity.class);
				// intent.putExtra("itemid", position);
				// startActivity(intent);
			}
		});
	}

	private void loadParticipated()
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final XMLRPCClient client = AppData.client.getXMLRPCClient();

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private Object[] forumList;

			@Override
			protected void callServer() throws IOException
			{

				Object[] params = new Object[] { prefs
						.getString("username", "").getBytes() };

				try
				{
					Map map = (Map) client.call("get_participated_topic");

					this.forumList = (Object[]) map.get("topics");
				}
				catch (XMLRPCException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (Object o : forumList)
				{
					list.add((Map) o);
				}
				ListAdapter adapter = new MapContentAdapter(ForumActivity.this,
						list, null, "topic_title", "short_content");
				// IBCActivity.this.setTitle(feed.getTitle());
				setListAdapter(adapter);

			}

		}.execute();
		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				// final Intent intent = new Intent(ForumActivity.this,
				// NewsDetailActivity.class);
				// intent.putExtra("itemid", position);
				// startActivity(intent);
			}
		});
	}

	private void loadForum(final String forumId)
	{

		final XMLRPCClient client = AppData.client.getXMLRPCClient();

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private Object[] forumList;

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					Map map = (Map) client.call("get_topic", forumId);

					this.forumList = (Object[]) map.get("topics");
				}
				catch (XMLRPCException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (Object o : forumList)
				{
					list.add((Map) o);
				}
				ListAdapter adapter = new MapContentAdapter(ForumActivity.this,
						list, "last_reply_time", "topic_title", "short_content");
				// IBCActivity.this.setTitle(feed.getTitle());
				setListAdapter(adapter);

			}

		}.execute();
		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				// final Intent intent = new Intent(ForumActivity.this,
				// NewsDetailActivity.class);
				// intent.putExtra("itemid", position);
				// startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.forum, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_mailbox:
				startActivity(new Intent(this, MailboxActivity.class));
				return true;

			case R.id.menu_participated_topics:
				Intent intent = new Intent(this, ForumActivity.class);
				intent.putExtra("participated", true);
				startActivity(intent);
				return true;

			case R.id.menu_latest_topics:
				Intent intent2 = new Intent(this, ForumActivity.class);
				intent2.putExtra("latest", true);
				startActivity(intent2);
				return true;
			case R.id.menu_unread_topics:
				Intent intent3 = new Intent(this, ForumActivity.class);
				intent3.putExtra("unread", true);
				startActivity(intent3);
				return true;

			case R.id.menu_logout:
				return false;

			case R.id.menu_login:

				if (TextUtils.isEmpty(prefs.getString("username", "")))
				{
					Intent intent4 = new Intent(this, Configuration.class);
					startActivity(intent4);
				}
				// Evtl. gibt es jetzt einen Benutzernamen ...

				if (!TextUtils.isEmpty(prefs.getString("username", "")))
				{
					Intent intent4 = new Intent(this, Configuration.class);
					startActivity(intent4);
				}

				login();

				return true;
		}
		return false;
	}
}
