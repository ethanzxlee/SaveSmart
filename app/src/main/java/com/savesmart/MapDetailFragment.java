package com.savesmart;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;


/**
 * Created by LestonYong on 11/7/2014.
 */
public class MapDetailFragment extends Fragment {

    //private static final String LOG_TAG = "ExampleApp";

    //private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    //private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    //private static final String OUT_JSON = "/json";

    //private static final String API_KEY = "AIzaSyAfmYVUlva-bXAzYPlE4QWZN6Rr5-a4cNM";
    GoogleMap newMap;
    CardView cardView;
    CardHeader header;
    Card card ;
    Fragment MapFragment;
    ProductView productView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        productView = new ProductView();

        cardView = (CardView)(getActivity().findViewById(R.id.myCard));
        setEmptyCard();
        getData(productView.gTitle.toLowerCase());

    }

    @Override
    public void onDestroy() {
       getFragmentManager().beginTransaction().remove(MapFragment).commit();
        super.onDestroy();

    }

    public void getData(String retailerName){
        final ParseQuery<ParseObject> retailerObject = ParseQuery.getQuery("Retailer");
        retailerObject.whereEqualTo("retailerName", retailerName);
        retailerObject.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                double Lat = 0, Log = 0;

                if(e == null){
                    MapFragment = (getFragmentManager().findFragmentById(R.id.map));
                    newMap = ((MapFragment) MapFragment).getMap();

                   for(int i = 0 ; i < parseObjects.size() ; i++){
                       Lat = parseObjects.get(i).getParseGeoPoint("coordinates").getLatitude() + Lat;
                       Log = parseObjects.get(i).getParseGeoPoint("coordinates").getLongitude() + Log;

                       newMap.setMyLocationEnabled(true);
                       newMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat/(parseObjects.size()),
                                       Log/(parseObjects.size())), 10));

                       newMap.addMarker(new MarkerOptions()
                               .title(parseObjects.get(i).get("shoppingMallName").toString())
                               .snippet(parseObjects.get(i).get("retailerAddress").toString())
                               .position(new LatLng(
                                       parseObjects.get(i).getParseGeoPoint("coordinates").getLatitude(),
                                       parseObjects.get(i).getParseGeoPoint("coordinates").getLongitude())).
                                       alpha(0.8f));
                   }
                } else
                    Toast.makeText(getActivity(), "Get Error", Toast.LENGTH_SHORT).show();


                newMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (!marker.equals(newMap)) {
                            cardUI(marker);
                        }
                        return false;

                    }
                });
            }
        });
    }
    public void setEmptyCard (){

        header  = new CardHeader(getActivity().getBaseContext());
        card = new Card(getActivity().getBaseContext());
        header.setTitle("Select marker to get address");
        card.addCardHeader(header);
        card.setTitle("");
        cardView.setCard(card);
    }
    public void cardUI (Marker marker){

        header.setTitle(marker.getTitle());
        card.setTitle(marker.getSnippet());
        card.addCardHeader(header);
        cardView.refreshCard(card);
    }
}


