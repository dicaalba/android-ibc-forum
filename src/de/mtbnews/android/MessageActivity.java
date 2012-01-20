package de.mtbnews.android;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListAdapter;
import de.mtbnews.android.adapter.RSSContentAdapter;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

public class MessageActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

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
				}
				catch (RSSReaderException e)
				{
					throw new ClientProtocolException(e);
				}
			}

			protected void doOnSuccess()
			{
				ListAdapter adapter = new RSSContentAdapter(
						MessageActivity.this, feed);
				MessageActivity.this.setTitle(feed.getTitle());
				// setListAdapter(adapter);
			}
		}.execute();

	}
}
