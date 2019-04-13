package com.example.librarymanager.AdminPackage.ClientsPackage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.librarymanager.AdminPackage.ClientsPackage.OrdersPackage.OrdersActivityAdmin;
import com.example.librarymanager.AdminPackage.ClientsPackage.RequestsPackage.RequestsActivityAdmin;
import com.example.librarymanager.R;
import com.example.librarymanager.Book;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "OrdersAdapterAdmin";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Context context;
    public ArrayList<Book> usersList;
    public ArrayList<Book> orig;

    public UsersAdapter(Context context, ArrayList<Book> usersList) {

        super();
        this.context = context;
        this.usersList = usersList;

    }

    public class BookHolder
    {
        TextView email;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
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

            convertView = LayoutInflater.from(context).inflate(R.layout.users_row, parent, false);

            holder.email = (TextView) convertView.findViewById(R.id.email);

            convertView.setTag(holder);
        } else {
            holder = (BookHolder) convertView.getTag();
        }

        holder.email.setText(usersList.get(position).getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

            }
        });

        Button orderButton = (Button) convertView.findViewById(R.id.selectButton);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("Manage user's data");
                alertDialog.setMessage(
                        "Manage user's " + usersList.get(position).getName() + " data: ");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Orders",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                Intent adminIntent = new Intent(context, OrdersActivityAdmin.class);
                                adminIntent.putExtra("id", usersList.get(position).getID());
                                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(adminIntent);

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Requests",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                Intent adminIntent = new Intent(context, RequestsActivityAdmin.class);
                                adminIntent.putExtra("id", usersList.get(position).getID());
                                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(adminIntent);

                            }
                        });

                alertDialog.show();
            }
        });

        return convertView;
    }

    public void updateList() {

        usersList.clear();

        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            List<DocumentSnapshot> listOfUsers = task.getResult().getDocuments();

                            for (int i = 0; i < listOfUsers.size(); i++) {

                                usersList.add(new Book(listOfUsers.get(i).get("email").toString()));

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

                if (orig == null) orig = usersList;
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

                usersList = (ArrayList<Book>) results.values;
                notifyDataSetChanged();

            }
        };
    }
}