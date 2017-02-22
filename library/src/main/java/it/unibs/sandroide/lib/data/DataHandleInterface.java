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
package it.unibs.sandroide.lib.data;

import java.util.HashMap;

/**
 * Interface for handling the incoming/outgoing data
 */
public interface DataHandleInterface {

    /**
     * Handles the incoming data
     * @param value the value of the incoming/outgoing
     * @param dataMap the HashMAp linking the incoming/outgoing data value with the related
     *                library/remote device (if needed, for non-linear conversion)
     * @param intercept intercept for setting the value to library/remote device format (for linear conversion)
     * @param slope slope for setting the value to library/remote device format (for linear conversion)
     * @param handle_value value used for some handling mode (e.g. onset)
     *
     */
    float handle(float value,HashMap<Object,Object> dataMap, float intercept,float slope,
                 float handle_value);

}
