/**
 * 
 */
package de.mtbnews.android.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.opengl.Visibility;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.mtbnews.android.R;

/**
 * @author dankert
 * @Deprecated use ListEntryContentAdapter
 */
@Deprecated
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

		if (dateKey != null)
		{
			viewHolder.datum.setText(DateFormat.getDateFormat(
					parent.getContext()).format(e.get(dateKey))
					+ " "
					+ DateFormat.getTimeFormat(parent.getContext()).format(
							e.get(dateKey)));
		}
		else
		{
			viewHolder.datum.setVisibility(View.INVISIBLE);
		}

		if (titleKey != null)
		{
			viewHolder.name.setText(new String((byte[]) e.get(titleKey)));
		}

		if (descriptionKey != null)
		{

			if (e.get(descriptionKey) != null)
				viewHolder.desc.setText(new String((byte[]) e
						.get(descriptionKey)));
			else
				viewHolder.desc.setText("");
		}
		else
		{
			viewHolder.desc.setText("");
		}

		return convertView;
	}

	static class ViewHolder
	{
		TextView datum;
		TextView name;
		TextView desc;
	}

}
