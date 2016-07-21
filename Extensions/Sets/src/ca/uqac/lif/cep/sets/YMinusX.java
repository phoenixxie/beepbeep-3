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
package ca.uqac.lif.cep.sets;

import ca.uqac.lif.cep.functions.BinaryFunction;

import java.util.HashSet;
import java.util.Set;

/**
 * Evaluates if the first set is a subset or is equal to the second set.
 * 
 * @author Sylvain Hallé
 */
@SuppressWarnings("rawtypes")
public class YMinusX extends BinaryFunction<Set,Set,Set>
{
	public static final transient YMinusX instance = new YMinusX();

	YMinusX()
	{
		super(Set.class, Set.class, Set.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set getValue(Set x, Set y)
	{
		Set z = new HashSet();
		z.retainAll(y);
		z.removeAll(x);
		return z;
	}
}
