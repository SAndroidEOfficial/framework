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
package it.unibs.sandroide.lib.complements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic support class
 */
public class Complements {

    public static void writeFile(String f_path,String str)
    {
		File f=new File(f_path);


        if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}}

   	  FileWriter fw = null;
   	  BufferedWriter bw = null;
   	  try{
   	    fw = new FileWriter(f, true);
   	    bw = new BufferedWriter(fw);
   	    bw.write(str);
   	    bw.close();
   	    fw.close();
   	    
   	  } catch (IOException e) {
   	    e.printStackTrace(); 
   	  }	  
	}
    
    
    public static void writeFileNewLine(String f_path,String str)
    {
    	writeFile(f_path, str+'\r'+'\n');
    }
    
    
    public static List<String> readFileByLine(String f_path)
    {
	  File f=new File(f_path);
   	  List<String> lines=new ArrayList<String>();
   	  String line; 
   	  FileReader fr = null;
   	  BufferedReader br = null;
   	  try{
   	    fr = new FileReader(f);
   	    br = new BufferedReader(fr);
   	    while (true)
   	    {
   	    	line=br.readLine();
   	    	if (line!=null)
   	    		lines.add(line);
   	    	else
   	    	{
		   	    br.close();
		   	    fr.close();
		   	    break;
	   	    }
   	    }
   	    
   	  } catch (IOException e) {
   	    e.printStackTrace(); 
   	  }
   	  return lines;
	 }

//	public static int make_int(byte b1, byte b2)
//	{
//		/*byte zero=0x00;
//		return (int) (bigEndian ? ((zero<<16) | (zero<<12) | (b1 << 8) | (b2 & 0xFF)) : ((b2 << 16) | (b1 << 14) | (zero << 8) | (zero & 0xFF)));*/
//
//		ByteBuffer aux = ByteBuffer.allocate(4);
//		aux.put((byte)0);
//		aux.put((byte)0);
//		aux.put(b1);
//		aux.put(b2);
//		aux.position(0);
//		int ret=(int) aux.getInt();
//		return ret;
//
//	}


	public static int byteArrayToLeInt(byte[] encodedValue) {
		int value = (encodedValue[3] << (Byte.SIZE * 3));
		value |= (encodedValue[2] & 0xFF) << (Byte.SIZE * 2);
		value |= (encodedValue[1] & 0xFF) << (Byte.SIZE * 1);
		value |= (encodedValue[0] & 0xFF);
		return value;
	}

	public static byte[] leIntToByteArray(int value) {
		byte[] encodedValue = new byte[Integer.SIZE / Byte.SIZE];
		encodedValue[3] = (byte) (value >> Byte.SIZE * 3);
		encodedValue[2] = (byte) (value >> Byte.SIZE * 2);
		encodedValue[1] = (byte) (value >> Byte.SIZE);
		encodedValue[0] = (byte) value;
		return encodedValue;
	}

	public static byte[] beIntToByteArray(int value) {
		byte[] encodedValue = new byte[Integer.SIZE / Byte.SIZE];
		encodedValue[0] = (byte) (value >> Byte.SIZE * 3);
		encodedValue[1] = (byte) (value >> Byte.SIZE * 2);
		encodedValue[2] = (byte) (value >> Byte.SIZE);
		encodedValue[3] = (byte) value;
		return encodedValue;
	}

	public static int twoBytestoSignedInt(byte[] bytes)
	{
		int i = ((bytes[0] & 0xff) | (bytes[1] << 8)) << 16 >> 16;
		return i;
	}

	public static int bytes2LeSignedInt(byte[] bytes, int offset , int length) throws BufferOverflowException
	{

		if (length>4){
			throw new BufferOverflowException();
		} else {
			int value = (bytes[offset] & 0xFF);
			for (int i=1;i<length;i++){
				value |= (bytes[offset+i] & 0xFF) << (Byte.SIZE * i);
			}
			return value;
		}
	}

	public static byte [] long2ByteArray (long value)
	{
		return ByteBuffer.allocate(8).putLong(value).array();
	}

	public static byte [] float2ByteArray (float value)
	{
		return ByteBuffer.allocate(4).putFloat(value).array();
	}

	public static float byteArray2Float(byte[] bytes, int startOffset, int length) throws BufferOverflowException{
		byte[] buffer;
		if (length<=4){
			buffer=new byte[4];
			for (int i=0;i<length;i++)
				buffer[i]=bytes[startOffset+i];
		} else{
			throw new BufferOverflowException();
		}
		//float foo = Float.intBitsToFloat( buffer[n] ^ buffer[n+1]<<8 ^ buffer[n+2]<<16 ^ buffer[n+3]<<24 );
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
	}



}
