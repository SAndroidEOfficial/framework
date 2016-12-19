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
package eu.angel.bleembedded.lib.device.action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.angel.bleembedded.lib.device.DeviceControl;

/**
 * Class for handling the action to interact with the remote device
 */
public class BLEAction {

    protected String id;
    protected String itemId;
    protected final List<BLESequenceElement> bleSequenceElements;
    private final List<BLESequenceElementInterface> bleSequenceElementInterfaces;
    @Deprecated
    protected int post_delay;
    private BLEActionEnded bleActionEnded;


    public BLEAction setBleActionEnded(BLEActionEnded bleActionEnded) {
        this.bleActionEnded = bleActionEnded;
        return this;
    }

    /**
     * Constructor
     * @param id identifier of the {@link BLEAction}
     * @param itemId identifier of the {@link eu.angel.bleembedded.lib.item.BLEItem} owner of the
     *               {@link BLEAction}
     * @param bleSequenceElements list of {@link BLESequenceElement}s with are used to fulfill
     *                            the {@link BLEAction} (used only for cloning purposes)
     * @param bleSequenceElementInterfaces list of {@link BLESequenceElementInterface}s generated
     *                                     by the bleSequenceElements. They are executed to fulfill
     *                                     the {@link BLEAction}
     * @param post_delay the delay after the execution of all the {@link BLESequenceElementInterface}s
     *                   of the {@link BLEAction}
     */
    protected BLEAction(String id, String itemId,
                        List<BLESequenceElement> bleSequenceElements,
                        List<BLESequenceElementInterface> bleSequenceElementInterfaces,
                        int post_delay){
        this.id=id;
        this.bleSequenceElements=bleSequenceElements;
        this.bleSequenceElementInterfaces=bleSequenceElementInterfaces;
        this.post_delay=post_delay;
        this.itemId=itemId;
    }

    public String getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    /**
     * Run the {@link BLEAction} executing the {@link BLEAction#bleSequenceElementInterfaces}. The
     * execution is handled by the {@link DeviceControl} element. At the end of the executions the
     * {@link BLEActionEnded#onBleActionEnded()} is triggered
     * @param deviceControl the {@link DeviceControl} which handles the {@link BLEAction} execution
     * @param input the data useful to execute some {@link BLESequenceElementInterface}s
     */
    synchronized public void runAction(final DeviceControl deviceControl,
                                      final List<Float> input){
        try {
            boolean lockGot;
            do{
                lockGot=deviceControl.getLock().tryLock(100, TimeUnit.MILLISECONDS);
            }
            while (!lockGot);
            try{
                int delay=0;

                //for (final BLESequenceElementInterface bleSequenceElementInterface:bleSequenceElementInterfaces){
                for (int i=0;i<bleSequenceElementInterfaces.size();i++){

                    final int index=i;

                    deviceControl.addBLEElement(new Runnable() {
                        @Override
                        public void run() {
                            bleSequenceElementInterfaces.get(index).sequenceElementFunction(deviceControl,
                                    input);
                        }
                    }, delay);

                    delay=bleSequenceElements.get(i).getPost_delay();
                }


                deviceControl.addBLEElement(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (bleActionEnded!=null){
                                Thread callback= new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bleActionEnded.onBleActionEnded();
                                    }
                                });
                                callback.start();
                            }
                            Thread.sleep(post_delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, delay);
                //return absoluteDelay+post_delay;


            } finally {
                deviceControl.getLock().unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clone the {@link BLEAction}
     */
    public BLEAction clone(){
        List<BLESequenceElementInterface> bleSequenceElementInterfaces=new ArrayList<>();
        for (BLESequenceElement bleSequenceElement:bleSequenceElements)
            bleSequenceElementInterfaces.add
                    (bleSequenceElement.getBleSequenceElementInterface());
        return new BLEAction(id, itemId, bleSequenceElements, bleSequenceElementInterfaces,
                post_delay);
    }

    public static class Builder{

        protected String id;
        protected String itemId;
        protected List<BLESequenceElement> bleSequenceElements=new ArrayList<>();
        @Deprecated
        protected String post_delay;

        public BLEAction build(){

            List<BLESequenceElementInterface> bleSequenceElementInterfaces=new ArrayList<>();
            for (BLESequenceElement bleSequenceElement:bleSequenceElements)
                bleSequenceElementInterfaces.add
                        (bleSequenceElement.getBleSequenceElementInterface());

            int post_delay_int=Integer.parseInt(post_delay);
            return new BLEAction(id, itemId, bleSequenceElements, bleSequenceElementInterfaces,
                    post_delay_int);
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setBleSequenceElements(List<BLESequenceElement> bleSequenceElements) {
            this.bleSequenceElements = bleSequenceElements;
            return this;
        }

        public Builder addBleSequenceElement(BLESequenceElement bleSequenceElement) {
            this.bleSequenceElements.add(bleSequenceElement);
            return this;
        }

        @Deprecated
        public Builder setPost_delay(String post_delay) {
            this.post_delay = post_delay;
            return this;
        }

        public Builder setItemId(String itemId) {
            this.itemId = itemId;
            return this;
        }
    }

}
