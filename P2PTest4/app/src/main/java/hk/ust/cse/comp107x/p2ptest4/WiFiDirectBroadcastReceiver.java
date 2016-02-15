package hk.ust.cse.comp107x.p2ptest4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alpha on 2015/10/1.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;


    private static final String TAG = "MainActivity";
    public static String mDeviceName;


    public List peers = new ArrayList();


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }


    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener(){
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {

            if (info.groupFormed && info.isGroupOwner) {

                mActivity.isOwner = true;
                //TextView stateText2 = (TextView) mActivity.findViewById(R.id.stateText2);   //KEEP IT
                //stateText2.setText("Group Owner");   //KEEP IT

            } else if (info.groupFormed) {

                mActivity.isOwner = false;
                //TextView stateText2 = (TextView) mActivity.findViewById(R.id.stateText2);  //KEEP IT
                //stateText2.setText("Group Client");   //KEEP IT
            }
        }
    };


    private WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener(){
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {

            if(group != null){

                //TextView commentText = (TextView) mActivity.findViewById(R.id.commentText);   //KEEP IT

                //commentText.setText(group.getClientList().toString());   //KEEP IT
            }
        }
    };



    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            mActivity.peersname.clear();                   //peersname is the list of names with Adapter

            mActivity.peersshow.clear();                   //peersshow is the list of OBJECTS
            mActivity.peersshow.addAll(peerList.getDeviceList());


            peers = mActivity.peersshow;

            System.out.println(peers);                   //peers simply includes the name list


            for(int i=0;i<peers.size();i++){
                WifiP2pDevice device = (WifiP2pDevice) peers.get(i);
                String deviceName=device.deviceName;

                System.out.println(deviceName);

                mActivity.peersname.add(deviceName);

            }


            System.out.println("PeersList added successful");


            mActivity.madapter.notifyDataSetChanged();

            mActivity.getTime(1);   //TEST
            mActivity.displayTime();

            if (peers.size() == 0) {
                return;
            }
        }
    };


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

            } else {

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The peer list has changed!  We should probably do something about that.

            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);

                Log.d("Toast", "P2P peers changed");
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            //System.out.println("Network Info: " + networkInfo);

            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                Log.d("Toast", "networkInfo_isConnected");
                //mActivity.finishConnectTime.setText(mActivity.getTime());
                //mActivity.displayTime();
                mActivity.stateText.setText("Connected");
                mManager.requestConnectionInfo(mChannel, connectionInfoListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mDeviceName = device.deviceName;
            //mActivity.idText.setText(mDeviceName);          KEEP IT

            mManager.requestGroupInfo(mChannel, groupInfoListener);

        }
    }

}
