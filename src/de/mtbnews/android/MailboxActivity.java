package de.mtbnews.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mcsoxford.rss.RSSItem;
import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.MapContentAdapter;
import de.mtbnews.android.util.ServerAsyncTask;

public class MailboxActivity extends ListActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		setContentView(R.layout.listing);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{

			private Object[] forumList;

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					Object l = ((IBCApplication) getApplication()).client
							.getXMLRPCClient().call("get_box_info");

					this.forumList = (Object[]) ((Map) l).get("list");

				}
				catch (XMLRPCException e)
				{
					throw new RuntimeException(e);
				}
			}

			protected void doOnSuccess()
			{
				List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
				for (Object o : forumList)
				{
					list1.add((Map) o);
				}
				ListAdapter adapter = new MapContentAdapter(
						MailboxActivity.this, list1, null, "box_name", null);
				// IBCActivity.this.setTitle(feed.getTitle());
				setListAdapter(adapter);

			}

		}.execute();
		final ListView list = getListView();

		list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				// final Intent intent = new Intent(ForumActivity.this,
				// NewsDetailActivity.class);
				// intent.putExtra("itemid", position);
				// startActivity(intent);
			}
		});

		final ListView list2 = getListView();

		list2.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				RSSItem item = (RSSItem) getListAdapter().getItem(position);

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(item.getLink());
				startActivity(i);
			}
		});

	}
}
