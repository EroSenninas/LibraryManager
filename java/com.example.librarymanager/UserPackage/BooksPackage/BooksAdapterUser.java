package com.example.librarymanager.UserPackage.BooksPackage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.example.librarymanager.R;
import com.example.librarymanager.Book;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksAdapterUser extends BaseAdapter implements Filterable {

    private static final String TAG = "BooksAdapterUser";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Context context;
    public ArrayList<Book> bookArrayList;
    public ArrayList<Book> orig;

    public BooksAdapterUser(Context context, ArrayList<Book> bookArrayList) {

        super();
        this.context = context;
        this.bookArrayList = bookArrayList;

    }

    public class BookHolder
    {
        TextView name;
        TextView info;
        TextView quantity;
    }

    @Override
    public int getCount() {
        return bookArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();

        final BookHolder holder;

        if(convertView == null)
        {
            holder = new BookHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.book_row_user, parent, false);

            holder.name = (TextView) convertView.findViewById(R.id.bookName);
            holder.info = (TextView) convertView.findViewById(R.id.bookInfo);
            holder.quantity = (TextView) convertView.findViewById(R.id.bookQuantity);

            convertView.setTag(holder);
        }
        else
        {
            holder = (BookHolder) convertView.getTag();
        }

        holder.name.setText(bookArrayList.get(position).getName());
        holder.info.setText(bookArrayList.get(position).getInfo());
        holder.quantity.setText(bookArrayList.get(position).getQuantity() + " left");

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

            }
        });

        Button orderButton = (Button) convertView.findViewById(R.id.orderButton);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("Book request");
                alertDialog.setMessage(
                        "Are you sure you want to request book '" + holder.name.getText().toString() + "'?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                final String userUID = mAuth.getCurrentUser().getUid();
                                final String bookID = bookArrayList.get(position).getID();

                                db
                                        .collection("orders")
                                        .document(mAuth.getCurrentUser().getUid())
                                        .collection("orders")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                if (task.isSuccessful()) {

                                                    boolean exists = false;

                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        if (document.getData().get("bookID").equals(bookID)) {
                                                            Toast.makeText(context, "You already requested this book!", Toast.LENGTH_SHORT).show();
                                                            exists = true;
                                                            return;
                                                        }
                                                    }

                                                    if (!exists) {

                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("bookID", bookID);
                                                        map.put("dateTaken", null);
                                                        map.put("extended", 0);
                                                        map.put("penalty", 0);
                                                        map.put("taken", false);
                                                        map.put("status", false);

                                                        db.collection("orders")
                                                                .document(userUID)
                                                                .collection("orders")
                                                                .document()
                                                                .set(map)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "Book request successfully created!");

                                                                        DocumentReference quantityReference = db.collection("books").document(bookID);

                                                                        quantityReference
                                                                                .update("quantity", bookArrayList.get(position).getQuantity() - 1)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d(TAG, "Book quantity successfully changed!");

                                                                                        Toast.makeText(context, "You have successfully requested a book\n'" +
                                                                                                holder.name.getText().toString() + "'", Toast.LENGTH_SHORT).show();

                                                                                        updateList();

                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d(TAG, "Error changing book's quantity!");
                                                                                    }
                                                                                });

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG, "Error creating book request!: ", e);
                                                                    }
                                                                });

                                                    }

                                                } else
                                                    Log.d(TAG, "Error getting document: ", task.getException());

                                            }
                                        });


                            }
                        });

                alertDialog.show();

            }
        });

        return convertView;
    }

    public void updateList() {

        bookArrayList.clear();

        FirebaseFirestore.getInstance()
                .collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            List<DocumentSnapshot> listOfBooks = task.getResult().getDocuments();

                            for (int i = 0; i < listOfBooks.size(); i++) {

                                bookArrayList.add(
                                        new Book(
                                                listOfBooks.get(i).getId(),
                                                listOfBooks.get(i).get("name").toString(),
                                                listOfBooks.get(i).get("about").toString(),
                                                Integer.parseInt(listOfBooks.get(i).get("quantity").toString())));

                            }
                        }

                        notifyDataSetChanged();
                    }
                });
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults oReturn = new FilterResults();
                final ArrayList<Book> results = new ArrayList<Book>();

                if (orig == null) orig = bookArrayList;
                if (constraint != null) {

                    if (orig != null && orig.size() > 0) {

                        for (final Book B : orig) {

                            if (B.getName().toLowerCase().contains(constraint.toString()))
                                results.add(B);

                        }

                    }

                    oReturn.values = results;

                }

                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                bookArrayList = (ArrayList<Book>) results.values;
                notifyDataSetChanged();

            }
        };
    }
}