package de.fruity.coffeeapp.adminmode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.io.File;
import java.io.IOException;

import de.fruity.coffeeapp.BackupManager;
import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.ReaderService;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;

public class AdminmodeActivity extends FragmentActivity implements OnRequestPermissionsResultCallback {

    @SuppressWarnings("unused")
    private static final String TAG = AdminmodeActivity.class.getSimpleName();

    private static final Pair[] optionsmenu = new Pair[]{
            new Pair<>(android.R.drawable.ic_menu_delete, R.string.export_database),
            new Pair<>(android.R.drawable.ic_menu_save, R.string.backup_database)
    };
    private static final int WRITE_EXTERNAL_STORAGE = 1;
    private DrawerLayout mDrawerLayout;




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Intent intent = null;
                try {
                    intent = SqliteDatabase.backupDatabaseCSV(new File(Environment.getExternalStorageDirectory()
                            + "/" + "coffeeDB.csv"), getContentResolver());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (intent != null) {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                    resetValuesInDatabase();
                    Toast.makeText(getApplicationContext(), "All done :)", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "You denied access backup failed", Toast.LENGTH_LONG).show();
                // Permission request was denied.
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    /**
     * Requests the {@link android.Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            //TODO write some fancy please do it dialog
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);

        } else {
//            Snackbar.make(mLayout, R.string.camera_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReaderService.stopContinuity();
        setContentView(R.layout.activity_adminmode_navigationdrawer);
        FrameLayout framelayout = findViewById(R.id.fl_adminactivity);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        ListView mDrawerList = findViewById(R.id.lv_admin_drawer);
        mDrawerList.setAdapter(new NavigationDrawerWithIcon(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (((Integer) optionsmenu[i].second)) {
                    case R.string.export_database:

                        try {
//                            outputFile = File.createTempFile("coffeeDB", "csv", outputDir);

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {

                                Intent intent = SqliteDatabase.backupDatabaseCSV(new File(Environment.getExternalStorageDirectory()
                                        + "/" + "coffeeDB.csv"), getContentResolver());
                                if (intent != null) {
                                    startActivity(Intent.createChooser(intent, "Send mail..."));
                                    resetValuesInDatabase();
                                    Toast.makeText(getApplicationContext(), "All done :)", Toast.LENGTH_LONG).show();
                                }
                            }
                            else
                            {
                                requestCameraPermission();
                            }
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        break;
                    case R.string.backup_database:
                        try {

                            String backupname = BackupManager.BackupDatabase(getContentResolver());
                            Toast.makeText(getApplicationContext(), "Backup to file " + backupname, Toast.LENGTH_LONG).show();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawer(Gravity.END);
            }
        });

        ViewGroup vg = findViewById(R.id.rl_activity_adminmode);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View myView = inflater.inflate(R.layout.activity_adminmode, vg, false);
        framelayout.addView(myView);

        // Set up the action bar.// ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        ViewPager viewPager = findViewById(R.id.pager_admin_activity);
        viewPager.setAdapter(new DemoCollectionPagerAdapter(getSupportFragmentManager()));
        PagerSlidingTabStrip tabsStrip = findViewById(R.id.pager_title_strip);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);


        FloatingActionButton fab = findViewById(R.id.fab_adminactivity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.END);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReaderService.startContinuity();
    }

    private static class ViewHolder {
        private TextView friendsname;
        private ImageView thumb_image;
    }

    private class NavigationDrawerWithIcon extends ArrayAdapter {
        private Context context;

        NavigationDrawerWithIcon(Context context) {
            super(context, R.layout.activity_adminmode_navigationdrawer_object);
            this.context = context;
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder mViewHolder;
            if (convertView == null) {
                mViewHolder = new ViewHolder();

                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = vi.inflate(R.layout.activity_adminmode_navigationdrawer_object, parent, false);
                mViewHolder.friendsname = convertView.findViewById(R.id.tv_drawerobject_adminactivity); // title
                mViewHolder.thumb_image = convertView.findViewById(R.id.iv_drawerobject_adminactivity); // thumb image

                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }


            mViewHolder.friendsname.setText(context.getText((Integer) optionsmenu[position].second));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mViewHolder.thumb_image.setImageDrawable(context.getResources().getDrawable((Integer) optionsmenu[position].first, context.getTheme()));
            } else {
                mViewHolder.thumb_image.setImageDrawable(context.getResources().getDrawable((Integer) optionsmenu[position].first));
            }


            return convertView;
        }

        @Override
        public int getCount() {
            return optionsmenu.length;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class DemoCollectionPagerAdapter extends FragmentPagerAdapter {

        DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return new AdminmodePeopleFragment();
                case 1:
                    return new AdminmodeGroupsFragment();
                case 2:
                    return new AdminmodeAdminsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getText(R.string.people);
                case 1:
                    return getString(R.string.groupmode);
                case 2:
                    return getString(R.string.admins);
            }
            return null;
        }
    }


    private void resetValuesInDatabase() {
        getContentResolver().delete(SqlDatabaseContentProvider.VALUE_URI, null, null);
    }

}
