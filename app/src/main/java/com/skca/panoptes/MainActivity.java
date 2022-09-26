package com.skca.panoptes;

import android.os.Bundle;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.skca.panoptes.databinding.FragmentPrefBinding;
import com.skca.panoptes.gnss.GoogleApiCallbacks;
import com.skca.panoptes.gnss.MainLogger;
import com.skca.panoptes.gnss.MeasurementProvider;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.helper.Recorder;
import com.skca.panoptes.ui.main.SectionsPagerAdapter;
import com.skca.panoptes.databinding.ActivityMainBinding;
import com.skca.panoptes.ui.main.SettingsFragment;
import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity
// GPUREQ: Unnecessary methods if we don't require GPU Information.
//        implements GLSurfaceView.Renderer
{

    private ActivityMainBinding binding;

    public static boolean showValues = true;
    public static Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);



        DataManager d = DataManager.get();
        d.init(this);
        d.loadSensors();
        recorder = new Recorder(this);


        GoogleApiCallbacks callbacks = new GoogleApiCallbacks();

        new MeasurementProvider(
            getApplicationContext(),
            new GoogleApiClient.Builder(this)
                .enableAutoManage(this, callbacks)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(callbacks)
                .addApi(ActivityRecognition.API).addApi(LocationServices.API)
                .build(),
            new MainLogger(recorder)
            );
        binding.button2.setText("▶");
        binding.button2.setOnClickListener(view -> {
            if (!recorder.isRecording) {
                recorder.start(this);
                SettingsFragment.instance.enable(false);
                binding.button2.setText("■");
            }
            else {
                recorder.stop();
                SettingsFragment.instance.enable(true);
                binding.button2.setText("▶");
            }
            //Snackbar.make(view, test(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
        });


    }


    /*
    * GPUREQ: Unnecessary methods if we dont require GPU Information.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {}
    public void onSurfaceChanged(GL10 gl, int width, int height) {}
    public void onDrawFrame(GL10 gl) {}
    */
}