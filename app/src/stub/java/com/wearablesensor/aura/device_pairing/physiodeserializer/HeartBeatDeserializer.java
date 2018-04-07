package com.wearablesensor.aura.device_pairing.physiodeserializer;


import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import org.json.simple.JSONObject;

/**
 * Created by octo_tbr on 22/03/18.
 */

public class HeartBeatDeserializer implements PhysioSignalDeserializer<RRIntervalModel> {

    @Override
    public RRIntervalModel deserialize(JSONObject object) {
        String uuid = (String)object.get("uuid");
        String time = (String)object.get("time");
        String address = (String)object.get("device_address");
        String user = (String)object.get("user");
        Long intervalLong = (Long) object.get("rr_interval");
        int interval = Integer.parseInt(intervalLong.toString());
        RRIntervalModel model = new RRIntervalModel(uuid, address, user, time, interval);
        return model;
    }
}
