package co.thnki.brandfever.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import co.thnki.brandfever.R;

public class SimpleCategoryViewHolder extends RecyclerView.ViewHolder
{
    public TextView mCategory;

    public SimpleCategoryViewHolder(View itemView)
    {
        super(itemView);
        mCategory = (TextView) itemView.findViewById(R.id.category);
    }
}
