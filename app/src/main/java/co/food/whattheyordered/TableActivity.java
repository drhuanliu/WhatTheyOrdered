package co.food.whattheyordered;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.v3.order.Order;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huan on 9/20/15.
 */
public class TableActivity extends Activity {

    ArrayList<String> mDishes = new ArrayList<String>();
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        Order order = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            order = (Order) extras.get("order");
        }

        mContext = this;

        try {
            JSONObject obj = (JSONObject) order.getJSONObject().get("lineItems");
            JSONArray obj1 = (JSONArray) obj.get("elements");
            int size = (int) obj1.length();
            for (int i=0 ; i<size ; i++ ) {
                JSONObject aaa = (JSONObject) obj1.get(i);
                String bbb = (String) aaa.get("name");
                mDishes.add(bbb);
            }

            ListView listview = (ListView) findViewById(R.id.dishlist);


            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                    Toast.makeText(mContext, "Ordered " + mDishes.get(position), Toast.LENGTH_LONG).show();
                }
            });


            DishAdapter adapter = new DishAdapter(this, mDishes);
            listview.setAdapter(adapter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
}
