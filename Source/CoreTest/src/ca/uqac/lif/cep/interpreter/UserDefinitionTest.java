/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2015 Sylvain Hallé

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
package ca.uqac.lif.cep.interpreter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.epl.QueueSource;
import ca.uqac.lif.cep.epl.Window;
import ca.uqac.lif.cep.eml.tuples.EmlPuller;
import ca.uqac.lif.cep.eml.tuples.EmlPuller.EmlPullable;
import ca.uqac.lif.cep.eml.tuples.EmlNumber;
import ca.uqac.lif.cep.eml.tuples.NamedTuple;
import ca.uqac.lif.cep.eml.tuples.Select;
import ca.uqac.lif.cep.eml.tuples.TupleFeeder;
import ca.uqac.lif.cep.eml.tuples.TupleGrammar;
import ca.uqac.lif.cep.interpreter.Interpreter.ParseException;
import ca.uqac.lif.cep.io.StreamGrammar;
import ca.uqac.lif.cep.io.StreamReader;
import ca.uqac.lif.cep.ltl.LtlGrammar;

public class UserDefinitionTest 
{
	protected InterpreterTestFrontEnd m_interpreter;

	@Before
	public void setUp()
	{
		m_interpreter = new InterpreterTestFrontEnd();		
		m_interpreter.extendGrammar(StreamGrammar.class);
		m_interpreter.extendGrammar(TupleGrammar.class);
		m_interpreter.extendGrammar(LtlGrammar.class);
	}
	
	@Test
	public void testPlaceholder1() throws ParseException
	{
		String expression = "@P";
		QueueSource qs = new QueueSource(new Integer(1), 1);
		m_interpreter.addPlaceholder("@P", "processor", qs);
		Object o = m_interpreter.parseQuery(expression);
		assertNotNull(o);
		assertTrue(o instanceof Processor);	
		Processor s = (Processor) o;
		Pullable p = s.getPullableOutput(0);
		Number n = (Number) p.pullHard();
		assertNotNull(n);
		assertEquals(1, n.intValue());
		n = (Number) p.pullHard();
		assertNotNull(n);
		assertEquals(1, n.intValue());
	}
	
	@Test
	public void testPlaceholder2() throws ParseException
	{
		String expression = "SELECT x FROM (@P)";
		QueueSource qs = new QueueSource(1, 1);
		m_interpreter.addPlaceholder("@P", "processor", qs);
		Object o = m_interpreter.parseQuery(expression);
		assertNotNull(o);
		assertTrue(o instanceof Select);	
		Select s = (Select) o;
		Pullable p = s.getPullableOutput(0);
		Number n = (Number) p.pullHard();
		assertNotNull(n);
		assertEquals(1, n.intValue());
	}
	
	@Test
	public void testPlaceholder3() throws ParseException
	{
		String expression = "abc IS THE processor @P";
		QueueSource qs = new QueueSource(1, 1);
		m_interpreter.executeQuery(expression);
		m_interpreter.addPlaceholder("@P", "processor", qs);
		Object o = m_interpreter.parseQuery("SELECT x FROM (abc)");
		assertNotNull(o);
		assertTrue(o instanceof Select);	
		Select s = (Select) o;
		Pullable p = s.getPullableOutput(0);
		Number n = (Number) p.pullHard();
		assertNotNull(n);
		assertEquals(1, n.intValue());
	}
	
	@Test
	public void testPlaceholder4() throws ParseException
	{
		StreamReader sr = new StreamReader();
		m_interpreter.addPlaceholder("@T", "p_reader", sr);
		Object o = m_interpreter.parseQuery("THE TUPLES OF @T");
		assertNotNull(o);
		assertTrue(o instanceof TupleFeeder);	
	}
	
	@Test
	public void testDefinition1() throws ParseException
	{
		String expression = "WHEN @P IS A processor: THE COUNT OF ( @P ) IS THE processor COMBINE (SELECT 1 FROM (@P)) WITH SUM";
		Object o = m_interpreter.parseQuery(expression);
		assertNotNull(o);
		assertTrue(o instanceof UserDefinition);
		UserDefinition user_def = (UserDefinition) o;
		user_def.addToInterpreter(m_interpreter);
		// Now, parse an expression that uses this definition
		String user_expression = "THE COUNT OF (0)";
		//m_interpreter.setDebugMode(true);
		Object user_stmt = m_interpreter.parseQuery(user_expression);
		assertNotNull(user_stmt);
		assertTrue(user_stmt instanceof Processor);
		Pullable p = ((Processor) user_stmt).getPullableOutput(0);
		// Pull a tuple from the resulting processor
		Object answer = p.pull();
		assertNotNull(answer);
		assertTrue(answer instanceof Number);
		Number num = (Number) answer;
		assertEquals(1, num.intValue());
		// Pull another
		num = (Number) p.pull();
		assertEquals(2, num.intValue());
		// Pull another
		num = (Number) p.pull();
		assertEquals(3, num.intValue());
	}
	
	@Test
	public void testDefinition2() throws ParseException
	{
		String expression = "PI IS THE eml_number 3.1416";
		Object o = m_interpreter.parseQuery(expression);
		assertNotNull(o);
		assertTrue(o instanceof UserDefinition);
		UserDefinition user_def = (UserDefinition) o;
		user_def.addToInterpreter(m_interpreter);
		// Now, parse an expression that uses this definition
		String user_expression = "PI";
		Object user_stmt = m_interpreter.parseQueryLifted(user_expression);
		assertTrue(user_stmt instanceof Processor);
		Pullable p = ((Processor) user_stmt).getPullableOutput(0);
		// Pull a tuple from the resulting processor
		Object answer = p.pull();
		assertNotNull(answer);
		assertTrue(answer instanceof Number);
		Number num = (Number) answer;
		assertEquals(3.1416, num.floatValue(), 0.01);
		// Pull another
		num = (Number) p.pull();
		assertEquals(3.1416, num.floatValue(), 0.01);
	}
	
	@Test
	public void testDefinition3() throws ParseException
	{
		UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("E IS THE eml_number 2");
		assertNotNull(e_def);
		e_def.addToInterpreter(m_interpreter);
		Processor proc = (Processor) m_interpreter.parseQuery("SELECT E FROM (1)");
		assertNotNull(proc);
		Pullable p = proc.getPullableOutput(0);
		Number number = (Number) p.pull();
		assertEquals(2, number.intValue());
	}
	
	@Test
	public void testDefinition4() throws ParseException
	{
		{
			UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("WHEN @P IS A processor: THE COUNT OF ( @P ) IS THE processor COMBINE (SELECT 1 FROM (@P)) WITH SUM");
			e_def.addToInterpreter(m_interpreter);
		}
		{
			UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("WHEN @P IS A processor: THE INVERSE OF ( @P ) IS THE processor SELECT (T.x) ÷ (U.x) FROM ((SELECT x FROM (@P)) AS T, (THE COUNT OF (@P)) AS U)");
			e_def.addToInterpreter(m_interpreter);
		}
		Processor proc = (Processor) m_interpreter.parseQuery("THE INVERSE OF (1)");
		assertNotNull(proc);
		Pullable p = proc.getPullableOutput(0);
		Number number = (Number) p.pull();
		assertEquals(1, number.floatValue(), 0.01);
		number = (Number) p.pull();
		assertEquals(0.5, number.floatValue(), 0.01);
		number = (Number) p.pull();
		assertEquals(0.33, number.floatValue(), 0.01);
	}
	
	@Test
	public void testDefinition5() throws ParseException
	{
		{
			UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("WHEN @P IS A processor: THE COUNT OF ( @P ) IS THE processor COMBINE (SELECT 1 FROM (@P)) WITH SUM");
			e_def.addToInterpreter(m_interpreter);
		}
		{
			UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("WHEN @P IS A processor: THE AVERAGE OF ( @P ) IS THE processor SELECT (T.x) ÷ (U.x) FROM ((COMBINE (SELECT x FROM (@P)) WITH SUM) AS T, (THE COUNT OF (@P)) AS U)");
			e_def.addToInterpreter(m_interpreter);
		}
		Processor proc = (Processor) m_interpreter.parseQuery("THE AVERAGE OF (SELECT a FROM (THE TUPLES OF FILE \"tuples3.csv\"))");
		assertNotNull(proc);
		Pullable p = proc.getPullableOutput(0);
		Number number = (Number) p.pull();
		assertEquals(0, number.floatValue(), 0.01);
		number = (Number) p.pull();
		//assertEquals(1, number.floatValue(), 0.01);
		number = (Number) p.pull();
		//assertEquals(1, number.floatValue(), 0.01);
		number = (Number) p.pull();
		//assertEquals(2, number.floatValue(), 0.01);
	}
	
	@Test
	public void testDefinition6() throws ParseException
	{
		{
			UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("WHEN @P IS A processor: FOO ( @P ) IS THE processor SELECT T.a AS x, U.a AS y FROM (@P AS T, @P AS U)");
			e_def.addToInterpreter(m_interpreter);
		}
		Processor proc = (Processor) m_interpreter.parseQuery("FOO (SELECT a FROM (THE TUPLES OF FILE \"tuples3.csv\"))");
		assertNotNull(proc);
		Pullable p = proc.getPullableOutput(0);
		assertNotNull(p);
		NamedTuple tuple = (NamedTuple) p.pull();
		assertEquals(0, tuple.get("x"));
		assertEquals(0, tuple.get("y"));
		tuple = (NamedTuple) p.pull();
		assertEquals(6, tuple.get("x"));
		assertEquals(6, tuple.get("y"));
	}
	
	@Test
	public void testDefinition7() throws ParseException
	{
		{
			UserDefinition e_def = (UserDefinition) m_interpreter.parseQuery("WHEN @P IS A processor: THE SUM OF ( @P ) IS THE processor COMBINE (@P) WITH SUM");
			e_def.addToInterpreter(m_interpreter);
		}
		QueueSource qs = new QueueSource(1, 1);
		m_interpreter.addPlaceholder("@T", "processor", qs);
		Processor proc = (Processor) m_interpreter.parseQuery("APPLY (THE SUM OF (*)) ON (@T) ON A WINDOW OF 5");
		assertNotNull(proc);
		assertTrue(proc instanceof Window);
		EmlPullable p = EmlPuller.getEmlPullable(proc, 0);
		assertNotNull(p);
		Object o = p.pullHard();
		assertNotNull(o);
		assertTrue(o instanceof Number);
		float n = EmlNumber.parseFloat(o);
		assertEquals(5, n, 0.01);
	}
	
	@Test
	public void testMultipleQueries1() throws ParseException, IOException
	{
		InputStream is = this.getClass().getResourceAsStream("test.esql");
		Object o = m_interpreter.executeQueries(is);
		assertNotNull(o);
	}
	
	@Test
	public void testMultipleQueries2() throws ParseException, IOException
	{
		InputStream is = this.getClass().getResourceAsStream("test2.esql");
		m_interpreter.executeQueries(is);
	}
	
	@Test
	public void testMultipleQueries3() throws ParseException, IOException
	{
		InputStream is = this.getClass().getResourceAsStream("test3.esql");
		Object o = m_interpreter.executeQueries(is);
		assertNotNull(o);
	}	
}
