package com.wearablesensor.aura.device_pairing;

import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;

/**
 * Created by octo_tbr on 13/03/18.
 */

public interface EventManager {

    public void processEvent(PhysioEvent event);
}
