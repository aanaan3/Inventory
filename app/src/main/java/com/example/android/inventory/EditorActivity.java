package com.example.android.inventory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.android.inventory.data.ItemsContract.ItemsEntry;
import com.example.android.inventory.data.ItemsContract.SupplierEntry;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

//Allows user to create a new item and supplier or edit an existing one.
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //All View to enter the item and supplier information.
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private ImageView mItemImageView;
    private Spinner mSpinner;
    private EditText mEmailEditText;
    private EditText mPhoneEditText;

    //Content URI for the existing item (null if it's a new item)
    private Uri mCurrentItemUri;

    //Identifier for the item data loader.
    private static final int EXISTING_ITEM_LOADER = 0;

    //List of supplier name for spinner that allows the user to select the supplier of the item.
    private ArrayList<String> supplierNameArrayList;

    //supplier ID for update
    private long mSupplierId;

    //supplier name the Selected from dropdown spinner
    private String mSupplierName;

    //Boolean flag that keeps track of whether the supplier has been new (true) or not (false)
    private boolean mSupplierNew = false;

    //private int currentQuantity;

    //Identifier for image request Code for startActivityForResult
    private static final int PICK_IMAGE = 100;

    //image uri
    private String imageUri="";

    //Boolean flag that keeps track of whether the item and supplier has been edited (true) or not (false)
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    ActivityResultLauncher<String> activityResultLauncher;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain a item content URI, then we know that we are
        // creating a new pet.
        if (mCurrentItemUri == null){
            // If the intent DOES NOT contain a item content URI, then we know that we are
            // creating a new item.
            setTitle("Add Item");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        }else {
            // Otherwise this is an existing item, so change app bar to say "Edit item"
            setTitle("Edit Item");
            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_item_name);
        mPriceEditText = findViewById(R.id.edit_item_price);
        mQuantityEditText = findViewById(R.id.edit_item_quantity);
        mItemImageView = findViewById(R.id.item_image_view);
        mSpinner = findViewById(R.id.spinner_supplier_name);
        mEmailEditText = findViewById(R.id.edit_supplier_email);
        mPhoneEditText = findViewById(R.id.edit_supplier_phone);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSpinner.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mItemImageView.setOnTouchListener(mTouchListener);

        setupSpinner();

        activityResultLauncher = registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        new ActivityResultCallback<Uri>() {
                            @Override
                            public void onActivityResult(Uri result) {
                                if (result == null) {
                                    Toast.makeText(getBaseContext(),"error upload image",Toast.LENGTH_LONG).show();
                                    return;
                                }

                                try {
                                    InputStream inputStream =
                                            getBaseContext().getContentResolver().openInputStream(result);
                                    Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                                    mItemImageView.setImageBitmap(bmp);
                                    imageUri = result.toString();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    //Setup the dropdown spinner that allows the user to insert and select the name of the supplier.
    private void setupSpinner(){

        // Fill the array list of suppliers name.
        String[] projection = {
                SupplierEntry.COLUMN_SUPPLIER_NAME
        };

        Cursor cursor = getContentResolver().query(
                SupplierEntry.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);

        try{
            int supplierNameColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME);

            supplierNameArrayList = new ArrayList<String>();

            supplierNameArrayList.add(getString(R.string.select_supplier));
            supplierNameArrayList.add(getString(R.string.add_new_supplier));

            while (cursor.moveToNext()){
                supplierNameArrayList.add(cursor.getString(supplierNameColumnIndex));
            }

        }finally {
            cursor.close();
        }

        // Create adapter for spinner. The list options are from the Array List it will use.
        // the spinner will use the default layout.
        final ArrayAdapter<String> supplierNameArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,supplierNameArrayList);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierNameArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSpinner.setAdapter(supplierNameArrayAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSupplierName = (String) parent.getItemAtPosition(position);
                mSupplierId = 0;
                mSupplierId = id-1;
                //AlertDialog for add new supplier in spinner
                if (mSupplierName.equals(getString(R.string.add_new_supplier))){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                    builder.setMessage(R.string.add_new_supplier_name);
                    builder.setTitle(R.string.new_supplier);
                    final EditText input = new EditText(EditorActivity.this);
                    builder.setView(input);
                    builder.setPositiveButton("yes",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String supplierNameInput = input.getText().toString().trim();
                            if (!supplierNameInput.isEmpty()){
                                supplierNameArrayList.add(supplierNameInput);
                                mSupplierName=supplierNameInput;
                                Toast.makeText(getApplicationContext(),getString(R.string.supplier_name_inserted) + mSupplierName,
                                        Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(getApplicationContext(),R.string.insert_supplier_name_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    //Boolean flag that keeps track of whether the supplier has been new (true) or not (false)
                    //that use for insert new supplier
                    mSupplierNew = true;

                }else {
                    //if supplier existed get all details for supplier
                    getSupplierDetails(position-2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });
    }

    //Get user input from editor and save new item into database.
    private void saveData(){

        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString().trim());

        // Create a ContentValues object for item
        ContentValues itemEntryValues = new ContentValues();
        itemEntryValues.put(ItemsEntry.COLUMN_ITEMS_NAME, nameString);
        itemEntryValues.put(ItemsEntry.COLUMN_ITEMS_PRICE,priceString);
        itemEntryValues.put(ItemsEntry.COLUMN_ITEMS_QUANTITY,quantityInt);
        itemEntryValues.put(ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID, mSupplierId);
        itemEntryValues.put(ItemsEntry.COLUMN_ITEMS_IMAGE,imageUri);

        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // Before update a new item, we need update or insert new supplier.
            saveSupplierData();

            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri itemUri = getContentResolver().insert(ItemsEntry.CONTENT_URI, itemEntryValues);

            // Show a toast message depending on whether or not the insertion was successful.
            if (itemUri == null) {
                Toast.makeText(this, R.string.insert_item_failed,Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.insert_item_successful,Toast.LENGTH_LONG).show();
            }
        } else {
            // Before update a new item, we need update or insert new supplier.
            saveSupplierData();

            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowItemAffected = getContentResolver().update(mCurrentItemUri, itemEntryValues, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowItemAffected == 0) {
                Toast.makeText(this, R.string.update_item_failed,Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.update_item_successful,Toast.LENGTH_LONG).show();
            }
        }
    }

    //Get user input from editor and save new supplier into database.
    private void saveSupplierData(){

        // Read from input fields
        String emailString = mEmailEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();

        // Create a ContentValues object for supplier
        ContentValues supplierEntryValues = new ContentValues();
        supplierEntryValues.put(SupplierEntry.COLUMN_SUPPLIER_NAME,mSupplierName);
        supplierEntryValues.put(SupplierEntry.COLUMN_SUPPLIER_EMAIL,emailString);
        supplierEntryValues.put(SupplierEntry.COLUMN_SUPPLIER_PHONE,phoneString);

        try {
            // Determine if this is a new or existing supplier by checking if mSupplierNew is true or false.
            if (mSupplierNew){
                // This is a NEW supplier, so insert a new supplier into the provider,
                // returning the content URI for the new supplier.
                Uri supplierUri = getContentResolver().insert(SupplierEntry.CONTENT_URI,supplierEntryValues );
                // Show a toast message depending on whether or not the insertion was successful.
                if (supplierUri == null) {
                    Toast.makeText(this,R.string.insert_supplier_failed,Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.insert_supplier_successful,Toast.LENGTH_LONG).show();
                }
            }else {
                // Otherwise this is an EXISTING supplier, so update the supplier with content URI: mCurrentSupplierUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentSupplierUri will already identify the correct row in the database that
                // we want to modify.
                Uri mCurrentSupplierUri = ContentUris.withAppendedId(SupplierEntry.CONTENT_URI, mSupplierId);
                int rowSupplierAffected = getContentResolver().update(mCurrentSupplierUri,supplierEntryValues,null,null);
                // Show a toast message depending on whether or not the update was successful.
                if (rowSupplierAffected == 0) {
                    Toast.makeText(this, R.string.update_supplier_failed,Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.update_supplier_successful,Toast.LENGTH_SHORT).show();
                }
            }
        // Show a toast message depending on whether the supplier was existed.
        }catch (Exception e){Toast.makeText(this, "supplier existed",Toast.LENGTH_SHORT).show();}

    }

    // check the user input value is valid.
    private Boolean checkValidateInputs(){
        Boolean check = true;

        if (mNameEditText.length() == 0){
            Toast.makeText(this,R.string.enter_valid_item_name,Toast.LENGTH_LONG).show();
            check = false;
        }else if (mPriceEditText.length() == 0){
            Toast.makeText(this,R.string.enter_valid_item_price,Toast.LENGTH_LONG).show();
            check = false;
        }else if (mQuantityEditText.length() <= 0){
            Toast.makeText(this,R.string.enter_valid_item_quantity,Toast.LENGTH_LONG).show();
            check = false;
        }else if (mPhoneEditText.length() == 0){
            Toast.makeText(this,R.string.enter_valid_supplier_phone,Toast.LENGTH_LONG).show();
            check = false;
        }else if (mEmailEditText.length() == 0){
            Toast.makeText(this,R.string.enter_valid_supplier_email,Toast.LENGTH_LONG).show();
            check = false;
        }else if (imageUri.isEmpty()){
            Toast.makeText(this,R.string.upload_item_image,Toast.LENGTH_LONG).show();
            check = false;
        }else if (mSpinner.getSelectedItemPosition() < 2){
            Toast.makeText(this,R.string.select_supplier_name,Toast.LENGTH_LONG).show();
            check = false;
        }

        return check;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    //This method is called after invalidateOptionsMenu(), so that the
    //menu can be updated (some menu items can be hidden or made visible).
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentItemUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch(item.getItemId()){
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Check if user input valid true or false.
                if (checkValidateInputs()){
                    // Save item and supplier to database
                    saveData();
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mItemHasChanged) {
                    // If the item of supplier hasn't changed, continue with navigating up to parent activity
                    // which is the {@link CatalogActivity}.
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String [] projection = {
                ItemsEntry._ID,
                ItemsEntry.COLUMN_ITEMS_NAME,
                ItemsEntry.COLUMN_ITEMS_PRICE,
                ItemsEntry.COLUMN_ITEMS_QUANTITY,
                ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID,
                ItemsEntry.COLUMN_ITEMS_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_QUANTITY);
            int supplierIdColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_SUPPLIER_ID);
            int imageColumnIndex = cursor.getColumnIndex(ItemsEntry.COLUMN_ITEMS_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int supplierId = cursor.getInt(supplierIdColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(Integer.toString(quantity));
            mItemImageView.setImageURI(Uri.parse(image));
            imageUri = Uri.parse(image).toString();

            // Based on supplierId column in item table, will get supplier details.
            loadSupplier(supplierId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mEmailEditText.setText("");
        mPhoneEditText.setText("");
    }

    // Get supplier details.
    // @param supplierId column in item table
    private void loadSupplier(int supplierId){

        //define a projection that contains all columns from the supplier table
        String[] projection = {
                BaseColumns._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME,
                SupplierEntry.COLUMN_SUPPLIER_EMAIL,
                SupplierEntry.COLUMN_SUPPLIER_PHONE
        };

        //define a selection by supplier ID
        String selection = SupplierEntry._ID + " =?";
        String[] selectionArgs = {String.valueOf(supplierId)};

        Cursor cursor = getContentResolver().query(
                SupplierEntry.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                selection,                   // Selection criteria
                selectionArgs,                   // Selection criteria
                null);                  // The sort order for the returned rows

        int currentId = 0;
        String currentEmail = null;
        String currentPhone = null;

        try{
            int idColumnIndex = cursor.getColumnIndex(SupplierEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME);
            int emailColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_EMAIL);
            int phoneColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_PHONE);

            if (cursor.moveToFirst()) {
                currentId = cursor.getInt(idColumnIndex);
                currentEmail = cursor.getString(emailColumnIndex);
                currentPhone = cursor.getString(phoneColumnIndex);
            }

            mSpinner.setSelection(currentId+1);
            mEmailEditText.setText(currentEmail);
            mPhoneEditText.setText(currentPhone);

        }finally{cursor.close();}
    }

    // Get supplier details if user select supplier name form spinner
    // @param position supplier name in spinner
    private void getSupplierDetails(int position){

        String[] projection = {
                SupplierEntry.COLUMN_SUPPLIER_EMAIL,
                SupplierEntry.COLUMN_SUPPLIER_PHONE
        };

        Cursor cursor = getContentResolver().query(
                SupplierEntry.CONTENT_URI,   // The content URI of the words table
                projection,             // The columns to return for each row
                null,                   // Selection criteria
                null,                   // Selection criteria
                null);                  // The sort order for the returned rows

        String currentEmail = null;
        String currentPhone = null;

        try{
            int emailColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_EMAIL);
            int phoneColumnIndex = cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_PHONE);

            while (cursor.moveToPosition(position)) {
                currentEmail = cursor.getString(emailColumnIndex);
                currentPhone = cursor.getString(phoneColumnIndex);
                break;
            }

            mEmailEditText.setText(currentEmail);
            mPhoneEditText.setText(currentPhone);

        }finally{cursor.close();}
    }

    // This method is called when the increase button is clicked.
    public void increase(View view){
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        quantity = quantity + 1;
        mQuantityEditText.setText(Integer.toString(quantity));
    }

    // This method is called when the decrease button is clicked.
    public void decrease(View view){
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        if (quantity > 0){
            quantity = quantity - 1;
            mQuantityEditText.setText(Integer.toString(quantity));
        }
    }

    // Prompt the user to confirm that they want to delete this item.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Perform the deletion of the item in the database.
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.delete_item_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.item_deleted,
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity.
        finish();
    }

    // This method is called when the upload image button is clicked.
    public void uploadImage(View view) {

        activityResultLauncher.launch("image/*");

//        Intent intent = new Intent();
//        if (Build.VERSION.SDK_INT > 21) {
//            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        }else {
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//        }
//
//        intent.setType("image/*");
//
//        mItemImageView.setImageMatrix(new Matrix());
//        mItemImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this,"error upload image",Toast.LENGTH_LONG).show();
                return;
            }

            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                mItemImageView.setImageBitmap(bmp);
                imageUri = data.getDataString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    //This method is called when the order button is clicked.
    public void orderMore(View view) {

        // Find all relevant views that we will need to knew user select
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radio_group);
        RadioButton phone = (RadioButton) findViewById(R.id.radio_phone);
        RadioButton email = (RadioButton) findViewById(R.id.radio_email);

        int selectedId = radioGroup.getCheckedRadioButtonId();

        Intent intent = new Intent();

        String supplierEmail = mEmailEditText.getText().toString().trim();
        String supplierPhone = mPhoneEditText.getText().toString().trim();

        switch(selectedId) {
            // Respond to a click on the "phone" radio button
            case R.id.radio_phone:
                if (selectedId == phone.getId())
                    if (!supplierPhone.isEmpty()){
                        intent = new Intent(Intent.ACTION_DIAL);
                        // only phone apps should handle this
                        intent.setData(Uri.parse("tel:" + supplierPhone));
                        startActivity(intent);
                    }else {
                        Toast.makeText(this,R.string.order_require_phone_number,Toast.LENGTH_LONG).show();
                    }
                    break;
            // Respond to a click on the "email" radio button
            case R.id.radio_email:
                if (selectedId == email.getId())
                    if (!supplierEmail.isEmpty()){
                        String itemName = mNameEditText.getText().toString().trim();

                        String orderMessage = "Inventory Order of Product:";
                        orderMessage += "\n" + "Item Name: " + itemName;
                        orderMessage += "\n" + "Quantity: ";
                        orderMessage += "\n\n" + "Thank You";

                        // only email apps should handle this
                        intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + supplierEmail));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Inventory Order");
                        intent.putExtra(Intent.EXTRA_TEXT, orderMessage);

                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }else {
                        Toast.makeText(this,R.string.order_require_email,Toast.LENGTH_LONG).show();
                    }
                    break;
        }
    }

    //Show a dialog that warns the user there are unsaved changes that will be lost
    //if they continue leaving the editor.
    //@param discardButtonClickListener is the click listener for what to do when
    //the user confirms they want to discard their changes.
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the item or supplier hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

}
