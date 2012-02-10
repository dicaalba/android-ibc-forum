/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.util.ServerAsyncTask;
import de.mtbnews.android.util.Utils;

/**
 * @author dankert
 * 
 */
public class SubscriptionForenActivity extends ListActivity
{
	private SharedPreferences prefs;

	private final List<Forum> forumList = new ArrayList<Forum>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		ListAdapter adapter = new ListEntryContentAdapter(
				SubscriptionForenActivity.this, forumList);
		setListAdapter(adapter);

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
				final Intent intent = new Intent(
						SubscriptionForenActivity.this, ForumActivity.class);
				intent.putExtra(ForumActivity.FORUM_ID, forumList.get(position)
						.getId());
				startActivity(intent);
			}
		});

		loadForumList();
	}

	private void loadForumList()
	{
		new ServerAsyncTask(this, R.string.waitingfor_subscription_forums)
		{

			private List<Forum> newForumList;

			@Override
			protected void callServer() throws IOException, TapatalkException
			{
				TapatalkClient client = ((IBCApplication) getApplication()).getTapatalkClient();

				// Login.
				if (Utils.loginExceeded(client))
					client.login(prefs.getString("username", ""), prefs
							.getString("password", ""));

				newForumList = client.getSubscribedForum(false);
			}

			protected void doOnSuccess()
			{
				// Die Referenz der Liste darf nicht geändert werden, da der
				// ListAdapter mit der Instanz verknüpft ist!
				forumList.clear();
				forumList.addAll(newForumList);

				((BaseAdapter) SubscriptionForenActivity.this.getListAdapter())
						.notifyDataSetChanged();
			}

		}.executeSynchronized();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId())
		{
			case R.id.menu_goto_top:

				final Intent intent = new Intent(
						SubscriptionForenActivity.this, ForumActivity.class);
				intent.putExtra("forum_id", forumList.get(menuInfo.position)
						.getId());
				intent.putExtra("first_post", true);
				startActivity(intent);
				return true;

			case R.id.menu_goto_bottom:

				final Intent intent2 = new Intent(
						SubscriptionForenActivity.this, ForumActivity.class);
				intent2.putExtra("forum_id", forumList.get(menuInfo.position)
						.getId());
				intent2.putExtra("last_post", true);
				startActivity(intent2);
				return true;
		}

		return super.onContextItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		if (((IBCApplication) getApplication()).getTapatalkClient().loggedIn)
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

		}
		return false;
	}

}
