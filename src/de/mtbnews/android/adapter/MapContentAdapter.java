/**
 * 
 */
package de.mtbnews.android.adapter;

import java.util.List;
import java.util.Map;

import org.mcsoxford.rss.RSSItem;

import android.content.Context;
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
public class MapContentAdapter extends BaseAdapter
{

	/** Remember our context so we can use it when constructing views. */
	private Context mContext;

	/**
	 * Hold onto a copy of the entire Contact List.
	 */

	private LayoutInflater inflator;

	private String dateKey;
	private String titleKey;
	private String descriptionKey;

	private List<Map<String, Object>> map;

	public MapContentAdapter(Context context, List<Map<String, Object>> map,
			String dateKey, String titleKey, String descriptionKey)
	{
		mContext = context;
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.map = map;
		this.dateKey = dateKey;
		this.titleKey = titleKey;
		this.descriptionKey = descriptionKey;
	}

	public int getCount()
	{
		return map.size();
	}

	public Object getItem(int position)
	{
		return map.get(position);
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

		Map<String, Object> e = map.get(position);

		final View view = inflator.inflate(R.layout.rss_item, null);

		if (dateKey != null)
		{
			TextView datum = (TextView) view.findViewById(R.id.item_date);
			datum.setText(DateFormat.getDateFormat(parent.getContext()).format(
					e.get(dateKey))
					+ " "
					+ DateFormat.getTimeFormat(parent.getContext()).format(
							e.get(dateKey)));
		}

		if (titleKey != null)
		{

			TextView name = (TextView) view.findViewById(R.id.item_title);
			name.setText(new String((byte[]) e.get(titleKey)));
		}

		if (descriptionKey != null)
		{
			TextView desc = (TextView) view.findViewById(R.id.item_description);

			if (e.get(descriptionKey) != null)
				desc.setText(new String((byte[]) e.get(descriptionKey))
						+ " ...");
			else
				desc.setText("");
		}

		return view;
	}

}
