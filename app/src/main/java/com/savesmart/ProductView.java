package com.savesmart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;


/**
 * Created by LestonYong on 10/7/2014.
 */
public class ProductView extends Fragment {

    static String gTitle;
    CardArrayAdapter mCardArrayAdapter;
    CardListView listView;
    Card card;
    MapDetailFragment mapDetailFragment;
    static String ProductObjectId, retailerObjectid;
    String ProductName;
    static String retailerName;
    ArrayList<ParseObject> retailerData = new ArrayList<ParseObject>();
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    Bitmap bitmap;
    productinnerClass card1;
    CardView cardView1;
    Button btn;
    ImageButton updatePrice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = getArguments();
        ProductObjectId = bundle.getString("objectId");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_list, container, false);
    }

    public void updatePriceOnClick(View view) {
        final DialogFragment updatePriceDialogFragment = new updatePriceDialogFragment();
        updatePriceDialogFragment.show(getFragmentManager(), "UpdatePrice");
    }

    public static class updatePriceDialogFragment extends DialogFragment {

        EditText price;
        Spinner retailers;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.update_price, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.update_price_thanks);
            builder.setView(view);
            builder.setPositiveButton(R.string.action_ok, null);
            price = (EditText) view.findViewById(R.id.et_update_price);
            retailers = (Spinner) view.findViewById(R.id.update_retailer);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.retailers_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            retailers.setAdapter(adapter);

            retailers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String item = adapterView.getItemAtPosition(i).toString();
                    retailerName = item;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            builder.setNegativeButton(R.string.action_cancel, null);

            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            final AlertDialog alertDialog = (AlertDialog) getDialog();

            if (alertDialog != null) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (price.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.error_price_empty, Toast.LENGTH_SHORT).show();
                            price.setError("");
                        } else {
                            String productPrice = price.getText().toString().trim();
                            final double newPrice = Double.parseDouble(productPrice);

                            final ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Retailers");
                            query1.whereEqualTo("retailerName",retailerName.toUpperCase());
                            query1.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if(e!=null){
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        System.out.println(e.getMessage());
                                    }else {
                                        retailerObjectid = parseObject.getObjectId();
                                        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Price");
                                        ParseObject P_Object = ParseObject.createWithoutData("Product",ProductObjectId);
                                        ParseObject R_Object = ParseObject.createWithoutData("Retailers",retailerObjectid);
                                        //query.whereEqualTo("product",ProductObjectId );
                                        //query.whereEqualTo("retailers", retailerObjectid);
                                        query.whereEqualTo("product",P_Object );
                                        query.whereEqualTo("retailers", R_Object);
                                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject parseObject, ParseException e) {
                                                if (e== null){
                                                    parseObject.put("price",newPrice);
                                                    parseObject.saveInBackground();
                                                } else {
                                                    Toast.makeText(getActivity(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });
                                    }


                                }
                            });

                            alertDialog.cancel();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cardView1 = (CardView)(getActivity().findViewById(R.id.myProductView));

        card1 = new productinnerClass(this.getActivity());

        setEmpty();
        getProductDetail(ProductObjectId);

    }

    private void retailerList(){

        ArrayList<Card> cards = new ArrayList<Card>();

        ArrayList<String> TitleTest = new ArrayList<String>();
        TitleTest.add("Jusco");
        TitleTest.add("Tesco");
        TitleTest.add("Giant");

        ArrayList<String> SubTitleTest = new ArrayList<String>();

        String temp;



            for (int i = 0; i < 3; i++) {
                temp = retailerData.get(i).getParseObject("retailers").getString("retailerName");
                if (temp.equals("JUSCO")) {
                    SubTitleTest.add(decimalFormat.format(retailerData.get(i).getDouble("price")));
                    break;
                }
            }

            for (int i = 0; i < 3; i++) {
                temp = retailerData.get(i).getParseObject("retailers").getString("retailerName");
                if (temp.equals("TESCO")) {
                    SubTitleTest.add(decimalFormat.format(retailerData.get(i).getDouble("price")));
                    break;
                }
            }

            for (int i = 0; i < 3; i++) {
                temp = retailerData.get(i).getParseObject("retailers").getString("retailerName");
                if (temp.equals("GIANT")) {
                    SubTitleTest.add(decimalFormat.format(retailerData.get(i).getDouble("price")));
                    break;
                }
            }


            ArrayList<Integer> img = new ArrayList<Integer>();
            img.add(R.drawable.retailer_jusco);
            img.add(R.drawable.retailer_tesco);
            img.add(R.drawable.retailer_giant);

            for (int i = 0; i < 3; i++) {

                innerClass card = new innerClass(this.getActivity());
                card.setTitle(TitleTest.get(i));
                card.setSecondaryTitle(SubTitleTest.get(i));
                card.setImg(img.get(i));
                card.init();
                cards.add(card);

            }
            CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
        try {
            listView = (CardListView) getActivity().findViewById(R.id.myList);
            listView.setAdapter(mCardArrayAdapter);
        }catch (Exception e){

        }

    }

    public void setEmpty(){


        card1.setDisplayProductName("");
        card1.setDisplayProductLowPrice("");
        //card1.setDisplayProductImg(bitmap);
        cardView1.setCard(card1);
    }

    public void displayproduct (){
        double tempPrice;
        String PriceInString;

        try {
            tempPrice = retailerData.get(0).getDouble("price");

            for (int i = 0; i < retailerData.size(); i++) {
                if (tempPrice > retailerData.get(i).getDouble("price"))
                    tempPrice = retailerData.get(i).getDouble("price");
            }

            PriceInString = decimalFormat.format(tempPrice);

            card1.setDisplayProductName(ProductName);
            card1.setDisplayProductLowPrice(PriceInString);
        } catch (Exception e){

        }



    }

    private void getProductDetail (String objectID){

        final HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("productObjectId", ProductObjectId);

        ParseCloud.callFunctionInBackground("GetProductDetails", hashMap, new FunctionCallback<HashMap<String, Object>>() {
            @Override
            public void done(HashMap<String, Object> data, ParseException e) {
                try{
                    if (e == null){

                        ProductName = ((ParseObject)data.get("product")).getString("productName");
                        retailerData = ((ArrayList<ParseObject>)data.get("price"));
                        //ParseFile parseFile = ((ArrayList<ParseObject>)data.get(0).data.get("photo")).getParseFile("photo");

                        ((ArrayList<ParseObject>)data.get("photo")).get(0).getParseFile("photo").getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                if (e == null){

                                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    card1.setDisplayProductImg(bitmap);
                                    cardView1.refreshCard(card1);
                                }

                                else Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });


                    } else {

                    }
                } catch (Exception e1){

                }
                displayproduct();
                //retailerList();
            }
        });
    }

    public String gettitle(){
        return gTitle;
    }

    public void settitle(String a){
        gTitle = a;
    }

    public class productinnerClass extends Card {

        protected TextView mDisplayProductName, mDisplayLowPrice;
        protected ImageView mDisplayProductImage;
        protected ImageButton mDisplayProductAddToListBtn, mDisaplyProductUpdate;
        protected ToggleButton mDisplayProductFavouriteToggleBtn;
        protected String displayProductName, displayProductLowPrice;
        protected int displayProductAddToListBtn, displayProductFavouriteToggleBtn;
        protected Bitmap displayProductImg;

        public productinnerClass(Context context) {
            this(context, R.layout.display_product_layout);
        }

        public productinnerClass(Context context, int innerlayout) {
            super(context, innerlayout);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            mDisplayProductName = (TextView) parent.findViewById(R.id.display_Product_Name);
            mDisplayLowPrice = (TextView) parent.findViewById(R.id.display_Product_Low_Price);
            mDisplayProductImage = (ImageView) parent.findViewById(R.id.display_Product_Image);
            mDisplayProductAddToListBtn = (ImageButton) parent.findViewById(R.id.display_Product_Add_To_List_Icon);
            mDisplayProductFavouriteToggleBtn = (ToggleButton) parent.findViewById(R.id.display_Product_Favourite_Icon);
            mDisaplyProductUpdate = (ImageButton) parent.findViewById(R.id.display_product_Update);

            mDisplayProductName.setText(displayProductName);
            mDisplayLowPrice.setText(displayProductLowPrice);
            mDisplayProductImage.setImageBitmap(displayProductImg);

            mDisplayProductFavouriteToggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "On Clicked Toggle", Toast.LENGTH_SHORT).show();
                }
            });
            mDisplayProductAddToListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Add to list + ", Toast.LENGTH_SHORT).show();
                }
            });
            mDisaplyProductUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePriceOnClick(view);
                    Toast.makeText(getActivity(), "Update price ", Toast.LENGTH_SHORT).show();
                }
            });


        }


        public void setmDisplayProductImage(ImageView mDisplayProductImage) {
            this.mDisplayProductImage = mDisplayProductImage;
        }

        public void setClickable(boolean mDisplayProductFavouriteToggleBtn) {
            super.setClickable(mDisplayProductFavouriteToggleBtn);

        }

        public void setDisplayProductName(String displayProductName) {
            this.displayProductName = displayProductName;
        }

        public void setDisplayProductLowPrice(String displayProductLowPrice) {
            this.displayProductLowPrice = displayProductLowPrice;
        }

        public void setDisplayProductImg(Bitmap displayProductImg) {
            this.displayProductImg = displayProductImg;
        }
    }

    public class innerClass extends Card {

        protected TextView mTitle;
        protected TextView mSecondaryTitle;
        protected ImageView mImage;
        protected int resourceIdThumbnail;
        protected String title;
        protected String secondaryTitle;
        protected int img;

        public innerClass(Context context) {
            this(context, R.layout.layout_id);
        }

        public innerClass(Context context, int innerlayout) {
            super(context, innerlayout);
        }

        private void init() {

            CardThumbnail cardThumbnail = new CardThumbnail(mContext);

            cardThumbnail.setDrawableResource(resourceIdThumbnail);

            addCardThumbnail(cardThumbnail);

            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    mapDetailFragment = new MapDetailFragment();
                    settitle(title);
                    ft.replace(R.id.main_container, mapDetailFragment).addToBackStack(null).commit();
                }
            });
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);

            mTitle = (TextView) parent.findViewById(R.id.retailerTitleid);
            mSecondaryTitle = (TextView) parent.findViewById(R.id.retailerSecTitleid);
            mImage = (ImageView) parent.findViewById(R.id.retailerImgid);

            mTitle.setText(title);
            mSecondaryTitle.setText(secondaryTitle);
            mImage.setImageResource(img);
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        public String getSecondaryTitle() {
            return secondaryTitle;
        }

        public void setSecondaryTitle(String secondaryTitle) {
            this.secondaryTitle = secondaryTitle;
        }

        public int getResourceIdThumbnail() {
            return resourceIdThumbnail;
        }

        public void setResourceIdThumbnail(int resourceIdThumbnail) {
            this.resourceIdThumbnail = resourceIdThumbnail;
        }

        public int getImg() {
            return img;
        }

        public void setImg(int img) {
            this.img = img;
        }
    }

}
