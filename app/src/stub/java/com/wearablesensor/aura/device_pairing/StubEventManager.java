package com.wearablesensor.aura.device_pairing;


import android.content.res.AssetManager;

import com.idevicesinc.sweetblue.utils.Uuids;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;
import com.wearablesensor.aura.device_pairing.physiodeserializer.PhysioSignalDeserializerFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.wearablesensor.aura.device_pairing.StubEventManager.EventType.HEART_BEAT;

public class StubEventManager<T extends PhysioSignalModel> {

    private static Map<EventType, StubEventManager> managers;
    private static AssetManager assetManager;

    private EventType type;
    private List<PhysioSignalModel> originalSignalList;
    private Iterator<PhysioSignalModel> signals;
    private long frequency = 300;

    public static enum EventType {
        HEART_BEAT,
    }

    static StubEventManager getEventManager(EventType type, AssetManager assetManger){
        if(managers == null){
            assetManager = assetManger;
            init(type);
        }
        return managers.get(type);
    }

    private static synchronized void init(EventType type){
        managers = new HashMap<>();
        managers.put(HEART_BEAT, new StubEventManager<RRIntervalModel>("RrInterval", type, 300));
    }

    private StubEventManager(String dataType, EventType type, long frequency){
        this.type = type;
        this.frequency = frequency;
        SortedList signalList = new SortedList();
        for (PhysioSignalModel signal : getSignalResources()) {
            if(dataType.equals(signal.getType())){
                signalList.add(signal);
            }
        }
        signalList.sort();
        originalSignalList = new ArrayList<>(signalList);
        signals = signalList.iterator();
    }

    public PhysioEvent getEvent(){
        PhysioSignalModel newtSignal = getNextSignal();
        return getEvent(newtSignal);
    }

    private PhysioEvent getEvent(PhysioSignalModel signal){
        PhysioEvent event = new PhysioEvent();
        event.setUuid(UUID.fromString(signal.getUuid()));
        event.setMacAddress(signal.getDeviceAdress());
        event.setData(new byte[]{});
        populateEvent(signal, event);
        return event;
    }

    private void populateEvent(PhysioSignalModel signal, PhysioEvent event) {
        if(signal instanceof RRIntervalModel){
            populateHeartBeatEvent((RRIntervalModel) signal, event);
        }
    }

    private void populateHeartBeatEvent(RRIntervalModel signal, PhysioEvent event) {
        Integer[] val = {signal.getRrInterval()};
        event.setmRrInterval(val);
        event.setmRrIntervalCount(1);
        event.setUuid(Uuids.HEART_RATE_MEASUREMENT);
    }

    private PhysioSignalModel getNextSignal(){
        if(signals.hasNext())
            return signals.next();
        signals = originalSignalList.iterator();
        return getNextSignal();
    }


    private static List<PhysioSignalModel> getSignalResources() {
        List<PhysioSignalModel> signals = new ArrayList<>();
        try {
            String[] files = assetManager.list("stub_data");
            for(String file : files){
                InputStream stream = assetManager.open("stub_data/" + file);
                JSONArray jsonArray = (JSONArray) new JSONParser().parse(new InputStreamReader(stream));
                for (Iterator it = jsonArray.iterator(); it.hasNext(); ) {
                    JSONObject object = (JSONObject) it.next();
                    PhysioSignalModel signal = PhysioSignalDeserializerFactory.deserialize(object);
                    signals.add(signal);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return signals;
    }

    private class SortedList extends ArrayList<PhysioSignalModel> {

        private Comparator<PhysioSignalModel> comparator = new Comparator<PhysioSignalModel>() {
            @Override
            public int compare(PhysioSignalModel physioSignalModel, PhysioSignalModel t1) {
                Date date1 = DateIso8601Mapper.getDate(physioSignalModel.getTimestamp());
                Date date2 = DateIso8601Mapper.getDate(t1.getTimestamp());
                return date1.compareTo(date2);
            }
        };

        public void sort() {
            Collections.sort(this, comparator);
        }
    }

    public long getFrequency() {
        return frequency;
    }
}
