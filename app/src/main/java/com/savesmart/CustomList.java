package com.savesmart;

/**
 * Created by LestonYong on 8/7/2014.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

public class CustomList extends ArrayAdapter<ParseObject> {

    List<ParseObject> productList;
    int res;
    ProductView productView;

    public CustomList(Context context,
                      int res, List<ParseObject> productList) {
        super(context, res, productList);
        this.productList = productList;
        this.res = res;

    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(res, parent, false);
        }

        TextView productName = (TextView) view.findViewById(R.id.txt);
        final ImageView productImage = (ImageView) view.findViewById(R.id.img);
        ParseFile parseFile = productList.get(position).getParseFile("productPhoto");
        productName.setText(productList.get(position).getString("productName"));

        if (parseFile != null)
            parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        productImage.setImageBitmap(bitmap);

                    }
                }
            });


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Bundle bundle = new Bundle();
//                bundle.putString("mTitle", productList.get(position).getString("productName"));
//                productView = new ProductView();
//                productView.setArguments(bundle);
//                Fragment a = new Fragment();
//                a.getFragmentManager().beginTransaction().replace(R.id.main_container, productView).addToBackStack(null).commit();

                Toast.makeText(getContext(), "Position : " + position
                        + "\n Hendler to get product Detail \n"
                        + " Name : "
                        +productList.get(position).getString("productName")
                        + "\nPass the string to next Fragment", Toast.LENGTH_LONG).show();
            }
        });


        return view;
    }
}