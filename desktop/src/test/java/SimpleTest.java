import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SimpleTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		System.out.println("A test was run.");
		org.junit.Assert.assertTrue(false);
	}

}
