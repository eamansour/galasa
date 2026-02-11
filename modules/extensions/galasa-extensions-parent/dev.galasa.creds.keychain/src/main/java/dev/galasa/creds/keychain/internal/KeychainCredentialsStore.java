/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.keychain.internal;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * macOS Keychain implementation of the Galasa Credentials Store.
 *
 * This store retrieves credentials from the macOS Keychain using the
 * Security Framework via JNA. It supports username and username/password
 * credentials stored as generic passwords in the keychain.
 *
 * Credentials are stored in the keychain with:
 * - Service name: The credential ID (used directly without prefix)
 * - Account name: The username
 * - Password: The password (if applicable)
 */
public class KeychainCredentialsStore implements ICredentialsStore {
    
    private static final Log logger = LogFactory.getLog(KeychainCredentialsStore.class);
    
    private final IFramework framework;
    private final SecurityFramework security;
    
    /**
     * Constructor for KeychainCredentialsStore.
     * 
     * @param framework The Galasa framework instance
     * @throws CredentialsException If initialization fails
     */
    public KeychainCredentialsStore(IFramework framework) throws CredentialsException {
        this.framework = framework;
        
        try {
            this.security = SecurityFramework.INSTANCE;
            logger.info("macOS Keychain Credentials Store initialized");
        } catch (UnsatisfiedLinkError e) {
            throw new CredentialsException(
                "Failed to load macOS Security Framework. This store only works on macOS.", e);
        }
    }
    
    /**
     * Package-private constructor for testing.
     */
    KeychainCredentialsStore(IFramework framework, SecurityFramework security) {
        this.framework = framework;
        this.security = security;
    }
    
    @Override
    public ICredentials getCredentials(String credentialsId) throws CredentialsException {
        if (credentialsId == null || credentialsId.trim().isEmpty()) {
            throw new CredentialsException("Credentials ID cannot be null or empty");
        }
        
        // Use the credentials ID directly as the service name (no prefix)
        String serviceName = credentialsId;
        
        logger.debug("Attempting to retrieve credentials for ID: " + credentialsId);
        
        // Try to find the generic password in the keychain
        IntByReference passwordLength = new IntByReference();
        PointerByReference passwordData = new PointerByReference();
        PointerByReference itemRef = new PointerByReference();
        
        // First, try to get the password without specifying an account name
        // This will find the first matching service
        int result = security.SecKeychainFindGenericPassword(
            null,  // Search default keychain
            serviceName.length(),
            serviceName,
            0,  // No account name specified
            null,
            passwordLength,
            passwordData,
            itemRef
        );
        
        if (result == SecurityFramework.errSecItemNotFound) {
            logger.debug("Credentials not found in keychain for ID: " + credentialsId);
            return null;
        }
        
        if (result != SecurityFramework.errSecSuccess) {
            throw new CredentialsException(
                "Failed to retrieve credentials from keychain for ID: " + credentialsId + 
                ". Error code: " + result);
        }
        
        try {
            // Extract the password
            String password = extractPassword(passwordData.getValue(), passwordLength.getValue());
            
            // Try to get the username from the keychain item using the service name
            String username = extractUsername(serviceName);
            
            // Register the password as confidential text
            if (password != null && framework.getConfidentialTextService() != null) {
                framework.getConfidentialTextService().registerText(
                    password, 
                    "Password for credentials ID: " + credentialsId
                );
            }
            
            // Return appropriate credentials object
            if (username != null && !username.isEmpty()) {
                if (password != null && !password.isEmpty()) {
                    logger.debug("Retrieved username/password credentials for ID: " + credentialsId);
                    return new CredentialsUsernamePassword(null, username, password);
                } else {
                    logger.debug("Retrieved username-only credentials for ID: " + credentialsId);
                    return new CredentialsUsername(null, username);
                }
            }
            
            logger.warn("Retrieved credentials but no username found for ID: " + credentialsId);
            return null;
            
        } finally {
            // Always free the allocated memory
            if (passwordData.getValue() != null) {
                security.SecKeychainItemFreeContent(null, passwordData.getValue());
            }
        }
    }
    
    /**
     * Extracts the password from the keychain data.
     */
    private String extractPassword(Pointer passwordPointer, int length) {
        if (passwordPointer == null || length <= 0) {
            return null;
        }
        
        byte[] passwordBytes = passwordPointer.getByteArray(0, length);
        return new String(passwordBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Extracts the username (account name) from the keychain item using the security command.
     * This is a fallback method that uses the macOS security command-line tool.
     */
    private String extractUsername(String serviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "security",
                "find-generic-password",
                "-s", serviceName,
                "-g"  // Display the password (we'll ignore it) and account name
            );
            
            // Redirect error stream to capture account name (it's in stderr)
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read the output
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    // Look for the account line: "acct"<blob>="username"
                    if (line.contains("\"acct\"<blob>=")) {
                        int start = line.indexOf("=\"") + 2;
                        int end = line.lastIndexOf("\"");
                        if (start > 1 && end > start) {
                            String username = line.substring(start, end);
                            logger.debug("Extracted username from security command: " + username);
                            return username;
                        }
                    }
                }
            }
            
            process.waitFor();
            logger.warn("Could not extract username from security command output");
            return null;
            
        } catch (Exception e) {
            logger.warn("Failed to extract username using security command: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        // Note: Enumerating all keychain items is complex and requires additional APIs
        // For now, return an empty map as this is typically not required for read-only access
        logger.warn("getAllCredentials() is not fully implemented for macOS Keychain store");
        return new HashMap<>();
    }
    
    @Override
    public void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException {
        throw new UnsupportedOperationException(
            "Setting credentials is not supported by the macOS Keychain store. " +
            "Please use the Keychain Access application or 'security' command-line tool to add credentials."
        );
    }
    
    @Override
    public void deleteCredentials(String credentialsId) throws CredentialsException {
        throw new UnsupportedOperationException(
            "Deleting credentials is not supported by the macOS Keychain store. " +
            "Please use the Keychain Access application or 'security' command-line tool to delete credentials."
        );
    }
    
    @Override
    public void shutdown() throws CredentialsException {
        logger.info("macOS Keychain Credentials Store shutting down");
        // No cleanup required for keychain access
    }
}
