package org.zeplinko.commons.lang.ext.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UUID64Test {
    private static Stream<Arguments> provideValidUUID64Strings() {
        return Stream.of(
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("VfO1sakQRC-BcgO4aVlmVQ"),
                Arguments.of("VfO1sakQRC_BcgO4aVlmVQ")
        );
    }

    private static Stream<Arguments> provideUUID64StringsWithInvalidCharacters() {
        return Stream.of(
                Arguments.of("AAAAAAAAAAAAAAAAAAAAA@"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAA="),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAA+"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAA/")
        );
    }

    private static Stream<Arguments> provideUUID64StringsWithInvalidLength() {
        return Stream.of(
                Arguments.of("AAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAA"),
                // Skipping the correct length entry
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAAAAAAA"),
                Arguments.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        );
    }

    private static Stream<Arguments> provideNonCanonicalUUID64Strings() {
        return Stream.of(
                Arguments.of("aaaaaaaaaaaaaaaaaaaaaa")
        );
    }

    @RepeatedTest(1000)
    void test_givenRandomUUID64_whenReconstructedUsingId_thenEquivalentUUID64GetsCreated() {
        UUID64 originalUUID64 = UUID64.randomUUID();
        UUID64 reconstructedUUID64 = UUID64.fromString(originalUUID64.toString());
        assertEquals(originalUUID64.getMostSignificantBits(), reconstructedUUID64.getMostSignificantBits());
        assertEquals(originalUUID64.getLeastSignificantBits(), reconstructedUUID64.getLeastSignificantBits());
        assertEquals(originalUUID64.toString(), reconstructedUUID64.toString());
    }

    @RepeatedTest(1000)
    void test_givenRandomUUID64UsingRandomLongBits_whenReconstructedUsingId_thenEquivalentUUID64GetsCreated() {
        Random random = new SecureRandom();
        UUID64 originalUUID = new UUID64(random.nextLong(), random.nextLong());
        UUID64 reconstructedUUID = UUID64.fromString(originalUUID.toString());
        assertEquals(originalUUID.getMostSignificantBits(), reconstructedUUID.getMostSignificantBits());
        assertEquals(originalUUID.getLeastSignificantBits(), reconstructedUUID.getLeastSignificantBits());
        assertEquals(originalUUID.toString(), reconstructedUUID.toString());
    }

    @RepeatedTest(1000)
    void test_givenRandomUUID_whenConvertedToUUID64AndReconstructedBack_thenEquivalentUUIDGetsCreated() {
        UUID originalUUID = UUID.randomUUID();
        UUID64 uuid64 = UUID64.fromUUID(originalUUID);
        UUID reconstructedUUID = uuid64.toUUID();
        assertEquals(originalUUID, reconstructedUUID);
    }

    @RepeatedTest(1000)
    void test_givenRandomUUID64_whenConvertedToUUIDAndReconstructedBack_thenEquivalentUUID64GetsCreated() {
        UUID64 originalUUID64 = UUID64.randomUUID();
        UUID uuid = originalUUID64.toUUID();
        UUID64 reconstructedUUID64 = UUID64.fromUUID(uuid);
        assertEquals(originalUUID64.getMostSignificantBits(), reconstructedUUID64.getMostSignificantBits());
        assertEquals(originalUUID64.getLeastSignificantBits(), reconstructedUUID64.getLeastSignificantBits());
        assertEquals(originalUUID64.toString(), reconstructedUUID64.toString());
    }

    @ParameterizedTest
    @MethodSource("provideValidUUID64Strings")
    void test_givenValidUUID64String_whenReconstructedUsingId_thenSucceeds(String uuid64String) {
        assertDoesNotThrow(() -> UUID64.fromString(uuid64String));
    }

    @ParameterizedTest
    @MethodSource("provideUUID64StringsWithInvalidCharacters")
    void test_givenInvalidCharactersInUUID64String_whenReconstructedUsingId_thenThrowsIllegalArgumentException(
            String uuid64String
    ) {
        assertThrowsExactly(IllegalArgumentException.class, () -> UUID64.fromString(uuid64String));
    }

    @ParameterizedTest
    @MethodSource("provideUUID64StringsWithInvalidLength")
    void test_givenInvalidLengthInUUID64String_whenReconstructedUsingId_thenThrowsIllegalArgumentException(
            String uuid64String
    ) {
        assertThrowsExactly(IllegalArgumentException.class, () -> UUID64.fromString(uuid64String));
    }

    @ParameterizedTest
    @MethodSource("provideNonCanonicalUUID64Strings")
    void test_givenNonCanonicalUUID64String_whenReconstructedUsingString_thenThrowsIllegalArgumentException(
            String uuid64String
    ) {
        assertThrowsExactly(IllegalArgumentException.class, () -> UUID64.fromString(uuid64String));
    }
}
