package com.searchresults.fragcom;

import android.graphics.PorterDuff;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.searchresults.fragcom.events.TextChangedEvent;
import com.searchresults.fragcom.fragments.OneFragment;
import com.searchresults.fragcom.fragments.ThreeFragment;
import com.searchresults.fragcom.fragments.TwoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    EventBus bus = EventBus.getDefault();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bus.register(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setCurrentItem(1);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(MainActivity.this, android.R.color.white);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

                switch(tab.getPosition()) {
                    case 0:
                        toolbar.setTitle("Relevant Topics");
                        break;
                    case 1:
                        toolbar.setTitle("PdfTube");
                        break;
                    case 2:
                        toolbar.setTitle("Videos Suggestions");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewpPagerAdapter adapter = new ViewpPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OneFragment(), "ONE");
        adapter.addFragment(new TwoFragment(), "TWO");
        adapter.addFragment(new ThreeFragment(), "THREE");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_subject_white_24px);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_picture_as_pdf_white_24px);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_video_library_white_24px);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TextChangedEvent event) {
        //tv.setText(event.newText);
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.app_bar);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);

        String showHide = event.newText.toString();

        if(showHide.equals("show")) {
            appBarLayout.setVisibility(View.VISIBLE);
            appBarLayout.setExpanded(true);
            tabLayout.setVisibility(View.VISIBLE);
        } else if(showHide.equals("hide")) {
            appBarLayout.setVisibility(View.VISIBLE);
            appBarLayout.setExpanded(false);
            tabLayout.setVisibility(View.GONE);

        }

    }
}
