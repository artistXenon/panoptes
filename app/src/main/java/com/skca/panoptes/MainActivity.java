package com.skca.panoptes;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.ui.main.SectionsPagerAdapter;
import com.skca.panoptes.databinding.ActivityMainBinding;



public class MainActivity extends AppCompatActivity
// GPUREQ: Unnecessary methods if we don't require GPU Information.
//        implements GLSurfaceView.Renderer
{

    private ActivityMainBinding binding;

    public static boolean showValues = true;

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
        FloatingActionButton fab = binding.fab;


        DataManager d = DataManager.get();
        d.init(this);
        d.loadSensors();


        fab.setOnClickListener(view -> {
            showValues = !showValues;
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