package comp5216.sydney.edu.au.camera;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Date;

public class ImageInfo {
    // A entity class save the image's information

    private String imagePath;  // Image's path
    private Bitmap bitmap;     // Image's bitmap
    private Date date;         // The last modified date
    private Uri uri;           // the item's url

    public ImageInfo (String imagePath) {
        this.imagePath = imagePath;
        File file = new File(imagePath);
        this.date = new Date(file.lastModified());
        this.uri = Uri.fromFile(new File(imagePath));


    }

    public Uri getUri() {
        return uri;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Date getDate() {
        return date;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}
