package de.fruity.coffeeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CheckboxListAdapter extends BaseAdapter implements OnClickListener {
		 
		/** The inflator used to inflate the XML layout */
		private LayoutInflater inflator;
	 
		/** A list containing some sample data to show. */
		private List<GroupmodeData> dataList;
	 
		public CheckboxListAdapter(LayoutInflater inflator) {
			super();
			this.inflator = inflator;
	 
			dataList = new ArrayList<>();
	 
		}
	 
		@Override
		public int getCount() {
			return dataList.size();
		}
	 
		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}
	 
		@Override
		public long getItemId(int position) {
			return position;
		}
	 
		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
	 
			// We only create the view if its needed
			if (view == null) {
				view = inflator.inflate(R.layout.list_object_tv_checkbox, viewGroup, false);
	 
				// Set the click listener for the checkbox
				view.findViewById(R.id.checkBox1).setOnClickListener(this);
			}
	 
			GroupmodeData data = (GroupmodeData) getItem(position);
	 
			// Set the example text and the state of the checkbox
			CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
			cb.setChecked(data.isSelected());
			// We tag the data object to retrieve it on the click listener.
			cb.setTag(data);
	 
			TextView tv = (TextView) view.findViewById(R.id.textView1);
			tv.setText(data.getName());
	 
			return view;
		}
	 
		@Override
		/** Will be called when a checkbox has been clicked. */
		public void onClick(View view) {
			GroupmodeData data = (GroupmodeData) view.getTag();
			data.setSelected(((CheckBox) view).isChecked());
		}

		public void add(GroupmodeData groupmodeData) {
			dataList.add(groupmodeData);
		}

		public List<GroupmodeData> getSelected() {
			List<GroupmodeData> ret = new ArrayList<>();
			for ( GroupmodeData gd : dataList){
				if ( gd.isSelected())
					ret.add(gd);
			}
			return ret;
		}
}


