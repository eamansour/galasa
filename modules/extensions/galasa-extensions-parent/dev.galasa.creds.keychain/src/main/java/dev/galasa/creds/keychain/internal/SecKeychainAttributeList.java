/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.keychain.internal;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA structure representing a SecKeychainAttributeList.
 * This corresponds to the SecKeychainAttributeList structure in the macOS Security Framework.
 */
public class SecKeychainAttributeList extends Structure {
    public int count;                           // Number of attributes
    public Pointer attr;                        // Pointer to array of attributes
    
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("count", "attr");
    }
    
    /**
     * Gets the attributes as an array.
     */
    public SecKeychainAttribute[] getAttributes() {
        if (attr == null || count == 0) {
            return new SecKeychainAttribute[0];
        }
        SecKeychainAttribute attribute = new SecKeychainAttribute(attr);
        return (SecKeychainAttribute[]) attribute.toArray(count);
    }
    
    public SecKeychainAttributeList() {
        super();
    }
    
    public SecKeychainAttributeList(Pointer p) {
        super(p);
        read();
    }
}
