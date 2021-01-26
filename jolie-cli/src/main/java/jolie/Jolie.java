/*
 * Copyright (C) 2006-2019 Fabrizio Montesi <famontesi@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package jolie;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;
import jolie.cli.CommandLineException;
import jolie.cli.CommandLineParser;
import jolie.lang.parse.ParserException;
import jolie.runtime.Value;
import jolie.js.JsUtils;

/**
 * Starter class of the Interpreter.
 * 
 * @author Fabrizio Montesi
 */
public class Jolie {
	private static final long TERMINATION_TIMEOUT = 100; // 0.1 seconds

	static {
		JolieURLStreamHandlerFactory.registerInVM();
	}

	private Jolie() {}

	private static void printErr( Throwable t, boolean printStackTraces ) {
		String mesg;
		if( printStackTraces ) {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			t.printStackTrace( new PrintStream( bs ) );
			mesg = bs.toString();
		} else {
			mesg = t.getMessage();
		}
		System.err.println( mesg );
	}

	/**
	 * Entry point of program execution.
	 * 
	 * @param args the command line arguments TODO Standardize the exit codes.
	 */
	public static void main( String[] args ) {
		int exitCode = 0;
		// TODO: remove this hack by extracting CommandLineParser here
		boolean printStackTraces = Arrays.asList( args ).contains( "--stackTraces" );

		try {
			CommandLineParser commandLineParser = new CommandLineParser( args, Jolie.class.getClassLoader(), false );
			Interpreter.Configuration config = commandLineParser.getInterpreterConfiguration();
			Optional< Value > params = Optional.of( Value.create() );
			if( config.getParametersFilePath().isPresent() ) {
				File paramsFilepath = config.getParametersFilePath().get();
				JsUtils.parseJsonIntoValue( new FileReader( paramsFilepath ), params.get(), true );
			}
			final Interpreter interpreter = new Interpreter( config, null, params );
			Thread.currentThread().setContextClassLoader( interpreter.getClassLoader() );
			Runtime.getRuntime().addShutdownHook( new Thread( () -> interpreter.exit( TERMINATION_TIMEOUT ) ) );
			interpreter.run();
		} catch( CommandLineException cle ) {
			printErr( cle, printStackTraces );
		} catch( FileNotFoundException fe ) {
			printErr( new FileNotFoundException( "File not found " + fe.getMessage() ), printStackTraces );
			exitCode = 1;
		} catch( IOException ioe ) {
			printErr( ioe, printStackTraces );
			exitCode = 2;
		} catch( InterpreterException ie ) {
			if( ie.getCause() instanceof ParserException ) {
				printErr( ie.getCause(), printStackTraces );
			} else {
				printErr( ie, printStackTraces );
			}
			exitCode = 3;
		} catch( Exception e ) {
			printErr( e, printStackTraces );
			exitCode = 4;
		}
		System.exit( exitCode );
	}
}
