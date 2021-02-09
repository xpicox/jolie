
package jolie.slicer;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jolie.lang.parse.ast.EmbedServiceNode;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ServiceNode;
import jolie.lang.parse.ast.expression.ConstantStringExpression;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Pair;

/**
 * Slicer
 */
public class Slicer {

	public static class ServiceInformation< N extends OLSyntaxNode > {
		private final N serviceNode;
		private List< String > dependsOn;

		ServiceInformation( N serviceNode, List< String > dependsOn ) {
			this.serviceNode = serviceNode;
			this.dependsOn = dependsOn;
		}

		ServiceInformation( N serviceNode ) {
			this( serviceNode, new ArrayList<>() );
		}

		void addDependency( String serviceName ) {
			dependsOn.add( serviceName );
		}

		public N getNode() {
			return serviceNode;
		}

		public List< String > getDependsOn() {
			return dependsOn;
		}

		public void setDependsOn( List< String > dependsOn ) {
			this.dependsOn = dependsOn;
		}
	}

	public static Collection< Pair< String, ServiceInformation< Program > > > sliceProgramIntoServices( Program prog ) {
		Collection< OLSyntaxNode > commonDefs = new ArrayList<>();
		Map< String, ServiceInformation< ServiceNode > > servicesMap = new HashMap<>();
		Collection< Pair< String, ServiceInformation< Program > > > slices = new ArrayList<>();

		for( OLSyntaxNode n : prog.children() ) {
			if( n instanceof ServiceNode ) {
				ServiceNode s = (ServiceNode) n;
				servicesMap.put( s.name(), new ServiceInformation<>( s ) );
				// System.out.println( ((ServiceNode) n).name() );
				// slices.add( new Program(prog.context(), Arrays.asList(n)));
			} else {
				commonDefs.add( n );
			}
		}
		// Set< String > serviceNames = servicesMap.keySet();
		// System.out.println( commonDefs.toString() );
		updateServiceLocationsAndCollectInformations( servicesMap );
		for( ServiceInformation< ServiceNode > si : servicesMap.values() ) {
			Program p = new Program( prog.context(), new ArrayList<>( commonDefs ) );
			p.children().add( si.getNode() );
			ServiceInformation< Program > pi = new ServiceInformation<>( p, si.getDependsOn() );
			slices.add( new Pair<>( si.getNode().name(), pi ) );
		}
		return slices;
	}

	private static void updateServiceLocationsAndCollectInformations(
		Map< String, ServiceInformation< ServiceNode > > serviceMap ) {
		Set< String > serviceNames = serviceMap.keySet();
		List< OLSyntaxNode > toRemove = new ArrayList<>();
		// Map< String, ServiceNode > newServiceMap = new HashMap<>();
		for( Map.Entry< String, ServiceInformation< ServiceNode > > service : serviceMap.entrySet() ) {
			List< OLSyntaxNode > serviceStatements = service.getValue().getNode().program().children();
			for( OLSyntaxNode child : serviceStatements ) {
				if( child instanceof EmbedServiceNode ) {
					EmbedServiceNode embedNode = (EmbedServiceNode) child;
					// change the outputport of an embeded-as service and remove the embed statement
					if( embedNode.isNewPort() && serviceNames.contains( embedNode.serviceName() ) ) {
						OutputPortInfo op = embedNode.bindingPort();
						op.setLocation( getLocationForService( embedNode.context(), embedNode.serviceName() ) );
						op.setProtocol( new ConstantStringExpression( embedNode.context(),
							"http { format = \"json\" }" ) );
						toRemove.add( embedNode );
						service.getValue().addDependency( embedNode.serviceName() );
					} else if( embedNode.isNewPort() ) { // remove auto-generated outputport
						toRemove.add( embedNode.bindingPort() );
					}
				} else if( child instanceof InputPortInfo ) {
					InputPortInfo ip = (InputPortInfo) child;
					OLSyntaxNode location = getLocationForService( ip.location().context(),
						service.getKey() );
					ip.setLocation( location );
					ip.setProtocol( new ConstantStringExpression( ip.context(),
						"http { format = \"json\" }" ) );
				}
			}
			serviceStatements.removeAll( toRemove );
		}
	}

	public static OLSyntaxNode getLocationForService( ParsingContext context, String serviceName ) {
		String location = "socket://" + serviceName.toLowerCase() + ":8080";
		return new ConstantStringExpression( context, location );
	}

	public static void generateServiceDirectories( String outputDirectory,
		Collection< Pair< String, ServiceInformation< Program > > > services )
		throws IOException {
		OutputStream os;
		Path outputPathDir = Paths.get( outputDirectory );
		Files.createDirectories( outputPathDir );
		for( Pair< String, ServiceInformation< Program > > service : services ) {
			Path serviceDir = outputPathDir.resolve( service.key() );
			JoliePrettyPrinter pp = new JoliePrettyPrinter();
			Files.createDirectories( serviceDir );
			Path jolieFilePath = serviceDir.resolve( service.key() + ".ol" );
			os = Files.newOutputStream( jolieFilePath, CREATE, TRUNCATE_EXISTING, WRITE );
			pp.visit( service.value().getNode() );
			os.write( pp.toString().getBytes() );
			os.close();
			os = Files.newOutputStream( serviceDir.resolve( "Dockerfile" ),
				CREATE, TRUNCATE_EXISTING, WRITE );
			String dfString = String.format(
				"FROM jolielang/jolie%n"
					+ "COPY %1$s .%n"
					+ "CMD [\"jolie\", \"%1$s\"]",
				jolieFilePath.getFileName() );
			os.write( dfString.getBytes() );
			os.close();
		}
		os = Files.newOutputStream( outputPathDir.resolve( "docker-compose.yml" ),
			CREATE, TRUNCATE_EXISTING, WRITE );
		os.write( createDockerCompose( services ).getBytes() );
		os.close();
	}

	public static String createDockerCompose( Collection< Pair< String, ServiceInformation< Program > > > services ) {
		Formatter fmt = new Formatter();
		String padding = "";
		fmt.format( "version: \"3.9\"%n" )
			.format( "services:%n" );
		for( Pair< String, ServiceInformation< Program > > service : services ) {
			fmt.format( "%2s%s:%n", padding, service.key().toLowerCase() );
			fmt.format( "%4s", padding )
				.format( "build: ./%s%n", service.key().toLowerCase() );
			List< String > dependencies = service.value().getDependsOn();
			if( !dependencies.isEmpty() ) {
				fmt.format( "%4s", padding )
					.format( "depends_on:%n" );
				for( String dependency : service.value().getDependsOn() ) {
					fmt.format( "%6s- \"%s\"", padding, dependency.toLowerCase() );
				}
			}
		}
		String result = fmt.out().toString();
		fmt.close();
		return result;
	}
}
