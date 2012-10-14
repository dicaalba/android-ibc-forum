package de.mtbnews.android;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFault;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
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

/**
 * Anzeige des RSSFeed für neue Fotos.
 * 
 * @author dankert
 * 
 */
public class PhotoActivity extends ListActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private RSSFeed feed;

			@Override
			protected void callServer() throws IOException
			{
				final RSSReader reader = new RSSReader();
				try
				{
					feed = reader.load(IBC.IBC_FOTOS_RSS_URL);
				}
				catch (RSSReaderException e)
				{
					throw new ClientProtocolException("Feed not available", e);
				}
				catch (RSSFault e)
				{
					throw new ClientProtocolException("Feed not available", e);
				}
			}

			protected void doOnSuccess()
			{
				ListAdapter adapter = new RSSContentAdapter(PhotoActivity.this, feed);
				PhotoActivity.this.setTitle(feed.getTitle());

				setListAdapter(adapter);
			}
		}.execute();

		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				RSSItem item = (RSSItem) getListAdapter().getItem(position);

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(item.getLink());
				startActivity(i);

			}
		});

	}
}
