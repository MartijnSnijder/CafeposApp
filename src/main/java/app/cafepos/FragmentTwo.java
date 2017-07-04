package app.cafepos;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import app.cafepos.R;

/**
 * Created by Martijn on 22-6-2017.
 */

public class FragmentTwo extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ListView lv2;
    private String TAG = MainActivity.class.getSimpleName();

    private static String url = "http://msnijder.nl/php/get_all_products.php";

    private ProgressDialog pDialog;

    private static ArrayList drinksList = new ArrayList<>();
    private static ArrayList foodList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    public FragmentTwo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SwipeRefreshLayout view = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_two, container, false);
        lv2 = (ListView) view.findViewById(R.id.list2);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout2);
        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //
                                    }
                                }
        );
        new GetProducts2().execute();
        return view;
    }

    @Override
    public void onRefresh() {
        foodList.clear();
        new GetProducts2().execute();
    }

    private class GetProducts2 extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Even geduld...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Respons van URL: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONArray products = jsonObj.getJSONArray("producten");

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        String id = c.getString("id");
                        String name = c.getString("naam");
                        String price = "€" + String.format("%.2f", (Double.parseDouble(c.getString("prijs"))/100));
                        String sub = c.getString("subcategorieen_id");

                        if (sub.equals("2") || sub.equals("3") || sub.equals("4")){
                            HashMap<String, String> food = new HashMap<>();
                            food.put("id", id);
                            food.put("name", name);
                            food.put("price", price);
                            food.put("type", "eten");

                            foodList.add(food);
                        }
                        else
                        {
                            HashMap<String, String> drink = new HashMap<>();
                            drink.put("id", id);
                            drink.put("name", name);
                            drink.put("price", price);
                            drink.put("type", "drinken");

                            drinksList.add(drink);
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Kon JSON niet van de server halen");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Kon de data niet ophalen, controleer je internet connectie",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            swipeRefreshLayout.setRefreshing(false);

            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), foodList,
                    R.layout.list_item, new String[]{"name", "price"}, new int[]{R.id.name,
                    R.id.price});

            lv2.setAdapter(adapter);
        }
    }
}