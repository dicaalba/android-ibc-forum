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
import org.mcsoxford.rss.RSSFault;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import de.mtbnews.android.adapter.ListEntryContentAdapter;
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
		setTheme(((IBCApplication) getApplication()).themeResId);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);

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
				startActivity(new Intent(IBCActivity.this,
						ForumOverviewActivity.class));
			}
		});

		// Button photoButton = (Button) findViewById(R.id.photo);
		// photoButton.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public void onClick(View v)
		// {
		// startActivity(new Intent(IBCActivity.this, PhotoActivity.class));
		// }
		// });

		reloadFeed();
	}

	/**
	 * 
	 */
	private void reloadFeed()
	{
		if (((IBCApplication) getApplication()).newsFeed != null)
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
					((IBCApplication) getApplication()).newsFeed = feed;
				}
				catch (RSSReaderException e)
				{
					throw new ClientProtocolException(e);
				}
				catch (RSSFault e)
				{
					throw new ClientProtocolException(e);
				}
			}

			protected void doOnSuccess()
			{
				IBCActivity.this.setTitle(feed.getTitle());

				ListAdapter adapter = new ListEntryContentAdapter(
						IBCActivity.this, this.feed.getItems());
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
			case R.id.menu_photo:
				startActivity(new Intent(IBCActivity.this, PhotoActivity.class));
				return true;
			case R.id.menu_mailbox:
				startActivity(new Intent(IBCActivity.this,
						MailboxActivity.class));
				return true;

			case R.id.www_bikemarkt:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://bikemarkt.mtb-news.de/bikemarkt/")));
				return true;
			case R.id.www_biketest:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.mtb-news.de/biketest/")));
				return true;
			case R.id.www_blog:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://schaltwerk.mtb-news.de/")));
				return true;
			case R.id.www_forum:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.mtb-news.de/forum/")));
				return true;
			case R.id.www_fotos:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://fotos.mtb-news.de/")));
				return true;
			case R.id.www_gewichte:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://gewichte.mtb-news.de/")));
				return true;
			case R.id.www_lmb:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.mtb-news.de/lmb/")));
				return true;
			case R.id.www_news:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.mtb-news.de/")));
				return true;
			case R.id.www_shop:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://shop.mtb-news.de/")));
				return true;
			case R.id.www_video:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://videos.mtb-news.de/")));
				return true;

		}
		return false;
	}
}