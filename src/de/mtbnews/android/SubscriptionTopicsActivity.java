/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
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
import de.mtbnews.android.tapatalk.wrapper.ListHolder;
import de.mtbnews.android.tapatalk.wrapper.Topic;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * Anzeige aller Beiträge eines Themas.
 * 
 * @author dankert
 * 
 */
public class SubscriptionTopicsActivity extends EndlessListActivity<Topic>
{
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
				final Intent intent = new Intent(SubscriptionTopicsActivity.this,
						TopicActivity.class);
				Topic topic = SubscriptionTopicsActivity.super.entries.get(position);
				intent.putExtra(TopicActivity.TOPIC_ID, topic.getId());
				startActivity(intent);
			}
		});


		Toast.makeText(this, R.string.hint_press_long, Toast.LENGTH_SHORT)
				.show();
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
						SubscriptionTopicsActivity.this, TopicActivity.class);
				intent.putExtra(TopicActivity.TOPIC_ID, super.entries.get(menuInfo.position)
						.getId());
				intent.putExtra(EndlessListActivity.FIRST_POST, true);
				startActivity(intent);
				return true;

			case R.id.menu_goto_bottom:

				final Intent intent2 = new Intent(
						SubscriptionTopicsActivity.this, TopicActivity.class);
				intent2.putExtra(TopicActivity.TOPIC_ID, super.entries.get(menuInfo.position)
						.getId());
				intent2.putExtra(EndlessListActivity.LAST_POST, true);
				startActivity(intent2);
				return true;
		}

		return super.onContextItemSelected(item);
	}
	@Override
	protected int getTotalSize()
	{
		return this.totalSize;
	}

	@Override
	protected void loadEntries(
			final OnListLoadedListener<Topic> onListLoadedListener,
			final int from, final int to, boolean firstLoad)
	{
		new ServerAsyncTask(SubscriptionTopicsActivity.this,
				firstLoad ? R.string.waitingfor_subscription_topics
						: R.string.waitingfor_loadmore)
		{
			private ListHolder<Topic> topicHolder;

			@Override
			protected void callServer() throws IOException
			{
				TapatalkClient client = ((IBCApplication) getApplication()).client;

				try
				{
					topicHolder = client.getSubscribedTopics(from, to,false);

					totalSize = topicHolder.totalCount;
				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				//SubscriptionTopicsActivity.this.setTitle(topicHolder.getTitle());
				onListLoadedListener.listLoaded(this.topicHolder.getChildren());
			}

		}.execute();
	}
}
