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
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Pair;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JoliePrettyPrinter implements UnitOLVisitor {
	final PrettyPrinter pp = new PrettyPrinter();
	boolean isTopLevelTypeDeclaration = false;

	public String toString() {
		return pp.pp.toString();
	}

	@Override
	public void visit( Program n ) {
		pp.intercalate( n.children(),
			(child, pp) -> child.accept( this ),
			PrettyPrinter::newline );
	}

	@Override
	public void visit( OneWayOperationDeclaration decl ) {
		pp.append( decl.id() )
			.parens( pp -> pp.append( decl.requestType().name() ) );
	}

	@Override
	public void visit( RequestResponseOperationDeclaration decl ) {
		// TODO: Print faults
		pp.append( decl.id() )
			.parens( pp -> pp
				.append( decl.requestType().name() ) )
			.parens( pp -> pp
				.append( decl.responseType().name() ) );
	}

	@Override
	public void visit( DefinitionNode n ) {
		pp.append( n.id() )
			.space()
			.newCodeBlock( pp -> {
				n.body().accept( this );
				return pp;
			});
	}

	@Override
	public void visit( ParallelStatement n ) {
		pp.append( "PARALLEL STMT:" )
			.newline()
			.intercalate( n.children(),
				(child, _0) -> _0.newCodeBlock( pp -> {
						child.accept( this );
						return pp;
					}),
				pp -> pp.append( '|' ).newline() );
	}

	@Override
	public void visit( NDChoiceStatement n ) {
		// TODO Auto-generated method stub
		pp.append( "NDChoiceStatement" );
	}

	@Override
	public void visit( SequenceStatement n ) {
		pp.intercalate( n.children(),
			(child, _0) -> child.accept( this ),
			PrettyPrinter::newline );
	}

	@Override
	public void visit( OneWayOperationStatement n ) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit( RequestResponseOperationStatement n ) {
		// TODO Pretty print n.process()
		pp.append( n.id() )
			.parens( pp -> {
				n.inputVarPath().accept( this );
				return pp;
			} )
			.parens( pp -> {
				n.outputExpression().accept( this );
				return pp;
			} )
			.newCodeBlock( pp -> {
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
			.parens( pp -> {
				n.outputExpression().accept( this );
				return pp;
			} )
			.parens( pp -> pp
				.ifPresent( Optional.ofNullable( n.inputVarPath() ),
					(varPath, _0) -> varPath.accept( this ) ) );
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
		pp.append( "inputPort" )
			.space()
			.append( n.id() )
			.space()
			.newCodeBlock( _0 -> _0
				.append( "location" )
				.colon()
				.space()
				.append( '"' )
				.append( n.location().toString() )
				.append( '"' )
				.newline()
				.onlyIf( n.protocol() != null, _1 -> _1
					.append( "protocol" )
					.colon()
					.space()
					.append( n.protocol().toString() )
					.newline() )
				.onlyIf( !n.getInterfaceList().isEmpty(), _1 -> _1
					.append( "interfaces" )
					.colon()
					.nest( _2 -> _2
						.ifTrueOrElse( n.getInterfaceList().size() > 1,
							PrettyPrinter::newline,
							PrettyPrinter::space)
						.intercalate( n.getInterfaceList(),
							(id, pp) -> pp.append( id.name() ),
							pp -> pp.comma().newline() )
					))
				.onlyIf( n.aggregationList().length > 0, _1 -> _1
					// TODO: pretty print aggregates
					.append( "aggregates" )
					.colon()
					.space()
					.append( "NOT IMPLEMENTED" ) )
				.onlyIf( !n.redirectionMap().isEmpty(), _1 -> _1
					// TODO: pretty print redirects
					.append( "redirects" )
					.colon()
					.space()
					.append( "NOT IMPLEMENTED" ) )
			);
	}

	@Override
	public void visit( OutputPortInfo n ) {
		pp.append( "outputPort" )
			.space()
			.append( n.id() )
			.space()
			.newCodeBlock( _0 -> _0
				.onlyIf( n.location() != null, _1 -> _1
					.append( "location" )
					.colon()
					.space()
					.append( '"' )
					.append( n.location().toString() )
					.append( '"' )
					.newline() )
				.onlyIf( n.protocol() != null, _1 ->_1
					.append( "protocol" )
					.colon()
					.space()
					.append( n.protocol().toString() )
					.newline() )
				.onlyIf( !n.getInterfaceList().isEmpty(), _1 -> _1
					.append( "interfaces" )
					.colon()
					.nest( _2 -> _2
						.ifTrueOrElse( n.getInterfaceList().size() > 1,
							PrettyPrinter::newline,
							PrettyPrinter::space)
						.intercalate( n.getInterfaceList(),
							(id, pp) -> pp.append( id.name() ),
							pp -> pp.comma().newline() )
					))
			);
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
		pp.append( n.toPrettyString() );
	}

	@Override
	public void visit( TypeInlineDefinition n ) {
		pp.onlyIf( isTopLevelTypeDeclaration, pp -> pp.append( "type" ).space() )
			.append( n.name() )
			.colon()
			.space()
			.append( n.basicType().nativeType().id() )
			.onlyIf( n.subTypes() != null && !n.subTypes().isEmpty(), _0 -> _0
				.space()
				.newCodeBlock( pp -> {
					boolean previousValue = isTopLevelTypeDeclaration;
					isTopLevelTypeDeclaration = false;
					List< Map.Entry< String, TypeDefinition > > subTypes = new ArrayList<>( n.subTypes() );
					subTypes.sort( Comparator.comparing( entry -> entry.getValue().context().line() ) );
					pp.intercalate( subTypes,
						(entry, _1) -> entry.getValue().accept( this ),
						PrettyPrinter::newline );
					isTopLevelTypeDeclaration = previousValue;
					return pp;
				} ) );
	}

	@Override
	public void visit( TypeDefinitionLink n ) {
		pp.onlyIf( isTopLevelTypeDeclaration, pp -> pp.append( "type" ).space() )
			.append( n.name() )
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
			.newCodeBlock( pp -> {
				Stream<OperationDeclaration> s = n.operationsMap().values().stream();
				Map<Boolean, List<OperationDeclaration>> operations =
					s.collect( Collectors.partitioningBy( op -> op instanceof OneWayOperationDeclaration ) );
				return pp
					.onlyIf( !operations.get( true ).isEmpty(), _0 -> _0
						.append( "OneWay" )
						.colon()
						.nest( _1 -> _1
							.newline()
							.intercalate( operations.get( true ),
								( opDecl, _2 ) -> opDecl.accept( this ),
								_2 -> _2.comma().newline() ) ) )
					.onlyIf( !operations.get( false ).isEmpty(), _0 -> _0
						.append( "RequestResponse" )
						.colon()
						.nest( _1 -> _1
							.newline()
							.intercalate( operations.get( false ),
								( opDecl, _2 ) -> opDecl.accept( this ),
								_2 -> _2.comma().newline() ) ) );
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
		n.left().accept( this );
		pp.space().append( '|' ).space();
		n.right().accept( this );
	}

	@Override
	public void visit( ImportStatement n ) {
		pp.append( n.toString() );
	}

	@Override
	public void visit( ServiceNode n ) {
		pp.append( "service" )
			.space()
			.append( n.name() )
			.space()
			.ifPresent( n.parameterConfiguration(), (param, _0) -> _0
				.parens( _1 -> _1
					.append( param.type().id() )
					.space()
					.colon()
					.space()
					.append( param.variablePath() ) ) )
			.newCodeBlock( pp -> {
				n.program().accept( this );
				return pp;
			} );
	}

	@Override
	public void visit( EmbedServiceNode n ) {
		pp.append( "embed" )
			.space()
			.append( n.serviceName() )
			.onlyIf( n.passingParameter() != null, _0 -> _0
				.parens( pp -> {
					n.passingParameter().accept( this );
					return pp;
				} ))
			.onlyIf( n.bindingPort() != null, _0 -> _0
				.space()
				.append( n.isNewPort() ? "as" : "in" )
				.space()
				.append( n.bindingPort().id() ) );
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
			pp.append( System.lineSeparator() );
			for( int i = 0; i < indentationLevel; ++i ) {
				pp.append( '\t' );
			}
			return this;
		}

		private PrettyPrinter run( Supplier<PrettyPrinter> prettyPrinter ) {
			return prettyPrinter.get();
		}

		private PrettyPrinter run( Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return prettyPrinter.apply( this );
		}

		public PrettyPrinter surround( String ldelimiter, String rdelimiter, Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return append( ldelimiter )
				.run( prettyPrinter )
				.append( rdelimiter );
		}

		public PrettyPrinter surround( Function<PrettyPrinter, PrettyPrinter> ldelimiter, Function<PrettyPrinter, PrettyPrinter> rdelimiter, Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return run( ldelimiter.andThen( prettyPrinter ).andThen( rdelimiter ) );
		}

		public PrettyPrinter surround( String delimiter, Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return surround( delimiter, delimiter, prettyPrinter );
		}

		public PrettyPrinter surround( Function<PrettyPrinter,PrettyPrinter> delimiter, Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return surround( delimiter, delimiter, prettyPrinter );
		}

		public PrettyPrinter braces( Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return surround( PrettyPrinter::lbrace, PrettyPrinter::rbrace, prettyPrinter);
		}

		public PrettyPrinter parens( Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return surround( PrettyPrinter::lparen, PrettyPrinter::rparen, prettyPrinter);
		}

		public PrettyPrinter spaces( Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return surround( PrettyPrinter::space, prettyPrinter );
		}

		public PrettyPrinter nest( Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			indentationLevel++;
			run( prettyPrinter );
			indentationLevel--;
			return this;
		}

		public PrettyPrinter newCodeBlock( Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			/* return lbrace()
				.nest( () -> newline().run( prettyPrinter ) )
				.newline()
				.rbrace(); */
			return braces( _1 ->
				nest( _2 -> newline().run( prettyPrinter ) ).newline()
			);
		}

		public < T > PrettyPrinter intercalate( Iterator< T > it,
			BiConsumer< T, PrettyPrinter > printer,
			Function<PrettyPrinter, PrettyPrinter> interleave ) {
			while( it.hasNext() ) {
				printer.accept( it.next(), this );
				if( it.hasNext() ) {
					run( interleave );
				}
			}
			return this;
		}

		public < T > PrettyPrinter intercalate( Collection< T > collection,
			BiConsumer< T, PrettyPrinter > printer,
			Function<PrettyPrinter, PrettyPrinter> interleave ) {
			return intercalate( collection.iterator(), printer, interleave );
		}

		public <T> PrettyPrinter ifPresent( Optional<T> optional, BiConsumer<T,PrettyPrinter> prettyPrinter ) {
			optional.ifPresent( t -> prettyPrinter.accept( t, this ) );
			return this;
		}


		public PrettyPrinter ifTrueOrElse( boolean condition,
			Function<PrettyPrinter, PrettyPrinter> trueCase,
			Function<PrettyPrinter, PrettyPrinter> falseCase ) {
			return condition ? trueCase.apply( this ) : falseCase.apply( this );
		}

		public PrettyPrinter onlyIf( boolean condition, Function<PrettyPrinter, PrettyPrinter> prettyPrinter ) {
			return ifTrueOrElse( condition, prettyPrinter, p -> p );
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
