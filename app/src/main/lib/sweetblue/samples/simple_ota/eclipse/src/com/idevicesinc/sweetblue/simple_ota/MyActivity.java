package com.idevicesinc.sweetblue.simple_ota;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleTransaction;
import com.idevicesinc.sweetblue.utils.Uuids;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MyActivity extends Activity
{
	private static final UUID MY_UUID = UUID.randomUUID();			// NOTE: Replace with your actual UUID.
	private static final byte[] MY_DATA = {(byte) 0xC0, (byte) 0xFF, (byte) 0xEE};		//  NOTE: Replace with your actual data, not 0xC0FFEE

	private BleManager m_bleManager;

	private static class MyOtaTransaction extends BleTransaction.Ota
	{
		private final List<byte[]> m_dataQueue;
		private int m_currentIndex = 0;

		private final BleDevice.ReadWriteListener m_readWriteListener = new BleDevice.ReadWriteListener()
		{
			@Override public void onEvent(ReadWriteEvent e)
			{
				if( e.wasSuccess() )
				{
					doNextWrite();
				}
				else
				{
					MyOtaTransaction.this.fail();
				}
			}
		};

		public MyOtaTransaction(final List<byte[]> dataQueue)
		{
			m_dataQueue = dataQueue;
		}

		@Override protected void start(BleDevice device)
		{
			doNextWrite();
		}

		private void doNextWrite()
		{
			if( m_currentIndex == m_dataQueue.size() )
			{
				this.succeed();
			}
			else
			{
				final byte[] nextData = m_dataQueue.get(m_currentIndex);
				getDevice().write(MY_UUID, nextData, m_readWriteListener);
				m_currentIndex++;
			}
		}
	}
	
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

								final ArrayList<byte[]> writeQueue = new ArrayList<byte[]>();
								writeQueue.add(MY_DATA);
								writeQueue.add(MY_DATA);
								writeQueue.add(MY_DATA);
								writeQueue.add(MY_DATA);
								
								event.device().performOta(new MyOtaTransaction(writeQueue));
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
