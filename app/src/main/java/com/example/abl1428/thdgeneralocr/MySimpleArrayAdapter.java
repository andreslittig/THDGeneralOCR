package com.example.abl1428.thdgeneralocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MySimpleArrayAdapter extends ArrayAdapter<ScanResult> {
    private final Context context;
    private final ScanResult[] values;
    private Mat img;

    public MySimpleArrayAdapter(Context context, ScanResult[] values, Mat img) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.img = img;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        imageView.setImageBitmap(getSubBit(position));
        textView.setText(values[position].getTextContent());
        // change the icon for Windows and iPhone
        //String s = values[position];
//        if (s.startsWith("iPhone")) {
//            imageView.setImageResource(R.drawable.no);
//        } else {
//            imageView.setImageResource(R.drawable.ok);
//        }

        return rowView;
    }

    public Bitmap getSubBit(int index) {
        Bitmap bmp = null;
        try {
            //Log.d("ITEM "+index,""+values[index].getBounds()[1]+", "+values[index].getBounds()[3]+", "+values[index].getBounds()[0]+", "+values[index].getBounds()[2]);
            Mat sub =  img.submat(values[index].getBounds()[1],values[index].getBounds()[3],values[index].getBounds()[0],values[index].getBounds()[2]);
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
            //Imgproc.cvtColor(sub, sub, Imgproc.COLOR_GRAY2RGBA, 4);
            bmp = Bitmap.createBitmap(sub.cols(), sub.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(sub, bmp);
        }
        catch (CvException e){
            Log.d("Exception", e.getMessage());}

        return bmp;
    }
}
