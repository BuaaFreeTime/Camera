package comp5216.sydney.edu.au.camera;

import android.content.Context;


import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;


public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ImageInfo> imageList;

    public GridViewAdapter(Context context, ArrayList<ImageInfo> appImageInfoList) {
        this.context = context;
        this.imageList = appImageInfoList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    public String getItemPath(int position) {
        return imageList.get(position).getImagePath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.image_gridview, null);
            imageView = (ImageView) convertView.findViewById(R.id.imagegredview);
            convertView.setTag(imageView);
        }
        else {
            imageView = (ImageView) convertView.getTag();
        }

        imageView.setImageURI(imageList.get(position).getUri());

        return convertView;
    }
}
