package io.licensemanager.example;

import io.licensemanager.example.model.License;
import io.licensemanager.example.util.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String licenseFilePath = readLicenseFilePath();
        String licenseKey = readLicenseKey();
        byte[] licenseContent = new byte[0];
        try {
            licenseContent = Files.readAllBytes(Paths.get(licenseFilePath));
        } catch (IOException e) {
            System.out.println(String.format("Failed to read license file's content, reason - %s", e.getMessage()));
            return;
        }

        boolean isVerified = CryptoUtils.verifySign(licenseContent, licenseKey);
        if (!isVerified) {
            System.out.println("License content hasn't been verified successfully - " +
                    "content was changed by unauthorized person or wrong license key was passed");
            return;
        }

        String json = CryptoUtils.decryptLicenseFile(licenseContent);
        License license = License.parseJson(json);
        System.out.println(String.format("Read license: %s", license));
    }

    private static String readLicenseFilePath() {
        System.out.print("Please enter license file path: ");

        return scanner.nextLine();
    }

    private static String readLicenseKey() {
        System.out.print("Please enter license key: ");

        return scanner.nextLine();
    }

}
