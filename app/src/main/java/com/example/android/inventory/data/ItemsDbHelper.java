package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.example.android.inventory.data.ItemsContract.ItemsEntry;
import com.example.android.inventory.data.ItemsContract.SupplierEntry;

public class ItemsDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemsDbHelper.class.getSimpleName();

    //Name of the database file.
    public static final String DATABASE_NAME = "inventory.db";

    //Database version.
    private static final int DATABASE_VERSION = 1;

    public ItemsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //This is called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS "+ItemsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+SupplierEntry.TABLE_NAME);

        // Create a String that contains the SQL statement to create the items table
        String SQL_CREATE_ITEMS_TABLE =  "CREATE TABLE " + ItemsEntry.TABLE_NAME
                + " ("
                + ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemsEntry.COLUMN_ITEMS_NAME  + " TEXT NOT NULL,"
                + ItemsEntry.COLUMN_ITEMS_PRICE + " TEXT NOT NULL,"
                + ItemsEntry.COLUMN_ITEMS_QUANTITY + " INTEGER NOT NULL,"
                + ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID + " INTEGER NOT NULL,"
                + ItemsEntry.COLUMN_ITEMS_IMAGE + " TEXT NOT NULL,"
                + " FOREIGN KEY(" + ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID + ")REFERENCES "
                + SupplierEntry.TABLE_NAME + "(" + SupplierEntry._ID + ")ON UPDATE CASCADE ON DELETE CASCADE"
                + ");";

        // Create a String that contains the SQL statement to create the supplier table
        String SQL_CREATE_SUPPLIER_TABLE =  "CREATE TABLE " + SupplierEntry.TABLE_NAME + " ("
                + SupplierEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SupplierEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + SupplierEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, "
                + SupplierEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL);";

        //UNIQUE

        String SQL_CREATE_ITEMS_TABLE_VIEW = "CREATE VIEW " + "v_items" + " AS SELECT " +
                ItemsEntry._ID + " AS '_id', " +
                ItemsEntry.COLUMN_ITEMS_NAME + " AS 'item_name', " +
                ItemsEntry.COLUMN_ITEMS_PRICE + " AS 'item_price', " +
                ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID + " AS 'item_email'";

        String SQL_CREATE__SUPPLIERS_VIEW = "CREATE VIEW " + "v_suppliers" + " AS SELECT " +
                SupplierEntry._ID + " AS 'suppliers_id', " +
                SupplierEntry.COLUMN_SUPPLIER_NAME + " AS 'suppliers_name', " +
                SupplierEntry.COLUMN_SUPPLIER_EMAIL + " AS 'suppliers_email'," +
                SupplierEntry.COLUMN_SUPPLIER_PHONE + " AS 'suppliers_phone'";

        // Execute the SQL statement.
        db.execSQL("DROP TABLE IF EXISTS "+ItemsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+SupplierEntry.TABLE_NAME);
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
        db.execSQL(SQL_CREATE_SUPPLIER_TABLE);
        //db.execSQL(SQL_CREATE_ITEMS_TABLE_VIEW);
        //db.execSQL(SQL_CREATE__SUPPLIERS_VIEW);
    }

    //This is called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

     @Override
    public void onConfigure (SQLiteDatabase db){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        } else {
        db.execSQL("PRAGMA foreign_keys=ON");
        }
    }

    @Override
    public synchronized void close() {
        super.close();
        //db.execSQL("DROP TABLE IF EXISTS "+ItemsEntry.TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS "+SupplierEntry.TABLE_NAME);
    }
}
