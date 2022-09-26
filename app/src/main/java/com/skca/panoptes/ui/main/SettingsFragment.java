package com.skca.panoptes.ui.main;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.ViewGroup.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import com.skca.panoptes.databinding.FragmentPrefBinding;
import com.skca.panoptes.gnss.MeasurementProvider;
import com.skca.panoptes.hardware.sensors.SensorWrapper;
import com.skca.panoptes.hardware.sensors.SensorsInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends Fragment {

    public static SettingsFragment instance;
    private FragmentPrefBinding binding;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        instance = fragment;
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentPrefBinding.inflate(inflater, container, false);
        ConstraintLayout constraintLayout = binding.constraintLayout;

        Set<Map.Entry<String, SensorWrapper>> s = SensorsInfo.getSensorMap().entrySet();

        ArrayList<Integer> ids = new ArrayList<>(s.size());

        for (Map.Entry<String, SensorWrapper> ss : s) {
            Sensor baseSensor = ss.getValue().getBaseSensor();

            TextView t = insertTextDetail(ss.getValue().getSensorDetails());
            t.setOnClickListener(v -> {
                int
                    min = Math.max(baseSensor.getMinDelay(), 0),
                    max = Math.max(baseSensor.getMaxDelay(), 0);
                if (min != 0 || max != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Sensors Details");
                    NumberPicker np = new NumberPicker(getContext());
                    if (max != 0) np.setMaxValue(max);
                    np.setMinValue(min);
                    np.setValue(ss.getValue().delay);
                    builder.setView(np);
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        ss.getValue().delay = np.getValue();
                        dialog.dismiss();
                        t.setText(ss.getValue().getSensorDetails());
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.create().show();
                }
            });

            ids.addAll(
                insertSwitch(ss.getKey(), (buttonView, isChecked) -> ss.getValue().listen = isChecked, t)
            );
        }

        ids.addAll(
                insertSwitch("NMEA", (buttonView, isChecked) -> MeasurementProvider.get().listen = isChecked, null)
        );

        ConstraintSet cs = new ConstraintSet();
        cs.clone(constraintLayout);
        cs.connect(ids.get(0), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP, 0);
        for (int i = 1; i < ids.size(); i++) {
            cs.connect(ids.get(i), ConstraintSet.TOP, ids.get(i - 1), ConstraintSet.BOTTOM, 0);
        }

        cs.applyTo(constraintLayout);

        return binding.getRoot();
    }

    private List<Integer> insertSwitch(CharSequence text, CompoundButton.OnCheckedChangeListener listener, TextView detailText) {
        SwitchCompat switchCompat = new SwitchCompat(getContext());
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(View.generateViewId());
        switchCompat.setId(ids.get(0));
        switchCompat.setText(text);
        switchCompat.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        switchCompat.setOnCheckedChangeListener(listener);

        if (detailText != null) {
            binding.constraintLayout.addView(detailText);
            ids.add(detailText.getId());
        }

        binding.constraintLayout.addView(switchCompat);
        return ids;
    }

    private TextView insertTextDetail(CharSequence detailText) {
        TextView textView = new TextView(getContext());
        int id = View.generateViewId();
        textView.setId(id);
        textView.setText(detailText);
        textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return textView;
    }

    public void enable(boolean enable) {
        for (int i = 0; i < binding.constraintLayout.getChildCount(); i++) {
            View v = binding.constraintLayout.getChildAt(i);
            v.setEnabled(enable);
        }
    }
}
