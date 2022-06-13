package com.skca.panoptes.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.skca.panoptes.MainActivity;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.hardware.sensors.SensorsInfo;
import com.skca.panoptes.hardware.specification.DeviceInfo;

public class PageViewModel extends ViewModel {

    private int index;

    private MutableLiveData<Long> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Long, String>() {
        @Override
        public String apply(Long input) {
            DeviceInfo d = DataManager.get().getDeviceInfo();
            switch (index) {
                case 1:
                    return
                        d.toString();
                case 2:
                    return SensorsInfo.readSensorValues(MainActivity.showValues);
            }


            return "Hello world from section: " + input;
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