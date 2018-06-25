package com.example.android.anagramfinder;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;


public class AnagramsActivity extends AppCompatActivity {
    private HashMap<String, ArrayList<String>> map;
    private ArrayList<String> anagramWords;
    private String key, word;
    private EditText typeView;
    private Button buttonFind;
    private AssetManager assetManager;

    @Override
    //execution starts here
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anagrams);

        try {
            assetManager = getAssets();
            typeView = (EditText) findViewById(R.id.et_enter);
            buttonFind = (Button) findViewById(R.id.bt_find);
            /* keys file consists of all the english words in sorted order
            the values file consists of all the sorted anagrams possible for a particular key.
            both keys and values are ordered sequentially such that the line number on which
            a particular key is present will be the same line no at which the value will be
            present in the values file.
             */
            InputStream inputStreamKey = assetManager.open("keys");
            InputStream inputStreamValue = assetManager.open("values");
            assetManager = null;
            /*invoke our asynchronous task
              the params here are passed to the doInBackground() method

             */
            new LongOperation().execute(inputStreamKey, inputStreamValue);
        } catch (IOException e) {
            Toast.makeText(this, "Could'nt load the files", Toast.LENGTH_SHORT).show();
        }

    }

    public void display(View V) {
        boolean foundAtLeastOneWord = false, flag = false;

        TextView displayView = (TextView) findViewById(R.id.txv_display);
        displayView.setText("");

        typeView = (EditText) findViewById(R.id.et_enter);
        String input = typeView.getText().toString().toLowerCase();

        String sortedInput = getSorted(input);

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
            key = entry.getKey();
            ArrayList<String> valueList = entry.getValue();
            for (String val : valueList) {
                if (sortedInput.equals(key) && !val.equalsIgnoreCase(input)) {
                    displayView.append(val + "\n");
                    foundAtLeastOneWord = true;
                }
            }
        }
        if (input.matches("")) {
            Toast.makeText(this, "You did not enter a username", Toast.LENGTH_SHORT).show();
            flag = true;
        } else if (foundAtLeastOneWord == false && !flag)
            displayView.setText("No Anagram exist for this word");
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public String getSorted(String str) {
        Collator collator = Collator.getInstance(new Locale("fr", "FR"));
        String original = str;
        String[] split = original.split("");
        Arrays.sort(split, collator);
        String sorted = "";
        for (int i = 0; i < split.length; i++) {
            sorted += split[i];
        }
        original = null;
        split = null;
        return sorted;
    }

    private class LongOperation extends AsyncTask<InputStream, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(InputStream... params) {
            map = new HashMap<>();
            try {
                /*
                create buffered character stream for reading from the files as characters
                params are inputstreams of those files passed as parameters
                 */
                BufferedReader keyDictionary = new BufferedReader(new InputStreamReader(params[0]));
                BufferedReader wordsDictionary = new BufferedReader(new InputStreamReader(params[1]));
                StringTokenizer t;
                // calls onProgressUpdate() method and restricts the user from inputting till map has been loaded
                publishProgress(1);
                // start reading from both the files simultaneously since the respective key,value pairs will be on the same line
                while (((key = keyDictionary.readLine()) != null) && (t = new StringTokenizer(wordsDictionary.readLine())) != null) {
                    anagramWords = new ArrayList<>();
                    word = "";
                    /*
                    each value read from the values file may contain more than one word. i.e:- a key may have more than
                    one anagram. so we read the values as tokens(basically splitting into different words) and store
                    to an array.
                     */
                    while (t.hasMoreTokens()) {
                        word = t.nextToken();
                        anagramWords.add(word);
                    }
                    /*
                    we store the key and values to a map where values are inside an ArrayList
                    eg:- iceman : {cinema,anemic}
                     */
                    map.put(key, anagramWords);
                }

            } catch (IOException e) {
                System.out.println("IOError");
            }
            // give user control when the map is filled and ready
            return "Type Here";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            typeView.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
            typeView.setHint("Loading...Please Wait");
            /*
            restrict user from typing inside the TextField
             */
            typeView.setFocusable(false);
            buttonFind.setClickable(false);
        }

        @Override
        /*
        after everything is loaded give user ability to type again
        also set a hint inside the TextField inorder make users know where to type
         */
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            typeView.setHint(result);
            typeView.setFocusableInTouchMode(true);
            buttonFind.setClickable(true);
        }
    }
}
