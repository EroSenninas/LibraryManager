package com.example.librarymanager.AdminPackage.BooksPackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.librarymanager.R;
import com.example.librarymanager.Book;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BooksActivityAdmin extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ListView listView;
    private ArrayList<Book> booksList;
    private BooksAdapterAdmin bookAdapter;

    List<DocumentSnapshot> listOfBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        listView = (ListView) findViewById(R.id.listView);
        booksList = new ArrayList<Book>();

        refreshListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);

        MenuItem searchAction = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchAction.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (TextUtils.isEmpty(newText)) {

                    bookAdapter.getFilter().filter("");
                    listView.clearTextFilter();

                } else bookAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh)
            refreshListView();

        return super.onOptionsItemSelected(item);
    }

    private void refreshListView() {

        listView.setAdapter(null);
        booksList.clear();

        FirebaseFirestore.getInstance()
                .collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            listOfBooks = task.getResult().getDocuments();

                            for (int i = 0; i < listOfBooks.size(); i++) {

                                booksList.add(
                                        new Book(
                                                listOfBooks.get(i).getId(),
                                                listOfBooks.get(i).get("name").toString(),
                                                listOfBooks.get(i).get("about").toString(),
                                                Integer.parseInt(listOfBooks.get(i).get("quantity").toString())));

                            }

                            bookAdapter = new BooksAdapterAdmin(BooksActivityAdmin.this, booksList);

                            listView.setAdapter(bookAdapter);
                            listView.setTextFilterEnabled(true);

                        } else Log.d(TAG, "Error getting documents", task.getException());

                    }
                });

    }
}
