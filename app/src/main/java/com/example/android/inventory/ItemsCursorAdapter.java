package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.inventory.data.ItemsContract.ItemsEntry;

public class ItemsCursorAdapter extends CursorAdapter {


    public ItemsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_items,parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name_textview);
        TextView priceTextView = (TextView)view.findViewById(R.id.price_textview);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_textview);
        ImageView itemImageView = (ImageView) view.findViewById(R.id.item_image_view_list_item);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of item attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndexOrThrow(ItemsEntry._ID);
        int nameColumnIndex = cursor.getColumnIndexOrThrow(ItemsEntry.COLUMN_ITEMS_NAME);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(ItemsEntry.COLUMN_ITEMS_PRICE);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(ItemsEntry.COLUMN_ITEMS_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndexOrThrow(ItemsEntry.COLUMN_ITEMS_IMAGE);

        // Read the item attributes from the Cursor for the current item
        final int itemId = cursor.getInt(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        String image = cursor.getString(imageColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        quantityTextView.setText(Integer.toString(itemQuantity));
        itemImageView.setImageURI(Uri.parse(image));

        // This method is called when the sale button is clicked.
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemQuantity > 0){
                    int newQuantity = itemQuantity - 1;
                    // Create a ContentValues object for item
                    ContentValues itemEntryValues = new ContentValues();
                    itemEntryValues.put(ItemsEntry.COLUMN_ITEMS_QUANTITY,newQuantity);
                    //update the item with content URI: currentItemsUri
                    Uri currentItemsUri = ContentUris.withAppendedId(ItemsEntry.CONTENT_URI,itemId);
                    int rowItemAffected = context.getContentResolver().update(currentItemsUri,itemEntryValues,null,null);
                    // Show a toast message depending on whether or not the update was successful.
                    if (rowItemAffected == 0) {
                        Toast.makeText(context, R.string.decrease_quantity_failed,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.decrease_quantity_successful,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}
