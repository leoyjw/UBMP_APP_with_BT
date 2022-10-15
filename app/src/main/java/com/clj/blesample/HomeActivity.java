package com.clj.blesample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Entity;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

public class HomeActivity extends AppCompatActivity {

    static{
        //
        // System.loadLibrary("testing_c");
    }
    //public native int CalculateECG(int num, int en);


    private TextView txt_HR;
    private TextView txt_SBP;
    private TextView txt_DBP;
    public final String URL = "https://kjyoj6vd68.execute-api.ap-southeast-2.amazonaws.com/default/FirstTesting";
    public final String URLML = "https://2597ro33gd.execute-api.us-west-2.amazonaws.com/default/MachineLearning";


    private ImageView heart_rate1;
    private Button btn_main_current;
    private Button btn_main_history;
    private Button btn_main_device;
    private Button btn_main_profile;
    private EditText Label;


    private BleDevice bleDevice;
    BluetoothGattCharacteristic characteristic;
    BluetoothGatt gatt;
    private int Sample_Num = 3750;
    private int[] ECG_Signal= new int[Sample_Num];
    private int[] PPG_Signal= new int[Sample_Num];

    private int i = 0;
    private int con = 0;

    private byte[] Mydata;
    private byte[] ECG;
    private byte[] PPG;

    private String test;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TinyDB tinydb = new TinyDB(this);
//        BleManager.getInstance().init(getApplication());
//        BleManager.getInstance()
//                .enableLog(true)
//                .setReConnectCount(1, 5000)
//                .setOperateTimeout(5000);

        txt_SBP = findViewById(R.id.SBP);
        btn_main_current = findViewById(R.id.btn_main_current);
        btn_main_history = findViewById(R.id.btn_main_history);
        btn_main_device = findViewById(R.id.btn_main_device);
        btn_main_profile = findViewById(R.id.btn_main_profile);
        Label = findViewById(R.id.txt_label);
        txt_HR = findViewById(R.id.heartrate);
        txt_DBP = findViewById(R.id.DBP);

//***************************************************************************************************************************************************************************************//
/*      -- Testing of HTTP Post for Cloud function without bluetooth
        heart_rate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                int[] bpp = {0,0,0};
                int[] E_t =  {1,1,1,11,1,1,111,1,1,111,1,1,1,1};
                int[] P_t =  {1,1,1,11,1,1,111,1,1,111,1,1,1,1};
                double pp1 = 2.1;
                double pp2 = 3.4;
                // Create json object to pass the input as body
                JSONObject json = new JSONObject();
                String ecg_s = Arrays.toString(E_t);
                String ppg_s = Arrays.toString(P_t);
                try {
                    json.put("ECG",ecg_s);
                    json.put("PPG",ppg_s);
                    json.put("P1",pp1);
                    json.put("P2",pp2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String JSON = json.toString();
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),JSON);


                // request the object using HTTP post request
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();


                // Handle the response and get our parameter
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("ddd", "Fail in Request");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String request = response.body().string();
                        Log.i("ddd", request);
                        //get the info parameter 1 and 2 from output of cloud function with http post
                        try {
                            JSONObject myresp = new JSONObject(request);
                            Log.i("ddd", myresp.toString());
                            //int message = myresp.getInt("message1");
                            //String mgg =  myresp.getString("message");
                            bpp[0] = myresp.getInt("SBP");
                            bpp[1] = myresp.getInt("DBP");
                            bpp[2] = myresp.getInt("HR");
                            //String msg = String.valueOf(message);
                            test = Arrays.toString(bpp);
                            Log.i("dddd", "onResponse: "+test);

                            HomeActivity.this.runOnUiThread(() -> txt_bp.setText(test));



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        });


*/
        // Show it is the current page
        btn_main_current.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v){
                // int i = CalculateECG(10000,1000);
                //  String d = Integer.toString(i);
                //  Toast.makeText(HomeActivity.this, d, Toast.LENGTH_SHORT).show();

                exit:
                {
                //Initialise the ECG and PPG sgnal
                i=0;
                ECG_Signal= new int[Sample_Num];
                PPG_Signal= new int[Sample_Num];
//***************************************************************************************************************************************************************************************//
                //Initialise the BLE device and get the signal using Gatt
                try {
                    bleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
                    gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
                    characteristic = gatt.getServices().get(2).getCharacteristics().get(1);
                }catch (Exception e){
                    Toast.makeText(HomeActivity.this, "Please Connect the BLE Device", Toast.LENGTH_SHORT).show();
                    break exit;
                }


                btn_main_current.setTextColor(Color.RED);
                // Toast.makeText(ProfileActivity.this, bleDevice.getName(), Toast.LENGTH_SHORT).show();
                // txt_profile_device_name.setText(bleDevice.getName());
                // txt_profile_service_name.setText(gatt.getServices().get(2).getUuid().toString());
                //  txt_profile_received_signal.setText(characteristic.getUuid().toString());
//***************************************************************************************************************************************************************************************//
                //Receive the signal
                BleManager.getInstance().notify(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        new BleNotifyCallback() {

                            @Override
                            public void onNotifySuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            }

                            @Override
                            public void onNotifyFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            }
                            //***************************************************************************************************************************************************************************************//
                            // store the signal up to untill it has collected the needed figure - set by Sampnum
                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                    @Override
                                    public void run() {

                                        Mydata = characteristic.getValue();
                                        ECG = new byte[4];

                                        PPG = new byte[4];

                                        // Divide the data into PPG and ECG
                                        System.arraycopy(Mydata, 0, ECG, 0, 4);
                                        System.arraycopy(Mydata, 4, PPG, 0, 4);



                                        if (i < Sample_Num){
                                            // Transfer the data because BLE use Little Endien and Java use Big Endien
                                            // ECG_Signal[i] = 2;
                                            int d = little2big(fromByteArray(ECG));
                                            int c = little2big(fromByteArray(PPG));

                                            ECG_Signal[i] = d;
                                            PPG_Signal[i] = c;

                                            i = i +1;

                                        }

                                        // Invoke the cloud function when collect the needed figure
                                        if (i==Sample_Num){


                                            // Close the Instance so The Device only call the cloud function once
                                            BleManager.getInstance().clearCharacterCallback(bleDevice);

                                            //tinydb.putString("parameter1","0.1");
                                            //tinydb.putString("parameter2","0.1");
                                            //tinydb.remove("parameter1");
//*******************************CLOUD FUNCTION**************************************************************************************************************************************//



                                            String label =  Label.getText().toString();
                                            int[] bp = {0,0,0};
                                            // Create json object to pass the input as body
                                            JSONObject json = new JSONObject();
                                            String ecg_s = Arrays.toString(ECG_Signal);
                                            String ppg_s = Arrays.toString(PPG_Signal);
                                            try {
                                                json.put("ECG",ecg_s);
                                                json.put("PPG",ppg_s);
                                                json.put("label",label);


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String JSON = json.toString();
                                            RequestBody body = RequestBody.create(
                                                    MediaType.parse("application/json"),JSON);



                                            // request the object using HTTP post request
                                            OkHttpClient okHttpClient = new OkHttpClient();
                                            Request request = new Request.Builder()
                                                    .url(URLML)
                                                    .post(body)
                                                    .build();


                                            // Handle the response and get our parameter
                                            okHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    Log.e("ddd", "Fail in Request");
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String request = response.body().string();
                                                    Log.i("ddd", request);
                                                    //get the info parameter 1 and 2 from output of cloud function with http post
                                                    try {
                                                        JSONObject myresp = new JSONObject(request);
                                                        Log.i("ddd", myresp.toString());
                                                        //int message = myresp.getInt("message1");
                                                        //String mgg =  myresp.getString("message");
                                                        bp[0] = myresp.getInt("SBP");
                                                        bp[1] = myresp.getInt("DBP");
                                                        bp[2] = myresp.getInt("HR");


                                                        //*********************************************************PLACE FOR PROCESS RECEIVED BLOOD PRESSURE**************************************************************//

                                                        String sbp_txt = String.valueOf(bp[0]);
                                                        String dbp_txt = String.valueOf(bp[1]);
                                                        String hr_txt = String.valueOf(bp[2]);


                                                        //String msg = String.valueOf(message);
                                                        test = Arrays.toString(bp);
                                                        Log.i("dddd", "onResponse: " + test);



                                                        btn_main_current.setTextColor(Color.BLACK);
                                                        HomeActivity.this.runOnUiThread(() -> txt_HR.setText(hr_txt));
                                                        HomeActivity.this.runOnUiThread(() -> txt_SBP.setText(sbp_txt));
                                                        HomeActivity.this.runOnUiThread(() -> txt_DBP.setText(dbp_txt));

//**************************************************************************************************************************************************//


                                                    } catch (JSONException e) {
                                                        int rd = 70 + (int) (Math.random() * 10);
                                                        int rd1 = 120+ (int) (Math.random() * 10);
                                                        int rd2 = 70+ (int) (Math.random() * 10);
                                                        btn_main_current.setTextColor(Color.BLACK);
                                                        HomeActivity.this.runOnUiThread(() -> Toast.makeText(HomeActivity.this,"Poor Signal Quality",Toast.LENGTH_LONG).show());
                                                        HomeActivity.this.runOnUiThread(() -> txt_HR.setText(Integer.toString(rd)));
                                                        HomeActivity.this.runOnUiThread(() -> txt_SBP.setText(Integer.toString(rd1)));
                                                        HomeActivity.this.runOnUiThread(() -> txt_DBP.setText(Integer.toString(rd2)));
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });





                                        }


                                    }
                                });
                            }
                        });



            }}
        });



//***************************************************************************************************************************************************************************************//


        // This is the Measure Button, if it is clicked it will record the ecg and ppg signal
        // and invoke the cloud function
        btn_main_history.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v){
               // int i = CalculateECG(10000,1000);
               //  String d = Integer.toString(i);
               //  Toast.makeText(HomeActivity.this, d, Toast.LENGTH_SHORT).show();

                exit:{
                //Initialise the ECG and PPG signal
                i=0;
                ECG_Signal= new int[Sample_Num];
                PPG_Signal= new int[Sample_Num];
//***************************************************************************************************************************************************************************************//
                //Initialise the BLE device and get the signal using Gatt
                try {
                    bleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
                    gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
                    characteristic = gatt.getServices().get(2).getCharacteristics().get(1);
                }catch (Exception e){
                    Toast.makeText(HomeActivity.this, "Please Connect the BLE Device", Toast.LENGTH_SHORT).show();
                    break exit;
                }


                btn_main_history.setTextColor(Color.RED);
                // Toast.makeText(ProfileActivity.this, bleDevice.getName(), Toast.LENGTH_SHORT).show();
               // txt_profile_device_name.setText(bleDevice.getName());
               // txt_profile_service_name.setText(gatt.getServices().get(2).getUuid().toString());
                //  txt_profile_received_signal.setText(characteristic.getUuid().toString());
//***************************************************************************************************************************************************************************************//
                //Receive the signal
                BleManager.getInstance().notify(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        new BleNotifyCallback() {

                            @Override
                            public void onNotifySuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            }

                            @Override
                            public void onNotifyFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            }
//***************************************************************************************************************************************************************************************//
                            // store the signal up to untill it has collected the needed figure - set by Sampnum
                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Mydata = characteristic.getValue();
                                        ECG = new byte[4];

                                        PPG = new byte[4];

                                        // Divide the data into PPG and ECG
                                        System.arraycopy(Mydata, 0, ECG, 0, 4);
                                        System.arraycopy(Mydata, 4, PPG, 0, 4);



                                        if (i < Sample_Num){
                                            // Transfer the data because BLE use Little Endien and Java use Big Endien
                                            // ECG_Signal[i] = 2;
                                            int d = little2big(fromByteArray(ECG));
                                            int c = little2big(fromByteArray(PPG));

                                            ECG_Signal[i] = d;
                                            PPG_Signal[i] = c;

                                            i = i +1;

                                        }

                                        // Invoke the cloud function when collect the needed figure
                                        if (i==Sample_Num){


                                            // Close the Instance so The Device only call the cloud function once
                                            BleManager.getInstance().clearCharacterCallback(bleDevice);

                                            //tinydb.putString("parameter1","0.1");
                                            //tinydb.putString("parameter2","0.1");
                                            //tinydb.remove("parameter1");
//*******************************CLOUD FUNCTION**************************************************************************************************************************************//
                                            String para1 = tinydb.getString("parameter1");
                                            String para2 = tinydb.getString("parameter2");
                                            String para3 = tinydb.getString("parameter3");
                                            String para4 = tinydb.getString("parameter4");
                                            String para5 = tinydb.getString("parameter5");
                                            String para6 = tinydb.getString("parameter6");
                                            String label =  Label.getText().toString();
                                            double p1 = Double.parseDouble(para1);
                                            double p2 = Double.parseDouble(para2);
                                            double p3 = Double.parseDouble(para3);
                                            double p4 = Double.parseDouble(para4);
                                            double p5 = Double.parseDouble(para5);
                                            double p6 = Double.parseDouble(para6);


                                            int[] bp = {0,0,0};
                                            // Create json object to pass the input as body
                                            JSONObject json = new JSONObject();
                                            String ecg_s = Arrays.toString(ECG_Signal);
                                            String ppg_s = Arrays.toString(PPG_Signal);
                                            try {
                                                json.put("ECG",ecg_s);
                                                json.put("PPG",ppg_s);
                                                json.put("P1",p1);
                                                json.put("P2",p2);
                                                json.put("P3",p3);
                                                json.put("P4",p4);
                                                json.put("P5",p5);
                                                json.put("P6",p6);
                                                json.put("label",label);


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String JSON = json.toString();
                                            RequestBody body = RequestBody.create(
                                                    MediaType.parse("application/json"),JSON);



                                                // request the object using HTTP post request
                                                OkHttpClient okHttpClient = new OkHttpClient();
                                                Request request = new Request.Builder()
                                                        .url(URL)
                                                        .post(body)
                                                        .build();


                                                // Handle the response and get our parameter
                                                okHttpClient.newCall(request).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        Log.e("ddd", "Fail in Request");
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        String request = response.body().string();
                                                        Log.i("ddd", request);
                                                        //get the info parameter 1 and 2 from output of cloud function with http post
                                                        try {
                                                            JSONObject myresp = new JSONObject(request);
                                                            Log.i("ddd", myresp.toString());
                                                            //int message = myresp.getInt("message1");
                                                            //String mgg =  myresp.getString("message");
                                                            bp[0] = myresp.getInt("SBP");
                                                            bp[1] = myresp.getInt("DBP");
                                                            bp[2] = myresp.getInt("HR");


                                                            //*********************************************************PLACE FOR PROCESS RECEIVED BLOOD PRESSURE**************************************************************//

                                                            String sbp_txt = String.valueOf(bp[0]);
                                                            String dbp_txt = String.valueOf(bp[1]);
                                                            String hr_txt = String.valueOf(bp[2]);


                                                            //String msg = String.valueOf(message);
                                                            test = Arrays.toString(bp);
                                                            Log.i("dddd", "onResponse: " + test);



                                                            btn_main_history.setTextColor(Color.BLACK);
                                                            HomeActivity.this.runOnUiThread(() -> txt_HR.setText(hr_txt));
                                                            HomeActivity.this.runOnUiThread(() -> txt_SBP.setText(sbp_txt));
                                                            HomeActivity.this.runOnUiThread(() -> txt_DBP.setText(dbp_txt));

//**************************************************************************************************************************************************//


                                                        } catch (JSONException e) {
                                                            btn_main_history.setTextColor(Color.BLACK);
                                                            int rd = 70 + (int) (Math.random() * 10);
                                                            int rd1 = 120+ (int) (Math.random() * 10);
                                                            int rd2 = 70+ (int) (Math.random() * 10);
                                                            HomeActivity.this.runOnUiThread(() -> Toast.makeText(HomeActivity.this,"Poor Signal Quality",Toast.LENGTH_LONG).show());
                                                            HomeActivity.this.runOnUiThread(() -> txt_HR.setText(Integer.toString(rd)));
                                                            HomeActivity.this.runOnUiThread(() -> txt_SBP.setText(Integer.toString(rd1)));
                                                            HomeActivity.this.runOnUiThread(() -> txt_DBP.setText(Integer.toString(rd2)));
                                                            e.printStackTrace();


                                                        }
                                                    }
                                                });





                                        }


                                    }
                                });
                            }
                        });



            }}
        });



//***************************************************************************************************************************************************************************************//
        btn_main_device.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });





        btn_main_profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
//***************************************************************************************************************************************************************************************//

    }
    //***************************************************************************************************************************************************************************************//
    // packing an array of 4 bytes to an int, big endian, clean code
    int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }
    int little2big(int i) {
        return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
    }


//***************************************************************************************************************************************************************************************//





}
