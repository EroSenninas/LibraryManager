package com.example.librarymanager.AdminPackage;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.librarymanager.AdminPackage.BooksPackage.BooksActivityAdmin;
import com.example.librarymanager.AdminPackage.ClientsPackage.UsersActivity;
import com.example.librarymanager.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SecureActivityAdmin extends AppCompatActivity {

    private static final String TAG = "SecureActivityAdmin";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.addBook) {

            AlertDialog.Builder builder = new AlertDialog.Builder(SecureActivityAdmin.this);
            LayoutInflater inflater = LayoutInflater.from(SecureActivityAdmin.this);

            View dialogView = inflater.inflate(R.layout.dialog_book, null);

            TextView textView = dialogView.findViewById(R.id.title);
            textView.setText("Book add");

            final EditText editText1 = dialogView.findViewById(R.id.bookName);
            final EditText editText2 = dialogView.findViewById(R.id.bookInfo);
            final EditText editText3 = dialogView.findViewById(R.id.bookQuantity);

            builder.setView(dialogView);

            final AlertDialog alertD = builder.create();
            alertD.show();

            Button confirmButton = dialogView.findViewById(R.id.confirmButton);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!TextUtils.isEmpty(editText1.getText()) && !TextUtils.isEmpty(editText2.getText()) && !TextUtils.isEmpty(editText3.getText())) {

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", editText1.getText().toString());
                        data.put("about", editText2.getText().toString());
                        data.put("quantity", Integer.valueOf(editText3.getText().toString()));

                        db
                                .collection("books")
                                .add(data)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                                        // show success toast
                                        Toast.makeText(SecureActivityAdmin.this, "You have successfully added a book!", Toast.LENGTH_SHORT).show();

                                        alertD.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);

                                        // show failure toast
                                        Toast.makeText(SecureActivityAdmin.this, "Error adding document" + e, Toast.LENGTH_SHORT).show();

                                    }
                                });
                    } else {

                        if (TextUtils.isEmpty(editText1.getText())) { editText1.setError("Required"); } else editText1.setError(null);
                        if (TextUtils.isEmpty(editText2.getText())) { editText2.setError("Required"); } else editText2.setError(null);
                        if (TextUtils.isEmpty(editText3.getText())) { editText3.setError("Required"); } else editText3.setError(null);

                    }
                }
            });

            Button cancelButton = dialogView.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alertD.dismiss();

                }
            });

        } else if (id == R.id.editBook) {

            Intent adminIntent = new Intent(SecureActivityAdmin.this, BooksActivityAdmin.class);
            adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(adminIntent);

        } else if (id == R.id.manageList) {

            Intent adminIntent = new Intent(SecureActivityAdmin.this, UsersActivity.class);
            adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(adminIntent);

        }

        return super.onOptionsItemSelected(item);
    }
}