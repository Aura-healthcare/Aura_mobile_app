package com.wearablesensor.aura.device_pairing.physiodeserializer;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

import org.json.simple.JSONObject;

/**
 * Created by octo_tbr on 22/03/18.
 */

public interface PhysioSignalDeserializer<T extends PhysioSignalModel> {

    T deserialize(JSONObject object);
}
