package de.fruity.coffeeapp.adminmode;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import de.fruity.coffeeapp.GroupmodeData;
import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.SqlAccessAPI;

public class ShowGroupsDetailFragment extends android.support.v4.app.DialogFragment {

    private static final String TAG = ShowGroupsDetailFragment.class.getSimpleName();
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean D = false;

    /**
     * Create a new instance of DetailsFragment, initialized to show the text at
     * 'index'.
     */
    public static ShowGroupsDetailFragment newInstance(int position, long id) {
        ShowGroupsDetailFragment mShowGroupDetailFragment = new ShowGroupsDetailFragment();
        Log.i(TAG, "Index is  " + position);
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", position);
        args.putLong("id", id);
        mShowGroupDetailFragment.setArguments(args);

        return mShowGroupDetailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List<GroupmodeData> name_list;
        ArrayAdapter<String> adapter;
        ListView elv;

        if (container == null) {
            return null;
        }
        // View for Measurements
        View mainMeasurementView = inflater.inflate(R.layout.fragment_adminmode_groupsdetail,
                container, false);
        name_list = SqlAccessAPI.getNamesInGroup(getActivity().getContentResolver(), getShownID());


        adapter = new ArrayAdapter<>(getActivity(), R.layout.activity_main_navigationdrawer_object);

        for (GroupmodeData data : name_list) {
            adapter.add(data.getName());
        }

        elv = (ListView) mainMeasurementView.findViewById(R.id.lv_people_in_group);
        elv.setAdapter(adapter);

        return mainMeasurementView;
    }

    /**
     * Necessary for the update in MeasurementsFragment
     *
     * @return Position in ListView
     */
    public int getShownPosition() {
        return getArguments().getInt("index", 0);
    }

    /**
     * @return id of sqliteobject
     */
    private long getShownID() {
        if (D)
            Log.d(TAG, "getShowID" + getArguments().getLong("id", 0));
        return getArguments().getLong("id", 0);
    }
}