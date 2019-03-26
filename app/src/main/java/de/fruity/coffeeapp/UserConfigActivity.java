package de.fruity.coffeeapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;
import de.fruity.coffeeapp.tools.HelperMethods;
import de.fruity.coffeeapp.ui_elements.CustomToast;

public class UserConfigActivity extends Activity {
    public static final String PK_USER = "pk_user";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReaderService.stopContinuity();
        setContentView(R.layout.activity_userconfig);

        final TextInputLayout floatingUsernameLabel = findViewById(R.id.til_userconfig_username);
        final TextInputLayout floatingPersnoLabel = findViewById(R.id.til_userconfig_personalnumber);
        final EditText et_name = findViewById(R.id.et_userconfig_username);
        final EditText et_persno = findViewById(R.id.et_userconfig_personalnumber);
        final EditText et_rfid = findViewById(R.id.et_userconfig_rfid);
        TextView tv_creation_date = findViewById(R.id.tv_userconfig_dateofcreation);
        Button btn_save = findViewById(R.id.btn_userconfig_apply);
        final int pk_user = getIntent().getIntExtra(UserConfigActivity.PK_USER, -1);
        et_rfid.setEnabled(false);

        HelperMethods.setupFloatingLabelErrorUsername(getApplicationContext(), floatingUsernameLabel);
        HelperMethods.setupFloatingLabelErrorPersonalNumber(getApplicationContext(), floatingPersnoLabel, pk_user);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!HelperMethods.isPersonalNumberValid(et_persno.getText().toString()))
                {
                    new CustomToast(getApplicationContext(), getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }
                if (HelperMethods.isNameSurnameTupleInvalid(et_name.getText().toString()))
                {
                    new CustomToast(getApplicationContext(), getText(R.string.error_invalid_username).toString(), 2000);
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
                tv_creation_date.setText( getText(R.string.user_creation_date) + ": " +
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
