package com.idevicesinc.sweetblue.current_time_server;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleServer;
import com.idevicesinc.sweetblue.BleServerState;
import com.idevicesinc.sweetblue.BleServices;
import com.idevicesinc.sweetblue.BleStatuses;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.ForEach_Void;
import com.idevicesinc.sweetblue.utils.Utils_Time;
import com.idevicesinc.sweetblue.utils.Uuids;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * This sample demonstrates setting up a server, scanning for a peripheral, connecting to that peripheral, then connecting our local time server
 * to the peripheral (treating it as a "client" as far as time synchronization is concerned), and providing the time to the peripheral when it
 * changes or when it asks for it. It will work with 4.3 and up.
 */
public class MyActivity extends Activity
{
	private static final String MY_DEVICE_NAME = "my_device"; // CHANGE to your device name or a substring thereof.

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        BluetoothEnabler.start(this);
        
		final BleManager mngr = BleManager.get(this);
		final BleServer server = mngr.getServer();

		// Set up a broadcast receiver to get updates to the phone's time and forward them to the client through BLE notifications.
		this.registerReceiver(new BroadcastReceiver()
		{
			@Override public void onReceive(Context context, Intent intent)
			{
				server.getClients(new ForEach_Void<String>()
				{
					@Override public void next(String macAddress)
					{
						// We use the "future data" construct here because SweetBlue's job queue might force
						// this operation to wait (absolute worst case second or two if you're really pounding SweetBlue, but still) a bit
						// before it actually gets sent out over the air, and we want to send the most recent time.
						server.sendNotification(macAddress, Uuids.CURRENT_TIME_SERVICE__CURRENT_TIME, Utils_Time.getFutureTime());
					}
				});
			}

		}, newTimeChangeIntentFilter());

		// Set up our incoming listener to listen for explicit read/write requests and respond accordingly.
		server.setListener_Incoming(new BleServer.IncomingListener()
		{
			@Override public Please onEvent(final IncomingEvent e)
			{
				if( e.target() == Target.CHARACTERISTIC )
				{
					if( e.charUuid().equals(Uuids.CURRENT_TIME_SERVICE__CURRENT_TIME) )
					{
						return Please.respondWithSuccess(Utils_Time.getFutureTime());
					}
					else if( e.charUuid().equals(Uuids.CURRENT_TIME_SERVICE__LOCAL_TIME_INFO) )
					{
						return Please.respondWithSuccess(Utils_Time.getFutureLocalTimeInfo());
					}
				}
				else if( e.target() == Target.DESCRIPTOR )
				{
					return Please.respondWithSuccess();
				}

				return Please.respondWithError(BleStatuses.GATT_ERROR);
			}
		});

		// In a real app you can use this listener to confirm that data was sent -
		// maybe pop up a toast or something to user depending on requirements.
		server.setListener_Outgoing(new BleServer.OutgoingListener()
		{
			@Override public void onEvent(final OutgoingEvent e)
			{
				if( e.wasSuccess() )
				{
					if( e.type().isNotificationOrIndication() )
					{
						Log.i("", "Current time change sent!");
					}
					else
					{
						Log.i("", "Current time or local info request successfully responded to!");
					}
				}
				else
				{
					Log.e("", "Problem sending time change or read request thereof.");
				}
			}
		});

		// Set a listener so we know when the server has finished connecting.
		server.setListener_State(new BleServer.StateListener()
		{
			@Override public void onEvent(BleServer.StateListener.StateEvent e)
			{
				if( e.didEnter(BleServerState.CONNECTED) )
				{
					Log.i("", "Server connected!");
				}
			}
		});

		// Kick things off...from here it's a flow of a bunch of async callbacks...obviously you may want to structure this differently for your actual app.
		server.addService(BleServices.currentTime(), new BleServer.ServiceAddListener()
		{
			@Override public void onEvent(final ServiceAddEvent e)
			{
				if( e.wasSuccess() )
				{
					mngr.startScan(new BleManagerConfig.ScanFilter()
					{
						@Override public Please onEvent(final ScanEvent e)
						{
							return Please.acknowledgeIf(e.name_normalized().contains(MY_DEVICE_NAME)).thenStopScan();
						}
					},

					new BleManager.DiscoveryListener()
					{
						@Override public void onEvent(DiscoveryEvent e)
						{
							if( e.was(LifeCycle.DISCOVERED) )
							{
								e.device().connect(new BleDevice.StateListener()
								{
									@Override public void onEvent(final StateEvent e)
									{
										if( e.didEnter(BleDeviceState.INITIALIZED) )
										{
											// Note that the peripheral may have already connected itself
											// as a client so this call may be redundant.
											server.connect(e.device().getMacAddress());
										}
									}
								});
							}
						}
					});
				}
			}
		});
	}

	private static IntentFilter newTimeChangeIntentFilter()
	{
		final IntentFilter filter = new IntentFilter();

		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

		return filter;
	}

	@Override protected void onResume()
	{
		super.onResume();

		BleManager.get(this).onResume();
	}

	@Override protected void onPause()
	{
		super.onPause();

		BleManager.get(this).onPause();
	}
}
