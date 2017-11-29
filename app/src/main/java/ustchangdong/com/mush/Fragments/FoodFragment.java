package ustchangdong.com.mush.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ustchangdong.com.mush.Adapters.CommentAdapter;
import ustchangdong.com.mush.Adapters.FoodAdapter;
import ustchangdong.com.mush.DataClasses.FoodPost;
import ustchangdong.com.mush.DataClasses.PostComment;
import ustchangdong.com.mush.R;
import ustchangdong.com.mush.Utils.EndlessRecyclerViewScrollListener;
import ustchangdong.com.mush.Utils.RecyclerViewClickListener;

import static android.app.Activity.RESULT_OK;
import static android.widget.PopupWindow.INPUT_METHOD_NEEDED;
import static ustchangdong.com.mush.MainActivity.mAuth;
import static ustchangdong.com.mush.MainActivity.mDatabase;

public class FoodFragment extends Fragment implements RecyclerViewClickListener {
    private static final String TAG = "FoodFragment";
    private Logger logger = Logger.getLogger("FoodFragment");

    private final static String POSTING_POSTS_TYPE_NAME = "posting_food";
    private final static String USER_POSTING_TYPE_NAME = "user-food";

    Context mContext;

    private CommentAdapter mAdapterComment;
    private LinearLayoutManager layoutManagerComment;
    private RecyclerView mRecyclerViewComment;
    private EndlessRecyclerViewScrollListener scrollListenerComment;

    private FoodAdapter mAdapter;
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
    ProgressDialog progressDialog;


    // Image Upload related
    private static final int SELECT_PHOTO = 100;
    Uri selectedImage;
    FirebaseStorage storage;
    StorageReference storageRef,imageRef;
    UploadTask uploadTask;
    ImageView addPostImageView;
    private Button choose;
    private Uri filePath;

    public FoodFragment(){
    }

    @Override
    public void onViewClicked(View view, int position) {
        onShowPopup(rootView, (FoodPost) mRecyclerViewItems.get(position));
    }

    @Override
    public void onRowClicked(int position) {
        onShowPopup(rootView, (FoodPost) mRecyclerViewItems.get(position));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_food, container, false);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout_food);
        mRecyclerView = rootView.findViewById(R.id.rv_food);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

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

        //accessing the firebase storage
        storage = FirebaseStorage.getInstance();
        //creates a storage reference
        storageRef = storage.getReference();

        setFloatingActionButton();
        return rootView;
    }

    public void onShowPopup(View v, final FoodPost post){
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
                    String key = postCommentRef.push().getKey();
                    PostComment comment = new PostComment(post.getUserid(), commentText, post.getTimestamp());
                    Map<String, Object> commentValues = comment.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + key, commentValues);
                    postCommentRef.updateChildren(childUpdates);

                    commentCnt.setText("");
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

        popWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
    }

    void setRecyclerViewAdapterComment(){
        scrollListenerComment = new EndlessRecyclerViewScrollListener(layoutManagerComment) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApiComment(totalItemsCount, rootView);
            }
        };
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
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    PostComment pc = ds.getValue(PostComment.class);
                    temp.add(pc);
                }

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
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(totalItemsCount);
            }
        };
        mRecyclerView.addOnScrollListener(scrollListener);
        mAdapter = new FoodAdapter(mContext, getData(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void loadNextDataFromApi(int offset) {
        FoodPost lastPostItem;
        Double lastTimeStamp = 0.0;
        lastPostItem = (FoodPost) mRecyclerViewItems.get(mRecyclerViewItems.size() - 1);
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
        lastPostItem = (PostComment) mRecyclerViewItemsComment.get(0);  // Get latest at bottom currently shown.
        lastTimeStamp = lastPostItem.getTimestamp();
        postCommentRef.orderByChild("timestamp").endAt(lastTimeStamp-1).limitToFirst(7).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<PostComment> temp = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostComment pc = ds.getValue(PostComment.class);
                    temp.add(pc);
                }
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
                for (DataSnapshot ds : dataSnapshot.getChildren()){
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
        ArrayList<FoodPost> temp = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            FoodPost bk = ds.getValue(FoodPost.class);
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

    boolean imageUploaded = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
//            Picasso.with(mContext).load("file://"+filePath).into(addPostImageView);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), filePath);
                addPostImageView.setImageBitmap(bitmap);
                imageUploaded = true;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void setFloatingActionButton(){
        fab = rootView.findViewById(R.id.fab_food);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.dialog_add_food_post, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

                alertDialogBuilder.setView(promptsView);
                final EditText foodEditTextContent = promptsView.findViewById(R.id.foodEditTextContent);
                addPostImageView = (ImageView) promptsView.findViewById(R.id.addPostFoodImageView);

                choose = (Button)promptsView.findViewById(R.id.chooseImage);
                choose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
                    }
                });

                try {
                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            String content = foodEditTextContent.getText().toString();
                                            if (!content.isEmpty() || !addPostImageView.isActivated()){
                                                writeNewPost(mAuth.getCurrentUser().getUid(), null, content, imageUploaded);
                                                Snackbar.make(rootView.findViewById(R.id.placeSnackBar2), "Post Successfully Added", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                                imageUploaded = false;
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

    private void writeNewPost(String userId, String title, String content, boolean imgUploaded) {
        String key = mDatabase.push().getKey();
        FoodPost post = new FoodPost(key, userId, title, content, "0", imgUploaded); // Comment is set to 0 for new posts.
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + POSTING_POSTS_TYPE_NAME + "/" + key, postValues);
        childUpdates.put("/" + USER_POSTING_TYPE_NAME + "/" + userId + "/" + key, postValues);

        final Map<String, Object> updates = childUpdates;
        if(filePath != null){
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            logger.info("path: " + "foodPictures/" + key + ".jpg");
            StorageReference picsRef = storageRef.child("foodPictures/" + key + ".jpg");

            picsRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),"Successfully posted image",Toast.LENGTH_SHORT).show();
                            mDatabase.updateChildren(updates);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),"Failed to upload image",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });

        } else {
            mDatabase.updateChildren(childUpdates);
        }
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
