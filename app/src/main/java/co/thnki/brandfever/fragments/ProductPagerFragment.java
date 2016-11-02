package co.thnki.brandfever.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import co.thnki.brandfever.R;

public class ProductPagerFragment extends Fragment
{


    public ProductPagerFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_product_pager, container, false);
        ButterKnife.bind(this, parentView);

        return parentView;
    }

}
