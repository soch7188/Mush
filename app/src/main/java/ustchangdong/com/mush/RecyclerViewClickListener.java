package ustchangdong.com.mush;

import android.view.View;

/**
 * Created by ziwon on 2017. 10. 3..
 */

public interface RecyclerViewClickListener {

    void onRowClicked(int position);
    void onViewClicked(View v, int position);
}