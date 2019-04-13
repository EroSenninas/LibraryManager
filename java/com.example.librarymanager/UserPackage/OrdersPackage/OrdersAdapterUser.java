package com.example.librarymanager.UserPackage.OrdersPackage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.librarymanager.R;
import com.example.librarymanager.Book;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class OrdersAdapterUser extends BaseAdapter implements Filterable {

    private static final String TAG = "OrdersAdapterUser";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public Context context;
    public ArrayList<Book> bookArrayList;
    public ArrayList<Book> orig;

    public OrdersAdapterUser(Context context, ArrayList<Book> bookArrayList) {

        super();
        this.context = context;
        this.bookArrayList = bookArrayList;

    }

    public class BookHolder
    {
        TextView name;
        TextView info;
        TextView timeLeft;
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

            convertView = LayoutInflater.from(context).inflate(R.layout.order_row_user, parent, false);

            //locate the views in book_row_user_user.xml
            holder.name = (TextView) convertView.findViewById(R.id.bookName);
            holder.info = (TextView) convertView.findViewById(R.id.bookInfo);
            holder.timeLeft = (TextView) convertView.findViewById(R.id.timeLeft);

            convertView.setTag(holder);
        }
        else
        {
            holder = (BookHolder) convertView.getTag();
        }

        //set the results into textViews
        holder.name.setText(bookArrayList.get(position).getName());
        holder.info.setText(bookArrayList.get(position).getInfo());

        long bookValue = Long.valueOf(bookArrayList.get(position).getDate())/60;
        long thirtyDays = 2592000;

        long answer = bookValue + thirtyDays;

        System.out.println("book value " + bookValue);
        System.out.println("30 days: " + thirtyDays);
        System.out.println("adding both " + answer);
        System.out.println("time now " + Timestamp.now().toDate().getTime());

        long seconds = answer - Timestamp.now().toDate().getTime()/60;

        System.out.println("remaining" + seconds);

        holder.timeLeft.setText(calculateTime(seconds));

        //on ListView item click
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

            }
        });

        //on ListView button item click

        return convertView;
    }

    private String calculateTime(long timeInSeconds) {

        int day = (int) TimeUnit.SECONDS.toDays(timeInSeconds);
        long hours = TimeUnit.SECONDS.toHours(timeInSeconds) -
                TimeUnit.DAYS.toHours(day);
        long minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds) -
                TimeUnit.DAYS.toMinutes(day) -
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.SECONDS.toSeconds(timeInSeconds) -
                TimeUnit.DAYS.toSeconds(day) -
                TimeUnit.HOURS.toSeconds(hours) -
                TimeUnit.MINUTES.toSeconds(minutes);

        return day + " days, " + hours + " hours,\n" + minutes + " minutes, " + seconds + " seconds\nremaining.";

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