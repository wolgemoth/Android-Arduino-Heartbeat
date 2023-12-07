package com.loui.sra2022bpm_2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.ecurtin.KDTree.Point;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import com.ecurtin.KDTree.KDTree;

public class MainActivity extends AppCompatActivity {

    private final static String ACTION_USB_PERMISSION = "permission";
    private final static String BPM_ALERT_NOTIFICATION_CHANNEL = "Alerts";

    private UsbDevice           m_Device     = null;
    private UsbDeviceConnection m_Connection = null;
    private UsbSerialDevice     m_Serial     = null;
    private UsbManager          m_UsbManager = null;

    private float m_Heartbeat = 0;

    private TextView m_LogText = null;
    private TextView m_BPMText = null;

    private ImageView m_HeartSymbol = null;
    private float m_DefaultHeartSymbolWidth;

    private final StringBuilder m_Buffer = new StringBuilder();

    private  InputStream m_InputStream;
    private OutputStream m_OutputStream;

    private    KDTree m_IAPS;
    private ImageView m_IAPSImageView = null;

    private SeekBar    m_Valence_MN_SeekBar;
    private SeekBar    m_Valence_SD_SeekBar;
    private SeekBar    m_Arousal_MN_SeekBar;
    private SeekBar    m_Arousal_SD_SeekBar;
    private SeekBar m_Dominance1_MN_SeekBar;
    private SeekBar m_Dominance1_SD_SeekBar;
    private SeekBar m_Dominance2_MN_SeekBar;
    private SeekBar m_Dominance2_SD_SeekBar;

    //region USB

    private final UsbSerialInterface.UsbReadCallback m_Callback = (byte[] _data) -> AppendToBuffer(_data);

    private final UsbBroadcastReceiver m_BroadcastReceiver = new UsbBroadcastReceiver();

    private class UsbBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context _context, Intent _intent) {

            String action = _intent.getAction();

            switch (action) {
                case ACTION_USB_PERMISSION: {

                    if (GetUSBPermission(_intent)) {
                        TryOpenUSBConnection();
                    }

                    break;
                }
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                    StartUsbConnection();
                    break;
                }
                case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                    Disconnect();
                    break;
                }
                default: {

                    Debug("Unhandled intent action in UsbBroadcastReceiver.\"" + action + "\"");

                    break;
                }
            }
        }

        protected Boolean GetUSBPermission(Intent _intent) {

            final boolean granted = _intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

            Debug("Permission " +
                            (granted ? "Granted." : "Denied."),
                    (granted ? Log.DEBUG : Log.ERROR)
            );

            return granted;
        }

        protected void TryOpenUSBConnection() {

            try {
                m_Connection = m_UsbManager.openDevice(m_Device);
                m_Serial = UsbSerialDevice.createUsbSerialDevice(m_Device, m_Connection);

                if (m_Serial != null) {

                    if (m_Serial.open()) {
                        m_Serial.setBaudRate(9600);
                        m_Serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        m_Serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        m_Serial.setParity(UsbSerialInterface.PARITY_NONE);
                        m_Serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                        m_Serial.read(m_Callback);

                        Debug("Port opened!");
                    } else {
                        Debug("Failure opening SerialPort.");
                    }
                } else {
                    Debug("Failure initialising SerialPort.");
                }
            } catch (Exception e) {
                Debug(e.toString(), Log.ERROR);
            }
        }
    }

    //endregion USB

    //region Bluetooth

    private void StartBluetoothConnection() {

        int requestCode = 0;

        // Check / request "BLUETOOTH" and "BLUETOOTH_CONNECT" permission.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT }, requestCode);
        }

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();

        if (bt != null && bt.isEnabled()) {

            Set<BluetoothDevice> devices = bt.getBondedDevices();
            for (BluetoothDevice device : devices) {

                //Debug(device.getName());

                if (Objects.equals(device.getName(), "HC-05")) {

                    Debug("Connecting to \"" + device.getName() + "\"...");

                    ParcelUuid[] uuids = device.getUuids();

                    try {
                        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                        socket.connect();

                        m_OutputStream = socket.getOutputStream();
                         m_InputStream = socket.getInputStream();

                         Debug("Successfully connected to \"" + device.getName() + "\"");
                    }
                    catch (IOException e) {
                        Debug("Failed connecting to device \"" + device.getName() + "\". " + e.toString(), Log.ERROR);
                    }
                }
            }
        }
        else {
            Debug("Bluetooth is disabled.", Log.ERROR);
        }
    }

    public void TryWriteBluetooth(String s) {

        try {
            m_OutputStream.write(s.getBytes());
        }
        catch (IOException e) {
            Debug(e.toString(), Log.ERROR);
        }
    }

    public void TryReadBluetooth() {

        try {

            // Read a chunk of bytes from the input stream and store them in an array.
            byte[] chunk = new byte[1024];

            int bytesRead = m_InputStream.read(chunk);

            // Filter empty values from the chunk and store them in an array.
            byte[] trimmed = new byte[bytesRead];

            for (int i = 0; i < trimmed.length; i++) {
                trimmed[i] = chunk[i];
            }

            // Append the filtered values to the buffer.
            AppendToBuffer(chunk);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion Bluetooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_LogText = findViewById(R.id.debug); m_LogText.setText(null);
        m_BPMText = findViewById(R.id.bpm);   m_BPMText.setText("0");

        m_HeartSymbol = findViewById(R.id.heart_symbol);
        m_DefaultHeartSymbolWidth  = m_HeartSymbol.getLayoutParams().width;

        m_IAPSImageView = findViewById(R.id.IAPSImage);

           m_Valence_MN_SeekBar = findViewById(R.id.valence_mn_seekbar);
           m_Valence_SD_SeekBar = findViewById(R.id.valence_sd_seekbar);
           m_Arousal_MN_SeekBar = findViewById(R.id.arousal_mn_seekbar);
           m_Arousal_SD_SeekBar = findViewById(R.id.arousal_sd_seekbar);
        m_Dominance1_MN_SeekBar = findViewById(R.id.dominance1_mn_seekbar);
        m_Dominance1_SD_SeekBar = findViewById(R.id.dominance1_sd_seekbar);
        m_Dominance2_MN_SeekBar = findViewById(R.id.dominance2_mn_seekbar);
        m_Dominance2_SD_SeekBar = findViewById(R.id.dominance2_sd_seekbar);

        TextPrepend("Awaiting USB connection...");

        m_UsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(m_BroadcastReceiver, filter);

        NotificationChannel notificationChannel = new NotificationChannel(
                BPM_ALERT_NOTIFICATION_CHANNEL,
                BPM_ALERT_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.enableVibration(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);

        Button button = findViewById(R.id.connect_button);
        button.setOnClickListener(view -> StartUsbConnection());

           m_Valence_MN_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
           m_Valence_SD_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
           m_Arousal_MN_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
           m_Arousal_SD_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
        m_Dominance1_MN_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
        m_Dominance1_SD_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
        m_Dominance2_MN_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);
        m_Dominance2_SD_SeekBar.setOnSeekBarChangeListener(m_IAPSSeekBarChanged);

        try {
            Point[] points = IAPSDatasetReader.Read(this,"AllSubjects_1-20.csv");

            double[] values = new double[8];

            for (Point point : points) {

                for (int i = 0; i < values.length; i++) {

                    double val = point.get(i);

                    if (val > values[i]) {
                        values[i] = val;
                    }
                }
            }

            //StringBuilder sb = new StringBuilder();

            //for (double value : values) {
            //    sb.append(value);
            //    sb.append(',');
            //}
            //Debug(sb.toString());

            m_IAPS = new KDTree(points);

        }
        catch (Exception e) {
            Debug(e.toString(), Log.ERROR);
        }

        // Get refresh rate of display.
        // We use this in the below timer to create code that recurs at the same rate the screen is refreshed.
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float refreshRate = display.getRefreshRate();

        Timer timer  = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(() -> {

                    float deltaT = 1.0f / refreshRate;

                    AnimateHeartSymbol(deltaT);
                });
            }

        }, 0, (int)((1.0 / refreshRate) * 1000));

        // Poll Bluetooth every 500ms.
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                try {
                    if (m_InputStream == null) {
                        StartBluetoothConnection();
                    }
                    else {
                        TryReadBluetooth();
                      //TryWriteBluetooth("Testing, testing. 123.");
                    }
                }
                catch (Exception e) {
                    Debug(e.toString(), Log.ERROR);
                }
            };
        }, 1, 100);
    }

    SeekBar.OnSeekBarChangeListener m_IAPSSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            UpdateIAPSImageView();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };


    private void UpdateIAPSImageView() {
        try {
            Point point = m_IAPS.nearestNeighborSearch(
                    new Point(new double[] {
                               m_Valence_MN_SeekBar.getProgress() / 100.0f,
                               m_Valence_SD_SeekBar.getProgress() / 100.0f,
                               m_Arousal_MN_SeekBar.getProgress() / 100.0f,
                               m_Arousal_SD_SeekBar.getProgress() / 100.0f,
                            m_Dominance1_MN_SeekBar.getProgress() / 100.0f,
                            m_Dominance1_SD_SeekBar.getProgress() / 100.0f,
                            m_Dominance2_MN_SeekBar.getProgress() / 100.0f,
                            m_Dominance2_SD_SeekBar.getProgress() / 100.0f
                    })
            );

            m_IAPSImageView.setImageBitmap(BitmapFactory.decodeStream(getAssets().open(point.m_IAPS_ID + ".jpg")));
        }
        catch (Exception e){
            Debug(e.toString(), Log.ERROR);
        }
    }

    private void StartUsbConnection() {

        final int arduinoVID = 9025;

        HashMap<?, ?> usbDevices = m_UsbManager.getDeviceList();

        if (usbDevices.isEmpty() == false) {

            for (Map.Entry<?, ?> entry : usbDevices.entrySet()) {

                m_Device = (UsbDevice)entry.getValue();
                int deviceVendorID = m_Device.getVendorId();

                Debug(Integer.toString(deviceVendorID));

                if (deviceVendorID == arduinoVID) {

                    PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    m_UsbManager.requestPermission(m_Device, intent);

                    Debug("Connection successful.");

                    break;
                }
                else {
                    m_Connection = null;
                    m_Device     = null;

                    Debug("Connection unsuccessful. Device vendor ID does not match the permitted ID.");
                }
            }
        }
        else { Debug("No USB devices found!"); }
    }

    private void AppendToBuffer(byte[] _data) {

        // Data received can sometimes be expected in reverse order.
        // Set this flag to true if you wish to process the data in reverse.
        final boolean reversed = false;

        try {

            // Append characters to buffer in forward or reverse order, depending on the "reversed" flag.
            for (int i = 0; i < _data.length; i++) {
                m_Buffer.append((char) _data[reversed ? (_data.length - 1) - 1 : i]);
            }

            ParseBuffer(); // Parse the buffer.
        }
        catch (Exception e) {
            Debug(e.toString(), Log.ERROR);
        }
    }

    private void ParseBuffer() {

        final String messageDivider = "\r\n";
        final String  sensorDivider = ": ";

        //final String pinIndexPattern = "^([0-9]+:\\s.*)";

        String[] sensorPatterns = {
            "([0-9]+)\\.([0-9]+)",
            "([0-9]+)",
        };

        // Create an array of booleans for detected sensors, and initialise it to false.
        boolean[] sensorsDetected = new boolean[sensorPatterns.length];

        // Split the buffer by the divider.
        String[] strings = m_Buffer.toString().split(messageDivider, 0);

        // Iterate from the most to least recent entry.
        for (int i = strings.length - 1; i > 0; i--) {

            final String[] input = strings[i].trim().split(sensorDivider, 0);

            //Debug(strings[i]);

            if (input.length == 2) {

                final int    sensor = Integer.parseInt(input[0]);
                final String  value = input[1];

                // Check if the sensor hasn't already been parsed and matches the regex pattern for input.
                if (sensorsDetected[sensor] == false && value.matches(sensorPatterns[sensor])) {
                    sensorsDetected[sensor]  = true;

                    switch(sensor) {

                        case 0: {
                            m_Heartbeat = Float.parseFloat(value);
                            OnHeartbeat();

                            break;
                        }
                        case 1: {
                            Debug(value);

                            break;
                        }

                        default: { break; }
                    }
                }
            }
        }

        // Clear the buffer if something was detected within it, or the size exceeds the max size.
        final int bufferClearThreshold = 1024;

        for (boolean b : sensorsDetected) {
            if (b || m_Buffer.length() > bufferClearThreshold) {

                m_Buffer.setLength(0); // Clear buffer.

                break;
            }
        }
    }

    private void TextPrepend(String _value) {

        String concat = _value + "\n" + m_LogText.getText();

        m_LogText.setText(concat);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void OnHeartbeat() {

        runOnUiThread(() -> {

            final float highBPM = 100.0f;

            /* VIBRATION EFFECT */
            Vibrate(50);

            /* HEARTBEAT EFFECT */
            final float heartbeatScale = 1.25f;

            int newSize = (int)(m_DefaultHeartSymbolWidth * heartbeatScale);

            SetImageViewDimensions(m_HeartSymbol, newSize, newSize);

            /* NOTIFICATION ALERT */

            if (m_Heartbeat > highBPM) {
                Notify(BPM_ALERT_NOTIFICATION_CHANNEL, "BPM Alert", "High BPM: " + m_Heartbeat);
            }

            // Write BPM.
            m_BPMText.setText(Integer.toString((int)m_Heartbeat));
        });
    }

    private void AnimateHeartSymbol(float _deltaT) {

        final float speed = 3.0f;

        float currSize  = m_HeartSymbol.getLayoutParams().width;
        int    newSize = (int) Lerp(currSize, m_DefaultHeartSymbolWidth, _deltaT * speed);

        SetImageViewDimensions(m_HeartSymbol, newSize, newSize);
    }

    private void SetImageViewDimensions(ImageView _image, int _width, int _height) {

        try {
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)_image.getLayoutParams();
            params.width  = _width;
            params.height = _height;

            _image.setLayoutParams(params);
        }
        catch (Exception e) {
            Debug(e.toString(), Log.ERROR);
        }
    }

    private float Lerp(float _a, float _b, float _t) {
        return (_a * (1.0f - _t)) + (_b * _t);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Disconnect();
    }

    private void Disconnect() {
        Debug("Device disconnected.");

        if (m_Serial != null) {
            m_Serial.close();
            m_Serial = null;
        }
    }

    //region Notifications

    private void Notify(String _category, String _title, String _message) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, _category);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setContentTitle(_title);
        builder.setContentText(_message);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);

        Intent intent = new Intent(this, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());
    }

    //endregion Notifications

    //region Vibration

    private void Vibrate(int _duration) {
        Vibrate(_duration, VibrationEffect.DEFAULT_AMPLITUDE);
    }

    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("deprecation")
    private void Vibrate(int _duration, int _amplitude) {

        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(_duration, _amplitude));
        }
        else {
            vibrator.vibrate(_duration); // Deprecated in API 26.
        }
    }

    //endregion Vibration

    // region Debugging

    private void Debug(String _message) {
        Debug(_message, Log.DEBUG);
    }

    private void Debug(String _message, int _code) {

        TextPrepend(_message);

        Log.println(_code, "Serial", _message);
    }

    //endregion Debugging

}
