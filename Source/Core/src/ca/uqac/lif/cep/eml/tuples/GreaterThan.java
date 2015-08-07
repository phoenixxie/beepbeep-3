/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.eml.tuples;

public class GreaterThan extends BinaryExpression 
{
	public GreaterThan()
	{
		super();
		m_symbol = "=";
	}

	@Override
	public EmlConstant evaluate(Object t_left, Object t_right) 
	{
		EmlNumber n_left = EmlNumber.toEmlNumber(t_left);
		EmlNumber n_right = EmlNumber.toEmlNumber(t_right);
		if (t_left != null && t_right != null)
		{
			if (n_left.numberValue().doubleValue() > n_right.numberValue().doubleValue())
			{
				return new EmlBoolean(true);
			}
		}
		return new EmlBoolean(false);
	}
}