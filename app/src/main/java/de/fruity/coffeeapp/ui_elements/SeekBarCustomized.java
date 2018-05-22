package de.fruity.coffeeapp.ui_elements;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.widget.SeekBar;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.RadiogroupMerger;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.tools.HelperMethods;

public class SeekBarCustomized extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener{
    private ContentResolver mContentResolver;
    private RadiogroupMerger mRadiogroupMerger;
    private String mDatabaseIdentifier;


    public SeekBarCustomized(Context context) {
        super(context);
        constructor(context, null, 0);
    }

    public SeekBarCustomized(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(context, attrs, 0);
    }

    public SeekBarCustomized(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor(context, attrs, defStyleAttr);
    }

    public void setTimerTrigger(RadiogroupMerger rgm)
    {
        mRadiogroupMerger = rgm;
    }

    private void constructor(Context context, AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RadioButtonCustomized, defStyle, 0);

        setTag("SeekBarCustomized");

        mDatabaseIdentifier = a.getString(R.styleable.RadioButtonCustomized_databaseIdentifier);

        if ( mDatabaseIdentifier == null)
            return;
        mContentResolver = context.getContentResolver();

        // sb_candy TODO onClicklistener
//        setMin(HelperMethods.roundAndConvert(
//                SqlAccessAPI.getPriceMin(mContentResolver, mDatabaseIdentifier)));
        setMax(HelperMethods.roundAndConvert(
                SqlAccessAPI.getPriceMax(mContentResolver, mDatabaseIdentifier)));
        setProgress(HelperMethods.roundAndConvert(
                SqlAccessAPI.getPriceByIdentifier(mContentResolver, mDatabaseIdentifier)));

        setOnSeekBarChangeListener(this);

        mContentResolver.registerContentObserver(SqlDatabaseContentProvider.PRODUCT_URI, true,
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        float value = SqlAccessAPI.getPriceByIdentifier(mContentResolver, mDatabaseIdentifier);
                        float candy_val = ((float) (getProgress()) / 100.0f);

                        if (!HelperMethods.roundTwoDecimals(candy_val).equals(
                                HelperMethods.roundTwoDecimals(value)))
                            setProgress(HelperMethods.roundAndConvert(value));
                        super.onChange(selfChange, uri);
                    }
                });
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mRadiogroupMerger.retriggerTimer();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mRadiogroupMerger.retriggerTimer();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mRadiogroupMerger.retriggerTimer();

        if (fromUser) {
            int step_size = HelperMethods.roundAndConvert(SqlAccessAPI.
                    getPriceStepsize(mContentResolver, mDatabaseIdentifier));

//            progress += (getMax()/step_size);
            if (progress % (step_size) == 0) {
                float candy_val = (progress / 100.0f);
                SqlAccessAPI.setCurrentPrice(mContentResolver, candy_val, mDatabaseIdentifier);
            }
        }
    }
}
