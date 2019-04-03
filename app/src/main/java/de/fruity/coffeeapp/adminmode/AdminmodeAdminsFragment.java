package de.fruity.coffeeapp.adminmode;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.AdminCursorAdapter;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link =.} interface
 * to handle interaction events.
 * Use the {@link AdminmodeAdminsFragment#} factory method to
 * create an instance of this fragment.
 */
public class AdminmodeAdminsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = AdminmodeAdminsFragment.class.getSimpleName();
    private static final boolean D = false;

    private AdminCursorAdapter mAdminCursorAdapter;
    private ListView mListView;

    private enum DATABASE_PRICE_FIELDS {
        MIN, MAX, STEPSIZE, DEFAULTVALUE
    }

    private void configureNumberpicker(NumberPicker np, final String database_id_field,
                                       final DATABASE_PRICE_FIELDS kind) {
        int index = 0;
        float current_val;
        final String[] nums = {"0.05", "0.10", "0.15", "0.2", "0.25", "0.30", "0.35", "0.40",
                "0.45", "0.50", "0.55", "0.60", "0.70", "0.75", "0.8", "0.9", "1", "1.5", "2.0", "2.5",
                "3.0", "3.5", "4.0", "4.5", "5.0", "10.0", "20.0", "30.0", "40.0", "50.0"};

        switch (kind){
            case MAX:
                current_val = SqlAccessAPI.getPriceMax(getActivity().getContentResolver(), database_id_field);
                break;
            case MIN:
                current_val = SqlAccessAPI.getPriceMin(getActivity().getContentResolver(), database_id_field);
                break;
            case STEPSIZE:
                current_val = SqlAccessAPI.getPriceStepsize(getActivity().getContentResolver(), database_id_field);
                break;
            case DEFAULTVALUE:
                current_val = SqlAccessAPI.getDefaultPrice(getActivity().getContentResolver(), database_id_field);
                break;
            default:
                throw new IllegalArgumentException();
        }

        np.setMaxValue(nums.length - 1);
        np.setMinValue(0);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(nums);

        for (String number : nums)
        {
            if ( Float.parseFloat(number) == current_val )
                np.setValue(index);
            index++;
        }

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int index = numberPicker.getValue();
                String val = nums[index];
                float selectedFloat = Float.parseFloat(val);

                switch (kind) {
                    case MIN:
                        SqlAccessAPI.setPriceMin(getActivity().getContentResolver(), selectedFloat,
                                database_id_field);
                        break;
                    case MAX:
                        SqlAccessAPI.setPriceMax(getActivity().getContentResolver(), selectedFloat,
                                database_id_field);
                        break;
                    case STEPSIZE:
                        SqlAccessAPI.setPriceStepsize(getActivity().getContentResolver(), selectedFloat,
                                database_id_field);
                        break;
                    case DEFAULTVALUE:
                        SqlAccessAPI.setPriceDefault(getActivity().getContentResolver(), selectedFloat,
                                database_id_field);
                        break;
                    default:
                        throw new IllegalArgumentException();

                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_adminmode_admins, container, false);
        int i;
        TextView tv;
        NumberPicker et_min;
        NumberPicker et_max;
        NumberPicker et_stepsize;
        NumberPicker et_defaultvalue;

        mListView = rootView.findViewById(R.id.lv_admins_fragment);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                int personal_id = (int) mAdminCursorAdapter.getItemId(position);

                if (SqlAccessAPI.isAdminByID(getActivity().getContentResolver(), personal_id))
                    SqlAccessAPI.deleteAdmin(getActivity().getContentResolver(), personal_id);
                else
                    SqlAccessAPI.setAdmin(getActivity().getContentResolver(), personal_id);

                mAdminCursorAdapter.notifyDataSetChanged();
            }
        });

        int[] arr_ids = new int[]{R.id.price_changer_1, R.id.price_changer_2,
                R.id.price_changer_3, R.id.price_changer_4, R.id.price_changer_5};
        String[] arr = new String[]{"coffee", "candy", "beer", "can", "misc"};

        for (i = 0; i < arr_ids.length; i++) {
            tv = rootView.
                    findViewById(arr_ids[i]).findViewById(R.id.tv_kind);
            tv.setText(arr[i]);

            et_min = rootView.
                    findViewById(arr_ids[i]).findViewById(R.id.np_min);
            configureNumberpicker(et_min, arr[i], DATABASE_PRICE_FIELDS.MIN);

            et_max = rootView.
                    findViewById(arr_ids[i]).findViewById(R.id.np_max);
            configureNumberpicker(et_max, arr[i], DATABASE_PRICE_FIELDS.MAX);

            et_stepsize = rootView.
                    findViewById(arr_ids[i]).findViewById(R.id.np_stepsize);
            configureNumberpicker(et_stepsize, arr[i], DATABASE_PRICE_FIELDS.STEPSIZE);

            et_defaultvalue = rootView.
                    findViewById(arr_ids[i]).findViewById(R.id.np_default);
            configureNumberpicker(et_defaultvalue, arr[i], DATABASE_PRICE_FIELDS.DEFAULTVALUE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData();
    }

    private void fillData() {
        getLoaderManager().initLoader(1, null, this);
        // Populate list with our static array of titles.
        mAdminCursorAdapter = new AdminCursorAdapter(getActivity(), null, false);
        mListView.setAdapter(mAdminCursorAdapter);
    }



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
        mAdminCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mAdminCursorAdapter.swapCursor(null);
    }


}
