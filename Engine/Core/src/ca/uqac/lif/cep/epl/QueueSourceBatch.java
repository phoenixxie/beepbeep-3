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

import java.util.LinkedList;
import java.util.Queue;

/**
 * Operates like {@link QueueSource}, except that it dumps all its
 * queue at once, instead of releasing the events one by one.
 * @author Sylvain Hallé
 *
 */
public class QueueSourceBatch extends QueueSource 
{
	public QueueSourceBatch(Object o, int arity) 
	{
		super(o, arity);
	}
	
	@Override
	protected Queue<Object[]> compute(Object[] inputs)
	{
		Queue<Object[]> queue = new LinkedList<Object[]>();
		int output_arity = getOutputArity();
		for (Object event : m_events)
		{
			Object[] out = new Object[output_arity];
			for (int i = 0; i < output_arity; i++)
			{
				out[i] = event;
			}
			queue.add(out);
		}
		return queue;
	}
}
