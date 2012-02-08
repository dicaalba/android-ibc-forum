package de.mtbnews.android;

import org.mcsoxford.rss.RSSItem;

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
		final String html = item.getFullContent();

		Html.ImageGetter imageGetter = null;
		if (prefs.getBoolean("load_images", false))
			imageGetter = new URLImageParser(desc,this);

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


}
