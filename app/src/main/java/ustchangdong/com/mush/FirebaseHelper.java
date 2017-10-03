package ustchangdong.com.mush;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.logging.Logger;

/**
 * Created by ziwon on 2017. 10. 3..
 */

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private Logger logger = Logger.getLogger("FirebaseHelper");

    private static CustomAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference postRef;

//    public void authenticate(final View rootView, FirebaseAuth mAuth, FirebaseAuth.AuthStateListener mAuthListener, DatabaseReference mDatabase){
//        mAuth = FirebaseAuth.getInstance();
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    // User is signed in
//                    mDatabase = FirebaseDatabase.getInstance().getReference();
//                    postRef = FirebaseDatabase.getInstance().getReference("love");
//                    setRecyclerViewAdapter(rootView);
//                    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                        @Override
//                        public void onRefresh() {
//                            refreshItems();
//                        }
//                    });
//                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUserid());
//                } else {
//                    // User is signed out
//                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                }
//            }
//        };
//        mAuth.addAuthStateListener(mAuthListener);
//    }

//    public void setRecyclerViewAdapter(final View rootView){
//        // Retain an instance so that you can call `resetState()` for fresh searches
//        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                // Triggered only when new mRecyclerViewItems needs to be appended to the list
//                // Add whatever code is needed to append new items to the bottom of the list
//                loadNextDataFromApi(totalItemsCount, rootView);
//            }
//        };
//        // Adds the scroll listener to RecyclerView
//        mRecyclerView.addOnScrollListener(scrollListener);
//        mAdapter = new CustomAdapter(mContext, getData(), this);
//        mRecyclerView.setAdapter(mAdapter);
//    }
//
//
//    // Append the next page of mRecyclerViewItems into the adapter
//    // This method probably sends out a network request and appends new mRecyclerViewItems items to your adapter.
//    public void loadNextDataFromApi(int offset, final View rootView) {
//        // Send an API request to retrieve appropriate paginated mRecyclerViewItems
//        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
//        //  --> Deserialize and construct new model objects from the API response
//        //  --> Append the new mRecyclerViewItems objects to the existing set of items inside the array of items
//        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
//        logger.info("LoadNext Called at offset: " + offset);
//        Post lastPostItem;
//        Double lastTimeStamp = 0.0;
//        lastPostItem = (Post) mRecyclerViewItems.get(mRecyclerViewItems.size() - 1);
//        lastTimeStamp = lastPostItem.getTimestamp();
//        postRef.orderByChild("timestamp").endAt(lastTimeStamp-1).limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                logger.info("Before Fetch dataset.size(): " + mRecyclerViewItems.size());
//                fetchData(dataSnapshot);
//                mAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    public ArrayList<Object> getData(){
//        logger.info("getData called.");
//        postRef.limitToLast(7).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (!mRecyclerViewItems.isEmpty()){
//                    mRecyclerViewItems.clear();
//                }
//                fetchData(dataSnapshot);
//                if (mAdapter != null){
//                    mAdapter.notifyDataSetChanged();
//                }
//                scrollListener.resetState();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        return mRecyclerViewItems;
//    }
//
//    public void fetchData(DataSnapshot dataSnapshot) {
//        logger.info("fetchData called.");
//        ArrayList<Post> temp = new ArrayList<>();
//        for (DataSnapshot ds : dataSnapshot.getChildren())
//        {
//            Post bk = ds.getValue(Post.class);
//            temp.add(bk);
//        }
//        Collections.reverse(temp);
//        mRecyclerViewItems.addAll(temp);
//        logger.info("After fetchData - mRecyclerViewItems.size(): " + mRecyclerViewItems.size());
//
//    }
//
//    void refreshItems() {
//        ArrayList<Object> temp = new ArrayList<>();
//        temp.addAll(mRecyclerViewItems);
//        try {
//            mRecyclerViewItems.clear();
//            getData();
//            scrollListener.resetState();
//        } catch (Exception e) {
//            e.printStackTrace();
//            mRecyclerViewItems.addAll(temp);
//        }
//        onItemsLoadComplete();
//    }
//
//    void onItemsLoadComplete() {
//        mSwipeRefreshLayout.setRefreshing(false);
//    }

}
