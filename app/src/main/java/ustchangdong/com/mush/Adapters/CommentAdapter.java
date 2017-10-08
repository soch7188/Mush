package ustchangdong.com.mush.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.logging.Logger;

import ustchangdong.com.mush.DataClasses.PostComment;
import ustchangdong.com.mush.R;
import ustchangdong.com.mush.Utils.RecyclerViewClickListener;
import ustchangdong.com.mush.Utils.Utils;

/**
 * Created by ziwon on 2017. 9. 27..
 */

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CommentAdapter";
    private Logger logger = Logger.getLogger("CommentAdapter");

    private RecyclerViewClickListener listener;

    // An Activity's Context.
    private final Context mContext;

    // The list of Native Express ads and menu items.
    private ArrayList<PostComment> mRecyclerViewItems;

    public CommentAdapter(Context context, ArrayList<PostComment> data, RecyclerViewClickListener listener) {
        this.mContext = context;
        this.mRecyclerViewItems = data;
        this.listener = listener;
    }

    public static class PostItemViewHolder extends RecyclerView.ViewHolder {

        TextView textViewContent;
        TextView textViewTime;


        public PostItemViewHolder(View itemView,final RecyclerViewClickListener listener) {
            super(itemView);
            this.textViewContent = (TextView) itemView.findViewById(R.id.comment_content);
            this.textViewTime = itemView.findViewById(R.id.comment_time);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(listener != null)
//                        listener.onRowClicked(getAdapterPosition());
//                }
//            });
//
//            textViewContent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(listener != null)
//                        listener.onViewClicked(v, getAdapterPosition());
//                }
//            });
        }
    }

    /**
     * Creates a new view for a menu item view or a Native Express ad view
     * based on the viewType. This method is invoked by the layout manager.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View postView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fb_comments_list_item, viewGroup, false);
        return new PostItemViewHolder(postView, listener);
    }

    /**
     *  Replaces the content in the views that make up the menu item view and the
     *  Native Express ad view. This method is invoked by the layout manager.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostItemViewHolder postItemHolder = (PostItemViewHolder) holder;
        PostComment postCommentItem = (PostComment) mRecyclerViewItems.get(position);

        // Add the menu item details to the menu item view.
        postItemHolder.textViewTime.setText(Utils.ProcessUnixTime((long) postCommentItem.getTimestamp()));
        postItemHolder.textViewContent.setText(postCommentItem.getContent());
    }

    @Override
    public int getItemCount() {
        return mRecyclerViewItems.size();
    }

}