package main.java.com.test;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import main.java.com.node.Main;
import main.java.com.node.TopKHeap;
import main.java.com.node.Utils;

public class TopKHeapTest {

	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void TestAvailableMemory() {
		long amem = Utils.getAvailableMemory();
		Assert.assertNotEquals(0, amem);
	}
	
	@Test
	public final void TestBlockSize() {
		long bs = Utils.getBlockSize(100000000, 1024, 400000000);
		Assert.assertEquals(200000000, bs);
	}
	
	@Test
	public final void TestStringSize() {
		long ss = Utils.getStringSize("test");
		Assert.assertNotEquals(0, ss);
	}

}
