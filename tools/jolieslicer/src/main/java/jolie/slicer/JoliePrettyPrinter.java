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

import jolie.lang.Constants;
import jolie.lang.parse.UnitOLVisitor;
import jolie.lang.parse.ast.*;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.*;
import jolie.lang.parse.ast.types.*;
import jolie.util.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JoliePrettyPrinter implements UnitOLVisitor {
	PrettyPrinter pp = new PrettyPrinter();
	boolean isTopLevelTypeDeclaration = true;

	public String toString() {
		return pp.pp.toString();
	}

	@Override
	public void visit( Program n ) {
		intercalate( n.children(), child -> child.accept( this ), pp::newline );
	}

	@Override
	public void visit( OneWayOperationDeclaration decl ) {
		pp.append( decl.id() )
			.parens( () -> {
				// decl.requestType().accept( this );
				pp.append( decl.requestType().name() );
				return pp;
			} )
			.toPP();
	}

	@Override
	public void visit( RequestResponseOperationDeclaration decl ) {
		// TODO: Print faults
		pp.append( decl.id() )
			.parens( () -> {
				// decl.requestType().accept( this );
				pp.append( decl.requestType().name() );
				return pp;
			} ).toPP()
			.parens( () -> {
				// decl.requestType().accept( this );
				pp.append( decl.responseType().name() );
				return pp;
			} ).toPP();
	}

	@Override
	public void visit( DefinitionNode n ) {
		pp.append( n.id() )
			.space()
			.newCodeBlock( () -> {
				n.body().accept( this );
				return pp;
			} );
		// pp.append( "DEFINITION NODE" ).newline();
	}

	@Override
	public void visit( ParallelStatement n ) {
		pp.append( "PARALLEL STMT" ).newline();
		intercalate( n.children(),
			child -> pp.newCodeBlock( () -> {
				child.accept( this );
				return pp;
			} ),
			() -> {
				return pp.append( '|' ).newline();
			} );
	}

	@Override
	public void visit( SequenceStatement n ) {
		intercalate( n.children(), child -> child.accept( this ), pp::newline );
	}

	@Override
	public void visit( NDChoiceStatement n ) {
		// TODO Auto-generated method stub
		pp.append( "NDChoiceStatement" );
	}

	@Override
	public void visit( OneWayOperationStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( RequestResponseOperationStatement n ) {
		// TODO Pretty print n.process()
		pp.append( n.id() )
			.parens( () -> {
				n.inputVarPath().accept( this );
				return pp;
			} )
			.toPP()
			.parens( () -> {
				n.outputExpression().accept( this );
				return pp;
			} )
			.toPP()
			.newCodeBlock( () -> {
				n.process().accept( this );
				return pp;
			} );
	}

	@Override
	public void visit( NotificationOperationStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( SolicitResponseOperationStatement n ) {
		// TODO pretty print install function node at the end
		pp.append( n.id() )
			.append( '@' )
			.append( n.outputPortId() )
			.parens( () -> {
				n.outputExpression().accept( this );
				return pp;
			} )
			.toPP()
			.parens( () -> {
				Optional.ofNullable( n.inputVarPath() )
					.ifPresent( varPath -> varPath.accept( this ) );
				return pp;
			} )
			.toPP();
	}

	@Override
	public void visit( LinkInStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( LinkOutStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( AssignStatement n ) {
		n.variablePath().accept( this );
		pp.space().append( '=' ).space();
		n.expression().accept( this );
	}

	@Override
	public void visit( AddAssignStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( SubtractAssignStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( MultiplyAssignStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( DivideAssignStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( IfStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( DefinitionCallStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( WhileStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( OrConditionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( AndConditionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( NotExpressionNode n ) {
		// TODO Auto-generated method stub
		pp.append( '!' );
		n.expression().accept( this );
	}

	@Override
	public void visit( CompareConditionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ConstantIntegerExpression n ) {
		// TODO Auto-generated method stub
		pp.append( n.value() );
	}

	@Override
	public void visit( ConstantDoubleExpression n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ConstantBoolExpression n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ConstantLongExpression n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ConstantStringExpression n ) {
		// TODO Auto-generated method stub
		pp.append( '"' ).append( n.value() ).append( '"' );
	}

	@Override
	public void visit( ProductExpressionNode n ) {
		Iterator< Pair< Constants.OperandType, OLSyntaxNode > > it = n.operands().iterator();
		if( it.hasNext() ) {
			it.next().value().accept( this );
		}
		it.forEachRemaining( ( operand ) -> {
			pp.space();
			switch( operand.key() ) {
			case MULTIPLY:
				pp.append( '*' );
				break;
			case DIVIDE:
				pp.append( '/' );
				break;
			case MODULUS:
				pp.append( '%' );
				break;
			default:
			}
			pp.space();
			operand.value().accept( this );
		} );
	}

	@Override
	public void visit( SumExpressionNode n ) {
		Iterator< Pair< Constants.OperandType, OLSyntaxNode > > it = n.operands().iterator();
		if( it.hasNext() ) {
			it.next().value().accept( this );
		}
		it.forEachRemaining( ( operand ) -> {
			pp.space();
			switch( operand.key() ) {
			case ADD:
				pp.append( '+' );
				break;
			case SUBTRACT:
				pp.append( '-' );
				break;
			default:
			}
			pp.space();
			operand.value().accept( this );
		} );
	}

	@Override
	public void visit( VariableExpressionNode n ) {
		pp.append( n.toString() );
	}

	@Override
	public void visit( NullProcessStatement n ) {
		pp.append( "nullProcess" );
	}

	@Override
	public void visit( Scope n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( InstallStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( CompensateStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ThrowStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ExitStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ExecutionInfo n ) {
		pp.append( "execution" )
			.colon()
			.space()
			.append( n.mode().name().toLowerCase() );
	}

	@Override
	public void visit( CorrelationSetInfo n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( InputPortInfo n ) {
		// TODO: An input port has several other things to be printed
		pp.append( "inputPort" )
			.space()
			.append( n.id() )
			.space();
		ppInputPortInfo( n );
	}

	private void ppInputPortInfo( InputPortInfo n ) {
		pp.newCodeBlock( () -> {
			pp.append( "location" )
				.colon()
				.space()
				.append( '"' )
				.append( n.location().toString() )
				.append( '"' )
				.newline();
			if( n.protocol() != null ) {
				pp.append( "protocol" )
					.colon()
					.space()
					.append( n.protocol().toString() )
					.newline();
			}

			if( !n.getInterfaceList().isEmpty() ) {
				pp.append( "interfaces" )
					.colon()
					.nest( () -> {
						Iterator< InterfaceDefinition > it = n.getInterfaceList().iterator();
						if( n.getInterfaceList().size() > 1 ) {
							pp.newline();
						} else {
							pp.space();
						}

						while( it.hasNext() ) {
							pp.append( it.next().name() );
							if( it.hasNext() ) {
								pp.comma().newline();
							}
						}
						return pp;
					} )
					.run();
			}

			if( n.aggregationList().length > 0 ) {
				// TODO: pretty print aggregates
				pp.append( "aggregates" )
					.colon()
					.space()
					.append( "NOT IMPLEMENTED" );
			}

			if( !n.redirectionMap().isEmpty() ) {
				// TODO: pretty print redirects
				pp.append( "redirects" )
					.colon()
					.space()
					.append( "NOT IMPLEMENTED" );
			}
			return pp;
		} );
	}

	@Override
	public void visit( OutputPortInfo n ) {
		pp.append( "outputPort" )
			.space()
			.append( n.id() )
			.space();
		ppOutputPortInfo( n );
	}

	private void ppOutputPortInfo( OutputPortInfo n ) {
		pp.newCodeBlock( () -> {
			if( n.location() != null ) {
				pp.append( "location" )
					.colon()
					.space()
					.append( '"' )
					.append( n.location().toString() )
					.append( '"' )
					.newline();
			}
			if( n.protocol() != null ) {
				pp.append( "protocol" )
					.colon()
					.space()
					.append( n.protocol().toString() )
					.newline();
			}

			if( !n.getInterfaceList().isEmpty() ) {
				pp.append( "interfaces" )
					.colon()
					.nest( () -> {
						Iterator< InterfaceDefinition > it = n.getInterfaceList().iterator();
						if( n.getInterfaceList().size() > 1 ) {
							pp.newline();
						} else {
							pp.space();
						}

						intercalate( it,
							iface -> pp.append( iface.name() ),
							() -> {
								return pp.comma().newline();
							} );
						return pp;
					} )
					.run();
			}
			return pp;
		} );
	}

	@Override
	public void visit( PointerStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( DeepCopyStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( RunStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( UndefStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ValueVectorSizeExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( PreIncrementStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( PostIncrementStatement n ) {
		n.variablePath().accept( this );
		pp.append( "++" );
	}

	@Override
	public void visit( PreDecrementStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( PostDecrementStatement n ) {
		n.variablePath().accept( this );
		pp.append( "--" );
	}

	@Override
	public void visit( ForStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ForEachSubNodeStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ForEachArrayItemStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( SpawnStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( IsTypeExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( InstanceOfExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( TypeCastExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( SynchronizedStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( CurrentHandlerStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( EmbeddedServiceNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( InstallFixedVariableExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( VariablePathNode n ) {
		// TODO Auto-generated method stub
		pp.append( n.toPrettyString() );
	}

	@Override
	public void visit( TypeInlineDefinition n ) {
		if( isTopLevelTypeDeclaration ) {
			pp.append( "type" ).space();
		}
		pp.append( n.name() )
			.colon()
			.space();
		printBasicType( n.basicType() );

		if( n.subTypes() != null && !n.subTypes().isEmpty() ) {
			pp.space()
				.newCodeBlock( () -> {
					isTopLevelTypeDeclaration = false;
					ArrayList< Map.Entry< String, TypeDefinition > > subTypes = new ArrayList<>( n.subTypes() );
					subTypes.sort( Comparator.comparing( entry -> entry.getValue().context().line() ) );
					intercalate( subTypes,
						entry -> entry.getValue().accept( this ),
						pp::newline );
					isTopLevelTypeDeclaration = true;
					return pp;
				} );
		}
	}

	private void printBasicType( BasicTypeDefinition n ) {
		pp.append( n.nativeType().id() );
	}

	@Override
	public void visit( TypeDefinitionLink n ) {
		if( isTopLevelTypeDeclaration ) {
			pp.append( "type" ).space();
		}
		pp.append( n.name() )
			.colon()
			.space()
			.append( n.linkedTypeName() );
	}

	@Override
	public void visit( InterfaceDefinition n ) {
		pp.append( "interface" )
			.space()
			.append( n.name() )
			.space()
			.newCodeBlock( () -> {
				Stream< OperationDeclaration > s = n.operationsMap().values().stream();
				Map< Boolean, List< OperationDeclaration > > operations =
					s.collect( Collectors.partitioningBy( op -> op instanceof OneWayOperationDeclaration ) );
				if( !operations.get( true ).isEmpty() ) {
					pp.append( "OneWay" )
						.colon()
						.nest( () -> {
							pp.newline();
							intercalate( operations.get( true ),
								opDecl -> opDecl.accept( this ),
								() -> pp.comma().newline() );
							return pp;
						} ).toPP();
				}
				if( !operations.get( false ).isEmpty() ) {
					pp.append( "RequestResponse" )
						.colon()
						.nest( () -> {
							pp.newline();
							intercalate( operations.get( false ),
								opDecl -> opDecl.accept( this ),
								() -> pp.comma().newline() );
							return pp;
						} ).toPP();
				}
				return pp;
			} );
	}

	@Override
	public void visit( DocumentationComment n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( FreshValueExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( CourierDefinitionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( CourierChoiceStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( NotificationForwardStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( SolicitResponseForwardStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( InterfaceExtenderDefinition n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( InlineTreeExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( VoidExpressionNode n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( ProvideUntilStatement n ) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit( TypeChoiceDefinition n ) {
		// TODO Auto-generated method stub
		n.left().accept( this );
		pp.space().append( '|' ).space();
		n.right().accept( this );
	}

	@Override
	public void visit( ImportStatement n ) {
		// TODO Auto-generated method stub
		pp.append( "from" )
			.space();
		n.importTarget().forEach( pp::append );
		pp.space()
			.append( "import" )
			.space();
		if( n.isNamespaceImport() ) {
			pp.append( "*" );
		} else {
			intercalate( Arrays.asList( n.importSymbolTargets() ).iterator(),
				symbol -> pp.append( symbol.toString() ),
				() -> {
					return pp.comma().space();
				} );
		}
	}

	@Override
	public void visit( ServiceNode n ) {
		// TODO Auto-generated method stub

		// Remove OutputPorts generated by an `embed as` statement
		ArrayList< String > embedNames = new ArrayList<>();
		n.program().children().forEach( node -> {
			if( node instanceof EmbedServiceNode ) {
				EmbedServiceNode es = (EmbedServiceNode) node;
				if( es.isNewPort() && es.hasBindingPort() ) {
					embedNames.add( es.bindingPort().id() );
				}
			}
		} );

		// n.program().children().removeIf(
		// node -> (node instanceof OutputPortInfo)
		// && embedNames.contains( ((OutputPortInfo) node).id() ) );


		pp.append( "service" )
			.space()
			.append( n.name() )
			.space();
		n.parameterConfiguration().ifPresent( param -> {
			pp.parens( () -> {
				return pp
					.append( param.type().id() )
					.space()
					.colon()
					.space()
					.append( param.variablePath() );
			} ).run();
		} );

		pp.newCodeBlock( () -> {
			n.program().accept( this );
			return pp;
		} );
	}

	@Override
	public void visit( EmbedServiceNode n ) {
		pp.append( "embed" )
			.space()
			.append( n.serviceName() );
		Optional.ofNullable( n.passingParameter() ).ifPresent( param -> {
			pp.parens( () -> {
				param.accept( this );
				return pp;
			} );
		} );
		Optional.ofNullable( n.bindingPort() ).ifPresent( port -> {
			pp.space()
				.append( n.isNewPort() ? "as" : "in" )
				.space()
				.append( port.id() );
		} );
	}

	/*
	 * Functional interface to handle PrettyPrinter method references
	 */
	private interface PPRunnable {
		PrettyPrinter run();

		default PrettyPrinter toPP() {
			return run();
		};
	}

	private interface PPFunction extends Function< PPRunnable, PPRunnable > {
	};


	private < T > void intercalate( Iterator< T > it, Consumer< T > printer, PPRunnable interleave ) {
		while( it.hasNext() ) {
			printer.accept( it.next() );
			if( it.hasNext() ) {
				interleave.run();
			}
		}
	}

	private < T > void intercalate( Collection< T > collection, Consumer< T > printer, PPRunnable interleave ) {
		intercalate( collection.iterator(), printer, interleave );
	}


	private static class PrettyPrinter {
		StringBuilder pp = new StringBuilder( 1000 );
		int indentationLevel = 0;


		public PrettyPrinter append( String a ) {
			pp.append( a );
			return this;
		}


		public PrettyPrinter append( char a ) {
			pp.append( a );
			return this;
		}

		public PrettyPrinter append( int a ) {
			pp.append( a );
			return this;
		}

		public PrettyPrinter append( long a ) {
			pp.append( a );
			return this;
		}

		public PrettyPrinter append( float a ) {
			pp.append( a );
			return this;
		}

		public PrettyPrinter append( double a ) {
			pp.append( a );
			return this;
		}

		public PrettyPrinter newline() {
			pp.append( '\n' );
			for( int i = 0; i < indentationLevel; ++i ) {
				pp.append( '\t' );
			}
			return this;
		}

		public PrettyPrinter newBlock() {
			pp.append( '\n' );
			indentationLevel++;
			for( int i = 0; i < indentationLevel; ++i ) {
				pp.append( '\t' );
			}
			return this;
		}

		public PrettyPrinter endBlock() {
			pp.append( '\n' );
			indentationLevel--;
			for( int i = 0; i < indentationLevel; ++i ) {
				pp.append( '\t' );
			}
			return this;
		}

		public PPRunnable braces( PPRunnable prettyPrinter ) {
			return () -> {
				pp.append( '{' );
				prettyPrinter.run();
				pp.append( '}' );
				return this;
			};
		}

		public PPRunnable parens( PPRunnable prettyPrinter ) {
			return () -> {
				lparen().space();
				prettyPrinter.run();
				space().rparen();
				return this;
			};
		}

		public PPRunnable spaces( PPRunnable prettyPrinter ) {
			return () -> {
				space();
				prettyPrinter.run();
				return space();
			};
		}

		public PPRunnable nest( PPRunnable prettyPrinter ) {
			return () -> {
				indentationLevel++;
				prettyPrinter.run();
				indentationLevel--;
				// newline();
				return this;
			};
		}

		public PrettyPrinter newCodeBlock( PPRunnable prettyPrinter ) {
			lbrace();
			nest( () -> {
				newline();
				prettyPrinter.run();
				return this;
			} ).toPP();
			newline();
			return rbrace();
			// return ((PPFunction) this::braces).compose( this::nest )
			// .apply( () -> {
			// newline();
			// prettyPrinter.run();
			// newline().append( "Code goes here.." );
			// return this;
			// } )
			// .run()
			// .newline();

			// .apply(() -> {
			// newline();
			// prettyPrinter.run();
			// } ).run();

			// return this;
			// braces( () -> {
			// nest();
			// newline();
			// prettyPrinter.run();
			// return this;
			// } );
		}

		public PrettyPrinter space() {
			pp.append( ' ' );
			return this;
		}

		public PrettyPrinter spaces( int n ) {
			for( int i = 0; i < n; ++i ) {
				space();
			}
			return this;
		}


		public PrettyPrinter colon() {
			pp.append( ':' );
			return this;
		}

		public PrettyPrinter comma() {
			pp.append( ',' );
			return this;
		}

		public PrettyPrinter lparen() {
			pp.append( '(' );
			return this;
		}

		public PrettyPrinter rparen() {
			pp.append( ')' );
			return this;
		}

		public PrettyPrinter lbrack() {
			pp.append( '[' );
			return this;
		}

		public PrettyPrinter rbrack() {
			pp.append( ']' );
			return this;
		}

		public PrettyPrinter lbrace() {
			pp.append( '{' );
			return this;
		}

		public PrettyPrinter rbrace() {
			pp.append( '}' );
			return this;
		}
	}
}
