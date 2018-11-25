package com.forgerock.edu.contactlist.rest.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Token id generator. Generates a unique id using SecureRandom and SHA-1.
 *
 * @author vrg
 */
public class TokenIdGenerator {

    private final SecureRandom prng;

    public TokenIdGenerator() {
        try {
            prng = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("No such algorithm: SHA1PRNG", ex);
        }
    }
    
    /**
     * Creates a random tokenId.
     * @return 
     */
    public String nextTokenId() {
        try {
            byte[] buffer = new byte[8];
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            prng.nextBytes(buffer);
            byte[] digest = sha.digest(buffer);
            return hexEncode(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("No such algorithm: SHA-1", ex);
        }
    }

    /**
     * The byte[] returned by MessageDigest does not have a nice textual
     * representation, so some form of encoding is usually performed.
     *
     * This implementation follows the example of David Flanagan's book "Java In
     * A Nutshell", and converts a byte array into a String of hex characters.
     *
     * Another popular alternative is to use a "Base64" encoding.
     */
    private static String hexEncode(byte[] aInput) {
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(digits[(b & 0xf0) >> 4]);
            result.append(digits[b & 0x0f]);
        }
        return result.toString();
    }

}
