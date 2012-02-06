package de.mtbnews.android;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
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

public class MailActivity extends EndlessListActivity<Message>
{
	private int totalMessageCount;
	private String boxId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		boxId = getIntent().getStringExtra("box_id");

		ListAdapter adapter = new ListEntryContentAdapter(MailActivity.this,
				super.entries);
		setListAdapter(adapter);

		initialLoad();

		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent i = new Intent(MailActivity.this, MessageActivity.class);
				i.putExtra("box_id", boxId);
				i.putExtra("message_id", MailActivity.super.entries
						.get(position).id);
				startActivity(i);
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
			{
				Intent i = new Intent(MailActivity.this,
						ReplyMailActivity.class);
				i.putExtra("box_id", boxId);
				i.putExtra("message_id",
						MailActivity.super.entries.get(arg2).id);
				startActivity(i);
				return true;
			}
		});

	}

	@Override
	protected int getTotalSize()
	{
		return totalMessageCount;
	}

	@Override
	protected void loadEntries(
			final OnListLoadedListener<Message> onListLoaded, final int from,
			final int to, boolean firstLoad)
	{

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private Mailbox mailbox;

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					TapatalkClient client = ((IBCApplication) getApplication())
							.getTapatalkClient();
					mailbox = client.getBoxContent(boxId, from, to);

					totalMessageCount = mailbox.countAll;

				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				// MailActivity.this.setTitle(mailbox.getName());
				onListLoaded.listLoaded(mailbox.messages);
			}

		}.execute();

	}
}
