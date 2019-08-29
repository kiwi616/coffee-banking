package de.fruity.coffeeapp.adminmode;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.achartengine.GraphicalView;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.tools.HelperMethods;


public class ShowPersonsDetailFragment extends android.support.v4.app.DialogFragment {

    private static final String TAG = ShowPersonsDetailFragment.class.getSimpleName();

    /**
     * Create a new instance of DetailsFragment, initialized to show the text at
     * 'index'.
     */
    public static ShowPersonsDetailFragment ShowPersonsDetailFragmentnewInstance
    (int position, long id) {
        ShowPersonsDetailFragment mShowPersonsDetailFragment = new ShowPersonsDetailFragment();
        Log.i(TAG, "Index is  " + position);
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", position);
        args.putLong("id", id);
        mShowPersonsDetailFragment.setArguments(args);

        return mShowPersonsDetailFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        // View for Measurements
        View mainMeasurementView = inflater.inflate(R.layout.fragment_adminmode_peopledetail,
                container, false);

        EditText etCoffee = mainMeasurementView.findViewById(R.id.et_coffee);
        EditText etCandy = mainMeasurementView.findViewById(R.id.et_candy);
        EditText etBeer = mainMeasurementView.findViewById(R.id.et_beer);
        EditText etCan = mainMeasurementView.findViewById(R.id.et_can);
        EditText etMisc = mainMeasurementView.findViewById(R.id.et_meat);

        ImageButton ib_inc_coffee = mainMeasurementView.findViewById(R.id.ib_coffee_up);
        ImageButton ib_dec_coffee = mainMeasurementView.findViewById(R.id.ib_coffee_down);
        ImageButton ib_inc_candy = mainMeasurementView.findViewById(R.id.ib_candy_up);
        ImageButton ib_dec_candy = mainMeasurementView.findViewById(R.id.ib_candy_down);
        ImageButton ib_inc_beer = mainMeasurementView.findViewById(R.id.ib_beer_up);
        ImageButton ib_dec_beer = mainMeasurementView.findViewById(R.id.ib_beer_down);
        ImageButton ib_inc_can = mainMeasurementView.findViewById(R.id.ib_can_up);
        ImageButton ib_dec_can = mainMeasurementView.findViewById(R.id.ib_can_down);
        ImageButton ib_inc_meat = mainMeasurementView.findViewById(R.id.ib_meat_up);
        ImageButton ib_dec_meat = mainMeasurementView.findViewById(R.id.ib_meat_down);

        assert getContext() != null;
        assert getActivity() != null;
        assert getActivity().getContentResolver() != null;

        etCoffee.setText(String.format("Coffee Balance: %s", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getCoffeeValueFromPerson(
                        getActivity().getContentResolver(), (int) getShownID()))));
        etCandy.setText(String.format("Candy Balance: %s", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getCandyValueFromPerson(
                        getActivity().getContentResolver(), (int) getShownID()))));
        etBeer.setText(String.format("Beer Balance: %s", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getBeerValueFromPerson(
                        getActivity().getContentResolver(), (int) getShownID()))));
        etCan.setText(String.format("Can Balance: %s", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getCanValueFromPerson(
                        getActivity().getContentResolver(), (int) getShownID()))));
        etMisc.setText(String.format("Meat and stuff Balance: %s", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getMiscValueFromPerson(
                        getActivity().getContentResolver(), (int) getShownID()))));

        ib_inc_coffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.bookCoffee(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_dec_coffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.unbookCoffee(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_inc_candy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.bookCandy(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_dec_candy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.unbookCandy(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_inc_beer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.bookBeer(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_dec_beer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.unbookBeer(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_inc_can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.bookCan(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_dec_can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.unbookCan(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });
        ib_inc_meat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.bookMisc(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });

        ib_dec_meat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SqlAccessAPI.unbookMisc(getActivity().getContentResolver(),
                        (int) getShownID());
            }
        });



        LinearLayout chartContainer = mainMeasurementView.findViewById(R.id.chart);
        GraphicalView chart = HelperMethods.createLineChart(getContext(), getShownID());
        chartContainer.addView(chart);

        return mainMeasurementView;
    }


    /**
     * @return id of sqliteobject
     */
    private long getShownID() {
        return getArguments().getLong("id", 0);
    }
}