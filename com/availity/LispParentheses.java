/**
 * Lisp Parentheses Checker
 * 
 */
package com.availity;

/**
 * @author George
 *
 */
public class LispParentheses {

	/**
	 * Check that the parentheses in the string are properly closed and nested
	 * @param s
	 * @return
	 */
	public static boolean hasMatchingParentheses(String s) {
		//use codePoints to properly support Unicode characters
		int cnt=s.codePoints().filter(p->(p=='('||p==')')).reduce(0,(total,element)->(
				//if total is negative it means closed parentheses exist without a matching open one e.g. 'a(b))c' 
				//need to retain this negative value till the end of the stream
				total<0?total:(
						element=='('?total+1:total-1))
				);
		return (cnt==0);
	}

}
