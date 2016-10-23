/** 
 */
package com.gewara.component;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  12:31:44 PM
 */
public class MultiNextList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 8695372508303893125L;
	private int cursor = 0;
	private final int stepNum = 888;
	
	public boolean hasNext() {
		if (cursor < size())
			return true;
		return false;
	}
	
	public List<E> nextRange(){
		if (!hasNext())
			throw new IndexOutOfBoundsException();
		int to =  size() - cursor < stepNum ? size(): stepNum + cursor;
		List<E> rangeElements = subList(cursor, to);
		cursor+=stepNum;
		return rangeElements;
	}
}
