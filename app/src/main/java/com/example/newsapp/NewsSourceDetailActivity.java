package com.example.newsapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewsSourceDetailActivity extends AppCompatActivity {

    // UI views
    private TextView nameTv, descriptionTv, countryTv, categoryTv, languageTv;
    private RecyclerView newsRv;

    private ArrayList<ModelNewsSourceDetail> sourceDetailArrayList;
    private AdapterNewsSourceDetail adapterNewsSourceDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_source_detail);

        // actionbar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Latest News");
        // add back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init UI views
        nameTv = findViewById(R.id.nameTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        countryTv = findViewById(R.id.countryTv);
        categoryTv = findViewById(R.id.categoryTv);
        languageTv = findViewById(R.id.languageTv);
        newsRv = findViewById(R.id.newsRv);

        // get data from intent (that we passed from adapter)
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String country = intent.getStringExtra("country");
        String category = intent.getStringExtra("category");
        String language = intent.getStringExtra("language");

        actionBar.setTitle(name); // set title/name of news source we selected

        nameTv.setText(name);
        descriptionTv.setText(description);
        countryTv.setText("Country: " + country);
        categoryTv.setText("Category: " +category);
        languageTv.setText("Language: "+language);

        loadNewsData(id);
    }

    private void loadNewsData(String id) {
        // init list
        sourceDetailArrayList = new ArrayList<>();

        // url
        String url = "https://newsapi.org/v2/top-headlines?sources="+ id +"&apiKey="+ Constants.API_KEY;
//=======================================================================================
        Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("User-Agent", "Mozilla/5.0");
// ======================================================================================

        // progress bar
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading News");
        progressDialog.show();

        // request data
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

//******************************************************************************************************
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
//  **********************************************************************

            @Override
            public void onResponse(String response) {
                // we got the response
                // response is in jason object
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    // we need to get array out of that object
                    JSONArray jsonArray = jsonObject.getJSONArray("articles");

                    // get all data from that array using loop
                    for (int i=0; i< jsonArray.length();i++){

                        // each array element is a json object, get at specific position
                        JSONObject jsonObjectNew = jsonArray.getJSONObject(i);
                        // get actual data from that json object, make sure to use same names as in response
                        String title = jsonObjectNew.getString("title");
                        String description = jsonObjectNew.getString("description");
                        String url = jsonObjectNew.getString("url");
                        String urlToImage = jsonObjectNew.getString("urlToImage");
                        String publishedAt = jsonObjectNew.getString("publishedAt");
                        String content = jsonObjectNew.getString("content");

                        // need to convert date format e.g. from 2020-08-15 T02:49:20Z to 15/08/2020 02:49
                        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String formattedDate ="";

                        try{
                            // try to format date time
                            Date date = dateFormat1.parse(publishedAt);
                            formattedDate = dateFormat2.format(date);
                        }catch (Exception e){
                            // if failed formatting, set deafaults
                            formattedDate= publishedAt;
                        }


                        // add data new new instance of model
                        ModelNewsSourceDetail model = new ModelNewsSourceDetail(
                                ""+ title,
                                ""+description,
                                ""+url,
                                ""+urlToImage,
                                ""+formattedDate,
                                ""+content
                        );

                        // add that model to our list
                        sourceDetailArrayList.add(model);

                    }

                    // dismiss dialog
                    progressDialog.dismiss();

                    // setup adapter , add list to that adapter
                    adapterNewsSourceDetail = new AdapterNewsSourceDetail(NewsSourceDetailActivity.this,sourceDetailArrayList);
                    //set adapter
                    newsRv.setAdapter(adapterNewsSourceDetail);

                } catch (Exception e) {
                    // exception while formating data
                    progressDialog.dismiss();
                    Toast.makeText(NewsSourceDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // failed getting response, dismiss progress, show error message
                progressDialog.dismiss();
                Toast.makeText(NewsSourceDetailActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();

            }

//--------------------------------------------------------------------------------------------------
        }){ @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("User-Agent", "Mozilla/5.0");
            return headers;
        }};
//  ------------------------------------------------------------------------------------------------



        // add request to volley queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go previous activity
        return super.onSupportNavigateUp();
    }
}