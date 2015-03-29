package ch.vorburger.hotea.tests.notoncp;

import ch.vorburger.hotea.tests.SomeInterface;

public class SomeInterfaceImpl implements SomeInterface {
	@Override public String whatup() {
		return "hello, world"; // or "world, hello!";
	}
}
