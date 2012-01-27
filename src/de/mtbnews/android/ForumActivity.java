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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.adapter.MapContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class ForumActivity extends ListActivity
{
	private Object[] forumList;
	private Forum forum;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if	( ((IBCApplication)getApplication()).ibcTheme )
			setTheme(R.style.IBC);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		TapatalkClient client = ((IBCApplication)getApplication()).getTapatalkClient();

		if (!client.loggedIn && prefs.getBoolean("auto_login", false))
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

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId())
		{
			case R.id.menu_goto_top:

				final Intent intent = new Intent(ForumActivity.this,
						TopicActivity.class);
				intent.putExtra("topic_id", forum.getTopics().get(
						menuInfo.position).getId());
				intent.putExtra("first_post", true);
				startActivity(intent);
				return true;

			case R.id.menu_goto_bottom:

				final Intent intent2 = new Intent(ForumActivity.this,
						TopicActivity.class);
				intent2.putExtra("topic_id", forum.getTopics().get(
						menuInfo.position).getId());
				intent2.putExtra("last_post", true);
				startActivity(intent2);
				return true;
		}

		return super.onContextItemSelected(item);
	}

	private void login()
	{
		final TapatalkClient client = ((IBCApplication)getApplication()).client;
		new ServerAsyncTask(this, R.string.waitingfor_login)
		{

			@Override
			protected synchronized void callServer() throws IOException
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

		}.executeSynchronized();
	}

	private void logout()
	{
		final TapatalkClient client = ((IBCApplication)getApplication()).client;

		new ServerAsyncTask(this, R.string.waitingfor_logout)
		{

			@Override
			protected synchronized void callServer() throws IOException
			{

				try
				{
					client.logout();

				}
				catch (TapatalkException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

		}.executeSynchronized();
	}

	private void loadUnread()
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final XMLRPCClient client = ((IBCApplication)getApplication()).client.getXMLRPCClient();

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
		final XMLRPCClient client = ((IBCApplication)getApplication()).client.getXMLRPCClient();

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

				final Intent intent = new Intent(ForumActivity.this,
						TopicActivity.class);
				intent.putExtra("topic_id",
						(String) ((Map) forumList[position]).get("topic_id"));
				startActivity(intent);

			}
		});
	}

	private void loadParticipated()
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final XMLRPCClient client = ((IBCApplication)getApplication()).client.getXMLRPCClient();

		new ServerAsyncTask(this, R.string.waitingfor_forum)
		{

			private Object[] forumList;

			@Override
			protected void callServer() throws IOException
			{

				Object[] params = new Object[] { prefs
						.getString("username", "").getBytes() };

				try
				{
					Map map = (Map) client.callEx("get_participated_topic",params);

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

				final Intent intent = new Intent(ForumActivity.this,
						TopicActivity.class);
				intent.putExtra("topic_id",
						(String) ((Map) forumList[position]).get("topic_id"));
				startActivity(intent);

			}
		});
	}

	private void loadForum(final String forumId)
	{

		final TapatalkClient client = ((IBCApplication)getApplication()).client;

		new ServerAsyncTask(this, R.string.waitingfor_forum)
		{

			// private Object[] forumList;

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					forum = client.getForum(forumId);
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
				ForumActivity.this.setTitle(forum.getTitle());

				ListAdapter adapter = new ListEntryContentAdapter(
						ForumActivity.this, forum.getTopics());
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
				intent.putExtra("topic_id", forum.getTopics().get(position)
						.getId());
				startActivity(intent);

			}
		});

		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo)
			{
				MenuInflater menuInflater = new MenuInflater(getApplication());
				menuInflater.inflate(R.menu.topic_context, menu);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		if (((IBCApplication)getApplication()).client.loggedIn)
			mi.inflate(R.menu.forum, menu);
		else
			mi.inflate(R.menu.forum_guest, menu);

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
				logout();
				return true;

			case R.id.menu_login:

				if (TextUtils.isEmpty(prefs.getString("username", "")))
				{
					Toast
							.makeText(this, R.string.nousername,
									Toast.LENGTH_LONG).show();

					Intent intent4 = new Intent(this, Configuration.class);
					startActivity(intent4);
				}
				// Evtl. gibt es jetzt einen Benutzernamen ...

				if (!TextUtils.isEmpty(prefs.getString("username", "")))
				{
					login();
				}
				else
				{
					Toast
							.makeText(this, R.string.nousername,
									Toast.LENGTH_LONG).show();
				}

				return true;
		}
		return false;
	}
}
