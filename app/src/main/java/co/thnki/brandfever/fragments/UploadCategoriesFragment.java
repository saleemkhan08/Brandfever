package co.thnki.brandfever.fragments;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.ProductUploadActivity;
import co.thnki.brandfever.R;
import co.thnki.brandfever.ViewHolders.SimpleCategoryViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.singletons.Otto;

import static android.app.Activity.RESULT_OK;
import static co.thnki.brandfever.ProductUploadActivity.PRODUCT_IMAGES;

public class UploadCategoriesFragment extends Fragment implements Const
{
    @Bind(R.id.mainCategoryRecyclerView)
    RecyclerView mCategoriesRecyclerView;

    public static final int PICK_IMAGE_MULTIPLE = 102;

    @Bind(R.id.mainCategoryProgress)
    ProgressBar mProgress;

    private DatabaseReference mAvailableCategoriesRef;

    public UploadCategoriesFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_main_category, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        initializeDatabaseRefs();
        initializeRecyclerView();
        mProgress.setVisibility(View.VISIBLE);
        return parentView;
    }

    private void initializeRecyclerView()
    {
        mCategoriesRecyclerView.setHasFixedSize(true);
        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.Adapter mAdapter = new FirebaseRecyclerAdapter<Category, SimpleCategoryViewHolder>(
                Category.class,
                R.layout.simple_list_item,
                SimpleCategoryViewHolder.class,
                getCategories())
        {
            @Override
            protected void populateViewHolder(SimpleCategoryViewHolder viewHolder, final Category model, int position)
            {
                mProgress.setVisibility(View.GONE);
                viewHolder.mCategory.setText(model.getCategory());
                viewHolder.mCategory.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
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
            mAvailableCategoriesRef = rootRef.child(bundle.getString(Const.AVAILABLE_CATEGORIES));
        }
    }

    public Query getCategories()
    {
        return mAvailableCategoriesRef.orderByChild("categoryId");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data)
            {
                ArrayList<String> mImagesEncodedList = new ArrayList<>();
                if (data.getData() != null)
                {
                    Uri mImageUri = data.getData();
                    mImagesEncodedList.add(mImageUri.toString());
                }
                else
                {
                    if (data.getClipData() != null)
                    {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++)
                        {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri mImageUri = item.getUri();
                            mImagesEncodedList.add(mImageUri.toString());
                        }
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(PRODUCT_IMAGES, mImagesEncodedList);
                Intent intent = new Intent(getActivity(), ProductUploadActivity.class);
                intent.putExtra(PRODUCT_IMAGES, bundle);
                startActivity(intent);
            }
            else
            {
                Brandfever.toast("You haven't picked Image");
            }
        }
        catch (Exception e)
        {
            Brandfever.toast("Something went wrong");
        }
    }
}