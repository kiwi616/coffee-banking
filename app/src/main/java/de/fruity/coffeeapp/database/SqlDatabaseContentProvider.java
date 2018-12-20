package de.fruity.coffeeapp.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ContentProvider for generated {@link SqliteDatabase}
 * {@link android.content.ContentProvider}
 *
 * @author kiwi
 */
public class SqlDatabaseContentProvider extends ContentProvider {

    private SqliteDatabase database;

    private static final String TAG = SqlDatabaseContentProvider.class
            .getSimpleName();

    // Used for the UriMatcher
    private static final int TablePeople = 10;
    private static final int TableValues = 11;
    private static final int TableGroupUserReference = 12;
    private static final int TableGroup = 13;
    private static final int TableProduct = 14;
    private static final int TableAdmins = 15;
    private static final int OBJECT_ID = 30;
    private static final int BackupId = 50;


    private static final String AUTHORITY = "de.fruity.coffeeapp";

    private static final String BACKUP_PATH = "Backup";
    public static final Uri BACKUP_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BACKUP_PATH);

    private static final String BASE_PATH = "MainData";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    private static final String VALUE_PATH = "ValueData";
    public static final Uri VALUE_URI = Uri.parse("content://" + AUTHORITY
            + "/" + VALUE_PATH);

    private static final String ADMIN_PATH = "AdminData";
    public static final Uri ADMIN_URI = Uri.parse("content://" + AUTHORITY
            + "/" + ADMIN_PATH);

    private static final String PRODUCT_PATH = "ProductData";
    public static final Uri PRODUCT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PRODUCT_PATH);

    private static final String GROUP_PATH = "GroupS";
    public static final Uri GROUP_URI = Uri.parse("content://" + AUTHORITY
            + "/" + GROUP_PATH);

    private static final String GROUPUSER_REFERENCE_PATH = "GroupData";
    public static final Uri GROUPUSER_REFERENCE_URI = Uri.parse("content://" + AUTHORITY
            + "/" + GROUPUSER_REFERENCE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TablePeople);
        sURIMatcher.addURI(AUTHORITY, VALUE_PATH, TableValues); //just for deletion
        sURIMatcher.addURI(AUTHORITY, VALUE_PATH + "/*", TableValues); //for valuetypes
        sURIMatcher.addURI(AUTHORITY, PRODUCT_PATH, TableProduct);
        sURIMatcher.addURI(AUTHORITY, PRODUCT_PATH + "/*", TableProduct);
        sURIMatcher.addURI(AUTHORITY, ADMIN_PATH, TableAdmins);
        sURIMatcher.addURI(AUTHORITY, GROUPUSER_REFERENCE_PATH, TableGroupUserReference);
        sURIMatcher.addURI(AUTHORITY, GROUP_PATH, TableGroup);
        sURIMatcher.addURI(AUTHORITY, BACKUP_PATH, BackupId);
        // # is wildcard for numbers; here all numbers in TableOwnObj
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", OBJECT_ID);
    }

    @Override
    public boolean onCreate() {
        database = new SqliteDatabase(getContext());

        return true;
    }

    /**
     * Syntax for query is: select PROJECTION from URI where SELECTION order by
     * SORTORDER
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = database.getWritableDatabase();
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;

        // Check if the caller has requested a column which does not exists
        // checkColumns(projection);

        // Set the table
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BackupId:
                Log.d(TAG, "backup Database ");

                try {
                    File filesDir = getContext().getFilesDir();
                    if (!database.backupDatabaseTo(getContext(), new File(filesDir,
                            selection + ".sqlite3")))
                        throw new IllegalArgumentException();
                } catch (NullPointerException ex)
                {
                    ex.printStackTrace();
                    throw new IllegalArgumentException();
                }

                return null;
            case TablePeople:
                queryBuilder.setTables(SqliteDatabase.TABLE_PEOPLE);
                break;
            case TableGroup:
                queryBuilder.setTables(SqliteDatabase.TABLE_GROUP);
                break;
            case TableGroupUserReference:
                queryBuilder.setTables(SqliteDatabase.TABLE_GROUPUSER_REFERENCE);
                break;
            case TableProduct:
                queryBuilder.setTables(SqliteDatabase.TABLE_PRODUCT);
                break;
            case TableAdmins:
                queryBuilder.setTables(SqliteDatabase.TABLE_ADMINS);
                break;
            case OBJECT_ID:
                // Adding the ID to the original query
                queryBuilder.setTables(
                        SqliteDatabase.TABLE_VALUES + " JOIN "
                                + SqliteDatabase.TABLE_PRODUCT + " as prod "
                                + " ON " + SqliteDatabase.COLUMN_VE_TYPE
                                + " = prod." + SqliteDatabase.COLUMN_PRODUCT_ID);
                queryBuilder.appendWhere(SqliteDatabase.COLUMN_VE_PEOPLE_ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor = queryBuilder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private int getIdOfProductKind(String product_kind) {
        int ret;
        Cursor cursor;
        SQLiteDatabase db = database.getWritableDatabase();

        cursor = db.query(SqliteDatabase.TABLE_PRODUCT, new String[]{SqliteDatabase.COLUMN_PRODUCT_ID},
                SqliteDatabase.COLUMN_PRODUCT_KIND + " = ?", new String[]{product_kind},
                null, null, null);

        cursor.moveToFirst();
        ret =  cursor.getInt(0);
        cursor.close();
        return ret;
    }

    @Override
    public void shutdown() {
        database.close();
        super.shutdown();
    }

    /**
     * Have no mimeType so it returns null
     */
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    private int getNextPostion(SQLiteDatabase sqlDB, String table, String column) {
        int curPosition = 0;

        Cursor mCursor = sqlDB.query(table, new String[]{column}, null,
                null, null, null, null);

        if (mCursor.moveToFirst()) {
            try {
                curPosition = mCursor.getInt(mCursor.getColumnIndex(column));
                curPosition++;
            } catch (Exception es) {
                Log.e(TAG, "I hate you all");
            }
        }
        mCursor.close();
        return (curPosition);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        switch (uriType) {
            case TablePeople:
                values.put(SqliteDatabase.COLUMN_POSITION, getNextPostion(
                        sqlDB, SqliteDatabase.TABLE_PEOPLE, SqliteDatabase.COLUMN_POSITION));
                Log.i(TAG, "insert new person " + values.toString());
                id = sqlDB.insertOrThrow(SqliteDatabase.TABLE_PEOPLE, null, values);
                uri = Uri.parse(CONTENT_URI + "/" + id);
                break;
            case TableGroupUserReference:
                Log.i(TAG, "insert new group " + values.toString());
                sqlDB.insertOrThrow(SqliteDatabase.TABLE_GROUPUSER_REFERENCE, null, values);
                break;
            case TableGroup:
                sqlDB.insertOrThrow(SqliteDatabase.TABLE_GROUP, null, values);
                break;
            case TableAdmins:
                sqlDB.insertOrThrow(SqliteDatabase.TABLE_ADMINS, null, values);
                break;
            case TableValues:
                int price_signness = 1;
                float price;
                String valuetype = uri.getLastPathSegment();
                if (valuetype.startsWith("-")) { //value negation
                    valuetype = valuetype.substring(1);
                    price_signness = -1;
                }

                if (getContext() != null) {
                    price = SqlAccessAPI.getPriceByIdentifier(getContext().getContentResolver(),
                            valuetype);
                    price = price * price_signness;


                    id = values.getAsInteger(SqliteDatabase.COLUMN_VE_PEOPLE_ID); //resolves foreign key constraint

                    values.put(SqliteDatabase.COLUMN_VE_VALUE, price);
                    values.put(SqliteDatabase.COLUMN_VE_TYPE, getIdOfProductKind(valuetype));
                    sqlDB.insertOrThrow(SqliteDatabase.TABLE_VALUES, null, values);
                    uri = Uri.parse(CONTENT_URI + "/" + id);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        Log.i(TAG, "item inserted");
        sqlDB.close();
        return Uri.parse(CONTENT_URI + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String id = uri.getLastPathSegment();
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;

        switch (uriType) {
            case TablePeople:
                if (selection.isEmpty() && selectionArgs == null) {
                    database.close(); //this is a dirty hack i have to close all references before i can delte the database
                    return -5;
                }
                else
                    rowsDeleted = sqlDB.delete(SqliteDatabase.TABLE_PEOPLE, selection, selectionArgs);
                break;
            case TableValues:
                rowsDeleted = sqlDB.delete(SqliteDatabase.TABLE_VALUES, selection, selectionArgs);
                break;
            case TableGroup:
                rowsDeleted = sqlDB.delete(SqliteDatabase.TABLE_GROUP, selection, selectionArgs);
                break;
            case TableAdmins:
                rowsDeleted = sqlDB.delete(SqliteDatabase.TABLE_ADMINS, selection, selectionArgs);
                break;
            case OBJECT_ID:

                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(SqliteDatabase.TABLE_PEOPLE, SqliteDatabase.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(SqliteDatabase.TABLE_PEOPLE,
                            SqliteDatabase.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    /**
     * Only implemented because compatibility hopefully never used. The items in
     * the de.fruity.tallysheet.database should not be changed
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int changed_columns;
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();

        switch (uriType) {
            case TableProduct:
                changed_columns = db.update(SqliteDatabase.TABLE_PRODUCT, values,
                        SqliteDatabase.COLUMN_PRODUCT_KIND + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TableGroup:
                changed_columns = db.update(SqliteDatabase.TABLE_GROUP, values,
                        selection, selectionArgs);
                break;
            case OBJECT_ID:
                changed_columns = db.update(SqliteDatabase.TABLE_PEOPLE, values,
                        SqliteDatabase.COLUMN_ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Make sure that potential listeners are getting notified
        // cursor.setNotificationUri(getContext().getContentResolver(), uri);

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return changed_columns;
    }
}
