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
package it.unibs.sandroide.lib.device.action;

import android.util.Log;

import java.util.List;

import it.unibs.sandroide.lib.device.DeviceControl;
import it.unibs.sandroide.lib.device.read.BLEReadableCharacteristic;

/**
 * Class for handling the single sequence element which together with the others
 * makes the {@link BLEAction}
 */
public class BLESequenceElement {

    private static final String TAG="BLESequenceElement";

    private final static int WRITE_CHAR=0;
    private final static int START_NOTIFY_CHAR=1;
    private final static int STOP_NOTIFY_CHAR=2;
    private final static int START_FAKE_NOTIFY_CHAR=3;
    private final static int READ_CHAR=4;
    private final static int IDLE=5;


    public final static String WRITE_CHAR_STRING="write";
    public final static String START_NOTIFY_CHAR_STRING="start_notify";
    public final static String STOP_NOTIFY_CHAR_STRING = "stop_notify";
    public final static String START_FAKE_NOTIFY_CHAR_STRING="start_fake_notify";
    public final static String READ_CHAR_STRING="read";
    public final static String IDLE_STRING="idle";

    final private int type;
    final private String modelWriteChar;
    final private BLEWritableCharacteristic bleWritableCharacteristic;
    final private int post_delay;
    final private BLEReadableCharacteristic bleReadableCharacteristic;

    /**
     * Constructor
     * @param type the type of the {@link BLESequenceElement}, which defines its behaviour
     * @param modelWriteChar the model of the message used by the {@link BLEWritableCharacteristic}
     *                       if the {@link BLESequenceElement} requires to write a characteristic
     * @param bleWritableCharacteristic the {@link BLEWritableCharacteristic} used
     *                       if the {@link BLESequenceElement} requires to write a characteristic
     * @param post_delay the delay required by the {@link BLESequenceElement} before the execution of
     *                   another {@link BLESequenceElement}
     * @param bleReadableCharacteristic the {@link BLEReadableCharacteristic} used
     *                       if the {@link BLESequenceElement} requires to read or start the
     *                                  notification of a characteristic
     */
    private BLESequenceElement(int type, String modelWriteChar,
                               BLEWritableCharacteristic bleWritableCharacteristic,
                               int post_delay, BLEReadableCharacteristic bleReadableCharacteristic){
        this.type=type;
        this.modelWriteChar=modelWriteChar;
        this.bleWritableCharacteristic=bleWritableCharacteristic;
        this.post_delay=post_delay;
        this.bleReadableCharacteristic=bleReadableCharacteristic;
    }


    public int getType() {
        return type;
    }

    public String getModelWriteChar() {
        return modelWriteChar;
    }

    public int getPost_delay() {
        return post_delay;
    }

    /**
     * Gets the {@link BLESequenceElementInterface} relating to the type of the {@link BLESequenceElement}.
     * The {@link BLESequenceElementInterface} is used to execute the action of the {@link BLESequenceElement}
     * @return
     */
    public BLESequenceElementInterface getBleSequenceElementInterface()
    {
        Log.d(TAG, "type: "+type+", wrCh: "+bleWritableCharacteristic+", model: "+modelWriteChar+", pd: "+post_delay);
        switch (type){

            case WRITE_CHAR:
                //TODO throw exception if modelWriteChar or bleWritableChar are null
                return new BLESequenceElementInterface() {
                    @Override
                    public void sequenceElementFunction(DeviceControl deviceControl,
                                                       List<Float> input) {
                        StringBuilder sb = new StringBuilder();
                        if(input!=null){
                            for (Float s : input)
                            {
                                sb.append(Float.toString(s));
                                sb.append("\t");
                            }
                        }
                        Log.d(TAG, "ser: "+bleWritableCharacteristic.getService_name()+", " +
                                "char: "+bleWritableCharacteristic.getName()+", input: "
                                +sb.toString());
                        deviceControl.writeChar(bleWritableCharacteristic.getService_name(),
                                bleWritableCharacteristic.getName(),
                                bleWritableCharacteristic.getModel(modelWriteChar).getMessage(input));
                    }
                };

            case READ_CHAR:
                //TODO throw exception if  bleReadableChar is null
                return new BLESequenceElementInterface() {
                    @Override
                    public void sequenceElementFunction(DeviceControl deviceControl,
                                                        List<Float> input) {
                        //TODO: pass directly the characteristic
                        Log.d(TAG, "ser: "+bleReadableCharacteristic.getService_name()+", " +
                                "char: "+bleReadableCharacteristic.getName());
                        deviceControl.readChar(bleReadableCharacteristic.getService_name(),
                                bleReadableCharacteristic.getName());
                    }
                };

            case START_NOTIFY_CHAR:
                //TODO throw exception if  bleReadableChar is null
                return new BLESequenceElementInterface() {
                    @Override
                    public void sequenceElementFunction(DeviceControl deviceControl,
                                                       List<Float> input) {
                        deviceControl.startCharacteristicNotification
                                (bleReadableCharacteristic.getService_name(),
                                bleReadableCharacteristic.getName());
                    }
                };
            case STOP_NOTIFY_CHAR:
                //TODO throw exception if  bleReadableChar is null
                return new BLESequenceElementInterface() {
                    @Override
                    public void sequenceElementFunction(DeviceControl deviceControl,
                                                        List<Float> input) {
                        deviceControl.stopCharacteristicNotification
                                (bleReadableCharacteristic.getService_name(),
                                        bleReadableCharacteristic.getName());
                    }
                };



            case START_FAKE_NOTIFY_CHAR:
                //TODO throw exception if  bleReadableChar is null
                return new BLESequenceElementInterface() {
                    @Override
                    public void sequenceElementFunction(DeviceControl deviceControl,
                                                        List<Float> input) {
                        deviceControl.startFakeCharacteristicNotification
                                (bleReadableCharacteristic.getService_name(),
                                        bleReadableCharacteristic.getName());
                    }
                };

            case IDLE:
                return new BLESequenceElementInterface() {
                    @Override
                    public void sequenceElementFunction(DeviceControl deviceControl,
                                                       List<Float> input) {
                        try {
                            Thread.sleep(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };

            default:
                //TODO generic Element matching with custom string id
                //TODO throw exception if no sequence Element matching type
                return null;

        }
    }


    private static int typeStringToInt(String type){
        switch (type){

            case WRITE_CHAR_STRING:
                return WRITE_CHAR;

            case READ_CHAR_STRING:
                return READ_CHAR;

            case START_NOTIFY_CHAR_STRING:
                return START_NOTIFY_CHAR;

            case STOP_NOTIFY_CHAR_STRING:
                return STOP_NOTIFY_CHAR;

            case START_FAKE_NOTIFY_CHAR_STRING:
                return START_FAKE_NOTIFY_CHAR;

            case IDLE_STRING:
                return IDLE;

            default:
                return -1;

        }
    }


    public static class Builder{

        private String type;
        private String modelWriteChar;
        private BLEWritableCharacteristic bleWritableCharacteristic;
        private String post_delay;
        private BLEReadableCharacteristic bleReadableCharacteristic;

        public BLESequenceElement build(){
            int post_delay_int=Integer.parseInt(post_delay);
            if (type==null){
                //TODO: throw exception type has to be declared
            }
            int type_int=BLESequenceElement.typeStringToInt(type);
            if (type_int==-1){
                //TODO: throw exception type not matching with any
            }
            if (bleWritableCharacteristic==null){
                //TODO: throw exception no writable char matching text
            } else {
                if (bleWritableCharacteristic.getModel(modelWriteChar)==null) {
                    //TODO: throw exception no model matching id found
                }
            }

            return new BLESequenceElement(type_int,
                    modelWriteChar, bleWritableCharacteristic, post_delay_int, bleReadableCharacteristic);
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setModelWriteChar(String modelWriteChar) {
            this.modelWriteChar = modelWriteChar;
            return this;
        }

        public Builder setBleWritableCharacteristic(BLEWritableCharacteristic bleWritableCharacteristic) {
            this.bleWritableCharacteristic = bleWritableCharacteristic;
            return this;
        }

        public Builder setPost_delay(String post_delay) {
            this.post_delay = post_delay;
            return this;
        }

        public Builder setBleReadableCharacteristic(BLEReadableCharacteristic bleReadableCharacteristic) {
            this.bleReadableCharacteristic = bleReadableCharacteristic;
            return this;
        }
    }

}
