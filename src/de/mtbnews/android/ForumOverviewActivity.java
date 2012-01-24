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

import android.app.ExpandableListActivity;
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
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
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
public class ForumOverviewActivity extends ListActivity
{
	private SharedPreferences prefs;

	private List<Map<String, Object>> list;

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

		loadForum();
	}

	private void login()
	{
		final TapatalkClient client = AppData.client;
		new ServerAsyncTask(this, R.string.waitingfor_login)
		{

			@Override
			protected void callServer() throws IOException
			{

				// add 2 to 4

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

	private void loadForum()
	{
		final XMLRPCClient client = AppData.client.getXMLRPCClient();

		new ServerAsyncTask(this, R.string.waitingfor_forum)
		{

			private Object[] forumList;

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					Object l = client.call("get_forum");

					this.forumList = (Object[]) l;
				}
				catch (XMLRPCException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			protected void doOnSuccess()
			{
				list = flattenArrayMap(this.forumList, "child");

				ListAdapter adapter = new MapContentAdapter(
						ForumOverviewActivity.this, list, null, "forum_name",
						"description");
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

				Map<String, Object> map = ForumOverviewActivity.this.list
						.get(position);

				boolean subOnly = (Boolean) map.get("sub_only");
				if (subOnly)
				{
					Toast.makeText(ForumOverviewActivity.this,
							R.string.sub_only, Toast.LENGTH_SHORT).show();
				}
				else
				{
					final Intent intent = new Intent(
							ForumOverviewActivity.this, ForumActivity.class);
					intent.putExtra("forum_id", (String) map.get("forum_id"));
					startActivity(intent);
				}
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

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested()
	{
		return super.onSearchRequested();
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
					Toast.makeText(this,R.string.nousername,Toast.LENGTH_LONG).show();
					
					Intent intent4 = new Intent(this, Configuration.class);
					startActivity(intent4);
				}
				// Evtl. gibt es jetzt einen Benutzernamen ...

				if (!TextUtils.isEmpty(prefs.getString("username", "")))
				{
					login();
				}
				else {
					Toast.makeText(this,R.string.nousername,Toast.LENGTH_LONG).show();
				}


				return true;
		}
		return false;
	}

	private List<Map<String, Object>> flattenArrayMap(Object[] objects,
			String childName)
	{

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (Object object : objects)
		{
			Map map = (Map) object;
			list.add(map);

			if (map.containsKey(childName))
			{
				list.addAll(flattenArrayMap((Object[]) map.get(childName),
						childName));
			}
		}
		return list;

	}
	
	
	private void logout()
	{
		final TapatalkClient client = AppData.client;
		
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


}
