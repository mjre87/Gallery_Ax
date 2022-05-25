package com.gallery_ax.adpaters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gallery_ax.fragments.CategoriesListFragment;
import com.gallery_ax.fragments.FavoritePicturesListFragment;
import com.gallery_ax.fragments.LatestPicturesFragment;

public class MainPageViewPagerAdapter extends FragmentStateAdapter {

    public MainPageViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // بر اساس موقعیت فعلی ViewPager2 فرگمنت مربوطه را نمایش میدهیم
        if (position == 0) {
            return new LatestPicturesFragment();
        } else if (position == 1){
            return new CategoriesListFragment();
        } else {
            return new FavoritePicturesListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
