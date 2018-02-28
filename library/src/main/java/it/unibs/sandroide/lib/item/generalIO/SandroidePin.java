package it.unibs.sandroide.lib.item.generalIO;

import java.nio.ByteBuffer;
import java.util.Locale;

public class SandroidePin {

    public static final int PIN_MODE_DIGITAL_INPUT = 0;
    public static final int PIN_MODE_DIGITAL_OUTPUT = 1;
    public static final int PIN_MODE_ANALOG_INPUT = 2;
    public static final int PIN_MODE_PWMOUT = 3;
    public static final int PIN_MODE_SERVO = 4;

    private OnRawValueReceivedListener rawvalueListener;
    private OnValueReceivedListener valueListener;
    public SandroideDevice device;   // parent device
    public int pinNo = -1;       // not set
    private int mode = -1;       // not set
    private int group = 0;       // no group
    private int periodMs = -1;       // not set
    private int samplingInterval = -1;       // not set
    private double delta = -1; // default: if value changes by 1%, then it should by device
    private double dutyCicle = -1;

    public Object value = null;         // last received value

    // by creating a ping object we also ask the device to keep the pin value monitored and to inform whenever the value changes
    public SandroidePin setDevice(SandroideDevice device, int pinNo) {
        if (device!=null) {
            this.pinNo = pinNo;
            this.device = device;
            device.attachPin(this);
        }
        return this;
    }

    public SandroideDevice getDevice() {
        return device;
    }


    public int getMode() {
        return mode;
    }

    public boolean isOutput() {return mode==PIN_MODE_DIGITAL_OUTPUT;}

    public SandroidePin setMode() {
        return setMode(this.mode);
    }

    public SandroidePin setMode(int mode) {
        return setMode(mode, this.group);             // TODO message to set pin mode:    input, output, analog, pwmout
    }

    public SandroidePin setMode(int mode, int group) {
        // in devices.xml era: mode_model_0   mode_model_1  ... ecc..
        // S|pin number|mode (0:input ... 4:servo)
        this.mode = mode;
        this.group = group;
        if (device.isDeviceControlConnected()) {
            byte[] towrite = new byte[]{'S', (byte) this.pinNo, (byte) mode, (byte) group};
            this.device.runAction(towrite);
        }

        return this;             // TODO message to set pin mode:    input, output, analog, pwmout
    }


    public double getDutyCicle() {
        return dutyCicle;
    }

    public SandroidePin setDutyCicle() {
        if (dutyCicle>=0) return setDutyCicle(dutyCicle);
        return this;
    }

    public SandroidePin setDutyCicle(double dutyCicle) {
        //device.sendMessage(); // TODO message to set duty cicle
        return this;
    }

    public int getPeriodMs() {
        return periodMs;
    }

    public SandroidePin setPeriodMs() {
        if (periodMs>=0) return setPeriodMs(periodMs);
        return this;
    }

    public SandroidePin setPeriodMs(int periodms) {  // set period in ms
        //device.sendMessage(); // TODO message to set period
        return this;
    }

    public double getDelta() {
        return delta;
    }

    public SandroidePin setDeltaThreshold() {
        if (delta>=0) return setDeltaThreshold(delta);
        return this;
    }

    public SandroidePin setDeltaThreshold(double delta) {
        // this is a threshold expressed as percentage [0-1], to consider a value as changed.
        // This thresold is sent to the device so it can know if the value is changed and thus needs to be sent
        // e.g. if working with [0-5V] devices, Setting this thresold to 0.2, means that the pin is considered changed
        // when the new value is changed(increased or decreased) of more than 20% of 5V, that is the new Voltage is at least +-1V from the old value

        this.delta = delta;
        if (device.isDeviceControlConnected()) {

            byte[] towrite = String.format(Locale.ENGLISH,"D%c%f",this.pinNo, this.delta).getBytes();
            //byte[] towrite = new byte[]{'D', (byte) this.pinNo, (byte) mode};
            this.device.runAction(towrite);
        }

        return this;
    }

    public double getSamplingInterval() {
        return this.samplingInterval;
    }

    public SandroidePin setSamplingInterval() {
        if (samplingInterval>=0) return setSamplingInterval(samplingInterval);
        return this;
    }

    public SandroidePin setSamplingInterval(int samplingInterval) {
        // we use timeslots instead of ms, for performance optimization
        // on the device, instead of polling each pins every millisecond, we can poll the value only on specified timeslots

        this.samplingInterval = samplingInterval;
        if (device.isDeviceControlConnected()) {
            byte[] towrite = String.format(Locale.ENGLISH,"I%c%d",this.pinNo, this.samplingInterval).getBytes();
            //byte[] towrite = String.format("I%c%d",this.pinNo,this.samplingInterval).getBytes();
            //byte[] towrite = new byte[]{'D', (byte) this.pinNo, (byte) mode};
            this.device.runAction(towrite);
        }

        return this;
    }


    public Object fetchValue() {
        // in devices.xml era: get_digital_value_0   set_model_1  ... ecc..
        // G|pin number

        //  era get_mode_0
        //  M | pin number
        switch (value.getClass().getName()) {
            //case "";
        }
        //device.sendMessage(); // TODO message to set pin value
        return this;
    }

    public SandroidePin setValue() {
        return this.setValue(this.value);
    }

    public SandroidePin setValue(Object value) {
        // in devices.xml era: set_model_0   set_model_1  ... ecc..
        // T|pin number|value
        if (value!=null) {
            this.value = value;
            if (device.isDeviceControlConnected()) {
                switch (value.getClass().getSimpleName().toLowerCase()) {
                    case "boolean":
                        byte[] towrite1 = String.format(Locale.ENGLISH, "T%c%c", this.pinNo, ((boolean)this.value)?1:0).getBytes();
                        this.device.runAction(towrite1);
                        break;
                    case "float":
                        byte[] towrite2 = String.format(Locale.ENGLISH, "T%c%c", this.pinNo, ((float)this.value)==0?0:1).getBytes();
                        this.device.runAction(towrite2);
                        break;
                    case "integer":
                        byte[] towrite3 = String.format(Locale.ENGLISH, "T%c%c", this.pinNo, ((int)this.value)==0?0:1).getBytes();
                        this.device.runAction(towrite3);
                        break;

                }
                //byte[] towrite = String.format("I%c%d",this.pinNo,this.samplingInterval).getBytes();
                //byte[] towrite = new byte[]{'D', (byte) this.pinNo, (byte) mode};
            }
        }

        //device.sendMessage(); // TODO message to set pin value
        return this;
    }

    public Object getValue() {
        return value;
    }

    public boolean getBoolValue() {
        switch (value.getClass().getSimpleName().toLowerCase()) {
            case "boolean":
                return (boolean)value;
            case "float":
                return (float)value != 0;
            case "integer":
                return (int)value != 0;

        }
        return false;
    }

    protected void receivedValue(Object value) {
        Object oldvalue = this.value;
        this.value = value;
        if (valueListener!=null) {
            this.valueListener.onEvent(value,oldvalue);
        } else if (rawvalueListener!=null) {
            this.rawvalueListener.onEvent(value,oldvalue,this,device);
        }
    }


    public SandroidePin setOnValueReceived(OnValueReceivedListener listener) {
        this.valueListener = listener;
        return this;
    }

    public SandroidePin setOnRawValueReceived(OnRawValueReceivedListener listener) {
        this.rawvalueListener = listener;
        return this;
    }

    /**
     * Interface definition for a callback to be invoked when a value is received.
     */
    public interface OnValueReceivedListener {
        void onEvent(Object newValue, Object oldValue);
    }

    /**
     * Interface definition for a callback to be invoked when a value is received.
     */
    public interface OnRawValueReceivedListener {
        void onEvent(Object newValue, Object oldValue, SandroidePin pin, SandroideDevice device);
    }

}
