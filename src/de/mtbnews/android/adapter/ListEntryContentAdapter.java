/**
 * 
 */
package de.mtbnews.android.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.mtbnews.android.IBCApplication;
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

	private boolean containsBBCode;
	private boolean containsHtml;

	public ListEntryContentAdapter(Context context, List<? extends ListEntry> list)
	{
		mContext = context;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
	}

	public ListEntryContentAdapter(Context context, List<? extends ListEntry> list, boolean containsBBCode,
			boolean containsHtml)
	{
		mContext = context;
		inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = list;
		this.containsBBCode = containsBBCode;
		this.containsHtml = containsHtml;
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

			viewHolder.datum = (TextView) convertView.findViewById(R.id.item_date);
			viewHolder.name = (TextView) convertView.findViewById(R.id.item_name);
			viewHolder.title = (TextView) convertView.findViewById(R.id.item_title);
			viewHolder.desc = (TextView) convertView.findViewById(R.id.item_description);
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (e.getDate() != null)
			viewHolder.datum.setText(DateFormat.getDateFormat(parent.getContext()).format(e.getDate()) + " "
					+ DateFormat.getTimeFormat(parent.getContext()).format(e.getDate()));
		else
			// viewHolder.datum.setEnabled(false);
			viewHolder.datum.setText("");

		if (e.getName() != null)
			viewHolder.name.setText(e.getName());
		else
			// viewHolder.name.setEnabled(false);
			viewHolder.name.setText("");

		if (e.getTitle() != null)
			viewHolder.title.setText(e.getTitle());
		else
			viewHolder.title.setEnabled(false);

		if (e.isUnread())
			viewHolder.title.setTypeface(null, Typeface.BOLD);
		else
			viewHolder.title.setTypeface(null, Typeface.NORMAL);

		if (e.getContent() != null)
		{
			SharedPreferences prefs = ((IBCApplication) ((Activity) mContext).getApplication()).prefs;
			if (containsBBCode && prefs.getBoolean("parse_bbcode", false))
			{
				String html;
				// TextProcessor create = BBProcessorFactory.getInstance()
				// .create();
				// html = create.process(e.getContent());
				html = e.getContent();
				html = new BBCodeConverter().process(html);

				// Html.ImageGetter imageGetter = null;
				// if (prefs.getBoolean("load_images", false))
				// // imageGetter = new ImageGetter();
				// imageGetter = new URLImageParser(viewHolder.desc, mContext);

				// viewHolder.desc.setText(Html.fromHtml(html.toString(),
				// imageGetter, null));
				viewHolder.desc.setText(Html.fromHtml(html));
				viewHolder.desc.setMovementMethod(LinkMovementMethod.getInstance());
				// viewHolder.desc.setAutoLinkMask(Linkify.ALL);
			}
			else if (containsHtml)
			{

				final CharSequence html = e.getContent();
				// if (prefs.getBoolean("load_images", false))
				// imageGetter = new URLImageParser(viewHolder.desc, mContext);

				viewHolder.desc.setText(Html.fromHtml(html.toString()));
			}
			else
			{
				viewHolder.desc.setText(e.getContent());
			}
		}
		else
			viewHolder.desc.setText("");

		return convertView;
	}

	static class ViewHolder
	{
		TextView datum;
		TextView title;
		TextView name;
		TextView desc;
	}

}
