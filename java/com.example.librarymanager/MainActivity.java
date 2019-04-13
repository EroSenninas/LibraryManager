package com.example.librarymanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.librarymanager.AdminPackage.SecureActivityAdmin;
import com.example.librarymanager.UserPackage.BooksPackage.BooksActivityUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressDialog mProgress;

    private EditText emailField;
    private EditText passwordField;

    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);


        mProgress = new ProgressDialog(this);

        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });

    }

    private void Login() {

        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgress.setMessage("Loging in...");
            mProgress.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    mProgress.dismiss();

                    if (task.isSuccessful()) {

                        DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                Map<String, Object> nestedData = (Map<String, Object>) documentSnapshot.getData().get("role");

                                if (nestedData.get("admin").equals(true)) {

                                    Intent adminIntent = new Intent(MainActivity.this, SecureActivityAdmin.class);
                                    adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(adminIntent);

                                } else {

                                    Intent userIntent = new Intent(MainActivity.this, BooksActivityUser.class);
                                    userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(userIntent);

                                }

                            }
                        });

                    } else Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {

            Toast.makeText(MainActivity.this, "Not all fields are filled!", Toast.LENGTH_SHORT).show();

            if (TextUtils.isEmpty(email)) { emailField.setError("Required"); } else emailField.setError(null);
            if (TextUtils.isEmpty(password)) { passwordField.setError("Required"); } else passwordField.setError(null);

        }
    }

    private void Register() {

        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgress.setMessage("Signing in...");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    mProgress.dismiss();

                    if (task.isSuccessful()) {

                        String userUID = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("email", mAuth.getCurrentUser().getEmail());

                        Map<String, Object> nestedData = new HashMap<>();
                        nestedData.put("admin", false);

                        user.put("role", nestedData);

                        db
                                .collection("users")
                                .document(userUID)
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "User successfully created!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error creating user!", e);
                                    }
                                });

                        Intent userIntent = new Intent(MainActivity.this, BooksActivityUser.class);
                        userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(userIntent);

                    } else  { Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show(); }

                }
            });
        } else {

            Toast.makeText(MainActivity.this, "Not all fields are filled!", Toast.LENGTH_SHORT).show();

            if (TextUtils.isEmpty(email)) { emailField.setError("Required"); } else emailField.setError(null);
            if (TextUtils.isEmpty(password)) { passwordField.setError("Required"); } else passwordField.setError(null);

        }
    }
}
