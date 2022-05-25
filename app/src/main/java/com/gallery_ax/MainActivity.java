package com.gallery_ax;

import android.content.Intent;
import android.os.Bundle;
import com.gallery_ax.adpaters.MainPageViewPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.gallery_ax.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String[] sectionTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.viewPager.setAdapter(new MainPageViewPagerAdapter(getSupportFragmentManager(), getLifecycle()));
        sectionTitles = getResources().getStringArray(R.array.section_titles);

        // تغییر عنوان صفحه و تب انتخاب شده با تغییر ViewPager2
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
                binding.sectionTitle.setText(sectionTitles[position]);
            }
        });

        // تغییر عنوان صفحه و ViewPager2 با تغییر Tab
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                binding.viewPager.setCurrentItem(position);
                binding.sectionTitle.setText(sectionTitles[position]);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}