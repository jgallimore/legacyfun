package example.web;

import junit.framework.TestCase;

/**
 * This is a simple JUnit test case to ensure that the environment is okay.
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: SanityTest.java 356097 2005-12-12 01:29:03Z jsisson $
 */
public class SanityTest extends TestCase {
  public void testSanity() {
    assertEquals( "test", "test" );
  }
}
