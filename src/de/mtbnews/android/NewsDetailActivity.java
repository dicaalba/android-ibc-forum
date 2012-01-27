package de.mtbnews.android;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.mcsoxford.rss.RSSItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NewsDetailActivity extends Activity
{
	protected SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.newsdetail);

		super.onCreate(savedInstanceState);

		final RSSItem item = ((IBCApplication)getApplication()).newsFeed.getItems().get(
				getIntent().getIntExtra("itemid", 0));

		TextView datum = (TextView) findViewById(R.id.item_date);
		datum.setText(DateFormat.getTimeFormat(this).format(item.getPubDate()));

		// TextView name = (TextView) findViewById(R.id.item_title);
		// name.setText(item.getTitle());

		final TextView desc = (TextView) findViewById(R.id.item_description);

		// if (e.getContent() != null)
		final String html = item.getContent();

		ImageGetter imageGetter = null;
		if (prefs.getBoolean("load_images", false))
			imageGetter = new ImageGetter();

		desc.setText(Html.fromHtml(html, imageGetter, null));

		setTitle(item.getTitle());

		Button button = (Button) findViewById(R.id.item_button);
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

	protected class ImageGetter implements Html.ImageGetter
	{

		public Drawable getDrawable(String source)
		{
			Drawable d = null;
			String imageSource;
			// if (!source.startsWith("http://www"))
			// {
			// imageSource = "http://www.minhembio.com" + source;
			// }
			// else
			// {
			imageSource = source;
			// }

			try
			{
				URL myFileUrl = new URL(imageSource);

				HttpURLConnection conn = (HttpURLConnection) myFileUrl
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				BitmapDrawable a = new BitmapDrawable(is);
				d = a.getCurrent();
				d
						.setBounds(0, 0, d.getIntrinsicWidth(), d
								.getIntrinsicHeight());
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
				// d = null;
			}

			return d;
		}
	};

}
