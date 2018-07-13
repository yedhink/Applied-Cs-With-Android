package com.example.android.anagramfinder;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
	private AssetManager assetManager;
	private TextView displayView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anagrams);

		try {
			assetManager = getAssets();
			typeView = (EditText) findViewById(R.id.et_enter);

			InputStream inputStreamKey = assetManager.open("keys");
			InputStream inputStreamValue = assetManager.open("values");
			assetManager = null;

			/*
			  invoke our asynchronous task for populating the hash map.
			 */
			new LongOperation().execute(inputStreamKey, inputStreamValue);

			/*
               fuzzy finder
			 */
			typeView.addTextChangedListener(new CustomWatcher());
		} catch (IOException e) {
			Toast.makeText(this, R.string.no_IO, Toast.LENGTH_SHORT).show();
		}
	}

	public String getSorted(String str) {
		Collator collator = Collator.getInstance(new Locale("fr", "FR"));
		String[] split = str.split("");
		Arrays.sort(split, collator);
		str = "";
		for (int i = 0; i < split.length; i++) {
			str += split[i];
		}
		return str;
	}

	/*
       text change listener
	 */
	public class CustomWatcher implements TextWatcher {
		String changed, newStr, edit, sortedInput;
		boolean foundAtLeastOneWord;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			displayView = (TextView) findViewById(R.id.txv_display);
			changed = s.subSequence(start, start + count).toString();// not used now. to be used in future development
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			/*
			on each update clear the previous results and set the flag whether an anagram is found to be false
			 */
			displayView.setText(null);
			foundAtLeastOneWord = false;
			newStr = s.toString();
		}

		/*
		perform fuzzy searching
		 */
		@Override
		public void afterTextChanged(Editable s) {
			boolean foundAtLeastOneWord = false;
			sortedInput = getSorted(newStr);

			/*
			eg:- iceman : [cinema,anemic]
			 */
			ArrayList<String> valueList = map.get(sortedInput);
			if(valueList!=null) {
				for (String val : valueList) {
					if (!val.equalsIgnoreCase(newStr)) {
						typeView.setTextColor(Color.GREEN);
						displayView.append(val + "\n");
						foundAtLeastOneWord = true;
					}
				}
			}

			/*
			case where no input is provided or no anagram is present for the word
			*/
			if (!foundAtLeastOneWord)
				typeView.setTextColor(Color.RED);
		}
	}

	/*
	populate map with anagrams
	 */
	private class LongOperation extends AsyncTask<InputStream, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(InputStream... params) {
			map = new HashMap<>();
			try {
				BufferedReader keyDictionary = new BufferedReader(new InputStreamReader(params[0]));
				BufferedReader wordsDictionary = new BufferedReader(new InputStreamReader(params[1]));
				StringTokenizer t;

				// restrict user access
				publishProgress(1);

				// start reading from both the files simultaneously since the respective key,value pairs will be on the same line
				while (((key = keyDictionary.readLine()) != null) && (t = new StringTokenizer(wordsDictionary.readLine())) != null) {
					anagramWords = new ArrayList<>();
					word = "";
					while (t.hasMoreTokens()) {
						word = t.nextToken();
						anagramWords.add(word);
					}
					map.put(key, anagramWords);
				}
			} catch (IOException e) {
				System.out.println("IOError");
			}
			return "Type Here";
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			typeView.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
			typeView.setHint("Loading...Please Wait");
			typeView.setFocusable(false);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			typeView.setHint(result);
			typeView.setFocusableInTouchMode(true);
		}
	}
}
