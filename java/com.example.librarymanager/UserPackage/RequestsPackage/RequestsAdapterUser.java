package com.example.librarymanager.UserPackage.RequestsPackage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.example.librarymanager.UserPackage.BooksPackage.BooksActivityUser;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class RequestsAdapterUser extends BaseAdapter implements Filterable {

    private static final String TAG = "RequestsAdapterUser";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Context context;
    private ArrayList<Book> bookArrayList;
    private ArrayList<Book> orig;

    private List<DocumentSnapshot> listOfBooks;

    public RequestsAdapterUser(Context context, ArrayList<Book> bookArrayList) {

        super();
        this.context = context;
        this.bookArrayList = bookArrayList;

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

        if(convertView == null)
        {
            holder = new BookHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.request_row_user, parent, false);

            holder.name = convertView.findViewById(R.id.bookName);
            holder.info = convertView.findViewById(R.id.bookInfo);
            holder.status = convertView.findViewById(R.id.bookStatus);

            convertView.setTag(holder);
        }
        else
        {
            holder = (BookHolder) convertView.getTag();
        }

        holder.name.setText(bookArrayList.get(position).getName());
        holder.info.setText(bookArrayList.get(position).getInfo());

        if (bookArrayList.get(position).getStatus())
            holder.status.setText("ready");
        else
            holder.status.setText("unready");

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

            }
        });

        Button cancelButton = (Button) convertView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("Book order");
                alertDialog.setMessage(
                        "Are you sure you want to cancel this request? \n " +
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

                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    if (document.get("bookID").equals(bookID)) {

                                                        db
                                                                .collection("orders")
                                                                .document(mAuth.getCurrentUser().getUid())
                                                                .collection("orders")
                                                                .document(document.getId())
                                                                .delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {
                                                                            Log.d(TAG, "Document successfully deleted!");

                                                                            final DocumentReference quantityReference = db.collection("books").document(bookID);

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

                                                                                    updateList();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.w(TAG, "Transaction failure.", e);

                                                                                    // show failure toast
                                                                                    Toast toast = Toast.makeText(context, "Error canceling a book request!" + e, Toast.LENGTH_SHORT);
                                                                                    toast.show();
                                                                                }
                                                                            });
                                                                        }
                                                                        else
                                                                            Log.d(TAG, "Error deleting document", task.getException());

                                                                    }
                                                                });
                                                    }
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

        db
                .collection("requests")
                .document(mAuth.getCurrentUser().getUid())
                .collection("requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                                if (!task.getResult().isEmpty()) {

                                listOfBooks = task.getResult().getDocuments();

                                for (int i = 0; i < listOfBooks.size(); i++) {
                                    bookArrayList.add(new Book(
                                            listOfBooks.get(i).getData().get("bookID").toString(), "", ""));

                                }

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

                                                        if (listOfBooks.get(i).getId().equals(bookArrayList.get(x).getID()))
                                                        {

                                                            bookArrayList.get(x).setName(listOfBooks.get(i).get("name").toString());
                                                            bookArrayList.get(x).setInfo(listOfBooks.get(i).get("about").toString());

                                                            if (++x > bookArrayList.size() - 1) {
                                                                break;
                                                            }
                                                            else {
                                                                i = -1;
                                                            }

                                                        }

                                                    }

                                                    notifyDataSetChanged();

                                                } else Log.d(TAG, "Error getting document: ", task.getException());

                                            }
                                        });
                                } else {

                                    Toast toast = Toast.makeText(context, "You do not have any requests!", Toast.LENGTH_SHORT);
                                    toast.show();

                                    Intent userIntent = new Intent(context, BooksActivityUser.class);
                                    userIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    context.startActivity(userIntent);

                                }

                        } else Log.d(TAG, "Error getting document: ", task.getException());

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