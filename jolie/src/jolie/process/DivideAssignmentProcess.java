/***************************************************************************
 *   Copyright (C) 2011 by Karoly Szanto                                   *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/

package jolie.process;

import jolie.ExecutionThread;
import jolie.lang.Constants;
import jolie.runtime.FaultException;
import jolie.runtime.expression.Expression;
import jolie.runtime.Value;
import jolie.runtime.VariablePath;
import jolie.runtime.typing.TypeCastingException;

/**
 * Divide a VariablePath's value with an expression value, assigning the resulting
 * value to the VariablePath.
 * @see Expression
 * @see VariablePath
 * @author Karoly Szanto
 */
public class DivideAssignmentProcess implements Process, Expression
{
	final private VariablePath varPath;
	final private Expression expression;

	/** Constructor.
	 *
	 * @param varPath the variable which will receive the value
	 * @param expression the expression to be evaluated and used to divide the variable's value
	 */
	public DivideAssignmentProcess( VariablePath varPath, Expression expression )
	{
		this.varPath = varPath;
		this.expression = expression;
	}

	public Process clone( TransformationReason reason )
	{
		return new DivideAssignmentProcess(
			(VariablePath) varPath.cloneExpression( reason ),
			expression.cloneExpression( reason ) );
	}

	public Expression cloneExpression( TransformationReason reason )
	{
		return new DivideAssignmentProcess(
			(VariablePath) varPath.cloneExpression( reason ),
			expression.cloneExpression( reason ) );
	}

	/** Evaluates the expression and adds its value to the variable's value. */
	public void run() throws FaultException
	{
		if ( ExecutionThread.currentThread().isKilled() ) {
			return;
		}
        try {
            varPath.getValue().divide( expression.evaluate() );
        } catch ( TypeCastingException e ){
            new FaultException(
              Constants.CASTING_EXCEPTION_FAULT_NAME,
              "Could not divide non-numberic values"
            );
        } catch ( ArithmeticException e ){
            new FaultException(
              Constants.ARITHMETIC_EXCEPTION_FAULT_NAME,
              "Division by zero"
            );
        }
	}

	public Value evaluate() throws FaultException
	{
		Value val = varPath.getValue();
		try {
            val.divide( expression.evaluate() );
        } catch ( TypeCastingException e ){
            new FaultException(
              Constants.CASTING_EXCEPTION_FAULT_NAME,
              "Could not divide non-numberic values"
            );
        } catch ( ArithmeticException e ){
            new FaultException(
              Constants.ARITHMETIC_EXCEPTION_FAULT_NAME,
              "Division by zero"
            );
        }
		return val;
	}

	public boolean isKillable()
	{
		return true;
	}
}
