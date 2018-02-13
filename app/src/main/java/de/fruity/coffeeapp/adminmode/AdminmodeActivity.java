package de.fruity.coffeeapp.adminmode;

import android.content.Context;
import android.content.Intent;
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
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.io.File;
import java.io.IOException;

import de.fruity.coffeeapp.BackupManager;
import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.ReaderService;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;

public class AdminmodeActivity extends FragmentActivity {

    @SuppressWarnings("unused")
    private static final String TAG = AdminmodeActivity.class.getSimpleName();

    private static final Pair[] optionsmenu = new Pair[]{
            new Pair<>(android.R.drawable.ic_menu_delete, R.string.export_database),
            new Pair<>(android.R.drawable.ic_menu_save, R.string.backup_database)
    };
    public static final int SECRET_ADMIN_CODE = Integer.MAX_VALUE;
    private static final String PREFERENCE_KEY_ADMINCODE = "preference_key_admcode";

    private DrawerLayout mDrawerLayout;

    private void createTestUser()
    {
        for (int i = 0; i < 10; i++)
            SqlAccessAPI.createUser(getContentResolver(), "user" + Integer.valueOf(i).toString(), 927139142 + i, 20 + i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReaderService.stopContinuity();
        setContentView(R.layout.activity_adminmode_navigationdrawer);
        FrameLayout framelayout = findViewById(R.id.content_frame);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        ListView mDrawerList = findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new NavigationDrawerWithIcon(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (((Integer) optionsmenu[i].second)) {
                    case R.string.export_database:

                        File outputDir = getApplicationContext().getCacheDir(); // context being the Activity pointer
                        File outputFile;
                        try {
                            outputFile = File.createTempFile("coffeeDB", "csv", outputDir);

                            Intent intent = SqliteDatabase.backupDatabaseCSV(outputFile, getContentResolver());
                            if (intent != null) {
                                startActivity(Intent.createChooser(intent, "Send mail..."));
                                resetValuesInDatabase();
                                Toast.makeText(getApplicationContext(), "All done :)", Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        break;
                    case R.string.backup_database:
//                        createTestUser();
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
        RelativeLayout myView = (RelativeLayout) inflater.inflate(R.layout.activity_adminmode, vg, false);
        framelayout.addView(myView);

        // Set up the action bar.// ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        ViewPager viewPager = findViewById(R.id.pager_admin_activity);
        viewPager.setAdapter(new DemoCollectionPagerAdapter(getSupportFragmentManager()));
        PagerSlidingTabStrip tabsStrip = findViewById(R.id.pager_title_strip);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);


        FloatingActionButton fab = findViewById(R.id.fab_admin);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDrawerLayout.openDrawer(Gravity.END);
                return true;
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
        private ViewHolder mViewHolder = null;

        NavigationDrawerWithIcon(Context context) {
            super(context, R.layout.activity_adminmode_navigationdrawer_object);
            this.context = context;
        }


        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
                //noinspection deprecation
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
