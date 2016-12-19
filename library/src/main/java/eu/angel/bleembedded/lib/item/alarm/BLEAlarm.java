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
package eu.angel.bleembedded.lib.item.alarm;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.angel.bleembedded.lib.item.BLEItem;
import eu.angel.bleembedded.lib.item.Bleresource;

/**
 * The class which implements the BLEAlarm resources (it's an actuator).
 */
public class BLEAlarm extends BLEItem{

	public static final int EVENT_SET_ALARM = 0;
	Handler mHandler;
	//BLEOnEventListener onAlarmSet;
	private boolean isAlarmOn=false;
	private int alarmLevel=1;

	/**
	 * Constructor of the {@link BLEAlarm} class.
	 * @param name the name of the device to connect with (the name identifies exactly the
	 *                   remote device anf the firmware version).
	 * @param bleresource {@link Bleresource} which defines the Item.
	 */
	public BLEAlarm(String name, Bleresource bleresource) {
		super(BLEItem.TYPE_ALARM, name, bleresource);
	}

	/**
	 * Plays the alarm.
	 * @param milliseconds defines how many milliseconds the alarm stays on.
	 */
	public void alarm(long milliseconds)
	{
	    mHandler = new Handler();
	    mHandler.postDelayed(mStopAlarm, milliseconds);
		if (!isAlarmOn)
		startAlarm();
	}

	/**
	 * Plays the alarm.
	 * @param delay defines the time before the sequence starts.
	 * @param repetitionPeriod period of the sequence.
	 */
	public void alarm(long delay, long repetitionPeriod)
	{
	    mHandler = new Handler();
	    mHandler.postDelayed(new ToggleAlarmRunnable(repetitionPeriod), delay);
		if (isAlarmOn)
		stopAlarm();
	}

	/**
	 * Plays the alarm.
	 * @param pattern defines the pattern of the alarm sequence.
	 * @param repeat defines the number of repetition of the sequence.
	 */
	public void alarm (long[] pattern, int repeat)
	{
	    mHandler = new Handler();
	    mHandler.post(new PatternedAlarmRunnable(pattern, repeat));
		if (isAlarmOn)
		stopAlarm();
	}


	/**
	 * Stops the alarm handler.
	 */
	public void cancel ()
	{
		if (mHandler!=null)
		{
			mHandler.removeCallbacksAndMessages(null);
			mHandler=null;
		}

		if (isAlarmOn)
		stopAlarm();
	}

	  private Runnable mStopAlarm = new Runnable() {

		    @Override
		    public void run() {
		    	stopAlarm();
		    }
		  };


	public void setAlarmLevel(int level){
		alarmLevel=level;
	}

	/**
	 * Starts the alarm.
	 */
	public void startAlarm()
	{

		List<Float> input=new ArrayList<>();
		input.add((float)alarmLevel);
		runAction("set_alarm", input);
		isAlarmOn=true;

	}

	/**
	 * Stops the alarm handler.
	 */
	public void stopAlarm()
	{
		List<Float> input=new ArrayList<>();
		runAction("stop_alarm", input);
		isAlarmOn=false;

	}

	/**
	 * Runnable for toggling the alarm.
	 */
	public class ToggleAlarmRunnable implements Runnable {
		  private long delay;
		  public ToggleAlarmRunnable(long delay) {
		    this.delay = delay;
		  }

		  public void run() {
		      Log.e("Handlers", "Calls");
		      if (!isAlarmOn)
		    	  startAlarm();
		      else
		    	  stopAlarm();
		      mHandler.postDelayed(this, delay);
		  }
		}

	/**
	 * Runnable for toggling the alarm, which run for a number of repetition.
	 */
	public class AutoTerminatingToggleAlarmRunnable implements Runnable {
		  private long delay;
		  private int repetition;
		  public AutoTerminatingToggleAlarmRunnable(long delay, int repetition) {
		    this.delay = delay;
		    this.repetition=repetition;
		  }

		  public void run() {
		      Log.e("Handlers", "Calls");
		      if (!isAlarmOn)
		    	  startAlarm();
		      else
		    	  stopAlarm();
		      if (repetition>0)
		      {
		    	  mHandler.postDelayed(this, delay);
		    	  repetition--;
		      }
		  }
		}

	/**
	 * Runnable for play the alarm following a specified pattern.
	 */
	public class PatternedAlarmRunnable implements Runnable {
		  private long[] pattern;
		  private int repetition;
		  private int index=0;
		  public PatternedAlarmRunnable(long[] pattern, int repetition) {
		    this.pattern = pattern;
		    this.repetition=repetition;
		  }

		  public void run() {
		      Log.e("Handlers", "Calls");
		      if (!isAlarmOn)
		    	  startAlarm();
		      else
		    	  stopAlarm();
		      if (index<pattern.length)
		      {
		    	  mHandler.postDelayed(this, pattern[index]);
		    	  index++;
		      }
		      else
		      {
		    	  if (repetition>=0 && repetition<pattern.length)
		    	  {
		    		  index=repetition;
		    		  mHandler.postDelayed(this, pattern[index]);
		    	  }
		    	  else
		    	  {
		    		  if (isAlarmOn)
		    			  stopAlarm();
		    	  }
		    		  
		      }
		  }
		}

	}
	
	

