/*******************************************************************************
 *   Copyright (C) 2018 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com> *
 *                                                                             *
 *   This program is free software; you can redistribute it and/or modify      *
 *   it under the terms of the GNU Library General Public License as           *
 *   published by the Free Software Foundation; either version 2 of the        *
 *   License, or (at your option) any later version.                           *
 *                                                                             *
 *   This program is distributed in the hope that it will be useful,           *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             *
 *   GNU General Public License for more details.                              *
 *                                                                             *
 *   You should have received a copy of the GNU Library General Public         *
 *   License along with this program; if not, write to the                     *
 *   Free Software Foundation, Inc.,                                           *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.                 *
 *                                                                             *
 *   For details about the authors of this software, see the AUTHORS file.     *
 *******************************************************************************/
package jolie.doc;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OperationDeclaration;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.ast.PortInfo;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.util.ProgramInspector;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;

public class JolieToValue
{
	private static class ProgramInfoType {
		private static final String PORT = "port";
		private static final String SUBTYPE = "subtype";
	}

	private static class PortInfoType {
		private static final String NAME = "name";
		private static final String LOCATION = "location";
		private static final String PROTOCOL = "protocol";
		private static final String INTERFACE = "interface";
		private static final String IS_OUTPUT = "isOutput";
		private static final String DOCUMENTATION = "documentation";
	}
	
	private static class InterfaceInfoType {
		private static final String NAME = "name";
		private static final String OPERATION = "operation";
		private static final String DOCUMENTATION = "documentation";
	}
	
	private static class OperationInfoType {
		private static final String NAME = "name";
		private static final String REQUEST_TYPE = "requestType";
		private static final String RESPONSE_TYPE = "responseType";
		private static final String FAULT = "fault";
		private static final String DOCUMENTATION = "documentation";
	}
	
	private static class TypeInfoType {
		private static final String NAME = "name";
		private static final String CODE = "code";
		private static final String DOCUMENTATION = "documentation";
	}
	
	private static class FaultInfoType {
		private static final String NAME = "name";
		private static final String TYPE = "type";
	}
	
	private static final String TYPE_DECLARATION_TOKEN = "type";
	private static final String TYPE_DEFINITION_TOKEN = ":";
	private static final String TYPE_CHOICE_TOKEN = "|";
	private static final String TYPE_SUBTYPE_OPEN = "{";
	private static final String TYPE_SUBTYPE_CLOSE = "}";
	private static final String TYPE_SUBTYPE_DEFINITON = ".";


	public static Value buildProgramInfo( ProgramInspector inspector ){
		
		Value returnValue = Value.create();
		
		ValueVector ports = ValueVector.create();
		ValueVector subtypes = ValueVector.create();
		Set<String> subtypesSet = new HashSet<>();
		returnValue.children().put ( ProgramInfoType.PORT, ports );
		returnValue.children().put( ProgramInfoType.SUBTYPE, subtypes );
		
		for ( InputPortInfo	portInfo : inspector.getInputPorts() ) {
			ports.add ( buildPortInfo( portInfo ) );
			buildSubTypes( portInfo, subtypes, subtypesSet );
		}
		
		for ( OutputPortInfo portInfo : inspector.getOutputPorts() ){
			ports.add ( buildPortInfo( portInfo ) );
			buildSubTypes( portInfo, subtypes, subtypesSet );
		}
		
		return returnValue;
	}
	
	private static Value buildPortInfo( InputPortInfo portInfo ){
		Value returnValue = Value.create();
		returnValue.setFirstChild( PortInfoType.NAME, portInfo.id() );
		returnValue.setFirstChild( PortInfoType.IS_OUTPUT, false );
		if ( portInfo.location() != null ){
			returnValue.setFirstChild( PortInfoType.LOCATION, portInfo.location() );
		}
		if( portInfo.protocolId() != null ){
			returnValue.setFirstChild( PortInfoType.PROTOCOL, portInfo.protocolId() );
		}
		if( portInfo.getDocumentation() != null ){
			returnValue.setFirstChild( PortInfoType.DOCUMENTATION, portInfo.getDocumentation() );
		}
		ValueVector interfaces = ValueVector.create();
		returnValue.children().put( PortInfoType.INTERFACE, interfaces );
		portInfo.getInterfaceList().forEach( ( i ) -> {
			interfaces.add( buildInterfaceInfo( i ) );
		} );
		return returnValue;
	}
	
	private static Value buildPortInfo( OutputPortInfo portInfo ){
		Value returnValue = Value.create();
				returnValue.setFirstChild( PortInfoType.NAME, portInfo.id() );
		returnValue.setFirstChild( PortInfoType.IS_OUTPUT, false );
		if ( portInfo.location() != null ){
			returnValue.setFirstChild( PortInfoType.LOCATION, portInfo.location() );
		}
		if( portInfo.protocolId() != null ){
			returnValue.setFirstChild( PortInfoType.PROTOCOL, portInfo.protocolId() );
		}
		if( portInfo.getDocumentation() != null ){
			returnValue.setFirstChild( PortInfoType.DOCUMENTATION, portInfo.getDocumentation() );
		}
		ValueVector interfaces = ValueVector.create();
		returnValue.children().put( PortInfoType.INTERFACE, interfaces );
		portInfo.getInterfaceList().forEach( ( i ) -> {
			interfaces.add( buildInterfaceInfo( i ) );
		} );
		return returnValue;
	}
	
	private static Value buildInterfaceInfo( InterfaceDefinition interfaceDefinition ){
		Value returnValue = Value.create();
		returnValue.setFirstChild( InterfaceInfoType.NAME, interfaceDefinition.name() );
		if( interfaceDefinition.getDocumentation() != null ){
			returnValue.setFirstChild( InterfaceInfoType.DOCUMENTATION, interfaceDefinition.getDocumentation() );
		}
		ValueVector operations = ValueVector.create();
		returnValue.children().put( InterfaceInfoType.OPERATION, operations );
		interfaceDefinition.operationsMap().entrySet().forEach( ( o ) -> {
			operations.add( buildOperationInfo( o.getValue() ) );
		} );
		return returnValue;
	}
	
	private static Value buildOperationInfo( OperationDeclaration operationDeclaration ){
		Value returnValue = Value.create();
		returnValue.setFirstChild( OperationInfoType.NAME, operationDeclaration.id() );
		if( operationDeclaration.getDocumentation() != null ){
			returnValue.setFirstChild( OperationInfoType.DOCUMENTATION, operationDeclaration.getDocumentation() );
		}
		if ( operationDeclaration instanceof RequestResponseOperationDeclaration ){
			RequestResponseOperationDeclaration rrod = (RequestResponseOperationDeclaration) operationDeclaration;
			returnValue.getChildren( OperationInfoType.REQUEST_TYPE ).add( buildTypeInfo( rrod.requestType() ) );
			returnValue.getChildren( OperationInfoType.RESPONSE_TYPE ).add( buildTypeInfo( rrod.responseType() ) );
			if ( rrod.faults().size() > 0 ){
				ValueVector faults = ValueVector.create();
				returnValue.children().put( OperationInfoType.FAULT, faults );
				rrod.faults().entrySet().forEach( ( fault ) -> {
					faults.add( buildFaultInfo( fault ) );
				} );
			}
		} else {
			OneWayOperationDeclaration owd = ( OneWayOperationDeclaration ) operationDeclaration;
			returnValue.getChildren( OperationInfoType.REQUEST_TYPE ).add( buildTypeInfo( owd.requestType() ) );
		}
		return returnValue;
	}
	
	private static Value buildFaultInfo(  Entry<String,TypeDefinition> fault ){
		Value returnValue = Value.create();
		returnValue.setFirstChild( FaultInfoType.NAME, fault.getKey() );
		returnValue.getChildren( FaultInfoType.TYPE ).add( buildTypeInfo( fault.getValue() ) );
		return returnValue;
	}
	
	private static Value buildTypeInfo( TypeDefinition typeDefinition ){
		Value returnValue = Value.create();
		returnValue.setFirstChild( TypeInfoType.NAME, typeDefinition.id() );
		if( typeDefinition.getDocumentation() != null ){
			returnValue.setFirstChild( TypeInfoType.DOCUMENTATION, typeDefinition.getDocumentation() );
		}
		returnValue.setFirstChild( TypeInfoType.CODE, buildTypeCode( typeDefinition ) );
		return returnValue;
	}
	
	private static String buildTypeCode( TypeDefinition typeDefinition ){
		String returnString = "";
		if( typeDefinition instanceof TypeChoiceDefinition
			||
			typeDefinition instanceof TypeDefinitionLink
			||
			( typeDefinition instanceof TypeInlineDefinition && ((TypeInlineDefinition) typeDefinition).hasSubTypes() ) ){
			returnString = TYPE_DECLARATION_TOKEN + " " + typeDefinition.id() + TYPE_DEFINITION_TOKEN + " " + buildSubTypeCode( typeDefinition );
		} else {
			returnString = typeDefinition.id();
		}
		return returnString;
	}
	
	private static String buildSubTypeCode( TypeDefinition typeDefinition ){
		String returnString = "";
		if( typeDefinition instanceof TypeChoiceDefinition ){
			returnString += buildTypeCode( ((TypeChoiceDefinition) typeDefinition).left() );
			returnString += TYPE_CHOICE_TOKEN;
			returnString += buildTypeCode( ((TypeChoiceDefinition) typeDefinition).right() );
		}
		if( typeDefinition instanceof TypeDefinitionLink ){
			returnString += ( ( TypeDefinitionLink ) typeDefinition ).linkedTypeName();
		}
		if ( typeDefinition instanceof TypeInlineDefinition ){
			TypeInlineDefinition tid = ( TypeInlineDefinition ) typeDefinition;
			returnString += tid.nativeType().id();
			if ( tid.hasSubTypes() ){
				returnString += " " + TYPE_SUBTYPE_OPEN;
				for ( Entry<String, TypeDefinition> subType : tid.subTypes() ) {
					returnString += " " + TYPE_SUBTYPE_DEFINITON + subType.getKey() + TYPE_DEFINITION_TOKEN + " " + buildSubTypeCode( subType.getValue() );
				}
				returnString += " " + TYPE_SUBTYPE_CLOSE;
			}
		}
		return returnString;
	}
	
	private static void buildSubTypes( PortInfo p, ValueVector v, Set<String> s ){
		p.getInterfaceList().forEach( ( i ) -> { buildSubTypes( i, v, s ); 	} );
	}
	
	private static void buildSubTypes( InterfaceDefinition i, ValueVector v, Set<String> s ){
		i.operationsMap().entrySet().forEach( ( Entry<String,OperationDeclaration> d ) -> { 
			buildSubTypes( d.getValue(), v, s );
		} );
	}
	
	private static void buildSubTypes( OperationDeclaration o, ValueVector v, Set<String> s ){
		if ( o instanceof OneWayOperationDeclaration ){
			buildSubTypes( ((OneWayOperationDeclaration) o ).requestType(), v, s);
		} else {
			buildSubTypes( ((RequestResponseOperationDeclaration) o ).requestType(), v, s );
			buildSubTypes( ((RequestResponseOperationDeclaration) o ).responseType(), v, s );
			((RequestResponseOperationDeclaration) o ).faults().entrySet().forEach( ( f ) -> {
				buildSubTypes( f.getValue(), v, s );
			} );
		}
	}
	
	private static void buildSubTypes( TypeDefinition d, ValueVector v, Set<String> s ){
		if( d instanceof TypeChoiceDefinition ){
			buildSubTypes( ((TypeChoiceDefinition) d).left(), v, s );
			buildSubTypes( ((TypeChoiceDefinition) d).right(), v, s );
		} else if ( d instanceof TypeDefinitionLink ){
			TypeDefinitionLink tdl = (TypeDefinitionLink) d;
			if ( !s.contains( tdl.id() ) ){
				s.add( tdl.id() );
				Value tv = Value.create();
				tv.setFirstChild( TypeInfoType.NAME, tdl.id() );
				tv.setFirstChild( TypeInfoType.CODE, buildTypeCode( tdl ) );
				if ( tdl.getDocumentation() != null ){
					tv.setFirstChild( TypeInfoType.DOCUMENTATION, tdl.getDocumentation() );
				}
				v.add( tv );
			}
		} else if ( d instanceof TypeInlineDefinition ){
			TypeInlineDefinition tid = (TypeInlineDefinition) d;
			if ( tid.hasSubTypes() ){
				tid.subTypes().forEach( ( td ) -> {
					buildSubTypes( td.getValue() , v, s );
				} );
			}
		}
	}
	
	
}
