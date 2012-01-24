/*
 * IBC-App for Android
 * 
 * Copyright (C) 2011 Jan Dankert
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.mtbnews.android;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import de.mtbnews.android.adapter.RSSContentAdapter;
import de.mtbnews.android.util.AppData;
import de.mtbnews.android.util.IBC;
import de.mtbnews.android.util.ServerAsyncTask;

/**
 * @author Jan Dankert
 */
public class IBCActivity extends ListActivity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);

		SharedPreferences globalPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Gast-Zugang ist ok, daher keine Meldung anzeigen...
		// if (globalPrefs.getString("username", "").equals("") )
		// {
		// // Noch kein Benutzer konfiguriert. Hinweis anzeigen!
		// final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setMessage(getResources().getString(R.string.noserver));
		// AlertDialog alert = builder.create();
		// alert.show();
		// }

		Button forumButton = (Button) findViewById(R.id.forum);
		forumButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(IBCActivity.this, ForumOverviewActivity.class));
			}
		});

		Button photoButton = (Button) findViewById(R.id.photo);
		photoButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(IBCActivity.this, PhotoActivity.class));
			}
		});

		reloadFeed();
	}

	/**
	 * 
	 */
	private void reloadFeed()
	{
		if (AppData.newsFeed != null)
		{
			// Nicht nochmal laden.
			// TODO: Reload-Funktion.
			return;
		}

		new ServerAsyncTask(this, R.string.waitingfor_news)
		{

			private RSSFeed feed;

			@Override
			protected void callServer() throws IOException
			{
				RSSReader reader = new RSSReader();
				try
				{
					feed = reader.load(IBC.IBC_NEWS_RSS_URL);
					AppData.newsFeed = feed;
				}
				catch (RSSReaderException e)
				{
					throw new ClientProtocolException(e);
				}
			}

			protected void doOnSuccess()
			{
				ListAdapter adapter = new RSSContentAdapter(IBCActivity.this,
						feed);
				IBCActivity.this.setTitle(feed.getTitle());
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
				final Intent intent = new Intent(IBCActivity.this,
						NewsDetailActivity.class);
				intent.putExtra("itemid", position);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.main, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_preferences:
				startActivity(new Intent(this, Configuration.class));
				return true;

		}
		return false;
	}
}