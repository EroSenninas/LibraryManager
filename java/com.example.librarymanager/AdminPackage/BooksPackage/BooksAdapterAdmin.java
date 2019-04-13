package com.example.librarymanager.AdminPackage.BooksPackage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksAdapterAdmin extends BaseAdapter implements Filterable {

    private static final String TAG = "BooksAdapterAdmin";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Context context;
    public ArrayList<Book> bookArrayList;
    public ArrayList<Book> orig;

    public BooksAdapterAdmin(Context context, ArrayList<Book> bookArrayList) {

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

        if (convertView == null) {
            holder = new BookHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.book_row_admin, parent, false);

            holder.name = (TextView) convertView.findViewById(R.id.bookName);
            holder.info = (TextView) convertView.findViewById(R.id.bookInfo);

            convertView.setTag(holder);
        } else {
            holder = (BookHolder) convertView.getTag();
        }

        holder.name.setText(bookArrayList.get(position).getName());
        holder.info.setText(bookArrayList.get(position).getInfo());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

            }
        });

        Button orderButton = (Button) convertView.findViewById(R.id.editButton);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);

                View dialogView = inflater.inflate(R.layout.dialog_book, null);

                TextView textView = dialogView.findViewById(R.id.title);
                textView.setText("Book edit");

                final EditText editText1 = dialogView.findViewById(R.id.bookName);
                editText1.setText(bookArrayList.get(position).getName());

                final EditText editText2 = dialogView.findViewById(R.id.bookInfo);
                editText2.setText(bookArrayList.get(position).getInfo());

                final EditText editText3 = dialogView.findViewById(R.id.bookQuantity);
                editText3.setText(String.valueOf(bookArrayList.get(position).getQuantity()));

                builder.setView(dialogView);

                final AlertDialog alertD = builder.create();
                alertD.show();

                Button confirmButton = dialogView.findViewById(R.id.confirmButton);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!TextUtils.isEmpty(editText1.getText()) && !TextUtils.isEmpty(editText2.getText()) && !TextUtils.isEmpty(editText3.getText())) {

                            // update values in database
                            DocumentReference docRef = db.collection("books").document(bookArrayList.get(position).getID());

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("name", editText1.getText().toString());
                            updates.put("info", editText2.getText().toString());
                            updates.put("quantity", Integer.valueOf(editText3.getText().toString()));

                            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {

                                        Log.d(TAG, "Document successfully edited!");

                                        // show success toast
                                        Toast.makeText(context, "You have successfully edited a book!", Toast.LENGTH_SHORT).show();

                                        // dismiss dialog
                                        alertD.dismiss();

                                        //update list
                                        updateList();

                                    } else {

                                        Log.w(TAG, "Error editing book", task.getException());

                                        // show failure toast
                                        Toast.makeText(context, "Error editing book" + task.getException(), Toast.LENGTH_SHORT).show();

                                    }

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


            }
        });

        Button removeButton = (Button) convertView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("Book remove");
                alertDialog.setMessage(
                        "Are you sure you want to remove this book? \n " +
                                holder.name.getText().toString() + "\n" +
                                holder.info.getText().toString());
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

                                db
                                        .collection("books")
                                        .document(bookArrayList.get(position).getID())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Document successfully deleted!");

                                                    // show success toast
                                                    Toast.makeText(context, "You have successfully deleted a book!", Toast.LENGTH_SHORT).show();

                                                    // update listView
                                                    updateList();
                                                } else {

                                                    Log.d(TAG, "Error deleting document", task.getException());

                                                    // failure toast
                                                    Toast.makeText(context, "Error deleting document" + task.getException(), Toast.LENGTH_SHORT).show();

                                                }

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