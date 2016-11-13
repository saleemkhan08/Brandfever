package co.thnki.brandfever.view.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder
{
    @Bind(R.id.text1)
    public CheckedTextView mCheckedTextView;

    public CategoryViewHolder(View itemView)
    {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
