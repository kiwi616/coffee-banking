package de.fruity.coffeeapp.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import de.fruity.coffeeapp.R;


/**
 * Simple ListAdapter showing the different Objects in the ListView
 * @author kiwi
 *
 */
public class AdminCursorAdapter extends CursorAdapter{

	private LayoutInflater inflator;

    public AdminCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		inflator = LayoutInflater.from (context);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = view.findViewById(R.id.tv_listobject_adminsfragment);
		Switch switchAdmin = view.findViewById(R.id.sw_listobject_adminsfragment);
        ContentResolver mContentResolver = context.getContentResolver();
		String name = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_NAME));
		final int id = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_ID));

        tv.setText(name);

		if (SqlAccessAPI.isAdminByID(mContentResolver, id)) {
			switchAdmin.setChecked(true);
		} else {
			switchAdmin.setChecked(false);
		}

	}

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflator.inflate(R.layout.list_object_tv_switch_, parent, false);
	}

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
