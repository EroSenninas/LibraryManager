package com.example.librarymanager.UserPackage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.librarymanager.R;
import com.example.librarymanager.UserPackage.BooksPackage.BooksActivityUser;
import com.example.librarymanager.UserPackage.OrdersPackage.OrdersActivityUser;
import com.example.librarymanager.UserPackage.RequestsPackage.RequestsActivityUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SecureActivityUser extends AppCompatActivity {

    private static final String TAG = "SecureActivityUser";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.booksList) {

            Intent userIntent = new Intent(SecureActivityUser.this, BooksActivityUser.class);
            userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(userIntent);

        } else if (id == R.id.ordersList) {

            Intent userIntent = new Intent(SecureActivityUser.this, OrdersActivityUser.class);
            userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(userIntent);

        } else if (id == R.id.requestsList) {

            Intent userIntent = new Intent(SecureActivityUser.this, RequestsActivityUser.class);
            userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(userIntent);

        } else if (id == R.id.penalty) {

            Toast.makeText(SecureActivityUser.this, "Under development!", Toast.LENGTH_SHORT).show();

            //Intent userIntent = new Intent(BooksActivityUser.this, PenaltyActivityUser.class);
            //userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //startActivity(userIntent);

        } else if (id == R.id.wishList) {

            Toast.makeText(SecureActivityUser.this, "Under development!", Toast.LENGTH_SHORT).show();

            //Intent userIntent = new Intent(BooksActivityUser.this, WishListActivityUser.class);
            //userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //startActivity(userIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}