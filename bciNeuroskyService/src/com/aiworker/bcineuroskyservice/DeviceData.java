package com.aiworker.bcineuroskyservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;

import com.neurosky.thinkgear.TGEegPower;

/*import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
*/
// This class represents the data from Neurosky device
class StatsCollector {
    private static final int MAX_POWER_VAL = (int) Math.pow(2, 15);
    private static final int POWER_DIFF = (int) Math.pow(2, 16);

    public double attention = 0.0;
    public double meditation = 0.0;
    public double delta = 0.0;
    public double high_alpha = 0.0;
    public double high_beta = 0.0;
    public double low_alpha = 0.0;
    public double low_beta = 0.0;
    public double low_gamma = 0.0;
    public double mid_gamma = 0.0;
    public double theta = 0.0;

    private int attention_count = 0;
    private int meditation_count = 0;
    private int eeg_power_count = 0; // all EEG waves

    private boolean hasStats = false;

    public StatsCollector() {}

    public void addAttention(int att) {
        hasStats = true;
        int curr_count = attention_count;
        attention_count++;

        attention = (attention * curr_count + att) / attention_count;
    }

    public void addMeditation(int med) {
        hasStats = true;
        int curr_count = meditation_count;
        meditation_count++;

        meditation = (meditation * curr_count + med) / meditation_count;
    }

    public void addEEGPower(TGEegPower eegPow) {
        hasStats = true;
        int curr_count = eeg_power_count;
        eeg_power_count++;

        delta = (delta * curr_count + normalizeRawValue(eegPow.delta)) / eeg_power_count;
        high_alpha = (high_alpha * curr_count + normalizeRawValue(eegPow.highAlpha)) / eeg_power_count;
        high_beta = (high_beta * curr_count + normalizeRawValue(eegPow.highBeta)) / eeg_power_count;
        low_alpha = (low_alpha * curr_count + normalizeRawValue(eegPow.lowAlpha)) / eeg_power_count;
        low_beta = (low_beta * curr_count + normalizeRawValue(eegPow.lowBeta)) / eeg_power_count;
        low_gamma = (low_gamma * curr_count + normalizeRawValue(eegPow.lowGamma)) / eeg_power_count;
        mid_gamma = (mid_gamma * curr_count + normalizeRawValue(eegPow.midGamma)) / eeg_power_count;
        theta = (theta * curr_count + normalizeRawValue(eegPow.theta)) / eeg_power_count;
    }

    private int normalizeRawValue(int value) {
        while (value > MAX_POWER_VAL) {
            value -= POWER_DIFF;
        }
        return value;
    }
}

class NeuroStatData {
    public int attention;
    public int meditation;
    public int delta;
    public int high_alpha;
    public int high_beta;
    public int low_alpha;
    public int low_beta;
    public int low_gamma;
    public int mid_gamma;
    public int theta;

    public NeuroStatData(StatsCollector sc) {
        this.attention = (int) Math.round(sc.attention);
        this.meditation = (int) Math.round(sc.meditation);
        this.delta = (int) Math.round(sc.delta);
        this.high_alpha = (int) Math.round(sc.high_alpha);
        this.high_beta = (int) Math.round(sc.high_beta);
        this.low_alpha = (int) Math.round(sc.low_alpha);
        this.low_beta = (int) Math.round(sc.low_beta);
        this.low_gamma = (int) Math.round(sc.low_gamma);
        this.mid_gamma = (int) Math.round(sc.mid_gamma);
        this.theta = (int) Math.round(sc.theta);
    }
}

class NeuroStat {
    public int id; // exercise id
    public String device_type;
    public String state;
    public String timestamp;
    public NeuroStatData data;

    public NeuroStat(int exerciseId,
                     String devType,
                     String devState,
                     String ts,
                     StatsCollector sc) {
        this.id = exerciseId;
        this.device_type = devType;
        this.state = "active";
        this.timestamp = ts;
        this.data = new NeuroStatData(sc);
    }
}

// This class represents the data for backend
class DeviceData {
    public Collection stats;

    public DeviceData() {
        this.stats = new ArrayList();
    }
}
/*
class DeviceDataAdapter implements JsonSerializer<DeviceData> {
    @Override
    public JsonElement serialize(DeviceData deviceData,
                                 Type type,
                                 JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("hw_id", deviceData.hardwareId);
        obj.addProperty("stats", deviceData.stats);

        return obj;
    }
}

class NeuroStatAdapter implements JsonSerializer<NeuroStat> {
    @Override
    public JsonElement serialize(NeuroStat neuroStat,
                                 Type type,
                                 JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("hw_id", neuroStat.hardwareId);
        obj.addProperty("component_type", neuroStat.componentType);
        obj.addProperty("timestamp", neuroStat.timestamp);
        obj.addProperty("data", neuroStat.data);

        return obj;
    }
    }*/