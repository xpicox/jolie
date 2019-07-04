/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joliex.java.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import jolie.CommandLineException;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.SemanticException;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.util.ParsingUtils;
import jolie.lang.parse.util.ProgramInspector;
import jolie.runtime.ByteArray;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import jolie.runtime.embedding.Jolie2JavaInterface;
import joliex.java.Jolie2Java;
import joliex.java.Jolie2JavaCommandLineParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author claudio
 */
public class JavaDocumentCreatorTest
{
	private static ProgramInspector inspector;
	private static final String TESTSTRING = "test";
	private static final Integer TESTINTEGER = 1;
	private static final Double TESTDOUBLE = 1.1;
	private static final byte[] TESTRAW = new byte[]{ (byte) 0xe0, 0x4f, (byte) 0xd0, 0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2 };
	private static final Boolean TESTBOOL = true;
	private static final Long TESTLONG = 2L;

	public JavaDocumentCreatorTest()
	{
	}

	@BeforeClass
	public static void setUpClass() throws IOException, ParserException, SemanticException, CommandLineException
	{
		// clean past generated files if they exist
		File generatedPath = new File( "./generated/com/test" );
		if ( generatedPath.exists() ) {
			String files[] = generatedPath.list();
			for( String temp : files ) {
				File fileDelete = new File( generatedPath, temp );
				fileDelete.delete();
			}
		}

		String[] args = { "./resources/main.ol" };
		Jolie2JavaCommandLineParser cmdParser = Jolie2JavaCommandLineParser.create( args, Jolie2Java.class.getClassLoader() );

		Program program = ParsingUtils.parseProgram(
			cmdParser.programStream(),
			cmdParser.programFilepath().toURI(), cmdParser.charset(),
			cmdParser.includePaths(), cmdParser.jolieClassLoader(), cmdParser.definedConstants() );

		//Program program = parser.parse();
		inspector = ParsingUtils.createInspector( program );
	}

	@AfterClass
	public static void tearDownClass()
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
	}

	/**
	 * Test of ConvertDocument method, of class JavaDocumentCreator.
	 */
	@Test
	public void testConvertDocument() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
	{
		System.out.println( "ConvertDocument" );
		JavaDocumentCreator instance = new JavaDocumentCreator( inspector, "com.test", null, false );
		instance.ConvertDocument();

		assertEquals( "The number of generated files is wrong", 2, new File( "./generated/com/test" ).list().length );

		// compile files
		File generatedPath = new File( "./generated/com/test" );
		if ( generatedPath.exists() ) {
			String files[] = generatedPath.list();
			for( String temp : files ) {
				File sourceFile = new File( generatedPath, temp );
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				compiler.run( null, null, null, sourceFile.getPath() );
			}
		}

		// load classes
		File pippo = new File( "./generated" );
		URLClassLoader classLoader = URLClassLoader.newInstance( new URL[]{ pippo.toURI().toURL() } );

		// FileStructure
		Class<?> FlatStructureType = Class.forName( "com.test.FlatStructureType", true, classLoader ); // Should print "hello".
		Constructor flatStructureTypeConstructor = FlatStructureType.getConstructor( new Class[]{ Value.class } );
		Jolie2JavaInterface flatStructureType = (Jolie2JavaInterface) flatStructureTypeConstructor.newInstance( getFlatStructuredType() );
		// check constructor and getValues
		assertTrue( compareValues( getFlatStructuredType(), flatStructureType.getValue() ) );
		Jolie2JavaInterface flatStructureTypeEmpty = (Jolie2JavaInterface) FlatStructureType.newInstance();
		// check methods
		HashMap<String, Method> setMethodList = new HashMap<>();
		HashMap<String, Method> getMethodList = new HashMap<>();
		for( Entry<String, ValueVector> vv : getFlatStructuredType().children().entrySet() ) {
			boolean foundGet = false;
			boolean foundSet = false;
			for( Method method : FlatStructureType.getDeclaredMethods() ) {
				String mNameTmp = vv.getKey().substring( 0, 1 ).toUpperCase() + vv.getKey().substring( 1 );
				if ( method.getName().equals( "get" + mNameTmp ) ) {
					foundGet = true;
					getMethodList.put( mNameTmp, method );
				}
				if ( method.getName().equals( "set" + mNameTmp ) ) {
					foundSet = true;
					setMethodList.put( mNameTmp, method );
				}
			}
			assertTrue( "get method for field " + vv.getKey() + "not found ", foundGet );
			assertTrue( "set method for field " + vv.getKey() + "not found ", foundSet );
		}
		// invoking methods 
		for( Entry<String, ValueVector> vv : getFlatStructuredType().children().entrySet() ) {
			String mNameTmp = vv.getKey().substring( 0, 1 ).toUpperCase() + vv.getKey().substring( 1 );
			if ( vv.getValue().get( 0 ).isBool() ) {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ).boolValue() );
				Boolean returnValue = (Boolean) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertEquals( "check methods for field " + vv.getKey() + " failed", vv.getValue().get( 0 ).boolValue(), returnValue );
			} else if ( vv.getValue().get( 0 ).isInt() ) {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ).intValue() );
				Integer returnValue = (Integer) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertEquals( "check methods for field " + vv.getKey() + " failed", vv.getValue().get( 0 ).intValue(), returnValue.intValue() );
			} else if ( vv.getValue().get( 0 ).isString() ) {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ).strValue() );
				String returnValue = (String) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertEquals( "check methods for field " + vv.getKey() + " failed", vv.getValue().get( 0 ).strValue(), returnValue );
			} else if ( vv.getValue().get( 0 ).isDouble() ) {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ).doubleValue() );
				Double returnValue = (Double) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertEquals( "check methods for field " + vv.getKey() + " failed", new Double(vv.getValue().get( 0 ).doubleValue()), returnValue );
			} else if ( vv.getValue().get( 0 ).isLong() ) {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ).longValue() );
				Long returnValue = (Long) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertEquals( "check methods for field " + vv.getKey() + " failed", vv.getValue().get( 0 ).longValue(), returnValue.longValue() );
			} else if ( vv.getValue().get( 0 ).isByteArray() ) {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ).byteArrayValue() );
				ByteArray returnValue = (ByteArray) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertTrue( "check methods for field " + vv.getKey() + " failed", compareByteArrays( returnValue, vv.getValue().get( 0 ).byteArrayValue() ) );
			} else {
				setMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty, vv.getValue().get( 0 ) );
				Value returnValue = (Value) getMethodList.get( mNameTmp ).invoke( flatStructureTypeEmpty );
				assertTrue( "check methods for field " + vv.getKey() + " failed", compareValues(returnValue, vv.getValue().get( 0 ) ) );
			}
		}
	}

	private Value getFlatStructuredType()
	{
		Value testValue = Value.create();
		testValue.getFirstChild( "afield" ).setValue( TESTSTRING );
		testValue.getFirstChild( "bfield" ).setValue( TESTINTEGER );
		testValue.getFirstChild( "cfield" ).setValue( TESTDOUBLE );
		testValue.getFirstChild( "dfield" ).setValue( new ByteArray( TESTRAW ) );
		testValue.getFirstChild( "efield" ).setValue( TESTSTRING );
		testValue.getFirstChild( "ffield" ).setValue( TESTBOOL );
		testValue.getFirstChild( "gfield" ).setValue( getTestUndefined() );
		testValue.getFirstChild( "hfield" ).setValue( TESTLONG );
		return testValue;
	}

	private Value getTestUndefined()
	{
		Value returnValue = Value.create();
		returnValue.getFirstChild( "a" ).setValue( TESTBOOL );
		returnValue.getFirstChild( "a" ).getFirstChild( "b" ).setValue( TESTSTRING );
		returnValue.getFirstChild( "a" ).getFirstChild( "b" ).getFirstChild( "c" ).setValue( TESTDOUBLE );

		return returnValue;
	}

	private boolean compareValues( Value v1, Value v2 )
	{
		boolean resp = true;
		if ( !checkRootValue( v1, v2 ) ) {
			System.out.println( "Root values are different" );
			System.out.println( v1.strValue() + "," + v2.strValue() );
			return false;
		}
		// from v1 -> v2
		for( Entry<String, ValueVector> entry : v1.children().entrySet() ) {
			if ( !v2.hasChildren( entry.getKey() ) ) {
				System.out.println( "from v1 -> v2: field " + entry.getKey() + " not present" );
				return false;
			} else {
				if ( entry.getValue().size() != v2.getChildren( entry.getKey() ).size() ) {
					System.out.println( "The number of subnodes is different: " + entry.getValue().size() + "," + v2.getChildren( entry.getKey() ).size() );
					return false;
				}
				for( int i = 0; i < entry.getValue().size(); i++ ) {
					resp = compareValues( entry.getValue().get( i ), v2.getChildren( entry.getKey() ).get( i ) );
					if ( !resp ) {
						System.out.println( "Error found in subnode " + entry.getKey() + ", index:" + i );
						return false;
					}
				}
			}
		}

		// from v2 -> v1
		for( Entry<String, ValueVector> entry : v2.children().entrySet() ) {
			if ( !v1.hasChildren( entry.getKey() ) ) {
				System.out.println( "from v2 -> v1: field " + entry.getKey() + " not present" );
				return false;
			} else {
				if ( entry.getValue().size() != v1.getChildren( entry.getKey() ).size() ) {
					System.out.println( "The number of subnodes is different: " + entry.getValue().size() + "," + v1.getChildren( entry.getKey() ).size() );
					return false;
				}
				for( int i = 0; i < entry.getValue().size(); i++ ) {
					resp = compareValues( entry.getValue().get( i ), v1.getChildren( entry.getKey() ).get( i ) );
					if ( !resp ) {
						System.out.println( "Error found in subnode " + entry.getKey() + ", index:" + i );
						return false;
					}
				}
			}
		}
		return resp;
	}

	private boolean checkRootValue( Value v1, Value v2 )
	{
		boolean resp = true;
		if ( v1.isBool() && !v2.isBool() ) {
			resp = false;
		} else if ( v1.isBool() && v2.isBool() && (v1.boolValue() != v2.boolValue()) ) {
			resp = false;
		}
		if ( v1.isByteArray() && !v2.isByteArray() ) {
			resp = false;
		} else if ( v1.isByteArray() && v2.isByteArray() ) {
			resp = compareByteArrays( v1.byteArrayValue(), v2.byteArrayValue() );
		}
		if ( v1.isDouble() && !v2.isDouble() ) {
			resp = false;
		} else if ( v1.isDouble() && !v2.isDouble() && (v1.doubleValue() != v2.doubleValue()) ) {
			resp = false;
		}
		if ( v1.isDouble() && !v2.isDouble() ) {
			resp = false;
		} else if ( v1.isDouble() && v2.isDouble() && (v1.intValue() != v2.intValue()) ) {
			resp = false;
		}
		if ( v1.isLong() && !v2.isLong() ) {
			resp = false;
		} else if ( v1.isLong() && v2.isLong() && (v1.longValue() != v2.longValue()) ) {
			resp = false;
		}

		if ( !resp ) {
			System.out.println( "v1:" + v1.strValue() + ",isBool:" + v1.isBool() + ",isInt:" + v1.isInt() + ",isLong:" + v1.isLong() + ",isDouble:" + v1.isDouble() + ",isByteArray:" + v1.isByteArray() );
			System.out.println( "v2:" + v2.strValue() + ",isBool:" + v2.isBool() + ",isInt:" + v2.isInt() + ",isLong:" + v2.isLong() + ",isDouble:" + v2.isDouble() + ",isByteArray:" + v2.isByteArray() );
		}
		return resp;

	}

	private boolean compareByteArrays( ByteArray b1, ByteArray b2 )
	{
		if ( b1.getBytes().length != b2.getBytes().length ) {
			System.out.println( "ByteArray sizes are different: " + b1.getBytes().length + "," + b2.getBytes().length );
			return false;
		} else {
			for( int i = 0; i < b1.getBytes().length; i++ ) {
				if ( b1.getBytes()[ i ] != b2.getBytes()[ i ] ) {
					System.out.println( "Bytes at index " + i + " are different: " + b1.getBytes()[ i ] + "," + b2.getBytes()[ i ] );
					return false;
				}
			}
		}
		return true;
	}
}