package hk.ust.cse.comp107x.p2ptest4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    private final IntentFilter mIntentFilter = new IntentFilter();



    private Button connectButton;
    private Button searchButton;
    private ToggleButton enableToggleButton;
    //private Button disConnectButton;

    /*
    public TextView startConnectTime;
    public TextView finishConnectTime;
    public TextView needConnectTime;
    */

    public TextView stateText;
    private TextView connectTimeText;


    List peersshow = new ArrayList();

    ArrayList<String> peersname = new ArrayList<String>(){};

    ArrayAdapter<String> madapter;

    public static String startTime;
    public static String endTime;

    //public static boolean enablenum = false;
    public static int peerpick = 0;

    String myDeviceName;
    public static boolean isOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);


        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        connectButton = (Button) findViewById(R.id.connectButton);
        stateText = (TextView) findViewById(R.id.stateText);
        searchButton = (Button) findViewById(R.id.searchButton);
        enableToggleButton = (ToggleButton) findViewById(R.id.enableToggleButton);
        connectTimeText = (TextView) findViewById(R.id.connectTimeText);

        /*
        disConnectButton = (Button) findViewById(R.id.disConnectButton);
        startConnectTime = (TextView) findViewById(R.id.start_connect_time);
        finishConnectTime = (TextView) findViewById(R.id.finish_connect_time);
        needConnectTime = (TextView) findViewById(R.id.need_connect_time);
        */




        searchButton.setOnClickListener(searchButtonClick);
        connectButton.setOnClickListener(connectButtonClick);
        enableToggleButton.setOnClickListener(enableToggleButtonClick);
        //disConnectButton.setOnClickListener(disConnectButtonClick);


        madapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peersname);
        ListView peersListView = (ListView) findViewById(R.id.peersListView);
        peersListView.setAdapter(madapter);


        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                peerpick = position;
                stateText.setText(peersname.get(peerpick));

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//==================================================================================================//

    View.OnClickListener searchButtonClick = new View.OnClickListener() {   //----------SearchButton

        public void onClick(View v){
            //startConnectTime.setText(getTime());
            getTime(0);
            Log.d("Toast", "Searching~");
            stateText.setText("Search~");

            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.d("Toast", Integer.toString(reasonCode));

                }
            });
        }
    };


    public void connect(){  //---------------------------------------------------------------CONNECT


        stateText.setText("Starting connection with: " + peersname.get(peerpick));

        WifiP2pDevice device = (WifiP2pDevice) peersshow.get(peerpick);   //modified
        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = device.deviceAddress;
        final String deviceName = device.deviceName;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.d("Toast", "Connection Init Successful!~");

                stateText.setText("Connected with: " + deviceName);
                //finishConnectTime.setText(getTime());
                getTime(1);

                displayTime();
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Toast", "Connect failed. Retry.");
            }
        });
    }


    //@Override
    public void disconnect() {  //--------------------------------------------------------DISCONNECT

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d("Toast", "Disconnect failed. Reason :" + reasonCode);
                //finishConnectTime.setText(getTime());

                //displayTime();
            }

            @Override
            public void onSuccess() {
                Log.d("Toast", "Disconnect Success");
                stateText.setText("Hello world!");
                //finishConnectTime.setText(getTime());

                //displayTime();
            }

        });
    }


    public View.OnClickListener connectButtonClick = new View.OnClickListener() {  //-----connectButton

        public void onClick(View v){
            //startConnectTime.setText(getTime());
            getTime(0);
            Log.d("Toast", "Connecting~");
            //Toast.makeText(getBaseContext(), "Connecting~", Toast.LENGTH_SHORT).show();
            stateText.setText("Connection Init");

            try {

                connect();

            } catch (Exception ex) {
                Log.d("Toast", "Connection Failed, PLZ try again");

            }

        }
    };

    /*

    View.OnClickListener disConnectButtonClick = new View.OnClickListener() {   //-----disConnectButton

        public void onClick(View v){
            startConnectTime.setText(getTime());
            Log.d("Toast", "disConnecting~");
            stateText.setText("DisConnect~");

            disconnect();
        }
    };

    */

    View.OnClickListener enableToggleButtonClick = new View.OnClickListener() {   //----enableToggleButton

        public void onClick(View v){
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            if (enableToggleButton.isChecked()) {
                wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
            }
            else {
                wifi.setWifiEnabled(false); // true or false to activate/deactivate wifi
            }
        }
    };

    /*
    public void getStartTime(){
        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        startTime = second * 1000 + millis;
    }

    public void getEndTime(){
        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);
        endTime = second * 1000 + millis;
    }
    */


    public void displayTime(){
        float foo;
        //foo = Float.parseFloat(finishConnectTime.getText().toString()) - Float.parseFloat(startConnectTime.getText().toString());
        foo = Float.parseFloat(endTime) - Float.parseFloat(startTime);

        if (foo < 0){
            foo += 60;
        }

        connectTimeText.setText(Float.toString(foo));
    }

    public void getTime(int foo){

        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        String timer = (Integer.toString(second) + "." + Integer.toString(millis));

        Log.d("Toast", "Get Time: " + timer);

        if(foo == 0){
            startTime = timer;
        }
        else if(foo == 1){
            endTime = timer;
        }

    }


}
