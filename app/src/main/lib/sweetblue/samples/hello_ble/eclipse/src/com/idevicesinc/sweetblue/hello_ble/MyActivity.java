package com.idevicesinc.sweetblue.hello_ble;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.utils.Uuids;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MyActivity extends Activity
{
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
								
								event.device().read(Uuids.BATTERY_LEVEL, new BleDevice.ReadWriteListener()
								{
									@Override public void onEvent(ReadWriteEvent result)
									{
										if( result.wasSuccess() )
										{
											Log.i("SweetBlueExample", "Battery level is " + result.data()[0] + "%");
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
