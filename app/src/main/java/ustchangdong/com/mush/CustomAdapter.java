package ustchangdong.com.mush;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.ArrayList;
import java.util.logging.Logger;

import static ustchangdong.com.mush.MainActivity.ITEMS_PER_AD;

/**
 * Created by ziwon on 2017. 9. 27..
 */

public class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CustomAdapter";
    private Logger logger = Logger.getLogger("CustomAdapter");

    private RecyclerViewClickListener listener;
    private Utils utils;

    // A menu item view type.
    private static final int POST_ITEM_VIEW_TYPE = 0;

    // The Native Express ad view type.
    private static final int NATIVE_EXPRESS_AD_VIEW_TYPE = 1;

    // An Activity's Context.
    private final Context mContext;

    // The list of Native Express ads and menu items.
    private ArrayList<Object> mRecyclerViewItems;

    public CustomAdapter(Context context, ArrayList<Object> data, RecyclerViewClickListener listener) {
        this.mContext = context;
        this.mRecyclerViewItems = data;
        this.listener = listener;
    }

    public static class PostItemViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        TextView textViewContent;
        TextView textViewComment;
        NativeExpressAdView adView;
        CardView adCardView;


        public PostItemViewHolder(View itemView,final RecyclerViewClickListener listener) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.textViewContent = (TextView) itemView.findViewById(R.id.textViewContent);
            this.textViewComment = itemView.findViewById(R.id.textViewComments);
            this.adView = (NativeExpressAdView) itemView.findViewById(R.id.adMobView);
//            this.adCardView = (CardView) itemView.findViewById(R.id.card_view2);
//            this.adCardView.setVisibility(View.GONE);
            this.adView.setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                        listener.onRowClicked(getAdapterPosition());
                }
            });

            textViewComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                        listener.onViewClicked(v, getAdapterPosition());
                }
            });
        }

        public void setTextViewTitle(String title) {
            textViewTitle.setText(title);
        }

        public void setTextViewContent(String content) {
            textViewContent.setText(content);
        }
    }
    /**
     * Creates a new view for a menu item view or a Native Express ad view
     * based on the viewType. This method is invoked by the layout manager.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View postView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_post, viewGroup, false);
        return new PostItemViewHolder(postView, listener);
    }

    /**
     *  Replaces the content in the views that make up the menu item view and the
     *  Native Express ad view. This method is invoked by the layout manager.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostItemViewHolder postItemHolder = (PostItemViewHolder) holder;
        Post postItem = (Post) mRecyclerViewItems.get(position);

        // Add the menu item details to the menu item view.
        postItemHolder.textViewTitle.setText(Utils.ProcessUnixTime((long) postItem.getTimestamp()));
        postItemHolder.textViewContent.setText(postItem.getContent());
        postItemHolder.textViewComment.setText(postItem.getComment() + " Comments");

        logger.info("onBindViewHolder position: " + position);
        if (postItemHolder.getAdapterPosition() % ITEMS_PER_AD == 0){
//            postItemHolder.adCardView.setVisibility(View.VISIBLE);
            postItemHolder.adView.setVisibility(View.VISIBLE);
            AdRequest request = new AdRequest.Builder().addTestDevice("AAF419D21A886806E91141DD6F8717B8").build();
//            AdRequest request = new AdRequest.Builder().build();
            postItemHolder.adView.loadAd(request);
        } else {
            postItemHolder.adView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mRecyclerViewItems.size();
    }


}