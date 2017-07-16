package de.fruity.coffeeapp.ui_elements;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.SeekBar;

import de.fruity.coffeeapp.RadiogroupMerger;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.tools.HelperMethods;

public class SeekBarCustomized extends SeekBar implements SeekBar.OnSeekBarChangeListener{
    private ContentResolver mContentResolver;
    private RadiogroupMerger mRadiogroupMerger;
    private String mDatabaseIdentifier;


    public SeekBarCustomized(Context context) {
        super(context);
        mContentResolver = context.getContentResolver();
    }

    public SeekBarCustomized(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContentResolver = context.getContentResolver();
    }

    public SeekBarCustomized(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContentResolver = context.getContentResolver();
    }

    public void init(RadiogroupMerger rgm, String database_ident) {
        mRadiogroupMerger = rgm;
        mDatabaseIdentifier = database_ident;

        // sb_candy TODO onClicklistener
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
