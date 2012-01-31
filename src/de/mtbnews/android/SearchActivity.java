/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Search;
import de.mtbnews.android.tapatalk.wrapper.Topic;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class SearchActivity extends EndlessListActivity<Topic>
{
	public static final String ACTION_SEARCH_UNREAD_TOPICS = "de.mtbnews.android.UNREAD_TOPICS";
	public static final String ACTION_SEARCH_PARTICIPATED_TOPICS = "de.mtbnews.android.PARTICIPATED_TOPICS";
	public static final String ACTION_SEARCH_LATEST_TOPICS = "de.mtbnews.android.LATEST_TOPICS";
	private static final String ACTION_SEARCH_TOPICS_BY_QUERY = Intent.ACTION_SEARCH;

	private int totalSize;
	private String searchId;
	private SharedPreferences prefs;
	private String query;
	private int searchType;
	private String username;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listing);

		username = prefs.getString("username", "");

		final Intent intent = getIntent();

		if (ACTION_SEARCH_TOPICS_BY_QUERY.equals(intent.getAction()))
		{
			searchType = TapatalkClient.SEARCHTYPE_QUERY;
			query = intent.getStringExtra(SearchManager.QUERY);
		}
		else if (ACTION_SEARCH_LATEST_TOPICS.equals(intent.getAction()))
		{
			searchType = TapatalkClient.SEARCHTYPE_LATEST;
			query = null;
		}
		else if (ACTION_SEARCH_PARTICIPATED_TOPICS.equals(intent.getAction()))
		{
			searchType = TapatalkClient.SEARCHTYPE_PARTICIPATED;
			query = null;
		}
		else if (ACTION_SEARCH_UNREAD_TOPICS.equals(intent.getAction()))
		{
			searchType = TapatalkClient.SEARCHTYPE_UNREAD;
			query = null;
		}
		else
		{
			throw new RuntimeException("Unknown search action: "
					+ intent.getAction());
		}

		ListAdapter adapter = new ListEntryContentAdapter(SearchActivity.this,
				entries);
		setListAdapter(adapter);

		initialLoad();

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
				final Intent intent = new Intent(SearchActivity.this,
						TopicActivity.class);
				Topic topic = SearchActivity.super.entries.get(position);
				intent.putExtra(TopicActivity.TOPIC_ID, topic.getId());
				startActivity(intent);
			}
		});
	}

	@Override
	protected int getTotalSize()
	{
		return totalSize;
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

				final Intent intent = new Intent(SearchActivity.this,
						TopicActivity.class);
				intent.putExtra(TopicActivity.TOPIC_ID, super.entries
						.get(menuInfo.position).getId());
				intent.putExtra("first_post", true);
				startActivity(intent);
				return true;

			case R.id.menu_goto_bottom:

				final Intent intent2 = new Intent(SearchActivity.this,
						TopicActivity.class);
				intent2.putExtra(TopicActivity.TOPIC_ID, super.entries.get(
						menuInfo.position).getId());
				intent2.putExtra("last_post", true);
				startActivity(intent2);
				return true;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void loadEntries(final OnListLoadedListener<Topic> onListLoaded,
			final int from, final int to, boolean firstLoad)
	{
		new ServerAsyncTask(SearchActivity.this, R.string.waitingfor_loadmore)
		{
			private Search search;

			@Override
			protected void callServer() throws IOException
			{
				TapatalkClient client = ((IBCApplication) getApplication()).client;

				try
				{
					search = client.searchTopics(searchType, query, username,
							from, to, searchId);

					totalSize = search.topicCount;
					searchId = search.searchId;
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				onListLoaded.listLoaded(search.getTopics());
			}

		}.execute();
	}
}
