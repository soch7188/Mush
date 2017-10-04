package ustchangdong.com.mush.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import ustchangdong.com.mush.Utils.EndlessRecyclerViewScrollListener;
import ustchangdong.com.mush.DataClasses.Post;
import ustchangdong.com.mush.DataClasses.PostComment;
import ustchangdong.com.mush.R;
import ustchangdong.com.mush.Utils.RecyclerViewClickListener;

import static android.widget.PopupWindow.INPUT_METHOD_NEEDED;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllFragment extends Fragment implements RecyclerViewClickListener {
    private static final String TAG = "AllFragment";
    private Logger logger = Logger.getLogger("AllFragment");
    Context mContext;

    private static CommentAdapter mAdapterComment;
    private LinearLayoutManager layoutManagerComment;
    private RecyclerView mRecyclerViewComment;
    private EndlessRecyclerViewScrollListener scrollListenerComment;

    private static CustomAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;

    private DatabaseReference postRef;
    private DatabaseReference commentRef;
    private DatabaseReference postCommentRef;

    private ValueEventListener commentValueEventListener;

    private static ArrayList<Object> mRecyclerViewItems = new ArrayList<>();
    private static ArrayList<PostComment> mRecyclerViewItemsComment = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private PopupWindow popWindow;
    private View rootView;

    public AllFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewClicked(View view, int position) {
        onShowPopup(rootView, (Post) mRecyclerViewItems.get(position));
    }

    @Override
    public void onRowClicked(int position) {
        onShowPopup(rootView, (Post) mRecyclerViewItems.get(position));
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment all.
     */
    // TODO: Rename and change types and number of parameters
    public static AllFragment newInstance(String param1, String param2) {
        AllFragment fragment = new AllFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_all, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_all);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_all);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        postRef = FirebaseDatabase.getInstance().getReference("all");
        commentRef = FirebaseDatabase.getInstance().getReference("comment");
        setRecyclerViewAdapter(rootView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
        return rootView;
    }

    public void onShowPopup(View v, final Post post){
        LayoutInflater layoutInflater = (LayoutInflater)v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        postCommentRef = commentRef.child("all").child(post.getFbdbid());

        final View inflatedView = layoutInflater.inflate(R.layout.fb_popup_layout, null,false);
        mRecyclerViewComment = inflatedView.findViewById(R.id.rv_comment);
        mRecyclerViewComment.setHasFixedSize(true);
        layoutManagerComment = new LinearLayoutManager(rootView.getContext());
        mRecyclerViewComment.setLayoutManager(layoutManagerComment);
        mRecyclerViewComment.setItemAnimator(new DefaultItemAnimator());

        final EditText commentCnt = inflatedView.findViewById(R.id.commentContent);
        Button commentSendBtn = inflatedView.findViewById(R.id.commentSendButton);

        commentSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create new comment at /comment/$postId/$commentId
                String key = postCommentRef.push().getKey();
                PostComment comment = new PostComment(post.getUserid(), commentCnt.getText().toString(), post.getTimestamp());
                Map<String, Object> commentValues = comment.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + key, commentValues);
                postCommentRef.updateChildren(childUpdates);

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
        });

        Display display = ((Activity)v.getContext()).getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int statusBarHeight = (int) (24 * v.getContext().getResources().getDisplayMetrics().density);
        final int mDeviceHeight = size.y - statusBarHeight;

        setRecyclerViewAdapterComment();

        popWindow = new PopupWindow(inflatedView, size.x, mDeviceHeight, true );
        popWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.fb_popup_bg));
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popWindow.setInputMethodMode(INPUT_METHOD_NEEDED);

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

    private void setRecyclerViewAdapter(final View rootView){
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(totalItemsCount, rootView);
            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);
        mAdapter = new CustomAdapter(mContext, getData(), this);
        mRecyclerView.setAdapter(mAdapter);
    }


    public void loadNextDataFromApi(int offset, final View rootView) {
        Post lastPostItem;
        Double lastTimeStamp = 0.0;
        lastPostItem = (Post) mRecyclerViewItems.get(mRecyclerViewItems.size() - 1);
        lastTimeStamp = lastPostItem.getTimestamp();
        postRef.orderByChild("timestamp").endAt(lastTimeStamp-1).limitToLast(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fetchData(dataSnapshot);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void loadNextDataFromApiComment(int offset, final View rootView) {
        PostComment lastPostItem;
        Double lastTimeStamp = 0.0;
        lastPostItem = (PostComment) mRecyclerViewItemsComment.get(mRecyclerViewItemsComment.size() - 1);
        lastTimeStamp = lastPostItem.getTimestamp();
        postCommentRef.orderByChild("timestamp").endAt(lastTimeStamp-1).limitToFirst(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<PostComment> temp = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    PostComment pc = ds.getValue(PostComment.class);
                    temp.add(pc);
                }
                Collections.reverse(temp);
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
        ArrayList<Post> temp = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            Post bk = ds.getValue(Post.class);
            temp.add(bk);
        }
        Collections.reverse(temp);
        mRecyclerViewItems.addAll(temp);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mContext = context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}