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
package com.angelo.bleembeddedflasher.flashers.Arduino;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.STK500Constants;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.STKCallback;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.STKCommunicator;

import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STKEnterProgMode;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STKGetParameter;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STKGetSync;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STKLoadAddress;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STKProgramPage;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.commands.STKSetDevice;
import com.angelo.bleembeddedflasher.flashers.Arduino.stk500.responses.STK500Response;
import com.angelo.bleembeddedflasher.flashers.Flasher;
import com.angelo.bleembeddedflasher.flashers.FlasherEvent;
import com.angelo.bleembeddedflasher.flashers.FlasherTaskEventListener;
import com.angelo.bleembeddedflasher.usb.UsbConnectionEventListener;
import com.angelo.bleembeddedflasher.usb.USB_DeviceID;
import com.hoho.android.usbserial.driver.*;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.jaybee.intelhex.IntelHexException;
import cz.jaybee.intelhex.Parser;
import cz.jaybee.intelhex.listeners.RangeDetector;

public class Arduino_flasher extends Flasher {

    private final String TAG = "Arduino_flasher";
    private enum task_req{NONE, FLASHER_ERASE, FLASHER_WRITE}
    private static task_req last_task_req=task_req.NONE;
    private static String write_file_path;

    private UsbSerialPort sPort = null;
    private STKCommunicator stk;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;

    private int addr = 0, page_size=128, a_div = 2;
    private ByteBuffer sketchBuffer = null;
    private String selectedSketch="200";
    private int uploadCount = 0;

    protected int n=0;
    private UsbManager usbManager;


    public Arduino_flasher(Context context, USB_DeviceID mDeviceID,
                           FlasherTaskEventListener flasherTaskEventListener, UsbDevice device,
                           UsbManager usbManager) {
        super(context, mDeviceID, flasherTaskEventListener, device);
        this.usbManager=usbManager;
    }


    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    String currentCommand = "";
                    if (STKCommunicator.currentCommand!=null) {
                        currentCommand  = STKCommunicator.currentCommand.getClass().getSimpleName();
                        //printLogLine("STKCommunicator.currentCommand: " + currentCommand);
                    }

                    try {
                        if (stk!=null) {
                            STK500Response rsp = stk.onDataReceived(data);
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                        //Log.e(TAG,e.toString());
                    }
                }
            };



    protected void loadAddrAndWritePage(){
        final byte[] tosend = new byte[page_size];
        sketchBuffer.get(tosend,0,Math.min(page_size,sketchBuffer.remaining()));

        ;

        if (addr < n) {
            new STKLoadAddress(addr/a_div).send(new STKCallback() {
                @Override

                public void callbackCall(STK500Response rsp) {
                    if (rsp.isOk()) {
                        new STKProgramPage("F",tosend).send(new STKCallback() {
                            @Override
                            public void callbackCall(STK500Response rsp) {
                                if (rsp.isOk()) {
                                    loadAddrAndWritePage();
                                }
                            }
                        });
                    }
                }
            });
            addr+=page_size;
        } else {
        }
    };

    protected void uploadSketch(String filePath) {
        //selectedSketch =String.format("blink%s.hex",true?"200":"1000");
        //String.format("blink%s.hex",uploadCount++%2==0?"200":"1000");

        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] binaryFile=null;


        File file = new File(filePath);


        InputStream is= null;


        try {
            //is = context.getContentResolver().openInputStream(uri);
            is=new FileInputStream(file);
            Log.d(TAG, is.toString());


            Parser ihp = new Parser(is);
            ihp.setDataListener(new RangeDetector() {
                @Override
                public void data(long address, byte[] data) throws IOException {
                    // process data





                    bo.write(data);
                }
                @Override
                public void eof() {
                    // do some action
                }
            });
            ihp.parse();

        } catch (IntelHexException | IOException ex) {
            ex.printStackTrace();
        }

        binaryFile = bo.toByteArray();
        sketchBuffer = ByteBuffer.wrap(binaryFile);

        int page_size = 128, n_bytes = binaryFile.length;

        if ((n_bytes % page_size) != 0) {
            n = n_bytes + page_size - (n_bytes % page_size);
        } else {
            n = n_bytes;
        }


        addr = 0;
        loadAddrAndWritePage();

//        int page_size = 128, n_bytes = binaryFile.length;
//
//        if ((n_bytes % page_size) != 0) {
//            n = n_bytes + page_size - (n_bytes % page_size);
//        } else {
//            n = n_bytes;
//        }
//
//        //printLogLine(String.format("Start writing %d bytes",binaryFile.length));
//
//        addr = 0;
//        //loadAddrAndWritePage();  // COMMENTED to test new intel hex parser
//        return bo;
    }

    protected void programAndUpload(final String filePath){
        try {
            sPort.setDTR(false);
            sPort.setRTS(false);
            Thread.sleep(50);

            long startTime = System.currentTimeMillis();

            sPort.setDTR(true);
            sPort.setRTS(true);
            Thread.sleep(300);

            new STKGetSync().send(new STKCallback() {
                @Override
                public void callbackCall(STK500Response rsp) {
                    if (rsp.isOk()) {
                        new STKGetParameter(STK500Constants.Parm_STK_SW_MAJOR).send(new STKCallback() {
                            @Override
                            public void callbackCall(STK500Response rsp) {
                                if (rsp.isOk()) new STKGetParameter(STK500Constants.Parm_STK_SW_MINOR).send(new STKCallback() {
                                    @Override
                                    public void callbackCall(STK500Response rsp) {
                                        if (rsp.isOk()) new STKSetDevice().send(new STKCallback() {
                                            @Override
                                            public void callbackCall(STK500Response rsp) {
                                                if (rsp.isOk()) new STKEnterProgMode().send(new STKCallback() {
                                                    @Override
                                                    public void callbackCall(STK500Response rsp) {
                                                        if (rsp.isOk()) uploadSketch(filePath);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }

                }
            });

        } catch(Exception ex) {
        }
    };

    @Override
    public void flasher_erase(Context context) {

    }

    @Override
    public void flasher_write(Context context, String file_path) {
        Log.d(TAG, "saving command "+task_req.FLASHER_WRITE);
        triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_START_WRITING_TASK));
        write_file_path=file_path;
        set_last_req(task_req.FLASHER_WRITE);
        refreshUsbConnection(context);
    }

    public static void set_last_req(task_req req)
    {
        last_task_req=req;
    }

    public void executeLastCommand()
    {
        Log.d(TAG, "executing last command... "+last_task_req);
        switch (last_task_req) {

            case FLASHER_WRITE:
                Log.d(TAG, "starting writing flash");
                if (write_file_path != null) {
                    if (sPort != null) {
                        STKTask stkTask = new STKTask(write_file_path);
                        stkTask.execute();
                    }

                }
                break;

            default:
                break;
        }
        Log.d(TAG, "last command executed "+last_task_req);

        last_task_req = task_req.NONE;
    }

    @Override
    protected boolean init_device(UsbDeviceConnection conn) {
        try {

            // Find all available drivers from attached devices.
            //UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers =
                    UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            if (availableDrivers.isEmpty()) {
                throw new Exception("Can't find any usb drivers attached!");
            }

            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
            if (connection == null) {
                throw new Exception("Can't open connection to first available driver!");
            }

            // Read some data! Most have just one port (port 0).
            List<UsbSerialPort> ports = driver.getPorts();
            if (ports.size() <= 0) throw new Exception("Can't find any USB serial ports on selected driver!");
            sPort = ports.get(0);
            try {
                sPort.open(connection);
            } catch (Exception e) {
                throw new Exception("Can't open first available port on first available driver!");
            }
            try {
                //sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                //sPort.setParameters(19200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            } catch (Exception e) {
                sPort.close();
                sPort = null;
                throw e;
            }
            //executeLastCommand();
        } catch(Exception e) {
            Log.e(TAG,e.getMessage());
        }
        onDeviceStateChange();
        executeLastCommand();
        return true;
    }

    class STKTask extends AsyncTask<String, Void, Integer> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */

        private String filePath;

        public STKTask(String filePath){
            this.filePath=filePath;
        }

        protected Integer doInBackground(String... urls) {

            if (sPort!=null) {
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_START_WRITING));
                programAndUpload(filePath);

            }

            return 1;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_STOP_WRITING));
        }
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        mDumpTextView = (TextView) findViewById(R.id.consoleText);
//        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
//
//        Button buttonClear = (Button) findViewById(R.id.btnClear);
//        Button buttonFlash = (Button) findViewById(R.id.btnFlash);
//
//        buttonClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    if (sPort!=null) {
//                        mDumpTextView.setText("");
//                    }
//                } catch(Exception e) {
//                    Log.e(TAG,e.getMessage());
//                }
//            }
//        });
//
//        buttonFlash.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (sPort != null) {
//                    STKTask stk = new STKTask();
//                    stk.execute();
//                }
//            }
//        });
//
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopIoManager();
//        if (sPort != null) {
//            try {
//                sPort.close();
//            } catch (IOException e) {
//                // Ignore.
//            }
//            sPort = null;
//        }
//        finish();
//    }



    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
            stk = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
            //runOnUiThread needed beacause STKCommunicator use handler
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stk = new STKCommunicator(sPort);}
            });

        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    @Override
    protected UsbConnectionEventListener getmConnectionHandler() {
        return 	new UsbConnectionEventListener() {
            @Override
            public void onUsbStopped() {
                Log.e(TAG, "Usb stopped!");
            }

            @Override
            public void onDeviceNotFound() {
                if(sUsbConnectionController != null){
                    sUsbConnectionController.stop();
                    sUsbConnectionController = null;
                }
            }

            @Override
            public void onPermissionGranted(UsbDevice dev) {
                triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_USB_PERMISSION_GRANTED));
                if (!flasher_connectToDev())
                {
                    triggerFlashserTaskEventListener(new FlasherEvent(mDeviceID, FlasherEvent.EVENT_ERROR));
                    Log.d(TAG, "failed init USB connection after permission granted");
                    return;
                }
                switch(last_task_req)
                {
                    case FLASHER_ERASE:
                        flasher_erase(context);
                        break;

                    case FLASHER_WRITE:
                        if (write_file_path!=null)
                            flasher_write(context, write_file_path);
                        write_file_path=null;
                        break;

                    default:
                        break;
                }
                last_task_req=task_req.NONE;
            }

        };
    }

}
