package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText searchEt;
    private ImageButton filterBtn;
    private RecyclerView sourcesRv;

    private ArrayList<ModelSourceList> sourceLists;
    private AdapterSourceList adapterSourceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init UI views
        progressBar = findViewById(R.id.progressBar);
        searchEt = findViewById(R.id.searchEt);
        filterBtn = findViewById(R.id.filterBtn);
        sourcesRv = findViewById(R.id.sourcesRv);
        System.out.println("about to call resources");
        loadSources();
        System.out.println("loaded resources");
        // search
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // called as and when user type/ remove letter
                try{
                    adapterSourceList.getFilter().filter(s);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // click to show filter dialog (bottom sheet)
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterBottomSheet();
            }
        });
    }

    // initially selected item ( by default show all )
    private String selectedCountry = "All", selectedCategory = "All", selectedLanguage = "All";
    private int selectedCountryPosition = 0, selectedCategoryPosition = 0, selectedLanguagePosition = 0 ;

    private void filterBottomSheet() {
        // inflate layout (filter_layout.xml) and its UI Views for bottom sheet
        View view = LayoutInflater.from(this).inflate(R.layout.filter_layout,null);
        Spinner countrySpinner = view.findViewById(R.id.countrySpinner);
        Spinner categorySpinner = view.findViewById(R.id.categorySpinner);
        Spinner languageSpinner = view.findViewById(R.id.languageSpinner);
        Button applyBtn = view.findViewById(R.id.applyBtn);

        // create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapterCountries = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,Constants.COUNTRIES);
        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,Constants.CATEGORIES);
        ArrayAdapter<String> adapterLanguages = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,Constants.LANGUAGES);

        // specify the layout to use when the list of choices appears
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_item);
        adapterLanguages.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // apply adapter to our spinners
        countrySpinner.setAdapter(adapterCountries);
        categorySpinner.setAdapter(adapterCategories);
        languageSpinner.setAdapter(adapterLanguages);

        // set last selected item in spinner
        countrySpinner.setSelection(selectedCountryPosition);
        categorySpinner.setSelection(selectedCategoryPosition);
        languageSpinner.setSelection(selectedLanguagePosition);

        // spinner item selected listeners
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = Constants.COUNTRIES[position];
                selectedCountryPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = Constants.CATEGORIES[position];
                selectedCategoryPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguage = Constants.LANGUAGES[position];
                selectedLanguagePosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // setup bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        // add layout/view to bottomSheet
        bottomSheetDialog.setContentView(view);
        // show bottomSheet
        bottomSheetDialog.show();

        // apply filter on click
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dismiss dialog
                bottomSheetDialog.dismiss();

                loadSources();
            }
        });

    }

    private void loadSources() {
        Log.d("FILTER_TAG","Country:" + selectedCountry);
        Log.d("FILTER_TAG","Category:" + selectedCategory);
        Log.d("FILTER_TAG","Language:" + selectedLanguage);

        // show selected filter option in actionbar
        getSupportActionBar().setSubtitle("Country: "+selectedCountry + " Category: "+ selectedCategory + " Language: "+ selectedLanguage);

        // initial value is "All" thats why it not filtering data, let replace it with ""

        if(selectedCountry.equals("All")){
            selectedCountry ="";
        }
        if(selectedCategory.equals("All")){
            selectedCategory ="";
        }
        if(selectedLanguage.equals("All")){
            selectedLanguage ="";
        }


        // init list
        sourceLists= new ArrayList<>();
        sourceLists.clear();

        progressBar.setVisibility(View.VISIBLE);

//      Request data
//      String url = "https://newsapi.org/v2/sources?apiKey=" + Constants.API_KEY;
        String url = "https://newsapi.org/v2/top-headlines/sources?apiKey=" + Constants.API_KEY +
                "&country=" + selectedCountry + "&category=" + selectedCategory + "&language=" + selectedLanguage;
        System.out.println("calling load resources");
//         ["User-Agent"]="Mozilla/5.0"
        Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("User-Agent", "Mozilla/5.0");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }

            @Override
            public void onResponse(String response) {
                // response is got as String
                System.out.println("receiveing data");
                try{
                    // convert string to JSON object
                    JSONObject jsonObject =new JSONObject(response);
                    // get sources array from that object
                    JSONArray jsonArray = jsonObject.getJSONArray("sources");
                    System.out.println("recieved maybe??");
                    // get all data from that array using loop
                    for(int i = 0 ; i< jsonArray.length();i++){
                        System.out.println("actually recieiveing");
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                        String id = jsonObject1.getString("id");
                        String name = jsonObject1.getString("name");
                        String description = jsonObject1.getString("description");
                        String url = jsonObject1.getString("url");
                        String country = jsonObject1.getString("country");
                        String category = jsonObject1.getString("category");
                        String language = jsonObject1.getString("language");

                        // set data to mode
                        ModelSourceList model = new ModelSourceList(
                                ""+id,
                                ""+name,
                                ""+description,
                                ""+url,
                                ""+category,
                                ""+language,
                                ""+country
                        );
                        System.out.println(model);
                        // add model to list
                        sourceLists.add(model);
                    }

                    progressBar.setVisibility(View.GONE);

                    // setup adapter
                    adapterSourceList= new AdapterSourceList(MainActivity.this,sourceLists);
                    //set adapter to recyclerview
                    sourcesRv.setAdapter(adapterSourceList);



                }catch (Exception e){
                    // exception while loading JSON data
                    System.out.println(e);
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }){ @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("User-Agent", "Mozilla/5.0");
            return headers;
        }};

        // add request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}