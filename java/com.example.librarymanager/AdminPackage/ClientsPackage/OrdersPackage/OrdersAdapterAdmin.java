package com.example.librarymanager.AdminPackage.ClientsPackage.OrdersPackage;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersAdapterAdmin extends BaseAdapter implements Filterable {

    private static final String TAG = "OrdersAdapterAdmin";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Context context;
    public ArrayList<Book> bookArrayList;
    public ArrayList<Book> orig;

    public String id;

    public OrdersAdapterAdmin(Context context, ArrayList<Book> bookArrayList, String id) {

        super();
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.id = id;

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

            convertView = LayoutInflater.from(context).inflate(R.layout.order_row_admin, parent, false);

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

        Button returnButton = (Button) convertView.findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("Book order");
                alertDialog.setMessage(
                        "Are you sure user returned this book? \n " +
                                holder.name.getText() + "\n" +
                                holder.info.getText());
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

                                // need to check for penalty!

                                db
                                        .collection("orders")
                                        .document(id)
                                        .collection("orders")
                                        .document(bookArrayList.get(position).getDocumentID())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Document successfully deleted!");

                                                    final DocumentReference quantityReference = db.collection("books").document(bookArrayList.get(position).getID());

                                                    db.runTransaction(new Transaction.Function<Void>() {

                                                        @Nullable
                                                        @Override
                                                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                            DocumentSnapshot snapshot = transaction.get(quantityReference);

                                                            long newQuantity = snapshot.getLong("quantity") + 1;
                                                            transaction.update(quantityReference, "quantity", newQuantity);

                                                            return null;
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "Transaction success!");

                                                            // show success toast
                                                            Toast toast = Toast.makeText(context, "You have successfully canceled a book request!", Toast.LENGTH_SHORT);
                                                            toast.show();

                                                            // update listView
                                                            updateList();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Transaction failure.", e);

                                                            // show failure toast
                                                            Toast toast = Toast.makeText(context, "Error canceling a book request!" + e, Toast.LENGTH_SHORT);
                                                            toast.show();

                                                            // update listView
                                                            updateList();

                                                        }
                                                    });
                                                }
                                                else
                                                    Log.d(TAG, "Error deleting document", task.getException());

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

                                    if (listOfBooks.get(i).get("taken").equals(true))
                                        bookArrayList.add(
                                                new Book(
                                                        listOfBooks.get(i).getData().get("bookID").toString(),
                                                        listOfBooks.get(i).getId(),
                                                        "",
                                                        "",
                                                        listOfBooks.get(i).getData().get("dateTaken").toString()));

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

                                    Toast toast = Toast.makeText(context, "User does not have any orders!", Toast.LENGTH_SHORT);
                                    toast.show();

                                    ((Activity)context).finish();

                                }
                            } else {

                                Toast toast = Toast.makeText(context, "User does not have any orders!", Toast.LENGTH_SHORT);
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