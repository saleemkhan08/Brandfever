package co.thnki.brandfever.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;
import co.thnki.brandfever.view.SquareImageView;

public class ImagePagerFragment extends Fragment
{
    @Bind(R.id.productImage)
    SquareImageView mProductImage;

    String mUrl;

    public ImagePagerFragment()
    {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_image_pager, container, false);
        ButterKnife.bind(this, parentView);
        Glide.with(this).load(mUrl)
                .asBitmap().placeholder(R.mipmap.price_tag)
                .centerCrop().into(mProductImage);
        return parentView;
    }

    public static ImagePagerFragment getInstance(String url)
    {
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.mUrl = url;
        return fragment;
    }
}
