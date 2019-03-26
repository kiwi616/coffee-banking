package de.fruity.coffeeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import de.fruity.coffeeapp.adminmode.AdminmodeActivity;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;
import de.fruity.coffeeapp.tools.HelperMethods;
import de.fruity.coffeeapp.ui_elements.CustomToast;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private BroadcastReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float default_coffee;
        ArrayAdapter<String> dla;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getActionBar() != null)
            getActionBar().hide();

        setContentView(R.layout.activity_main_navigationdrawer);
        FrameLayout framelayout = findViewById(R.id.fl_adminactivity);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.lv_admin_drawer);

        ViewGroup vg = findViewById(R.id.rl_activity_main);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View myView = inflater.inflate(R.layout.activity_main_test, vg, false);
        framelayout.addView(myView);

        // Set the adapter for the list view
        dla = new ArrayAdapter<>(this, R.layout.activity_main_navigationdrawer_object);
        for (String s : SqlAccessAPI.getGroupNamesFromDatabase(getContentResolver()))
            dla.add(s);
        mDrawerList.setAdapter(dla);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        default_coffee = SqlAccessAPI.getPriceMin(getContentResolver(), "coffee");

        RadiogroupMerger mRadiogroupMerger = new RadiogroupMerger();
        mRadiogroupMerger.addView((LinearLayout) findViewById(R.id.candy));
        mRadiogroupMerger.addView((LinearLayout) findViewById(R.id.coffee));
        mRadiogroupMerger.addView((LinearLayout) findViewById(R.id.can));
        mRadiogroupMerger.addView((LinearLayout) findViewById(R.id.balance));
        mRadiogroupMerger.addView((LinearLayout) findViewById(R.id.beer));
        mRadiogroupMerger.addView((LinearLayout) findViewById(R.id.meat));

        mRadiogroupMerger.setDefaults(getContentResolver(), R.id.coffee, default_coffee, "coffee");

        FloatingActionButton fab = findViewById(R.id.fab_admin);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SqlAccessAPI.isUserDbEmpty(getContentResolver())){
                    Dialog d = HelperMethods.createNewUser(MainActivity.this, null, null);
                    d.show();
                }
                else {
                    dialogWaitForAdminRfid();
                }
            }
        });

        Button select_by_person_Button = findViewById(R.id.btn_main);
        // Initialize get features button
        select_by_person_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEnterPersonal();
            }
        });

        mReceiver = new RFIDReaderReceiver(mRadiogroupMerger);

        Intent i = new Intent(this, ReaderService.class);
        startService(i);
    }


    protected void onResume() {
        super.onResume();
        // registering our receiver
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
        this.registerReceiver(mReceiver, intentFilter);
        ReaderService.startContinuity();
    }

    public void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            groupMode(position + 1);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }


    private void dialogWaitForAdminRfid() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_enter_personalnumber);
        dialog.setTitle(R.string.enter_personalnumber);

        // set the custom dialog components - text, image and button

        Button cancelButton = dialog.findViewById(R.id.personalnumber_dialog_btn_cancel);
        final Button btnSave = dialog.findViewById(R.id.personalnumber_dialog_btn_save);

        final EditText et_personalnumber = dialog.findViewById(R.id.personalnumber_dialog_et);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int persno;

                if (!HelperMethods.isPersonalNumberValid(et_personalnumber.getText().toString()))
                {
                    customToast(getText(R.string.no_personalnumber_number).toString(), 800);
                    return;
                }

                persno = Integer.parseInt(et_personalnumber.getText().toString());

                if (SqlAccessAPI.isAdminByPersonalnumber(getContentResolver(), persno)) {

                    dialog.dismiss();
                    Intent startAdminMode = new Intent(getApplication(), AdminmodeActivity.class);
                    startActivity(startAdminMode);
                }
                else
                {
                    customToast(getText(R.string.enter_admin_code).toString(), 800);
                }
            }
        });

        et_personalnumber.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    return btnSave.callOnClick();
                }
                return false;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void dialogEnterPersonal() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_enter_personalnumber);
        dialog.setTitle(R.string.enter_personalnumber);

        // set the custom dialog components - text, image and button

        Button cancelButton = dialog.findViewById(R.id.personalnumber_dialog_btn_cancel);
        final Button btnSave = dialog.findViewById(R.id.personalnumber_dialog_btn_save);

        final EditText et_personalnumber = dialog.findViewById(R.id.personalnumber_dialog_et);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int persno;

                if (!HelperMethods.isPersonalNumberValid(et_personalnumber.getText().toString()))
                {
                    customToast(getText(R.string.no_personalnumber_number).toString(), 800);
                    return;
                }

                persno = Integer.parseInt(et_personalnumber.getText().toString());
                dialog.dismiss();


                Cursor rfidCursor = getContentResolver().query(SqlDatabaseContentProvider.CONTENT_URI, null,
                        SqliteDatabase.COLUMN_PERSONAL_NUMBER + " =  ?", new String[]{Integer.toString(persno)}, null);


                if (rfidCursor != null && rfidCursor.moveToFirst()) {
                    String s = rfidCursor.getString(rfidCursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_NAME));

                    showIsThisYourNameDialog(s, persno);

                    rfidCursor.close();
                } else {
                    dialog.dismiss();
                    Dialog d = HelperMethods.createNewUser(MainActivity.this, persno, null);
                    d.show();
//                    customToast(getText(R.string.personalnumber_not_found).toString(), 2500);
                }
            }
        });

        et_personalnumber.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    btnSave.callOnClick();
                    return true;
                }
                return false;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void groupMode(int group_id) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_groupmode);
        dialog.setTitle(R.string.groupmode);

        // set the custom dialog components - text, image and button
        ListView lvData = dialog.findViewById(R.id.groupmode_lv_dialog);
        List<GroupmodeData> list_names = SqlAccessAPI.getNamesInGroup(getContentResolver(), group_id);

        final CheckboxListAdapter adapter = new CheckboxListAdapter(getLayoutInflater());

        lvData.setAdapter(adapter);
        for (GroupmodeData data : list_names)
            adapter.add(data);

        dialog.setCancelable(false);
        Button cancelButton = dialog.findViewById(R.id.groupmode_btn_cancel);
        Button btnSave = dialog.findViewById(R.id.groupmode_btn_save);
        btnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                for (GroupmodeData gd : adapter.getSelected()) {
                    SqlAccessAPI.bookCoffee(getContentResolver(), gd.getID());
                    sb.append(gd.getName());
                    sb.append(", ");
                }

                if (TextUtils.isEmpty(sb.toString()))
                    Log.i(TAG, "no one selected");
                else
                    customToast(sb.toString().substring(0, sb.toString().length() -2) + " \n" +
                        getText(R.string.group_booked).toString(), 2500);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showIsThisYourNameDialog(String name, final int persno) {

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.is_correct_name);
        adb.setMessage(getText(R.string.hello) + " " + name + getText(R.string.comma_is_correct_name));
        adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent outgoing = new Intent("android.intent.action.MAIN");
                outgoing.putExtra(ReaderService.PERSONAL_ID, persno);
                sendBroadcast(outgoing);
            }
        });

        adb.show();
    }

    private void customToast(String message, int duration) {
        new CustomToast(getApplicationContext(), message, duration);
    }


}
