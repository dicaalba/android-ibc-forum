/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.Map;

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
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.tapatalk.wrapper.Topic;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class ForumActivity extends EndlessListActivity<Topic>
{
	private SharedPreferences prefs;
	private int totalSize;
	private String forumId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		TapatalkClient client = ((IBCApplication) getApplication())
				.getTapatalkClient();

		if (!client.loggedIn && prefs.getBoolean("auto_login", false))
		{
			login();
		}

		forumId = getIntent().getStringExtra("forum_id");

		ListAdapter adapter = new ListEntryContentAdapter(ForumActivity.this,
				entries);
		setListAdapter(adapter);
		initialLoad();

		// TODO: ggf. das hier in die Oberklasse?
		ListView list = getListView();
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
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				// int aktPosition = displayFrom + position + 1;
				final Intent intent = new Intent(ForumActivity.this,
						TopicActivity.class);
				Topic topic = ForumActivity.super.entries.get(position);
				intent.putExtra(TopicActivity.TOPIC_ID, topic.getId());
				startActivity(intent);
			}
		});
	}

	// TODO: Das auch in die anderen Listviews einbauen.
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
				intent.putExtra("topic_id", super.entries
						.get(menuInfo.position).getId());
				intent.putExtra("first_post", true);
				startActivity(intent);
				return true;

			case R.id.menu_goto_bottom:

				final Intent intent2 = new Intent(ForumActivity.this,
						TopicActivity.class);
				intent2.putExtra("topic_id", super.entries.get(
						menuInfo.position).getId());
				intent2.putExtra("last_post", true);
				startActivity(intent2);
				return true;
		}

		return super.onContextItemSelected(item);
	}

	private void login()
	{
		final TapatalkClient client = ((IBCApplication) getApplication()).client;
		new ServerAsyncTask(this, R.string.waitingfor_login)
		{

			@Override
			protected synchronized void callServer() throws IOException
			{

				try
				{
					client.login(prefs.getString(
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
		final TapatalkClient client = ((IBCApplication) getApplication()).client;

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		if (((IBCApplication) getApplication()).client.loggedIn)
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
				
			case R.id.menu_create_topic:
				Intent intent5 = new Intent(this, CreateTopicActivity.class);
				intent5.putExtra("forum_id",forumId);
				startActivity(intent5);
				return true;

			case R.id.menu_participated_topics:
				Intent intent = new Intent(this, SearchActivity.class);
				intent
						.setAction(SearchActivity.ACTION_SEARCH_PARTICIPATED_TOPICS);
				startActivity(intent);
				return true;

			case R.id.menu_latest_topics:
				Intent intent2 = new Intent(this, SearchActivity.class);
				intent2.setAction(SearchActivity.ACTION_SEARCH_LATEST_TOPICS);
				startActivity(intent2);
				return true;
			case R.id.menu_unread_topics:
				Intent intent3 = new Intent(this, SearchActivity.class);
				intent3.setAction(SearchActivity.ACTION_SEARCH_UNREAD_TOPICS);
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

	@Override
	protected int getTotalSize()
	{
		return totalSize;
	}

	@Override
	protected void loadEntries(
			final de.mtbnews.android.EndlessListActivity.OnListLoadedListener<Topic> onListLoaded,
			final int from, final int to, boolean firstLoad)
	{

		new ServerAsyncTask(this, R.string.waitingfor_forum)
		{
			private Forum forum;

			@Override
			protected void callServer() throws IOException
			{

				TapatalkClient client = ((IBCApplication) getApplication()).client;
				try
				{
					this.forum = client.getForum(forumId, from, to);
					totalSize = this.forum.topicCount;
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				ForumActivity.this.setTitle(forum.getTitle());
				onListLoaded.listLoaded(this.forum.getTopics());
			}

		}.execute();

	}

}
