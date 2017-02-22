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

import android.util.Log;

import java.util.Arrays;

import it.unibs.sandroide.lib.complements.BufferOverflowException;
import it.unibs.sandroide.lib.complements.Complements;
import it.unibs.sandroide.lib.complements.DataTypeLengthMismatchException;

/**
 * Support class for the data conversion
 */
public class BLEDataConversionComplements {

    public final static String TAG="BLEDataConversionC";

    public static final int FORMAT_UINT8 = 0x11;
    public static final int FORMAT_UINT16 = 0x12;
    public static final int FORMAT_UINT32 = 0x14;
    public static final int FORMAT_SINT8 = 0x21;
    public static final int FORMAT_SINT16 = 0x22;
    public static final int FORMAT_SINT32 = 0x24;
    public static final int FORMAT_SFLOAT = 0x32;
    public static final int FORMAT_FLOAT = 0x34;
    public static final int FORMAT_CHAR8 = 0x41;
    public static final int FORMAT_CHAR16 = 0x42;
    public static final int FORMAT_LONG = 0x58;



    //region dataformat

    public static final String UINT8_STRING = "uint8";
    public static final String UINT16_STRING = "uint16";
    public static final String UINT32_STRING = "uint32";
    public static final String SINT8_STRING = "sint8";
    public static final String SINT16_STRING = "sint16";
    public static final String SINT32_STRING = "sint32";
    public static final String SFLOAT_STRING = "sfloat";
    public static final String FLOAT_STRING = "float";
    public static final String CHAR8_STRING = "char8";
    public static final String CHAR16_STRING = "char16";
    public static final String LONG_STRING = "long";

    //TODO: unused should be a data handle

    public final static int UNUSED=-1;

    public static final int BIT_LOGIC_NONE = 0;
    public static final int BIT_LOGIC_END = 1;
    public static final int BIT_LOGIC_OR = 2;
    public static final int BIT_LOGIC_XOR = 3;
    public static final int BIT_LOGIC_RIGHT_SHIFT = 4;
    public static final int BIT_LOGIC_LEFT_SHIFT = 5;


    public static final String BIT_LOGIC_NONE_STRING= "none";
    public static final String BIT_LOGIC_END_STRING= "and";
    public static final String BIT_LOGIC_OR_STRING= "or";
    public static final String BIT_LOGIC_XOR_STRING= "xor";
    public static final String BIT_LOGIC_RIGHT_SHIFT_STRING= "right_shift";
    public static final String BIT_LOGIC_LEFT_SHIFT_STRING= "left_shift";

    public static int getDataFormatIntFromString(String format){
        if(format!=null){
            switch (format){

                case UINT8_STRING: return FORMAT_UINT8;
                case UINT16_STRING: return FORMAT_UINT16;
                case UINT32_STRING: return FORMAT_UINT32;
                case SINT8_STRING: return FORMAT_SINT8;
                case SINT16_STRING: return FORMAT_SINT16;
                case SINT32_STRING: return FORMAT_SINT32;
                case SFLOAT_STRING: return FORMAT_SFLOAT;
                case FLOAT_STRING: return FORMAT_FLOAT;
                case CHAR8_STRING: return FORMAT_CHAR8;
                case CHAR16_STRING: return FORMAT_CHAR16;
                case LONG_STRING: return FORMAT_LONG;

                default:
                    //TODO exception...
                    return -1;
            }} else
            return FORMAT_UINT8;
    }


    public static int getBitLogicOperationIntFromString(String bitLogicOp){
        if(bitLogicOp!=null){
            switch (bitLogicOp){

                case BIT_LOGIC_NONE_STRING: return BIT_LOGIC_NONE;
                case BIT_LOGIC_END_STRING: return BIT_LOGIC_END;
                case BIT_LOGIC_OR_STRING: return BIT_LOGIC_OR;
                case BIT_LOGIC_XOR_STRING: return BIT_LOGIC_XOR;
                case BIT_LOGIC_RIGHT_SHIFT_STRING: return BIT_LOGIC_RIGHT_SHIFT;
                case BIT_LOGIC_LEFT_SHIFT_STRING: return BIT_LOGIC_LEFT_SHIFT;

                default:
                    //TODO exception...
                    return -1;
            }
        } else
            return BIT_LOGIC_NONE;
    }


    private static int getTypeLen(int formatType) {
        return formatType & 0xF;
    }

    /**
     *
     * @param value New value for this characteristic
     * @param formatType Integer format type used to transform the value parameter
     * @param offset Offset at which the value should be placed
     * @return true if the locally stored value has been set
     */

    public static boolean setMessageValue(byte[] bytes, int value, int formatType, int offset) {
        int len = offset + getTypeLen(formatType);
        if (len > bytes.length) return false;

        switch (formatType) {

            case FORMAT_SINT8:
                value = intToSignedBits(value, 8);
                // Fall-through intended
            case FORMAT_CHAR8:
            case FORMAT_UINT8:
                bytes[offset] = (byte)(value & 0xFF);
                break;

            case FORMAT_SINT16:
                value = intToSignedBits(value, 16);
                // Fall-through intended
            case FORMAT_CHAR16:
            case FORMAT_UINT16:
                bytes[offset++] = (byte)(value & 0xFF);
                bytes[offset] = (byte)((value >> 8) & 0xFF);
                break;

            case FORMAT_SINT32:
                value = intToSignedBits(value, 32);
                // Fall-through intended
            case FORMAT_UINT32:
                bytes[offset++] = (byte)(value & 0xFF);
                bytes[offset++] = (byte)((value >> 8) & 0xFF);
                bytes[offset++] = (byte)((value >> 16) & 0xFF);
                bytes[offset] = (byte)((value >> 24) & 0xFF);
                break;

            default:
                return false;
        }
        return true;
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size-1)) != 0) {
            unsigned = -1 * ((1 << size-1) - (unsigned & ((1 << size-1) - 1)));
        }
        return unsigned;
    }

    /**
     * Convert an integer into the signed bits of a given length.
     */
    private static int intToSignedBits(int i, int size) {
        if (i < 0) {
            i = (1 << size-1) + (i & ((1 << size-1) - 1));
        }
        return i;
    }

    /**
     * The method returns the {@link DataHandleInterface} related to the format required. The
     * interface will be used to convert the incoming bytes into Float value, not only converting
     * the bytes, but if defined also using the logic operations or the byte logic handler
     * @param format the configuration of the received bytes in order to properly acquire the value
     * @param logic_operation the logic operation made on the formatted value
     * @param logic_operation_value the value used to make the logic operation
     * @param byteLogicHandler the byte logic operation made on the unformatted value
     */
    //TODO: complete with missing bitlogic ops and unfinished format
    //TODO: big endian and little endian handling
    public static DataConversionInterface intFormatToDataConversionInterface
            (int format, int logic_operation, String logic_operation_value,
             final ByteLogicHandler byteLogicHandler){
        //region dataConversion prototype

            if (byteLogicHandler==null){
                switch (logic_operation){

                    case BIT_LOGIC_NONE:
                        switch(format){

                            case FORMAT_FLOAT:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            case FORMAT_SINT16:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>4){
                                            throw new BufferOverflowException();
                                        } else {
                                            int value = (bytes[offset] & 0xFF);
                                            for (int i=1;i<length;i++){
                                                value |= (bytes[offset+i]) << (Byte.SIZE * i);
                                            }
                                            return value;

                                        }

                                    }
                                };

                            case FORMAT_UINT16:
                                //TODO: test
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>2){
                                            throw new BufferOverflowException();
                                        } else if (length<2){
                                            throw new DataTypeLengthMismatchException();
                                        } else {
                                            return (int) ( ((bytes[offset+1] & 0xFF) << 8) | (bytes[offset] & 0xFF));
                                        }
                                    }
                                };

                            case FORMAT_LONG:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            case FORMAT_SINT8:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        int value = bytes[offset];
                                        return value;

                                    }
                                };

                            case FORMAT_UINT8:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return bytes[offset] & 0xFF;
                                    }
                                };

                            //FIXME: all the Cluster not associated with an Item should be unused
                            //FIXME: unused should be the data handler
                            case UNUSED:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            default:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>4){
                                            throw new BufferOverflowException();
                                        } else {
                                            int value = (bytes[offset] & 0xFF);
                                            for (int i=1;i<length;i++){
                                                value |= (bytes[offset+i]) << (Byte.SIZE * i);
                                            }
                                            return value;

                                        }

                                    }
                                };

                        }
                    ///////////////////////region BIT_LOGIC_END////////////////////////
                    case BIT_LOGIC_END:

                        final int mask=getByteArrayFromString(logic_operation_value);

                        switch(format){

                            case FORMAT_FLOAT:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            case FORMAT_SINT16:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>4){
                                            throw new BufferOverflowException();
                                        } else {
                                            int value = (bytes[offset] & 0xFF);
                                            for (int i=1;i<length;i++){
                                                value |= (bytes[offset+i]) << (Byte.SIZE * i);
                                            }
                                            return (value&mask);

                                        }

                                    }
                                };

                            case FORMAT_UINT16:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>2){
                                            throw new BufferOverflowException();
                                        } else if (length<2){
                                            throw new DataTypeLengthMismatchException();
                                        } else {
                                            int ret= ( ((bytes[offset+1] & 0xFF) << 8) | (bytes[offset] & 0xFF));
                                            return (ret&mask);
                                        }
                                    }
                                };

                            case FORMAT_LONG:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            case FORMAT_SINT8:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        int value = bytes[offset];
                                        return (value&mask);

                                    }
                                };

                            case FORMAT_UINT8:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return (bytes[offset] & 0xFF)&mask;
                                    }
                                };

                            //FIXME: all the Cluster not associated with an Item should be unused
                            //FIXME: unused should be the data handler
                            case UNUSED:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            default:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>4){
                                            throw new BufferOverflowException();
                                        } else {
                                            int value = (bytes[offset] & 0xFF);
                                            for (int i=1;i<length;i++){
                                                value |= (bytes[offset+i]) << (Byte.SIZE * i);
                                            }
                                            return (value&mask);

                                        }

                                    }
                                };








                        }
                        ////////////////////////////////////

                    /////////////BIT_LOGIC_OR/////////////////
                    case BIT_LOGIC_OR:

                        final int ormask=getByteArrayFromString(logic_operation_value);

                        switch(format){

                            case FORMAT_FLOAT:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            case FORMAT_SINT16:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>4){
                                            throw new BufferOverflowException();
                                        } else {
                                            int value = (bytes[offset] & 0xFF);
                                            for (int i=1;i<length;i++){
                                                value |= (bytes[offset+i]) << (Byte.SIZE * i);
                                            }
                                            return (value|ormask);

                                        }

                                    }
                                };

                            case FORMAT_UINT16:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>2){
                                            throw new BufferOverflowException();
                                        } else if (length<2){
                                            throw new DataTypeLengthMismatchException();
                                        } else {
                                            int ret= ( ((bytes[offset+1] & 0xFF) << 8) | (bytes[offset] & 0xFF));
                                            return (ret|ormask);
                                        }
                                    }
                                };

                            case FORMAT_LONG:
                                //TODO
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            case FORMAT_SINT8:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        int value = bytes[offset];
                                        return (value|ormask);

                                    }
                                };

                            case FORMAT_UINT8:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return (bytes[offset] & 0xFF)|ormask;
                                    }
                                };

                            //FIXME: all the Cluster not associated with an Item should be unused
                            //FIXME: unused should be the data handler
                            case UNUSED:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        return 0;
                                    }
                                };

                            default:
                                return new DataConversionInterface() {
                                    @Override
                                    public float convert(byte[] bytes, int offset, int length) {
                                        if (length>4){
                                            throw new BufferOverflowException();
                                        } else {
                                            int value = (bytes[offset] & 0xFF);
                                            for (int i=1;i<length;i++){
                                                value |= (bytes[offset+i]) << (Byte.SIZE * i);
                                            }
                                            return (value|ormask);

                                        }

                                    }
                                };
                        }
                        ////////////////

                    default:
                        return null;
                }
            } else{
                //////////byteLogicFunction
                switch(format){

                    case FORMAT_FLOAT:
                        //TODO
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                return 0;
                            }
                        };

                    case FORMAT_SINT16:
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                if (length>4){
                                    throw new BufferOverflowException();
                                } else {
                                    if (byteLogicHandler.setValue(bytes)) {
                                        bytes = Complements.leIntToByteArray(byteLogicHandler.handle());
                                        int value = (bytes[offset] & 0xFF);
                                        for (int i = 1; i < length; i++) {
                                            value |= (bytes[i]) << (Byte.SIZE * i);
                                        }
                                        return value;
                                    } else
                                        //TODO: maybe throw an exception
                                        return 0;

                                }
                            }
                        };

                    case FORMAT_UINT16:
                        //TODO: test
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                if (length>2){
                                    throw new BufferOverflowException();
                                } else if (length<2){
                                    throw new DataTypeLengthMismatchException();
                                } else {

                                    if (byteLogicHandler.setValue(new byte[]{bytes[offset], bytes[offset+1]})) {
                                        Log.d(TAG,"incoming bytes: "+Arrays.toString(bytes)+", offs: "+offset);
                                        int bytesInt=byteLogicHandler.handle();
                                        bytes = Complements.leIntToByteArray(bytesInt);
                                        Log.d(TAG,"int: "+bytesInt+"elaborated bytes: "+Arrays.toString(bytes));
                                        return (int) (((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF));
                                    } else
                                        //TODO: maybe throw an exception
                                        return 0;
                                }
                            }
                        };

                    case FORMAT_LONG:
                        //TODO
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                return 0;
                            }
                        };

                    case FORMAT_SINT8:
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                if (byteLogicHandler.setValue(bytes)) {
                                    bytes = Complements.leIntToByteArray(byteLogicHandler.handle());
                                    int value = bytes[0];
                                    return value;
                                } else
                                    //TODO: maybe throw an exception
                                    return 0;

                            }
                        };

                    case FORMAT_UINT8:
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                if (byteLogicHandler.setValue(bytes)) {
                                    bytes = Complements.leIntToByteArray(byteLogicHandler.handle());
                                    return bytes[0] & 0xFF;
                                } else
                                    //TODO: maybe throw an exception
                                    return 0;
                            }
                        };

                    //FIXME: all the Cluster not associated with an Item should be unused
                    //FIXME: unused should be the data handler
                    case UNUSED:
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                return 0;
                            }
                        };

                    default:
                        return new DataConversionInterface() {
                            @Override
                            public float convert(byte[] bytes, int offset, int length) {
                                if (length>4){
                                    throw new BufferOverflowException();
                                } else {
                                    if (byteLogicHandler.setValue(bytes)) {
                                        Log.d(TAG,"incoming bytes: "+Arrays.toString(bytes));
                                        int bytesInt=byteLogicHandler.handle();
                                        bytes = Complements.leIntToByteArray(bytesInt);
                                        Log.d(TAG,"int: "+bytesInt+"elaborated bytes: "+Arrays.toString(bytes));
                                        int value = (bytes[0] & 0xFF);
                                        for (int i = 1; i < length; i++) {
                                            value |= (bytes[i]) << (Byte.SIZE * i);
                                        }
                                        return value;
                                    } else
                                        //TODO: maybe throw an exception
                                        return 0;
                                }

                            }
                        };

                }
            }


        }

        private static int getByteArrayFromString (String num){
//            byte[] ret;
            if (num.contains("0x")){
                String[] s=num.split("x");
                if (s.length!=2){
                    throw new StringToNumberConversionFormatException();
                }

                return Integer.parseInt(s[1], 16);
//                if (s[1].length()==4){
//                    ret=new byte[2];
//
//                }
            }
            else
                return Integer.parseInt(num);
        }


}
















