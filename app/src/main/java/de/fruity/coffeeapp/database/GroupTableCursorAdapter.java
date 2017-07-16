package de.fruity.coffeeapp.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.fruity.coffeeapp.R;


/**
 * Simple ListAdapter showing the different Objects in the ListView
 *
 * @author kiwi
 */
public class GroupTableCursorAdapter extends CursorAdapter {

    private LayoutInflater inflator;

    public GroupTableCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        inflator = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.main_tv_names);

        //int tmp = cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_TYPE);
        String type = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_G_GROUP_NAME));

        tvName.setText(type);

    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflator.inflate(R.layout.list_object_tv, parent, false);
    }
}
