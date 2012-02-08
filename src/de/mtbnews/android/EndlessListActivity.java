/**
 * 
 */
package de.mtbnews.android;

import java.util.ArrayList;
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
	/**
	 * boolean-Parameter der ansagt, dass die Anzeige mit dem letzten Element
	 * beginnen soll.
	 */
	public static final String LAST_POST = "last_post";

	/**
	 * boolean-Parameter der ansagt, dass die Anzeige mit dem ersten Element
	 * beginnen soll.
	 */
	public static final String FIRST_POST = "first_post";

	private boolean loadingInProgress = true;

	protected int displayFrom;
	protected int displayTo;
	protected boolean autoScrollDown;

	/**
	 * Die in dieser ListActivity enthaltene Liste.<br>
	 * Die Referenz auf diese Liste darf sich nicht ändern, da der ListAdapter
	 * mit dieser Instanz verbunden ist. Durch 'final' wird das sicher gestellt.
	 */
	final protected List<T> entries = new ArrayList<T>();

	private SharedPreferences prefs;

	private boolean firstLoad;

	/**
	 * Absolute Anzahl aller verfügbaren Elemente.
	 * 
	 * @return Anzahl
	 */
	protected abstract int getTotalSize();

	/**
	 * Initiales Laden der ersten Listeneinträge.
	 */
	protected void initialLoad()
	{
		firstLoad = true;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final int numLoad = Integer.parseInt(prefs.getString("num_load", "10"));

		if (getIntent().getBooleanExtra(FIRST_POST, false))
			autoScrollDown = false;
		else if (getIntent().getBooleanExtra(LAST_POST, false))
			autoScrollDown = true;
		else
			autoScrollDown = prefs.getBoolean("scroll_down", false);

		if (autoScrollDown)
		{
			loadEntries(new OnListLoadedListener<T>()
			{

				@Override
				public void listLoaded(List<T> list)
				{
					final int end = getTotalSize();
					final int start = Math.max(0, end - numLoad);

					loadEntries(new OnListLoadedListener<T>()
					{

						@Override
						public void listLoaded(List<T> list)
						{
							entries.clear();
							entries.addAll(list);
							((BaseAdapter) getListAdapter())
									.notifyDataSetChanged();

							displayFrom = start;
							displayTo = end;

							getListView().setSelection(entries.size() - 1);

							setOnScrollListener();

							loadingInProgress = false;
						}
					}, start, end);

				}
			}, 0, 1);

		}
		else
		{
			final int start = 0;
			final int end = numLoad - 1;

			loadEntries(new OnListLoadedListener<T>()
			{

				@Override
				public void listLoaded(List<T> list)
				{
					entries.clear();
					entries.addAll(list);
					((BaseAdapter) getListAdapter()).notifyDataSetChanged();

					displayFrom = start;
					displayTo = start + entries.size() - 1;

					setOnScrollListener();

					loadingInProgress = false;
				}
			}, start, end);
		}

	}

	/**
	 * @param from
	 * @param to
	 * @param firstLoad
	 * @return gelandene Elemente
	 */
	abstract protected void loadEntries(OnListLoadedListener<T> onListLoaded,
			final int from, final int to, boolean firstLoad);

	/**
	 * @param onListLoaded
	 * @param from
	 * @param to
	 * @param firstLoad
	 * @return gelandene Elemente
	 */
	private void loadEntries(OnListLoadedListener<T> onListLoaded, int from,
			int to)
	{
		loadEntries(onListLoaded, from, to, firstLoad);
		firstLoad = false;
	}

	private void setOnScrollListener()
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
			public void onScroll(AbsListView view, final int firstVisibleItem,
					final int visibleItemCount, final int totalItemCount)
			{

				// Letztes Item, dass angezeigt wird.
				int lastInScreen = firstVisibleItem + visibleItemCount;

				if (autoScrollDown)
				{
					// Sind wir am oberen Rand der Liste und die Liste ist noch
					// nicht vollständig geladen?
					if (firstVisibleItem == 0 && displayFrom > 0
							&& !loadingInProgress)
					{
						loadingInProgress = true;

						int start = Math.max(0, displayFrom
								- Integer.parseInt(prefs.getString("num_load",
										"10")));

						int end = displayFrom - 1;

						loadEntries(new OnListLoadedListener<T>()
						{

							@Override
							public void listLoaded(List<T> list)
							{
								int loadedSize = list.size();
								displayFrom -= loadedSize;

								list.addAll(entries);
								entries.clear();
								entries.addAll(list);

								((BaseAdapter) getListAdapter())
										.notifyDataSetChanged();

								// Zur gleichen Position springen (die jetzt
								// aber etwas weiter nach hinten verschoben
								// ist).
								getListView().setSelection(
										firstVisibleItem + loadedSize);

								loadingInProgress = false;
							}
						}, start, end);

					}
				}
				else
				{
					// Sind wir am unterenRand der Liste?
					if ((lastInScreen == totalItemCount)
							&& (totalItemCount < getTotalSize())
							&& !(loadingInProgress))
					{
						loadingInProgress = true;

						int start = displayTo + 1;
						int end = start
								+ Integer.parseInt(prefs.getString("num_load",
										"10")) - 1;

						loadEntries(new OnListLoadedListener<T>()
						{
							@Override
							public void listLoaded(List<T> list)
							{
								int loadedSize = list.size();
								entries.addAll(list);

								displayTo += loadedSize;

								((BaseAdapter) getListAdapter())
										.notifyDataSetChanged();

								loadingInProgress = false;
							}
						}, start, end);

					}
				}
			}
		});
	}

	interface OnListLoadedListener<T>
	{
		abstract void listLoaded(List<T> list);
	}
}
