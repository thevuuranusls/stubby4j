package by.stub.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.util.NoSuchElementException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 11:14 AM
 */
public class StringUtilsTest {

   @Rule
   public ExpectedException expectedException = ExpectedException.none();

   @Test
   public void shouldConvertObjectToString_WhenObjectIsNotNull() throws Exception {

      final String result = StringUtils.objectToString(new Integer(888));

      assertThat(result).isEqualTo("888");
   }

   @Test
   public void shouldConvertObjectToString_WhenObjectIsNull() throws Exception {

      final String result = StringUtils.objectToString(null);

      assertThat(result).isEqualTo("");
   }

   @Test
   public void shouldConvertObjectToString_WhenObjectIsStringNull() throws Exception {

      final String result = StringUtils.objectToString("null");

      assertThat(result).isEqualTo("");
   }

   @Test
   public void shouldDetermineObjectStringValue_WhenObjectIsFailedMessage() throws Exception {

      final String result = StringUtils.determineObjectStringValue(StringUtils.getBytesUtf8(StringUtils.FAILED));

      assertThat(result).isEqualTo(StringUtils.FAILED);
   }

   @Test
   public void shouldFilterOutSpacesBetweenElementsWithQuotes() throws Exception {

      final String originalElementsWithQuotes = "[\"alex\", \"tracy\", \"logan\", \"charlie\", \"isa\"]";
      final String expectedElementsWithQuotes = "[\"alex\",\"tracy\",\"logan\",\"charlie\",\"isa\"]";

      final String filteredElementsWithQuotes = StringUtils.trimSpacesBetweenCSVElements(originalElementsWithQuotes);

      assertThat(expectedElementsWithQuotes).isEqualTo(filteredElementsWithQuotes);
   }

   @Test
   public void shouldFilterOutSpacesBetweenElementsWithoutQuotes() throws Exception {

      final String originalElements = "[alex, tracy, logan, charlie, isa]";
      final String expectedElements = "[alex,tracy,logan,charlie,isa]";

      final String filteredElements = StringUtils.trimSpacesBetweenCSVElements(originalElements);

      assertThat(expectedElements).isEqualTo(filteredElements);
   }

   @Test
   public void shouldRemoveEncodedSquareBracketsFromString() throws Exception {

      final String originalElements = "%5Balex,tracy,logan,charlie,isa%5D";
      final String expectedElements = "alex,tracy,logan,charlie,isa";

      final String filteredElements = StringUtils.removeSquareBrackets(originalElements);

      assertThat(expectedElements).isEqualTo(filteredElements);
   }

   @Test
   public void shouldRemoveSquareBracketsFromString() throws Exception {

      final String originalElements = "[alex,tracy,logan,charlie,isa]";
      final String expectedElements = "alex,tracy,logan,charlie,isa";

      final String filteredElements = StringUtils.removeSquareBrackets(originalElements);

      assertThat(expectedElements).isEqualTo(filteredElements);
   }

   @Test
   public void shouldReturnTrueWhenStringWithinSquareBrackets() throws Exception {

      final String originalElements = "[%22id%22,%20%22uuid%22,%20%22created%22,%20%22lastUpdated%22,%20%22displayName%22,%20%22email%22,%20%22givenName%22,%20%22familyName%22]";

      final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

      assertThat(isWithinSquareBrackets).isTrue();
   }

   @Test
   public void shouldReturnTrueWhenStringWithinEncodedSquareBrackets() throws Exception {

      final String originalElements = "%5Balex,tracy,logan,charlie,isa%5D";

      final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

      assertThat(isWithinSquareBrackets).isTrue();
   }


   @Test
   public void shouldReturnFalseWhenStringWithinNotPairOfEscapedSquareBracket() throws Exception {

      final String originalElements = "%5Balex,tracy,logan,charlie,isa";

      final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

      assertThat(isWithinSquareBrackets).isFalse();
   }

   @Test
   public void shouldReturnFalseWhenStringWithinNotPairOfSquareBracket() throws Exception {

      final String originalElements = "[alex,tracy,logan,charlie,isa";

      final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

      assertThat(isWithinSquareBrackets).isFalse();
   }

   @Test
   public void shouldReturnNullWhenTryingTolowerEmptyString() throws Exception {
      assertThat(StringUtils.toLower("")).isNull();
   }

   @Test
   public void shouldReturnNullWhenTryingToUpperEmptyString() throws Exception {
      assertThat(StringUtils.toUpper("")).isNull();
   }

   @Test
   public void shouldReturnErrorWhenTryingToConvertNullInputStreamToString() throws Exception {
      assertThat(StringUtils.inputStreamToString(null)).isEqualTo("Could not convert empty or null input stream to string");
   }

   @Test
   public void shouldReturnErrorWhenTryingToConvertEmptyInputStreamToString() throws Exception {

      expectedException.expect(NoSuchElementException.class);

      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(StringUtils.getBytesUtf8(""));
      assertThat(StringUtils.inputStreamToString(byteArrayInputStream)).isEqualTo("");
   }

   @Test
   public void shouldReturnErrorWhenTryingToConvertSpaceInputStreamToString() throws Exception {

      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(StringUtils.getBytesUtf8(" "));
      assertThat(StringUtils.inputStreamToString(byteArrayInputStream)).isEqualTo("");
   }


   @Test
   public void shouldCorrectlyEncodeSingleQuotesInURL() throws Exception {

      final String originaUrl = "http://localhost:8882/entity.find.single.quote?client_secret=secret&attributes=['id','uuid','created','lastUpdated','displayName','email','givenName','familyName']";
      final String expectedEncodedUrl = "http://localhost:8882/entity.find.single.quote?client_secret=secret&attributes=[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]";
      final String actualEncodedUrl = StringUtils.encodeSingleQuotes(originaUrl);

      assertThat(actualEncodedUrl).isEqualTo(expectedEncodedUrl);
   }

   @Test
   public void shouldConstructUserAgentNameWhenImplementationTitleNotSet() throws Exception {

      final String userAgentName = StringUtils.constructUserAgentName();

      assertThat(userAgentName).contains("stubby4j");
      assertThat(userAgentName).contains("(HTTP stub client request)");
   }
}
