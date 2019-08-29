package de.fruity.coffeeapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;

public class BackupManager extends BroadcastReceiver {
	static final String TAG = BackupManager.class.getSimpleName();
	
	private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat(
			"yyyyMMdd_HHmmss",  Locale.GERMAN);

	public static String BackupDatabase(ContentResolver provider)
	{
        String out = DATEFORMAT.format(new Date());
		Log.d(TAG, "backup Database ");

		Cursor c = provider.query(SqlDatabaseContentProvider.BACKUP_URI, null, out, null, null);
		assert c != null;
		c.close();

		return out;
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		// alarm get called
		BackupDatabase(context.getContentResolver());

	}

	public void SetAlarm(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, BackupManager.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				1000 * 60 * 60 * 24 , pi); // Millisec * Second * Minute * hour
	}

	public void CancelAlarm(Context context) {
		Intent intent = new Intent(context, BackupManager.class);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}