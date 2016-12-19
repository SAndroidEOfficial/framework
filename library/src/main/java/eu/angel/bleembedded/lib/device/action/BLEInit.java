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

import eu.angel.bleembedded.lib.item.BLEItem;

/**
 * Class for handling the init action to interact with the remote device
 */
public class BLEInit extends BLEAction {

    private boolean isInitiated =false;
    private boolean isInitializationEnded=false;
    private List<BLEItem> initiatedItems;

    public boolean isInitiated() {
        return isInitiated;
    }

    public BLEInit setInitiated(boolean initiated) {
        isInitiated = initiated;
        return this;
    }

    public boolean isInitializationEnded() {
        return isInitializationEnded;
    }

    public BLEInit setInitializationEnded(boolean initializationEnded) {
        isInitializationEnded = initializationEnded;
        return this;
    }

    /**
     * Constructor
     * @param id identifier of the {@link BLEInit}
     * @param itemId identifier of the {@link eu.angel.bleembedded.lib.item.BLEItem} owner of the
     *               {@link BLEInit}
     * @param bleSequenceElements list of {@link BLESequenceElement}s with are used to fulfill
     *                            the {@link BLEInit} (used only for cloning purposes)
     * @param bleSequenceElementInterfaces list of {@link BLESequenceElementInterface}s generated
     *                                     by the bleSequenceElements. They are executed to fulfill
     *                                     the {@link BLEInit}
     * @param post_delay the delay after the execution of all the {@link BLESequenceElementInterface}s
     *                   of the {@link BLEInit}
     */
    private BLEInit(String id, String itemId, List<BLESequenceElement> bleSequenceElements,
                    List<BLESequenceElementInterface> bleSequenceElementInterfaces, int post_delay) {
        super(id, itemId, bleSequenceElements, bleSequenceElementInterfaces, post_delay);
    }

    /**
     * Add to the list the {@link BLEItem} trying to start the initialization of its resource. Useful to not repeat
     * the initialization of the same resource shared by different {@link BLEItem}
     */
    public void addInitiatedItem(BLEItem bleItem){
        if (initiatedItems==null)
            initiatedItems=new ArrayList<>();
        initiatedItems.add(bleItem);
    }

    /**
     * Remove the Item from the initiatedItems List. WARNING: Whether the list is empty after
     * the item removal, the method return true, otherwise the method return false
     *
     * @param bleItem Item to remove from {@link BLEInit#initiatedItems}
     *
     *                @return WARNING: return true if the List is empty after removal, false otherwise
     */
    public boolean removeInitiatedItem(BLEItem bleItem){
        if(initiatedItems!=null){
            for (int i=0; i<initiatedItems.size();i++){
                if (initiatedItems.get(i)==bleItem){
                    initiatedItems.remove(i);
                }
            }
            if (initiatedItems.size()==0)
                return true;
            else
                return false;
        } else
            return false;
    }


    @Override
    public BLEInit clone(){
        List<BLESequenceElementInterface> bleSequenceElementInterfaces=new ArrayList<>();
        for (BLESequenceElement bleSequenceElement:bleSequenceElements)
            bleSequenceElementInterfaces.add
                    (bleSequenceElement.getBleSequenceElementInterface());
        return new BLEInit(id, itemId, bleSequenceElements, bleSequenceElementInterfaces,
                post_delay);
    }

    public static class Builder extends BLEAction.Builder{

        @Override
        public BLEInit build(){

            List<BLESequenceElementInterface> bleSequenceElementInterfaces=new ArrayList<>();
            for (BLESequenceElement bleSequenceElement:this.bleSequenceElements)
                bleSequenceElementInterfaces.add
                        (bleSequenceElement.getBleSequenceElementInterface());

            int post_delay_int=Integer.parseInt(this.post_delay);
            return new BLEInit(this.id, this.itemId, this.bleSequenceElements, bleSequenceElementInterfaces,
                    post_delay_int);
        }
    }

}
