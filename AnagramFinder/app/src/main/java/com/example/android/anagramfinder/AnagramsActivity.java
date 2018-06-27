package com.example.android.anagramfinder;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
	private TextView displayView;

	@Override
	//execution starts here
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anagrams);

		try {
			// gets reference to the files in assets dir
			assetManager = getAssets();

			// basic object declaration
			typeView = (EditText) findViewById(R.id.et_enter);
			buttonFind = (Button) findViewById(R.id.bt_find);

			/*
			"keys" file consists of all the english words in sorted order
			the "values" file consists of all the sorted anagrams possible for a particular key.
			both keys and values are ordered sequentially such that the line number on which
			a particular key is present will be the same line no at which the value will be
			present in the values file.
			NOTE THAT CURRENTLY WE WILL ONLY BE ABLE TO FIND ANAGRAMS FOR WORDS OF MAX LENGTH OF 11
			WE CAN ADD SUPPORT FOR MORE IF NECESSARY
			 */
			InputStream inputStreamKey = assetManager.open("keys");
			InputStream inputStreamValue = assetManager.open("values");
			assetManager = null;

			/*
			  invoke our asynchronous task for populating the hash map.
			  the arguments from here are passed to the doInBackground() method inside it
			 */
			new LongOperation().execute(inputStreamKey, inputStreamValue);

			/*
			add a new listener which would check the user input change and fuzzy finds
			the corresponding anagrams of the user input if exists.
			 */
			typeView.addTextChangedListener(new CustomWatcher());
		} catch (IOException e) {
			Toast.makeText(this, R.string.no_IO, Toast.LENGTH_SHORT).show();
		}

	}

	/*
	Collator class is used to build searching and sorting routines for natural language text.
	we split our string into character array. then we sort the char array and return it
	 */
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
	our text listener for the the EditText typeview.
	here we fuzzy find the anagrams on the go, without
	relying on clicking a find button or something like that.
	 */
	public class CustomWatcher implements TextWatcher {
		String changed, newStr, edit, sortedInput;
		boolean foundAtLeastOneWord;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			/*
			txv_display is the id given for that particular text view in the XML
			 */
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
			// this the newly inputted word by the user
			newStr = s.toString();
			edit = s.subSequence(start, start + count).toString();// not used now. to be used in future development
		}

		/*
		currently we get the desired output, but the computation is really slow,
		since it's performed each time the user enters a character.
		TRYING TO FIND A BETTER SOLUTION
		 */
		@Override
		public void afterTextChanged(Editable s) {
			boolean foundAtLeastOneWord = false;
			/*
			the important part where we get the alphabetically sorted form of the user input,
			so as to compare it with the values inside the keys file
			newstr is the changed user input.
			 */
			sortedInput = getSorted(newStr);

			/*
			we store the key and values to a hash map named map where values are inside an ArrayList
			eg:- iceman : [cinema,anemic]
			traverse through each line in the map
			 */
			for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
				key = entry.getKey();
				// the value in the map is in ArrayList form
				ArrayList<String> valueList = entry.getValue();
				// iterate through the ArrayList
				for (String val : valueList) {
					/*
					check if the sorted user input exists as a key. ie:- whether the word has anagrams
					also make sure that the user input is not redisplayed in output
					 */
					if (sortedInput.equals(key) && !val.equalsIgnoreCase(newStr)) {
						typeView.setTextColor(Color.GREEN);
						displayView.append(val + "\n");
						foundAtLeastOneWord = true;
					}
				}
			}

			/*
			case where no input is provided or no anagram is present for the word
			we change text color to red as a visual cue
			*/
			if (!foundAtLeastOneWord)
				typeView.setTextColor(Color.RED);
		}
	}

	/*
	i thought trying to perform the computation for finding the anagrams as an AsyncTask(so as to make
	it faster) would work.
	but the thing is calling an async task(in afterTextChanged() method of our TextWatcher) each time
	an user changes text is not working out.
	TRYING TO FIND A BETTER SOLUTION
	*/
	// ignore the whole code under async task written below.
	private class fuzzyFind extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		// populate the hash map in background
		@Override
		protected String doInBackground(String... params) {
			/*
			the important part where we get the alphabetically sorted form of the user input,
			so as to compare it with the values inside the keys file
			 */
			boolean foundAtLeastOneWord = false;
			String sortedInput = getSorted(params[0]);

			// traverse through each line in the map
			for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
				key = entry.getKey();
				// the value in the map is in ArrayList form
				ArrayList<String> valueList = entry.getValue();
				// iterate through the ArrayList
				for (String val : valueList) {
					/*
					check if the sorted user input exists as a key. ie:- whether the word has anagrams
					also make sure that the user input is not redisplayed in output
					 */
					if (sortedInput.equals(key) && !val.equalsIgnoreCase(params[0])) {
						displayView.append(val + "\n");
						foundAtLeastOneWord = true;
					}
				}
			}

			/*
			case where no input is provided or no anagram is present for the word
			*/
			if (!foundAtLeastOneWord)
				Toast.makeText(AnagramsActivity.this, R.string.no_anagram, Toast.LENGTH_SHORT).show();
			// give user control when the map is filled and ready
			return "Type Here";
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}
	// ignore the async task written above.

	/*
	perform our asynchronous task for populating the map
	 */
	private class LongOperation extends AsyncTask<InputStream, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		// populate the hash map in background
		@Override
		protected String doInBackground(InputStream... params) {
			map = new HashMap<>();
			try {
					/*
					create buffered character stream for reading from the key and values files as characters
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

		/*
		publishProgress() would call this method which would display whats happening in background
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			typeView.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
			typeView.setHint("Loading...Please Wait");
				/*
				restrict user from typing inside the EditText field
				 */
			typeView.setFocusable(false);
			buttonFind.setClickable(false);
		}

		/*
		after everything is loaded give user ability to type again
		also set a hint inside the TextField inorder make users know where to type
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			typeView.setHint(result);
			typeView.setFocusableInTouchMode(true);
			buttonFind.setClickable(true);
		}
	}
}
