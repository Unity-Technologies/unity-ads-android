package com.unity3d.ads.test.instrumentation.services.core.webview.bridge;

import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;
import com.unity3d.services.core.webview.bridge.WebViewBridgeSharedObjectStore;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class WebViewBridgeSharedObjectTests {
	//Test Implementation of WebViewBridgeSharedObjectStore
	private class WebViewBridgeSharedObjectStoreTestImplementation extends WebViewBridgeSharedObjectStore<IWebViewSharedObject> {

		@Override
		public synchronized IWebViewSharedObject get(String id) {
			return super.get(id);
		}

		@Override
		public synchronized void set(IWebViewSharedObject sharedObject) {
			super.set(sharedObject);
		}
	}

	private static String TestId = "TestId";

	private WebViewBridgeSharedObjectStoreTestImplementation sharedObjectStore;
	private IWebViewSharedObject sharedObject;

	@Before
	public void beforeEachTest() {
		sharedObjectStore = new WebViewBridgeSharedObjectStoreTestImplementation();
		sharedObject = mock(IWebViewSharedObject.class);
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				return TestId;
			}}).when(sharedObject).getId();
	}

	@Test
	public void testGetReturnsObjectStoredInHashMapBySet() {
		sharedObjectStore.set(sharedObject);
		Assert.assertThat("Expected object was not returned by get", sharedObjectStore.get(sharedObject.getId()), Is.is(sharedObject));
	}

	@Test
	public void testGetReturnsNullWhenObjectDoesNotExist() {
		Assert.assertNull(sharedObjectStore.get(sharedObject.getId()));
	}

	@Test
	public void testSetWithNullObject() {
		sharedObjectStore.set(null);
	}

	@Test
	public void testGetWithNullObject() {
		Assert.assertNull(sharedObjectStore.get(null));
	}

	@Test
	public void testGetReturnsNullAfterRemove() {
		sharedObjectStore.set(sharedObject);
		Assert.assertThat("Shared object was not stored properly", sharedObjectStore.get(sharedObject.getId()), Is.is(sharedObject));
		sharedObjectStore.remove(sharedObject.getId());
		Assert.assertNull("Shared object should have been removed", sharedObjectStore.get(sharedObject.getId()));
	}

	@Test
	public void testRemoveDoesNothingIfSharedObjectIsNull() {
		sharedObjectStore.remove((IWebViewSharedObject) null);
	}

	@Test
	public void testRemoveDoesNothingIfSharedObjectIdDoesNotExist() {
		sharedObjectStore.remove(sharedObject.getId());
	}
}
