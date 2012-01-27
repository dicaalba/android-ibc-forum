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
 * Anzeige eines Themas.
 * 
 * @author dankert
 * 
 */
public class TopicActivity extends EndlessListActivity<Post>
{
	private int totalSize;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		ListAdapter adapter = new ListEntryContentAdapter(TopicActivity.this,
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
				Toast.makeText(TopicActivity.this,
						displayFrom + " - " + displayTo, Toast.LENGTH_SHORT)
						.show();

				// final Intent intent = new Intent(TopicActivity.this,
				// PostActivity.class);
				// intent.putExtra("itemid", position);
				// startActivity(intent);
			}
		});
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
		new ServerAsyncTask(TopicActivity.this,
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
					String topicId = TopicActivity.this.getIntent()
							.getStringExtra("topic_id");

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
				TopicActivity.this.setTitle(topic.getTitle());
				onListLoadedListener.listLoaded(this.posts);
			}

		}.execute();
	}
}
