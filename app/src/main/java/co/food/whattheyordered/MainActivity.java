package co.food.whattheyordered;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.order.OrderContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private Account account;
    private OrderConnector orderConnector;
    private Order order;
    private static ArrayList<Order> mOrders = new ArrayList<Order>();
    private static OrderAdapter mAdapter=null;



    private ArrayList<String> mDishes = new ArrayList<String>();
    private Context mContext;
    ListView dishlistview;
    DishAdapter dishAdapter;

//    private TextView orderId;
//    private TextView lineItemCount;
//    private TextView total;
//    private TextView createTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;



        ListView listview = (ListView) findViewById(R.id.tablelist);


        MainActivity.mAdapter = new OrderAdapter(this, mOrders);
        listview.setAdapter(MainActivity.mAdapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Intent intent = new Intent(getBaseContext(), TableActivity.class);
                intent.putExtra("order", MainActivity.mOrders.get(position));
                startActivity(intent);
            }
        });


        dishlistview = (ListView) findViewById(R.id.dishlist);


        dishlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                    Toast.makeText(mContext, "Ordered " + mDishes.get(position), Toast.LENGTH_LONG).show();
                }
        });


        DishAdapter dishAdapter = new DishAdapter(this, mDishes);
        dishlistview.setAdapter(dishAdapter);

    }


    void updateOrder(Order order) {
        try {
            JSONObject obj = (JSONObject) order.getJSONObject().get("lineItems");
            JSONArray obj1 = (JSONArray) obj.get("elements");
            int size = (int) obj1.length();
            for (int i=0 ; i<size ; i++ ) {
                JSONObject aaa = (JSONObject) obj1.get(i);
                String bbb = (String) aaa.get("name");
                mDishes.add(bbb);
            }

            dishAdapter.notifyDataSetChanged();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(this);

            if (account == null) {
                Toast.makeText(this, "no account found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Create and Connect to the OrderConnector
        connect();

        // Load the last order or create a new order
        loadLastOrder();
    }

    @Override
    protected void onPause() {
        disconnect();
        super.onPause();
    }

    private void connect() {
        disconnect();
        if (account != null) {
            orderConnector = new OrderConnector(this, account, null);
            orderConnector.connect();
        }
    }

    private void disconnect() {
        if (orderConnector != null) {
            orderConnector.disconnect();
            orderConnector = null;
        }
    }

    private void loadLastOrder() {
        new OrderAsyncTask().execute();
    }


    public class DishAdapter extends ArrayAdapter<String> {
        Context mContext;

        public DishAdapter(Context context, ArrayList<String> values) {
            super(context, R.layout.dish_cell, values);
            mContext = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                convertView = inflater.inflate(R.layout.dish_cell, parent, false);

            String name = mDishes.get(position);

            TextView nameview = (TextView) convertView.findViewById(R.id.dishname);
            nameview.setText(mDishes.get(position));
            ImageView img = (ImageView) convertView.findViewById(R.id.dishimage);
//            img.setImageResource(R.drawable.dish1);



            String mDrawableName = "dish" + position;
            int resID = getResources().getIdentifier(mDrawableName , "drawable", getPackageName());
            img.setImageResource(resID);
//            Bitmap bmImg = BitmapFactory.decodeFile("file:///android_res/drawable/dish" + position + ".jpeg");
//            img.setImageBitmap(bmImg);

            return convertView;
        }
    }

    public class OrderAdapter extends ArrayAdapter<Order> {
        Context mContext;

        public OrderAdapter(Context context, ArrayList<Order> values) {
            super(context, R.layout.order_cell, values);
            mContext = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                convertView = inflater.inflate(R.layout.order_cell, parent, false);

            Order order = MainActivity.mOrders.get(position);

            TextView nameview = (TextView) convertView.findViewById(R.id.ordername);
            nameview.setText( "Table " + position );

            return convertView;
        }
    }

    private class OrderAsyncTask extends AsyncTask<Void, Void, ArrayList<Order>> {

        @Override
        protected final ArrayList<Order> doInBackground(Void... params) {
            String orderId = null;
            Cursor cursor = null;
            ArrayList<Order> orders = new ArrayList<Order>();
            try {
                // Query the last order
                cursor = MainActivity.this.getContentResolver().query(OrderContract.Summaries.contentUriWithAccount(account), null, null, null, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    orderId = cursor.getString(cursor.getColumnIndex(OrderContract.Summaries.ID));
//                }

//                if (cursor != null)
//                    cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    orderId = cursor.getString(cursor.getColumnIndex(OrderContract.Summaries.ID));
                    if (orderId == null) {
                        order = orderConnector.createOrder(new Order());
                    } else {
                        order = orderConnector.getOrder(orderId);
                        //            orderId.setText(order.getId());

                        orders.add(order);
//                        int lineItemSize = 0;
//
//                        if (order.getLineItems() != null) {
//                            lineItemSize = order.getLineItems().size();
//                        }
                        //            lineItemCount.setText(Integer.toString(lineItemSize));
                        //            total.setText(BigDecimal.valueOf(order.getTotal()).divide(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        //            createTime.setText(new Date(order.getCreatedTime()).toString());

                    }

//                    cursor.moveToNext();
                }


//                if (orderId == null) {
//                    return orderConnector.createOrder(new Order());
//                } else {
//                    return orderConnector.getOrder(orderId);
//                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return orders;
        }

        @Override
        protected final void onPostExecute(ArrayList<Order> orders) {
            // Populate the UI
//            orderId.setText(order.getId());

            MainActivity.mOrders.clear();
            MainActivity.mOrders.addAll(orders);
            MainActivity.mAdapter.notifyDataSetChanged();

            int lineItemSize = 0;


//            if (order.getLineItems() != null) {
//                lineItemSize = order.getLineItems().size();
//            }
//            lineItemCount.setText(Integer.toString(lineItemSize));
//            total.setText(BigDecimal.valueOf(order.getTotal()).divide(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
//            createTime.setText(new Date(order.getCreatedTime()).toString());
        }
    }

}
