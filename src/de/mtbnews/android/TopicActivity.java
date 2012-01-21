/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.MapContentAdapter;
import de.mtbnews.android.util.AppData;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author dankert
 * 
 */
public class TopicActivity extends ListActivity
{
	public static final String ID = "id";
	public static final String CLIENT = "client";

	private Object[] forumList;

	Map<String, String> data;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.listing);

		new ServerAsyncTask(this, R.string.waitingforcontent)
		{
			@Override
			protected void callServer() throws IOException
			{

				XMLRPCClient client = AppData.client;
				// add 2 to 4
				try
				{

					// add 2 to 4
					Object[] params = new Object[] { TopicActivity.this
							.getIntent().getStringExtra("topic_id") };
					Object l = client.call("get_thread",params[0]);

					forumList = (Object[]) ((Map) l).get("posts");

//					TopicActivity.this.setTitle(((Map) l).get("topic_title")
//							.toString());

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
				ListAdapter adapter = new MapContentAdapter(TopicActivity.this,
						list1, null, "post_title", "post_content");
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
				final Intent intent = new Intent(TopicActivity.this,
						PostActivity.class);
				intent.putExtra("itemid", position);
				startActivity(intent);
			}
		});

	}
}
