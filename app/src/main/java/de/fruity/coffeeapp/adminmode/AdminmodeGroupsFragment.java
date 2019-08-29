package de.fruity.coffeeapp.adminmode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

import de.fruity.coffeeapp.CheckboxListAdapter;
import de.fruity.coffeeapp.GroupmodeData;
import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.GroupTableCursorAdapter;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link AdminmodeGroupsFragment} factory method to
 * create an instance of this fragment.
 */
public class AdminmodeGroupsFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = AdminmodeGroupsFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final int DELETE_ID = Menu.FIRST + 3;
    private static final int RENAME_ID = Menu.FIRST + 4;

    private int mCurCheckPosition = 0;
    private long mCurId = 1;
    private ContentResolver mContentResolver;

    private GroupTableCursorAdapter mAdapter;

    private static boolean datahaschanged = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_adminmode_groups, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContentResolver = Objects.requireNonNull(getActivity()).getContentResolver();

        fillData();

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGroup();
            }
        });

        getListView().setDividerHeight(2);
        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    }

    private void fillData() {
        getLoaderManager().initLoader(2, null, this);
        // Populate list with our static array of titles.
        mAdapter = new GroupTableCursorAdapter(getActivity(),null, false);
        setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // In dual-pane mode, the list view highlights the selected item.
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    // Opens the second activity if an entry is clicked
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mCurCheckPosition = position;
        mCurId = id;
        if (D)
            Log.w(TAG, "List item id/position " + id + '/' + position);
        datahaschanged = true;
        showDetail(position, id);
    }

    private void createNewGroup() {

        final Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.setContentView(R.layout.dialog_groupmode);
        dialog.setTitle(R.string.groupmode);

        // set the custom dialog components - text, image and button
        ListView lvData = dialog
                .findViewById(R.id.groupmode_lv_dialog);

        final CheckboxListAdapter adapter = new CheckboxListAdapter(getLayoutInflater(null));

        lvData.setAdapter(adapter);

        dialog.setCancelable(false);
        Button cancelButton = dialog
                .findViewById(R.id.groupmode_btn_cancel);
        Button btnSave = dialog.findViewById(R.id.groupmode_btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int curPosition = 0;
                Cursor mCursor = getActivity().getContentResolver().query(
                        SqlDatabaseContentProvider.GROUPUSER_REFERENCE_URI,
                        new String[]{"MAX("
                                + SqliteDatabase.COLUMN_GU_GROUP_ID + ")"},
                        null, null, null);

                assert mCursor != null;
                if (mCursor.moveToFirst()) {
                    try {
                        curPosition = mCursor.getInt(0);
                        curPosition++;
                    } catch (Exception es) {
                        Log.e(TAG, "I hate you all");
                    }
                }
                mCursor.close();

                enterNameDialog(curPosition, adapter);

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Cursor rfidCursor = getActivity().getContentResolver().query(
                SqlDatabaseContentProvider.CONTENT_URI, null, null, null, null);
        if (rfidCursor != null) {
            while (rfidCursor.moveToNext()) {
                String s = rfidCursor.getString(rfidCursor
                        .getColumnIndexOrThrow(SqliteDatabase.COLUMN_NAME));
                int rfid = rfidCursor.getInt(rfidCursor
                        .getColumnIndexOrThrow(SqliteDatabase.COLUMN_RFID));
                int id = rfidCursor.getInt(rfidCursor
                        .getColumnIndexOrThrow(SqliteDatabase.COLUMN_ID));
                adapter.add(new GroupmodeData(s, false, rfid, id));
            }
            rfidCursor.close();
        }
        dialog.show();
    }

    private void enterNameDialog(final int positionId,
                                 final CheckboxListAdapter adapter) {

        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        final EditText inputName = new EditText(getContext());
        adb.setView(inputName);
        adb.setTitle("gruppen name");
        adb.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues groupUserReferenceValues = new ContentValues();
                        ContentValues groupValues = new ContentValues();

                        groupValues.put(SqliteDatabase.COLUMN_G_GROUP_NAME, inputName.getText().toString());
                        groupValues.put(SqliteDatabase.COLUMN_G_GROUP_ID, positionId);

                        getActivity().getContentResolver()
                                .insert(SqlDatabaseContentProvider.GROUP_URI,
                                        groupValues);


                        groupUserReferenceValues.put(SqliteDatabase.COLUMN_GU_GROUP_ID,
                                positionId);
                        for (GroupmodeData gd : adapter.getSelected()) {
                            groupUserReferenceValues.put(SqliteDatabase.COLUMN_GU_USER_ID,
                                    gd.getID());
                            getActivity().getContentResolver()
                                    .insert(SqlDatabaseContentProvider.GROUPUSER_REFERENCE_URI,
                                            groupUserReferenceValues);
                        }

                        Toast.makeText(getContext(),
                                "Gruppe mit name " + inputName.getText().toString() + " erstellt",
                                Toast.LENGTH_LONG).show();


                    }
                });
        adb.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        adb.show();
    }

    private void showDetail(int postion, long id) {

        setSelection(mCurCheckPosition);

        if (!getListAdapter().isEmpty()) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.

            getListView().setItemChecked(postion, true);
            if (D)
                Log.d(TAG, "showDeatil Id =  " + mCurId);
            if (D)
                Log.d(TAG, "showDeatil position =  " + mCurCheckPosition);

            // Check what fragment is currently shown, replace if needed.
            Fragment databaseDetail = getFragmentManager().findFragmentById(
                    R.id.fl_groups_detail);

            // if (databaseDetail == null || ((ShowDetailsFragment)
            // databaseDetail).getShownPosition() != postion) {
            if (databaseDetail == null || datahaschanged) {
                // Make new fragment to show this selection.
                databaseDetail = ShowGroupsDetailFragment.newInstance(postion, id);
                // Execute a transaction, replacing any existing
                // fragment with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fl_groups_detail, databaseDetail);
                ft.setTransition(FragmentTransaction.TRANSIT_NONE);
                ft.commit();

                datahaschanged = false;

            } else {
                if (D)
                    Log.d(TAG, "same item selected");
            }
        }
    }

    /**
     * Show first item. Is called if new items are inserted or loader is created
     */
    private void refreshView() {
        Handler h = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                datahaschanged = true;
                showDetail(mCurCheckPosition, mCurId);
            }
        };
        h.post(r);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, android.R.string.cut);
        menu.add(0, RENAME_ID, 0, R.string.rename);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                long id;
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                id = info.id;

                mContentResolver.delete(SqlDatabaseContentProvider.GROUP_URI,
                        SqliteDatabase.COLUMN_G_GROUP_ID + " = ?", new String[]{Long.toString(id)});
                fillData();
                return true;
            case RENAME_ID:
                AdapterView.AdapterContextMenuInfo info_name = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                updateGroupName(info_name.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (D)
            Log.i(TAG, "onCreateLoader");
        String[] projection = {SqliteDatabase.COLUMN_G_GROUP_ID, SqliteDatabase.COLUMN_G_GROUP_NAME,};
        return new CursorLoader(getActivity(),
                SqlDatabaseContentProvider.GROUP_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Set the new data in the adapter.
        mAdapter.swapCursor(cursor);
        refreshView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private void updateGroupName(final long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Enter a new name");

        // Set an EditText view to get user input
        final EditText inputName = new EditText(getActivity());
        alert.setView(inputName);

        alert.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String routeName;
                        routeName = inputName.getText().toString();
                        ContentValues group_values = new ContentValues();
                        group_values.put(SqliteDatabase.COLUMN_G_GROUP_NAME, routeName);
                        mContentResolver.update(SqlDatabaseContentProvider.GROUP_URI,
                                group_values,
                                SqliteDatabase.COLUMN_G_GROUP_ID + " = ?",
                                new String[]{Long.valueOf(id).toString()});
                    }
                });

        alert.show();
    }
}
