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
		super.onCreate(savedInstanceState);

		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.detail);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		final WebView webView = (WebView) findViewById(R.id.webView);

		new ServerAsyncTask(this, R.string.waitingfor_news)
		{
			private RSSFeed feed;

			@Override
			protected void callServer() throws IOException
			{
				final RSSFeed oldFeed = ((IBCApplication) getApplication()).getNewsFeed();

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
				final RSSItem item = feed.getItems().get(getIntent().getIntExtra("itemid", 0));

				final String html = item.getFullContent();

				webView.getSettings().setLoadsImagesAutomatically(prefs.getBoolean("load_images", false));
				webView.loadDataWithBaseURL(IBC.IBC_NEWS_RSS_URL, html, "text/html", "UTF-8", null);

				setTitle(item.getTitle());
			}
		}.execute();
	}

}
