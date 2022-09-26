package com.skca.panoptes.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.skca.panoptes.MainActivity;
import com.skca.panoptes.gnss.MeasurementProvider;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.hardware.sensors.SensorWrapper;
import com.skca.panoptes.hardware.sensors.SensorsInfo;
import com.skca.panoptes.hardware.specification.DeviceInfo;

import java.util.Map;
import java.util.Set;

public class PageViewModel extends ViewModel {

    private int index;

    private MutableLiveData<Long> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Long, String>() {
        @Override
        public String apply(Long input) {
            if (index == 1) return DataManager.get().getDeviceInfo().toString();
            StringBuilder sb = new StringBuilder("Record Count\n");
            Set<Map.Entry<String, SensorWrapper>> s = SensorsInfo.getSensorMap().entrySet();
            Long curCount;
            for (Map.Entry<String, SensorWrapper> se : s) {
                String sk = se.getKey();
                SensorWrapper sw = se.getValue();
                if (!sw.listen) continue;
                curCount = MainActivity.recorder.counter.getOrDefault(sk, 0L);
                curCount = curCount == null ? 0 : curCount;
                sb
                    .append(sk + ": ")
                    .append(curCount)
                    .append(" (+" + (curCount - sw.recordCount) +")\n");

                sw.recordCount = curCount;
            }
            if (MeasurementProvider.get().listen) {
                curCount = MainActivity.recorder.counter.getOrDefault("NMEA", 0L);
                sb
                        .append("NMEA: ")
                        .append(curCount)
                        .append(" (+" + (curCount - MeasurementProvider.get().mListeners.count) + ")");
                MeasurementProvider.get().mListeners.count = curCount;
            }
            return sb.toString();
        }
    });

    public void setIndex(int index) {
        this.index = index;
    }

    public void refresh() {
        mIndex.postValue(System.currentTimeMillis());
    }


    public LiveData<String> getText() {
        return mText;
    }
}