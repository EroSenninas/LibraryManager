package com.example.librarymanager;

public class Book {

    private String ID;
    private String documentID;
    private String name;
    private String info;
    private String date;
    private int quantity;
    private boolean status;

    // constructos for users list
    public Book(String ID) {

        this.ID = ID;

    }

    // constructos for users list
    public Book(String ID, String name) {

        this.ID = ID;
        this.name = name;

    }

    // constructor for books' list
    public Book(String ID, String name, String info, int quantity) {

        this.ID = ID;
        this.name = name;
        this.info = info;
        this.quantity = quantity;

    }

    // constructor for wishes' list
    // constructor for requests' list
    public Book(String ID, String name, String info) {

        this.ID = ID;
        this.name = name;
        this.info = info;

    }

    // constructor for orders' list
    public Book(String ID, String name, String info, String date) {

        this.ID = ID;
        this.name = name;
        this.info = info;
        this.date = date;

    }

    // constructor for orders' list
    public Book(String ID, String name, String info, boolean status) {

        this.ID = ID;
        this.name = name;
        this.info = info;
        this.status = status;

    }

    // constructor for orders' list
    public Book(String ID, String documentID, String name, String info, String date) {

        this.ID = ID;
        this.documentID = documentID;
        this.name = name;
        this.info = info;
        this.date = date;

    }

    public String getID() { return ID; }
    public String getDocumentID() { return documentID; }
    public String getName() { return name; }
    public String getInfo() { return info; }
    public String getDate() { return date; }
    public int getQuantity() { return quantity; }
    public boolean getStatus() { return status; }

    public void setDocumentID(String documentID) { this.documentID = documentID; }
    public void setName(String name) { this.name = name; }
    public void setInfo(String info) { this.info = info; }
    public void setDate(String date) { this.date = date; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setStatus(boolean status) { this.status = status; }

}
