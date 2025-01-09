package com.uijae.cms.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Aes256UtilTest {

    @Test
    void test() {
        String originalText = "TestUserPk123";
        String encryptedText = Aes256Util.encrypt(originalText);
        String decryptedText = Aes256Util.decrypt(encryptedText);

        System.out.println("Original Text: " + originalText);
        System.out.println("Encrypted Text: " + encryptedText);
        System.out.println("Decrypted Text: " + decryptedText);

        if (!originalText.equals(decryptedText)) {
            throw new AssertionError("Decrypted text does not match the original text!");
        } else {
            System.out.println("Encryption and Decryption are working correctly.");
        }
    }
}