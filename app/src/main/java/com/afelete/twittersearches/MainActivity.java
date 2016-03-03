package com.afelete.twittersearches;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
}
