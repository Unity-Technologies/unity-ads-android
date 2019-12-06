package com.unity3d.ads.example.ui.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.unity3d.ads.example.R;

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
		switch (position) {
			case 0:
				return UnityAdsFragment.newInstance(position);
			default:
				return UnityAdsFragment.newInstance(position);
		}
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