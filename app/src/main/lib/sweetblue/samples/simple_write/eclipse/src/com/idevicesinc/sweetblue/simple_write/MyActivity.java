package com.idevicesinc.sweetblue.simple_write;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.utils.Uuids;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.UUID;


public class MyActivity extends Activity
{
	private static final UUID MY_UUID = UUID.randomUUID();			// NOTE: Replace with your actual UUID.
	private static final byte[] MY_DATA = {(byte) 0xC0, (byte) 0xFF, (byte) 0xEE};		//  NOTE: Replace with your actual data, not 0xC0FFEE

	private BleManager m_bleManager;
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		m_bleManager = BleManager.get(this);
		
		m_bleManager.startScan(new BleManager.DiscoveryListener()
		{
			@Override public void onEvent(DiscoveryEvent event)
			{
				m_bleManager.stopScan();
				
				if( event.was(LifeCycle.DISCOVERED) )
				{
					event.device().connect(new BleDevice.StateListener()
					{
						@Override public void onEvent(StateEvent event)
						{
							if( event.didEnter(BleDeviceState.INITIALIZED) )
							{
								Log.i("SweetBlueExample", event.device().getName_debug() + " just initialized!");
								
								event.device().write(MY_UUID, MY_DATA, new BleDevice.ReadWriteListener()
								{
									@Override public void onEvent(ReadWriteEvent event)
									{
										if( event.wasSuccess() )
										{
											Log.i("", "Write successful");
										}
										else
										{
											Log.e("", event.status().toString()); // Logs the reason why it failed.
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
	
	@Override protected void onResume()
	{
		super.onResume();
		
		m_bleManager.onResume();
	}
	
	@Override protected void onPause()
	{
		super.onPause();
		
		m_bleManager.onPause();
	}
}
