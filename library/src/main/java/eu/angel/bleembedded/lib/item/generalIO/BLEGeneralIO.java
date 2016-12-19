/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini and Angelo Vezzoli, All rights reserved.
 *
 * @author  Angelo Vezzoli
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
package eu.angel.bleembedded.lib.item.generalIO;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.angel.bleembedded.lib.data.BLEDeviceData;
import eu.angel.bleembedded.lib.data.ParsingComplements;
import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.BLEOnItemInitiatedListener;
import eu.angel.bleembedded.lib.item.BLEOnItemUpdateListener;
import eu.angel.bleembedded.lib.item.Bleresource;

/**
 * The class which implements the BLEGeneralIO resources (it's an actuator/sensor/button).
 */
public class BLEGeneralIO extends BLEItem {

    private final static String TAG="BLEGeneralIO";
    /*
    * GENERAL_IO status
    * */
    public static final int GENERAL_IO_DI = 0;
    public static final int GENERAL_IO_DO = 1;
    public static final int GENERAL_IO_AI = 2;
    public static final int GENERAL_IO_PWM = 3;
    public static final int GENERAL_IO_SERVO = 4;
    public static final int GENERAL_IO_DIO = 5;
    public static final int GENERAL_IO_UNDETERMINATED_STATUS = 6;

    /*
    * GENERAL_IO events
    * */
    public static final int GENERAL_IO_SET_STATUS = 0;
    public static final int GENERAL_IO_GET_VALUE = 1;
    public static final int GENERAL_IO_SET_VALUE = 2;
    public static final int GENERAL_IO_START = 3;
    public static final int GENERAL_IO_STOP = 4;
    public static final int GENERAL_IO_SET_PIN_CAPABILITIES = 5;
    public static final int GENERAL_IO_SET_PWM_VALUE = 6;
    public static final int GENERAL_IO_SET_SERVO_VALUE = 7;
    public static final int GENERAL_IO_SET_DIGITAL_VALUE = 8;

    public int getStatus() {
        return status;
    }

    private int status=0;
    private int pin_capabilities=0;

    private BLEOnGeneralIOEventListener bleOnGeneralIOEventListenerToApp;

    private BLEOnGeneralIOEventListener bleOnGeneralIOEventListener=
            new BLEOnGeneralIOEventListener() {

        @Override
        public void onBoardInitEnded() {
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onBoardInitEnded();
        }

        @Override
        public void onDigitalInputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
            int new_status=(int)bleGeneralIOEvent.values[0];
            if (((new_status!=status)&&(new_status!=GENERAL_IO_UNDETERMINATED_STATUS)))
                status=new_status;
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onDigitalInputValueChanged(bleGeneralIOEvent);
        }

        @Override
        public void onAnalogValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
            int new_status=(int)bleGeneralIOEvent.values[0];
            if (((new_status!=status)&&(new_status!=GENERAL_IO_UNDETERMINATED_STATUS)))
                status=new_status;
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onAnalogValueChanged(bleGeneralIOEvent);
        }

        @Override
        public void onDigitalOutputValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
            int new_status=(int)bleGeneralIOEvent.values[0];
            if (((new_status!=status)&&(new_status!=GENERAL_IO_UNDETERMINATED_STATUS)))
                status=new_status;
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onDigitalOutputValueChanged(bleGeneralIOEvent);
        }

        @Override
        public void onServoValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
            int new_status=(int)bleGeneralIOEvent.values[0];
            if (((new_status!=status)&&(new_status!=GENERAL_IO_UNDETERMINATED_STATUS)))
                status=new_status;
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onServoValueChanged(bleGeneralIOEvent);
        }

        @Override
        public void onPWMValueChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
            int new_status=(int)bleGeneralIOEvent.values[0];
            if (((new_status!=status)&&(new_status!=GENERAL_IO_UNDETERMINATED_STATUS)))
                status=new_status;
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onPWMValueChanged(bleGeneralIOEvent);
        }

        @Override
        public void onGeneralIOStatusChanged(BLEGeneralIOEvent bleGeneralIOEvent) {
            status=(int)bleGeneralIOEvent.values[0];
            if (bleOnGeneralIOEventListenerToApp!=null)
                bleOnGeneralIOEventListenerToApp.onGeneralIOStatusChanged(bleGeneralIOEvent);
        }

        @Override
        public void onSetGeneralIOParameter(BLEGeneralIOEvent bleGeneralIOEvent) {

            switch(bleGeneralIOEvent.event)
            {
                case GENERAL_IO_SET_PIN_CAPABILITIES:
                    pin_capabilities=(int) bleGeneralIOEvent.values[0];
                    break;

                default:
                    break;
            }
        }
    };

    private boolean generalIOEventListeredCalled = false;
    private int generalIOnumber=0;

    public int getGeneralIOnumber()
    {
        return generalIOnumber;
    }

    /**
     * Constructor of the {@link BLEGeneralIO} class.
     * @param name the name of the device to connect with (the name identifies exactly the
     *                   remote device anf the firmware version).
     * @param bleresource {@link Bleresource} which defines the Item.
     */
    public BLEGeneralIO(String name, Bleresource bleresource){
        super(BLEItem.TYPE_GENERALIO, name, bleresource);
        generalIOnumber=bleresource.getCardinality();
    }


    /**
     * Set the status of the {@link BLEGeneralIO}
     * (GENERAL_IO_DI, GENERAL_IO_DO,
     * GENERAL_IO_AI, GENERAL_IO_AO, GENERAL_IO_SERVO),
     * if the {@link BLEGeneralIO}
     * has such state available.
     * <p>The response of the setting action will be pointed out
     * by the callback
     * {@link BLEOnGeneralIOEventListener#onGeneralIOStatusChanged}
     *
     * @param status the status to be set
     */
    public void setStatus(int status)
    {
        List<Float> input=new ArrayList<>();
        input.add((float)status);
        Log.d(TAG,bleresource.getDevItem()+": Action: set_mode");
        switch(status){

            case GENERAL_IO_DI:
                if(runAction("set_di_mode", input)) {
                    this.status = status;
                    return;
                }
                break;
            case GENERAL_IO_DO:
                if(runAction("set_do_mode", input)) {
                    this.status = status;
                    return;
                }
                break;
            case GENERAL_IO_AI:
                if(runAction("set_ai_mode", input)) {
                    this.status = status;
                    return;
                }
                break;
            case GENERAL_IO_PWM:
                if(runAction("set_pwm_mode", input)) {
                    this.status = status;
                    return;
                }
                break;
            case GENERAL_IO_SERVO:
                if(runAction("set_servo_mode", input)) {
                    this.status = status;
                    return;
                }
                break;
            case GENERAL_IO_DIO:
                if(runAction("set_dio_mode", input)) {
                    this.status = status;
                    return;
                }
                break;
            case GENERAL_IO_UNDETERMINATED_STATUS:
                if(runAction("set_undeterminated_mode", input)) {
                    this.status = status;
                    return;
                }
                break;

        }
        runAction("set_mode", input);
        this.status=status;
    }

    /**
     * Set the value of the {@link BLEGeneralIO},
     * if the {@link BLEGeneralIO}
     * is in output state.
     * <p>The response of the setting action will be pointed out
     * by the callbacks
     * {@link BLEOnGeneralIOEventListener#onDigitalInputValueChanged} or
     * {@link BLEOnGeneralIOEventListener#onAnalogValueChanged}
     * related to the status of the {@link BLEGeneralIO}
     *
     * @param isHigh if TRUE the output is set high if false it is set low
     * @return whether the action was successful related
     * to the status of of the {@link BLEGeneralIO}
     */
    public boolean setDigitalValueHigh(boolean isHigh)
    {
        if (status==GENERAL_IO_DO)
        {
            float value= isHigh ? 1 : 0;
            List<Float> input=new ArrayList<>();
            input.add(value);
            Log.d(TAG, bleresource.getDevItem()+": Action: set_do_value");
            runAction("set_do_value", input);
            return true;

        }
        else
            return false;
    }

    /**
     * Get the value of the {@link BLEGeneralIO},
     * if the {@link BLEGeneralIO}
     * is in input state.
     * <p>The response of the setting action will be pointed out
     * by the callbacks
     * {@link BLEOnGeneralIOEventListener#onDigitalInputValueChanged} or
     * {@link BLEOnGeneralIOEventListener#onAnalogValueChanged}
     * related to the status of the {@link BLEGeneralIO}
     *
     * @return whether the action was successful related
     * to the status of of the {@link BLEGeneralIO}
     */
    public boolean getDigitalValue()
    {
        if ((status==GENERAL_IO_AI)&&(status==GENERAL_IO_DI))
        {
            List<Float> input=new ArrayList<>();
            Log.d(TAG, bleresource.getDevItem()+": Action: getDigitalValue");
            runAction("get_digital_value", input);
            return true;
        }
        else
            return false;
    }

    /**
     * Get the mode of the {@link BLEGeneralIO}.
     * <p>The response of the setting action will be pointed out
     * by the callback
     * {@link BLEOnGeneralIOEventListener#onGeneralIOStatusChanged(BLEGeneralIOEvent)} or
     */
    public void getMode()
    {
        List<Float> input=new ArrayList<>();
        Log.d(TAG, bleresource.getDevItem()+": Action: get_mode");
        runAction("get_mode", input);
    }

    /**
     * set {@link BLEOnGeneralIOEventListener} of this {@link BLEGeneralIO}
     */
    public void setOnGeneralIOEventListener
    (BLEOnGeneralIOEventListener bleOnGeneralIOEventListener)
    {
        this.bleOnGeneralIOEventListenerToApp=bleOnGeneralIOEventListener;
        generalIOEventListeredCalled=true;
        initOnGeneralIOEventListener();
    }

    /**
     * init the {@link BLEGeneralIO} setting the {@link BLEOnItemUpdateListener}
     */
    void initOnGeneralIOEventListener()
    {
        if (mDeviceControl!=null)
        {
            setBleItemListeners(new BLEOnItemUpdateListener() {
                @Override
                public void onItemUpdate(BLEDeviceData[] data) {
                    if (isItemInitiated){
                        //TODO: WARN about the position of the mode data: sensor data is handled related to the mode,
                        // thus whether the mode is changed
                        // before or after change the behaviour of the GPIO
                    for (BLEDeviceData deviceData : data) {
                        switch(deviceData.getData_type()){
                            case ParsingComplements.DT_GPIO_MODE:
                                handleGpioModeData(deviceData);
                                break;

                            default:
                                handleGpioData(deviceData);
                                break;
                        }
                    }}
                }
            }, new BLEOnItemInitiatedListener() {
                @Override
                public void onItemInitiated() {
                    onBoardInitiated();
                    isItemInitiated=true;
                }
            });
            initItem();
        }
    }

    /**
     * Handles the gpio_mode {@link BLEDeviceData} and passes that through the
     * {@link BLEOnGeneralIOEventListener#onGeneralIOStatusChanged} callback
     */
    private void handleGpioModeData(BLEDeviceData bleDeviceData){
        Log.d(TAG, "IO: "+bleresource.getDevItem()+" status: "+status+", val: "+bleDeviceData.getValue());
        Log.d(TAG, "__D Modedata: "+bleDeviceData);
        if (status!=bleDeviceData.getValue()){
            status=(int)bleDeviceData.getValue();
            if (bleOnGeneralIOEventListenerToApp!=null) {
                bleOnGeneralIOEventListenerToApp.onGeneralIOStatusChanged
                        (new BLEGeneralIOEvent(new float[]{status},
                        BLEGeneralIO.GENERAL_IO_SET_STATUS, this,0, 0));
            }
        }
    }

    /**
     * Handles the gpio_data {@link BLEDeviceData} and passes that through the
     * callback related to the type of the {@link BLEDeviceData}
     */
    private void handleGpioData(BLEDeviceData bleDeviceData){
        Log.d(TAG, "__D handledata: "+bleDeviceData);
        switch (status){

            case BLEGeneralIOComplements.INPUT:
                if ((bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_DI)||
                        ((bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_DIO))){
                    if (bleOnGeneralIOEventListenerToApp!=null)
                        bleOnGeneralIOEventListenerToApp.onDigitalInputValueChanged
                                (new BLEGeneralIOEvent(new float[]{(float) status, bleDeviceData.getValue()},
                                        BLEGeneralIO.GENERAL_IO_SET_VALUE, this,0, 0));
                }
                break;


            case BLEGeneralIOComplements.OUTPUT:
                if ((bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_DO||
                        ((bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_DIO)))){
                    if (bleOnGeneralIOEventListenerToApp!=null)
                        bleOnGeneralIOEventListenerToApp.onDigitalOutputValueChanged
                                (new BLEGeneralIOEvent(new float[]{(float) status, bleDeviceData.getValue()},
                                        BLEGeneralIO.GENERAL_IO_SET_VALUE, null,0, 0));
                }
                break;

            //verificare la conversione del dato analog
            case BLEGeneralIOComplements.ANALOG:
                if (bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_AI){
                    if (bleOnGeneralIOEventListenerToApp!=null)
                        bleOnGeneralIOEventListenerToApp.onAnalogValueChanged(new BLEGeneralIOEvent
                                (new float[]{(float) status, (float) ((status >> 4) << 8) + bleDeviceData.getValue()},
                                        BLEGeneralIO.GENERAL_IO_SET_VALUE, this,0, 0));
                }
                break;


            case BLEGeneralIOComplements.PWM:
                if (bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_PWM){
                    if (bleOnGeneralIOEventListenerToApp!=null)
                        bleOnGeneralIOEventListenerToApp.onPWMValueChanged(new BLEGeneralIOEvent
                                (new float[]{(float) status, bleDeviceData.getValue()},
                                        BLEGeneralIO.GENERAL_IO_SET_VALUE, null,0, 0));
                }
                break;

            case BLEGeneralIOComplements.SERVO:
                if (bleDeviceData.getData_type()==ParsingComplements.DT_GPIO_SERVO){
                    if (bleOnGeneralIOEventListenerToApp!=null)
                        bleOnGeneralIOEventListenerToApp.onServoValueChanged(new BLEGeneralIOEvent
                                (new float[]{(float) status, bleDeviceData.getValue()},
                                        BLEGeneralIO.GENERAL_IO_SET_VALUE, null,0, 0));
                }
                break;

        }
    }

    public void onBoardInitiated(){
        bleOnGeneralIOEventListenerToApp.onBoardInitEnded();
    }

    /**
     * Overrides the initialization of the {@link eu.angel.bleembedded.lib.device.DeviceControl}
     */
    @Override
    protected void initDeviceControl()
    {
        super.initDeviceControl();
        if (generalIOEventListeredCalled)
            initOnGeneralIOEventListener();
    }

    /**
     * Set the value of the {@link BLEGeneralIO},
     * if the {@link BLEGeneralIO}
     * is in PWM state.
     * <p>The response of the setting action will be pointed out
     * by the callbacks
     * {@link BLEOnGeneralIOEventListener#onPWMValueChanged}
     * related to the status of the {@link BLEGeneralIO}
     *
     * @param value set the value of the PWM from 0 to 100 (min to max)
     * @return whether the action was successful related
     * to the status of of the {@link BLEGeneralIO}
     */
    public boolean setPWMValue(float value)
    {
        //if (status==GENERAL_IO_PWM)
        //{
            List<Float> input=new ArrayList<>();
            input.add(value);
            Log.d(TAG,bleresource.getDevItem()+": Action: set_pwm_value to "+value);
            runAction("set_pwm_value", input);
            return true;
//        }
//        else
//            return false;
    }

    /**
     * Set the value of the {@link BLEGeneralIO},
     * if the {@link BLEGeneralIO}
     * is in Servo state.
     * <p>The response of the setting action will be pointed out
     * by the callbacks
     * {@link BLEOnGeneralIOEventListener#onServoValueChanged}
     * related to the status of the {@link BLEGeneralIO}
     *
     * @param value set the value of the Servo from 0 to 100 (min to max)
     * @return whether the action was successful related
     * to the status of of the {@link BLEGeneralIO}
     */
    public boolean setServoValue(float value)
    {
//        if (status==GENERAL_IO_SERVO)
//        {
            List<Float> input=new ArrayList<>();
            input.add(value);
            Log.d(TAG,bleresource.getDevItem()+": Action: set_servo_value to "+value);
            runAction("set_servo_value", input);
            return true;

//        }
//        else
//            return false;
    }

    //TODO maybe could be useful introducing a 'disable method' to interrupt the data flow, without destroy the Item connection with the device
    //TODO: remove GPIO (the gpio has to be removed from init and the related callback has to be removed
    // from the Cluster (I guess))

}
