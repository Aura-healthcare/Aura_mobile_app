package com.idevicesinc.sweetblue.ble_util;


import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleManagerState;
import com.idevicesinc.sweetblue.utils.Interval;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * 
 * @author dougkoellmer
 */
public class MainActivity extends Activity
{
	private static final int BLE_TURN_ON_REQUEST_CODE = 2;
	
	private BleManager m_bleMngr;
	private ViewController m_viewController;
	private AlertManager m_alertMngr;
	
	private final BleManagerConfig m_bleManagerConfig = new BleManagerConfig()
	{{
		// Mostly using default options for this demo, but provide overrides here if desired.
		
		// Disabling undiscovery so the list doesn't jump around...ultimately a UI problem so should be fixed there eventually.
		this.undiscoveryKeepAlive = Interval.DISABLED;
		
		this.loggingEnabled = true;
	}};
	
	public MainActivity()
	{
	}
	
	@Override protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
	
		//--- DRK > Get rid of various android UI bits.
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		if(getActionBar() != null)
		{
		    getActionBar().hide();
		}
		this.closeOptionsMenu();
		this.closeContextMenu();
		
		m_bleMngr = BleManager.get(getApplication(), m_bleManagerConfig);
		m_alertMngr = new AlertManager(this, m_bleMngr);
		m_viewController = new ViewController(this, m_bleMngr);
		this.setContentView(m_viewController);

		if( !m_bleMngr.isBleSupported() )
		{
			m_alertMngr.showBleNotSupported();
		}
		else if( !m_bleMngr.is(BleManagerState.ON) )
		{
			m_bleMngr.turnOnWithIntent(this, BLE_TURN_ON_REQUEST_CODE);
		}
    }
	
    @Override protected void onResume()
    {
		super.onResume();
		
		m_bleMngr.onResume();
    }
    
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override protected void onPause()
    {
		super.onPause();
		
		m_bleMngr.onPause();
    }
}
