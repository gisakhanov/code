/**
 * Lisp Parentheses Checker
 */
package com.availity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author George
 *
 */
class LispParenthesesTest {

	@Test
	void noParentheses() {
		assertTrue(LispParentheses.hasMatchingParentheses("abc"));
	}

	@Test
	void openCloseParentheses() {
		assertTrue(LispParentheses.hasMatchingParentheses("a(b)c"));
	}

	@Test
	void openParentheses() {
		assertFalse(LispParentheses.hasMatchingParentheses("(a(b)c"));
	}

	@Test
	void closeParentheses() {
		assertFalse(LispParentheses.hasMatchingParentheses("a(b)c)"));
	}
}
