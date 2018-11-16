package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventory.data.ItemsContract.ItemsEntry;
import com.example.android.inventory.data.ItemsContract.SupplierEntry;

import static com.example.android.inventory.data.ItemsDbHelper.LOG_TAG;

public class ItemsProvider extends ContentProvider {

    private ItemsDbHelper mDbHelper;

    // URI matcher code for the content URI for the items table
    private static final int ITEMS = 100;

    //URI matcher code for the content URI for a single item in the items table
    private static final int ITEM_ID = 101;

    // URI matcher code for the content URI for the Supplier table
    private static final int SUPPLIER = 200;

    //URI matcher code for the content URI for a single item in the Supplier table
    private static final int SUPPLIER_ID = 201;

    //UriMatcher object to match a content URI to a corresponding code.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY,ItemsContract.PATH_ITEMS,ITEMS);
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY,ItemsContract.PATH_ITEMS + "/#",ITEM_ID);

        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY,ItemsContract.PATH_SUPPLIERS,SUPPLIER);
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY,ItemsContract.PATH_SUPPLIERS + "/#",SUPPLIER_ID);
    }

    //Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        mDbHelper = new ItemsDbHelper(getContext());
        return true;
    }

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                cursor = database.query(ItemsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(ItemsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SUPPLIER:
                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SUPPLIER_ID:
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(SupplierEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert( Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            case SUPPLIER:
                return insertSupplier(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        String name = values.getAsString(ItemsEntry.COLUMN_ITEMS_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        String price = values.getAsString(ItemsEntry.COLUMN_ITEMS_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Item requires a price");
        }

        String quantity = values.getAsString(ItemsEntry.COLUMN_ITEMS_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Item requires a quantity");
        }

        String supplierId = values.getAsString(ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID);
        if (supplierId == null) {
            throw new IllegalArgumentException("Item requires a supplierId");
        }

        String image = values.getAsString(ItemsEntry.COLUMN_ITEMS_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Item requires a image");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ItemsEntry.TABLE_NAME,null,values);

        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertSupplier(Uri uri, ContentValues values) {

        String name = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Supplier requires a name");
        }

        String email = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException("Supplier requires a email");
        }

        String phone = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Supplier requires a phone");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(SupplierEntry.TABLE_NAME,null,values);

        if (id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    //Delete the data at the given selection and selection arguments.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match){
            case ITEMS:
                rowsDeleted = database.delete(ItemsEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ItemsEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case SUPPLIER:
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case SUPPLIER_ID:
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(SupplierEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    //Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, values, selection, selectionArgs);
            case SUPPLIER:
                return updateSuppler(uri, values, selection, selectionArgs);
            case SUPPLIER_ID:
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSuppler(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdatedItem = database.update(ItemsEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdatedItem != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdatedItem;
    }

    private int updateSuppler(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdatedSupplier = database.update(SupplierEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdatedSupplier != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdatedSupplier;
    }

    //Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case ITEMS:
                return ItemsEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemsEntry.CONTENT_ITEM_TYPE;
            case SUPPLIER:
                return SupplierEntry.CONTENT_LIST_TYPE;
            case SUPPLIER_ID:
                return SupplierEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
