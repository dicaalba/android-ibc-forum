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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Jan Dankert
 */
public class IBCActivity extends Activity
{
	private static final String PREFS_NAME = "OR_BLOG_PREFS";
	private List<String> serverList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences globalPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		ArrayList<String> list = new ArrayList<String>();
		if (globalPrefs.getString("username", "").equals("") )
		{
			// Noch kein Benutzer konfiguriert. Hinweis anzeigen!
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getResources().getString(R.string.noserver));
			AlertDialog alert = builder.create();
			alert.show();
		}
		
		Button forumButton = (Button) findViewById(R.id.forum);
		forumButton.setOnClickListener( new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(IBCActivity.this, ForumActivity.class));
			}
		});
		
		Button newsButton = (Button) findViewById(R.id.news);
		newsButton.setOnClickListener( new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(IBCActivity.this, NewsActivity.class));
			}
		});
		
		Button photoButton = (Button) findViewById(R.id.photo);
		photoButton.setOnClickListener( new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(IBCActivity.this, PhotoActivity.class));
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

	@Override
	protected void onStop()
	{
		super.onStop();

		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		// editor.putBoolean("silentMode", mSilentMode);

		// Don't forget to commit your edits!!!
		editor.commit();
	}
}