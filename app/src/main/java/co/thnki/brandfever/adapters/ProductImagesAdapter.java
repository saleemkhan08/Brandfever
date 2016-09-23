package co.thnki.brandfever.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.ViewHolders.SimpleImageViewHolder;

public class ProductImagesAdapter extends RecyclerView.Adapter<SimpleImageViewHolder>
{

    ArrayList<String> mImageUriList;
    Map<String,Boolean> mSelectImageMap;

    public ProductImagesAdapter(ArrayList<String> imageUriList)
    {
        mImageUriList = imageUriList;
        mSelectImageMap = new HashMap<>();
        for(String key : mImageUriList)
        {
            mSelectImageMap.put(key, false);
        }
    }

    @Override
    public SimpleImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new SimpleImageViewHolder(
                LayoutInflater.from(Brandfever.getAppContext())
                        .inflate(R.layout.product_images_row, parent, false));
    }

    @Override
    public void onBindViewHolder(final SimpleImageViewHolder holder, final int position)
    {
        Glide.with(Brandfever.getAppContext()).load(Uri.parse(mImageUriList.get(position)))
        .into(holder.mImageView);
        holder.mImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!mSelectImageMap.get(mImageUriList.get(position)))
                {
                    holder.mSelectedImageView.setVisibility(View.VISIBLE);
                    mSelectImageMap.put(mImageUriList.get(position), true);
                }
                else
                {
                    holder.mSelectedImageView.setVisibility(View.GONE);
                    mSelectImageMap.put(mImageUriList.get(position), false);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mImageUriList.size();
    }
}
