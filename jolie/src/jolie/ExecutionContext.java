/**
 * *************************************************************************
 *   Copyright (C) 2006-2008 by Fabrizio Montesi <famontesi@gmail.com> * * This
 * program is free software; you can redistribute it and/or modify * it under
 * the terms of the GNU Library General Public License as * published by the
 * Free Software Foundation; either version 2 of the * License, or (at your
 * option) any later version. * * This program is distributed in the hope that
 * it will be useful, * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. * * You should have received a
 * copy of the GNU Library General Public * License along with this program; if
 * not, write to the * Free Software Foundation, Inc., * 59 Temple Place - Suite
 * 330, Boston, MA 02111-1307, USA. * * For details about the authors of this
 * software, see the AUTHORS file. *
 **************************************************************************
 */
package jolie;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import jolie.behaviours.Behaviour;
import jolie.behaviours.ScopeBehaviour;
import jolie.lang.Constants;
import jolie.net.SessionMessage;
import jolie.runtime.AbstractIdentifiableObject;
import jolie.runtime.FaultException;
import jolie.runtime.InputOperation;

/**
 * Replaces ExecutionThread.
 * 
 * @author martin
 */
public abstract class ExecutionContext extends JolieContext
{
	/**
	 * A Scope object represents a fault handling scope,
	 * containing mappings for fault handlers and termination/compensation handlers.
	 */
	protected class Scope extends AbstractIdentifiableObject implements Cloneable {
		
		private final Map< String, Behaviour > faultMap;
		private final Map< String, Behaviour > compMap;

		
		@Override
		public Scope clone()
		{
			return new Scope( id, new HashMap<>( faultMap ), new HashMap<>( compMap ) );
		}
		
		private Scope( String id, Map< String, Behaviour > faultMap, Map< String, Behaviour > compMap )
		{
			super( id );
			this.faultMap = faultMap;
			this.compMap = compMap;
		}

		/**
		 * Constructor
		 * @param id the name identifier of the Scope instance to be created.
		 */
		public Scope( String id )
		{
			this( id, new HashMap<>(), new HashMap<>() );
		}

		/**
		 * Installs a termination/compensation handler for this <code>Scope</code>.
		 * @param process the termination/compensation handler to install.
		 */
		public void installCompensation( Behaviour process )
		{
			compMap.put( id, process );
		}

		/**
		 * Installs a fault handler for a fault.
		 * @param faultName the fault name to install this handler for
		 * @param process the fault handler to install
		 */
		public void installFaultHandler( String faultName, Behaviour process )
		{
			faultMap.put( faultName, process );
		}

		/**
		 * Returns the installed fault handler for the specified fault name.
		 * If no fault handler is present, the default fault handler is returned instead.
		 * If there is no fault handler and there is no default fault handler, <code>null</code> is returned.
		 * @param faultName the fault name of the fault handler to retrieve
		 * @param erase <code>true</code> if after getting the fault handler it is to be uninstalled from the scope, <code>false</code> otherwise
		 * @return the installed fault handler for the specified fault name
		 */
		public Behaviour getFaultHandler( String faultName, boolean erase )
		{
			Behaviour p = faultMap.get( faultName );
			if ( erase ) { // Not called by cH (TODO: this is obscure!)
				if ( p == null ) {
					// Give the default handler
					faultName = Constants.Keywords.DEFAULT_HANDLER_NAME;
					p = faultMap.get( faultName );
				}
				if ( p != null ) {
					// Could still be null if there was not a default handler
					faultMap.remove( faultName );
				}
			}
				
			return p;
		}

		/**
		 * Returns the termination/compensation handler for this scope.
		 * The handler does not get uninstalled.
		 * @return the termination/compensation handler for this scope
		 */
		public Behaviour getSelfCompensation()
		{
			return compMap.get( id );
		}

		/**
		 * Returns the termination/compensation handler for the specified sub-scope.
		 * The handler gets uninstalled as a result of this method.
		 * @param scopeName the scope name of the termination/compensation handler to retrieve
		 * @return the termination/compensation handler for the specified sub-scope
		 */
		public Behaviour getCompensation( String scopeName )
		{
			Behaviour p = compMap.get( scopeName );
			if ( p != null )
				compMap.remove( scopeName );
			return p;
		}

		/**
		 * Puts all the compensation handlers defined in the passed <code>Scope</code> in the handler map of this scope.
		 * @param otherScope the scope whose compensation handlers are to be taken
		 */
		public void mergeCompensations( Scope otherScope )
		{
			compMap.putAll( otherScope.compMap );
		}
	}

	protected Deque< Scope > scopeStack = new ArrayDeque<>();
	private Deque< WeakReference< Future< ? > > > futureToCancel = new ArrayDeque<>();
	private FaultException killerFault = null;
	protected final ExecutionContext parent;
	protected Deque<Behaviour> processStack = new ArrayDeque<>();
	protected Deque<Deque<Behaviour>> scopeProcessStack = new ArrayDeque<>();
	protected boolean pauseExecution = false;
	
	/**
	 * Sets if this thread can be interrupted by a fault signal or not.
	 * @deprecated
	 */
	public void setCanBeInterrupted( boolean b )
	{}
	
	/**
	 * Constructor
	 * @param process the Process to be executed by this thread
	 * @param parent the parent of this thread
	 */
	public ExecutionContext( Behaviour process, ExecutionContext parent )
	{
		super( parent.interpreter() );
		if ( process != null )
			this.processStack.push( process );
		this.parent = parent;
	}
	
	/**
	 * Constructor
	 * @param interpreter the Interpreter this thread should refer to
	 * @param process the Process to be executed by this thread
	 */
	public ExecutionContext( Interpreter interpreter, Behaviour process )
	{
		super( interpreter );
		if ( process != null )
			this.processStack.push( process );
		this.parent = null;
	}

	/**
	 * Kills this ExecutionThread, interrupting its activity as soon as possible.
	 * @param fault the fault causing the interruption.
	 */
	public synchronized void kill( FaultException fault )
	{
		killerFault = fault;
		while( !futureToCancel.isEmpty() ) {
			final WeakReference< Future< ? > > ref = futureToCancel.poll();
			if ( ref.get() != null ) {
				ref.get().cancel( true );
			}
		}
		
		// TODO handle this (remove links?)
//		if( canBeInterrupted ) {
//			taskFuture.cancel( canBeInterrupted );
//		}
	}
	
	/**
	 * Returns the fault which killed this thread, if any. null otherwise.
	 * @return the fault which killed this thread, if any. null otherwise.
	 */
	public FaultException killerFault()
	{
		return killerFault;
	}

	/**
	 * Resets the killed state of this thread, returning it to normal execution.
	 */
	public void clearKill()
	{
		killerFault = null;
	}

	/**
	 * Returns true if this thread is killed, false otherwise.
	 * @return true if this thread is killed, false otherwise.
	 */
	public boolean isKilled()
	{
		return (killerFault != null);
	}
	
	/**
	 * Executes {@code runnable} if this context is not killed.
	 * @param runnable 
	 */
	public void ifNotKilled( Runnable runnable )
	{
		if ( !isKilled() ) {
			runnable.run();
		}
	}

	/**
	 * Returns the compensator of the current executing scope.
	 * @return the compensator of the current executing scope.
	 */
	public synchronized Behaviour getCurrentScopeCompensation()
	{
		if( scopeStack.isEmpty() && parent != null ) {
			return parent.getCurrentScopeCompensation();
		}
		
		return scopeStack.peek().getSelfCompensation();
	}
	
	/**
	 * Returns the compensator for scope name id.
	 * @param id the scope name owning the compensator to retrieve
	 * @return the compensator for scope name id.
	 */
	public synchronized Behaviour getCompensation( String id )
	{
		if ( scopeStack.isEmpty() && parent != null ) {
			return parent.getCompensation( id );
		}
		
		return scopeStack.peek().getCompensation( id );
	}
	
	/**
	 * Returns true if this thread is executing inside a scope.
	 * Use this method to check if calling a variant of popScope is safe.
	 * @see #popScope()
	 * @see #popScope(boolean)
	 * @return true if this thread is executing inside a scope.
	 */
	public synchronized boolean hasScope()
	{
		return !scopeStack.isEmpty();
	}
	
	/**
	 * Returns the id of the current executing scope.
	 * @return the id of the current executing scope.
	 */
	public synchronized String currentScopeId()
	{
		if( scopeStack.isEmpty() && parent != null ) {
			return parent.currentScopeId();
		}
		
		return scopeStack.peek().id();
	}
	
	/**
	 * Registers a future to be cancelled when this thread is killed.
	 * @param f the future to cancel
	 */
	public synchronized void cancelIfKilled( Future< ? > f )
	{
		cleanFuturesToKill();
		if ( isKilled() ) {
			f.cancel( true );
		}
		futureToCancel.add( new WeakReference<>( f ) );
	}
	
	private void cleanFuturesToKill()
	{
		boolean keepAlive = true;
		while( !futureToCancel.isEmpty() && keepAlive ) {
			final WeakReference< Future< ? > > ref = futureToCancel.peek();
			if ( ref.get() == null ) {
				futureToCancel.removeFirst();
			} else {
				keepAlive = false;
			}
		}
	}

	/**
	 * Returns the current fault handler for fault id.
	 * @param id the id of the fault handler to retrieve.
	 * @param erase <code>true</code> if the fault handler should be
	 *		removed before returning it.
	 * @return the current fault handler for fault id.
	 */
	public synchronized Behaviour getFaultHandler( String id, boolean erase )
	{
		if ( scopeStack.isEmpty() && parent != null ) {
			return parent.getFaultHandler( id, erase );
		}
		
		return scopeStack.peek().getFaultHandler( id, erase );
	}
	
	private ScopeBehaviour.Execution scopeExecution;
	public ScopeBehaviour.Execution getCurrentScopeExecution()
	{
		return scopeExecution;
	}
	
	/**
	 * Pushes scope id as the new current executing scope in the scope stack of this thread.
	 * @param id the id of the scope to push.
	 */
	public synchronized ScopeBehaviour.Execution pushScope( String id, ScopeBehaviour.Execution replacingExecution )
	{
		scopeStack.push( new ExecutionContext.Scope( id ) );
		ScopeBehaviour.Execution replacedExecution = scopeExecution;
		scopeExecution = replacingExecution;
		scopeProcessStack.push( processStack );
		processStack = new ArrayDeque<>();
		return replacedExecution;
	}
	
	/**
	 * Pops the current executing scope from the scope stack of this thread.
	 * @param merge <code>true</code> if the popped scope compensators
	 *		should be propagated upstream to the parent scope.
	 */
	public synchronized void popScope( boolean merge, ScopeBehaviour.Execution replacingExecution )
	{
		final ExecutionContext.Scope s = scopeStack.pop();
		
		if ( merge ) {
			mergeCompensations( s );
		}
		
		if ( !scopeProcessStack.isEmpty() ){
			processStack = scopeProcessStack.pop();
		}
		
		scopeExecution = replacingExecution;
	}
	
	/**
	 * Pops the current executing scope from the scope stack of this thread.
	 * This method is a shortcut for <code>popScope(true)</code>.
	 */
	public synchronized void popScope(ScopeBehaviour.Execution replacingExecution)
	{
		popScope( true, replacingExecution );
	}
	
	private synchronized void mergeCompensations( ExecutionContext.Scope s )
	{
		if ( scopeStack.isEmpty() ) {
			if ( parent != null ) {
				parent.mergeCompensations( s );
			}
		} else {
			scopeStack.peek().mergeCompensations( s );
		}
	}
	
	/**
	 * Installs process as the compensator for the current scope.
	 * @param process the process to install as compensator for the current scope
	 */
	public synchronized void installCompensation( Behaviour process )
	{
		if ( scopeStack.isEmpty() && parent != null ) {
			parent.installCompensation( process );
		} else {
			scopeStack.peek().installCompensation( process );
		}
	}
	
	/**
	 * Installs process as the fault handler for fault id.
	 * @param id the fault to be handled by process
	 * @param process the Process to be called for handling fault id
	 */
	public synchronized void installFaultHandler( String id, Behaviour process )
	{
		if ( scopeStack.isEmpty() && parent != null ) {
			parent.installFaultHandler( id, process );
		} else {
			scopeStack.peek().installFaultHandler( id, process );
		}
	}

	/**
	 * Returns the State this ExecutionThread refers to.
	 * @return the State this ExecutionThread refers to
	 * @see jolie.State
	 */
	public abstract jolie.State state();

	/**
	 * Requests a message from the currently executing session.
	 * @param operation the operation on which the process wants to receive the message
	 * @param ctx
	 * @return a {@link Future} that will return the received message.
	 */
	public abstract SessionMessage requestMessage( InputOperation operation, ExecutionContext ctx );

	/**
	 * Requests a message from the currently executing session.
	 * @param operations the map of possible operations on which the process wants to receive the message
	 * @param ctx
	 * @return a {@link Future} that will return the received message.
	 */
	public abstract SessionMessage requestMessage( Map< String, InputOperation > operations, ExecutionContext ctx);

	public abstract String getSessionId();
	
	public void start()
	{
		interpreter().runJolieThread( this );
	}
	
	public void executeNext( Behaviour ... behaviours )
	{
		for( int i = behaviours.length  - 1; i >= 0; i-- ) {
			processStack.push( behaviours[ i ] );
		}
	}
	
	/**
	 * Breaks execution at next before next process is run.
	 * This ExecutionContext should be rescheduled for execution at a later point 
	 * in time.
	 */
	public void pauseExecution()
	{
		pauseExecution = true;
	}
}
