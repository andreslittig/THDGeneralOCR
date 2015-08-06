package com.example.abl1428.thdgeneralocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;


import android.graphics.Rect;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by OXS3462 on 6/4/2015.
 */


// TODO:
//    1. Access image
//    2. OCR the image w/ proper settings for tesseract
//    3. Retrieve data w/ regex
//    4. Populate form w/ data


public class TesseractActivity extends Activity {

    public static final String FILEPATH = "FILEPATH";
    private static final String TAG = "TesseractActivity";
    private HashMap<String, ArrayList<String>> out;
    private double numGotten = 0;
    private String filepath;
    private String computedState;

    public View.OnClickListener getOnClickListener(final View anchor, final ArrayList<String> menuOptions) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(TesseractActivity.this, anchor);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());
                for(String item : menuOptions) {
                    popup.getMenu().add(item);
                }

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        ((TextView)anchor).setText(item.getTitle());
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        };
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tess);
        if (getActionBar() != null && getActionBar().isShowing()) {
            getActionBar().hide();
        }
        out = new HashMap<String, ArrayList<String>>();
        Intent data = getIntent();
        filepath = data.getExtras().getString(FILEPATH);
        TesseractProcessing tesseract = new TesseractProcessing(TesseractActivity.this);
        tesseract.execute(filepath);

    }

    private class StableArrayAdapter extends ArrayAdapter<ScanResult> {

        HashMap<ScanResult, Integer> mIdMap = new HashMap<ScanResult, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<ScanResult> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            ScanResult item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }





    public class TesseractProcessing extends AsyncTask<String, Integer, String> {

        private List<ScanResult> result;

        private ProgressDialog dialog;

        public TesseractProcessing(TesseractActivity tess) {
            dialog = new ProgressDialog(tess);
        }

        protected List<ScanResult> getResults() {
            return result;
        }


        @Override
        protected void onPreExecute() {
            dialog.setTitle("Scanning Image");
            dialog.setMessage("Please wait...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            //Log.d("ASYNC", "The dialog showing is " + dialog.isShowing());
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            final List<ScanResult> results = getResults();
            Log.d("SIZE", ""+results.size());
            Mat img = Imgcodecs.imread(filepath);

            ListView listview = (ListView)findViewById(R.id.listview);
            final MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(TesseractActivity.this,
                     results.toArray(new ScanResult[results.size()]), img);
            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    final String item = (String) parent.getItemAtPosition(position);
                    view.animate().setDuration(2000).alpha(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    results.remove(item);
                                    adapter.notifyDataSetChanged();
                                    view.setAlpha(1);
                                }
                            });
                }

            });


        }

        @Override
        protected String doInBackground(String... params) {
            TessBaseAPI mTess = new TessBaseAPI();
            mTess.setDebug(true);
            String path = Environment.getExternalStorageDirectory().getPath();
            //String path = "/mnt/sdcard/";
            String filepath = params[0];
            mTess.setPageSegMode(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE);
            mTess.init(path, "eng");
            Log.d("Tesseract", path);
            //Configs

            File imgfile = new File(filepath);
            mTess.setVariable("textord_all_prop", "1");
            mTess.setVariable("load_freq_dawg", "0");
            mTess.setVariable("load_unambig_dawg", "0");
            mTess.setVariable("load_punc_dawg", "0");
            mTess.setVariable("load_system_dawg", "0");
            mTess.setVariable("load_number_dawg", "0");
           // mTess.setVariable("tessedit_char_whitelist", "1234567890/QWERTYUIOPASDFGHJKLZXCVBNM-,estricnyg ");

            mTess.setImage(imgfile);
            String print = mTess.getUTF8Text();
            final ResultIterator iterator = mTess.getResultIterator();
            String lastTest;
            float lastConfidence;
            int count = 0;
            iterator.begin();
            ArrayList<ScanResult> results = new ArrayList<ScanResult>();
            do {
                results.add(new ScanResult(iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE),
                        iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE),
                        iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE)));

            } while(iterator.next(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE));
            result = results;


         //   Pixa blank = mTess.getTextlines();

//            ArrayList<Rect> pixas = blank.getBoxRects();
//            for(Rect rect : pixas) {
//                Log.d("RECT ",rect.flattenToString());
//            }

            Log.d("WITH NOISE", print);
            //identifyState(print);
           // processNoise(print);
            mTess.end();
            dialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // fillForm();
                }
            });

            return "";
        }



    }
}
