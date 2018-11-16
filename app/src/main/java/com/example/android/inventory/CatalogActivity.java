package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
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

        // Because we have FOREIGN KEY "ON DELETE CASCADE" in Items Table, So no Need to Call
        // getContentResolver().delete(ItemsEntry.CONTENT_URI,null,null).
        int rowsSuppliersDeleted = getContentResolver().delete(SupplierEntry.CONTENT_URI,null,null);

        //Delete Table and Create.
        // without this procedure you will get FOREIGN KEY constraint failed
        ItemsDbHelper mDbHelper = new ItemsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(db);
    }

    // Dialog Delete Confirmation
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
