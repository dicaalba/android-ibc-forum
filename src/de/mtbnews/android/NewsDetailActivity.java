package de.mtbnews.android;

import org.mcsoxford.rss.RSSItem;

import de.mtbnews.android.util.AppData;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NewsDetailActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.newsdetail);

		super.onCreate(savedInstanceState);

		final RSSItem item = AppData.newsFeed.getItems().get(
				getIntent().getIntExtra("itemid", 0));

		TextView datum = (TextView) findViewById(R.id.item_date);
		datum.setText(DateFormat.getTimeFormat(this).format(item.getPubDate()));

		// TextView name = (TextView) findViewById(R.id.item_title);
		// name.setText(item.getTitle());

		TextView desc = (TextView) findViewById(R.id.item_description);

		// if (e.getContent() != null)
		desc.setText(Html.fromHtml(item.getContent()));
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
