package de.mtbnews.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Message;
import de.mtbnews.android.util.ServerAsyncTask;
import de.mtbnews.android.util.Utils;

public class MessageActivity extends Activity
{
	private String boxId;
	private String messageId;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.newsdetail);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boxId = getIntent().getStringExtra("box_id");
		messageId = getIntent().getStringExtra("message_id");

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{
			private TapatalkClient client;
			private Message message;

			@Override
			protected void callServer() throws TapatalkException
			{
				client = ((IBCApplication) getApplication()).getTapatalkClient();

				if (Utils.loginExceeded(client))
					client.login(prefs.getString("username", ""), prefs.getString("password", ""));

				message = client.getMessage(boxId, messageId);
			}

			protected void doOnSuccess()
			{
				// MessageActivity.this.setTitle(feed.getTitle());

				MessageActivity.this.setTitle(message.getTitle());

				TextView datum = (TextView) findViewById(R.id.item_date);
				datum.setText(DateFormat.getTimeFormat(MessageActivity.this).format(message.getDate()));

				// TextView name = (TextView) findViewById(R.id.item_title);
				// name.setText(item.getTitle());

				final TextView desc = (TextView) findViewById(R.id.item_description);

				// if (e.getContent() != null)
				// final String html = item.getContent();
				desc.setText(message.getContent());
				// setTitle(item.getTitle());

				// Button button = (Button) findViewById(R.id.item_button);
				// button.setOnClickListener(new OnClickListener()
				// {
				//
				// @Override
				// public void onClick(View v)
				//
				// {
				// Intent i = new Intent(Intent.ACTION_VIEW);
				// // i.setData(item.getLink());
				// startActivity(i);
				// }
				// });

			}
		}.execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		mi.inflate(R.menu.message, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_reply:
				Intent intent = new Intent(this, ReplyMailActivity.class);
				intent.putExtra("message_id", messageId);
				intent.putExtra("box_id", boxId);
				startActivity(intent);
				return true;

			case R.id.menu_new_message:
				Intent intent2 = new Intent(this, ReplyMailActivity.class);
				startActivity(intent2);
				return true;
		}
		return false;
	}

}
