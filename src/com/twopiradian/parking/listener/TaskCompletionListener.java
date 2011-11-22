/**
 * 
 */
package com.twopiradian.parking.listener;

/**
 * @author Prodipta Golder
 *
 */
public interface TaskCompletionListener<T> {

	public void execute(T result, Object output);
}
