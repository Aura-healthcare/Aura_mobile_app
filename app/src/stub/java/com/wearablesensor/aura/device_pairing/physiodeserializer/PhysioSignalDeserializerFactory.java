package com.wearablesensor.aura.device_pairing.physiodeserializer;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

import org.json.simple.JSONObject;

/**
 * Created by octo_tbr on 22/03/18.
 */

public class PhysioSignalDeserializerFactory {

    public static final String HEART_BEAT_SIGNAL = "RrInterval";


    public static PhysioSignalModel deserialize(JSONObject object) {
        String type = (String)object.get("type");
        return getDeserializer(type).deserialize(object);
    }

    static PhysioSignalDeserializer getDeserializer(String objectType){
        if(HEART_BEAT_SIGNAL.equals(objectType)){
            return new HeartBeatDeserializer();
        }
        return null;
    }
}
