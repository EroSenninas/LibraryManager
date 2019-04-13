package com.example.librarymanager.AdminPackage.ClientsPackage.OrdersPackage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.example.librarymanager.UserPackage.BooksPackage.BooksAdapterUser;
import com.example.librarymanager.UserPackage.OrdersPackage.OrdersActivityUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivityAdmin extends AppCompatActivity {

    private static final String TAG = "OrdersActivityAdmin";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ListView listView;
    private ArrayList<Book> booksList;
    private OrdersAdapterAdmin bookAdapter;

    String userUID;

    List<DocumentSnapshot> listOfBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        listView = (ListView) findViewById(R.id.listView);
        booksList = new ArrayList<Book>();

        Intent intent = getIntent();
        userUID = intent.getStringExtra("id");

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
                .collection("orders")
                .document(userUID)
                .collection("orders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            if (!task.getResult().isEmpty()) {

                                listOfBooks = task.getResult().getDocuments();

                                for (int i = 0; i < listOfBooks.size(); i++) {

                                    if (listOfBooks.get(i).get("taken").equals(true))
                                        booksList.add(
                                                    new Book(
                                                            listOfBooks.get(i).getData().get("bookID").toString(),
                                                            listOfBooks.get(i).getId(),
                                                            "",
                                                            "",
                                                            listOfBooks.get(i).getData().get("dateTaken").toString()));

                                }

                                if (booksList.size() != 0) {

                                    FirebaseFirestore.getInstance()
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

                                                        bookAdapter = new OrdersAdapterAdmin(OrdersActivityAdmin.this, booksList, userUID);

                                                        listView.setAdapter(bookAdapter);
                                                        listView.setTextFilterEnabled(true);

                                                    } else
                                                        Log.d(TAG, "Error getting document", task.getException());

                                                }
                                            });

                                } else {

                                    Toast toast = Toast.makeText(getApplicationContext(), "User does not have any orders!", Toast.LENGTH_SHORT);
                                    toast.show();

                                    finish();

                                }
                            } else {

                                Toast toast = Toast.makeText(getApplicationContext(), "User does not have any orders!", Toast.LENGTH_SHORT);
                                toast.show();

                                finish();

                            }

                        } else Log.d(TAG, "Error getting documents", task.getException());

                    }
                });

    }
}
