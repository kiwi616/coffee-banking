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
                    ReaderService.stopContinuity();
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
                createNewUser(context, rfidNumber);
            }
        }


        Log.i(TAG, " id received " + rfidNumber);

    }

    private void createNewUser(final Context context, final int tid) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_new_person);
        dialog.setTitle(R.string.save_hint_enter_name);

        // set the custom dialog components - text, image and button
        final Button cancelButton = (Button) dialog.findViewById(R.id.newperson_dialog_btn_cancel);
        final Button btnSave = (Button) dialog.findViewById(R.id.newperson_dialog_btn_save);
        final EditText et = (EditText) dialog.findViewById(R.id.newperson_dialog_et_name);
        final EditText et_personalnumber = (EditText) dialog.findViewById(R.id.newperson_dialog_et_personalnumber);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int personalnumber;

                try {
                    personalnumber = Integer.parseInt(et_personalnumber.getText().toString());
                } catch (NumberFormatException ex) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }
                if (String.valueOf(personalnumber).length() < 3) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                try {
                    SqlAccessAPI.createUser(context.getContentResolver(), et.getText().toString(), tid, personalnumber);
                    if (SqlAccessAPI.isAdmin(context.getContentResolver(), tid))
                        createAdminCode(context);
                } catch (SQLiteConstraintException ex) {
                    new CustomToast(context,
                            context.getText(R.string.personalnumber_in_use).toString(), Toast.LENGTH_LONG);
                    return;
                }

                new CustomToast(context,
                        context.getText(R.string.user_created).toString(), Toast.LENGTH_LONG);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void createAdminCode(final Context context) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_new_admincode);
        dialog.setTitle(R.string.enter_admin_code);
        dialog.setCancelable(false);

        // set the custom dialog components - text, image and button
        final Button btnSave = (Button) dialog.findViewById(R.id.newperson_dialog_btn_save);
        final EditText et = (EditText) dialog.findViewById(R.id.et_admincode);
        final EditText et_reentered = (EditText) dialog.findViewById(R.id.et_admincode_reenter);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int first_et_value;
                int second_et_value;

                try {
                    first_et_value = Integer.parseInt(et.getText().toString());
                    second_et_value = Integer.parseInt(et_reentered.getText().toString());
                } catch (NumberFormatException ex) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                if(first_et_value != second_et_value)
                {
                    new CustomToast(context,
                            context.getText(R.string.two_field_dont_match).toString(), 2000);
                    return;
                }

                if (String.valueOf(et.getText().toString()).length() != 4) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                AdminmodeActivity.saveAdminCode(context, second_et_value);
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AdminmodeActivity.saveAdminCode(context, 4711);
                new CustomToast(context,
                        context.getText(R.string.admincode_set_to_default).toString(), 5000);
                dialog.dismiss();
            }
        });

        dialog.show();
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
