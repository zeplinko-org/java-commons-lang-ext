package org.zeplinko.commons.lang.ext.util;

import org.zeplinko.commons.lang.ext.annotations.Preview;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * An <strong>immutable, URL-safe</strong> alternative to {@link java.util.UUID}
 * that writes the 128-bit value in Base 64 rather than hexadecimal. A canonical
 * {@code UUID64} is always exactly <em>22&nbsp;characters</em> long and
 * consists only of the URL-safe alphabet {@code [A-Za-z0-9_-]} (no '+', '/', or
 * '=' padding).
 * </p>
 *
 * <h2>Motivation</h2>
 * <ul>
 * <li><b>Compact representation</b> &mdash; 22 characters vs. the 36-character
 * hex form (<code>xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</code>).</li>
 * <li><b>URL-safe</b> &mdash; the encoded string can be embedded in paths,
 * query parameters, HTML id attributes, filenames, or QR codes without
 * escaping.</li>
 * <li><b>Interoperable</b> &mdash; conversions to and from the standard
 * {@link java.util.UUID} are loss-less via {@link #toUUID()} and
 * {@link #fromUUID(UUID)}.</li>
 * <li><b>Thread-safe / immutable</b> &mdash; the two 64-bit halves are stored
 * in <code>final</code> fields and never change.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * {
 *     &#64;code
 *     UUID64 id = UUID64.randomUUID(); // ⇢ "VfO1sakQRC-BcgO4aVlmVQ"
 *     String s = id.toString(); // 22-char Base64 string
 *     UUID64 id2 = UUID64.fromString(s); // parse and validate
 *     UUID jdk = id.toUUID(); // standard java.util.UUID
 * }
 * </pre>
 *
 * <p>
 * The class is annotated {@code @Preview} to signal that the public API may
 * evolve until stabilized.
 * </p>
 *
 * <p>
 * <strong>Implementation note:</strong> 22 characters arise from encoding 16
 * bytes (128 bits) with 6-bit Base 64 symbols and omitting the two trailing '='
 * padding bytes (16 × 8 / 6 = 21 ⅓ → 22 symbols).
 * </p>
 *
 * @author Shivam&nbsp;Nagpal
 */
@Preview
public class UUID64 {
    private final long mostSignificantBits;

    private final long leastSignificantBits;

    /**
     * Constructs a new {@code UUID64} directly from the two 64-bit halves of a
     * standard UUID.
     *
     * @param mostSignificantBits  the most-significant 64&nbsp;bits
     * @param leastSignificantBits the least-significant 64&nbsp;bits
     */
    public UUID64(long mostSignificantBits, long leastSignificantBits) {
        this.mostSignificantBits = mostSignificantBits;
        this.leastSignificantBits = leastSignificantBits;
    }

    /**
     * Generates a cryptographically strong random UUID (version 4) and returns it
     * as a {@code UUID64}.
     *
     * @return a new, random {@code UUID64}
     */
    public static UUID64 randomUUID() {
        UUID uuid = UUID.randomUUID();
        return fromUUID(uuid);
    }

    /**
     * Converts a standard {@link java.util.UUID} to its {@code UUID64} counterpart.
     * The operation is loss-less and reversible.
     *
     * @param uuid the JDK UUID to convert
     * @return a {@code UUID64} representing the same 128-bit value
     * @throws NullPointerException if {@code uuid} is {@code null}
     */
    public static UUID64 fromUUID(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid must not be null");
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();
        return new UUID64(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Parses the supplied text into a {@code UUID64}.
     * <p>
     * The method performs four independent validations:
     * </p>
     * <ol>
     * <li>the argument is not {@code null};</li>
     * <li>the text is valid Base 64;</li>
     * <li>decodes to exactly 16&nbsp;bytes; and</li>
     * <li>round-trips back to the <em>identical</em> string via {@link #toString()}
     * (guaranteeing canonical form).</li>
     * </ol>
     *
     * @param name the 22-character, URL-safe Base 64 representation
     * @return the corresponding {@code UUID64}
     * @throws IllegalArgumentException if the string fails any validation rule
     * @throws NullPointerException     if {@code name} is {@code null}
     */
    public static UUID64 fromString(String name) {
        Objects.requireNonNull(name, "name must not be null");
        byte[] decodedBytes;
        try {
            decodedBytes = decodeFromUrlSafeBase64(name);
        } catch (IllegalArgumentException iae) {
            throw getInvalidUUID64NameException(name);
        }
        if (decodedBytes.length != 16) {
            throw getInvalidUUID64NameException(name);
        }
        if (!name.equals(encodeToUrlSafeBase64(decodedBytes))) {
            throw getInvalidUUID64NameException(name);
        }
        ByteBuffer buffer = ByteBuffer.wrap(decodedBytes);
        return new UUID64(buffer.getLong(), buffer.getLong());
    }

    private static byte[] decodeFromUrlSafeBase64(String name) {
        return Base64.getUrlDecoder().decode(name);
    }

    private static IllegalArgumentException getInvalidUUID64NameException(String name) {
        return new IllegalArgumentException("Invalid UUID64 encoded string: " + name);
    }

    private static String encodeToUrlSafeBase64(byte[] uuidBytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
    }

    /**
     * Converts this instance back to a standard {@link java.util.UUID}.
     *
     * @return an equivalent {@code java.util.UUID}
     */
    public UUID toUUID() {
        return new UUID(getMostSignificantBits(), getLeastSignificantBits());
    }

    /**
     * Returns the most-significant 64&nbsp;bits of this UUID.
     *
     * @return the MSB half
     */
    public long getMostSignificantBits() {
        return mostSignificantBits;
    }

    /**
     * Returns the least-significant 64&nbsp;bits of this UUID.
     *
     * @return the LSB half
     */
    public long getLeastSignificantBits() {
        return leastSignificantBits;
    }

    /**
     * Encodes this UUID as a 22-character, URL-safe Base 64 string with no padding.
     *
     * @return the canonical string representation of this UUID
     */
    @Override
    public String toString() {
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes)
                .putLong(this.getMostSignificantBits())
                .putLong(this.getLeastSignificantBits());
        return encodeToUrlSafeBase64(uuidBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UUID64)) {
            return false;
        }
        UUID64 uuid64 = (UUID64) o;
        return getMostSignificantBits() == uuid64.getMostSignificantBits()
                && getLeastSignificantBits() == uuid64.getLeastSignificantBits();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMostSignificantBits(), getLeastSignificantBits());
    }
}
