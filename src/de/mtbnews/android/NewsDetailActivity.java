package de.mtbnews.android;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFault;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.mtbnews.android.image.URLImageParser;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

public class NewsDetailActivity extends Activity
{
	protected SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.newsdetail);

		super.onCreate(savedInstanceState);

		final TextView datum = (TextView) findViewById(R.id.item_date);
		final TextView desc = (TextView) findViewById(R.id.item_description);
		final Button button = (Button) findViewById(R.id.item_button);

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

				datum.setText(DateFormat.getTimeFormat(NewsDetailActivity.this)
						.format(item.getPubDate()));

				// TextView name = (TextView) findViewById(R.id.item_title);
				// name.setText(item.getTitle());

				// if (e.getContent() != null)
				final String html = item.getFullContent();

				Html.ImageGetter imageGetter = null;
				if (prefs.getBoolean("load_images", false))
					imageGetter = new URLImageParser(desc,
							NewsDetailActivity.this);

				desc.setText(Html.fromHtml(html, imageGetter, null));

				setTitle(item.getTitle());

				button.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)

					{
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(item.getLink());
						startActivity(i);
					}
				});
			}
		}.execute();
	}

}
