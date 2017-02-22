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

import java.util.List;

import it.unibs.sandroide.lib.complements.expressionParser.ExpressionNode;
import it.unibs.sandroide.lib.complements.expressionParser.Parser;
import it.unibs.sandroide.lib.complements.expressionParser.VariableExpressionNode;

/**
 * Handler for the byte logic operation
 */
public class ByteLogicHandler {

    ExpressionNode logicFunction;
    List<VariableExpressionNode> bytes;
    String byteLogic;

    /**
     * Constructor
     * @param byteLogic The expression defining the byte logic operation
     */
    public ByteLogicHandler(String byteLogic){
        this.byteLogic=byteLogic;
        Parser parser = new Parser();
        this.logicFunction = parser.parse(byteLogic);
        this.bytes=parser.getVariableExpressionNodes();
    }

    /**
     * Sets the values of the variables of the expression (the incoming values)
     * @param input the values of the variable of the expression (the incoming values)
     */
    public boolean setValue(byte[] input){
        if (bytes.size()==input.length){
            for (int i=0;i<input.length;i++){
                bytes.get(i).setValue((input[i]&0xFF));
            }
            return true;
        } else{
            return false;
        }
    }

    /**
     * @return the results of the byte logic function (in order to get the results of the incoming
     * data is necessary use {@link ByteLogicHandler#setValue(byte[])} before handle)
     */
    public int handle(){
        return (int)logicFunction.getValue();
    }

    /**
     * Clones the {@link ByteLogicHandler}
     */
    public ByteLogicHandler clone(){
        return new ByteLogicHandler(byteLogic);
    }

}
