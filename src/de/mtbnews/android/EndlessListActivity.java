/**
 * 
 */
package de.mtbnews.android;

import java.util.List;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Erweitert die Oberklasse {@link ListActivity} für "endlose" Listen, die
 * seitenweise vom Server geladen werden. Bei Erreichen des Endes wird
 * automatisch der nächste Bereich nachgeladen.
 * 
 * @author dankert
 * 
 */
public abstract class EndlessListActivity<T> extends ListActivity
{
	private boolean loadingMore = true;

	private int displayFrom;
	private int displayTo;

	private SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(this);

	/**
	 * Absolute Anzahl aller verfügbaren Elemente.
	 * 
	 * @return Anzahl
	 */
	protected abstract int getTotalSize();

	protected void initialLoad()
	{
		final int numLoad = Integer.parseInt(prefs.getString("num_load", "10"));
		final boolean autoScrollDown = prefs.getBoolean("scroll_down", false);

		if (autoScrollDown)
		{
			loadEntries(0, 1, true);

			int end = getTotalSize();
			int start = Math.max(0, end - numLoad);

			loadEntries(start, end, true);

			displayFrom = start;
			displayTo = end;
		}
		else
		{
			int start = 0;
			int end = numLoad - 1;

			List<T> entries = loadEntries(start, end, true);
			displayFrom = start;
			displayTo = start + entries.size() - 1;
		}

		((BaseAdapter) getListAdapter()).notifyDataSetChanged();
	}

	/**
	 * @param from
	 * @param to
	 * @param firstLoad
	 * @return gelandene Elemente
	 */
	abstract protected List<T> loadEntries(int from, int to, boolean firstLoad);

	protected void setOnScrollListener()
	{
		final ListView list = getListView();

		/**
		 * Weitere List-Einträge automatisch nachladen.
		 */
		list.setOnScrollListener(new OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
			}

			/**
			 * Callback method to be invoked when the list or grid has been
			 * scrolled. This will be called after the scroll has completed
			 * Parameters
			 * 
			 * @param view
			 *            The view whose scroll state is being reported
			 * @param firstVisibleItem
			 *            the index of the first visible cell (ignore if
			 *            visibleItemCount == 0)
			 * @param visibleItemCount
			 *            the number of visible cells
			 * @param totalItemCount
			 *            the number of items in the list adaptor {@inheritDoc}
			 * 
			 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView,
			 *      int, int, int)
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{

				// Letztes Item, dass angezeigt wird.
				int lastInScreen = firstVisibleItem + visibleItemCount;

				// is the bottom item visible & not loading more already ? Load
				// more !
				if ((lastInScreen == totalItemCount)
						&& (totalItemCount < getTotalSize()) && !(loadingMore))
				{
					loadingMore = true;

					int start = displayTo + 1;
					int end = start
							+ Integer.parseInt(prefs
									.getString("num_load", "10")) - 1;

					displayTo = end;

					loadEntries(start, end, false);

					loadingMore = false;
					((BaseAdapter) getListAdapter()).notifyDataSetChanged();

				}
			}
		});
	}
}
