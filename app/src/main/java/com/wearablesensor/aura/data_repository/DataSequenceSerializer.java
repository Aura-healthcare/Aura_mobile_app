/**
 * @file DataSequenceSerializer
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 * DataSequenceSerializer allows to serialize a bunch of specific phyisio signal type in
 * a compact and optimized JSON format
 *
 */

package com.wearablesensor.aura.data_repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataSequenceSerializer implements JsonSerializer<ConcurrentLinkedQueue<PhysioSignalModel> >/*,JsonDeserializer<ConcurrentLinkedQueue<PhysioSignalModel> >*/ {

    private final static String USER_TAG = "user";
    private final static String TYPE_TAG = "type";
    private final static String DATA_TAG = "data";

    private final static String DEVICE_ADDRESS_TAG = "device_address";

    @Override
    public JsonElement serialize(ConcurrentLinkedQueue<PhysioSignalModel> concurrentLinkedQueue, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject lJsonObject = new JsonObject();

        if(concurrentLinkedQueue.size() == 0){
            return lJsonObject;
        }

        PhysioSignalModel lFirstSample = concurrentLinkedQueue.peek();
        lJsonObject.addProperty(USER_TAG, lFirstSample.getUser());
        lJsonObject.addProperty(TYPE_TAG, lFirstSample.getType());
        lJsonObject.addProperty(DEVICE_ADDRESS_TAG, lFirstSample.getDeviceAdress());

        JsonArray lJsonData = new JsonArray();

        for(PhysioSignalModel lPhysioSignal : concurrentLinkedQueue){
            lJsonData.add(new JsonPrimitive(lPhysioSignal.toString()));
        }

        lJsonObject.add(DATA_TAG, lJsonData);

        return lJsonObject;
    }
}
