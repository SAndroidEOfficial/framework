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

/**
 * An ExpressionNode that handles additions and subtractions. The node can hold
 * an arbitrary number of terms that are either added or subtraced from the sum.
 * 
 */
public class BitwiseAndExpressionNode extends SequenceExpressionNode
{


  /**
   * Constructor to create a bitwise and with the first term already added.
   *
   * @param node
   *          the node containing the value to be masked
   * @param positive
   *          the mask for the bitwise and operation
   */
  public BitwiseAndExpressionNode(ExpressionNode node, boolean positive)
  {
    super(node, positive);
  }

  /**
   * Returns the type of the node, in this case ExpressionNode.ADDITION_NODE
   */
  public int getType()
  {
    return ExpressionNode.BITWISE_AND_NODE;
  }

  /**
   * Returns the value of the sub-expression that is rooted at this node.
   *
   */
  public double getValue()
  {

    int and = 0xFFFFFFFF;
    for (Term t : terms)
    {
      if (t.positive)
        and &= ((int)t.expression.getValue());
      else
        and &= -((int)t.expression.getValue());
    }
    return and;
  }

  /**
   * Implementation of the visitor design pattern.
   * 
   * Calls visit on the visitor and then passes the visitor on to the accept
   * method of all the terms in the sum.
   * 
   * @param visitor
   *          the visitor
   */
  public void accept(ExpressionNodeVisitor visitor)
  {
    visitor.visit(this);
    for (Term t : terms)
      t.expression.accept(visitor);
  }

}
