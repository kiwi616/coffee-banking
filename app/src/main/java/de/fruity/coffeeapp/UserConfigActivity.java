package de.fruity.coffeeapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import de.fruity.coffeeapp.adminmode.AdminmodeAdminsFragment;
import de.fruity.coffeeapp.adminmode.AdminmodeGroupsFragment;
import de.fruity.coffeeapp.adminmode.AdminmodePeopleFragment;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;
import de.fruity.coffeeapp.tools.HelperMethods;
import de.fruity.coffeeapp.ui_elements.CustomToast;

public class UserConfigActivity extends Activity {
    public static final String PK_USER = "pk_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReaderService.stopContinuity();
        setContentView(R.layout.activity_userconfig);
        final EditText et_name = findViewById(R.id.et_username);
        final EditText et_rfid = findViewById(R.id.et_rfid);
        final EditText et_persno = findViewById(R.id.et_personal_number);
        EditText et_creation_date = findViewById(R.id.et_creation_date);
        Button btn_save = findViewById(R.id.btn_activity_userconfig_save);
        final int pk_user = getIntent().getIntExtra(UserConfigActivity.PK_USER, -1);

        et_persno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                boolean valid;

                valid = HelperMethods.isPersonalNumberValid(editable.toString());

                if (valid) {
                    Cursor cursor = getContentResolver().query(
                            SqlDatabaseContentProvider.CONTENT_URI,
                            null,
                            SqliteDatabase.COLUMN_PERSONAL_NUMBER + "= ?",
                            new String[]{editable.toString()}, null);

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            valid = false;
                        }
                        cursor.close();
                    }
                }

                if (!valid)
                    et_persno.setTextColor(Color.RED);
                else
                    et_persno.setTextColor(Color.BLACK);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!HelperMethods.isPersonalNumberValid(et_persno.getText().toString()))
                {
                    new CustomToast(getApplicationContext(), getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                ContentValues cv = new ContentValues();
                Uri uri = Uri.parse(SqlDatabaseContentProvider.CONTENT_URI + "/" + pk_user);
                cv.put(SqliteDatabase.COLUMN_NAME, et_name.getText().toString());
                cv.put(SqliteDatabase.COLUMN_PERSONAL_NUMBER, et_persno.getText().toString());
                cv.put(SqliteDatabase.COLUMN_RFID, et_rfid.getText().toString());

                try {
                    getContentResolver().update(uri, cv, null, null);
                    new CustomToast(getApplicationContext(),
                            getApplicationContext().getText(R.string.all_fine).toString(),
                            1000);
                } catch (SQLiteConstraintException ex) {
                    new CustomToast(getApplicationContext(),
                            getApplicationContext().getText(R.string.personalnumber_in_use).toString(),
                            1000);
                }
            }
        });

        if ( pk_user > 0)
        {
            Integer id = pk_user;
            Cursor cursor = getContentResolver().
                    query(SqlDatabaseContentProvider.CONTENT_URI, null,
                    SqliteDatabase.COLUMN_ID + " =  ?",
                    new String[] {id.toString()}, null);
            if (cursor != null && cursor.moveToFirst()) {
                et_name.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_NAME)));
                et_rfid.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_RFID)));
                et_persno.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_PERSONAL_NUMBER)));
                et_creation_date.setText(
                        cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_DATE)));

                cursor.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReaderService.startContinuity();
    }


}
