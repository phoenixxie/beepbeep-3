/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015 Sylvain Hallé

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
package ca.uqac.lif.cep.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

public class CommandRunner extends Thread
{
	protected String[] m_command;
	
	protected byte[] m_stdin;
	
	protected volatile boolean m_stop = false; 
	
	protected StreamGobbler m_stdoutGobbler;
	
	protected StreamGobbler m_stderrGobbler;
	
	protected int m_errorCode = 0;

	/**
	 * Creates a CommandRunner to run a command.
	 * @param command The command to run
	 * @param stdin If not set to null, this string will be sent to the stdin
	 *   of the command being run
	 */
	public CommandRunner(List<String> command, byte[] stdin)
	{
		super();
		m_command = new String[command.size()];
		int i = 0;
		for (String part : command)
		{
			m_command[i++] = part;
		}
		m_stdin = stdin;
	}
	
	/**
	 * Creates a CommandRunner to run a command.
	 * @param command The command to run
	 * @param stdin If not set to null, this array of bytes
	 * will be sent to the stdin of the command being run
	 */
	public CommandRunner(String[] command, byte[] stdin)
	{
		super();
		m_command = command;
		m_stdin = stdin;
	}
	
	/**
	 * Creates a CommandRunner to run a command.
	 * @param command The command to run
	 * @param stdin If not set to null, this string will be sent to the stdin
	 *   of the command being run
	 */
	public CommandRunner(String[] command, String stdin)
	{
		this(command, stdin.getBytes());
	}
	
	/**
	 * Creates a CommandRunner to run a command.
	 * @param command The command to run
	 */
	public CommandRunner(String[] command)
	{
		this(command, new byte[0]);
	}

	/**
	 * Constantly reads an input stream and captures its content.
	 * Inspired from <a href="http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki">Stack Overflow</a>
	 */
	protected class StreamGobbler extends Thread
	{
		InputStream m_is;
		Vector<Byte> m_contents;
		String m_name;

		private StreamGobbler(InputStream is, String name)
		{
			super();
			m_contents = new Vector<Byte>();
			this.m_is = is;
			m_name = name;
		}

		@Override
		public void run()
		{
			try
			{
				byte[] buffer = new byte[8192];
				int len = -1;
				while (!m_stop && (len = m_is.read(buffer)) > 0)
				{
					synchronized (this)
					{
						for (int i = 0; i < len; i++)
						{
							m_contents.add(buffer[i]);
						}
					}
				}
				m_is.close();
			}
			catch (IOException ioe) 
			{
				ioe.printStackTrace();
			} 
		}

		/**
		 * Returns the contents captured by the gobbler as an array of bytes
		 * @return The contents
		 */
		public synchronized byte[] getBytes()
		{
			int size = m_contents.size();
			byte[] out = new byte[size];
			int i = 0;
			for (byte b : m_contents)
			{
				out[i++] = b;
			}
			return out;
		}
	}

	/**
	 * Checks whether a file exists in the filesystem
	 * @param filename The filename to look for
	 * @return true if file exists, false otherwise
	 */
	public static boolean fileExists(String filename)
	{
		File f = new File(filename);
		return f.exists();
	}

	/**
	 * Replace the extension of a filename with another. For example,
	 * one can replace /my/path/foo.bar with /my/path/foo.baz.
	 * @param filename The original filename
	 * @param extension The extension to replace with
	 * @return The modified filename
	 */
	public static String replaceExtension(String filename, String extension)
	{
		String without_extension = trimExtension(filename);
		return without_extension + "." + extension;
	}

	/**
	 * Trims the extension of a filename. For example, with /my/path/foo.bar,
	 * would return /my/path/foo
	 * @param filename The filename
	 * @return The filename without the extension
	 */
	public static String trimExtension(String filename)
	{
		int position = filename.lastIndexOf(".");
		if (position < 0)
			return filename;
		return filename.substring(0, position);
	}
	
	/**
	 * Deletes a file
	 * @param filename The filename
	 * @return true if the file could be deleted, false otherwise
	 */
	public static boolean deleteFile(String filename)
	{
		File f = new File(filename);
		return f.delete();
	}
	
	public static byte[] runAndGet(String command, String inputs)
	{
		return runAndGet(command, inputs.getBytes());
	}
	
	public static byte[] runAndGet(String command, byte[] inputs)
	{
		String[] s_command = new String[1];
		s_command[0] = command;
		CommandRunner runner = new CommandRunner(s_command, inputs);
		runner.run();
		// Wait until the command is done
		while (runner.isAlive())
		{
			// Wait 0.1 s and check again
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e) 
			{
				// This happens if the user cancels the command manually
				runner.stopCommand();
				runner.interrupt();
				System.err.println("Interrupted");
				return null;
			}
		}
		byte[] out = runner.getBytes();
		return out;
	}

	@Override
	public void run()
	{
		ProcessBuilder builder = new ProcessBuilder(m_command);
		Process process = null;
		try 
		{
			process = builder.start();
			m_stderrGobbler = new StreamGobbler(process.getErrorStream(), "ERR");
			m_stdoutGobbler = new StreamGobbler(process.getInputStream(), "IN");
			// Send data into stdin of process
			if (m_stdin != null)
			{
				OutputStream process_stdin = process.getOutputStream();
				process_stdin.write(m_stdin, 0, m_stdin.length);
				process_stdin.flush();
				process_stdin.close();
				//System.out.println("Writing " + stdin_bytes.length + " bytes");
			}
			// Start gobblers
			m_stderrGobbler.start();
			m_stdoutGobbler.start();
			m_errorCode = process.waitFor();
			do
			{
				// Wait for both gobblers to finish
			} while (!m_stop && (m_stderrGobbler.isAlive() || m_stdoutGobbler.isAlive()));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e) 
		{
			// Destroy the running command
			if (process != null)
			{
				process.destroy();
			}
		}
		//System.err.println(new String(error_gobbler.getBytes()));
	}
	
	/**
	 * Gets the contents of stdout sent by the command as an array of bytes
	 * @return The contents of stdout
	 */
	synchronized public byte[] getBytes()
	{
		return m_stdoutGobbler.getBytes();
	}
	
	/**
	 * Gets the contents of stdout sent by the command as a string
	 * @return The contents of stdout
	 */
	synchronized public String getString()
	{
		byte[] out = m_stdoutGobbler.getBytes();
		return new String(out);
	}
	
	synchronized public void stopCommand()
	{
		m_stop = true;
	}

}
