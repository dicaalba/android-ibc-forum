/**
 * 
 */
package de.mtbnews.android;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
public class TopicActivity extends EndlessListActivity<Post>
{
	public static final String TOPIC_ID = "topic_id";

	private String forumId;
	private String topicId;
	private String topicTitle;

	private int totalSize;
	private TapatalkClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.listing);

		client = ((IBCApplication) getApplication()).getTapatalkClient();

		topicId = TopicActivity.this.getIntent().getStringExtra(TOPIC_ID);
		// forumId = TopicActivity.this.getIntent().getStringExtra("forum_id");

		ListAdapter adapter = new ListEntryContentAdapter(TopicActivity.this,
				entries, true, false);
		setListAdapter(adapter);

		initialLoad();

		final ListView list = getListView();

		/*
		 * list.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { int aktPosition = displayFrom + position +
		 * 1; Toast.makeText(TopicActivity.this, "" + aktPosition,
		 * Toast.LENGTH_SHORT).show();
		 * 
		 * // final Intent intent = new Intent(TopicActivity.this, //
		 * PostActivity.class); // intent.putExtra("itemid", position); //
		 * startActivity(intent); } });
		 */
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
			protected void callServer() throws TapatalkException
			{

				topic = client.getTopic(topicId, from, to);

				forumId = topic.forumId;
				topicTitle = topic.getTitle();
				totalSize = topic.getPostCount();
				this.posts = topic.getPosts();
			}

			protected void doOnSuccess()
			{
				TopicActivity.this.setTitle(topic.getTitle());
				onListLoadedListener.listLoaded(this.posts);
			}

		}.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		mi.inflate(R.menu.topic, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_preferences:
				startActivity(new Intent(this, Configuration.class));
				return true;
				
			case R.id.menu_top:

				getListView().setOnScrollListener(null);
				getListView().setSelection(0);
				getIntent().putExtra(FIRST_POST, true);
				initialLoad();
				return true;

			case R.id.menu_bottom:

				getListView().setOnScrollListener(null);
				getListView().setSelection(super.entries.size() - 1);
				getIntent().putExtra(LAST_POST, true);
				initialLoad();
				return true;

			case R.id.menu_reply:
				Intent intent = new Intent(this, ReplyPostActivity.class);
				intent.putExtra("topic_id", topicId);
				intent.putExtra("forum_id", forumId);
				intent.putExtra("subject", topicTitle);
				startActivity(intent);
				return true;

			case R.id.menu_subscribe:

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.subscribe_topic);
				builder.setItems(R.array.subscription_modes,
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									final int item)
							{
								new ServerAsyncTask(TopicActivity.this,
										R.string.subscribe_topic)
								{

									@Override
									protected void callServer()
											throws TapatalkException
									{
										client
												.subscribeTopic(topicId,
														item - 1);
									}

									@Override
									protected void doOnSuccess()
									{
										Toast.makeText(getApplicationContext(),
												R.string.subscription_saved,
												Toast.LENGTH_SHORT).show();
									}
								}.execute();
							}
						});
				builder.create().show();
				return true;

			case R.id.menu_mark_read:

				// TODO: In Tapatalk-API-Version 3 nicht verfügbar!
				new ServerAsyncTask(TopicActivity.this,
						R.string.mark_topic_read)
				{

					@Override
					protected void callServer() throws TapatalkException
					{
						client.markTopicAsRead(topicId);
					}
				}.execute();
				return true;

		}
		return false;
	}

}
