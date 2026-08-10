package org.qubership.automation.itf.core.util.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionsTest {

    // ==================== getMessagesOnly() TESTS ====================

    @Test
    public void getMessagesOnly_singleExceptionWithMessage() {
        // Given
        String exceptionMessage = "Database connection failed";
        Exception exception = new Exception(exceptionMessage);

        // When
        String result = Exceptions.getMessagesOnly(exception);

        // Then
        assertEquals("Exception: " + exceptionMessage, result);
    }

    @Test
    public void getMessagesOnly_singleExceptionWithNullMessage() {
        // Given
        Exception exception = new NullPointerException(); // message is null

        // When
        String result = Exceptions.getMessagesOnly(exception);

        // Then
        assertEquals("NullPointerException: ", result);
    }

    @Test
    public void getMessagesOnly_chainedExceptions() {
        // Given
        String rootCauseMessage = "Invalid parameter: userId";
        Exception rootCause = new IllegalArgumentException(rootCauseMessage);

        String middleMessage = "Processing failed";
        Exception middle = new RuntimeException(middleMessage, rootCause);

        String topMessage = "Operation aborted";
        Exception top = new Exception(topMessage, middle);

        // When
        String result = Exceptions.getMessagesOnly(top);

        // Then
        String expected = "Exception: " + topMessage + ":\n" +
                "RuntimeException: " + middleMessage + ":\n" +
                "IllegalArgumentException: " + rootCauseMessage;
        assertEquals(expected, result);
    }

    @Test
    public void getMessagesOnly_chainedWithNullMessages() {
        // Given
        Exception rootCause = new NullPointerException(); // message is null
        Exception top = new RuntimeException(rootCause); // Once rootCause message is null, its class name is used

        // When
        String result = Exceptions.getMessagesOnly(top);

        // Then
        assertEquals("RuntimeException: java.lang.NullPointerException:\n"
                + "NullPointerException: ", result); // all messages are null, but exceptions' class names are printed
    }

    @Test
    public void getMessagesOnly_mixedNullAndNonNullMessages() {
        // Given
        String rootCauseMessage = "Root cause message";
        Exception rootCause = new IllegalArgumentException(rootCauseMessage);

        Exception middle = new NullPointerException(); // null message
        middle.initCause(rootCause);

        String topMessage = "Top message";
        Exception top = new RuntimeException(topMessage, middle);

        // When
        String result = Exceptions.getMessagesOnly(top);

        // Then
        String expected = "RuntimeException: " + topMessage + ":\n"
                + "NullPointerException: :\n"
                + "IllegalArgumentException: " + rootCauseMessage;
        assertEquals(expected, result);
    }

    @Test
    public void getMessagesOnly_withNullInput() {
        // When/Then - should not throw NPE
        String result = Exceptions.getMessagesOnly(null);

        // Then
        assertEquals("", result); // Joiner.on(":\n").join(empty list) returns empty string
    }

    // ==================== fillWithBriefInfo() TESTS ====================

    @Test
    public void fillWithBriefInfo_withCoreException() {
        // Given
        String message = "Core error occurred";
        StringBuilder sb = new StringBuilder();
        CoreException coreException = new CoreException(message);

        // When
        Exceptions.fillWithBriefInfo(sb, coreException);

        // Then
        assertEquals("CoreException: " + message, sb.toString());
    }

    @Test
    public void fillWithBriefInfo_withCoreExceptionAndNullMessage() {
        // Given
        StringBuilder sb = new StringBuilder();
        CoreException coreException = new CoreException((String) null);

        // When
        Exceptions.fillWithBriefInfo(sb, coreException);

        // Then
        assertEquals("CoreException: ", sb.toString());
    }

    @Test
    public void fillWithBriefInfo_withNonCoreExceptionWithoutCause() {
        // Given
        StringBuilder sb = new StringBuilder();
        Exception regularException = new IllegalArgumentException("Invalid argument");

        // When
        Exceptions.fillWithBriefInfo(sb, regularException);

        // Then
        String result = sb.toString();
        assertTrue(result.startsWith("java.lang.IllegalArgumentException: Invalid argument\n"));
    }

    @Test
    public void fillWithBriefInfo_withNonCoreException_chained() {
        // Given
        StringBuilder sb = new StringBuilder();
        Exception rootCause = new ArithmeticException("Division by zero");
        Exception topException = new RuntimeException("Calculation error", rootCause);

        // When
        Exceptions.fillWithBriefInfo(sb, topException);

        // Then
        String result = sb.toString();
        assertTrue(result.startsWith("RuntimeException: Calculation error\n"
                + "java.lang.ArithmeticException: Division by zero\n"));
    }

    @Test
    public void fillWithBriefInfo_withNonCoreExceptionAndNullMessages() {
        // Given
        StringBuilder sb = new StringBuilder();
        Exception rootCause = new NullPointerException(); // null message
        Exception topException = new RuntimeException(rootCause);

        // When
        Exceptions.fillWithBriefInfo(sb, topException);

        // Then
        String result = sb.toString();
        assertTrue(result.startsWith("RuntimeException: java.lang.NullPointerException\n"
                + "java.lang.NullPointerException\n"));
    }

    @Test(expected = NullPointerException.class)
    public void fillWithBriefInfo_withNullStringBuilder_throwsNPE() {
        // When
        Exceptions.fillWithBriefInfo(null, new Exception());
    }

    @Test
    public void fillWithBriefInfo_withNullThrowable_resultsEmptyString() {
        // Given
        StringBuilder sb = new StringBuilder();

        // When
        Exceptions.fillWithBriefInfo(sb, null);

        // Then
        String result = sb.toString();
        assertTrue(result.isEmpty());
    }

    // ==================== getExceptionSummary() TESTS ====================

    @Test
    public void getExceptionSummary_withNullInput() {
        // When
        String result = Exceptions.getExceptionSummary(null);

        // Then
        assertEquals("null exception", result);
    }

    @Test
    public void getExceptionSummary_withNormalMessage() {
        // Given
        String message = "Something went wrong";
        Exception exception = new Exception(message);

        // When
        String result = Exceptions.getExceptionSummary(exception);

        // Then
        assertEquals(message, result);
    }

    @Test
    public void getExceptionSummary_withEmptyMessage() {
        // Given
        Exception exception = new Exception("");

        // When
        String result = Exceptions.getExceptionSummary(exception);

        // Then
        assertEquals("Exception", result); // falls back to class name
    }

    @Test
    public void getExceptionSummary_withBlankMessage() {
        // Given
        Exception exception = new Exception("   ");

        // When
        String result = Exceptions.getExceptionSummary(exception);

        // Then
        assertEquals("Exception", result);
    }

    @Test
    public void getExceptionSummary_withNullMessage_fallsBackToClassName() {
        // Given
        Exception exception = new NullPointerException(); // message is null

        // When
        String result = Exceptions.getExceptionSummary(exception);

        // Then
        assertEquals("NullPointerException", result);
    }

    @Test
    public void getExceptionSummary_withNullMessageButCauseHasMessage() {
        // Given
        IllegalArgumentException cause = new IllegalArgumentException("Invalid ID format");
        Exception topException = new RuntimeException(null, cause); // null message

        // When
        String result = Exceptions.getExceptionSummary(topException);

        // Then
        assertEquals("RuntimeException, caused by: Invalid ID format", result);
    }

    @Test
    public void getExceptionSummary_withNullMessageAndCauseAlsoHasNullMessage() {
        // Given
        NullPointerException cause = new NullPointerException(); // null message
        Exception topException = new RuntimeException(null, cause);

        // When
        String result = Exceptions.getExceptionSummary(topException);

        // Then
        assertEquals("RuntimeException, caused by: NullPointerException", result);
    }

    @Test
    public void getExceptionSummary_withDeepCauseChain() {
        // Given
        Exception rootCause = new IllegalArgumentException("Root cause");
        Exception middle = new RuntimeException(null, rootCause);
        Exception top = new Exception(null, middle);

        // When
        String result = Exceptions.getExceptionSummary(top);

        // Then
        // Only exception itself and its cause are checked, not deeper
        assertEquals("Exception, caused by: RuntimeException", result);
    }

    @Test
    public void getExceptionSummary_withSelfCausingException_protectsAgainstRecursion() throws Exception {
        // 1. Create common exception
        Exception circular = new Exception("Test message");

        // 2. Via Reflection, we forcibly set cause to itself
        //    It emulates broken object (which can't be created via constructors/setters),
        //    produced by means of deserializing.
        Field causeField = Throwable.class.getDeclaredField("cause");
        causeField.setAccessible(true);
        causeField.set(circular, circular); // Bypass check of initCause()

        // 3. Also, one should turn off flag, which is used by Throwable for checks.
        //    Depending on JDK version, it can be 'cause' or 'suppressedExceptions' field.
        //    It's enough for the majority of versions.

        // 4. Invoke our method
        String result = Exceptions.getExceptionSummary(circular);

        // 5. Check that the method isn't in the infinite recursion and returned normal result
        //    Expect simple exception message
        assertNotNull(result);
        assertTrue(result.contains("Test message") || result.contains("Exception"));
    }

    @Test
    public void getExceptionSummary_withCustomException() {
        // Given
        class CustomException extends Exception {
            CustomException(String message) { super(message); }
        }
        CustomException custom = new CustomException("Custom error occurred");

        // When
        String result = Exceptions.getExceptionSummary(custom);

        // Then
        assertEquals("Custom error occurred", result);
    }

    @Test
    public void getExceptionSummary_withCoreException() {
        // Given
        CoreException coreException = new CoreException("Core business error");

        // When
        String result = Exceptions.getExceptionSummary(coreException);

        // Then
        assertEquals("Core business error", result);
    }

    // ==================== Edge Cases ====================

    @Test
    public void getExceptionSummary_withVeryLongMessage() {
        // Given
        String longMessage = StringUtils.repeat("AAAAA", 2000);
        Exception exception = new Exception(longMessage);

        // When
        String result = Exceptions.getExceptionSummary(exception);

        // Then
        assertEquals(longMessage, result);
    }

    @Test
    public void getMessagesOnly_withVeryDeepChain() {
        // Given
        Throwable current = new Exception("Level 1");
        for (int i = 2; i <= 100; i++) {
            current = new Exception("Level " + i, current);
        }

        // When
        String result = Exceptions.getMessagesOnly(current);

        // Then
        assertTrue(result.contains("Level 100"));
        assertTrue(result.contains("Level 1"));
        assertTrue(result.split(":\n").length >= 100);
    }

    @Test
    public void fillWithBriefInfo_appendsToExistingStringBuilderContent() {
        // Given
        StringBuilder sb = new StringBuilder("Prefix: ");
        Exception exception = new Exception("Test error");

        // When
        Exceptions.fillWithBriefInfo(sb, exception);

        // Then
        assertTrue(sb.toString().startsWith("Prefix: "));
        assertTrue(sb.toString().contains("Test error"));
    }
}