/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import de.mtbnews.android.adapter.ExpandableForumContentAdapter;
import de.mtbnews.android.tapatalk.TapatalkClient;
import de.mtbnews.android.tapatalk.TapatalkException;
import de.mtbnews.android.tapatalk.wrapper.Forum;
import de.mtbnews.android.util.ServerAsyncTask;
import de.mtbnews.android.util.Utils;

/**
 * @author dankert
 * 
 */
public class ForumOverviewActivity extends ExpandableListActivity
{
	private SharedPreferences prefs;
	private List<Forum> forumList;
	private TapatalkClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.exp_listing);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		loadForum();
	}

	private void unterforenFlachkloppen()
	{
		for (Forum forum : this.forumList)
		{
			final List<Forum> newSubForen = new ArrayList<Forum>();

			// Wenn Forum Themen enthalten kann, dann mit aufnehmen in die 2.
			// Hierarchie.
			if (!forum.subOnly)
				newSubForen.add(forum);

			newSubForen.addAll(machFlach(forum.subForen));

			forum.subForen = newSubForen;
		}
	}

	private List<Forum> machFlach(List<Forum> subForen)
	{
		final List<Forum> newSubForen = new ArrayList<Forum>();
		if (subForen == null)
			return newSubForen;

		for (Forum subForum : subForen)
		{
			newSubForen.add(subForum);
			newSubForen.addAll(machFlach(subForum.subForen));
		}
		return newSubForen;
	}

	private void loadForum()
	{
		client = ((IBCApplication) getApplication())
				.getTapatalkClient();

		new ServerAsyncTask(this, R.string.waitingfor_forum)
		{

			@Override
			protected void callServer() throws TapatalkException
			{
				// Forumliste nur laden, wenn noch nicht vorhanden.
				if (forumList == null)
				{
					// Login.
					if (prefs.getBoolean("auto_login", false))
						if (Utils.loginExceeded(client))
							client.login(prefs.getString("username", ""), prefs
									.getString("password", ""));

					forumList = client.getAllForum();
					unterforenFlachkloppen();

				}
			}

			protected void doOnSuccess()
			{
				ExpandableListAdapter adapter = new ExpandableForumContentAdapter(
						ForumOverviewActivity.this, forumList);
				setListAdapter(adapter);
			}

		}.executeSynchronized();

		final ExpandableListView list = getExpandableListView();

		list.setOnChildClickListener(new OnChildClickListener()
		{

			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1,
					int groupPosition, int childPosition, long rowId)
			{
				Forum forum = forumList.get(groupPosition).subForen
						.get(childPosition);
				if (forum.subOnly)
				{
					Toast.makeText(ForumOverviewActivity.this,
							R.string.sub_only, Toast.LENGTH_SHORT).show();
				}
				else if (!TextUtils.isEmpty(forum.url))
				{
			        if (!Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse(forum.url)));
			        }
				}
				else
				{
			        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {

			        	Intent shortcutIntent = new Intent(
								ForumOverviewActivity.this, ForumActivity.class);
//			            Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
//			            shortcutIntent.setClassName(ForumActivity.class.getPackage().getName(), "." + ForumActivity.class.getSimpleName());
//			            shortcutIntent.setClassName(ForumActivity.class.getPackage().getName(), ForumActivity.class.getName());
			            shortcutIntent.putExtra("forum_id", forum.getId());

			            // Then, set up the container intent (the response to the caller)

			            Intent intent = new Intent();
			            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, forum.getTitle());
			            Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
			                    ForumOverviewActivity.this,  R.drawable.ibc_icon);
			            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

			            // Now, return the result to the launcher

			            setResult(RESULT_OK, intent);
			            finish();

			        } else {
						final Intent intent = new Intent(
								ForumOverviewActivity.this, ForumActivity.class);
						intent.putExtra("forum_id", forum.getId());
						startActivity(intent);
			        }
				}
				return true;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// see #onPrepareOptionsMenu

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		menu.clear();
		MenuInflater mi = new MenuInflater(getApplication());

		if (client.loggedIn)
			mi.inflate(R.menu.forumoverview, menu);
		else
			mi.inflate(R.menu.forum_guest, menu);

		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested()
	{
		return super.onSearchRequested();
	}

	/**
	 * {@inheritDoc}
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_mailbox:
				startActivity(new Intent(this, MailboxActivity.class));
				return true;

			case R.id.menu_search:
				onSearchRequested();
				return true;

			case R.id.menu_subscribed_forums:
				startActivity(new Intent(this, SubscriptionForenActivity.class));
				return true;

			case R.id.menu_subscribed_topics:
				startActivity(new Intent(this, SubscriptionTopicsActivity.class));
				return true;

			case R.id.menu_participated_topics:
				Intent intent = new Intent(this, SearchActivity.class);
				intent
						.setAction(SearchActivity.ACTION_SEARCH_PARTICIPATED_TOPICS);
				startActivity(intent);
				return true;

			case R.id.menu_latest_topics:
				Intent intent2 = new Intent(this, SearchActivity.class);
				intent2.setAction(SearchActivity.ACTION_SEARCH_LATEST_TOPICS);
				startActivity(intent2);
				return true;

			case R.id.menu_unread_topics:
				Intent intent3 = new Intent(this, SearchActivity.class);
				intent3.setAction(SearchActivity.ACTION_SEARCH_UNREAD_TOPICS);
				startActivity(intent3);
				return true;

			case R.id.menu_logout:
				logout();
				
				// Forum-Übersicht neu laden.
				forumList = null;
				loadForum();
				return true;

			case R.id.menu_login:

				if (TextUtils.isEmpty(prefs.getString("username", "")))
				{
					Toast
							.makeText(this, R.string.nousername,
									Toast.LENGTH_LONG).show();

					Intent intent4 = new Intent(this, Configuration.class);
					startActivity(intent4);
				}
				// Evtl. gibt es jetzt einen Benutzernamen ...

				if (!TextUtils.isEmpty(prefs.getString("username", "")))
				{
					new ServerAsyncTask(this, R.string.waitingfor_login)
					{

						@Override
						protected void callServer() throws IOException,
								TapatalkException
						{
							client.login(prefs.getString("username", ""), prefs
									.getString("password", ""));

						}

						@Override
						protected void doOnSuccess()
						{
							Log.d("IBC", "login success");
							Toast.makeText(ForumOverviewActivity.this,
									R.string.login_success, Toast.LENGTH_SHORT)
									.show();
							forumList = null;
							loadForum();
						}

					}.executeSynchronized();

				}
				else
				{
					Toast
							.makeText(this, R.string.nousername,
									Toast.LENGTH_LONG).show();
				}

				return true;
		}
		return false;
	}

	private void logout()
	{
		new ServerAsyncTask(this, R.string.waitingfor_logout)
		{

			@Override
			protected synchronized void callServer() throws IOException,
					TapatalkException
			{
				client.logout();
			}

			@Override
			protected void doOnSuccess()
			{
				Toast.makeText(ForumOverviewActivity.this, R.string.logout,
						Toast.LENGTH_SHORT).show();
			}

		}.executeSynchronized();
	}

}
