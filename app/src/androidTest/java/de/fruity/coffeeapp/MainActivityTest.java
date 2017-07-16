package de.fruity.coffeeapp;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import de.fruity.coffeeapp.R;

/**
 * Created by kiwi on 22.06.15.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private final int test_rfid = 85547111;
    private MainActivity mMainActivityTest;
    private RadiogroupMerger mRadioButtonCustomized;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMainActivityTest = getActivity();
//        mRadioButtonCustomized = mMainActivityTest.getRgmForTests();
    }

    public void isCoffeeCheckedTest()
    {
        assertEquals(mRadioButtonCustomized.getChecked(), R.id.coffee);
    }

    public void testCreateUser()
    {
        Intent outgoing = new Intent("android.intent.action.MAIN");
        outgoing.putExtra(ReaderService.TID, test_rfid);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mMainActivityTest.sendBroadcast(outgoing);

        final EditText et = (EditText) mMainActivityTest.findViewById(R.id.newperson_dialog_et_name);
        et.setText("testuser_4711");

        final EditText et_persnumb = (EditText) mMainActivityTest.
                findViewById(R.id.newperson_dialog_et_personalnumber);
        et_persnumb.setText("4711");

        final Button btnSave = (Button) mMainActivityTest.findViewById(R.id.newperson_dialog_btn_save);
        btnSave.callOnClick();


    }



    public void bookCoffee()
    {


    }
}