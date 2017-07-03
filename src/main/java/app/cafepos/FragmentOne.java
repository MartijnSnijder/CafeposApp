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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Martijn on 22-6-2017.
 */

public class FragmentOne extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String TAG = MainActivity.class.getSimpleName();

    private static String url = "http://msnijder.nl/php/get_all_products.php";

    ListView lv;
    private ProgressDialog pDialog;

    private static ArrayList drinksList = new ArrayList<>();
    private static ArrayList foodList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    public FragmentOne() {
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
        SwipeRefreshLayout view = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_one, container, false);
        lv = (ListView) view.findViewById(R.id.list);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //
                                    }
                                }
        );
        new GetProducts().execute();
        return view;
    }

    @Override
    public void onRefresh() {
        drinksList.clear();
        new GetProducts().execute();
    }

    private class GetProducts extends AsyncTask<Void, Void, Void> {

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

                    JSONArray products = jsonObj.getJSONArray("products");

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        String id = c.getString("id");
                        String name = c.getString("naam");
                        String price = "€" + String.format("%.2f", (Double.parseDouble(c.getString("prijs"))/100));
                        String type = c.getString("type");

                        if (type.equals("drinken")){
                            HashMap<String, String> drink = new HashMap<>();
                            drink.put("id", id);
                            drink.put("name", name);
                            drink.put("price", price);

                            drinksList.add(drink);
                        }
                        else
                        {
                            HashMap<String, String> food = new HashMap<>();
                            food.put("id", id);
                            food.put("name", name);
                            food.put("price", price);

                            foodList.add(food);
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
                                "Kon JSON niet van de server halen, check logcat voor mogelijk errors",
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

            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);

            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), drinksList,
                    R.layout.list_item, new String[]{"name", "price"}, new int[]{R.id.name,
                    R.id.price});

            lv.setAdapter(adapter);
        }
    }

    public ArrayList getFoodList(){
        return foodList;
    }
}
