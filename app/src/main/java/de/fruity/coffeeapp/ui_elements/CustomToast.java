package de.fruity.coffeeapp.ui_elements;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast extends Toast {


    public CustomToast(Context context, String message, int duration) {
        super(context);

        LinearLayout layout = new LinearLayout(context);

        layout.setBackgroundResource(android.R.color.background_dark);
        TextView tv = new TextView(context);
        // set the TextView properties like color, size etc
        tv.setTextSize(34);
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        if (Build.VERSION.SDK_INT < 23)
            tv.setTextColor(context.getResources().getColor(android.R.color.white));
        else
            tv.setTextColor(context.getResources().getColor(android.R.color.white, context.getTheme()));
        tv.setText(message);
        layout.addView(tv);

        // write
        // "this" if you are an Activity

        setDuration(Toast.LENGTH_LONG);

        // Set The layout as Toast View
        setView(layout);

        // Position you toast here toast position is 50 dp from bottom you can
        // give any integral value
        setGravity(Gravity.TOP, 0, 50);

        show();

        Handler handler = new Handler();
        handler.removeMessages(0); // this id is 100 percent random :P seems
        // to be the default id
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancel();
            }

        }, duration);
    }
}
