package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventory.data.ItemsContract.ItemsEntry;
import com.example.android.inventory.data.ItemsContract.SupplierEntry;
import com.example.android.inventory.EditorActivity;
import com.example.android.inventory.data.ItemsDbHelper;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ITEM_LOADER = 1;

    // This is the Adapter being used to display the list's data
    ItemsCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //displayDatabaseInfo();

        // Find the ListView which will be populated with the item data
        ListView itemListView = (ListView) findViewById(R.id.item_list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new ItemsCursorAdapter(this,null,0);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI,id);

                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        //Kick off the Loader
        getLoaderManager().initLoader(ITEM_LOADER,null,  this);

    }



    /*
    private void displayDatabaseInfo() {


        String[] projection = {
                ItemsEntry._ID,
                ItemsEntry.COLUMN_ITEMS_NAME,
                ItemsEntry.COLUMN_ITEMS_PRICE,
                ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID,
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME
        };

        Cursor cursor = getContentResolver().query(
                ItemsEntry.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);                  // The sort order for the returned rows

        String[] projection2 = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME,
                SupplierEntry.COLUMN_SUPPLIER_EMAIL,
                SupplierEntry.COLUMN_SUPPLIER_PHONE
        };

        Cursor cursor2 = getContentResolver().query(
                SupplierEntry.CONTENT_URI,   // The content URI of the words table
                projection2,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);                  // The sort order for the returned rows



        TextView displayView = (TextView) findViewById(R.id.text_view_items);

        try{


            displayView.setText("The items table contains " + cursor.getCount() + " items.\n\n");
            displayView.append(ItemsEntry._ID + " - " +
                    ItemsEntry.COLUMN_ITEMS_NAME + " - " +
                    ItemsEntry.COLUMN_ITEMS_PRICE + " - " +
                     SupplierEntry._ID + "\n");


            int idColumnIndex = cursor.getColumnIndex(ItemsEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_NAME);
            int supplieridColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_PRICE);
            int idsupplierColumnIndex = cursor.getColumnIndex(SupplierEntry._ID);
            int supplierNameColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME);

            while (cursor.moveToNext()) {
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentSupplierid = cursor.getInt(supplieridColumnIndex);
                int currentIdSupplier = cursor.getInt(idsupplierColumnIndex);
                String currentNameSupplier = cursor.getString(supplierNameColumnIndex);
                displayView.append(("\n" + currentID + " - " + currentName + " - " + currentSupplierid + " - " + currentIdSupplier + " - " + currentNameSupplier));
            }



            int idColumnIndex = cursor.getColumnIndex("v_items._id");
            int nameColumnIndex = cursor.getColumnIndex("v_items.itemsname");
            int supplieridColumnIndex = cursor.getColumnIndex("v_items.supplierid");
            int idsupplierColumnIndex = cursor.getColumnIndex("v_suppliers.suppliers_id");

            while (cursor.moveToNext()) {
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentSupplierid = cursor.getInt(supplieridColumnIndex);
                int currentIdSupplier = cursor.getInt(idsupplierColumnIndex);
                displayView.append(("\n" + currentID + " - " + currentName + " - " + currentSupplierid + " - " + currentIdSupplier));
            }



            displayView.append("\n\nThe items table contains " + cursor2.getCount() + " items.\n\n");
            displayView.append(SupplierEntry._ID + " - " +
                    //SupplierEntry.COLUMN_SUPPLIER_NAME + " - " +
                    //SupplierEntry.COLUMN_SUPPLIER_EMAIL + " - " +
                    SupplierEntry.COLUMN_SUPPLIER_PHONE+"\n");

            int idColumnIndex2 = cursor2.getColumnIndex(SupplierEntry._ID);
            int nameColumnIndex2 = cursor2.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME);
            int emailColumnIndex2 = cursor2.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_EMAIL);
            int phoneColumnIndex2 = cursor2.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_PHONE);

            while (cursor2.moveToNext()) {
                int currentID2 = cursor2.getInt(idColumnIndex2);
                String currentName2 = cursor2.getString(nameColumnIndex2);
                String currentEmail2 = cursor2.getString(emailColumnIndex2);
                String currentPhone2 = cursor2.getString(phoneColumnIndex2);
                displayView.append(("\n" + currentID2 + " - " + currentName2 + " - " + currentEmail2 + " - " + currentPhone2));
            }

        }finally {
            cursor.close();
            cursor2.close();
        }

    }
    */


    /*
    private void insertPet() {

        ContentValues values1 = new ContentValues();
        values1.put(SupplierEntry.COLUMN_SUPPLIER_NAME,"Ali");
        values1.put(SupplierEntry.COLUMN_SUPPLIER_EMAIL,"Ali@gmail.com");
        values1.put(SupplierEntry.COLUMN_SUPPLIER_PHONE,"0503933294");

        getContentResolver().insert(SupplierEntry.CONTENT_URI, values1);

        ContentValues values = new ContentValues();
        values.put(ItemsEntry.COLUMN_ITEMS_NAME, "phone");
        values.put(ItemsEntry.COLUMN_ITEMS_PRICE,"9");
        values.put(ItemsEntry.COLUMN_ITEMS_QUANTITY,"1");
        values.put(ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID,"Ali@gmail.com");

        getContentResolver().insert(ItemsEntry.CONTENT_URI, values);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            /*
            case R.id.action_insert_dummy_data:
                //insertPet();
                //displayDatabaseInfo();
                return true;
                */
            case R.id.action_delete_all_items:
                // Respond to a click on the "Delete all entries" menu option
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String [] projection = {
                BaseColumns._ID,
                ItemsEntry.COLUMN_ITEMS_NAME,
                ItemsEntry.COLUMN_ITEMS_PRICE,
                ItemsEntry.COLUMN_ITEMS_QUANTITY,
                ItemsEntry.COLUMN_ITEMS_IMAGE
        };

        return new CursorLoader(this,
                ItemsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    //Helper method to delete all item and supplier in the database.
    private void deleteAllItems(){
        //int rowsItemsDeleted = getContentResolver().delete(ItemsEntry.CONTENT_URI,null,null);
        // Because we have FOREIGN KEY "ON DELETE CASCADE" in Items Table, So no Need to Call
        // getContentResolver().delete(ItemsEntry.CONTENT_URI,null,null).
        int rowsSuppliersDeleted = getContentResolver().delete(SupplierEntry.CONTENT_URI,null,null);

        //Delete Table and Create.
        // without this procedure you will get FOREIGN KEY constraint failed
        ItemsDbHelper mDbHelper = new ItemsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(db);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete All Items and Suppliers");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllItems();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
