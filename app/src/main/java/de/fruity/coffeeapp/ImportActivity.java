package de.fruity.coffeeapp;

import android.app.Activity;
import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import de.fruity.coffeeapp.database.SqliteDatabase;
import de.fruity.coffeeapp.ui_elements.CustomToast;

/**
 * A login screen that offers login via email/password.
 */
public class ImportActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        Intent startIntent = getIntent();
        final Uri fileUri = startIntent.getData();

        if (fileUri == null) {
            finish();
            return;
        }

        Button btn_ok = (Button) findViewById(R.id.btn_activity_import_ok);
        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomToast ct;

                switch (SqliteDatabase.importCsv(fileUri, getApplicationContext())) {
                    case -2:
                        ct = new CustomToast(getApplicationContext(), getText(R.string.all_deleted_you_are_fucked).toString(), 1500);
                        break;
                    case -1:
                        ct = new CustomToast(getApplicationContext(), getText(R.string.shit_happens).toString(), 1500);
                        break;
                    case 0:
                        ct = new CustomToast(getApplicationContext(), getText(R.string.all_fine).toString(), 1500);
                        break;
                    default:
                        ct = new CustomToast(getApplicationContext(), getText(R.string.all_deleted_you_are_fucked).toString(), 1500);
                }

                ct.show();
                finish();
            }

        });

        Button btn_cancel = (Button) findViewById(R.id.btn_activity_import_cancel);
        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

