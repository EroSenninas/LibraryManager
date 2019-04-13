package com.example.librarymanager.UserPackage.RequestsPackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

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

public class RequestsActivityUser extends AppCompatActivity {

    private static final String TAG = "RequestsActivityUser";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ListView listView;
    private ArrayList<Book> booksList;
    private RequestsAdapterUser requestAdapter;

    List<DocumentSnapshot> listOfBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // configure Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // configure Android Studio
        ActionBar actionBar = getSupportActionBar();
        listView = (ListView) findViewById(R.id.listView);
        booksList = new ArrayList<Book>();

        // show books list view
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

                    requestAdapter.getFilter().filter("");
                    listView.clearTextFilter();

                } else requestAdapter.getFilter().filter(newText);

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
                .collection("orders")
                .document(mAuth.getCurrentUser().getUid())
                .collection("orders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            if (!task.getResult().isEmpty()) {

                                listOfBooks = task.getResult().getDocuments();

                                for (int i = 0; i < listOfBooks.size(); i++) {

                                    if (listOfBooks.get(i).get("taken").equals(false))
                                        booksList.add(
                                                new Book(
                                                        listOfBooks.get(i).getData().get("bookID").toString(),
                                                        "",
                                                        "",
                                                        Boolean.valueOf(listOfBooks.get(i).getData().get("status").toString())));

                                }

                                if (booksList.size() != 0) {

                                    db
                                            .collection("books")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                    if (task.isSuccessful()) {

                                                        listOfBooks = task.getResult().getDocuments();

                                                        int x = 0;

                                                        for (int i = 0; i < listOfBooks.size(); i++) {

                                                            if (listOfBooks.get(i).getId().equals(booksList.get(x).getID())) {

                                                                booksList.get(x).setName(listOfBooks.get(i).get("name").toString());
                                                                booksList.get(x).setInfo(listOfBooks.get(i).get("about").toString());

                                                                if (++x > booksList.size() - 1) {
                                                                    break;
                                                                } else {
                                                                    i = -1;
                                                                }

                                                            }

                                                        }

                                                        requestAdapter = new RequestsAdapterUser(RequestsActivityUser.this, booksList);

                                                        listView.setAdapter(requestAdapter);
                                                        listView.setTextFilterEnabled(true);

                                                    } else
                                                        Log.d(TAG, "Error getting document: ", task.getException());

                                                }
                                            });
                                } else {

                                    Toast toast = Toast.makeText(getApplicationContext(), "User does not have any requests!", Toast.LENGTH_SHORT);
                                    toast.show();

                                    finish();
                                }
                            } else {

                            Toast toast = Toast.makeText(getApplicationContext(), "You do not have any requests!", Toast.LENGTH_SHORT);
                            toast.show();

                            finish();

                        }

                        } else {

                            Log.d(TAG, "Error getting documents", task.getException());
                        }

                    }
                });

    }
}
