package com.example.librarymanager.AdminPackage.ClientsPackage.RequestsPackage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestsAdapterAdmin extends BaseAdapter implements Filterable {

    private static final String TAG = "BooksAdapterAdmin";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Context context;
    public ArrayList<Book> bookArrayList;
    public ArrayList<Book> orig;

    private String id;

    public RequestsAdapterAdmin(Context context, ArrayList<Book> bookArrayList, String id) {

        super();
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.id = id;

    }

    public class BookHolder
    {
        TextView name;
        TextView info;
        TextView status;
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

        if (convertView == null) {
            holder = new BookHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.request_row_admin, parent, false);

            holder.name = convertView.findViewById(R.id.bookName);
            holder.info = convertView.findViewById(R.id.bookInfo);
            holder.status = convertView.findViewById(R.id.bookStatus);

            convertView.setTag(holder);
        } else {
            holder = (BookHolder) convertView.getTag();
        }

        holder.name.setText(bookArrayList.get(position).getName());
        holder.info.setText(bookArrayList.get(position).getInfo());

        if (bookArrayList.get(position).getDate().equals("true"))
            holder.status.setText("ready");
        else
            holder.status.setText("unready");

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

            }
        });

        Button statusButton = (Button) convertView.findViewById(R.id.statusButton);
        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("Book status");
                alertDialog.setMessage(holder.name.getText());
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "TAKEN",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                db
                                        .collection("orders")
                                        .document(id)
                                        .collection("orders")
                                        .document(bookArrayList.get(position).getDocumentID())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                Map<String, Object> updates = new HashMap<>();

                                                updates.put("dateTaken", Timestamp.now());
                                                updates.put("taken", true);

                                                db
                                                        .collection("orders")
                                                        .document(id)
                                                        .collection("orders")
                                                        .document(task.getResult().getId())
                                                        .update(updates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    Log.d(TAG, "Document successfully updated!");

                                                                    // show success toast
                                                                    Toast.makeText(context, "You have successfully update a book status!", Toast.LENGTH_SHORT).show();

                                                                    // update listView
                                                                    updateList();

                                                                }
                                                                else {

                                                                    Log.d(TAG, "Error updating document", task.getException());

                                                                    // show failure toast
                                                                    Toast.makeText(context, "Error updating document" + task.getException(), Toast.LENGTH_SHORT).show();

                                                                }

                                                            }
                                                        });

                                            }
                                        });

                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CHANGE STATUS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                db
                                        .collection("orders")
                                        .document(id)
                                        .collection("orders")
                                        .document(bookArrayList.get(position).getDocumentID())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                Map<String, Object> updates = new HashMap<>();

                                                if (task.getResult().get("status").equals(true))
                                                    updates.put("status", false);
                                                else
                                                    updates.put("status", true);

                                                db
                                                        .collection("orders")
                                                        .document(id)
                                                        .collection("orders")
                                                        .document(task.getResult().getId())
                                                        .update(updates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    Log.d(TAG, "Document successfully updated!");

                                                                    // show success toast
                                                                    Toast.makeText(context, "You have successfully update a book status!", Toast.LENGTH_SHORT).show();

                                                                    // update listView
                                                                    updateList();

                                                                }
                                                                else {

                                                                    Log.d(TAG, "Error updating document", task.getException());

                                                                    // show failure toast
                                                                    Toast.makeText(context, "Error updating document" + task.getException(), Toast.LENGTH_SHORT).show();

                                                                }

                                                            }
                                                        });

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
                .collection("orders")
                .document(id)
                .collection("orders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            if (!task.getResult().isEmpty()) {

                                List<DocumentSnapshot> listOfBooks = task.getResult().getDocuments();

                                for (int i = 0; i < listOfBooks.size(); i++) {

                                    if (listOfBooks.get(i).get("taken").equals(false))
                                        bookArrayList.add(
                                                new Book(
                                                        listOfBooks.get(i).get("bookID").toString(),
                                                        listOfBooks.get(i).getId(),
                                                        "",
                                                        "",
                                                        listOfBooks.get(i).get("status").toString()));

                                }

                                if (bookArrayList.size() != 0) {

                                    FirebaseFirestore.getInstance()
                                            .collection("books")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                    if (task.isSuccessful()) {

                                                        List<DocumentSnapshot> listOfBooks = task.getResult().getDocuments();

                                                        int x = 0;

                                                        for (int i = 0; i < listOfBooks.size(); i++) {

                                                            if (listOfBooks.get(i).getId().equals(bookArrayList.get(x).getID())) {

                                                                bookArrayList.get(x).setName(listOfBooks.get(i).get("name").toString());
                                                                bookArrayList.get(x).setInfo(listOfBooks.get(i).get("about").toString());

                                                                if (++x > bookArrayList.size() - 1) {
                                                                    break;
                                                                } else {
                                                                    i = -1;
                                                                }

                                                            }

                                                        }

                                                        notifyDataSetChanged();

                                                    } else
                                                        Log.d(TAG, "Error getting document", task.getException());

                                                }
                                            });

                                } else {

                                    Toast toast = Toast.makeText(context, "User does not have any requests!", Toast.LENGTH_SHORT);
                                    toast.show();

                                    ((Activity)context).finish();

                                }
                            } else {

                                Toast toast = Toast.makeText(context, "User does not have any requests!", Toast.LENGTH_SHORT);
                                toast.show();

                                ((Activity)context).finish();

                            }

                        } else Log.d(TAG, "Error getting documents", task.getException());

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