/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.keychain.internal;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

/**
 * OSGI component that registers the macOS Keychain Credentials Store.
 * 
 * This registration class checks if the credentials store URI scheme is "keychain"
 * and if so, registers the KeychainCredentialsStore with the framework.
 * 
 * To use this store, set the framework.credentials.store property to:
 * keychain:
 * 
 * Example in bootstrap.properties:
 * framework.credentials.store=keychain:
 */
@Component(service = { ICredentialsStoreRegistration.class })
public class KeychainCredentialsStoreRegistration implements ICredentialsStoreRegistration {
    
    private static final Log logger = LogFactory.getLog(KeychainCredentialsStoreRegistration.class);
    
    /**
     * Initializes and registers the macOS Keychain Credentials Store if applicable.
     * 
     * This method checks if:
     * 1. The credentials store URI scheme is "keychain"
     * 2. The current OS is macOS
     * 
     * If both conditions are met, it registers the KeychainCredentialsStore.
     * 
     * @param frameworkInitialisation The framework initialization instance
     * @throws CredentialsException If registration fails
     */
    @Override
    public void initialise(IFrameworkInitialisation frameworkInitialisation) throws CredentialsException {
        URI credentialsUri = frameworkInitialisation.getCredentialsStoreUri();
        
        if (!isKeychainUri(credentialsUri)) {
            logger.debug("Credentials store URI is not 'keychain:', skipping macOS Keychain registration");
            return;
        }
        
        // Check if running on macOS
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (!osName.contains("mac")) {
            logger.warn("macOS Keychain credentials store requested but not running on macOS. OS: " + osName);
            throw new CredentialsException(
                "macOS Keychain credentials store can only be used on macOS. Current OS: " + osName
            );
        }
        
        try {
            logger.info("Registering macOS Keychain Credentials Store");
            KeychainCredentialsStore keychainStore = new KeychainCredentialsStore(
                frameworkInitialisation.getFramework()
            );
            frameworkInitialisation.registerCredentialsStore(keychainStore);
            logger.info("macOS Keychain Credentials Store registered successfully");
        } catch (Exception e) {
            throw new CredentialsException("Failed to initialize macOS Keychain Credentials Store", e);
        }
    }
    
    /**
     * Checks if the URI scheme is "keychain".
     * 
     * @param uri The credentials store URI
     * @return true if the scheme is "keychain", false otherwise
     */
    public static boolean isKeychainUri(URI uri) {
        return uri != null && "keychain".equals(uri.getScheme());
    }
}
