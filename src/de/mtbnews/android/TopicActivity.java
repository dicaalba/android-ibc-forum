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
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.adapter.MapContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.ListEntry;
import de.mtbnews.android.tapatalk.wrapper.Post;
import de.mtbnews.android.tapatalk.wrapper.Topic;
import de.mtbnews.android.util.AppData;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class TopicActivity extends ListActivity
{
	public static final String ID = "id";
	public static final String CLIENT = "client";

	private boolean loadingMore = true;

	private int displayFrom;
	private int displayTo;
	private int postCount;

	private List<Post> posts;
	private Topic topic;

	Map<String, String> data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		new ServerAsyncTask(this, R.string.waitingfor_topic)
		{

			@Override
			protected void callServer() throws IOException
			{
				TapatalkClient client = AppData.client;

				try
				{
					int start = 0;
					int end = Integer.parseInt(prefs
							.getString("num_load", "10")) - 1;

					displayFrom = start;
					displayTo = end;

					String topicId = TopicActivity.this.getIntent()
							.getStringExtra("topic_id");

					topic = client.getTopic(topicId, start, end);

					postCount = topic.getPostCount();
					posts = topic.getPosts();

					loadingMore = false;
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				TopicActivity.this.setTitle(topic.getTitle());
				List p = posts;
				ListAdapter adapter = new ListEntryContentAdapter(
						TopicActivity.this, p);
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
				final Intent intent = new Intent(TopicActivity.this,
						PostActivity.class);
				intent.putExtra("itemid", position);
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
				if ((lastInScreen == totalItemCount) && !(loadingMore))
				{
					loadingMore = true;
					new ServerAsyncTask(TopicActivity.this,
							R.string.waitingfor_loadmore)
					{
						@Override
						protected void callServer() throws IOException
						{
							TapatalkClient client = AppData.client;

							try
							{
								int start = displayTo + 1;
								int end = start
										+ Integer.parseInt(prefs.getString(
												"num_load", "10")) - 1;

								displayTo = end;

								String topicId = TopicActivity.this.getIntent()
										.getStringExtra("topic_id");

								Topic topic = client.getTopic(topicId, start,
										end);

								postCount = topic.getPostCount();
								posts.addAll(topic.getPosts());

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
