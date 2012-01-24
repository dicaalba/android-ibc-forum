/**
 * 
 */
package de.mtbnews.android.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.mtbnews.android.R;
import de.mtbnews.android.tapatalk.wrapper.ListEntry;

/**
 * @author dankert
 * 
 */
public class ListEntryContentAdapter extends BaseAdapter
{

	/** Remember our context so we can use it when constructing views. */
	private Context mContext;

	/**
	 * Hold onto a copy of the entire Contact List.
	 */

	private LayoutInflater inflator;

	private List<? extends ListEntry> list;

	public ListEntryContentAdapter(Context context, List<? extends ListEntry> list)
	{
		mContext = context;
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	public int getCount()
	{
		return list.size();
	}

	public Object getItem(int position)
	{
		return list.get(position);
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

		ListEntry e = list.get(position);

		final View view = inflator.inflate(R.layout.rss_item, null);

		// Linken Rand ggf. erhöhen.
		// LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// params2.setMargins(20,0,0,0);
		// view.setLayoutParams(params2);

		TextView datum = (TextView) view.findViewById(R.id.item_date);
		if (e.getDate() != null)
		{
			datum.setText(DateFormat.getDateFormat(parent.getContext()).format(
					e.getDate())
					+ " "
					+ DateFormat.getTimeFormat(parent.getContext()).format(
							e.getDate()));
		}
		else
		{
			datum.setEnabled(false);
		}

		if (e.getTitle() != null)
		{
			TextView name = (TextView) view.findViewById(R.id.item_title);
			name.setText(e.getTitle());
		}

		if (e.getContent() != null)
		{
			TextView desc = (TextView) view.findViewById(R.id.item_description);
			desc.setText(e.getContent());
		}

		return view;
	}
}
