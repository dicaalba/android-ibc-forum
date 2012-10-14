package de.mtbnews.android;

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
import android.widget.AdapterView.OnItemLongClickListener;
import de.mtbnews.android.adapter.ListEntryContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Mailbox;
import de.mtbnews.android.tapatalk.wrapper.Message;
import de.mtbnews.android.util.ServerAsyncTask;
import de.mtbnews.android.util.Utils;

public class MailActivity extends EndlessListActivity<Message>
{
	private int totalMessageCount;
	private String boxId;
	private SharedPreferences prefs;

	/**
	 * Diese Liste immer von oben beginnen.
	 * 
	 * @see de.mtbnews.android.EndlessListActivity#isAutoScrolldown()
	 */
	@Override
	protected boolean isAutoScrolldown()
	{
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setTheme(((IBCApplication) getApplication()).themeResId);
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boxId = getIntent().getStringExtra("box_id");

		ListAdapter adapter = new ListEntryContentAdapter(MailActivity.this, super.entries);
		setListAdapter(adapter);

		initialLoad();

		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent i = new Intent(MailActivity.this, MessageActivity.class);
				i.putExtra("box_id", boxId);
				i.putExtra("message_id", MailActivity.super.entries.get(position).id);
				startActivity(i);
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				Intent i = new Intent(MailActivity.this, ReplyMailActivity.class);
				i.putExtra("box_id", boxId);
				i.putExtra("message_id", MailActivity.super.entries.get(arg2).id);
				startActivity(i);
				return true;
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

	@Override
	protected int getTotalSize()
	{
		return totalMessageCount;
	}

	@Override
	protected void loadEntries(final OnListLoadedListener<Message> onListLoaded, final int from, final int to,
			boolean firstLoad)
	{

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private Mailbox mailbox;

			@Override
			protected void callServer() throws TapatalkException
			{
				TapatalkClient client = ((IBCApplication) getApplication()).getTapatalkClient();

				if (Utils.loginExceeded(client))
					client.login(prefs.getString("username", ""), prefs.getString("password", ""));

				mailbox = client.getBoxContent(boxId, from, to);

				totalMessageCount = mailbox.countAll;
			}

			protected void doOnSuccess()
			{
				// MailActivity.this.setTitle(mailbox.getName());
				onListLoaded.listLoaded(mailbox.messages);
			}

		}.execute();

	}
}
