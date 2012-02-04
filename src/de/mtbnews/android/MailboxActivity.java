package de.mtbnews.android;

import java.io.IOException;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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

public class MailboxActivity extends ListActivity
{
	private List<Mailbox> mailboxList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					TapatalkClient client = ((IBCApplication) getApplication())
							.getTapatalkClient();
					mailboxList = client.getMailbox();

				}
				catch (TapatalkException e)
				{
					throw new RuntimeException(e);
				}
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
}
