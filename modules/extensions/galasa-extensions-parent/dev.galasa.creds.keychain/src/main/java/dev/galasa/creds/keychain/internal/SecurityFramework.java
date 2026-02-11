/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.keychain.internal;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * JNA interface to macOS Security Framework for Keychain access.
 * 
 * This interface provides access to the macOS Keychain Services API
 * for retrieving credentials stored in the system keychain.
 */
public interface SecurityFramework extends Library {
    
    SecurityFramework INSTANCE = Native.load("Security", SecurityFramework.class);
    
    // Error codes
    int errSecSuccess = 0;
    int errSecItemNotFound = -25300;
    int errSecAuthFailed = -25293;
    
    /**
     * Finds a generic password in the keychain.
     * 
     * @param keychainOrArray A reference to an array of keychains to search, or NULL to search the default keychain search list
     * @param serviceNameLength The length of the service name
     * @param serviceName The service name
     * @param accountNameLength The length of the account name
     * @param accountName The account name
     * @param passwordLength On return, the length of the password
     * @param passwordData On return, a pointer to the password data
     * @param itemRef On return, a reference to the keychain item
     * @return A result code (errSecSuccess if successful)
     */
    int SecKeychainFindGenericPassword(
        Pointer keychainOrArray,
        int serviceNameLength,
        String serviceName,
        int accountNameLength,
        String accountName,
        IntByReference passwordLength,
        PointerByReference passwordData,
        PointerByReference itemRef
    );
    
    /**
     * Finds an internet password in the keychain.
     * 
     * @param keychainOrArray A reference to an array of keychains to search, or NULL to search the default keychain search list
     * @param serverNameLength The length of the server name
     * @param serverName The server name
     * @param securityDomainLength The length of the security domain
     * @param securityDomain The security domain
     * @param accountNameLength The length of the account name
     * @param accountName The account name
     * @param pathLength The length of the path
     * @param path The path
     * @param port The port number
     * @param protocol The protocol type
     * @param authenticationType The authentication type
     * @param passwordLength On return, the length of the password
     * @param passwordData On return, a pointer to the password data
     * @param itemRef On return, a reference to the keychain item
     * @return A result code (errSecSuccess if successful)
     */
    int SecKeychainFindInternetPassword(
        Pointer keychainOrArray,
        int serverNameLength,
        String serverName,
        int securityDomainLength,
        String securityDomain,
        int accountNameLength,
        String accountName,
        int pathLength,
        String path,
        short port,
        int protocol,
        int authenticationType,
        IntByReference passwordLength,
        PointerByReference passwordData,
        PointerByReference itemRef
    );
    
    /**
     * Copies a single attribute from a keychain item.
     *
     * @param itemRef The keychain item
     * @param tag The attribute tag (e.g., kSecAccountItemAttr)
     * @param format On return, the format of the attribute
     * @param length On return, the length of the attribute data
     * @param outData On return, a pointer to the attribute data
     * @return A result code (errSecSuccess if successful)
     */
    int SecKeychainItemCopyAttributesAndData(
        Pointer itemRef,
        Pointer info,
        IntByReference itemClass,
        PointerByReference attrList,
        IntByReference length,
        PointerByReference outData
    );
    
    /**
     * Frees an attribute list and optionally the data.
     *
     * @param attrList The attribute list to free
     * @param data The data to free (can be null)
     * @return A result code (errSecSuccess if successful)
     */
    int SecKeychainItemFreeAttributesAndData(
        Pointer attrList,
        Pointer data
    );
    
    // Attribute tags
    int kSecAccountItemAttr = 1633903476; // 'acct' - account name attribute
    int kSecServiceItemAttr = 1937138533; // 'svce' - service name attribute
    
    /**
     * Releases memory allocated by Keychain Services.
     *
     * @param attributes The attributes to free
     * @param data A pointer to the data to be released
     * @return A result code (errSecSuccess if successful)
     */
    int SecKeychainItemFreeContent(
        Pointer attributes,
        Pointer data
    );
    
    /**
     * Copies an attribute from a keychain item.
     * 
     * @param itemRef The keychain item
     * @param info The attribute info
     * @param itemClass On return, the item class
     * @param attrList On return, the attribute list
     * @param length On return, the length of the data
     * @param outData On return, a pointer to the data
     * @return A result code (errSecSuccess if successful)
     */
    int SecKeychainItemCopyContent(
        Pointer itemRef,
        Pointer info,
        IntByReference itemClass,
        Pointer attrList,
        IntByReference length,
        PointerByReference outData
    );
}
