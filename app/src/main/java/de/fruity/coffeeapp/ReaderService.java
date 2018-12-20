package de.fruity.coffeeapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.acs.smartcard.Features;
import com.acs.smartcard.PinProperties;
import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.TlvProperties;

@SuppressLint("DefaultLocale")
public class ReaderService extends Service {

	private static final String TAG = ReaderService.class.getSimpleName();

	public static final String TID = "tag_id";
	public static final String PERSONAL_ID = "personal_no";
	static private Features mFeatures = new Features();

	static private int mSlotNum;
	static private Reader mReader;

	final Handler handler = new Handler();
	static boolean mCheckReaderState = true;

	private static final String ACTION_USB_PERMISSION = "de.fruity.USB_PERMISSION";

	private static final String[] stateStrings = { "Unknown", "Absent", "Present", "Swallowed", "Powered",
			"Negotiable", "Specific" };

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							// Open reader
							Log.d(TAG, "Opening reader: " + device.getDeviceName() + "...");
							new ReaderService.OpenTask().execute(device);
						}
					} else {
						Log.d(TAG, "Permission denied for device " + device.getDeviceName());
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				synchronized (this) {
					// Update reader list
//					for (UsbDevice device : mManager.getDeviceList().values()) {
//						if (mReader.isSupported(device)) {
//							// mReaderAdapter.add(device.getDeviceName());
//						}
//					}

					UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (device != null && device.equals(mReader.getDevice())) {

						// Close reader
						Log.d(TAG, "Closing reader...");
						new ReaderService.CloseTask().execute();
					}
				}
			}
		}
	};

	@Override
	public void onCreate() {
		handler.post(runnable);
	}

	private void initReader() {
		// Get USB manager
		UsbManager mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// Initialize reader
		mReader = new Reader(mManager);

		mReader.setOnStateChangeListener(new OnStateChangeListener() {

			@Override
			public void onStateChange(int slotNum, int prevState, int currState) {

				if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
					prevState = Reader.CARD_UNKNOWN;
				}

				if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
					currState = Reader.CARD_UNKNOWN;
				}

				if (prevState == Reader.CARD_ABSENT && currState == Reader.CARD_PRESENT) {
					ReaderService.PowerParams para = new ReaderService.PowerParams();
					para.action = Reader.CARD_WARM_RESET;

					new ReaderService.PowerTask().execute(para);

					ReaderService.SetProtocolParams protParas = new ReaderService.SetProtocolParams();
					protParas.preferredProtocols = Reader.PROTOCOL_T1;

					new ReaderService.SetProtocolTask().execute(protParas);

					byte[] baCommandAPDUKeyA = ReaderService.toByteArray("FFCA000000");
					ReaderService.TransmitParams params = new ReaderService.TransmitParams(-1, ReaderService
							.toHexString(baCommandAPDUKeyA));
					// Transmit control command
					new TransmitTask().execute(params);

				}
				// Show output
				Log.d(TAG, "Slot " + slotNum + ": " + stateStrings[prevState] + " -> " + stateStrings[currState]);
			}
		});

		// Register receiver for USB permission
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mReceiver, filter);

		String deviceName = null;
		for (UsbDevice device : mManager.getDeviceList().values()) {
			if (mReader.isSupported(device)) {
				deviceName = device.getDeviceName();
			}
		}

		if (deviceName != null) {
			// For each device
			for (UsbDevice device : mManager.getDeviceList().values()) {
				// If device name is found
				if (deviceName.equals(device.getDeviceName())) {
					// Request permission
					// does not work dont know why
					// new ReaderAPI.OpenTask().execute(device);
					mManager.requestPermission(device, mPermissionIntent);
					break;
				}
			}
		}
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (mCheckReaderState) {
				Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(dialogIntent);

				try {
					if (mReader.getNumSlots() == 0) {
						new CloseTask().execute();
						unregisterReceiver(mReceiver);
						initReader();
					}
				} catch (NullPointerException e) {
					initReader();
				}

			}
			handler.postDelayed(runnable, 10000);
		}
	};

	public static void stopContinuity() {
		mCheckReaderState = false;
	}

	public static void startContinuity() {
		mCheckReaderState = true;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		new CloseTask().execute();
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	static public class TransmitParams {
		public int controlCode;
		public String commandString;

		public TransmitParams(int controlCode, String commandString) {
			this.controlCode = controlCode;
			this.commandString = commandString;
		}

	}

	static public class TransmitProgress {

		public int controlCode;
		public byte[] command;
		public int commandLength;
		public byte[] response;
		public int responseLength;
		public Exception e;
	}

	public class TransmitTask extends AsyncTask<TransmitParams, TransmitProgress, Void> {

		@Override
		protected Void doInBackground(TransmitParams... params) {

			TransmitProgress progress = new TransmitProgress();

			byte[] command;
			byte[] response = new byte[300];
			int responseLength;
			int foundIndex;
			int startIndex = 0;

			do {
				// Find carriage return
				foundIndex = params[0].commandString.indexOf('\n', startIndex);
				if (foundIndex >= 0) {
					command = toByteArray(params[0].commandString.substring(startIndex, foundIndex));
				} else {
					command = toByteArray(params[0].commandString.substring(startIndex));
				}

				// Set next start index
				startIndex = foundIndex + 1;
				progress.controlCode = params[0].controlCode;
				try {
					if (params[0].controlCode < 0) {
						// Transmit APDU
						responseLength = mReader.transmit(mSlotNum, command, command.length, response, response.length);
					} else {
						// Transmit control command
						responseLength = mReader.control(mSlotNum, params[0].controlCode, command, command.length,
								response, response.length);
					}

					progress.command = command;
					progress.commandLength = command.length;
					progress.response = response;
					progress.responseLength = responseLength;
					progress.e = null;

				} catch (Exception e) {
					progress.command = null;
					progress.commandLength = 0;
					progress.response = null;
					progress.responseLength = 0;
					progress.e = e;
				}
				publishProgress(progress);
			} while (foundIndex >= 0);
			return null;
		}

		@Override
		protected void onProgressUpdate(TransmitProgress... progress) {
			if (progress[0].e != null) {
				Log.e(TAG, "not cool  " + progress[0].e.toString());
			} else {
				Log.w(TAG, "Response:");
				StringBuilder sbs = new StringBuilder();
				for (byte b : progress[0].response) {
					sbs.append(String.format("%02X", b));
				}
				final int tid = sbs.toString().hashCode();
				Intent outgoing = new Intent("android.intent.action.MAIN");
				outgoing.putExtra(TID, tid);
				sendBroadcast(outgoing);

				if (progress[0].response != null && progress[0].responseLength > 0) {
					int controlCode;
					int i;

					// Show control codes for IOCTL_GET_FEATURE_REQUEST
					if (progress[0].controlCode == Reader.IOCTL_GET_FEATURE_REQUEST) {

						mFeatures.fromByteArray(progress[0].response, progress[0].responseLength);

						Log.e(TAG, "Features:");
						for (i = Features.FEATURE_VERIFY_PIN_START; i <= Features.FEATURE_CCID_ESC_COMMAND; i++) {
							controlCode = mFeatures.getControlCode(i);
							if (controlCode >= 0) {
								Log.e(TAG, "Control Code: " + controlCode + " (" + i + ")");
							}
						}
						// Enable buttons if features are supported
					}

					controlCode = mFeatures.getControlCode(Features.FEATURE_IFD_PIN_PROPERTIES);
					if (controlCode >= 0 && progress[0].controlCode == controlCode) {

						PinProperties pinProperties = new PinProperties(progress[0].response,
								progress[0].responseLength);

						Log.e(TAG, "PIN Properties:");
						Log.e(TAG, "LCD Layout: " + toHexString(pinProperties.getLcdLayout()));
						Log.e(TAG,
								"Entry Validation Condition: "
										+ toHexString(pinProperties.getEntryValidationCondition()));
						Log.e(TAG, "Timeout 2: " + toHexString(pinProperties.getTimeOut2()));
					}

					controlCode = mFeatures.getControlCode(Features.FEATURE_GET_TLV_PROPERTIES);
					if (controlCode >= 0 && progress[0].controlCode == controlCode) {

						TlvProperties readerProperties = new TlvProperties(progress[0].response,
								progress[0].responseLength);

						Object property;
						Log.e(TAG, "TLV Properties:");
						for (i = TlvProperties.PROPERTY_wLcdLayout; i <= TlvProperties.PROPERTY_wIdProduct; i++) {

							property = readerProperties.getProperty(i);
							if (property instanceof Integer) {
								Log.e(TAG, i + ": " + toHexString((Integer) property));
							} else if (property instanceof String) {
								Log.e(TAG, i + ": " + property);
							}
						}
					}
				}
			}
		}
	}

	static public class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

		@Override
		protected Exception doInBackground(UsbDevice... params) {

			Exception result = null;
			try {
				mReader.open(params[0]);
			} catch (Exception e) {
				result = e;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Exception result) {

			if (result != null) {
				Log.i(TAG, result.toString());
			} else {
				Log.i(TAG, "Reader name: " + mReader.getReaderName());
				int numSlots = mReader.getNumSlots();
				Log.i(TAG, "Number of slots: " + numSlots);
				// Remove all control codes
				mSlotNum = (numSlots - 1);
				mFeatures.clear();

			}
		}
	}

	static public class CloseTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Log.e(TAG, "Reader got closed");
			mReader.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}

	}

	static public class PowerParams {
		public int action;
	}

	static public class PowerResult {

		public byte[] atr;
		public Exception e;
	}

	static public class PowerTask extends AsyncTask<PowerParams, Void, PowerResult> {

		@Override
		protected PowerResult doInBackground(PowerParams... params) {

			PowerResult result = new PowerResult();

			try {
				result.atr = mReader.power(mSlotNum, params[0].action);
			} catch (Exception e) {
				result.e = e;
			}

			return result;
		}

		@Override
		protected void onPostExecute(PowerResult result) {
			if (result.e != null) {
				Log.e(TAG, result.e.toString());
			} else {
				// Show ATR
				if (result.atr != null) {
					Log.i(TAG, "ATR: catched");
					// Log.i(TAG, new String(result.atr, result.atr.length));
				} else {
					Log.e(TAG, "ATR: None");
				}
			}
		}
	}

	static public class SetProtocolParams {

		public int preferredProtocols;
	}

	static public class SetProtocolResult {

		public int activeProtocol;
		public Exception e;
	}

	static public class SetProtocolTask extends AsyncTask<SetProtocolParams, Void, SetProtocolResult> {

		@Override
		protected SetProtocolResult doInBackground(SetProtocolParams... params) {

			SetProtocolResult result = new SetProtocolResult();

			try {

				result.activeProtocol = mReader.setProtocol(mSlotNum, params[0].preferredProtocols);

			} catch (Exception e) {
				result.e = e;
			}
			return result;
		}

		@Override
		protected void onPostExecute(SetProtocolResult result) {

			if (result.e != null) {

				Log.e(TAG, result.e.toString());

			} else {

				String activeProtocolString = "Active Protocol: ";

				switch (result.activeProtocol) {

				case Reader.PROTOCOL_T0:
					activeProtocolString += "T=0";
					break;

				case Reader.PROTOCOL_T1:
					activeProtocolString += "T=1";
					break;

				default:
					activeProtocolString += "Unknown";
					break;
				}

				// Show active protocol
				Log.d(TAG, activeProtocolString);
			}
		}
	}

	/**
	 * Logs the contents of buffer.
	 * 
	 * @param buffer
	 *            the buffer.
	 * @param bufferLength
	 *            the buffer length.
	 * 
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private void logBuffer(byte[] buffer, int bufferLength) {

		String bufferString = "";

		for (int i = 0; i < bufferLength; i++) {

			String hexChar = Integer.toHexString(buffer[i] & 0xFF);
			if (hexChar.length() == 1) {
				hexChar = "0" + hexChar;
			}

			if (i % 16 == 0) {

				if (!bufferString.isEmpty()) {

					Log.w(TAG, bufferString);
					bufferString = "";
				}
			}

			bufferString += hexChar.toUpperCase() + " ";
		}

		if (!bufferString.isEmpty()) Log.w(TAG, bufferString);
	}

	/**
	 * Converts the HEX string to byte array.
	 * 
	 * @param hexString
	 *            the HEX string.
	 * @return the byte array.
	 */
	static public byte[] toByteArray(String hexString) {

		int hexStringLength = hexString.length();
		byte[] byteArray;
		int count = 0;
		char c;
		int i;

		// Count number of hex characters
		for (i = 0; i < hexStringLength; i++) {

			c = hexString.charAt(i);
			if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
				count++;
			}
		}

		byteArray = new byte[(count + 1) / 2];
		boolean first = true;
		int len = 0;
		int value;
		for (i = 0; i < hexStringLength; i++) {

			c = hexString.charAt(i);
			if (c >= '0' && c <= '9') {
				value = c - '0';
			} else if (c >= 'A' && c <= 'F') {
				value = c - 'A' + 10;
			} else if (c >= 'a' && c <= 'f') {
				value = c - 'a' + 10;
			} else {
				value = -1;
			}

			if (value >= 0) {

				if (first) {

					byteArray[len] = (byte) (value << 4);

				} else {

					byteArray[len] |= value;
					len++;
				}

				first = !first;
			}
		}

		return byteArray;
	}

	/**
	 * Converts the byte array to HEX string.
	 * 
	 * @param buffer
	 *            the buffer.
	 * @return the HEX string.
	 */
	public static String toHexString(byte[] buffer) {

		String bufferString = "";

		for (byte aBuffer : buffer) {

			String hexChar = Integer.toHexString(aBuffer & 0xFF);
			if (hexChar.length() == 1) {
				hexChar = "0" + hexChar;
			}

			bufferString += hexChar.toUpperCase() + " ";
		}

		return bufferString;
	}

	/**
	 * Converts the integer to HEX string.
	 * 
	 * @param i
	 *            the integer.
	 * @return the HEX string.
	 */
	private static String toHexString(int i) {

		String hexString = Integer.toHexString(i);
		if (hexString.length() % 2 != 0) {
			hexString = "0" + hexString;
		}

		return hexString.toUpperCase();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
