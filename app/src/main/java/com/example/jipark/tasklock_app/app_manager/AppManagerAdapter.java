package com.example.jipark.tasklock_app.app_manager;

/**
 * Created by Scott on 10/13/2017.
 */
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import com.example.jipark.tasklock_app.R;

import android.widget.ImageView;
import android.widget.Toast;

public class AppManagerAdapter extends RecyclerView.Adapter<AppManagerAdapter.AppManagerHolder> {

    //private List<App> appList;
    List<String> labelList;
    List<Drawable> iconList;


    public AppManagerAdapter(List<String> labelList, List<Drawable> iconList) {
        this.labelList = labelList;
        this.iconList = iconList;
    }

    @Override
    public AppManagerAdapter.AppManagerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_manager_row,parent,false);
        AppManagerHolder viewHolder = new  AppManagerHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AppManagerAdapter.AppManagerHolder holder, int position) {
        //App app = appList.get(position);//breaks here null position:0
        holder.text.setText(labelList.get(position).toString());
        holder.image.setImageDrawable(iconList.get(position));
    }

    @Override
    public int getItemCount() {
        return labelList.size();
    }

    public static class AppManagerHolder extends RecyclerView.ViewHolder{

        protected TextView text;
        protected ImageView image;

        public AppManagerHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text_id);
            image = (ImageView) itemView.findViewById(R.id.image_id);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"You clicked "+ text.getText() , Toast.LENGTH_SHORT).show();

//                    appList.remove(position);
//                    notifyItemRemoved(position);
//                    notifyItemRangeChanged(position, appList.size());
//                    try {
//                        mAdapterCallback.onMethodCallback();
//                    }
//                    catch (ClassCastException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }
    }
}