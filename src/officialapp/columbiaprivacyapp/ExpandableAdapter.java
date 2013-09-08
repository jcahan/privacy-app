package officialapp.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import officialapp.columbiaprivacyapp.R;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;

/*Please note I encountered two errors with the ExpandableListAdapter Class, as explained more below. The first dealt with random checkboxes being checked/unchecked when 
 * scrolling. I circumvented it by limiting the number of updates to the checkboxes to every 145 milliseconds, which has fixed it. Though this gives rise to the potential issue, 
 * that some users might attempt to quickly add items to their lists, I doubt it will be significant. Secondly, there is an issue neither I, nor others of the SO community, could solve, 
 * regarding the parent checkboxes's listener, upon expansion, would disrupt other action on the page. That is, occasionally after opening a parent checkbox (to display its children), if no children are selected, 
 * and I would later attempt to display another parent's children, I would be unable to do so.  
 */
public class ExpandableAdapter extends BaseExpandableListAdapter {
	private long lastAction = 0; 
	private LayoutInflater layoutInflater;
	private LinkedHashMap<Item, ArrayList<Item>> groupList;
	private ArrayList<Item> mainGroup;
	private int[] groupStatus;
	private long lastGroupAction = 0; 

	final private int TIME_TO_WAIT = 145; 

	//NB: Encountered this issue, worked around by limiting number of updates to list to at most one ever 145 milliseconds 
	//http://stackoverflow.com/questions/6143499/expandablelistview-open-collapse-problem
	//Creates he 
	public ExpandableAdapter(Context context, ExpandableListView listView,
			LinkedHashMap<Item, ArrayList<Item>> groupsList) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		this.groupList = groupsList;
		groupStatus = new int[groupsList.size()];

		listView.setOnGroupExpandListener(new OnGroupExpandListener() {

			public void onGroupExpand(int groupPosition) {
				if(lastGroupAction==0) lastGroupAction= System.currentTimeMillis();

				lastGroupAction = System.currentTimeMillis();
				Item group = mainGroup.get(groupPosition);
				if (groupList.get(group).size() > 0)
					groupStatus[groupPosition] = 1;
			}
		});
		//NB: Inherent Group Listener Issue with Android, where a Group Listener might disrupt other actions on a page. That is, 
		listView.setOnGroupClickListener(new android.widget.ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return false;
			}
		});
		listView.setOnGroupCollapseListener(new android.widget.ExpandableListView.OnGroupCollapseListener() {

			public void onGroupCollapse(int groupPosition) {
				lastGroupAction = System.currentTimeMillis();
				Item group = mainGroup.get(groupPosition);
				if (groupList.get(group).size() > 0)
					groupStatus[groupPosition] = 0;

			}
		});

		mainGroup = new ArrayList<Item>();
		for (Map.Entry<Item, ArrayList<Item>> mapEntry : groupList.entrySet()) {
			mainGroup.add(mapEntry.getKey());
		}
	}



	public Item getChild(int groupPosition, int childPosition) {
		Item item = mainGroup.get(groupPosition);
		return groupList.get(item).get(childPosition);

	}

	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final ChildHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.group_item, null);
			holder = new ChildHolder();
			holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.title.setGravity(Gravity.CENTER); //centers the children 
			convertView.setTag(holder);
		} 
		else {
			holder = (ChildHolder) convertView.getTag();
		}
		final Item child = getChild(groupPosition, childPosition);
		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//TODO: Continue looking for ways to improve the 145ms delay.. 
				boolean firstActionOkay = false; 
				if(lastAction==0) {
					lastAction = System.currentTimeMillis();
					firstActionOkay = true; 
				}
				if(firstActionOkay) {
					if(System.currentTimeMillis() - lastGroupAction < TIME_TO_WAIT) return; 
				}
				if(System.currentTimeMillis() - lastAction < TIME_TO_WAIT && !firstActionOkay) {
					return; 
				}
				//TODO: Might be too high
				else if(System.currentTimeMillis() - lastGroupAction < TIME_TO_WAIT) {
					return; 
				}
				else {
					lastAction = System.currentTimeMillis();
				}

				firstActionOkay = false; //might be an unnecessary line  

				Item parentGroup = getGroup(groupPosition);
				child.isChecked = isChecked;

				MainActivity.getInstance().refreshBlackListTree();
				//if the CHILD is checked 
				if (isChecked) {
					holder.cb.setChecked(child.isChecked);
					MainActivity.getInstance().addToBlackList(child.name);
					//ITEM was just checked, add it to the list
					ArrayList<Item> childList = getChild(parentGroup);
					int childIndex = childList.indexOf(child);
					boolean isAllChildClicked = true;
					for (int i = 0; i < childList.size(); i++) {
						if (i != childIndex) {
							Item siblings = childList.get(i);
							if (!siblings.isChecked) {
								isAllChildClicked = false;
								DataHolder.checkedChilds.put(child.name,
										parentGroup.name);
								//	}
								break;
							}
						}
					}
					//All the children are checked
					if (isAllChildClicked) {
						parentGroup.isChecked = true;
						if(!(DataHolder.checkedChilds.containsKey(child.name)==true)){
							DataHolder.checkedChilds.put(child.name,
									parentGroup.name);
						}
						checkAll = false;
					}
				} 
				//not all of the children are checked
				else {
					holder.cb.setChecked(child.isChecked);
					MainActivity.getInstance().deleteFromBlackList(child.name);
					if (parentGroup.isChecked) {
						parentGroup.isChecked = false;
						checkAll = false;
						DataHolder.checkedChilds.remove(child.name);
					} else {
						checkAll = true;
						DataHolder.checkedChilds.remove(child.name);
					}
				}
				//updates lists
				notifyDataSetChanged(); 
				MainActivity.getInstance().refreshAndSort();
			}
		});

		holder.cb.setChecked(child.isChecked);
		holder.title.setText(child.name);
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		Item item = mainGroup.get(groupPosition);
		return groupList.get(item).size();
	}

	public Item getGroup(int groupPosition) {
		return mainGroup.get(groupPosition);
	}

	public int getGroupCount() {
		return mainGroup.size();
	}

	public long getGroupId(int groupPosition) {
		return 0;
	}
	//works with the GroupView
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		final GroupHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.group_list, null);
			holder = new GroupHolder();
			holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.label_indicator);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.title.setGravity(Gravity.CENTER);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}

		holder.imageView
		.setImageResource(groupStatus[groupPosition] == 0 ? R.drawable.expand
				: R.drawable.collapse);
		final Item groupItem = getGroup(groupPosition);

		holder.title.setText(" "+ groupItem.name);

		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (checkAll) {
					ArrayList<Item> childItem = getChild(groupItem);
					MainActivity.getInstance().refreshBlackListTree();

					//Entire group is checked, add each item
					if(isChecked) {
						for (Item children : childItem) {
							if(!children.isChecked) {
								MainActivity.getInstance().addToBlackList(children.name);
								children.isChecked = true;
							}
						}
					}
					//deletes all items from list 
					else {
						for (Item children : childItem) {
							if(children.isChecked) {
								MainActivity.getInstance().deleteFromBlackList(children.name);
								children.isChecked = false;
							}
						}
					}
					MainActivity.getInstance().refreshAndSort();
				}

				groupItem.isChecked = isChecked;
				notifyDataSetChanged();
				new Handler().postDelayed(new Runnable() {

					public void run() {
						if (!checkAll)
							checkAll = true;
					}
				}, 50);
			}
		});
		holder.cb.setChecked(groupItem.isChecked);
		return convertView;
	}

	private boolean checkAll = true;

	private ArrayList<Item> getChild(Item group) {
		return groupList.get(group);
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private class GroupHolder {
		public ImageView imageView;
		public CheckBox cb;
		public TextView title;
	}

	private class ChildHolder {
		public TextView title;
		public CheckBox cb;
	}
}
