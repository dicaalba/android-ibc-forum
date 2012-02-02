/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Post;
import de.mtbnews.android.tapatalk.wrapper.Topic;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * Anzeige aller Beiträge eines Themas.
 * 
 * @author dankert
 * 
 */
public class SubscriptionTopicsActivity extends EndlessListActivity<Post>
{
	public static final String TOPIC_ID = "topic_id";

	private int totalSize;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		ListAdapter adapter = new ListEntryContentAdapter(SubscriptionTopicsActivity.this,
				entries);
		setListAdapter(adapter);

		initialLoad();

		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				int aktPosition = displayFrom + position + 1;
				Toast.makeText(SubscriptionTopicsActivity.this, "" + aktPosition,
						Toast.LENGTH_SHORT).show();

				// final Intent intent = new Intent(TopicActivity.this,
				// PostActivity.class);
				// intent.putExtra("itemid", position);
				// startActivity(intent);
			}
		});

		Toast.makeText(this, R.string.hint_press_long, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected int getTotalSize()
	{
		return this.totalSize;
	}

	@Override
	protected void loadEntries(
			final OnListLoadedListener<Post> onListLoadedListener,
			final int from, final int to, boolean firstLoad)
	{
		new ServerAsyncTask(SubscriptionTopicsActivity.this,
				firstLoad ? R.string.waitingfor_topic
						: R.string.waitingfor_loadmore)
		{
			private List<Post> posts;
			private Topic topic;

			@Override
			protected void callServer() throws IOException
			{
				TapatalkClient client = ((IBCApplication) getApplication()).client;

				try
				{
					String topicId = SubscriptionTopicsActivity.this.getIntent()
							.getStringExtra(TOPIC_ID);

					topic = client.getTopic(topicId, from, to);

					totalSize = topic.getPostCount();
					this.posts = topic.getPosts();
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				SubscriptionTopicsActivity.this.setTitle(topic.getTitle());
				onListLoadedListener.listLoaded(this.posts);
			}

		}.execute();
	}
}
