package com.example.bluetooth_communication_6_send

import DogData
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile.GATT
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dogactivityplanner.ctm.CTM
import com.example.dogactivityplanner.ctm.EnvironmentalData
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    // deviceSensorName is used to identify the device in order to connect to it
    private val deviceSensorName = "HumiDetector"
    private val deviceWatchAddress = "DC:CC:E6:EB:D6:E1"
    private val deviceSensorUuid = UUID.fromString("d9559455-03e7-4018-aec1-ec1c6380347a")
    private val sendServiceUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var temperatureValue: Float? = null
    private var humidityValue: Float? = null
    //private var humidexValue: Float? = null

    // initialize variables for bluetooth gatt connection
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSensorDevice: BluetoothDevice? = null
    private var bluetoothWatchDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothGattService: BluetoothGattService? = null
    private var bluetoothGattCharacteristics: MutableList<BluetoothGattCharacteristic>? = null

    // define bluetooth gatt callback
    private val bluetoothGattCallback = object: BluetoothGattCallback() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // create the view layout based on the activity_main.xml file
        setContentView(R.layout.activity_main)

        // begin a coroutine that periodically updates the displayed data
        val dataJob = dataJobStart()
        // begin the listener for the ble button
        bleButtonListener()
        sendButtonListener()
    }
    private fun sendButtonListener(){
        val btBluetooth = findViewById<Button>(R.id.sendButton)
        btBluetooth.setOnClickListener{
            if(bluetoothSocket == null){
                if(bluetoothWatchDevice == null){
                    bluetoothWatchDevice = getWatchDevice()
                }
                if(bluetoothWatchDevice != null){
                    bluetoothSocket = connectToDevice(bluetoothWatchDevice)
                }
            }else{
                Toast.makeText(this,"送信中...",Toast.LENGTH_SHORT).show()
            }
        }
    }

    // if the button is pressed, call the specified functions
    private fun bleButtonListener() {
        val bleButton = findViewById<Button>(R.id.bleButton)
        bleButton?.setOnClickListener {
            // raise toast message when the button is pressed
            val message = "*beep*"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            // if the button is pressed, call the functions

            // if null, find the bluetooth device
            if (bluetoothSensorDevice == null) {
                bluetoothSensorDevice = getBondedDevice()
            }
            // if null, find the bluetooth gatt
            if (bluetoothGatt == null) {
                bluetoothGatt = getGatt(bluetoothSensorDevice)
            }
            // if null, find the bluetooth gatt service
            if (bluetoothGattService == null) {
                // getGattServices() needs to be called twice for it to work properly for some reason
                bluetoothGattService = getGattServices(bluetoothGatt)
                if (bluetoothGattService == null) {
                    bluetoothGattService = getGattServices(bluetoothGatt)
                }
            }
            // if null, find the bluetooth gatt service characteristics
            if (bluetoothGattCharacteristics == null) {
                bluetoothGattCharacteristics = getGattCharacteristics(bluetoothGattService)
            }
            if (bluetoothGattCharacteristics != null) {
                updateStatusView("Connection established.")
            }
        }
    }

    // create a coroutine job
    // periodically calls readGattCharacteristics() and updateDataValues()
    private fun dataJobStart(): Job {
        return MainScope().launch {
            while (isActive) {
                // delay for only 5 seconds if gatt is not found
                delay(5000L)
                // read gatt characteristics if they are available
                if (bluetoothGattCharacteristics != null) {
                    readGattCharacteristics(bluetoothGattCharacteristics)
                    // get the values of the characteristics
                    updateDataValues(bluetoothGattCharacteristics)
                    // update the displayed heatstroke risk
                    if(temperatureValue != null&&humidityValue != null){
                        if(bluetoothSocket != null){
                            var walkableTime = calculateWalkableTime(temperatureValue!!.toDouble(), humidityValue!!.toDouble())
                            if (walkableTime != null) {
                                updateWalkableTime(walkableTime)
                            }
                            val sendMessage = "${walkableTime}分${temperatureValue}℃${humidityValue}%"
                            sendMessage(bluetoothSocket, sendMessage)
                        }
                    }
                    //updateHeatstrokeRisk()

                    connectionSensorCheck()

                    // delay for 30 seconds total if gatt is found
                    delay(25000L)
                }
            }
        }
    }
    // check connection status and reconnect gatt if disconnected
    private fun connectionSensorCheck() {
        if (!permissionCheck()) {
            return
        }
        if (bluetoothManager?.getConnectionState(bluetoothSensorDevice, GATT) == STATE_DISCONNECTED) {
            bluetoothGatt?.connect()
        }
    }

    private fun connectionWatchCheck(){
        Log.d("connectionCheck","connection check")
        val handler = Handler(Looper.getMainLooper())
        Log.d("connectionCheck","connection check")
        val runnable = object : Runnable{
            override fun run(){
                bluetoothSocket?.let{
                    if(it.isConnected()){
                        return
                    }else{
                        bluetoothSocket = connectToDevice(bluetoothWatchDevice)
                    }
                }
            }
        }
        handler.post(runnable)
    }

    // update the sensor data values with the values of the read characteristics
    // requires that there are exactly 3 gatt characteristics
    private fun updateDataValues(bluetoothGattCharacteristics: MutableList<BluetoothGattCharacteristic>?) {
        if (bluetoothGattCharacteristics?.size == 3) {
            // read values from the characteristics
            temperatureValue = byteArrayToFloat32(bluetoothGattCharacteristics[0].value)
            humidityValue = byteArrayToFloat32(bluetoothGattCharacteristics[1].value)
            //humidexValue = byteArrayToFloat32(bluetoothGattCharacteristics[2].value)
            // if not null, update each stored sensor value
            if (temperatureValue != null) {
                // display the data rounded up to 2nd decimal and add the unit
                val temperatureText = "${(temperatureValue!! * 100.0).roundToInt() / 100.0} °C"
                val temperatureTextView = findViewById<TextView>(R.id.temperatureValueView)
                temperatureTextView.text = temperatureText
            }
            if (humidityValue != null) {
                // display the data rounded up to 2nd decimal and add the unit
                val humidityText = "${(humidityValue!! * 100.0).roundToInt() / 100.0} %"
                val humidityTextView = findViewById<TextView>(R.id.humidityValueView)
                humidityTextView.text = humidityText
            }
            /*if (humidexValue != null) {
                // display the data rounded up to 2nd decimal and add the unit
                val humidexText = "${(humidexValue!! * 100.0).roundToInt() / 100.0} °C"
                val humidexTextView = findViewById<TextView>(R.id.humidexValueView)
                humidexTextView.text = humidexText
            }
            */
        } else {
            return
        }
    }
    private fun calculateWalkableTime(temperature:Double,humidity:Double):Int?{
        val dogData=DogData(MET = 4.0, Wt = 30.0, Lnth = 100.0)
        val envData= EnvironmentalData(Tdb = temperature, RH = humidity, WS = 1.5)

        val ctm = CTM(dogData)
        ctm.setEnvironmentalValue(envData)
        ctm.MRT = 34.2

        var counter = 0
        for(i in 0..30){
            ctm.calculateNextTemperature()
            if(ctm.TTCR<ctm.maxTTCR){
                counter++
            }
        }
        if(bluetoothSocket != null){
            Toast.makeText(this,"${counter}分",Toast.LENGTH_SHORT).show()
            return counter
        }
        return null
    }
    private fun updateWalkableTime(WalkableTime:Int){
        val time = WalkableTime
        val walkableText = "${time}分"
        val walkableTextView = findViewById<TextView>(R.id.WalkableTimeValueView)
        walkableTextView.text = walkableText
    }

    // updates the displayed heatstroke risk estimation based on the Humidex value
    // Based on the recommendations set by the Canadian Centre for Occupational Health and Safety (CCOHS)
    // 29 or under: Little to no risk
    // 30-33: Low Risk
    // 34-37: Medium Risk
    // 38-39: High Risk
    // 40-41: Danger
    // 42-44: Great Danger
    // 45 or over: Extreme Danger
    /*
    private fun updateHeatstrokeRisk() {
        if (humidexValue == null) {
            return
        }
        val text0 = "Very Low"
        val text1 = "Low"
        val text2 = "Medium"
        val text3 = "High"
        val text4 = "Dangerous"
        val text5 = "Very Dangerous"
        val text6 = "Extremely Dangerous"
        val riskTextView = findViewById<TextView>(R.id.riskValueView)
        // update the displayed heatstroke risk based on current Humidex value
        if (humidexValue!! <= 29) {
            riskTextView.text = text0
        } else if (humidexValue!! <= 33) {
            riskTextView.text = text1
        } else if (humidexValue!! <= 37) {
            riskTextView.text = text2
        } else if (humidexValue!! <= 39) {
            riskTextView.text = text3
        } else if (humidexValue!! <= 41) {
            riskTextView.text = text4
        } else if (humidexValue!! <= 44) {
            riskTextView.text = text5
        } else {
            riskTextView.text = text6
        }
    }
    */

    private fun getWatchDevice(): BluetoothDevice?{
        var message : String? = null
        if(!permissionCheck()){
            message = "Permission がありません"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            return null
        }

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothAdapter = bluetoothManager?.adapter as BluetoothAdapter

        if(!bluetoothAdapter?.isEnabled!!){
            return null
        }

        val bondedDevice = bluetoothAdapter?.bondedDevices

        if(bondedDevice!!.isEmpty()){

            message = "${deviceWatchAddress}と接続されていません"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            return null
        }
        for(device in bondedDevice){
            val deviceAddress = device.address.toString()
            Log.d("device address","${deviceAddress}")

            if(deviceAddress == deviceWatchAddress){
                return device
            }
        }
        return null
    }

    private fun connectToDevice(device : BluetoothDevice?): BluetoothSocket?{
        var message = ""

        if(!permissionCheck()){
            message = "Permission がありません"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            return null
        }
        try{
            if (device != null) {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(sendServiceUuid)
            }
            bluetoothSocket?.connect()
            message = "接続成功"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            return bluetoothSocket
        }catch (e: IOException){
            message = "接続失敗"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            try{
                bluetoothSocket?.close()
                return null
            }catch (closeException: IOException){
                closeException.printStackTrace()
            }
        }
        return null
    }


    // read the gatt characteristics
    // requires that there are exactly 3 gatt characteristics
    private fun readGattCharacteristics(bluetoothGattCharacteristics: MutableList<BluetoothGattCharacteristic>?) {
        // check and request for bluetooth permissions
        if (!permissionCheck()) {
            return
        }
        if (bluetoothGattCharacteristics?.size == 3) {
            // read characteristics
            for (characteristic in bluetoothGattCharacteristics) {
                bluetoothGatt?.readCharacteristic(characteristic)
                // this delay should be the same as the one set in the sensor device's main loop
                runDelay(250L)
            }
        }
    }

    private fun sendMessage(bluetoothSocket: BluetoothSocket?, messageToSend:String){
        connectionWatchCheck()
        var message = ""
        val sendMessage = messageToSend
        try{
            val outputStream: OutputStream = bluetoothSocket?.outputStream?:return
            outputStream.write(sendMessage.toByteArray())
            message = "送信成功"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
        }catch (e: IOException){
            e.printStackTrace()
            message = "送信失敗"
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getGattCharacteristics(bluetoothGattService: BluetoothGattService?): MutableList<BluetoothGattCharacteristic>? {
        // check the service
        if (bluetoothGattService == null) {
            return null
        }
        val bluetoothGattCharacteristics = bluetoothGattService.characteristics
        updateStatusView("$bluetoothGattCharacteristics")
        return bluetoothGattCharacteristics
    }

    // get services through a gatt connection
    private fun getGattServices(bluetoothGatt: BluetoothGatt?): BluetoothGattService? {
        // check and request for bluetooth permissions
        if (!permissionCheck()) {
            return null
        }
        // check the gatt
        if (bluetoothGatt == null) {
            return null
        }
        // begin service discovery
        bluetoothGatt.discoverServices()

        // update status text
        var message = "Discovering services..."
        updateStatusView(message)

        var bluetoothGattService: BluetoothGattService? = null
        // loop until result or timeout
        var timeoutCounter = 0
        while (timeoutCounter < 4) {
            // return the service if it is found
            bluetoothGattService = bluetoothGatt.getService(deviceSensorUuid)
            if (bluetoothGattService != null) {
                // update status text
                message = "Service found: $bluetoothGattService"
                updateStatusView(message)
                return bluetoothGattService
            }
            // if the service is not found, add to the timeoutCounter
            timeoutCounter ++
            // delay before starting the next iteration
            runDelay(500L)
        }
        // if timed out, update status text and return null
        message = "Service discovery timeout."
        updateStatusView(message)
        return null
    }

    // form a gatt connection with a peripheral Bluetooth Low Energy device
    private fun getGatt(bluetoothDevice: BluetoothDevice?): BluetoothGatt? {
        // check and request for bluetooth permissions
        if (!permissionCheck()) {
            return null
        }
        // check the bonded device
        if (bluetoothDevice == null) {
            return null
        }
        // connect to bonded device via GATT
        val bluetoothGatt = bluetoothDevice.connectGatt(
            this,
            false,
            bluetoothGattCallback)
        // raise toast to inform of established connection
        val message = "GATT connected with: ${bluetoothGatt.device}"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        // update status text
        updateStatusView(message)
        return bluetoothGatt
    }

    // get devices that have been bonded via Bluetooth Low Energy connection
    // returns BluetoothDevice if it's name is the same as deviceSensorName
    // returns false if the device could not be found
    // uses deviceSensorName
    private fun getBondedDevice(): BluetoothDevice? {
        // check and request for bluetooth permissions
        if (!permissionCheck()) {
            return null
        }
        // get bluetooth manager from system services
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        // get bluetooth adapter from bluetooth manager
        bluetoothAdapter = bluetoothManager?.adapter as BluetoothAdapter
        // check if bluetooth is disabled
        if (!bluetoothAdapter?.isEnabled!!) {
            val message = "Please enable Bluetooth."
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            // update status text
            updateStatusView(message)
            return null
        }
        // get bonded bluetooth devices as a set
        val bondedDevices = bluetoothAdapter?.bondedDevices
        // if bondedDevices is empty, there are no bonded devices to connect to
        if (bondedDevices!!.isEmpty()) {
            // raise toast message asking to bond to the sensor device
            val message = "Please bond via Bluetooth to: $deviceSensorName."
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            // update status text
            updateStatusView(message)
            return null
        }
        // check devices in bondedDevices for the sensor device
        for (device in bondedDevices) {
            val deviceName = device.name
            // return the device if its name is the same as deviceSensorName
            if (deviceName == deviceSensorName) {
                return device
            }
        }
        // if no correct device was found
        return null
    }

    // check permissions depending on Android version
    // uses permissionRequest()
    private fun permissionCheck(): Boolean {
        // return permissionGranted to show if permissions have been granted or not
        var permissionGranted = true
        // if current Android version is 12 or newer (API level 31 or more)
        var permissionArray = arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        // if current Android version is 11 or older (API level 30 or less)
        if (Build.VERSION.SDK_INT <= 30) {
            permissionArray = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        for (permission in permissionArray) {
            // check if permission is granted
            if (ContextCompat.checkSelfPermission(this,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                // if permission is not granted
                permissionGranted = false
                // update status text
                val message = "no $permission, requesting permission"
                updateStatusView(message)
                // request permission
                permissionRequest(permission)
                // check if permission was granted
                if (ContextCompat.checkSelfPermission(this,
                        permission) == PackageManager.PERMISSION_GRANTED) {
                    // if permission was granted
                    permissionGranted = true }
            }
        }
        if (permissionGranted == false) {
            updateStatusView("Permission is required for BLE connection.")
        }
        return permissionGranted
    }

    // function requests a permission that has been declared in the manifest
    // permission is Manifest.permission.BLUETOOTH_CONNECT for Android 13 and newer
    // permissions are Manifest.permission.BLUETOOTH and Manifest.permission.ACCESS_COARSE_LOCATION for Android 11 and older
    private fun permissionRequest(permission: String) {
        var requestCode = 101
        if (permission != Manifest.permission.ACCESS_COARSE_LOCATION) { requestCode = 102 }
        // request permission
        ActivityCompat.requestPermissions(this,
            arrayOf(permission), requestCode)
    }

    // update a TextView to display application status messages
    private fun updateStatusView(message: String) {
        // update status text
        val statusTextView = findViewById<TextView>(R.id.statusView)
        statusTextView.text = message
    }

    // convert byte array to float 32
    private fun byteArrayToFloat32(byteArray: ByteArray?): Float? {
        if (byteArray == null) {
            return null
        }
        val float32 = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).float
        return float32
    }

    // block the current thread for X milliseconds
    // for example: runDelay(1000L) blocks for one second
    private fun runDelay(timeMillis: Long) {
        // delay before starting the next iteration
        runBlocking { launch { delay(timeMillis) } }
    }
}