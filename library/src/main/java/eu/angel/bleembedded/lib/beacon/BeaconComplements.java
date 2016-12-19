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
package eu.angel.bleembedded.lib.beacon;

/**
 * Support class for beacon handling.
 */
public class BeaconComplements {

    //TODO: queste associazioni dovrebbero essere caricate da un xml
    public static int getModelFromString(String model)
    {
        switch(model)
        {
            case "ESTIMOTE":
            case "Estimote":
            case "estimote":
                return BLEBeacon.MODEL_ESTIMOTE;

            case "gymbal":
            case "Gymbal":
            case "GYMBAL":
                return BLEBeacon.MODEL_GYMBAL;

            default:
                return BLEBeacon.MODEL_UNKNOWN;
        }
    }

    //TODO: queste associazioni dovrebbero essere caricate da un xml
    public static String getStringFromModel(int model)
    {
        switch(model)
        {
            case BLEBeacon.MODEL_ESTIMOTE:
                return "estimote";

            case BLEBeacon.MODEL_GYMBAL:
                return "gymbal";

            default:
                return "";
        }
    }

    //TODO: queste associazioni dovrebbero essere caricate da un xml
    public static int getTypeFromString(String type)
    {
        switch(type)
        {
            case "EDDY_STONE_UID":
                return BLEBeacon.EDDY_STONE_UID;

            case "EDDY_STONE_UID_AND_TELEMETRY":
                return BLEBeacon.EDDY_STONE_UID_AND_TELEMETRY;

            case "EDDY_STONE_URL_AND_TELEMETRY":
                return BLEBeacon.EDDY_STONE_URL_AND_TELEMETRY;

            case "EDDY_STONE_URL":
                return BLEBeacon.EDDY_STONE_URL;

            case "I_BEACON":
                return BLEBeacon.I_BEACON;

            case "ALT_BEACON":
                return BLEBeacon.ALT_BEACON;

            case "URI_BEACON":
                return BLEBeacon.URI_BEACON;

            default:
                return BLEBeacon.UNKNOWN_BEACON;
        }
    }

    //TODO: queste associazioni dovrebbero essere caricate da un xml
    public static String getStringFromType(int type)
    {
        switch(type)
        {
            case BLEBeacon.EDDY_STONE_UID:
                return "EDDY_STONE_UID";

            case BLEBeacon.EDDY_STONE_UID_AND_TELEMETRY:
                return "EDDY_STONE_UID_AND_TELEMETRY";

            case BLEBeacon.EDDY_STONE_URL_AND_TELEMETRY:
                return "EDDY_STONE_URL_AND_TELEMETRY";

            case  BLEBeacon.EDDY_STONE_URL:
                return "EDDY_STONE_URL";

            case BLEBeacon.I_BEACON:
                return "I_BEACON";

            case BLEBeacon.ALT_BEACON:
                return "ALT_BEACON";

            case BLEBeacon.URI_BEACON:
                return "URI_BEACON";

            default:
                return "";
        }
    }

}
