package de.fruity.coffeeapp;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.fruity.coffeeapp.adminmode.AdminmodeActivity;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.tools.HelperMethods;
import de.fruity.coffeeapp.ui_elements.CustomToast;

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
        int pk_id = SqlAccessAPI.getPeopleIdByRFID(context.getContentResolver(), rfidNumber);


        if (position == R.id.admin) {
            try {
                if (SqlAccessAPI.isAdmin(context.getContentResolver(), rfidNumber)
                        || rfidNumber == AdminmodeActivity.SECRET_ADMIN_CODE) {
                    Intent startAdminMode = new Intent(context, AdminmodeActivity.class);
                    context.startActivity(startAdminMode);
                }
            } catch (Exception ignored) {
            }
        } else {

            try {
                if (position != R.id.bilance)
                    mRadiogroupMerger.getChecked().bookValue(pk_id);
                else
                    showBalance(context, rfidNumber);

            } catch (IllegalArgumentException | SQLiteConstraintException ia_ex) {
                Dialog d = HelperMethods.createNewUser(context, null, new Integer(rfidNumber));
                d.show();
            }
        }


        Log.i(TAG, " id received " + rfidNumber);

    }

    private void showBalance(Context context, int tid) {
        long pk_id = SqlAccessAPI.getPeopleIdByRFID(context.getContentResolver(), tid);

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_billance);
        dialog.setTitle(SqlAccessAPI.getName(context.getContentResolver(), pk_id));

        TextView tv_coffee = (TextView) dialog.findViewById(R.id.value_coffee_billance_dialog);
        TextView tv_candy = (TextView) dialog.findViewById(R.id.value_candy_billance_dialog);
        TextView tv_metcan = (TextView) dialog.findViewById(R.id.value_metcan_billance_dialog);
        TextView tv_beer = (TextView) dialog.findViewById(R.id.value_beer_billance_dialog);

        tv_coffee.setText(String.format("%s€", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getCoffeeValueFromPerson(context.getContentResolver(), pk_id))));
        tv_candy.setText(String.format("%s€", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getCandyValueFromPerson(context.getContentResolver(), pk_id))));
        tv_metcan.setText(String.format("%s€", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getCanValueFromPerson(context.getContentResolver(), pk_id))));
        tv_beer.setText(String.format("%s€", HelperMethods.roundTwoDecimals(
                SqlAccessAPI.getBeerValueFromPerson(context.getContentResolver(), pk_id))));

        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        };
        handler.postDelayed(runnable, 10000);
        dialog.show();

    }
}
