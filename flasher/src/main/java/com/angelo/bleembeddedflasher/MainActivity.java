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
package com.angelo.bleembeddedflasher;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.angelo.bleembeddedflasher.ble.ResourcesActivity;
import com.angelo.bleembeddedflasher.ble.communication.DeviceScanActivity;
import com.angelo.bleembeddedflasher.flashers.FlasherEvent;
import com.angelo.bleembeddedflasher.flashers.FlasherTaskEventListener;
import com.angelo.bleembeddedflasher.flashers.MasterFlasher;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xml.sax.SAXException;

import eu.angel.bleembedded.lib.BLEContext;
import eu.angel.bleembedded.lib.complements.XmlHandler;
import eu.angel.bleembedded.lib.device.DevicesDescriptorNew;
import eu.angel.bleembedded.lib.device.GattAttributesComplements;
import eu.angel.bleembedded.lib.item.BleResourcesHandler;
import eu.angel.bleembedded.lib.item.Bleresource;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;


public class MainActivity extends RootActivity {

	private static final int PICKFILE_RESULT_CODE = 1;


	private static final boolean logOnFile=false;
	private static final String TAG = "MainActivity";
	Context context;
//	TextView tv;
	MasterFlasher masterFlasher;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
//        tv= (TextView)findViewById(R.id.textView);

		BLEContext.initBLE(this);
		masterFlasher=MasterFlasher.getInstance();

        context=this;
		masterFlasher.setFlasherTaskEventListener(flasherTaskEventListener);

        ((Button)findViewById(R.id.buttonErase)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			 new Thread(new Runnable() { public void run()
			 {
				 masterFlasher.erase(context);
//					final String t= stFlasher.invokeNativeFunction();
//					Log.d(TAG, t);
//					((Activity) context).runOnUiThread(new Runnable() {
//						@Override
//						public void run()
//						{
//							tv.setText(t);
//						}
//					});

			 } }).start();
			}
		});

        ((Button)findViewById(R.id.buttonWrite)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	            intent.setType("file/*");
	            startActivityForResult(intent,PICKFILE_RESULT_CODE);
			}
		});

        ((Button)findViewById(R.id.buttonScan)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        final Intent intent = new Intent(context, DeviceScanActivity.class);
		        startActivity(intent);
			}
		});



		((Button)findViewById(R.id.buttonShowResources)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//
//				final MyTask myTask=new MyTask();
//				((Activity) context).runOnUiThread(new Runnable() {
//					@Override
//					public void run()
//					{
//						myTask.execute();
//
//					}
//				});
				final Intent intent = new Intent(context, ResourcesActivity.class);
				startActivity(intent);

			}
		});
//
//
//
//		((Button)findViewById(R.id.buttonWrite)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				try {
//					XmlHandler.saveBleresources(context, XmlHandler.parse2(context));
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (SAXException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//		((Button)findViewById(R.id.buttonScan)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				List<Bleresource> bleresources=XmlHandler.parse(context);
//				Log.d(TAG, bleresources.toString());
//			}
//		});

        
        
		  //////////////////Log.d///////
		  if (logOnFile)
		  {
			  
			    File folder = new File(Environment.getExternalStorageDirectory() + "/log");
			    if (!folder.exists()) {
			        //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
			        folder.mkdir();
			    }
			  
			  try {
				  //risiede in mem interna cell
				    String cmd = "logcat -c";

				    Runtime.getRuntime().exec(cmd);
				  
				    File filename = new File(Environment.getExternalStorageDirectory()+"/log/logfile.log"); 
				    filename.createNewFile(); 
				    //String cmd = "logcat -v time -r 5120 -n 100 -f "+filename.getAbsolutePath();
				    cmd = "logcat -v time -r 5120 -n 100 -f "+filename.getAbsolutePath();

				    Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
				    e.printStackTrace();
				}
		  }
		  //////////////////////////////

		BLEContext.startFlicManager();
    }

	public void grabButton(View v) {
		if (BLEContext.flicManager != null) {
			BLEContext.flicManager.initiateGrabButton(this);
		}
	}

	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  switch(requestCode){
		  case PICKFILE_RESULT_CODE:
		   if(resultCode==RESULT_OK){
			final String file_path = data.getData().getPath();
			 new Thread(new Runnable() { public void run() {
				 masterFlasher.write(context, file_path);
				 } }).start();
		   }
		   break;
		  case FlicManager.GRAB_BUTTON_REQUEST_CODE:
			  if (BLEContext.flicManager != null) {
				  final FlicButton button = BLEContext.flicManager.completeGrabButton(requestCode, resultCode, data);
				  if (button != null) {
					  Log.d(TAG, "Got a button: " + button);

					  // chiedo il nome da dare al device prima di salvarlo
					  AlertDialog.Builder builder = new AlertDialog.Builder(
							  MainActivity.this);
					  final EditText et = new EditText(MainActivity.this);
					  //et.setOnClickListener(DeviceControlActivity.this);
					  et.requestFocus();
					  builder.setMessage(R.string.Enter_a_specific_name_for_the_selected_Device)
							  .setTitle(R.string.Device_name)
							  .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
								  @Override
								  public void onClick(DialogInterface dlg, int which) {
									  String input = et.getText().toString();
									  try {
										  if (input == null || input.trim().length() == 0) throw new ToastException(context.getString(R.string.resource_name_cannot_be_left_blank));

										  dlg.dismiss();
										  DevicesDescriptorNew devicesDescriptorNew = DevicesDescriptorNew.getDeviceDescriptorByName("FlicButton", null);
										  if (devicesDescriptorNew == null) throw new ToastException("\"flic\" device descriptor not found in bleresources.xml");

										  // creo nuova risorsa in bleresources.xml
										  List<Bleresource> bleresources=new ArrayList<>();
										  String devItem = "flicbutton";

										  Bleresource.Builder bleresourceBuilder=new Bleresource.Builder();
										  bleresourceBuilder.setDevname(input);
										  bleresourceBuilder.setDevtype(devicesDescriptorNew.getDeviceType());
										  bleresourceBuilder.setDevversion("");
										  bleresourceBuilder.setDevmacaddress(button.mac);
										  bleresourceBuilder.setType(GattAttributesComplements.getItemTypeFromDevItem(devicesDescriptorNew, devItem));
										  bleresourceBuilder.setDevItem(devItem);
										  bleresourceBuilder.setCardinality(GattAttributesComplements.getDevItemCardinality(devicesDescriptorNew, devItem));
										  bleresourceBuilder.setName(input+"_"+devItem);
										  bleresources.add(bleresourceBuilder.build());

										  try {
											  XmlHandler.saveAndAppendBleresources(context, bleresources);
										  } catch (FileNotFoundException e) {
											  //TODO: evaluate whether throwing exception
											  e.printStackTrace();
										  } catch (SAXException e) {
											  e.printStackTrace();
										  } catch (IOException e) {
											  e.printStackTrace();
										  }

										  //BleResourcesHandler.storeAllDevResources2(context, input, devicesDescriptorNew, "", button.mac, null);
										  throw new ToastException(context.getString(R.string.device_attributes_stored));

									  } catch (ToastException ex) {
										  Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
									  }
								  }
							  })
							  .setNegativeButton(R.string.Cancel,  new DialogInterface.OnClickListener() {
								  @Override
								  public void onClick(DialogInterface dlg, int which) {
									  dlg.dismiss();
								  }
							  })
							  .setView(et)
							  .create().show();
				  }
			  }
			  break;
	   
	  }
	 }


	 FlasherTaskEventListener flasherTaskEventListener = new FlasherTaskEventListener()
	 {

		@Override
		public void on_flasherTaskEvent(FlasherEvent i) {

			switch(i.event)
			{
			case FlasherEvent.EVENT_START_ERASING:
				showToastOnUI(
						context,
						"start erasing target", Toast.LENGTH_SHORT);
				//setLoadingPanelVisibility(View.VISIBLE);
				break;
			case FlasherEvent.EVENT_STOP_ERASING:
				showToastOnUI(
						context,
						"erasing terminated", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_START_WRITING:
				showToastOnUI(
						context,
						"writing on target", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.VISIBLE);
				break;
			case FlasherEvent.EVENT_STOP_WRITING:
				showToastOnUI(
						context,
						"flash writing finished", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_ERROR_ERASING:
				showToastOnUI(
						context,
						"error erasing", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_ERROR_TARGET_LOCKET:
				showToastOnUI(
						context,
						"not able to write, target is still locked!", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_ERROR_WRITNG:
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_ERROR_LOADING_FILE:
				showToastOnUI(
						context,
						"error loading file!!!", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_ERROR_NO_PERMISSION:
				showToastOnUI(
						context,
						"no USB_permission", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_USB_PERMISSION_GRANTED:
				showToastOnUI(
						context,
						"USB_permission granted", Toast.LENGTH_SHORT);
				break;
			case FlasherEvent.EVENT_ERROR:
				showToastOnUI(
						context,
						"error...", Toast.LENGTH_SHORT);
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_START_LOADING_FILE:
				showToastOnUI(
						context,
						"start loading file", Toast.LENGTH_SHORT);
				break;
			case FlasherEvent.EVENT_STOP_LOADING_FILE:
				showToastOnUI(
						context,
						"finish loading file", Toast.LENGTH_SHORT);
				break;
			case FlasherEvent.EVENT_UNABLE_TO_CONNECT_TO_USB_FROM_NATIVE_CODE:
				showToastOnUI(
						context,
						"unable to connect usb from native code", Toast.LENGTH_SHORT);
				break;
			case FlasherEvent.EVENT_PROBABLY_UNABLE_TO_RECOGNIZE_TARGET_DEVICE:
				showToastOnUI(
						context,
						"probably unable to recognize target device", Toast.LENGTH_SHORT);
				break;
			case FlasherEvent.EVENT_START_ERASING_TASK:
				setLoadingPanelVisibility(View.VISIBLE);
				break;
			case FlasherEvent.EVENT_STOP_ERASING_TASK:
				setLoadingPanelVisibility(View.GONE);
				break;
			case FlasherEvent.EVENT_START_WRITING_TASK:
				setLoadingPanelVisibility(View.VISIBLE);
				break;
			case FlasherEvent.EVENT_STOP_WRITING_TASK:
				setLoadingPanelVisibility(View.GONE);
				showToastOnUI(
						context,
						"now make the BLEdevice discoverable and click start scan Button", Toast.LENGTH_SHORT);
				break;
			}

		}

	 };

		private void showToastOnUI(final Context context, final String mex, final int length)
		{
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run()
				{
					Toast.makeText(
							context,
							mex, length)
							.show();
				}
			});
		}
		
		private void setLoadingPanelVisibility(final int setting)
		{
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() 
				{	
					findViewById(R.id.loadingPanel).setVisibility(setting);
		
				}
			});
		}

//	@Override
//	protected void onStop() {
//		MasterFlasher.getInstance().stop(this);
//		super.onStop();
//	}

	private class MyTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {

			Uri uri = Uri.parse("content://com.angelo.bleembeddedflasher.fileprovider/bleresources.xml");
			InputStream is = null;
			StringBuilder result = new StringBuilder();
			try {
				is = getApplicationContext().getContentResolver().openInputStream(uri);
				BufferedReader r = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = r.readLine()) != null) {
					result.append(line);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try { if (is != null) is.close(); } catch (IOException e) { }
			}

			return result.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
		}
	}
}