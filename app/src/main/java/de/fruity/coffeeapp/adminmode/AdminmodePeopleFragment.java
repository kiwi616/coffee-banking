package de.fruity.coffeeapp.adminmode;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.ListView;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.PeopleTableCursorAdapter;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link } interface
 * to handle interaction events.
 * Use the {@link AdminmodePeopleFragment#} factory method to
 * create an instance of this fragment.
 */
public class AdminmodePeopleFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = AdminmodePeopleFragment.class.getSimpleName();
    private static final boolean D = false;

    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int RENAME_ID = Menu.FIRST + 2;

    private int mCurCheckPosition = 0;
    private long mCurId = 1;
    private ContentResolver mContentResolver;

    private PeopleTableCursorAdapter mAdapter;

    private static boolean datahaschanged = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_adminmode_people, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity() != null;

        mContentResolver = getActivity().getContentResolver();

        fillData();

        getListView().setDividerHeight(2);
        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    }

    private void fillData() {
        getLoaderManager().initLoader(1, null, this);
        // Populate list with our static array of titles.
        mAdapter = new PeopleTableCursorAdapter(getActivity(), null, false);
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
            assert getFragmentManager() != null;
            Fragment databaseDetail = getFragmentManager().findFragmentById(
                    R.id.people_details_fragment);

            // if (databaseDetail == null || ((ShowDetailsFragment)
            // databaseDetail).getShownPosition() != postion) {
            if (databaseDetail == null || datahaschanged) {
                // Make new fragment to show this selection.
                databaseDetail = ShowPersonsDetailFragment.
                        ShowPersonsDetailFragmentnewInstance(postion, id);
                // Execute a transaction, replacing any existing
                // fragment with this one inside the frame.
                FragmentTransaction ft = getFragmentManager()
                        .beginTransaction();
                ft.replace(R.id.people_details_fragment, databaseDetail);
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
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                Uri uri = Uri.parse(SqlDatabaseContentProvider.CONTENT_URI + "/"
                        + info.id);

                mContentResolver.delete(uri, null, null);
                fillData();
                return true;
            case RENAME_ID:
                AdapterView.AdapterContextMenuInfo info_name = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                updateName(info_name.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (D)
            Log.i(TAG, "onCreateLoader");
        String[] projection = {SqliteDatabase.COLUMN_ID, SqliteDatabase.COLUMN_NAME};
        return new CursorLoader(getActivity(),
                SqlDatabaseContentProvider.CONTENT_URI, projection, null, null,
                SqliteDatabase.COLUMN_POSITION + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        // Set the new data in the adapter.
        mAdapter.swapCursor(cursor);
        refreshView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private void updateName(final long id) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());


        alert.setTitle("Enter a new name");

        // Set an EditText view to get user input
        final EditText inputName = new EditText(getActivity());
        alert.setView(inputName);

        alert.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alert.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Uri uri = Uri.parse(SqlDatabaseContentProvider.CONTENT_URI + "/"
                                + id);
                        String routeName;
                        routeName = inputName.getText().toString();
                        ContentValues coffee = new ContentValues();
                        coffee.put(SqliteDatabase.COLUMN_NAME, routeName);
                        mContentResolver.update(uri, coffee, null, null);
                    }
                });

        alert.show();
    }
}
