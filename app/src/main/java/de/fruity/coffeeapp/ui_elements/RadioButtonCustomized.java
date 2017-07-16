package de.fruity.coffeeapp.ui_elements;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RadioButton;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.tools.HelperMethods;


/**
 * TODO: document your custom view class.
 */
public class RadioButtonCustomized extends RadioButton {

    private Drawable mIdleDrawable = null;
    private Drawable mIdleSecondDrawable = null;
    private Drawable mSelectedFirstDrawable = null;
    private Drawable mSelectedSecondDrawable = null;
    private boolean mIsLowerSelected = false;

    private float mCurrentValue;
    private String mDatabaseIdentifier;

    private Paint mTextPaint;

    final private Handler mHandler = new Handler();

    public RadioButtonCustomized(Context context) {
        super(context);
        constructor(null, 0);
    }

    public RadioButtonCustomized(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(attrs, 0);
    }

    public RadioButtonCustomized(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        constructor(attrs, defStyle);
    }

    private boolean isNearMin() {
        float min = SqlAccessAPI.getPriceMin(getContext().getContentResolver(),
                mDatabaseIdentifier);
        float max = SqlAccessAPI.getPriceMax(getContext().getContentResolver(),
                mDatabaseIdentifier);

        float to_min_diff = mCurrentValue - min;
        float to_max_diff = max - mCurrentValue;

        return to_min_diff < to_max_diff;
    }

    private void triggerTimer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    float default_price;
                    default_price = SqlAccessAPI.getDefaultPrice(getContext().getContentResolver(),
                            mDatabaseIdentifier);

                    SqlAccessAPI.setCurrentPrice(getContext().getContentResolver(), default_price,
                            mDatabaseIdentifier);
                } catch (NullPointerException ex)
                {
                    Log.i("RadiogroupMerger", "no defaults available");
                }
            }
        };
        if (mHandler != null) {
            mHandler.removeMessages(0); // this id is 100 percent random :P seems
            // to be the default id
            mHandler.postDelayed(runnable, 20000);
        }
    }

    private void constructor(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RadioButtonCustomized, defStyle, 0);

        mDatabaseIdentifier = a.getString(R.styleable.RadioButtonCustomized_databaseIdentifier);

        if (a.hasValue(R.styleable.RadioButtonCustomized_idleDrawable))
            mIdleDrawable = a.getDrawable(R.styleable.RadioButtonCustomized_idleDrawable);
        if (a.hasValue(R.styleable.RadioButtonCustomized_idleSecondDrawable))
            mIdleSecondDrawable = a.getDrawable(R.styleable.RadioButtonCustomized_idleSecondDrawable);
        if (a.hasValue(R.styleable.RadioButtonCustomized_selectedFirstDrawable))
            mSelectedFirstDrawable = a.getDrawable(R.styleable.RadioButtonCustomized_selectedFirstDrawable);
        if (a.hasValue(R.styleable.RadioButtonCustomized_selectedSecondDrawable))
            mSelectedSecondDrawable = a.getDrawable(R.styleable.RadioButtonCustomized_selectedSecondDrawable);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new Paint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setStrokeWidth(3f);

        mCurrentValue = SqlAccessAPI.getPriceByIdentifier(getContext().getContentResolver(),
                mDatabaseIdentifier);
        mIsLowerSelected = isNearMin();

        getContext().getContentResolver().registerContentObserver(
                SqlDatabaseContentProvider.PRODUCT_URI, true, new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        mCurrentValue = SqlAccessAPI.getPriceByIdentifier(getContext().getContentResolver(),
                                mDatabaseIdentifier);
                        mIsLowerSelected = isNearMin();
                        super.onChange(selfChange, uri);
                    }
                });
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isChecked()) {
                float new_value = mCurrentValue + SqlAccessAPI.getPriceStepsize(
                        getContext().getContentResolver(), mDatabaseIdentifier);
                SqlAccessAPI.setCurrentPrice(getContext().getContentResolver(),
                        new_value, mDatabaseIdentifier);

                triggerTimer();
            }

            invalidate(); //should be useless
        }

        return super.onTouchEvent(event);
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        final int OFFSET;
        Drawable drawable;

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        OFFSET = 20;

        // Draw the example drawable on top of the text.
        if (isChecked()) {
            if (mIsLowerSelected || mSelectedSecondDrawable == null)
                drawable = mSelectedFirstDrawable;
            else
                drawable = mSelectedSecondDrawable;

            //draw frame arround button
            canvas.drawLine(0, 0, getWidth(), 0, mTextPaint);
            canvas.drawLine(0, 0, 0, getHeight(), mTextPaint);
            canvas.drawLine(0, getHeight(), getHeight(), getWidth(), mTextPaint);
            canvas.drawLine(getWidth(), 0, getWidth(), getHeight(), mTextPaint);
        } else {
            if (mIsLowerSelected || mIdleSecondDrawable == null)
                drawable = mIdleDrawable;
            else
                drawable = mIdleSecondDrawable;
        }

        super.onDraw(canvas);

        int drawable_left_to_content = ((contentWidth - drawable.getMinimumWidth()) / 2) + OFFSET;
        int drawable_up_to_content = ((contentHeight - drawable.getMinimumHeight()) / 2) + OFFSET;

        drawable.setBounds(
                drawable_left_to_content,
                drawable_up_to_content,
                contentWidth - drawable_left_to_content,
                contentHeight - drawable_up_to_content);
        drawable.draw(canvas);

        if (mCurrentValue != 0.0f)
            setText(String.format("%s€", HelperMethods.roundTwoDecimals(mCurrentValue)));
    }

    public void bookValue(int people_id)
    {
        SqlAccessAPI.bookValueByName(getContext().getContentResolver(), mDatabaseIdentifier,
                people_id);

        HelperMethods.billanceToast(getContext(), people_id, mDatabaseIdentifier);
    }
}
