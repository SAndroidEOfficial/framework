/**
 * Original work  Copyright (c) 2013 Cogito Learning Ltd
 * Modified Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
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
package it.unibs.sandroide.lib.complements.expressionParser;

import android.util.Log;

/**
 * An ExpressionNode that handles exponentiation. The node holds
 * a base and an exponent and calulates base^exponent 
 * 
 */
public class LeftShiftExpressionNode implements ExpressionNode
{


  public final static String TAG="LeftShiftExpressionNode";

  /** the node containing the base */
  private ExpressionNode base;
  /** the node containing the number of bits to shift */
  private ExpressionNode shiftBits;

  /**
   * Construct the ExponentiationExpressionNode with base and exponent
   * @param base the node containing the base
   * @param shiftBits the node containing the number of bits to shift
   */
  public LeftShiftExpressionNode(ExpressionNode base, ExpressionNode shiftBits)
  {
    this.base = base;
    this.shiftBits = shiftBits;
  }

  /**
   * Returns the type of the node, in this case ExpressionNode.LEFT_SHIFT_NODE
   */
  public int getType()
  {
    return ExpressionNode.LEFT_SHIFT_NODE;
  }
  
  /**
   * Returns the value of the sub-expression that is rooted at this node.
   * 
   * Calculates the shifted value
   */
  public double getValue()
  {
    int bas=((int)base.getValue());
    int sh=(int)shiftBits.getValue();
    Log.d(TAG, "base: "+bas+", sh:"+sh);
    return bas << sh;
  }

  /**
   * Implementation of the visitor design pattern.
   * 
   * Calls visit on the visitor and then passes the visitor on to the accept
   * method of the base and the exponent.
   * 
   * @param visitor
   *          the visitor
   */
  public void accept(ExpressionNodeVisitor visitor)
  {
    visitor.visit(this);
    base.accept(visitor);
    shiftBits.accept(visitor);
  }
}
