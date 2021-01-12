/***************************************************************************
 *   Copyright (C) 2020 by Valentino Picotti                               *
 *   Copyright (C) 2020 by Fabrizio Montesi                                *
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

package jolie.slicer;

import java.io.IOException;
import java.util.Collection;
import jolie.CommandLineException;
import jolie.Interpreter;
import jolie.JolieURLStreamHandlerFactory;
import jolie.lang.CodeCheckingException;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.module.ModuleException;
import jolie.lang.parse.util.ParsingUtils;
import jolie.util.Pair;

/**
 *
 * @author Valentino
 */
public class Main {

	static {
		JolieURLStreamHandlerFactory.registerInVM();
	}

	// private static final boolean INCLUDE_DOCUMENTATION = false;

	public static void main( String[] args ) {
		System.out.println( "Hello world!" );

		// TODO: Extend CommandLineParser with a new option to slice a service
		// - Understand if its okey to extend the command line parser or what
		// - There are a lot of things that the constructor of CLP does that maybe
		// we are not inrested in
		try {
			JolieSlicerCommandLineParser cmdLnParser =
				JolieSlicerCommandLineParser.create( args, Main.class.getClassLoader() );
			if( cmdLnParser.getOutputDirectory() == null ) {
				throw new CommandLineException( "Missing output directory (-o output_dir)" );
			}
			Interpreter.Configuration intConf = cmdLnParser.getInterpreterConfiguration();
			// ModuleParsingConfiguration config =
			// new ModuleParsingConfiguration( intConf.charset(),
			// intConf.includePaths(), intConf.packagePaths(),
			// intConf.jolieClassLoader(), intConf.constants(),
			// INCLUDE_DOCUMENTATION );

			// ModuleParser parser = new ModuleParser( config );
			// ModuleRecord mainRecord = parser.parse(
			// new Scanner( intConf.inputStream(), intConf.programFilepath().toURI(),
			// config.charset(), config.includeDocumentation() ) );

			// /*
			// * A program before the SymbolReferenceResolver is phase is run
			// */
			// Program rawProgram = mainRecord.program();

			JoliePrettyPrinter pp = new JoliePrettyPrinter();

			// pp.visit( rawProgram );

			// Modules.ModuleParsedResult parseResult =
			// Modules.parseModule( config, intConf.inputStream(),
			// intConf.programFilepath().toURI() );


			/**
			 * The semantic verifier introduces output port definitions for embedded services We pretty print
			 * the program before it introduces names which we don't know from where they come from.
			 */
			// SemanticVerifier semanticVerifier =
			// new SemanticVerifier( parseResult.mainProgram(),
			// parseResult.symbolTables(),
			// new SemanticVerifier.Configuration( intConf.executionTarget() ) );
			// semanticVerifier.validate();

			// SymbolTable st = SymbolTableGenerator.generate( program );
			// new ModuleRecord( scanner.source(), program, st );

			Program program = ParsingUtils.parseProgram(
				intConf.inputStream(),
				intConf.programFilepath().toURI(),
				intConf.charset(),
				intConf.includePaths(),
				intConf.packagePaths(),
				intConf.jolieClassLoader(),
				intConf.constants(),
				intConf.executionTarget(), false );

			Collection< Pair< String, Slicer.ServiceInformation< Program > > > services =
				Slicer.sliceProgramIntoServices( program );

			// int i = 0;
			for( Pair< String, Slicer.ServiceInformation< Program > > service : services ) {
				System.out.println( "Service " + service.key() + ":" );
				JoliePrettyPrinter prettyService = new JoliePrettyPrinter();
				prettyService.visit( service.value().getNode() );
				System.out.println( prettyService.toString() );
			}
			// pp.visit( program );
			// pp.prettyPrint( parseResult );
			pp.visit( program );

			Slicer.generateServiceDirectories( cmdLnParser.getOutputDirectory(), services );
			// System.out.println( pp.toString() );

			// ProgramInspector inspector = ParsingUtils.createInspector( program );

			// System.out.println( "Found the following services:" );
			// for( ServiceNode s : inspector.getServiceNodes() ) {
			// System.out.println( s.name() );
			// }
			cmdLnParser.close();
		} catch( CommandLineException e ) {
			System.out.println( e.getMessage() );
		} catch( IOException | ParserException | CodeCheckingException | ModuleException e ) {
			e.printStackTrace();
		}
	}
}
