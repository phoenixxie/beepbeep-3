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
package ca.uqac.lif.cep.epl;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Connector.ConnectorException;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SingleProcessor;

/**
 * Separates an input trace into different "slices". The slicer
 * takes as input a processor <i>p</i> and
 * a slicing function <i>f</i>.
 * There exists potentially one instance of <i>p</i> for each value
 * in the image of <i>f</i>.
 * <p>
 * When an event <i>e</i> arrives, the slicer evaluates
 * <i>c</i> = <i>f</i>(<i>e</i>). This value determines to what instance
 * of <i>p</i> the event will be dispatched.
 *  
 * @author Sylvain Hallé
 */
public class Slicer extends SingleProcessor
{
	/**
	 * The slicing function
	 */
	protected Function m_slicingFunction = null;
	
	/**
	 * The internal processor
	 */
	protected Processor m_processor = null;
	
	protected Map<Object,Processor> m_slices;
	
	protected Map<Object,QueueSink> m_sinks; 

	Slicer()
	{
		super(1, 1);
	}
	
	public Slicer(/*@NonNull*/ Function func, /*@NonNull*/ Processor proc)
	{
		super(proc.getInputArity(), proc.getOutputArity());
		m_processor = proc;
		m_slicingFunction = func;
		m_slices = new HashMap<Object,Processor>();
		m_sinks = new HashMap<Object,QueueSink>();
	}

	@Override
	protected Queue<Object[]> compute(Object[] inputs) 
	{
		int output_arity = getOutputArity();
		Object[] f_value = m_slicingFunction.evaluate(inputs);
		Object slice_id = f_value[0];
		if (!m_slices.containsKey(slice_id))
		{
			// First time we see this value: create new slice
			Processor p = m_processor.clone();
			m_slices.put(slice_id, p);
			QueueSink sink = new QueueSink(output_arity);
			try 
			{
				Connector.connect(p, sink);
			} 
			catch (ConnectorException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_sinks.put(slice_id, sink);
		}
		// Find processor corresponding to that slice
		Processor slice_p = m_slices.get(slice_id);
		QueueSink sink_p = m_sinks.get(slice_id);
		// Push the input into the processor
		for (int i = 0; i < inputs.length; i++)
		{

			Object o_i = inputs[i];
			Pushable p = slice_p.getPushableInput(i);
			p.push(o_i);
		}
		// Collect the output from that processor
		Object[] out = sink_p.remove();
		return wrapVector(out);
	}
	
	@Override
	public void reset()
	{
		super.reset();
		m_slices.clear();
		m_slicingFunction.reset();
	}
	
	public static void build(Stack<Object> stack) throws ConnectorException
	{
		Function f = (Function) stack.pop();
		stack.pop(); // ON
		stack.pop(); // (
		Processor p2 = (Processor) stack.pop();
		stack.pop(); // )
		stack.pop(); // WITH
		stack.pop(); // (
		Processor p1 = (Processor) stack.pop();
		stack.pop(); // )
		stack.pop(); // SLICE
		Slicer out = new Slicer(f, p2);
		Connector.connect(p1, out);
		stack.push(out);
	}
	
	@Override
	public Slicer clone()
	{
		return new Slicer(m_slicingFunction.clone(), m_processor.clone());
	}
}
