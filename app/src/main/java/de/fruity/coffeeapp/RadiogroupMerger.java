package de.fruity.coffeeapp;

import android.content.ContentResolver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.ui_elements.RadioButtonCustomized;
import de.fruity.coffeeapp.ui_elements.SeekBarCustomized;

public class RadiogroupMerger {
    private List<LinearLayout> mList;
    private int mChecked;

    private String mDatabaseIdentifier;
    private float mDefaultValue;
    private int mDefaultViewId;
    private ContentResolver mContentResolver;

    final private Handler mHandler = new Handler();

    RadiogroupMerger() {
        mList = new ArrayList<>();
    }

    public void retriggerTimer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SqlAccessAPI.setCurrentPrice(mContentResolver, mDefaultValue, mDatabaseIdentifier);
                    check(mDefaultViewId);
                } catch (NullPointerException ex) {
                    Log.i("RadiogroupMerger", "no defaults available");
                }
            }
        };
        mHandler.removeMessages(0); // this id is 100 percent random :P seems
        // to be the default id
        mHandler.postDelayed(runnable, 15000);
    }

    void addView(LinearLayout v) {
        RadioButtonCustomized rbc = v.findViewWithTag("RadioButtonCustomized");

        SeekBarCustomized sbc = v.findViewWithTag("SeekBarCustomized");
        if (sbc != null)
            sbc.setTimerTrigger(this);

        mList.add(v);
        rbc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checkedId = ((View) buttonView.getParent()).getId();
                if(isChecked)
                    check(checkedId);

                if (checkedId != mDefaultViewId)
                    retriggerTimer();
            }
        });
    }

    private LinearLayout GetViewFromId(int id)
    {
        for(LinearLayout v : mList)
        {
            if (v.getId() == id)
                return v;
        }
        return null;
    }

    int getCheckedId() {
        return mChecked;
    }

    private void check(int check_id) {
        LinearLayout v = GetViewFromId(mChecked);
        if(v != null) { //allowed because inital set
            RadioButtonCustomized rbc = v.findViewWithTag("RadioButtonCustomized");
            SeekBarCustomized sbc = v.findViewWithTag("SeekBarCustomized");
            rbc.setChecked(false);
            if (sbc != null)
                sbc.setVisibility(View.INVISIBLE);
        }

        mChecked = check_id;

        v = GetViewFromId(mChecked);
        assert v != null;
        RadioButtonCustomized rbc = v.findViewWithTag("RadioButtonCustomized");
        SeekBarCustomized sbc = v.findViewWithTag("SeekBarCustomized");
        rbc.setChecked(true);
        if (sbc != null)
            sbc.setVisibility(View.VISIBLE);
    }

    void bookValueOnCustomer(int pk)
    {
        LinearLayout v = GetViewFromId(mChecked);
        assert v != null;
        RadioButtonCustomized rbc = v.findViewWithTag("RadioButtonCustomized");
        rbc.bookValue(pk);
    }

    void setDefaults(ContentResolver cr, float default_value) {

        mContentResolver = cr;
        mDefaultViewId = R.id.coffee;
        mDefaultValue = default_value;
        mDatabaseIdentifier = "coffee";

        check(mDefaultViewId);
    }
}
