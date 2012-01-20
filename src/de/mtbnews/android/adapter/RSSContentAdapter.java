/**
 * 
 */
package de.mtbnews.android.adapter;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;

import android.content.Context;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.mtbnews.android.R;

/**
 * @author dankert
 * 
 */
public class RSSContentAdapter extends BaseAdapter
{

	/** Remember our context so we can use it when constructing views. */
	private Context mContext;

	/**
	 * Hold onto a copy of the entire Contact List.
	 */

	private LayoutInflater inflator;

	private RSSFeed feed;

	public RSSContentAdapter(Context context, RSSFeed feed)
	{
		mContext = context;
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.feed = feed;
	}

	public int getCount()
	{
		return feed.getItems().size();
	}

	public Object getItem(int position)
	{
		return feed.getItems().get(position);
	}

	/** Use the array index as a unique id. */
	public long getItemId(int position)
	{
		return position;

	}

	/**
	 * @param convertView
	 *            The old view to overwrite, if one is passed
	 * @returns a ContactEntryView that holds wraps around an ContactEntry
	 */
	public View getView(int position, View convertView, ViewGroup parent)
	{

		RSSItem e = feed.getItems().get(position);

		final View view = inflator.inflate(R.layout.rss_item, null);

		TextView datum = (TextView) view.findViewById(R.id.item_date);
		datum.setText(DateFormat.getDateFormat(parent.getContext()).format(
				e.getPubDate())
				+ " "
				+ DateFormat.getTimeFormat(parent.getContext()).format(
						e.getPubDate()));

		TextView name = (TextView) view.findViewById(R.id.item_title);
		name.setText(e.getTitle());

		TextView desc = (TextView) view.findViewById(R.id.item_description);

		if (e.getDescription() != null)
			desc.setText(e.getDescription() + " ...");
		else
			desc.setText("");

		return view;
	}

}
