---
title: "Controlling code execution after test failure"
---

As a tester, you may want the ability to access the result so far of a Galasa test at different points during the test lifecycle, to control the execution of code based on that result.

The `ITestResultProvider` can be used to get the test result so far after the first method invocation in a test.
It can give you access to if the test is in Passed or Failed state.
You can use the state of the test (Passed or Failed) to control how non-test code is run.

The annotation `@TestResultProvider` injects a `ITestResultProvider` object into your test class. The Core Manager updates the provider with the test result so far after each `@BeforeClass`, `@Before`, `@Test`, `@After` and `@AfterClass` method.

To use this capability, simply include the lines of code below in your test:

```java
@TestResultProvider
public ITestResultProvider testResultProvider;
```

In the example below, the `@AfterClass` method retrieves the result from the `ITestResultProvider` and checks if it is Failed.
If the result is Failed, the method `myCustomCleanupMethod` is called.
Running the `myCustomCleanupMethod` generates additional information to help diagnose the cause of the failure and cleans up resources.

```java
@AfterClass
public void afterClassMethod() throws FrameworkException {
    if (testResultProvider.getResult().isFailed()) {
        myCustomCleanupMethod();
    }
}

private void myCustomCleanupMethod() {
    try {
      // Custom diagnostic collection and cleanup logic that only happens on failures.
    } catch(Exception ex) {
       logger.error("Failing while cleaning up in myCustomCleanupMethod()");
       // Ignore the problem.
    }
}
```
