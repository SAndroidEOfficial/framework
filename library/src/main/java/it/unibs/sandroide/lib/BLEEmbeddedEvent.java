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
package it.unibs.sandroide.lib;

/**
 * Collection of the type of events
 */
public class BLEEmbeddedEvent {

	//if the name in description file is different from the one of the real ble-device
	public static final int NAME_BLERESOURCE_DIFFERENT_FROM_REAL_NAME = 0;
	//if the required ble-device connected to one resources is not reachable
	public static final int BLE_DEVICE_NOT_FOUND = 1;
	//if the description file doesn't exist
	public static final int RESOURCE_DESCRIPTION_NOT_FOUND = 2;
	//if the smart device doesn't support BLE communication
	public static final int BLE_NOT_SUPPORTED = 3;
	//if BLE scan doesn't retrieve any device
	public static final int NO_BLE_DEVICES_FOUND = 4;

}
