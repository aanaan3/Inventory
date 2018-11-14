package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ItemsContract {

    private ItemsContract(){}

    // The "Content authority" is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Possible path (appended to base content URI for possible URI's)
    public static final String PATH_ITEMS = "items";
    public static final String PATH_SUPPLIERS = "suppliers";

    //Inner class that defines constant values for the items database table.
    public static final class ItemsEntry implements BaseColumns{

        // The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        //The MIME type of the {@link #CONTENT_URI} for a list of items.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        //The MIME type of the {@link #CONTENT_URI} for a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;


        public final static String TABLE_NAME = "items";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_ITEMS_NAME ="item_name";

        public final static String COLUMN_ITEMS_PRICE ="item_price";

        public final static String COLUMN_ITEMS_QUANTITY ="item_quantity";

        public final static String COLUMN_ITEMS_SUPPLIER_ID ="item_supplier_id";

        public final static String COLUMN_ITEMS_IMAGE ="item_image";

    }

    //Inner class that defines constant values for the Supplier database table.
    public static final class SupplierEntry implements BaseColumns{

        // The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLIERS);

        //The MIME type of the {@link #CONTENT_URI} for a list of suppliers.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIERS;

        //The MIME type of the {@link #CONTENT_URI} for a single supplier.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLIERS;

        public final static String TABLE_NAME = "suppliers";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_SUPPLIER_NAME ="supplier_name";

        public final static String COLUMN_SUPPLIER_EMAIL ="supplier_email";

        public final static String COLUMN_SUPPLIER_PHONE ="supplier_phone";
    }
}
