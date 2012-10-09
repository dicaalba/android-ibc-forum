package de.mtbnews.android;

import java.io.IOException;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
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
		super.onCreate(savedInstanceState);
		
		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.listing);

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
				final Mailbox mailbox = mailboxList.get(position);

				if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent()
						.getAction()))
				{

					Intent shortcutIntent = new Intent(MailboxActivity.this,
							MailActivity.class);
					// Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
					shortcutIntent.setClassName(MailboxActivity.this,
							MailActivity.class.getName());
					shortcutIntent.putExtra("box_id", mailbox.getId());

					// Then, set up the container intent (the response to the
					// caller)

					Intent intent = new Intent();
					intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
							shortcutIntent);
					intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mailbox
							.getTitle());
					Parcelable iconResource = Intent.ShortcutIconResource
							.fromContext(MailboxActivity.this,
									R.drawable.ibc_icon);
					intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
							iconResource);

					// Now, return the result to the launcher

					setResult(RESULT_OK, intent);
					finish();

				}
				else
				{

					Intent i = new Intent(MailboxActivity.this,
							MailActivity.class);
					i.putExtra("box_id", mailbox.getId());
					startActivity(i);
				}
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
