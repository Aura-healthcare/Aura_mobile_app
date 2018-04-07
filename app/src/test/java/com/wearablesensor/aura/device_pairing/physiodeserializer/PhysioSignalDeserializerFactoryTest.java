package com.wearablesensor.aura.device_pairing.physiodeserializer;

import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by octo_tbr on 22/03/18.
 */
public class PhysioSignalDeserializerFactoryTest {

    @Test
    @Ignore
    public void deserialize() throws Exception {
        // Given
        String uuid = "48774b4a-8c3f-47db-be6d-8cc80db82fc5";
        String deviceAddress = "EF:C8:6B:22:19:C8";
        String user = "2deb4300-eac7-4667-b47c-a3e51e0e2fe7";
        String timeStamp = "2018-03-20T13:47:36.444Z";
        int rRInterval = 747;
        String objectStr = "{\"device_address\": \""+deviceAddress+"\", " +
                "\"uuid\": \""+uuid+"\", " +
                "\"rr_interval\": "+rRInterval+", " +
                "\"user\": \""+user+"\", " +
                "\"time\": \""+timeStamp+"\", " +
                "\"type\": \"RrInterval\"}";
        JSONObject object = (JSONObject) new JSONParser().parse(objectStr);

        // When
        // PhysioSignalModel signal = PhysioSignalDeserializerFactory.deserialize(object);

        // Then
        // PhysioSignalModel expected = new RRIntervalModel(uuid, deviceAddress, user, timeStamp, rRInterval);
        // assertThat(signal, is(equalTo(expected)));
    }

}