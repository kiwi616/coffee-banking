package de.fruity.coffeeapp.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.fruity.coffeeapp.GroupmodeData;
import de.fruity.coffeeapp.tools.HelperMethods;

public class SqlAccessAPI {
    static final String TAG = SqlAccessAPI.class.getSimpleName();

    static private float valueQueryFor(ContentResolver cr, int people_id, String value_type)
    {
        Uri uri = Uri.parse(SqlDatabaseContentProvider.CONTENT_URI + "/" + people_id);
        float coffeeValue = 0;

        Cursor cursor = cr.query(
                uri, new String[]{"sum( " + SqliteDatabase.COLUMN_VE_VALUE + ")"},
                SqliteDatabase.COLUMN_PRODUCT_KIND + "= ?", new String[]{value_type}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                coffeeValue = cursor.getFloat(0);
            }
            cursor.close();
        }
        return coffeeValue;
    }

    static private void insertValue(ContentResolver cr, int people_id, String value_type, boolean negate)
    {
        Uri uri;
        if (negate)
            uri = Uri.parse(SqlDatabaseContentProvider.VALUE_URI + "/-" + value_type);
        else
            uri = Uri.parse(SqlDatabaseContentProvider.VALUE_URI + "/" + value_type);

        ContentValues cv = new ContentValues();
        Log.d(TAG, "value " + value_type + " inserted");

        cv.put(SqliteDatabase.COLUMN_VE_PEOPLE_ID, people_id);

        cr.insert(uri, cv);
    }

    static private float queryPrices(ContentResolver cr, String value_type, String column_type)
    {
        float value = 0;

        Cursor cursor = cr.query(
                SqlDatabaseContentProvider.PRODUCT_URI,
                new String[]{column_type},
                SqliteDatabase.COLUMN_PRODUCT_KIND + "= ?", new String[]{value_type}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                value = cursor.getFloat(0);
            }
            cursor.close();
        }
        return value;
    }

    static private int updatePrices(ContentResolver cr, String value_type,
                                     String column_type, float new_price) {
        ContentValues cv = new ContentValues();
        Uri uri = Uri.parse(SqlDatabaseContentProvider.PRODUCT_URI + "/" + value_type);
        String[] available = {SqliteDatabase.COLUMN_PRODUCT_CURVALUE,
                SqliteDatabase.COLUMN_PRODUCT_VALUE_MIN,
                SqliteDatabase.COLUMN_PRODUCT_VALUE_STEPSIZE,
                SqliteDatabase.COLUMN_PRODUCT_VALUE_MAX,
                SqliteDatabase.COLUMN_PRODUCT_VALUE_DEFAULT};
        //check columns
        if (column_type != null) {
            HashSet<String> requestedColumns = new HashSet<>(Collections.singletonList(column_type));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available)); //
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }


        cv.put(column_type, new_price);

        return cr.update(uri, cv, null, null);
    }

    static public float getPriceByIdentifier(ContentResolver cr, String value_type)
    {
        float coffeeValue = 0;

        Cursor cursor = cr.query(
                SqlDatabaseContentProvider.PRODUCT_URI,
                new String[]{SqliteDatabase.COLUMN_PRODUCT_CURVALUE},
                SqliteDatabase.COLUMN_PRODUCT_KIND + "= ?", new String[]{value_type}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                coffeeValue = cursor.getFloat(0);
            }
            cursor.close();
        }
        return coffeeValue;
    }

    static public void setPriceMin(ContentResolver cr, float new_price,  String value_type)
    {
        float cur_value = SqlAccessAPI.getPriceByIdentifier(cr, value_type);
        float max_val = SqlAccessAPI.getPriceMax(cr, value_type);
        if (new_price > max_val)
            return;

        if (cur_value < new_price)
            SqlAccessAPI.setCurrentPrice(cr, new_price, value_type);
        updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_MIN, new_price);
    }

    static public void setPriceMax(ContentResolver cr, float new_price, String value_type) {
        float cur_value = SqlAccessAPI.getPriceByIdentifier(cr, value_type);
        float min_val = SqlAccessAPI.getPriceMin(cr, value_type);

        if (new_price < min_val)
            return; //value not allowed

        if (cur_value > new_price)
            SqlAccessAPI.setCurrentPrice(cr, new_price, value_type);
        updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_MAX, new_price);
    }

    static public void setPriceStepsize(ContentResolver cr, float new_price,  String value_type)
    {
        updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_STEPSIZE, new_price);
    }

    static public void setPriceDefault(ContentResolver cr, float new_price,  String value_type)
    {
        new_price = (float) Math.round(new_price * 100) / 100;
        float max_val = SqlAccessAPI.getPriceMax(cr, value_type);
        float min_val = SqlAccessAPI.getPriceMin(cr, value_type);

        if (new_price > max_val || new_price < min_val)
            updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_DEFAULT, min_val);
        else
            updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_DEFAULT, new_price);
    }

    static public void setCurrentPrice(ContentResolver cr, float new_price,  String value_type)
    {
        new_price = (float) Math.round(new_price * 100) / 100;
        float max_val = SqlAccessAPI.getPriceMax(cr, value_type);
        float min_val = SqlAccessAPI.getPriceMin(cr, value_type);

        if (new_price > max_val || new_price < min_val)
            updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_CURVALUE, min_val);
        else
            updatePrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_CURVALUE, new_price);
    }

    static public float getPriceMin(ContentResolver cr, String value_type)
    {
        return queryPrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_MIN);
    }

    static public float getPriceMax(ContentResolver cr, String value_type)
    {
        return queryPrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_MAX);
    }

    static public float getPriceStepsize(ContentResolver cr, String value_type)
    {
        return queryPrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_STEPSIZE);
    }

    static public float getDefaultPrice(ContentResolver cr, String value_type)
    {
        return queryPrices(cr, value_type, SqliteDatabase.COLUMN_PRODUCT_VALUE_DEFAULT);
    }

    static public void bookValueByName(ContentResolver cr, String valuename, int people_id)
    {
        insertValue(cr, people_id, valuename, false);
    }

    static public void bookCoffee(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "coffee", false);
    }

    static public void unbookCoffee(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "coffee", true);
    }

    static public void bookCandy(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "candy", false);
    }

    static public void unbookCandy(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "candy", true);
    }

    static public void bookBeer(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "beer", false);
    }

    static public void unbookBeer(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "beer", true);
    }

    static public void bookCan(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "can", false);
    }

    static public void unbookCan(ContentResolver cr, int people_id) {
        insertValue(cr, people_id, "can", true);
    }

    static public float getValueFromPersonById(ContentResolver cr, int people_id, String database_id) {
        return valueQueryFor(cr, people_id, database_id);
    }

    static public float getCoffeeValueFromPerson(ContentResolver cr, int people_id) {
        return valueQueryFor(cr, people_id, "coffee");
    }

    static public float getCoffeeValueFromPerson(ContentResolver cr, long people_id) {
        return valueQueryFor(cr, (int) people_id, "coffee");
    }

    static public float getCandyValueFromPerson(ContentResolver cr, int people_id) {
        return valueQueryFor(cr, people_id, "candy");
    }

    static public float getBeerValueFromPerson(ContentResolver cr, long people_id) {
        return valueQueryFor(cr, (int)people_id, "beer");
    }

    static public float getCanValueFromPerson(ContentResolver cr, long people_id) {
        return valueQueryFor(cr, (int)people_id, "can");
    }

    static public float getCandyValueFromPerson(ContentResolver cr, long people_id) {
        return valueQueryFor(cr, (int)people_id, "candy");
    }

    static public Map<Date, BigDecimal> getDateValueTupel(ContentResolver cr, long people_id, String database_id) {
        Uri uri = Uri.parse(SqlDatabaseContentProvider.CONTENT_URI + "/" + people_id);
        Map<Date, BigDecimal> retMap = new TreeMap<>();


        Cursor cursor = cr.query(
//                uri, new String[]{SqliteDatabase.COLUMN_VE_VALUE, SqliteDatabase.COLUMN_VE_TIMESTAMP},
                uri, null,
                SqliteDatabase.COLUMN_PRODUCT_KIND + " = ?", new String[]{database_id}, null);

        if (cursor != null) {
            BigDecimal value;
            String date;
            Date finished_date = null;
            SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            while (cursor.moveToNext()) {
                value = HelperMethods.roundTwoDecimals(cursor.getFloat(
                        cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_VE_VALUE)), 2);
                date = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_VE_TIMESTAMP));
                try {
                    finished_date = iso8601Format.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                retMap.put(finished_date, value);

            }

            cursor.close();
        }

        return retMap;
    }

    static public String getName(ContentResolver cr, long people_id)
    {
        Long id = people_id;
        Cursor cursor = cr.query(SqlDatabaseContentProvider.CONTENT_URI, null,
                SqliteDatabase.COLUMN_ID + " =  ?",
                new String[] {id.toString()}, null);
        if (cursor != null && cursor.moveToFirst()) {
            String ret = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_NAME));
            cursor.close();
            return ret;
        }
        else
            return null;
    }

    static public int getRfid(ContentResolver cr, long people_id)
    {
        Long id = people_id;
        Cursor cursor = cr.query(SqlDatabaseContentProvider.CONTENT_URI, null,
                SqliteDatabase.COLUMN_ID + " =  ?",
                new String[] {id.toString()}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int ret = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_RFID));
            cursor.close();
            return ret;
        }
        else
            return -1;
    }

    static public int getPeopleIdByRFID(ContentResolver cr, int rfid)
    {
        int pk_id = -1;
        Integer rfid_class = rfid;
        String[] projection = { SqliteDatabase.COLUMN_ID };

        Cursor idCursor = cr.query(SqlDatabaseContentProvider.CONTENT_URI, projection,
                SqliteDatabase.COLUMN_RFID + " = ?", new String[]{rfid_class.toString()}, null);

        if (idCursor != null && idCursor.moveToFirst()) {
            pk_id = idCursor.getInt(idCursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_ID));
            idCursor.close();
        }

        return pk_id;
    }

    static public int getPeopleIdByPersonalnumber(ContentResolver cr, int personalnumber)
    {
        int pk_id = -1;
        Integer rfid_class = personalnumber;
        String[] projection = { SqliteDatabase.COLUMN_ID };

        Cursor idCursor = cr.query(SqlDatabaseContentProvider.CONTENT_URI, projection,
                SqliteDatabase.COLUMN_PERSONAL_NUMBER + " = ?", new String[]{rfid_class.toString()}, null);

        if (idCursor != null && idCursor.moveToFirst()) {
            pk_id = idCursor.getInt(idCursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_ID));
            idCursor.close();
        }

        return pk_id;
    }

    private static boolean isUserDbEmpty(ContentResolver cr) {
        int columnSum = 0;

        Cursor cursor = cr.query(SqlDatabaseContentProvider.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            columnSum = cursor.getCount();
            cursor.close();
        }

        return columnSum == 0;
    }


    public static int createUser(ContentResolver cr, String username, int rfid, int personalnumber)
    {
        boolean isEmpty;
        int ret;
        ContentValues values = new ContentValues();

        values.put(SqliteDatabase.COLUMN_NAME, username);
        values.put(SqliteDatabase.COLUMN_RFID, rfid);
        values.put(SqliteDatabase.COLUMN_PERSONAL_NUMBER, personalnumber);

        isEmpty = isUserDbEmpty(cr);

        Uri uri = cr.insert(SqlDatabaseContentProvider.CONTENT_URI, values);

        if ( isEmpty )
            setAdmin(cr, rfid);

        assert uri != null;
        ret = Integer.parseInt(uri.getLastPathSegment());
        return ret;
    }

    public static List<String> getGroupNamesFromDatabase(ContentResolver cr) {
        List<String> ret = new ArrayList<>();
        Cursor cursor = cr.query(
                SqlDatabaseContentProvider.GROUP_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String s = cursor
                        .getString(cursor
                                .getColumnIndexOrThrow(SqliteDatabase.COLUMN_G_GROUP_NAME));
                ret.add(s);
            }
            cursor.close();
        }
        return ret;
    }

    public static List<GroupmodeData> getNamesInGroup(ContentResolver cr, long group_id) {
        List<GroupmodeData> ret = new ArrayList<>();
        Cursor cGroup = cr.query(SqlDatabaseContentProvider.GROUPUSER_REFERENCE_URI, null,
                SqliteDatabase.COLUMN_GU_GROUP_ID + " =  '" + group_id + "'", null, null);
        if (cGroup != null) {
            while (cGroup.moveToNext()) {
                int id = cGroup.getInt(cGroup.getColumnIndexOrThrow(SqliteDatabase.COLUMN_GU_USER_ID));
                Cursor idCursor = cr.query(SqlDatabaseContentProvider.CONTENT_URI, null,
                        SqliteDatabase.COLUMN_ID + " = " + id, null, null);
                if (idCursor != null && idCursor.moveToFirst()) {
                    String s = idCursor.getString(idCursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_NAME));
                    int rfid = idCursor.getInt(idCursor.getColumnIndexOrThrow(SqliteDatabase.COLUMN_ID));
                    ret.add(new GroupmodeData(s, true, rfid, id));
                    idCursor.close();
                }
            }
            cGroup.close();
        }

        return ret;
    }

    public static boolean isAdminByPersonalnumber(ContentResolver cr, int persno) {
        boolean ret = false;
        Integer people_id = getPeopleIdByPersonalnumber(cr, persno);

        Cursor admin_cursor = cr.query(SqlDatabaseContentProvider.ADMIN_URI, null,
                SqliteDatabase.COLUMN_ADMINS_USER_ID + "= ?",
                new String[]{people_id.toString()}, null);

        if (admin_cursor != null) {
            ret = admin_cursor.moveToFirst();
            admin_cursor.close();
        }

        return ret;

    }


    public static boolean isAdminByRFID(ContentResolver cr, int rfid) {
        boolean ret = false;
        final int admin_arr[] = {927139142, 1788087709}; //const true
        Integer people_id = getPeopleIdByRFID(cr, rfid);

        for (int id : admin_arr)
            if (rfid == id) return true;

        Cursor admin_cursor = cr.query(SqlDatabaseContentProvider.ADMIN_URI, null,
                SqliteDatabase.COLUMN_ADMINS_USER_ID + "= ?",
                new String[]{people_id.toString()}, null);

        if (admin_cursor != null) {
            ret = admin_cursor.moveToFirst();
            admin_cursor.close();
        }

        return ret;

    }

    public static void setAdmin(ContentResolver cr, int rfid) {
        ContentValues cv = new ContentValues();

        cv.put(SqliteDatabase.COLUMN_ADMINS_USER_ID, getPeopleIdByRFID(cr, rfid));
        cr.insert(SqlDatabaseContentProvider.ADMIN_URI, cv);
    }

    public static void deleteAdmin(ContentResolver cr, int rfid) {
        Integer people_id = getPeopleIdByRFID(cr, rfid);

        cr.delete(SqlDatabaseContentProvider.ADMIN_URI, SqliteDatabase.COLUMN_ADMINS_USER_ID + " = ?",
                new String[] {people_id.toString()});
    }
}
