package jolie.slicer;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.*;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Unit;

import java.util.*;

public class DependenciesResolver implements OLVisitor< Unit, ArrayList< OLSyntaxNode > > {
	final Map< OLSyntaxNode, ArrayList< OLSyntaxNode > > declDependencies = new HashMap<>();
	final Map< String, ImportStatement > importedSymbolsMap = new HashMap<>();

	DependenciesResolver( Program p ) {
		collectDeclarationsAndImportedSymbols( p );
		/* Compute dependencies */
		p.accept( this );
	}

	ArrayList< OLSyntaxNode > getServiceDependencies( ServiceNode n ) {
		assert declDependencies.containsKey( n );
		assert declDependencies.get( n ) != null;
		return declDependencies.get( n );
	}

	private void collectDeclarationsAndImportedSymbols( Program program ) {
		for( OLSyntaxNode n : program.children() ) {
			if( n instanceof ImportStatement ) {
				ImportStatement is = (ImportStatement) n;
				ImportSymbolTarget[] importedSymbols = is.importSymbolTargets();
				for( ImportSymbolTarget ist : importedSymbols ) {
					importedSymbolsMap.put( ist.localSymbolName(), is );
				}
			} else {
				declDependencies.put( n, null );
			}
		}
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( Program n, Unit ctx ) {
		ArrayList< OLSyntaxNode > dependencies = new ArrayList<>();
		n.children().forEach( c -> dependencies.addAll( c.accept( this ) ) );
		return dependencies;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( OneWayOperationDeclaration ow, Unit ctx ) {
		ArrayList< OLSyntaxNode > result = new ArrayList<>();
		/*
		 * If a type is a top level program declaration, we add it to the dependencies of the Operation
		 * Declaration, otherwise it is an imported symbol and the visitor will add it's import statement as
		 * dependency.
		 */
		if( declDependencies.containsKey( ow.requestType() ) ) {
			result.add( ow.requestType() );
		}
		result.addAll( ow.requestType().accept( this ) );
		return result;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( RequestResponseOperationDeclaration rr, Unit ctx ) {
		ArrayList< OLSyntaxNode > result = new ArrayList<>();
		/*
		 * If a type is a top level program declaration, we add it to the dependencies of the
		 * OperationDeclaration, otherwise it is an imported symbol and the visitor will add it's import
		 * statement as dependency.
		 */
		if( declDependencies.containsKey( rr.requestType() ) ) {
			result.add( rr.requestType() );
		}
		if( declDependencies.containsKey( rr.responseType() ) ) {
			result.add( rr.requestType() );
		}
		result.addAll( rr.requestType().accept( this ) );
		result.addAll( rr.responseType().accept( this ) );
		return result;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( DefinitionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ParallelStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SequenceStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( NDChoiceStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( OneWayOperationStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( RequestResponseOperationStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( NotificationOperationStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SolicitResponseOperationStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( LinkInStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( LinkOutStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( AssignStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( AddAssignStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SubtractAssignStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( MultiplyAssignStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( DivideAssignStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( IfStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( DefinitionCallStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( WhileStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( OrConditionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( AndConditionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( NotExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( CompareConditionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ConstantIntegerExpression n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ConstantDoubleExpression n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ConstantBoolExpression n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ConstantLongExpression n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ConstantStringExpression n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ProductExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SumExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( VariableExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( NullProcessStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( Scope n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InstallStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( CompensateStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ThrowStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ExitStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ExecutionInfo n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( CorrelationSetInfo n, Unit ctx ) {
		return new ArrayList<>();
	}

	public ArrayList< OLSyntaxNode > visit( PortInfo n, Unit ctx ) {
		ArrayList< OLSyntaxNode > result = new ArrayList<>();
		n.getInterfaceList().forEach(
			iFace -> result.addAll( iFace.accept( this ) ) );
		return result;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InputPortInfo n, Unit ctx ) {
		return visit( (PortInfo) n, ctx );
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( OutputPortInfo n, Unit ctx ) {
		return visit( (PortInfo) n, ctx );
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( PointerStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( DeepCopyStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( RunStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( UndefStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ValueVectorSizeExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( PreIncrementStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( PostIncrementStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( PreDecrementStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( PostDecrementStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ForStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ForEachSubNodeStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ForEachArrayItemStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SpawnStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( IsTypeExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InstanceOfExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( TypeCastExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SynchronizedStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( CurrentHandlerStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( EmbeddedServiceNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InstallFixedVariableExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( VariablePathNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( TypeInlineDefinition tid, Unit ctx ) {
		if( declDependencies.get( tid ) != null ) {
			return declDependencies.get( tid );
		}
		ArrayList< OLSyntaxNode > newDependencies = new ArrayList<>();
		assert declDependencies.containsKey( tid );

		if( tid.subTypes() != null ) {
			tid.subTypes()
				.stream()
				.map( e -> e.getValue().accept( this ) )
				.forEach( newDependencies::addAll );
		}

		declDependencies.put( tid, newDependencies );
		return newDependencies;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( TypeDefinitionLink tdl, Unit ctx ) {
		if( declDependencies.get( tdl ) != null ) {
			return declDependencies.get( tdl );
		}
		ArrayList< OLSyntaxNode > newDependencies = new ArrayList<>();
		if( declDependencies.containsKey( tdl.linkedType() ) ) {
			newDependencies.add( tdl.linkedType() );
			newDependencies.addAll( tdl.linkedType().accept( this ) );
		} else if( importedSymbolsMap.containsKey( tdl.linkedTypeName() ) ) {
			newDependencies.add( importedSymbolsMap.get( tdl.linkedTypeName() ) );
		} else {
			assert false;
		}
		declDependencies.put( tdl, newDependencies );
		return newDependencies;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( TypeChoiceDefinition tcd, Unit ctx ) {
		if( declDependencies.get( tcd ) != null ) {
			return declDependencies.get( tcd );
		}
		assert declDependencies.containsKey( tcd );
		ArrayList< OLSyntaxNode > newDependencies = new ArrayList<>();
		newDependencies.addAll( tcd.left().accept( this ) );
		newDependencies.addAll( tcd.right().accept( this ) );
		declDependencies.put( tcd, newDependencies );
		return newDependencies;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InterfaceDefinition n, Unit ctx ) {
		/*
		 * We have to distinguish between interface definitions of a port declaration and actual interface
		 * definitions at the top level of the program.
		 */
		if( declDependencies.get( n ) != null ) {
			return declDependencies.get( n );
		}
		ArrayList< OLSyntaxNode > newDependencies = new ArrayList<>();
		if( declDependencies.containsKey( n ) ) { // The declaration is an actual interface declaration
			n.operationsMap().entrySet()
				.stream()
				.map( e -> e.getValue().accept( this ) )
				.forEach( newDependencies::addAll );
			declDependencies.put( n, newDependencies );
		} else { // The interface definition is an interface appearing in a Port declaration.
			if( importedSymbolsMap.containsKey( n.name() ) ) { // The interface is an imported symbol
				newDependencies.add( importedSymbolsMap.get( n.name() ) );
			} else { // The interface is not imported, find the actual definition in this program
				InterfaceDefinition actualDefinition = null;
				for( OLSyntaxNode decl : declDependencies.keySet() ) {
					if( decl instanceof InterfaceDefinition
						&& ((InterfaceDefinition) decl).name().equals( n.name() ) ) {
						actualDefinition = (InterfaceDefinition) decl;
					}
				}
				assert actualDefinition != null;
				// The actual definition is a dependency of the Port declaration
				newDependencies.add( actualDefinition );
				newDependencies.addAll( actualDefinition.accept( this ) );
			}
		}
		return newDependencies;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( DocumentationComment n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( FreshValueExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( CourierDefinitionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( CourierChoiceStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( NotificationForwardStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( SolicitResponseForwardStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InterfaceExtenderDefinition n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( InlineTreeExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( VoidExpressionNode n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ProvideUntilStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ImportStatement n, Unit ctx ) {
		return new ArrayList<>();
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( ServiceNode n, Unit ctx ) {
		if( declDependencies.get( n ) != null ) {
			return declDependencies.get( n );
		}
		ArrayList< OLSyntaxNode > newDependencies = new ArrayList<>( n.program().accept( this ) );
		declDependencies.put( n, newDependencies );
		return newDependencies;
	}

	@Override
	public ArrayList< OLSyntaxNode > visit( EmbedServiceNode n, Unit ctx ) {
		if( importedSymbolsMap.containsKey( n.serviceName() ) ) {
			return new ArrayList<>( Arrays.asList( importedSymbolsMap.get( n.serviceName() ) ) );
		} else {
			// The service name is not imported. It refers to a ServiceNode declared in this program
			assert declDependencies.containsKey( n.service() );
			return n.service().accept( this );
		}
	}
}
