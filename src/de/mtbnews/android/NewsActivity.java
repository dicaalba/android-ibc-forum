package de.mtbnews.android;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.RSSContentAdapter;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

public class NewsActivity extends ListActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		reloadFeed();

	}

	/**
	 * 
	 */
	private void reloadFeed()
	{
		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private RSSFeed feed;

			@Override
			protected void callServer() throws IOException
			{
				RSSReader reader = new RSSReader();
				try
				{
					feed = reader.load(IBC.IBC_NEWS_RSS_URL);
					((IBCApplication)getApplication()).newsFeed = feed;
				}
				catch (RSSReaderException e)
				{
					throw new ClientProtocolException(e);
				}
			}

			protected void doOnSuccess()
			{
				ListAdapter adapter = new RSSContentAdapter(NewsActivity.this,
						feed);
				NewsActivity.this.setTitle(feed.getTitle());
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
				final Intent intent = new Intent(NewsActivity.this,
						NewsDetailActivity.class);
				intent.putExtra("itemid", position);
				startActivity(intent);
			}
		});
	}
}
