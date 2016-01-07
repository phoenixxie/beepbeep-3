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
package ca.uqac.lif.cep.io;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.QueueSink;
import ca.uqac.lif.util.PackageFileReader;
import ca.uqac.lif.util.StringUtils;

public class IoTest
{

	@Before
	public void setUp() throws Exception
	{
	}
	
	@Test
	public void testStreamReaderPush1() throws FileNotFoundException
	{
		String file_contents = PackageFileReader.readPackageFile(this.getClass(), "resource/test1.txt");
		InputStream stream = StringUtils.toInputStream(file_contents);
		StreamReader sr = new StreamReader(stream);
		QueueSink sink = new QueueSink(1);
		Connector.connect(sr, sink);
		sr.push();
		String recv = (String) sink.getQueue(0).remove();
		assertNotNull(recv);
		recv = recv.trim();
		assertEquals(35, recv.length());
	}
	
	@Test
	public void testUrlFeeder1()
	{
		HttpReader hr = new HttpReader("https://bitbucket.org/sylvainhalle/beepbeep-3/raw/4dcb7fa4330a079d532394d48ba8f519e5021148/tuples1.csv");
		Pullable p = hr.getPullableOutput(0);
		assertNotNull(p);
		Object o = p.pull();
		assertTrue(o instanceof String);
	}
	
}