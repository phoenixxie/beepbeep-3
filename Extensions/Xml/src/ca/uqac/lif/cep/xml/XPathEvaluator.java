/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2016 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.xml;

import java.util.Collection;

import ca.uqac.lif.cep.functions.FunctionProcessor;
import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.xml.XPathExpression;
import ca.uqac.lif.xml.XmlElement;

public class XPathEvaluator extends FunctionProcessor
{	
	public XPathEvaluator(XPathExpression exp)
	{
		super(new XPathFunction(exp));
	}



    /**
	 * Function that converts a string into an XML element
	 */
	public static class XPathFunction extends UnaryFunction<XmlElement,Collection<XmlElement>> 
	{
		/**
		 * The XPath expression this function evaluates
		 */
		private final XPathExpression m_expression;
		
		/**
		 * Creates a new XPath function
		 * @param exp The XPath expression to evaluate
		 */
		@SuppressWarnings("unchecked")
		public XPathFunction(XPathExpression exp)
		{
			/* The double cast below is a bit of trickery to pass the
			 * runtime type of the collection. It was found here:
			 * http://stackoverflow.com/a/30754982
			 */
			super(XmlElement.class, (Class<Collection<XmlElement>>) (Object) Collection.class);
			m_expression = exp;
		}
		
		@Override
		public /*@NonNull*/ Collection<XmlElement> getValue(/*NonNull*/ XmlElement x)
		{
			return m_expression.evaluate(x);
		}
	}
}
