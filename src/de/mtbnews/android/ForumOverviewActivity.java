/**
 * 
 */
package de.mtbnews.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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

/**
 * @author dankert
 * 
 */
public class ForumOverviewActivity extends ExpandableListActivity
{
	private SharedPreferences prefs;
	private List<Forum> forumList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (((IBCApplication) getApplication()).ibcTheme)
			setTheme(R.style.IBC);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.exp_listing);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!((IBCApplication) getApplication()).getTapatalkClient().loggedIn
				&& prefs.getBoolean("auto_login", false))
			login();

		loadForum();
	}

	private void login()
	{
		final TapatalkClient client = ((IBCApplication) getApplication()).client;
		new ServerAsyncTask(this, R.string.waitingfor_login)
		{

			@Override
			protected void callServer() throws IOException
			{

				// add 2 to 4

				try
				{
					Map<String, Object> map = client.login(prefs.getString(
							"username", ""), prefs.getString("password", ""));

				}
				catch (TapatalkException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}

			@Override
			protected void doOnSuccess()
			{
				Toast.makeText(ForumOverviewActivity.this, R.string.login,
						Toast.LENGTH_SHORT).show();
			}

		}.executeSynchronized();
	}

	private void unterforenFlachkloppen()
	{
		for (Forum forum : this.forumList)
		{
			final List<Forum> newSubForen = new ArrayList<Forum>();

			// Wenn Forum Themen enthalten kann, dann mit aufnehmen in die 2. Hierarchie.
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
		final TapatalkClient client = ((IBCApplication) getApplication())
				.getTapatalkClient();

		new ServerAsyncTask(this, R.string.waitingfor_forum)
		{

			@Override
			protected void callServer() throws IOException
			{

				try
				{
					forumList = client.getAllForum();
					unterforenFlachkloppen();
				}
				catch (TapatalkException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
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

		// list.setOnGroupClickListener(new OnGroupClickListener()
		// {
		// @Override
		// public boolean onGroupClick(ExpandableListView parent, View v,
		// int groupPosition, long id)
		// {
		// Forum forum = forumList.get(groupPosition);
		//				
		// if (forum.subOnly)
		// {
		// Toast.makeText(ForumOverviewActivity.this,
		// R.string.sub_only, Toast.LENGTH_SHORT).show();
		// }
		// else
		// {
		// final Intent intent = new Intent(
		// ForumOverviewActivity.this, ForumActivity.class);
		// intent.putExtra("forum_id", forum.getId());
		// startActivity(intent);
		// }
		// return true;
		// }
		// });
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
				else
				{
					final Intent intent = new Intent(
							ForumOverviewActivity.this, ForumActivity.class);
					intent.putExtra("forum_id", forum.getId());
					startActivity(intent);
				}
				return true;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());

		TapatalkClient client = ((IBCApplication) getApplication())
				.getTapatalkClient();

		if (client.loggedIn)
			mi.inflate(R.menu.forum, menu);
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

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_mailbox:
				startActivity(new Intent(this, MailboxActivity.class));
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
					login();
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
		final TapatalkClient client = ((IBCApplication) getApplication()).client;

		new ServerAsyncTask(this, R.string.waitingfor_logout)
		{

			@Override
			protected synchronized void callServer() throws IOException
			{

				try
				{
					client.logout();
				}
				catch (TapatalkException e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}

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
