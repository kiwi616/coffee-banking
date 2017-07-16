package de.fruity.coffeeapp;

import android.content.ContentResolver;
import android.os.Handler;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.ui_elements.RadioButtonCustomized;

public class RadiogroupMerger {
    private RadioGroup radiogroup1;
    private RadioGroup radiogroup2;
    private OnCheckedChangeListener globalListener;

    private String mDatabaseIdentifier;
    private float mDefaultValue;
    private int mDefaultViewId;
    private ContentResolver mContentResolver;

    final private Handler mHandler = new Handler();

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
        if (mHandler != null) {
            mHandler.removeMessages(0); // this id is 100 percent random :P seems
            // to be the default id
            mHandler.postDelayed(runnable, 15000);
        }
    }

    public RadiogroupMerger(RadioGroup r1, RadioGroup r2) {
        radiogroup1 = r1;
        radiogroup2 = r2;
        globalListener = null;

        radiogroup1.clearCheck(); // this is so we can start fresh, with no selection on both RadioGroups
        radiogroup2.clearCheck();

        radiogroup1.setOnCheckedChangeListener(listener1);
        radiogroup2.setOnCheckedChangeListener(listener2);
    }

    public int getCheckedId() {
        int chkId1 = radiogroup1.getCheckedRadioButtonId();
        int chkId2 = radiogroup2.getCheckedRadioButtonId();
        return (chkId1 == -1 ? chkId2 : chkId1);
    }

    private boolean isIdGroupOfRg1(int id) {
        return radiogroup1.findViewById(id) != null;
    }

    public void check(int check_id) {
        if (isIdGroupOfRg1(check_id))
            radiogroup1.check(check_id);
        else
            radiogroup2.check(check_id);
    }

    public RadioButtonCustomized getChecked() {
        RadioButtonCustomized return_handle;
        if (isIdGroupOfRg1(getCheckedId()))
            return_handle = (RadioButtonCustomized) radiogroup1.findViewById(getCheckedId());
        else
            return_handle = (RadioButtonCustomized) radiogroup2.findViewById(getCheckedId());

        return return_handle;
    }

    private OnCheckedChangeListener listener1 = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                radiogroup2.setOnCheckedChangeListener(null); // remove the listener before clearing so we don't throw that stackoverflow exception(like Vladimir Volodin pointed out)
                radiogroup2.clearCheck(); // clear the second RadioGroup!
                radiogroup2.setOnCheckedChangeListener(listener2); //reset the listener


                if (group.getCheckedRadioButtonId() != mDefaultViewId)
                    retriggerTimer();

                if (globalListener != null)
                    globalListener.onCheckedChanged(group, checkedId);
            }
        }
    };

    private OnCheckedChangeListener listener2 = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                radiogroup1.setOnCheckedChangeListener(null);
                radiogroup1.clearCheck();
                radiogroup1.setOnCheckedChangeListener(listener1);

                if (group.getCheckedRadioButtonId() != mDefaultViewId)
                    retriggerTimer();

                if (globalListener != null)
                    globalListener.onCheckedChanged(group, checkedId);
            }
        }
    };


    public void setDefaults(ContentResolver cr, int default_view,
                            float default_value, String default_datbaseident) {

        mContentResolver = cr;
        mDefaultViewId = default_view;
        mDefaultValue = default_value;
        mDatabaseIdentifier = default_datbaseident;

        check(mDefaultViewId);
    }
}
