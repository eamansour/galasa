# Galasa macOS Keychain Credentials Store

This module provides a Galasa credentials store implementation that retrieves credentials from the macOS Keychain using the Security Framework via Java Native Access (JNA).

## Overview

The macOS Keychain Credentials Store allows Galasa tests to securely access credentials stored in the macOS Keychain without hardcoding sensitive information in test code or configuration files.

## Requirements

- **macOS** operating system (10.12 Sierra or later recommended)
- Credentials must be stored in the macOS Keychain


## Configuration

To use the macOS Keychain credentials store, configure your Galasa bootstrap properties:

### bootstrap.properties

```properties
framework.credentials.store=keychain
```

The `keychain` value tells Galasa to use the macOS Keychain credentials store.

## Storing Credentials in macOS Keychain

Credentials must be stored in the macOS Keychain as generic password items.

### Using Keychain Access Application

1. Open **Keychain Access** application
2. Select **File > New Password Item**
3. Enter:
   - **Keychain Item Name**: Your credentials ID (e.g., `SIMBANK`)
   - **Account Name**: The username
   - **Password**: The password/access token
4. Click **Add**

### Using the `security` Command-Line Tool

```bash
# Add a generic password to the keychain
security add-generic-password \
  -s "MYACCOUNT" \
  -a "MYUSER" \
  -w "MYPASSWORD" \
  -U

# Verify the credential was added
security find-generic-password -s "MYACCOUNT"
```

**Parameters:**
- `-s`: Service name (your credentials ID)
- `-a`: Account name (username)
- `-w`: Password
- `-U`: Update if exists

## Usage in Galasa Tests

Once credentials are stored in the Keychain and the store is configured, you can retrieve them in your tests as normal credentials:

```java
import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.CoreManager;

@Test
public class MyTest {
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Test
    public void testWithKeychainCredentials() throws Exception {
        // Retrieve credentials from Keychain
        ICredentials creds = coreManager.getCredentials("SIMBANK");
        
        if (creds instanceof ICredentialsUsernamePassword) {
            ICredentialsUsernamePassword userPass = (ICredentialsUsernamePassword) creds;
            String username = userPass.getUsername();
            String password = userPass.getPassword();
            
            // Use credentials in your test
            // Password is automatically masked in logs
        }
    }
}
```

## Troubleshooting

### Credentials Not Found

If you get a "credentials not found" error:

1. Verify the credential exists in Keychain:
   ```bash
   security find-generic-password -s "{credentialsId}"
   ```

2. Check the service name matches your credentials ID exactly

3. Ensure the Keychain is unlocked

### Permission Denied

If you get permission errors:

1. Check Keychain Access settings
2. Ensure your application has permission to access the keychain
3. You may need to approve access in System Preferences > Security & Privacy

### Not Running on macOS

This store only works on macOS. If you try to use it on other operating systems, you'll get an error:

```
macOS Keychain credentials store can only be used on macOS
```

Use a different credentials store (file-based or etcd) for other platforms.


## Architecture

### Components

1. **SecurityFramework** - JNA interface to macOS Security Framework
2. **KeychainCredentialsStore** - Implementation of `ICredentialsStore`
3. **KeychainCredentialsStoreRegistration** - OSGi component registration

### Flow

1. Framework initialization reads `framework.credentials.store=keychain`
2. `KeychainCredentialsStoreRegistration` detects the keychain store URI
3. `KeychainCredentialsStore` is instantiated and registered
4. Tests call `getCredentials(id)` which:
   - Uses the credentials ID directly as the service name
   - Calls `SecKeychainFindGenericPassword` via JNA
   - Extracts username and password
   - Returns appropriate `ICredentials` implementation

## Related Documentation

- [macOS Security Framework](https://developer.apple.com/documentation/security)
- [JNA Documentation](https://github.com/java-native-access/jna)