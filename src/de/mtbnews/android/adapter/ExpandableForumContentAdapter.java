/**
 * 
 */
package de.mtbnews.android.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import de.mtbnews.android.R;
import de.mtbnews.android.tapatalk.wrapper.Forum;

/**
 * @author dankert
 * 
 */
public class ExpandableForumContentAdapter extends BaseExpandableListAdapter
{

	private static final int IBC_GELB = Color.parseColor("#FFCC66");

	/** Remember our context so we can use it when constructing views. */
	private Context mContext;

	/**
	 * Hold onto a copy of the entire Contact List.
	 */

	private LayoutInflater inflator;
	private List<Forum> forumList;

	public ExpandableForumContentAdapter(Context context, List<Forum> forumList)
	{
		mContext = context;
		inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.forumList = forumList;
	}

	@Override
	public Object getChild(int arg0, int arg1)
	{
		Forum e = forumList.get(arg0).subForen.get(arg1);
		return e;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent)
	{
		Forum e = forumList.get(groupPosition).subForen.get(childPosition);

		final View view = inflator.inflate(R.layout.rss_item, null);

		TextView datum = (TextView) view.findViewById(R.id.item_date);
		datum.setVisibility(View.INVISIBLE);

		TextView name = (TextView) view.findViewById(R.id.item_title);
		name.setText(e.getTitle());

		TextView desc = (TextView) view.findViewById(R.id.item_description);

		desc.setText(e.getContent());

		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return forumList.get(groupPosition).subForen.size();
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return forumList.get(groupPosition);
	}

	@Override
	public int getGroupCount()
	{
		return forumList.size();
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent)
	{
		Forum e = forumList.get(groupPosition);

		final View view = inflator.inflate(R.layout.rss_item, null);
		view.setBackgroundColor(IBC_GELB);

		TextView datum = (TextView) view.findViewById(R.id.item_date);
		datum.setText("");
		datum.setBackgroundColor(IBC_GELB);
		// datum.setVisibility(View.INVISIBLE);

		TextView name = (TextView) view.findViewById(R.id.item_name);
		name.setText("");
		name.setBackgroundColor(IBC_GELB);
		// name.setVisibility(View.INVISIBLE);

		TextView title = (TextView) view.findViewById(R.id.item_title);
		title.setGravity(Gravity.CENTER);
		title.setBackgroundColor(IBC_GELB);
		title.setText(e.getTitle());

		TextView desc = (TextView) view.findViewById(R.id.item_description);
		// desc.setVisibility(View.INVISIBLE);
		// desc.setText(e.getContent());
		desc.setText("");
		desc.setBackgroundColor(IBC_GELB);

		return view;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

}
