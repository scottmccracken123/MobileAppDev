package placemate.placemate;

/*
Class override for custom adapter view.
Used in myplaces activity
Allows for images and different configurations of texts to be used with mylist.xml
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ImageCursorAdapter extends ArrayAdapter<String> {

    //
    private final Activity context;
    private final ArrayList<String> itemname;
    private final ArrayList<byte[]> imgid;
    private final ArrayList<String> itemtype;

    //variables passed in from DB
    public ImageCursorAdapter(Activity context, ArrayList<String> itemname, ArrayList<String> itemtype, ArrayList<byte[]> imgid) {
        super(context, R.layout.mylist, itemname);

        this.context=context;
        this.itemname=itemname;
        this.itemtype=itemtype;
        this.imgid=imgid;
    }

    //setup DB
    public View getView(int position,View view,ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        //setup text on listview
        TextView txtTitle = (TextView) rowView.findViewById(R.id.placeName);
        TextView txtType = (TextView) rowView.findViewById(R.id.placeType);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);


        txtType.setText(itemtype.get(position));
        txtTitle.setText(itemname.get(position));

        //setup imageview on list
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap downloadedImg = BitmapFactory.decodeByteArray(imgid.get(position), 0, imgid.get(position).length, options);
            imageView.setImageBitmap(downloadedImg);
        } catch (Exception e){
            imageView.setImageResource(R.drawable.default_list_icon);
            e.printStackTrace();
        }
        //return row
        return rowView;

    };
}