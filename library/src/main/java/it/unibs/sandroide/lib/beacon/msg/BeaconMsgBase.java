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

package it.unibs.sandroide.lib.beacon.msg;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;

import java.util.Date;

public class BeaconMsgBase extends Beacon {

    public static String MSG_LAYOUT = "";

    private long lastSeen = 0;

    public BeaconMsgBase(BeaconMsgBase b) {
        super(b);
        lastSeen = b.lastSeen;
    }

    public BeaconMsgBase(Beacon b) {
        super(b);
        lastSeen = new Date().getTime()/1000;
    }

//    todo: How should it be implemented?
    public BeaconMsgBase parse() throws Exception {
        throw new Exception("This must be implemented by subclasses");
    };

//    todo: how should it be implemented?
    public static void initLayout(BeaconManager mgr) throws Exception {
        throw new Exception("This must be implemented by subclasses");
    }

    public String getKeyIdentifier() {
        return this.mParserIdentifier+"_"+getIdentifiers().toString();
        //return getParserIdentifier();
        //return TextUtils.join("_",getIdentifiers().toArray());
    }

    public String getParserSimpleClassname() {
        String[] arr = getParserIdentifier().split("\\.");
        return arr[arr.length-1];
    }

    public long getLastSeen(){
        return this.lastSeen;
    }

    public String getImage() {
        return BASE64_IMAGE;
    }

    private static String BASE64_IMAGE = "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";
}
