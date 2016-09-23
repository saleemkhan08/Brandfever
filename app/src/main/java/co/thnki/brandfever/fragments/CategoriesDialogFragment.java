package co.thnki.brandfever.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.ViewHolders.CategoryViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.singletons.Otto;

public class CategoriesDialogFragment extends DialogFragment implements Const
{
    @Bind(R.id.categoriesRecyclerView)
    RecyclerView mCategoriesRecyclerView;

    @Bind(R.id.recyclerProgress)
    RelativeLayout mProgress;

    private DatabaseReference mCategoriesRef;
    private DatabaseReference mAvailableCategoriesRef;
    private RecyclerView.Adapter mAdapter;

    public CategoriesDialogFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View parentView = inflater.inflate(R.layout.fragment_categories, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(Brandfever.getTypeFace());
        initializeDatabaseRefs();
        initializeRecyclerView();
        mProgress.setVisibility(View.VISIBLE);
        return parentView;
    }

    private void initializeRecyclerView()
    {
        mCategoriesRecyclerView.setHasFixedSize(true);
        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(
                Category.class,
                R.layout.checked_item_view,
                CategoryViewHolder.class,
                getCategories(20))
        {
            @Override
            protected void populateViewHolder(CategoryViewHolder viewHolder, final Category model, int position)
            {
                mProgress.setVisibility(View.GONE);
                viewHolder.mCheckedTextView.setText(model.getCategory());
                viewHolder.mCheckedTextView.setChecked(model.isCategorySelected());
                viewHolder.mCheckedTextView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String catId = model.getCategoryId() + "";
                        CheckedTextView category = (CheckedTextView) view;
                        if (category.isChecked())
                        {
                            mAvailableCategoriesRef.child(catId).removeValue();
                            category.setChecked(false);
                            mCategoriesRef.child(catId).child("categorySelected").setValue(false);
                        }
                        else
                        {
                            category.setChecked(true);
                            mAvailableCategoriesRef.child(catId).setValue(model);
                            mCategoriesRef.child(catId).child("categorySelected").setValue(true);
                        }
                    }
                });
            }
        };
        mCategoriesRecyclerView.setAdapter(mAdapter);
    }

    private void initializeDatabaseRefs()
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            mCategoriesRef = rootRef.child(bundle.getString(ALL_CATEGORIES));
            mAvailableCategoriesRef = rootRef.child(bundle.getString(Const.AVAILABLE_CATEGORIES));
        }
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
    }

    public Query getCategories(int limit)
    {
        return mCategoriesRef.limitToLast(limit).orderByChild("categoryId");
    }
}
