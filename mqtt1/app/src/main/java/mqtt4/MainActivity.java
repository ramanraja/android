package org.home.soma.mqtt4;
/*
https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
other links:
https://github.com/eclipse/paho.mqtt.android/blob/master/paho.mqtt.android.example/src/main/java/paho/mqtt/java/example/PahoExampleActivity.java
https://medium.com/@gaikwadchetan93/android-real-time-communication-using-mqtt-9ea42551475d
https://wildanmsyah.wordpress.com/2017/05/11/mqtt-android-client-tutorial/
http://androidkt.com/android-mqtt/
https://android.jlelse.eu/about-the-mqtt-protocol-for-iot-on-android-efb4973577b
https://github.com/wildan2711/mqtt-android-tutorial
https://wildanmsyah.wordpress.com/2017/05/11/mqtt-android-client-tutorial/
https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
https://www.survivingwithandroid.com/2016/10/mqtt-protocol-tutorial.html
 */
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    TextView mLabel1;
    MqttAndroidClient client;
    String broker = "tcp://broker.mqttdashboard.com:1883";
    String topic = "india/delhi/command";
    String TAG = "Somasi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button)findViewById(R.id.button1)).setOnClickListener((View.OnClickListener) this);
        ((Button)findViewById(R.id.button2)).setOnClickListener((View.OnClickListener) this);
        mLabel1 = (TextView)findViewById(R.id.label1);
        try {
            Log.d(TAG, " --- connecting to broker --- ");
            makeMqttClient(this, broker, "SomasClient");
            IMqttToken token = client.connect();
            Log.d(TAG, " --- connected --- ");
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, " --- onSuccess --- ");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, " --- onFailure --- ");

                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeMqttClient(Context context, String brokerUrl, String clientId) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill(topic, "I am going offline".getBytes(), 1, true);
        //mqttConnectOptions.setUserName("username");
        //mqttConnectOptions.setPassword("password".toCharArray());
        final DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);

        client = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = client.connect(mqttConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setBufferOpts(disconnectedBufferOptions);
                    Log.d(TAG, "Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, " --- onDestroy --- ");
        //disconnectMqttClient ?
        client.unregisterResources();
        client.close();
        super.onDestroy();
    }
    
    @Override
    public void onClick(View view) {
        String payload = "ON";
        Log.d(TAG, "Publishing: ");
        if (view.getId()==R.id.button2)
           payload = "OFF";
        Log.d(TAG, payload);
        String dispstr = "Sending msg: " +payload;
        mLabel1.setText(dispstr);
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (Exception e) {
            Log.d(TAG, " --- EXCEPTION, Somas ! --- ");
            e.printStackTrace();
        }
    }
}
