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
import de.mtbnews.android.adapter.MapContentAdapter.ViewHolder;
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

	public ListEntryContentAdapter(Context context,
			List<? extends ListEntry> list)
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

		final ListEntry e = list.get(position);
		final ViewHolder viewHolder;

		if (convertView == null)
		{
			convertView = inflator.inflate(R.layout.rss_item, null);

			// Linken Rand ggf. erhöhen.
			// LinearLayout.LayoutParams params2 = new
			// LinearLayout.LayoutParams(
			// LinearLayout.LayoutParams.FILL_PARENT,
			// LinearLayout.LayoutParams.WRAP_CONTENT);
			// params2.setMargins(20,0,0,0);
			// view.setLayoutParams(params2);

			viewHolder = new ViewHolder();

			viewHolder.datum = (TextView) convertView
					.findViewById(R.id.item_date);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.item_title);
			viewHolder.desc = (TextView) convertView
					.findViewById(R.id.item_description);
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (e.getDate() != null)
			viewHolder.datum.setText(DateFormat.getDateFormat(
					parent.getContext()).format(e.getDate())
					+ " "
					+ DateFormat.getTimeFormat(parent.getContext()).format(
							e.getDate()));
		else
			viewHolder.datum.setEnabled(false);

		if (e.getTitle() != null)
			viewHolder.name.setText(e.getTitle());
		else
			viewHolder.name.setEnabled(false);

		if (e.getContent() != null)
			viewHolder.desc.setText(e.getContent());
		else
			viewHolder.desc.setText("");

		return convertView;
	}

	static class ViewHolder
	{
		TextView datum;
		TextView name;
		TextView desc;
	}

}
