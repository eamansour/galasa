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
 * JNA structure representing a SecKeychainAttribute.
 * This corresponds to the SecKeychainAttribute structure in the macOS Security Framework.
 */
public class SecKeychainAttribute extends Structure {
    public int tag;        // The attribute tag
    public int length;     // The length of the data
    public Pointer data;   // Pointer to the attribute data
    
    public SecKeychainAttribute() {
        super();
    }
    
    public SecKeychainAttribute(Pointer p) {
        super(p);
        read();
    }
    
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("tag", "length", "data");
    }
}

