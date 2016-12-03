package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.adapters.UsersAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.utils.UserUtil;
import co.thnki.brandfever.view.holders.AccountsViewHolder;

public class UserListFragment extends Fragment
{
    @Bind(R.id.userListRecyclerView)
    RecyclerView mUserListRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    public static final String TAG = "UserListFragment";

    public UserListFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parent = inflater.inflate(R.layout.fragment_user_list, container, false);
        ButterKnife.bind(this, parent);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mUserDbRef = rootRef.child(UserUtil.USER_LIST);
        FirebaseRecyclerAdapter<Accounts, AccountsViewHolder> mAdapter = UsersAdapter.getInstance(mUserDbRef, getActivity());
        mUserListRecyclerView.setAdapter(mAdapter);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUserDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    mProgress.setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return parent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.customerList));
        }
    }
}
