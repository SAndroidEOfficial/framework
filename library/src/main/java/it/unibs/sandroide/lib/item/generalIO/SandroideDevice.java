/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package it.unibs.sandroide.lib.item.generalIO;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import it.unibs.sandroide.lib.communication.BluetoothLeDevice;
import it.unibs.sandroide.lib.data.BLEDeviceData;
import it.unibs.sandroide.lib.device.DeviceControl;
import it.unibs.sandroide.lib.item.BLEItem;
import it.unibs.sandroide.lib.item.BLEOnItemInitiatedListener;
import it.unibs.sandroide.lib.item.BLEOnItemUpdateListener;
import it.unibs.sandroide.lib.item.Bleresource;

/**
 * The class which implements the BLEGeneralIO resources (it's an actuator/sensor/button).
 */
public class SandroideDevice extends BLEItem {
    private final static String TAG="SandroideDevice";

    private final static int ANALOG_MAX_VALUE = 1024;

    protected Map<Integer, SandroidePin> pins = new HashMap<Integer,SandroidePin>();
    protected OnDeviceConnectedListener bleOnDeviceConnectedListenerListener;
    protected OnMessageReceivedListener customOnMessageReceivedListener;

    private int devDummy = 0;

    public int getDevDummy(){
        return devDummy;
    }

    public SandroideDevice setDevDummy(int dummy) {
        this.devDummy = dummy;
        return this;
    }

    /**
     * Constructor of the {@link SandroideDevice} class.
     * @param name the name of the device to connect with (the name identifies exactly the
     *                   remote device anf the firmware version).
     * @param bleresource {@link Bleresource} which defines the Item.
     */
    public SandroideDevice(String name, Bleresource bleresource){
        super(BLEItem.TYPE_DEVICEIO, name, bleresource);
    }

    protected void sendMessage(String msg) {
        //this.mDeviceControl.writeChar(this.mDeviceControl.);
        //device
        //this.run
        //this.runA
        //
        //runAction(msg)
    }

    //@Override
    synchronized protected void runAction(byte[] towrite) {
        List input = new ArrayList<Float>();
        for(int i=0;i<towrite.length;i++) {
            input.add(new Float(towrite[i]));
            //input.addAll(new ArrayList<byte>(towrite));
        }
        //input) float
        //towrite
        runAction("send_string_value",input);
/*
        DeviceControl deviceControl = this.mDeviceControl;
        //BluetoothLeDevice mBluetoothLeDevice = deviceControl.mBluetoothLeDevice;
        //mServices =

        try {
            boolean lockGot;
            do{
                lockGot=deviceControl.getLock().tryLock(100, TimeUnit.MILLISECONDS);
            }
            while (!lockGot);
            try{
                int delay=0;
                //BluetoothGatt mBluetoothGatt = mBluetoothLeDevice.getBluetoothGatt();
                deviceControl.writeChar("713d0000-503e-4c75-ba94-3148f18d941e","713d0003-503e-4c75-ba94-3148f18d941e",towrite);
                //BluetoothGattCharacteristic gattCharacteristic = mServices.get(service).get(characteristic);
                //gattCharacteristic.setValue(command);
                //boolean a=mBluetoothGatt.writeCharacteristic(gattCharacteristic);
                //Log.d(TAG, "writing was " + a);
                //Log.d(TAG, "byteArray was " + Arrays.toString(command));
                //Delay(20);
            } finally {
                deviceControl.getLock().unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }

    public SandroideDevice setCustomOnMessageReceived(OnMessageReceivedListener listener) {
        this.customOnMessageReceivedListener = listener;
        return this;
    }

    protected void jsonReceived(JSONObject obj) {
        // TODO: Json handling
        int pinNo = obj.optInt("pin",-1);
        SandroidePin pin = pins.containsKey(pinNo) ? (SandroidePin) pins.get(pinNo) : null;
        if (pin!=null) {
            double val = obj.optDouble("v", -1);
            if (val >= 0) {
                pin.receivedValue(val);
            }
        }
        /*String action = obj.optString("action",null);
        switch(action) {
            case "config":
                    // TODO
                break;
        }*/

    }

    public void msgReceived(String msg) {
        msgReceived(msg.getBytes());
    }

    public void msgReceived(byte[] msg) {

        // if pin is set then we must pass the message to the right pin, if pin is omitted then this is a device-wide message
        //String msg2 = "J{\"pin\":2,\"cmd\":\"read\",value:3}";
        //Log.d(TAG,"Message arrived");

        boolean defaultHandling = true;
        if (customOnMessageReceivedListener !=null) {
            defaultHandling = customOnMessageReceivedListener.onEvent(msg);
        }

        if (defaultHandling) {  // default handling is not executed if customMessageReceived says to not process further
            if (msg.length>0) {
                byte cmd = msg[0]; // first byte is the command
                switch(cmd) {
                    case 'G': // these commands are pin commands, second byte is pin number
                        int i=1;
                        while(i+2 < msg.length) {
                            SandroidePin pin = pins.containsKey((int)msg[i]) ? (SandroidePin) pins.get((int)msg[i]) : null;
                            if (pin != null) {
                                i++;
                                while (i+1<msg.length) {
                                    pin.receivedValue(ByteBuffer.wrap(msg).getShort(i) / (float) ANALOG_MAX_VALUE);
                                    i += 2;
                                }
                            }
                            i+=3;
                        }
                        break;
                    /*case 'M': // at least 3 bytes: command, pin number, data
                        if (msg.length<=2) throw new RuntimeException("Command '%c' must be at least 3 characters long");
                        int pinNo = msg[1];
                        if (pinNo >= 0) { // if pin is set then we must pass the message to the right pin
                            SandroidePin pin = pins.containsKey(pinNo) ? (SandroidePin) pins.get(pinNo) : null;
                            if (pin != null) {
                                switch(cmd) {
                                    case 'G':
                                        pin.receivedValue(ByteBuffer.wrap(msg).getShort(2) / (float)ANALOG_MAX_VALUE);

                                        break;
                                    case 'M': //pin.receivedValue();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else { // this is a device-wide message
                            Log.w(TAG,String.format("Unknown pin message: %s", msg));
                        }
                        break;*/

                    //
                    // add more case here to add extra commands
                    // case 'S':

                    case '{': // { means that what follows is a JSON string
                        try {
                            Log.d(TAG,new String(msg));
                            jsonReceived(new JSONObject(new String(msg)));
                         } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    default: // is a JSON string
                        Log.w(TAG,String.format("Unknown device message: %s", new String(msg)));
                        break;
                }
            }
        }
    }

    public SandroidePin getPin(int pinNo) {
        if (pins.containsKey(pinNo)) return pins.get(pinNo);
        return null;
    }

    public SandroideDevice attachPin(SandroidePin pin) {
        if (pin==null) throw new RuntimeException("Error calling setPin! Pin object cannot be null!");
        if (!this.pins.containsKey(pin.pinNo)) this.pins.put(pin.pinNo,pin);
        if (pin.getDevice()==null) pin.setDevice(this, pin.pinNo);

        return this;
    }

    public SandroideDevice setOnDeviceConnected(OnDeviceConnectedListener listener) {
        this.bleOnDeviceConnectedListenerListener = listener;
        return this;
    }

    /**
     * Overrides the initialization of the {@link it.unibs.sandroide.lib.device.DeviceControl}
     */
    @Override
    protected void initDeviceControl()
    {
        final SandroideDevice self = this;
        super.initDeviceControl();
        if (mDeviceControl!=null)
        {
            setBleItemListeners(new BLEOnItemUpdateListener() {
                @Override
                public void onItemUpdate(BLEDeviceData[] data) {
                    if (isItemInitiated){
                        // NOT DEFINED HERE: msgReceived is called directly from DeviceControl on ACTION_DATA_AVAILABLE
                    }
                }
            }, new BLEOnItemInitiatedListener() {
                @Override
                public void onItemInitiated() {

                    for (SandroidePin p : pins.values()){
                        // this will send pin parameters to device even if they have been set before bluetooth connection
                        p.setMode();
                        if (p.isOutput()) p.setValue();
                        p.setSamplingInterval();
                        p.setDeltaThreshold();
                        p.setDutyCicle();
                        p.setPeriodMs();
                    }

                    if (bleOnDeviceConnectedListenerListener !=null) bleOnDeviceConnectedListenerListener.onEvent(self);
                    isItemInitiated=true;
                }
            });
            initItem();
        }
    }

    /**
     * Interface definition for a callback to be invoked when a message is received.
     */
    public interface OnMessageReceivedListener {
        // return true to allow default handling, false if message has been already handled by user defined callback
        boolean onEvent(byte[] value);
    }

    /**
     * Interface definition for a callback to be invoked when a message is received.
     */
    public interface OnDeviceConnectedListener {
        // return true to allow default handling, false if message has been already handled by user defined callback
        void onEvent(SandroideDevice device);
    }

}
