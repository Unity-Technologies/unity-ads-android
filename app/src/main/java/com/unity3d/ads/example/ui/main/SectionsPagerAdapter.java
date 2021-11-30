package com.unity3d.ads.example.ui.main;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	private final Context mContext;

	public SectionsPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a UnityAdsFragment (defined as a static inner class below).
		return UnityAdsFragment.newInstance(position);
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return null;
	}

	@Override
	public int getCount() {
		// Show 1 total pages.
		return 1;
	}
}