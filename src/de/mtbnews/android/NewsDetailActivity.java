package de.mtbnews.android;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFault;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

public class NewsDetailActivity extends Activity
{
	protected SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.detail);

		super.onCreate(savedInstanceState);

		final WebView webView = (WebView) findViewById(R.id.webView);

		new ServerAsyncTask(this, R.string.waitingfor_news)
		{
			private RSSFeed feed;

			@Override
			protected void callServer() throws IOException
			{
				final RSSFeed oldFeed = ((IBCApplication) getApplication())
						.getNewsFeed();

				if (oldFeed != null)
				{
					feed = oldFeed;
				}
				else
				{

					RSSReader reader = new RSSReader();
					try
					{
						feed = reader.load(IBC.IBC_NEWS_RSS_URL);
						((IBCApplication) getApplication()).setNewsFeed(feed);
					}
					catch (RSSReaderException e)
					{
						throw new ClientProtocolException(e);
					}
					catch (RSSFault e)
					{
						throw new ClientProtocolException(e);
					}
				}
			}

			protected void doOnSuccess()
			{
				final RSSItem item = feed.getItems().get(
						getIntent().getIntExtra("itemid", 0));

//				final String title = DateFormat.getTimeFormat(NewsDetailActivity.this)
//				.format(item.getPubDate());
				
//				NewsDetailActivity.this.setTitle(title);
				

				final String html = item.getFullContent();
				
				webView.getSettings().setLoadsImagesAutomatically(prefs.getBoolean("load_images", false));
				webView.loadData(html,"text/html","UTF-16");


				setTitle(item.getTitle());

				// button.setOnClickListener(new OnClickListener()
				// {
				//
				// @Override
				// public void onClick(View v)
				//
				// {
				// Intent i = new Intent(Intent.ACTION_VIEW);
				// i.setData(item.getLink());
				// startActivity(i);
				// }
				// });
			}
		}.execute();
	}

}
