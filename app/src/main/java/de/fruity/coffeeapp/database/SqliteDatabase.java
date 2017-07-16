package de.fruity.coffeeapp.database;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.tools.HelperMethods;
import de.fruity.coffeeapp.ui_elements.CustomToast;

/**
 * Simple SqliteDatabase with 3 Tables * ownObjects the Objects also show in the ListView
 * valuesTab the Table for the measurement Values * xmlTab for the XML databaseobjects
 * 
 * The Tables have on delete Cascade 
 * 
 * @author kiwi
 *
 *	TODO implement Indices
 */
public class SqliteDatabase extends SQLiteOpenHelper {
	private static final String TAG = SqliteDatabase.class.getSimpleName();

	private static final String DATABASE_NAME = "CustomerDatabase.db";
	private static final int DATABASE_VERSION = 1;
	// Main table
    public static final String TABLE_PEOPLE_OLD = "people_table";
	public static final String TABLE_PEOPLE = "peoples_table";
    public static final String TABLE_VALUES = "values_table";
	public static final String TABLE_PRODUCT = "product_table";
	public static final String TABLE_ADMINS = "admin_table";
    public static final String TABLE_GROUP = "group_table";
    public static final String TABLE_GROUPUSER_REFERENCE = "group_user_reference_table";

	public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RFID = "rfid";
    public static final String COLUMN_PERSONAL_NUMBER= "personal_number";
	public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_POSITION = "position";

    public static final String COLUMN_VE_ID = "_id";
    public static final String COLUMN_VE_TYPE = "value_type";//beer, candy ...
    public static final String COLUMN_VE_TIMESTAMP = "booking_timestamp";
    public static final String COLUMN_VE_VALUE = "value_in_euro";
    public static final String COLUMN_VE_PEOPLE_ID = "people_id";

    public static final String COLUMN_ADMINS_ID = "_id";
    public static final String COLUMN_ADMINS_USER_ID = "group_user_id";

    public static final String COLUMN_PRODUCT_ID = "_id";//beer, candy ...
    public static final String COLUMN_PRODUCT_KIND = "product_kind";//beer, candy ...
    public static final String COLUMN_PRODUCT_CURVALUE = "cur_value";
    public static final String COLUMN_PRODUCT_VALUE_MIN = "min_value";
    public static final String COLUMN_PRODUCT_VALUE_MAX = "max_value";
    public static final String COLUMN_PRODUCT_VALUE_STEPSIZE = "step_size_value";
    public static final String COLUMN_PRODUCT_VALUE_DEFAULT = "default_value";

    public static final String COLUMN_GU_ID = "_id";
    public static final String COLUMN_GU_USER_ID = "user_id";
    public static final String COLUMN_GU_GROUP_ID = "group_id";
    
    public static final String COLUMN_G_GROUP_ID = "_id";
    public static final String COLUMN_G_GROUP_NAME = "group_name";

    // Database creation SQL statement
	private static final String PEOPLE_TAB_CREATE = "create table "
			+ TABLE_PEOPLE + "("
			+ COLUMN_ID	+ " integer primary key autoincrement, " 
			+ COLUMN_NAME + " text not null, "
            + COLUMN_RFID + " integer unique not null, "
            + COLUMN_PERSONAL_NUMBER + " integer unique not null, "
			+ COLUMN_DATE + " datetime default current_timestamp, "
			+ COLUMN_POSITION + " integer not null"
            +  ");";

    private static final String VALUE_TAB_CREATE = "create table "
            + TABLE_VALUES + "("
            + COLUMN_VE_ID	+ " integer primary key autoincrement, "
            + COLUMN_VE_TYPE + " integer NOT NULL, "
            + COLUMN_VE_TIMESTAMP + " datetime default current_timestamp, "
            + COLUMN_VE_VALUE + " double NOT NULL,"
            + COLUMN_VE_PEOPLE_ID + " integer NOT NULL, "
            + " foreign key (" + COLUMN_VE_PEOPLE_ID + ") references " + TABLE_PEOPLE
            + " (" + COLUMN_ID + ") ON DELETE CASCADE ON UPDATE CASCADE "
            + " foreign key (" + COLUMN_VE_TYPE + ") references " + TABLE_PRODUCT
            + " (" + COLUMN_PRODUCT_ID + ") ON DELETE CASCADE ON UPDATE CASCADE "
            +  ");";

    private static final String PRODUCT_TAB_CREATE = "create table "
            + TABLE_PRODUCT + "("
            + COLUMN_PRODUCT_ID	+ " integer primary key autoincrement, "
            + COLUMN_PRODUCT_KIND+ " varchar NOT NULL,"
            + COLUMN_PRODUCT_CURVALUE + " float default 0.0, "
            + COLUMN_PRODUCT_VALUE_MAX +  " float NOT NULL,"
            + COLUMN_PRODUCT_VALUE_MIN +  " float NOT NULL, "
            + COLUMN_PRODUCT_VALUE_STEPSIZE +  " float NOT NULL, "
            + COLUMN_PRODUCT_VALUE_DEFAULT +  " float NOT NULL "
            +  ");";

    private static final String ADMINS_TAB_CREATE = "create table "
            + TABLE_ADMINS + "("
            + COLUMN_ADMINS_ID	+ " integer primary key autoincrement, "
            + COLUMN_ADMINS_USER_ID+ " varchar NOT NULL,"
            + " foreign key (" + COLUMN_ADMINS_USER_ID + ") references " + TABLE_PEOPLE
            + " (" + COLUMN_ID + ") ON DELETE CASCADE ON UPDATE CASCADE "
            +  ");";

    private static final String GROUP_TAB_CREATE = "create table "
            + TABLE_GROUP + "("
            + COLUMN_G_GROUP_ID	+ " integer primary key autoincrement, "
            + COLUMN_G_GROUP_NAME + " text not null "
            +  ");";
    
    private static final String GROUP_USER_TAB_CREATE = "create table "
            + TABLE_GROUPUSER_REFERENCE + "("
            + COLUMN_GU_ID	+ " integer primary key autoincrement, "
            + COLUMN_GU_USER_ID	+ " integer not null, "
            + COLUMN_GU_GROUP_ID	+ " integer not null, "
            + " foreign key (" + COLUMN_GU_USER_ID + ") references " + TABLE_PEOPLE
            + " (" + COLUMN_ID + ") ON DELETE CASCADE ON UPDATE CASCADE "
            + " foreign key (" + COLUMN_GU_GROUP_ID + ") references " + TABLE_GROUP
            + " (" + COLUMN_G_GROUP_ID + ") ON DELETE CASCADE ON UPDATE CASCADE "
            +  ");";

//    private static final String DELETE_TRIGGER = "CREATE TRIGGER on_delete_delete_ref BEFORE DELETE ON "
//            + TABLE_GROUP
//            + " FOR EACH ROW BEGIN "
//            + " DELETE FROM " + TABLE_GROUPUSER_REFERENCE
//            + " WHERE " + TABLE_GROUP + "." + COLUMN_G_GROUP_ID
//            + " = "
//            + TABLE_GROUPUSER_REFERENCE + "." + COLUMN_GU_GROUP_ID
//            + " END;";

    private static final String PRODUCT_TAB_DEFAULT_COFFEE = "insert into "
            + TABLE_PRODUCT + " VALUES( 1, 'coffee', 0.25, 0.50, 0.25, 0.25, 0.25);";
    private static final String PRODUCT_TAB_DEFAULT_CANDY = "insert into "
            + TABLE_PRODUCT + " VALUES( 2, 'candy', 0.10, 0.40, 0.05, 0.05, 0.10);";
    private static final String PRODUCT_TAB_DEFAULT_BEER = "insert into "
            + TABLE_PRODUCT + " VALUES( 3, 'beer', 0.35, 0.70, 0.35, 0.35, 0.70);";
    private static final String PRODUCT_TAB_DEFAULT_CAN = "insert into "
            + TABLE_PRODUCT + " VALUES( 4, 'can', 2.00, 3.00, 2.00, 1.0, 2.00);";


    public SqliteDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(TAG, PEOPLE_TAB_CREATE);
        database.execSQL(PEOPLE_TAB_CREATE);

        Log.i(TAG, VALUE_TAB_CREATE);
        database.execSQL(VALUE_TAB_CREATE);

        Log.i(TAG, GROUP_TAB_CREATE);
        database.execSQL(GROUP_TAB_CREATE);

        Log.i(TAG, GROUP_USER_TAB_CREATE);
        database.execSQL(GROUP_USER_TAB_CREATE);

        Log.i(TAG, PRODUCT_TAB_CREATE);
        database.execSQL(PRODUCT_TAB_CREATE);

        Log.i(TAG, ADMINS_TAB_CREATE);
        database.execSQL(ADMINS_TAB_CREATE);

        database.execSQL(PRODUCT_TAB_DEFAULT_COFFEE);
        database.execSQL(PRODUCT_TAB_DEFAULT_CANDY);
        database.execSQL(PRODUCT_TAB_DEFAULT_BEER);
        database.execSQL(PRODUCT_TAB_DEFAULT_CAN);
	}

	@Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {


        Log.w(TAG, "Upgrading de.fruity.tallysheet.database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);

        database.execSQL(PRODUCT_TAB_CREATE);

        database.execSQL(PRODUCT_TAB_DEFAULT_COFFEE);
        database.execSQL(PRODUCT_TAB_DEFAULT_CANDY);
        database.execSQL(PRODUCT_TAB_DEFAULT_BEER);
        database.execSQL(PRODUCT_TAB_DEFAULT_CAN);

////		database.execSQL("alter table " + TABLE_VALUES + " add column " + COLUMN_V_BEER + " double default 0.0");
//
//
//		database.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_VALUES);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPUSER_REFERENCE);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMINS);
//
//
//        onCreate(database);
//
////        database.execSQL("SELECT name FROM sqlite_master WHERE type='table' AND name=" + "'" + TABLE_PEOPLE_OLD + "'" + ";");
//
//
//        String data_from_old_table = "INSERT INTO " + TABLE_PEOPLE  + " (" + COLUMN_ID + "," + COLUMN_NAME + ","
//                + COLUMN_RFID + " ,"
//                + COLUMN_PERSONAL_NUMBER + ","
//                + COLUMN_DATE + ","
//                + COLUMN_POSITION + ") "
//                + " SELECT " +COLUMN_ID + "," + COLUMN_NAME + "," + COLUMN_RFID + " ,"
//                + COLUMN_PERSONAL_NUMBER + "," + COLUMN_DATE + "," + COLUMN_POSITION
//                + " FROM "+ TABLE_PEOPLE_OLD;
//
//        database.execSQL(data_from_old_table);
//
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE_OLD);

	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}


    private boolean transferFrom(File src, File dst) throws IOException {
        FileOutputStream fos = new FileOutputStream(dst);
        FileInputStream fis = new FileInputStream(src);

        FileChannel srcChannel = fis.getChannel();
        FileChannel dstChannel = fos.getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        dstChannel.close();
        return true;
    }

	public boolean backupDatabaseTo(Context context, File backup_file) {

        File sourceFile = context.getDatabasePath(getDatabaseName());

        try {
            return transferFrom(sourceFile, backup_file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreDatabaseFrom(Context context, File srcfile) {

        File db_file = context.getDatabasePath(getDatabaseName());
        close();

        try {
            return transferFrom(srcfile, db_file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public static void moveDatabaseToReadable(String backupFilename) {

		String sourcePath = "/data/data/de.fruity.coffeeapp/databases/" + DATABASE_NAME;

		File sourceFile = new File(sourcePath);
		try {
			FileInputStream fis = new FileInputStream(sourceFile);

			String outFilePath = Environment.getExternalStorageDirectory() + "/" + backupFilename;
			
			OutputStream output = new FileOutputStream(outFilePath);

			byte[] buff = new byte[1024];
			int len;
			while ((len = fis.read(buff)) > 0) {
				output.write(buff, 0, len);
			}
			output.flush();
			output.close();
			fis.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public static int importCsv(Uri fileuri, Context context) {
        ContentResolver cr = context.getContentResolver();
        boolean valid = false;
        List<String[]> userlist_with_values = new ArrayList<>();

        try {
            InputStream is = cr.openInputStream(fileuri);
            if (is == null)
            {
                Log.e(TAG, "No file under uri "  + fileuri.toString());
                return -1;
            }

            InputStreamReader csvStreamReader = new InputStreamReader(is);

            CSVReader csvReader = new CSVReader(csvStreamReader, ';');
            String[] line;
            String[] secondline;

            // throw away the header
            secondline = csvReader.readNext();
            csvReader.readNext();
            csvReader.readNext();

            if (Integer.parseInt(secondline[0]) == DATABASE_VERSION)
                valid = true; //break could be better

            while ((line = csvReader.readNext()) != null) {
                userlist_with_values.add(line);
            }
        } catch (IOException e) {
            valid = false;
            e.printStackTrace();
        }

        if (valid) {
            cr.delete(SqlDatabaseContentProvider.CONTENT_URI, "", null);
            if ( !context.deleteDatabase(DATABASE_NAME))
                return -3;

            try {

                for (String[] user : userlist_with_values) {
                    int pid = SqlAccessAPI.createUser(cr,
                            user[0], Integer.parseInt(user[1]), Integer.parseInt(user[2]));

                    while (SqlAccessAPI.getValueFromPersonById(cr, pid, "coffee") <
                            NumberFormat.getNumberInstance(Locale.getDefault()).parse(user[3]).floatValue())
                        SqlAccessAPI.bookValueByName(cr, "coffee", pid);
                    while (SqlAccessAPI.getValueFromPersonById(cr, pid, "candy") <
                            NumberFormat.getNumberInstance(Locale.getDefault()).parse(user[4]).floatValue())
                        SqlAccessAPI.bookValueByName(cr, "candy", pid);
                    while (SqlAccessAPI.getValueFromPersonById(cr, pid, "beer") <
                            NumberFormat.getNumberInstance(Locale.getDefault()).parse(user[5]).floatValue())
                        SqlAccessAPI.bookValueByName(cr, "beer", pid);
                    while (SqlAccessAPI.getValueFromPersonById(cr, pid, "can") <
                            NumberFormat.getNumberInstance(Locale.getDefault()).parse(user[6]).floatValue())
                        SqlAccessAPI.bookValueByName(cr, "can", pid);
                }
            } catch (NumberFormatException | SQLiteConstraintException | ParseException ex) {
                ex.printStackTrace();
                return -2;
            }

            return 0;

        } else
            return -1;
    }

    static public Intent backupDatabaseCSV(File outFile, ContentResolver cr) {
        Log.d(TAG, "backupDatabaseCSV");
        String csvHeader = "";
        String csvValues;

        csvHeader += DATABASE_VERSION + ";;;Kaffee;Candy;Bier;Dose\n";
        csvHeader += ";;;Giacomo Gusto;Beck Ralph;Buongustaio Birra;Luigi Salsiccia\n";
        csvHeader += "\n";
        Log.d(TAG, "header=" + csvHeader);
        try {
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            Cursor cursor = cr.query(
                    SqlDatabaseContentProvider.CONTENT_URI, null, null, null,
                    null);
            if (cursor != null) {
                out.write(csvHeader);
                while (cursor.moveToNext()) {
                    csvValues = cursor.getString(cursor
                            .getColumnIndex(SqliteDatabase.COLUMN_NAME)) + ";";
                    csvValues += cursor
                            .getInt(cursor
                                    .getColumnIndex(SqliteDatabase.COLUMN_RFID))
                            + ";";
                    csvValues += cursor
                            .getInt(cursor
                                    .getColumnIndex(SqliteDatabase.COLUMN_PERSONAL_NUMBER))
                            + ";";

                    long person_id = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(SqliteDatabase.COLUMN_ID));

                    csvValues += HelperMethods.roundTwoDecimals(
                            SqlAccessAPI.getCoffeeValueFromPerson(cr, person_id))
                            + ";";
                    csvValues += HelperMethods.roundTwoDecimals(
                            SqlAccessAPI.getCandyValueFromPerson(cr, person_id))
                            + ";";
                    csvValues += HelperMethods.roundTwoDecimals(
                            SqlAccessAPI.getBeerValueFromPerson(cr, person_id))
                            + ";";
                    csvValues += HelperMethods.roundTwoDecimals(
                            SqlAccessAPI.getCanValueFromPerson(cr, person_id))
                            + "\n";


                    out.write(csvValues);
                }
                cursor.close();
            }
            out.close();
            fileWriter.close();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/xml");
            intent.putExtra(Intent.EXTRA_SUBJECT, "coffeapp database");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outFile));

            return intent;
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.getMessage());
        }

        return null;
    }

}

