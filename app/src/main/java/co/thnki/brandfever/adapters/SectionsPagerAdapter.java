package co.thnki.brandfever.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.TreeSet;

import co.thnki.brandfever.fragments.ImagePagerFragment;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter
{
    private Bundle mPhotoUrls;
    private ArrayList<String> mPhotoUrlKeys;

    public SectionsPagerAdapter(FragmentManager fragmentManager)
    {
        super(fragmentManager);
    }

    public void updateDataSet(Bundle mPhotoUrlMap)
    {
        mPhotoUrls = mPhotoUrlMap;
        mPhotoUrlKeys = new ArrayList<>();
        TreeSet<String> set = new TreeSet<>(mPhotoUrls.keySet());
        for (String url : set)
        {
            if (url != null && !url.trim().isEmpty())
            {
                mPhotoUrlKeys.add(url);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position)
    {
        String key = mPhotoUrlKeys.get(position);
        String url = mPhotoUrls.getString(key);
        Log.d("SectionsPho", "mPhotoUrlKeys : " + mPhotoUrlKeys.toString() + ", mPhotoUrls : " + mPhotoUrls.toString());
        Log.d("SectionsPho", "key : " + key + ", url : " + url + ", position : " + position);
        return ImagePagerFragment.getInstance(url);
    }

    @Override
    public int getCount()
    {
        return mPhotoUrlKeys.size();
    }

    public String getItemKey(int position)
    {
        return mPhotoUrlKeys.get(position);
    }
}
