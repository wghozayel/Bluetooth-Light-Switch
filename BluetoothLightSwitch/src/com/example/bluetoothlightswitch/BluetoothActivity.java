package com.example.bluetoothlightswitch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

public class BluetoothActivity extends Activity {
	
	/* Get Default Adapter */
	private BluetoothAdapter _bluetooth;

	/* request BT enable */
	private static final int REQUEST_ENABLE = 0x1;
	/* request BT discover */
	private static final int REQUEST_DISCOVERABLE = 1;
	/* For getting our bluetooth - scanning */
	private BroadcastReceiver mReceiver = null;
	/* String for connected device */
	private String _bluetoothDevice = ""; // TODO : Turn into an arrayList, to
											// display for user and select from
	private SeekBar seek;
	private OutputStream out;
	private BluetoothConnection connexion;
	
	private Button button;
	private ProgressBar spinner;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		_bluetooth = BluetoothAdapter.getDefaultAdapter();

		((Button) findViewById(R.id.button1))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onEnableButtonClicked();
					}
				});
		seek = (SeekBar) findViewById(R.id.seekBar1);
		seek.setMax(254);
		button = (Button) findViewById(R.id.button2);
		spinner = (ProgressBar) findViewById(R.id.progressBar1);
		
	}

	/* Enable BT */
	public void onEnableButtonClicked() {

		if (_bluetooth == null) {
			Log.d("Waseem", "Sorry but this device does not support Bluetooth"); // TODO Switch to Dialog
		}

		else
			setupBluetooth();
	}


	/* Used when Bluetooth has been enabled, in order to set to discoverable and
	 * carry out any subsequent operations
	 */
	private void setupBluetooth() {
		// TODO : Use query for devices to see if previously connected to avoid
		// discovery mode - cut to the chase

		Intent enabler = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		enabler.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
		startActivityForResult(enabler, REQUEST_DISCOVERABLE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_DISCOVERABLE: {
			Log.d("Waseem", "" + resultCode);
			if (resultCode != RESULT_CANCELED) {
				spinner.setVisibility(View.VISIBLE);
				// Create a BroadcastReceiver for ACTION_FOUND
				mReceiver = new BroadcastReceiver() {
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();
						// When discovery finds a device
						if (BluetoothDevice.ACTION_FOUND.equals(action)) {
							// Get the BluetoothDevice object from the Intent
							BluetoothDevice device = intent
									.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							Log.d("Waseem", device.getName());
							// Add the name and address to our device string
							// //TODO : Remove the leading if(), and simply add
							// to arrayList:
							// _bluetoothDevice.add(device.getName() + "\n" +
							// device.getAddress());
							if (device.getName().equals("HC-06")) {
								_bluetoothDevice = device.getName() + "\n"
										+ device.getAddress();

								connexion = new BluetoothConnection(device);
								connexion.run();
							}
						}
					}
				};
				// Register the BroadcastReceiver
				IntentFilter filter = new IntentFilter(
						BluetoothDevice.ACTION_FOUND);
				registerReceiver(mReceiver, filter);
				_bluetooth.startDiscovery();
			} else {
				Log.d("Waseem", "Discoverable : Failed");
			}
			break;
		}
		}
	}

	@Override
	protected void onDestroy() {
		if (mReceiver != null)
			unregisterReceiver(mReceiver);
		if(connexion != null)
			connexion.cancel();
		_bluetooth.disable();
		super.onDestroy();
		
	}

	private class BluetoothConnection extends Thread {
		private BluetoothSocket socket;
		private BluetoothDevice device;
		private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		public BluetoothConnection(BluetoothDevice d) {
			device = d;

			try {
				socket = device.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
			}

		}

		public void run() {
			_bluetooth.cancelDiscovery();

			try {
				socket.connect();
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException g) {
				}
				return;
			}
			manageConnectedSocket(socket);
		}
		
		public void cancel(){
			
			try {
				socket.close();
			} catch (IOException g) {
			}
		}

	}

	private void manageConnectedSocket(BluetoothSocket socket) {
		
		try {
			out = socket.getOutputStream();
		} catch (IOException e) {
		}
		seek.setVisibility(View.VISIBLE);
		seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {		
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
				try{
					out.write(seek.getProgress());
				} catch(IOException e){} 
			}
		});
		
		button.setVisibility(View.VISIBLE);
		button.setOnClickListener(new View.OnClickListener() {
			
			
			@Override
			public void onClick(View v) {
				try {
					out.write(255);
				} catch (IOException e) {
				}
			}
		});
		spinner.setVisibility(View.INVISIBLE);
	}
	
}