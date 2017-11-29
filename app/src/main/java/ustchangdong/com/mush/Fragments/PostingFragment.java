package ustchangdong.com.mush.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnScrollChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ustchangdong.com.mush.Adapters.CommentAdapter;
import ustchangdong.com.mush.Adapters.CustomAdapter;
import ustchangdong.com.mush.DataClasses.Post;
import ustchangdong.com.mush.DataClasses.PostComment;
import ustchangdong.com.mush.MainActivity;
import ustchangdong.com.mush.R;
import ustchangdong.com.mush.Utils.EndlessRecyclerViewScrollListener;
import ustchangdong.com.mush.Utils.RecyclerViewClickListener;

import static android.widget.PopupWindow.INPUT_METHOD_NEEDED;
import static ustchangdong.com.mush.MainActivity.mAuth;
import static ustchangdong.com.mush.MainActivity.mDatabase;

public class PostingFragment extends Fragment implements RecyclerViewClickListener {
    private static final String TAG = "PostingsFragment";
    private Logger logger = Logger.getLogger("PostingsFragment");

    private final static String POSTING_POSTS_TYPE_NAME = "posting_posts";
    private final static String USER_POSTING_TYPE_NAME = "user-posts";

    Context mContext;

    private CommentAdapter mAdapterComment;
    private LinearLayoutManager layoutManagerComment;
    private RecyclerView mRecyclerViewComment;
    private EndlessRecyclerViewScrollListener scrollListenerComment;

    private CustomAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;

    private DatabaseReference postRef;
    private DatabaseReference commentRef;
    private DatabaseReference postCommentRef;

    private ValueEventListener commentValueEventListener;

    private ArrayList<Object> mRecyclerViewItems = new ArrayList<>();
    private ArrayList<PostComment> mRecyclerViewItemsComment = new ArrayList<>();

    private PopupWindow popWindow;
    private View rootView;

    public FloatingActionButton fab;

    public PostingFragment(){
    }

    @Override
    public void onViewClicked(View view, int position) {
        onShowPopup(rootView, (Post) mRecyclerViewItems.get(position));
    }

    @Override
    public void onRowClicked(int position) {
        onShowPopup(rootView, (Post) mRecyclerViewItems.get(position));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_postings, container, false);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout_posting);
        mRecyclerView = rootView.findViewById(R.id.rv_posting);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                // google refer 해
//        mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
//
//            }
//        });
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dy < 0) {
                            MainActivity.fab.setVisibility(View.VISIBLE);
                        } else if (dy > 0) {
                            MainActivity.fab.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    fab.setVisibility(View.VISIBLE);
                } else if (dy > 0) {
                    fab.setVisibility(View.INVISIBLE);
                }
            }
        });

        postRef = FirebaseDatabase.getInstance().getReference(POSTING_POSTS_TYPE_NAME);
        commentRef = FirebaseDatabase.getInstance().getReference("comment");
        setRecyclerViewAdapter();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        setFloatingActionButton();

        return rootView;
    }

    public void onShowPopup(View v, final Post post){
        LayoutInflater layoutInflater = (LayoutInflater)v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        postCommentRef = commentRef.child(POSTING_POSTS_TYPE_NAME).child(post.getFbdbid());

        final View inflatedView = layoutInflater.inflate(R.layout.fb_popup_layout, null,false);
        mRecyclerViewComment = inflatedView.findViewById(R.id.rv_comment);
        mRecyclerViewComment.setHasFixedSize(true);
        layoutManagerComment = new LinearLayoutManager(rootView.getContext());
        mRecyclerViewComment.setLayoutManager(layoutManagerComment);
        mRecyclerViewComment.setItemAnimator(new DefaultItemAnimator());

        final EditText commentCnt = inflatedView.findViewById(R.id.commentContent);
        final Button commentSendBtn = inflatedView.findViewById(R.id.commentSendButton);

        commentSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentText = commentCnt.getText().toString();
                if (!commentText.isEmpty()){
                    // Create new comment at /comment/$postId/$commentId
                    String key = postCommentRef.push().getKey();
                    PostComment comment = new PostComment(post.getUserid(), commentText, post.getTimestamp());
                    Map<String, Object> commentValues = comment.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + key, commentValues);
                    postCommentRef.updateChildren(childUpdates);

                    commentCnt.setText("");

                    int cmtNum = 0;
                    PostComment lastPostItem;
                    Double lastTimeStamp = 0.0;
                    if (!mRecyclerViewItemsComment.isEmpty()){
                        lastPostItem = (PostComment) mRecyclerViewItemsComment.get(mRecyclerViewItemsComment.size() - 1);
                        lastTimeStamp = lastPostItem.getTimestamp();
                    }
                    ValueEventListener commentVel = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int cmtNum = 0;
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                cmtNum += 1;
                            }
                            String newNumComment = String.valueOf(cmtNum);
                            Map<String, Object> childUpdate = new HashMap<>();
                            childUpdate.put("comment", newNumComment);
                            postRef.child(post.getFbdbid()).updateChildren(childUpdate);

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    postCommentRef.addListenerForSingleValueEvent(commentVel);

                    mRecyclerViewComment.smoothScrollToPosition(mRecyclerViewItemsComment.size());
                }
            }
        });

        // get device size
        Display display = ((Activity)v.getContext()).getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int statusBarHeight = (int) (24 * v.getContext().getResources().getDisplayMetrics().density);
        final int mDeviceHeight = size.y - statusBarHeight;

        // fill the data to the list items
        setRecyclerViewAdapterComment();

        // set height depends on the device size
        popWindow = new PopupWindow(inflatedView, size.x, mDeviceHeight, true );
        // set a background drawable with rounders corners
        popWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.fb_popup_bg));
        // make it focusable to show the keyboard to enter in `EditText`
        popWindow.setFocusable(true);
        // make it outside touchable to dismiss the popup window
        popWindow.setOutsideTouchable(true);

        popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popWindow.setInputMethodMode(INPUT_METHOD_NEEDED);

//        popWindow.setAnimationStyle(-1);

//      Attempt to allow swipe to dismiss feature.
//        listView.setOnTouchListener(new View.OnTouchListener() {
//            private int dx = 0;
//            private int dy = 0;
//
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        dx = (int) event.getX();
//                        dy = (int) event.getY();
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                        int xp = (int) event.getRawX();
//                        int yp = (int) event.getRawY();
//                        int sides = (xp - dx);
//                        int topBot = (yp - dy);
//                        Log.d("test", "x: " + sides + " y: " + topBot);
//
//                        listView.pos
//
//                        popWindow.setHeight(mDeviceHeight - topBot);
//                        break;
//                }
//                return true;
//            }
//        });

        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                postCommentRef.removeEventListener(commentValueEventListener);
                mRecyclerViewItemsComment.clear();
            }
        });

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
    }

    void setRecyclerViewAdapterComment(){
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListenerComment = new EndlessRecyclerViewScrollListener(layoutManagerComment) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new mRecyclerViewItems needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApiComment(totalItemsCount, rootView);
            }
        };
        // Adds the scroll listener to RecyclerView
        mRecyclerViewComment.addOnScrollListener(scrollListenerComment);
        mAdapterComment = new CommentAdapter(mContext, getCommentData(), this);
        mRecyclerViewComment.setAdapter(mAdapterComment);

        commentValueEventListener = postCommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!mRecyclerViewItemsComment.isEmpty()){
                    mRecyclerViewItemsComment.clear();
                }

                ArrayList<PostComment> temp = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    PostComment pc = ds.getValue(PostComment.class);
                    temp.add(pc);
                }

//                Collections.reverse(temp);    // Comments should show up on bottom.
                mRecyclerViewItemsComment.addAll(temp);

                if (mAdapterComment != null){
                    mAdapterComment.notifyDataSetChanged();
                }
                scrollListener.resetState();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecyclerViewComment.setAdapter(mAdapterComment);
    }

    private void setRecyclerViewAdapter(){
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new mRecyclerViewItems needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(totalItemsCount);
            }
        };
        // Adds the scroll listener to RecyclerView
        mRecyclerView.addOnScrollListener(scrollListener);
        mAdapter = new CustomAdapter(mContext, getData(), this);
        mRecyclerView.setAdapter(mAdapter);
    }


    // Append the next page of mRecyclerViewItems into the adapter
    // This method probably sends out a network request and appends new mRecyclerViewItems items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated mRecyclerViewItems
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new mRecyclerViewItems objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        logger.info("LoadNext Called at offset: " + offset);
        Post lastPostItem;
        Double lastTimeStamp = 0.0;
        lastPostItem = (Post) mRecyclerViewItems.get(mRecyclerViewItems.size() - 1);
        lastTimeStamp = lastPostItem.getTimestamp();
        postRef.orderByChild("timestamp").endAt(lastTimeStamp-1).limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                logger.info("Before Fetch dataset.size(): " + mRecyclerViewItems.size());
                fetchData(dataSnapshot);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void loadNextDataFromApiComment(int offset, final View rootView) {
        logger.info("LoadNext Called at offset: " + offset);
        PostComment lastPostItem;
        Double lastTimeStamp = 0.0;
        lastPostItem = (PostComment) mRecyclerViewItemsComment.get(0);  // Get latest at bottom currently shown.
        lastTimeStamp = lastPostItem.getTimestamp();
        postCommentRef.orderByChild("timestamp").endAt(lastTimeStamp-1).limitToFirst(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                logger.info("Before Fetch dataset.size(): " + mRecyclerViewItemsComment.size());
                ArrayList<PostComment> temp = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostComment pc = ds.getValue(PostComment.class);
                    temp.add(pc);
                }
//                Collections.reverse(temp);
                mRecyclerViewItemsComment.addAll(temp);
                mAdapterComment.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private ArrayList<Object> getData(){
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!mRecyclerViewItems.isEmpty()) {
                    mRecyclerViewItems.clear();
                }
                fetchData(dataSnapshot);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                scrollListener.resetState();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        postRef.limitToLast(7).addValueEventListener(vel);
        return mRecyclerViewItems;
    }

    private ArrayList<PostComment> getCommentData(){
        commentValueEventListener = postCommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!mRecyclerViewItemsComment.isEmpty()){
                    mRecyclerViewItemsComment.clear();
                }

                ArrayList<PostComment> temp = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    PostComment pc = ds.getValue(PostComment.class);
                    temp.add(pc);
                }

                Collections.reverse(temp);
                mRecyclerViewItemsComment.addAll(temp);

                if (mAdapterComment != null){
                    mAdapterComment.notifyDataSetChanged();
                }
                scrollListener.resetState();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return mRecyclerViewItemsComment;
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        logger.info("fetchData called.");
        ArrayList<Post> temp = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            Post bk = ds.getValue(Post.class);
            temp.add(bk);
        }
        Collections.reverse(temp);
        mRecyclerViewItems.addAll(temp);
        logger.info("After fetchData - mRecyclerViewItems.size(): " + mRecyclerViewItems.size());

    }

    void refreshItems() {
        ArrayList<Object> temp = new ArrayList<>();
        temp.addAll(mRecyclerViewItems);
        try {
            mRecyclerViewItems.clear();
            getData();
            scrollListener.resetState();
        } catch (Exception e) {
            e.printStackTrace();
            mRecyclerViewItems.addAll(temp);
        }
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setFloatingActionButton(){
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.dialog_add_post, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

//                final EditText editTextTitle = promptsView.findViewById(R.id.editTextTitle);
                final EditText editTextContent = promptsView.findViewById(R.id.editTextContent);
//                final RadioGroup radioGroupCategory = promptsView.findViewById(R.id.rg_category);

                try {
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
//                                            String title = editTextTitle.getText().toString();
                                            String content = editTextContent.getText().toString();
                                            if (!content.isEmpty()){
//                                                writeNewPost(mAuth.getCurrentUser().getUid(), title, content, radioGroupCategory.getCheckedRadioButtonId());
                                                writeNewPost(mAuth.getCurrentUser().getUid(), null, content, -1);
                                                Snackbar.make(rootView.findViewById(R.id.placeSnackBar2), "Post Successfully Added", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            }
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    })

                            .create()
                            .show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }



    private void writeNewPost(String userId, String title, String content, int radioGroupCategory) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.push().getKey();
        Post post = new Post(key, userId, title, content, "0"); // Comment is set to 0 for new posts.
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();

        switch(radioGroupCategory) {
//            case R.id.rb_posting:
//                childUpdates.put("/" + POSTING_POSTS_TYPE_NAME + "/" + key, postValues);
//                childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
//                break;
//            case R.id.rb_market:
//                childUpdates.put("/" + POSTING_MARKET_TYPE_NAME + "/" + key, postValues);
//                childUpdates.put("/user-market/" + userId + "/" + key, postValues);
//                break;
//            case R.id.rb_all:
//                childUpdates.put("/all/" + key, postValues);
//                childUpdates.put("/user-all/" + userId + "/" + key, postValues);
//                break;
            default:
                childUpdates.put("/" + POSTING_POSTS_TYPE_NAME + "/" + key, postValues);
                childUpdates.put("/" + USER_POSTING_TYPE_NAME + "/" + userId + "/" + key, postValues);
                break;
        }

        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
