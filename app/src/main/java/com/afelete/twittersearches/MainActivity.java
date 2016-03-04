package com.afelete.twittersearches;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String SEARCHES= "searches";
    private EditText queryEditText;
    private EditText tagEditText;
    private FloatingActionButton saveFloatingActionButton;
    private SharedPreferences savedSearches;
    private List<String> tags;
    private SearchesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        queryEditText = ((TextInputLayout) findViewById(R.id.queryTextInputLayout)).getEditText();
        tagEditText = ((TextInputLayout) findViewById(R.id.tagTextInputLayout)).getEditText();
        tagEditText.addTextChangedListener(textWatcher);

        // get the sharedPreferences that contain the user saved searches
        savedSearches = getSharedPreferences(SEARCHES,MODE_PRIVATE);

        //store the saved tag in arrayList
        tags = new ArrayList<>(savedSearches.getAll().keySet());
        Collections.sort(tags,String.CASE_INSENSITIVE_ORDER);

        //get reference to the recyclerView and configure it
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //use the linearLayoutManager to display items in a vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //create recyclerview.adpter to bibd tags to the recyclerview
        adapter = new SearchesAdapter(tags, itemClickListener, itemLongClickListener);
        recyclerView.setAdapter(adapter);

        //specify a custom ItemDecorator to draw lines
        recyclerView.addItemDecoration(new ItemDivider(this));

       //register listner to save a new or edit search
        saveFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        saveFloatingActionButton.setOnClickListener(saveButtonListener);
        updateFAB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateFAB();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private  void updateFAB(){
        if (queryEditText.getText().toString().isEmpty()|| tagEditText.getText().toString().isEmpty())
            saveFloatingActionButton.hide();
        else
            saveFloatingActionButton.show();
    }

    private final OnClickListener saveButtonListener = new OnClickListener(){
        @Override
        public void onClick(View view){
            String query = queryEditText.getText().toString();
            String tag = tagEditText.getText().toString();
            if (!query.isEmpty() && !tag.isEmpty()){
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(),0);
                addTaggedSearch(tag, query);
                queryEditText.setText("");
                tagEditText.setText("");
                queryEditText.requestFocus();

            }
        }
    };
    private void addTaggedSearch(String tag, String query){
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply();

        if(!tags.contains(tag)){
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }

    }
    private final OnClickListener itemClickListener = new OnClickListener(){
        @Override
        public void onClick(View view){
             String tag = ((TextView) view).getText().toString();
            String urlString = getString(R.string.search_URL) + Uri.encode(savedSearches.getString(tag,""), "UTF-8");

            //create an intent to lunch a web browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            startActivity(webIntent);
        }
    };

    private final OnLongClickListener itemLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            final String tag = ((TextView) view).getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(getString(R.string.share_edit_delete_title, tag));
            builder.setItems(R.array.dialog_items,new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    switch(which){
                        case 0:
                            shareSearch(tag);
                            break;
                        case 1:
                            tagEditText.setText(tag);
                            queryEditText.setText(savedSearches.getString(tag,""));
                            break;
                        case 2:
                            deleteSearch(tag);
                            break;
                    }
                }
            } );
            builder.setNegativeButton(getString(R.string.cancel),null);
            builder.create().show();
            return true;
        }
    };
    private void shareSearch(String tag){
        String urlString = getString(R.string.search_URL)+ Uri.encode(savedSearches.getString(tag,""),"UTF-8");
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_message, urlString));
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent,getString(R.string.share_search)));
    }
    private void deleteSearch(final String tag){
        AlertDialog.Builder confirmBuilber = new AlertDialog.Builder(this);
        confirmBuilber.setMessage(getString(R.string.confirm_message, tag));
        confirmBuilber.setNegativeButton(getString(R.string.cancel), null);
        confirmBuilber.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                tags.remove(tag);
                SharedPreferences.Editor preferencesEditor = savedSearches.edit();
                preferencesEditor.remove(tag);
                preferencesEditor.apply();
                adapter.notifyDataSetChanged();
            }
        });
        confirmBuilber.create().show();

    }

}
