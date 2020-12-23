
package jolie.slicer;

import java.util.ArrayList;
import java.util.Collection;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ServiceNode;

/**
 * Slicer
 */
public class Slicer {

	public static Collection< Program > sliceProgramIntoServices( Program prog ) {
		// Program commonDefinitions = new Program(prog.context(), new ArrayList<>());
		Collection< OLSyntaxNode > commonDefs = new ArrayList<>();
		Collection< ServiceNode > services = new ArrayList<>();
		Collection< Program > slices = new ArrayList<>();

		for( OLSyntaxNode n : prog.children() ) {
			if( n instanceof ServiceNode ) {
				services.add( (ServiceNode) n );
				// System.out.println( ((ServiceNode) n).name() );
				// slices.add( new Program(prog.context(), Arrays.asList(n)));
			} else {
				commonDefs.add( n );
			}
		}
		// System.out.println( commonDefs.toString() );
		for( ServiceNode n : services ) {
			ArrayList< OLSyntaxNode > children = new ArrayList<>();
			children.addAll( commonDefs );
			children.add( n );
			slices.add( new Program( prog.context(), children ) );
			// children.clear();
		}
		return slices;
	}

}
