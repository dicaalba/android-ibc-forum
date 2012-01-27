/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
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
public class SearchActivity extends ListActivity
{
	public static final String ID = "id";
	public static final String CLIENT = "client";

	private boolean loadingMore = true;

	private int displayFrom;
	private int displayTo;
	private int postCount;

	private List<Topic> topics;
	private Search search;

	Map<String, String> data;
	private String query;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if	( ((IBCApplication)getApplication()).ibcTheme )
			setTheme(R.style.IBC);

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.listing);

		Intent intent = getIntent();

		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			query = intent.getStringExtra(SearchManager.QUERY);
		}

		Log.d("IBC", "Searching for '" + query + "'");

		new ServerAsyncTask(this, R.string.waitingfor_search)
		{

			@Override
			protected void callServer() throws IOException
			{
				TapatalkClient client = ((IBCApplication)getApplication()).getTapatalkClient();

				try
				{
					int start = 0;
					int end = Integer.parseInt(prefs
							.getString("num_load", "10")) - 1;

					displayFrom = start;
					displayTo = end;

					search = client.searchTopics(query, start, end, null);

					postCount = search.topicCount;
					topics = search.getTopics();

					loadingMore = false;
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				SearchActivity.this.setTitle(query);
				ListAdapter adapter = new ListEntryContentAdapter(
						SearchActivity.this, topics);
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
				final Intent intent = new Intent(SearchActivity.this,
						TopicActivity.class);
				Topic topic = topics.get(position);
				intent.putExtra("topic_id", topic.getId());
				startActivity(intent);
			}
		});

		/**
		 * Weitere List-Einträge automatisch nachladen.
		 */
		list.setOnScrollListener(new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{

				// what is the bottom iten that is visible
				int lastInScreen = firstVisibleItem + visibleItemCount;

				// is the bottom item visible & not loading more already ? Load
				// more !
				if ((lastInScreen == totalItemCount) && !(loadingMore)
						/*&& !(totalItemCount >= search.topicCount)*/)
				{
					loadingMore = true;
					new ServerAsyncTask(SearchActivity.this,
							R.string.waitingfor_loadmore)
					{
						@Override
						protected void callServer() throws IOException
						{
							TapatalkClient client = ((IBCApplication)getApplication()).client;

							try
							{
								int start = displayTo + 1;
								int end = start
										+ Integer.parseInt(prefs.getString(
												"num_load", "10")) - 1;
								displayTo = end;

								search = client.searchTopics(null, start, end,
										search.searchId);

								topics.addAll(search.getTopics());

								loadingMore = false;
							}
							catch (TapatalkException e)
							{
								throw new RuntimeException(e);
							}
						}

						protected void doOnSuccess()
						{
							((BaseAdapter) getListAdapter())
									.notifyDataSetChanged();
						}

					}.execute();

				}
			}
		});

	}
}
