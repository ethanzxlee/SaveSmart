package com.savesmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.nineoldandroids.animation.Animator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by Jason on 7/14/2014.
 */
public class AddProductActivity extends Activity {

    protected TextView.OnFocusChangeListener emptyCheckingListener;
    private Spinner category, subcategory;
    private EditText name, barcode;
    private Button btnAdd;
    private ParseObject categoryID, subCategoryID, productPhotoName;
    private ImageView productPic, addIcon;
    private RelativeLayout rlProductPicChooserLayout;
    private Bitmap bitmap;
    ParseFile newProductPhoto;
    GridView gridGallery;
    GalleryAdapter adapter;
    Handler handler;
    ImageLoader imageLoader;
    ViewSwitcher viewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        name = (EditText) findViewById(R.id.productName);
        barcode = (EditText) findViewById(R.id.productBarcode);
        category = (Spinner) findViewById(R.id.productCategory);
        subcategory = (Spinner) findViewById(R.id.productSubCategory);
        btnAdd = (Button) findViewById(R.id.btn_addProduct);
        final ScrollView svAddProduct = (ScrollView) findViewById(R.id.sv_addProduct);

        emptyCheckingListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    svAddProduct.smoothScrollTo(0, svAddProduct.getBottom());
                if (!b && ((TextView) view).getText().toString().equals(""))
                    ((TextView) view).setError("");
                else
                    ((TextView) view).setError(null);
            }
        };

        rlProductPicChooserLayout = (RelativeLayout) findViewById(R.id.product_image_upload);
        productPic = (ImageView) findViewById(R.id.add_product_pic_chooser);
        rlProductPicChooserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select photo using"), MainApplication.SELECT_FROM_GALLERY);
            }
        });

        name.setOnFocusChangeListener(emptyCheckingListener);
        barcode.setOnFocusChangeListener(emptyCheckingListener);

        //initImageLoader();
        //init();
        addItemsOnCategory();
        addListenerOnSpinnerItemSelection();
        addListenerOnButton();
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    /*private void init() {
        handler = new Handler() {
            @Override
            public void close() {
            }

            @Override
            public void flush() {
            }

            @Override
            public void publish(LogRecord logRecord) {
            }
        };

        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setDisplayedChild(1);

        rlProductPicChooserLayout = (LinearLayout) findViewById(R.id.product_image_upload);
        productPic = (ImageView) findViewById(R.id.add_product_pic_chooser);
        addIcon = (ImageView) findViewById(R.id.add_product_icon);

        final AlertDialog dialog = new AlertDialog.Builder(this).create();

        rlProductPicChooserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setTitle("Select option");
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Take from Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, MainApplication.TAKE_FROM_CAMERA);
                        addIcon.setVisibility(View.INVISIBLE);
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,"Select from Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Action.ACTION_MULTIPLE_PICK);
                        startActivityForResult(intent, MainApplication.SELECT_FROM_GALLERY);
                        addIcon.setVisibility(View.INVISIBLE);
                    }
                });
               dialog.show();
            }
        });
    }*/


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MainApplication.SELECT_FROM_GALLERY) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                        InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        if (bitmap.getHeight() > 512) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 512.0 / bitmap.getHeight()), 512, false);
                        } else if (bitmap.getWidth() > 512) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, 512, (int) (bitmap.getHeight() * 512.0 / bitmap.getWidth()), false);
                        }

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                        inputStream.close();
                        outputStream.close();

                        ParseObject productObject = new ParseObject("Product");
                        newProductPhoto = new ParseFile(productObject.getObjectId() + ".jpg", outputStream.toByteArray());
                        newProductPhoto.saveInBackground();

                        productPhotoName = productObject;
                        productPic.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        /*if (requestCode == MainApplication.TAKE_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            adapter.clear();

                viewSwitcher.setDisplayedChild(1);
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                if (bitmap.getHeight() > 512) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 512.0 / bitmap.getHeight()), 512, false);
                } else if (bitmap.getWidth() > 512) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, 512, (int) (bitmap.getHeight() * 512.0 / bitmap.getWidth()), false);
                }

                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

                byte[] image = outputStream.toByteArray();

                //newProductPhoto = new ParseFile(productPhotoName.getObjectId() + ".jpg", image);
                //newProductPhoto.saveInBackground();

                productPic.setImageBitmap(bitmap);

        }else if (requestCode == MainApplication.SELECT_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");

            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

            for (String string : all_path) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = string;

                try {

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    InputStream inputStream = this.getContentResolver().openInputStream(data.getData());

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap.getHeight() > 512) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 512.0 / bitmap.getHeight()), 512, false);
                } else if (bitmap.getWidth() > 512) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, 512, (int) (bitmap.getHeight() * 512.0 / bitmap.getWidth()), false);
                }

                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                inputStream.close();
                outputStream.close();

                    newProductPhoto = new ParseFile(productPhotoName.getObjectId() + ".jpg", outputStream.toByteArray());
                    newProductPhoto.saveInBackground();

                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dataT.add(item);
            }

            viewSwitcher.setDisplayedChild(0);

            adapter.addAll(dataT);
        }*/
    }

    public void addItemsOnCategory(){
        final List<String> categoryList = new ArrayList<String>();
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");

        query.addAscendingOrder("categoryName");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.setMaxCacheAge(TimeUnit.DAYS.toMillis(1)); //cache age is set to one day only
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < parseObjects.size(); i++)
                        categoryList.add(i,parseObjects.get(i).getString("categoryName"));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
                            android.R.layout.simple_spinner_item, categoryList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    category.setAdapter(dataAdapter);
                } else {}
            }
        });
    }

    public void addListenerOnSpinnerItemSelection(){
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String item = adapterView.getItemAtPosition(i).toString();
                final List<String> subCategoryList = new ArrayList<String>();

                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Category");
                query1.whereEqualTo("categoryName",item);
                query1.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if(parseObject != null){
                            categoryID = parseObject;
                        }
                    }
                } );
                ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Subcategory");
                query2.whereMatchesQuery("subcategory", query1);
                query2.addAscendingOrder("name");
                query2.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                query2.setMaxCacheAge(TimeUnit.DAYS.toMillis(1)); //cache age is set to one day only
                query2.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                        if (e == null) {
                            for (int i = 0; i < parseObjects.size(); i++)
                                subCategoryList.add(parseObjects.get(i).getString("name"));
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
                                    android.R.layout.simple_spinner_item, subCategoryList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            subcategory.setAdapter(dataAdapter);
                        } else {}
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        subcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Subcategory");
                query2.whereEqualTo("name",item);
                query2.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if(parseObject != null){
                            subCategoryID = parseObject;
                        }
                    }
                } );
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void addListenerOnButton(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String productName = name.getText().toString().trim();
                final String productBarcode = barcode.getText().toString().trim();

                if (productName.equals("") || productBarcode.equals("")) {
                    Toast.makeText(getBaseContext(),"Please fill in empty fields.", Toast.LENGTH_LONG).show();
                    if (productName.equals(""))
                        name.setError("Product name can't be empty.");
                    if (productBarcode.equals(""))
                        barcode.setError("Product barcode can't be empty.");
                }
                else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(barcode.getWindowToken(), 0);

                    final RelativeLayout rlAddingProduct = (RelativeLayout) findViewById(R.id.rl_adding_product);

                    rlAddingProduct.setVisibility(View.VISIBLE);
                    rlAddingProduct.setAnimation(new Animation() {
                    });
                    animate(rlAddingProduct).alpha(0f).setDuration(0).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            name.setFocusable(false);
                            barcode.setFocusable(false);
                            category.setClickable(false);
                            subcategory.setClickable(false);
                            btnAdd.setClickable(false);

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animate(rlAddingProduct).alpha(1f).setDuration(320).start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }

                    }).start();

                    final ParseObject productObject;
                    final ParseObject productPhoto = new ParseObject("ProductPhoto");

                    productObject = productPhotoName;

                    ParseQuery<ParseObject> nameQuery = ParseQuery.getQuery("Product");
                    nameQuery.whereEqualTo("productName", productName);

                    ParseQuery<ParseObject> barcodeQuery = ParseQuery.getQuery("Product");
                    barcodeQuery.whereEqualTo("productBarcode", productBarcode);

                    List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
                    queries.add(nameQuery);
                    queries.add(barcodeQuery);

                    ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
                    mainQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if(parseObject == null){
                                productObject.put("productName",productName);
                                productObject.put("productBarcode", productBarcode);
                                productObject.put("productCategory",categoryID);
                                productObject.put("productSubCategory",subCategoryID);

                                if(newProductPhoto != null) {
                                    productPhoto.put("product", productObject);
                                    productPhoto.put("photo", newProductPhoto);
                                }

                                productPhoto.saveInBackground();
                                productObject.saveInBackground();
                                Toast.makeText(getBaseContext(), "Product successfuly added.", Toast.LENGTH_LONG).show();
                                rlAddingProduct.setVisibility(View.INVISIBLE);
                                finish();
                            }else{
                                name.setError("");
                                barcode.setError("");
                                Toast.makeText(getBaseContext(), "Product already existed.", Toast.LENGTH_LONG).show();
                                animate(rlAddingProduct).alpha(0f).setDuration(320).setListener(null).start();
                                name.setFocusableInTouchMode(true);
                                barcode.setFocusableInTouchMode(true);
                                category.setClickable(true);
                                subcategory.setClickable(true);
                                btnAdd.setClickable(true);
                            }
                        }
                    });
                }
            }
        });
    }
}
