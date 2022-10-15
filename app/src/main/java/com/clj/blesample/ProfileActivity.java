package com.clj.blesample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentHostCallback;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.operation.OperationActivity;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
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

import org.w3c.dom.Text;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.stream.Stream;

public class ProfileActivity extends AppCompatActivity {

    static{
        //System.loadLibrary("testing_c");
    }

    public native int CalculateECG(int num, int en);
    //*****************************************Initialising***************************************************************************************************************************************//
    private Button btn_main_current;
    private Button btn_main_device;
    private Button RequestRecalibration;
    private Button btn_ML_calibration;
    private TextView txt_profile_name_ans;
    private EditText txt_current_bp_ans;
    private EditText txt_current_hr_ans;
    private Button RequestFirstCalibration;
    private Button RequestMeasure;


    private TextView txt_profile_received_signal;

    private BleDevice bleDevice;
    BluetoothGattCharacteristic characteristic;
    BluetoothGatt gatt;

    //determine the sampling number of the ECG and PPG using for calculation
    private int Sample_Num = 300;
    private int[] ECG_Signal= new int[Sample_Num];
    private int[] PPG_Signal= new int[Sample_Num];
    private int i = 0;
    private byte[] Mydata;
    private byte[] ECG;
    private byte[] PPG;

    // the URL of the CLoud function
    public final String URL = "https://kjyoj6vd68.execute-api.ap-southeast-2.amazonaws.com/default/FunctionCalibration";
    public final String URL1 = "https://kjyoj6vd68.execute-api.ap-southeast-2.amazonaws.com/default/CalibrationV2";
    public final String URL2 = "https://2597ro33gd.execute-api.us-west-2.amazonaws.com/default/Training";

//***************************************************************************************************************************************************************************************//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Create a shared reference to store the parameter
        TinyDB tinydb = new TinyDB(this);

//***************************************************************************************************************************************************************************************//
        btn_main_current = findViewById(R.id.btn_main_current);
        btn_main_device = findViewById(R.id.btn_main_device);
        RequestRecalibration = findViewById(R.id.btn_profile_calibration);
        txt_profile_name_ans = findViewById(R.id.txt_profile_name_ans);
        txt_current_bp_ans = findViewById(R.id.txt_current_bp_ans);
        txt_current_hr_ans = findViewById(R.id.txt_current_hr_ans);
        //txt_profile_received_signal = findViewById(R.id.txt_profile_received_signal);
        RequestFirstCalibration = findViewById(R.id.btn_profile_cali);
        RequestMeasure = findViewById(R.id.btn_profile_measure);
        btn_ML_calibration = findViewById(R.id.btn_ml_calibration);
//***************************************************************************************************************************************************************************************//
        // The information box of at the Profile page




        // bottom buttons
        btn_main_current.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btn_main_device.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        RequestRecalibration.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v){
                // int i = CalculateECG(10000,1000);
                //  String d = Integer.toString(i);
                //  Toast.makeText(HomeActivity.this, d, Toast.LENGTH_SHORT).show();

                exit:{
                int Sample_Num = 6000;
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
                    Toast.makeText(ProfileActivity.this, "Please Connect the BLE Device", Toast.LENGTH_SHORT).show();
                    break exit;
                }


                RequestRecalibration.setTextColor(Color.RED);
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
                                    @RequiresApi(api = Build.VERSION_CODES.N)
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
                                        if (i==Sample_Num) {

                                            BleManager.getInstance().clearCharacterCallback(bleDevice);
                                            String SBP = txt_current_bp_ans.getText().toString();
                                            String DBP = txt_current_hr_ans.getText().toString();




                                            String para1 = tinydb.getString("parameter1");
                                            String para2 = tinydb.getString("parameter2");
                                            String para3 = tinydb.getString("parameter3");
                                            String para4 = tinydb.getString("parameter4");
                                            String para5 = tinydb.getString("parameter5");
                                            String para6 = tinydb.getString("parameter6");
                                            String Pvalue = tinydb.getString("pvalue");
                                            double p1 = Double.parseDouble(para1);
                                            double p2 = Double.parseDouble(para2);
                                            double p3 = Double.parseDouble(para3);
                                            double p4 = Double.parseDouble(para4);
                                            double p5 = Double.parseDouble(para5);
                                            double p6 = Double.parseDouble(para6);




                                            String label =  txt_profile_name_ans.getText().toString();

                                            // Create json object to pass the input as body
                                            JSONObject json = new JSONObject();
                                            String ecg_s = Arrays.toString(ECG_Signal);
                                            String ppg_s = Arrays.toString(PPG_Signal);
                                            int dbp_s = Integer.parseInt(DBP);
                                            int sbp_s = Integer.parseInt(SBP);
                                            try {
                                                json.put("ECG",ecg_s);
                                                json.put("PPG",ppg_s);
                                                json.put("SBP",sbp_s);
                                                json.put("DBP",dbp_s);
                                                json.put("P1",p1);
                                                json.put("P2",p2);
                                                json.put("P3",p3);
                                                json.put("P4",p4);
                                                json.put("P5",p5);
                                                json.put("P6",p6);
                                                json.put("pvalue",Pvalue);
                                                json.put("label",label);


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                ProfileActivity.this.runOnUiThread(() ->RequestRecalibration.setTextColor(Color.BLACK));
                                                ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());
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
                                                    ProfileActivity.this.runOnUiThread(() ->RequestRecalibration.setTextColor(Color.BLACK));
                                                    ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String request = response.body().string();
                                                    Double[] bp_para = {0.0,0.0,0.0,0.0,0.0,0.0};
                                                    String Pvalue = "";
                                                    Log.i("ddd", request);
                                                    //get the info parameter 1 and 2 from output of cloud function with http post
                                                    try {

                                                        JSONObject myresp = new JSONObject(request);
                                                        Log.i("ddd", myresp.toString());
                                                        //int message = myresp.getInt("message1");
                                                        //String mgg =  myresp.getString("message");
                                                        bp_para[0] = myresp.getDouble("p1");
                                                        bp_para[1] = myresp.getDouble("p2");
                                                        bp_para[2] = myresp.getDouble("p3");
                                                        bp_para[3] = myresp.getDouble("p4");
                                                        bp_para[4] = myresp.getDouble("p5");
                                                        bp_para[5] = myresp.getDouble("p6");
                                                        Pvalue = myresp.getString("pvalue");





//*********************************************************PLACE FOR PROCESS RECEIVED Parameter****************************************************************************************************//
                                                        //String msg = String.valueOf(message);
                                                        String p1 = Double.toString(bp_para[0]);
                                                        String p2 = Double.toString(bp_para[1]);
                                                        String p3 = Double.toString(bp_para[2]);
                                                        String p4 = Double.toString(bp_para[3]);
                                                        String p5 = Double.toString(bp_para[4]);
                                                        String p6 = Double.toString(bp_para[5]);


                                                        tinydb.remove("parameter1");
                                                        tinydb.remove("parameter2");
                                                        tinydb.remove("parameter3");
                                                        tinydb.remove("parameter4");
                                                        tinydb.remove("parameter5");
                                                        tinydb.remove("parameter6");
                                                        tinydb.remove("pvalue");

                                                        tinydb.putString("parameter1",p1);
                                                        tinydb.putString("parameter2",p2);
                                                        tinydb.putString("parameter3",p3);
                                                        tinydb.putString("parameter4",p4);
                                                        tinydb.putString("parameter5",p5);
                                                        tinydb.putString("parameter6",p6);
                                                        tinydb.putString("pvalue",Pvalue);



//***************************************************************************************************************************************************************************************//



                                                        String txt = "ReCalibration Successful!";
                                                        //ProfileActivity.this.runOnUiThread(() -> txt_profile_received_signal.setText(txt));
                                                        //Toast.makeText(ProfileActivity.this,"Get Result",Toast.LENGTH_LONG).show();


                                                        ProfileActivity.this.runOnUiThread(() ->RequestRecalibration.setTextColor(Color.BLACK));



                                                        //MainActivity.this.runOnUiThread(() -> title.setText(request));
                                                    } catch (JSONException e) {
                                                        ProfileActivity.this.runOnUiThread(() ->RequestRecalibration.setTextColor(Color.BLACK));
                                                        ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            //txt_profile_received_signal.setText("ST");




                                        }

                                    }
                                });
                            }
                        });



                }}
        });











//***************************************************************************************************************************************************************************************//
//***************************************************************************************************************************************************************************************//


        RequestFirstCalibration.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                exit:{
                String SBP = txt_current_bp_ans.getText().toString();
                String DBP = txt_current_hr_ans.getText().toString();
                RequestFirstCalibration.setTextColor(Color.RED);
                    if(SBP.length()<5 || DBP.length()<5){
                        RequestFirstCalibration.setTextColor(Color.BLACK);
                        ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Please Input Calibration SBP and DBP in Proper Form",Toast.LENGTH_LONG).show());
                        break exit;
                    }

                int [] SBP_cali = Stream.of(SBP.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                int [] DBP_cali = Stream.of(DBP.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();

                String label =  txt_profile_name_ans.getText().toString();

                // Create json object to pass the input as body
                JSONObject json = new JSONObject();
                String ecg_s = Arrays.toString(ECG_Signal);
                String ppg_s = Arrays.toString(PPG_Signal);
                String dbp_s = Arrays.toString(DBP_cali);
                String sbp_s = Arrays.toString(SBP_cali);
                try {
                    json.put("ECG",ecg_s);
                    json.put("PPG",ppg_s);
                    json.put("SBP",sbp_s);
                    json.put("DBP",dbp_s);
                    json.put("label",label);

                } catch (JSONException e) {
                    ProfileActivity.this.runOnUiThread(() ->RequestFirstCalibration.setTextColor(Color.BLACK));
                    ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());

                    e.printStackTrace();
                }
                String JSON = json.toString();
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),JSON);


                // request the object using HTTP post request
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(URL1)
                        .post(body)
                        .build();


                // Handle the response and get our parameter
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ProfileActivity.this.runOnUiThread(() ->RequestFirstCalibration.setTextColor(Color.BLACK));
                        ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());

                        Log.e("ddd", "Fail in Request");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String request = response.body().string();
                        Double[] bp_para = {0.0,0.0,0.0,0.0,0.0,0.0};
                        String Pvalue = "";
                        Log.i("ddd", request);
                        //get the info parameter 1 and 2 from output of cloud function with http post
                        try {

                            JSONObject myresp = new JSONObject(request);
                            Log.i("ddd", myresp.toString());
                            //int message = myresp.getInt("message1");
                            //String mgg =  myresp.getString("message");
                            bp_para[0] = myresp.getDouble("p1");
                            bp_para[1] = myresp.getDouble("p2");
                            bp_para[2] = myresp.getDouble("p3");
                            bp_para[3] = myresp.getDouble("p4");
                            bp_para[4] = myresp.getDouble("p5");
                            bp_para[5] = myresp.getDouble("p6");
                            Pvalue = myresp.getString("pvalue");






                            String p1 = Double.toString(bp_para[0]);
                            String p2 = Double.toString(bp_para[1]);
                            String p3 = Double.toString(bp_para[2]);
                            String p4 = Double.toString(bp_para[3]);
                            String p5 = Double.toString(bp_para[4]);
                            String p6 = Double.toString(bp_para[5]);


                            tinydb.remove("parameter1");
                            tinydb.remove("parameter2");
                            tinydb.remove("parameter3");
                            tinydb.remove("parameter4");
                            tinydb.remove("parameter5");
                            tinydb.remove("parameter6");
                            tinydb.remove("pvalue");


                            tinydb.putString("parameter1",p1);
                            tinydb.putString("parameter2",p2);
                            tinydb.putString("parameter3",p3);
                            tinydb.putString("parameter4",p4);
                            tinydb.putString("parameter5",p5);
                            tinydb.putString("parameter6",p6);
                            tinydb.putString("pvalue",Pvalue);



                            String txt = "First Calibration Successful!";
                            ProfileActivity.this.runOnUiThread(() -> RequestFirstCalibration.setTextColor(Color.BLACK));
                            //Toast.makeText(ProfileActivity.this,"Get Result",Toast.LENGTH_LONG).show();




                            //MainActivity.this.runOnUiThread(() -> title.setText(request));
                        } catch (JSONException e) {
                            ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());
                            ProfileActivity.this.runOnUiThread(() -> RequestFirstCalibration.setTextColor(Color.BLACK));

                            e.printStackTrace();
                        }
                    }
                });
                //txt_profile_received_signal.setText("ST");

            }}
        });



//***************************************************************************************************************************************************************************************//

        //***************************************************************************************************************************************************************************************//


        btn_ML_calibration.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                exit:{
                String SBP = txt_current_bp_ans.getText().toString();
                String DBP = txt_current_hr_ans.getText().toString();

                btn_ML_calibration.setTextColor(Color.RED);
                    if(SBP.length()<5 || DBP.length()<5){
                        btn_ML_calibration.setTextColor(Color.BLACK);
                        ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Please Input Calibration SBP and DBP in Proper Form",Toast.LENGTH_LONG).show());
                        break exit;
                    }
                int [] SBP_cali = Stream.of(SBP.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                int [] DBP_cali = Stream.of(DBP.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();


                String label =  txt_profile_name_ans.getText().toString();
                // Create json object to pass the input as body
                JSONObject json = new JSONObject();
                String ecg_s = Arrays.toString(ECG_Signal);
                String ppg_s = Arrays.toString(PPG_Signal);
                String dbp_s = Arrays.toString(DBP_cali);
                String sbp_s = Arrays.toString(SBP_cali);
                try {
                    json.put("ECG",ecg_s);
                    json.put("PPG",ppg_s);
                    json.put("SBP",sbp_s);
                    json.put("DBP",dbp_s);
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
                        .url(URL2)
                        .post(body)
                        .build();


                // Handle the response and get our parameter
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());
                        ProfileActivity.this.runOnUiThread(() -> btn_ML_calibration.setTextColor(Color.BLACK));
                        e.printStackTrace();

                        Log.e("ddd", "Fail in Request");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String request = response.body().string();
                        Double[] bp_para = {0.0,0.0,0.0,0.0,0.0,0.0};
                        String Pvalue = "";
                        Log.i("ddd", request);
                        //get the info parameter 1 and 2 from output of cloud function with http post
                        try {


                            JSONObject myresp = new JSONObject(request);
                            Log.i("ddd", myresp.toString());


                            String txt = "First Calibration Successful!";
                            ProfileActivity.this.runOnUiThread(() -> btn_ML_calibration.setTextColor(Color.BLACK));
                            //Toast.makeText(ProfileActivity.this,"Get Result",Toast.LENGTH_LONG).show();

                            //MainActivity.this.runOnUiThread(() -> title.setText(request));
                        } catch (JSONException e) {
                            ProfileActivity.this.runOnUiThread(() -> Toast.makeText(ProfileActivity.this,"Fail Calibration",Toast.LENGTH_LONG).show());
                            ProfileActivity.this.runOnUiThread(() -> btn_ML_calibration.setTextColor(Color.BLACK));
                            e.printStackTrace();

                        }
                    }
                });
                //txt_profile_received_signal.setText("ST");

            }}
        });


//***************************************************************************************************************************************************************************************//

/*

        // When the Calibration button is clicked, this assumed that the profile has been filled
        btn_profile_calibration.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                String SBP = txt_current_bp_ans.getText().toString();
                String DBP = txt_current_hr_ans.getText().toString();

                int [] SBP_cali = Stream.of(SBP.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                int [] DBP_cali = Stream.of(DBP.split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();


                String para1 = tinydb.getString("parameter1");
                String para2 = tinydb.getString("parameter2");
                String para3 = tinydb.getString("parameter3");
                String para4 = tinydb.getString("parameter4");
                String para5 = tinydb.getString("parameter5");
                String para6 = tinydb.getString("parameter6");
                String Pvalue = tinydb.getString("pvalue");
                double p1 = Double.parseDouble(para1);
                double p2 = Double.parseDouble(para2);
                double p3 = Double.parseDouble(para3);
                double p4 = Double.parseDouble(para4);
                double p5 = Double.parseDouble(para5);
                double p6 = Double.parseDouble(para6);




                String label =  txt_profile_name_ans.getText().toString();

                // Create json object to pass the input as body
                JSONObject json = new JSONObject();
                String ecg_s = Arrays.toString(ECG_Signal);
                String ppg_s = Arrays.toString(PPG_Signal);
                String dbp_s = Arrays.toString(DBP_cali);
                String sbp_s = Arrays.toString(SBP_cali);
                try {
                    json.put("ECG",ecg_s);
                    json.put("PPG",ppg_s);
                    json.put("SBP",sbp_s);
                    json.put("DBP",dbp_s);
                    json.put("P1",p1);
                    json.put("P2",p2);
                    json.put("P3",p3);
                    json.put("P4",p4);
                    json.put("P5",p5);
                    json.put("P6",p6);
                    json.put("pvalue",Pvalue);
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
                        Double[] bp_para = {0.0,0.0,0.0,0.0,0.0,0.0};
                        String Pvalue = "";
                        Log.i("ddd", request);
                        //get the info parameter 1 and 2 from output of cloud function with http post
                        try {

                            JSONObject myresp = new JSONObject(request);
                            Log.i("ddd", myresp.toString());
                            //int message = myresp.getInt("message1");
                            //String mgg =  myresp.getString("message");
                            bp_para[0] = myresp.getDouble("p1");
                            bp_para[1] = myresp.getDouble("p2");
                            bp_para[2] = myresp.getDouble("p3");
                            bp_para[3] = myresp.getDouble("p4");
                            bp_para[4] = myresp.getDouble("p5");
                            bp_para[5] = myresp.getDouble("p6");
                            Pvalue = myresp.getString("pvalue");


                            //String msg = String.valueOf(message);
                            String p1 = Double.toString(bp_para[0]);
                            String p2 = Double.toString(bp_para[1]);
                            String p3 = Double.toString(bp_para[2]);
                            String p4 = Double.toString(bp_para[3]);
                            String p5 = Double.toString(bp_para[4]);
                            String p6 = Double.toString(bp_para[5]);


                            tinydb.remove("parameter1");
                            tinydb.remove("parameter2");
                            tinydb.remove("parameter3");
                            tinydb.remove("parameter4");
                            tinydb.remove("parameter5");
                            tinydb.remove("parameter6");
                            tinydb.remove("pvalue");

                            tinydb.putString("parameter1",p1);
                            tinydb.putString("parameter2",p2);
                            tinydb.putString("parameter3",p3);
                            tinydb.putString("parameter4",p4);
                            tinydb.putString("parameter5",p5);
                            tinydb.putString("parameter6",p6);
                            tinydb.putString("pvalue",Pvalue);



                            String txt = "ReCalibration Successful!";
                            ProfileActivity.this.runOnUiThread(() -> txt_profile_received_signal.setText(txt));
                            //Toast.makeText(ProfileActivity.this,"Get Result",Toast.LENGTH_LONG).show();




                            //MainActivity.this.runOnUiThread(() -> title.setText(request));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //txt_profile_received_signal.setText("ST");



            }
        });
*/



//***************************************************************************************************************************************************************************************//

        // When the Calibration button is clicked, this assumed that the profile has been filled
        RequestMeasure.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                exit:
                {


                    int long_measure = 30000;


                    i = 0;
                    ECG_Signal = new int[long_measure];
                    PPG_Signal = new int[long_measure];
//*********************************BLE************************************************************************************************************************************************//
                    // Get the connected blue tooth and read the sending information
                    try {
                        bleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
                        gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
                        characteristic = gatt.getServices().get(2).getCharacteristics().get(1);
                    } catch (Exception e) {
                        Toast.makeText(ProfileActivity.this, "Please Connect the BLE Device", Toast.LENGTH_SHORT).show();
                        break exit;

                    }
                    RequestMeasure.setTextColor(Color.RED);


                    // Using BLE notification
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


                                // Read the Characteristic one by one, Global variable i is used to count the number
                                // Note the vairable initialisation of ECG and PPG array in global or local
                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            // Read the Notification message
                                            Mydata = characteristic.getValue();
                                            ECG = new byte[4];
                                            PPG = new byte[4];
                                            System.arraycopy(Mydata, 0, ECG, 0, 4);
                                            System.arraycopy(Mydata, 4, PPG, 0, 4);

                                            // Read upto the Sample Num
                                            if (i < long_measure) {
                                                // ECG_Signal[i] = 2;
                                                int d = little2big(fromByteArray(ECG));
                                                int c = little2big(fromByteArray(PPG));

                                                ECG_Signal[i] = d;
                                                PPG_Signal[i] = c;
                                                String TestingValue = Integer.toString(d) + "    " + Integer.toString(c);

                                                i = i + 1;
                                            }

                                            //Invoke the cloud function when the bluetooth get enough sample
                                            //It will call the cloud function using http cloud
                                            if (i == long_measure) {


                                                ProfileActivity.this.runOnUiThread(() -> RequestMeasure.setTextColor(Color.BLACK));
                                                // Close the Instance so The Device only call the cloud function once
                                                BleManager.getInstance().clearCharacterCallback(bleDevice);
                                                //Request the CLoud function to do the calculation
                                                //ProfileActivity.this.runOnUiThread(() -> txt_profile_received_signal.setText("Signal Retrieved"));
                                                //Toast.makeText(ProfileActivity.this,"Signal Retrived",Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });
                                }
                            });


                }


            }});




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
