package ustchangdong.com.mush.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.logging.Logger;

import ustchangdong.com.mush.DataClasses.FoodPost;
import ustchangdong.com.mush.R;
import ustchangdong.com.mush.Utils.RecyclerViewClickListener;
import ustchangdong.com.mush.Utils.Utils;

import static ustchangdong.com.mush.MainActivity.ITEMS_PER_AD;

/**
 * Created by ziwon on 2017. 9. 27..
 *
 */

public class FoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "FoodAdapter";
    private Logger logger = Logger.getLogger("FoodAdapter");

    private RecyclerViewClickListener listener;

    private final Context mContext;
    private ArrayList<Object> mRecyclerViewItems;

    public FoodAdapter(Context context, ArrayList<Object> data, RecyclerViewClickListener listener) {
        this.mContext = context;
        this.mRecyclerViewItems = data;
        this.listener = listener;
    }

    public static class FoodItemViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        TextView textViewContent;
        TextView textViewComment;
        ImageView foodImage;
        NativeExpressAdView adView;
        CardView adCardView;


        public FoodItemViewHolder(View itemView,final RecyclerViewClickListener listener) {
            super(itemView);
            this.textViewTitle = (TextView) itemView.findViewById(R.id.foodTextViewTitle);
            this.textViewContent = (TextView) itemView.findViewById(R.id.foodTextViewContent);
            this.textViewComment = itemView.findViewById(R.id.foodTextViewComments);
            this.foodImage = (ImageView) itemView.findViewById(R.id.foodImage);
            this.adView = (NativeExpressAdView) itemView.findViewById(R.id.foodAdMobView);
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
        final View postView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_post_food, viewGroup, false);
        return new FoodItemViewHolder(postView, listener);
    }

    /**
     *  Replaces the content in the views that make up the menu item view and the
     *  Native Express ad view. This method is invoked by the layout manager.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FoodItemViewHolder foodItemHolder = (FoodItemViewHolder) holder;
        FoodPost foodItem = (FoodPost) mRecyclerViewItems.get(position);

        // Add the menu item details to the menu item view.
        foodItemHolder.textViewTitle.setText(Utils.ProcessUnixTime((long) foodItem.getTimestamp()));
        foodItemHolder.textViewContent.setText(foodItem.getContent());
        foodItemHolder.textViewComment.setText(foodItem.getComment() + " Comments");

        // --------

        StorageReference ref = FirebaseStorage.getInstance().getReference().child("foodPictures/" + foodItem.getFbdbid() + ".jpg");
        logger.info("Path: " + FirebaseStorage.getInstance().getReference().child("foodPictures/" + foodItem.getFbdbid()).toString() + ".jpg");
//        Glide.with(mContext)
//                .load(ref)
//                .into(foodItemHolder.foodImage);
//        Picasso.with(mContext)
//                .load(ref)
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.error)
//                .resize(screenWidth, imageHeight)
//                .centerInside()
//                .into(imageView);
//        Picasso.with(mContext).load("file://"+filePath).into(addPostImageView);

        if (foodItem.isHasImage()){
            try {
                Glide.with(mContext)
                        .using(new FirebaseImageLoader())
                        .load(ref)
                        .into(foodItemHolder.foodImage);
//                Picasso.with(mContext).load(ref.getDownloadUrl().getResult()).into(foodItemHolder.foodImage);
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), ref.getDownloadUrl().getResult());
//                foodItemHolder.foodImage.setImageBitmap(bitmap);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if (foodItemHolder.getAdapterPosition() % ITEMS_PER_AD == 0){
            foodItemHolder.adView.setVisibility(View.VISIBLE);
            AdRequest request = new AdRequest.Builder().build();
            foodItemHolder.adView.loadAd(request);
        } else {
            foodItemHolder.adView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mRecyclerViewItems.size();
    }


}