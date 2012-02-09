package de.mtbnews.android;

import java.io.IOException;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Mailbox;
import de.mtbnews.android.util.ServerAsyncTask;
import de.mtbnews.android.util.Utils;

public class MailboxActivity extends ListActivity
{
	private List<Mailbox> mailboxList;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws TapatalkException
			{
				TapatalkClient client = ((IBCApplication) getApplication())
						.getTapatalkClient();

				// Login.
				if (Utils.loginExceeded(client))
					client.login(prefs.getString("username", ""), prefs
							.getString("password", ""));

				mailboxList = client.getMailbox();

			}

			protected void doOnSuccess()
			{
				ListAdapter adapter = new ListEntryContentAdapter(
						MailboxActivity.this, mailboxList);
				// IBCActivity.this.setTitle(feed.getTitle());
				setListAdapter(adapter);
			}

		}.execute();

		final ListView list2 = getListView();

		list2.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent i = new Intent(MailboxActivity.this, MailActivity.class);
				i.putExtra("box_id", mailboxList.get(position).getId());
				startActivity(i);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		mi.inflate(R.menu.mailbox, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_new_message:
				Intent intent = new Intent(this, ReplyMailActivity.class);
				startActivity(intent);
				return true;
		}
		return false;
	}

}
