package de.fruity.coffeeapp;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.GraphicalView;

import de.fruity.coffeeapp.adminmode.AdminmodeActivity;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.tools.HelperMethods;

public class RFIDReaderReceiver extends BroadcastReceiver {

    static final String TAG = RFIDReaderReceiver.class.getSimpleName();

    private RadiogroupMerger mRadiogroupMerger;

    public RFIDReaderReceiver(RadiogroupMerger rgm) {
        mRadiogroupMerger = rgm;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int position = mRadiogroupMerger.getCheckedId();
        int rfidNumber = intent.getIntExtra(ReaderService.TID, 0);
        int pk_id;
        if (rfidNumber != 0) {
            pk_id = SqlAccessAPI.getPeopleIdByRFID(context.getContentResolver(), rfidNumber);
        } else {
            int persno = intent.getIntExtra(ReaderService.PERSONAL_ID, 0);
            pk_id = SqlAccessAPI.getPeopleIdByPersonalnumber(context.getContentResolver(), persno);
        }

        try {
            mRadiogroupMerger.retriggerTimer();
            if (position != R.id.balance)
                mRadiogroupMerger.bookValueOnCustomer(pk_id);
            else
                showBalance(context, pk_id);

        } catch (IllegalArgumentException | SQLiteConstraintException ia_ex) {
            Dialog d = HelperMethods.createNewUser(context, null, rfidNumber);
            d.show();
        }


        Log.i(TAG, " id received " + rfidNumber);
    }

    private void showBalance(final Context context, final int pk_id) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_billance);
        dialog.setTitle(SqlAccessAPI.getName(context.getContentResolver(), pk_id));

        LinearLayout chartContainer = dialog.findViewById(R.id.chart);
        GraphicalView chart = HelperMethods.createLineChart(context, pk_id);
        chartContainer.addView(chart);

        FloatingActionButton fab = dialog.findViewById(R.id.fab_config_user);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startAdminMode = new Intent(context, UserConfigActivity.class);
                startAdminMode.putExtra(UserConfigActivity.PK_USER, pk_id);
                context.startActivity(startAdminMode);
            }
        });

        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        };
        handler.postDelayed(runnable, 15000);
        dialog.show();

    }
}
