package comp5216.sydney.edu.au.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    //request codes
    public final int EDIT_ITEM_REQUEST_CODE = 647;
    public final int TAKE_PHOTO_CODE = 648;

    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    private ArrayList<ImageInfo> imageList;
    private GridViewAdapter gridViewAdapter;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = (GridView)findViewById(R.id.gridview);
        readImage();
        sortByDate();
        for (ImageInfo i : imageList) {
            System.out.println(i.getDate());
        }
        gridViewAdapter = new GridViewAdapter(this, imageList);
        gridView.setAdapter(gridViewAdapter);

        setupGridViewListener();
    }

    public void readImage() {
        if (!marshmallowPermission.checkPermissionForReadfiles()) {
            marshmallowPermission.requestPermissionForReadfiles();
        } else {
            imageList = new ArrayList<ImageInfo>();
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            try {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    ImageInfo imageInfo = new ImageInfo(path);
                    imageList.add(imageInfo);
                }
            } catch (Exception ex) {
                Log.e("read photo error", ex.getStackTrace().toString());
            }
        }

    }

    private void setupGridViewListener() {

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String photoPath = gridViewAdapter.getItemPath(position);
                Intent intent = new Intent(MainActivity.this, EditImageActivity.class);

                if (intent != null) {
                    intent.putExtra("path", photoPath);

                    startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE);
                }
            }
        });
    }

    public void onTakePhotoClick(View v) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()
                || !marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to take a picture and return control to the calling application

            Intent intent = new Intent(MainActivity.this, CameraActivity.class);

            startActivityForResult(intent,TAKE_PHOTO_CODE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EDIT_ITEM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ImageInfo imageInfo = new ImageInfo(data.getStringExtra("path"));
                imageList.add(imageInfo);
                sortByDate();
                gridViewAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == TAKE_PHOTO_CODE) {
            ImageInfo imageInfo = new ImageInfo(data.getStringExtra("path"));
            imageList.add(imageInfo);
            sortByDate();
            gridViewAdapter.notifyDataSetChanged();
        }

    }

    public void sortByDate() {
        // A method to sort items by date
        Collections.sort(imageList, new Comparator<ImageInfo>() {

            @Override
            public int compare(ImageInfo o1, ImageInfo o2) {
                boolean flag = o1.getDate().before(o2.getDate());
                if (flag) return 1;
                else return -1;
            }
        });
    }

}
