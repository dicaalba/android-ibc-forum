package de.mtbnews.android;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import de.mtbnews.android.NewsDetailActivity.ImageGetter;
import de.mtbnews.android.adapter.RSSContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.util.AppData;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

public class MessageActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.newsdetail);

		super.onCreate(savedInstanceState);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{
			private TapatalkClient client;

			@Override
			protected void callServer() throws IOException
			{
				client = AppData.getTapatalkClient();
			}

			protected void doOnSuccess()
			{
				// MessageActivity.this.setTitle(feed.getTitle());

				TextView datum = (TextView) findViewById(R.id.item_date);
				// datum.setText(DateFormat.getTimeFormat(this).format(item.getPubDate()));

				// TextView name = (TextView) findViewById(R.id.item_title);
				// name.setText(item.getTitle());

				final TextView desc = (TextView) findViewById(R.id.item_description);

				// if (e.getContent() != null)
				// final String html = item.getContent();
				// desc.setText(Html.fromHtml(html, new ImageGetter(), null));
				// setTitle(item.getTitle());

				Button button = (Button) findViewById(R.id.item_button);
				button.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)

					{
						Intent i = new Intent(Intent.ACTION_VIEW);
						// i.setData(item.getLink());
						startActivity(i);
					}
				});

			}
		}.execute();

	}
}
