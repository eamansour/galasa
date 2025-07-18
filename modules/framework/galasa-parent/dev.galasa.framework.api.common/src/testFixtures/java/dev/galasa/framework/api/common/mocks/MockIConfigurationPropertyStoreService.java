/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockIConfigurationPropertyStoreService implements IConfigurationPropertyStoreService{

    protected String namespaceInput;
    protected Map<String, String> properties = new HashMap<String,String>();
    private boolean throwError = false;
    private Set<String> keysThrowOnDelete = new HashSet<>();

    public MockIConfigurationPropertyStoreService() {
        this("framework");
    }

    public MockIConfigurationPropertyStoreService(@NotNull String namespace) {
        this.namespaceInput = namespace;
        if (this.namespaceInput.equals("multi")){
            this.properties.put("multi.test.property", "value1");
            this.properties.put("multi.example.charity1", "value2");
            this.properties.put("multi.example.lecture101", "value101");
            this.properties.put("multi.example.hospitality", "value3");
            this.properties.put("multi.test.aunty5", "value4");
            this.properties.put("multi.test.empty", "value5");
        }else if (this.namespaceInput.equals("infixes")){
            this.properties.put(namespace+".test.aproperty.stream", "value1");
            this.properties.put(namespace+".test.bproperty.stream", "value2");
            this.properties.put(namespace+".test.property.testing.local.stream", "value3");
            this.properties.put(namespace+".test.property.testing.stream", "value4");
            this.properties.put(namespace+".test.stream", "value5");
        }else if (this.namespaceInput.equals("empty")){
            //add no properties
        } else {
            this.properties.put(namespace+".property.1", "value1");
            this.properties.put(namespace+".property.2", "value2");
            this.properties.put(namespace+".property.3", "value3");
            this.properties.put(namespace+".property.4", "value4");
            this.properties.put(namespace+".property.5", "value5");
        }
	}

    public String getNamespaceInput(){
        return this.namespaceInput;
    }

    @Override
    public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
            checkThrowError();
            for (Map.Entry<String,String> property : properties.entrySet()){
                String key = property.getKey().substring(property.getKey().indexOf(".")+1);
                String match = prefix+"."+suffix;
                if (key.equals(match)){
                    return property.getValue();
                }
            }
            return null;
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        checkThrowError();
                
        Map<String, String> fetchedProperties = new HashMap<>();

        for(Map.Entry<String, String> property : properties.entrySet()) {
            if(property.getKey().startsWith(prefix)){
                fetchedProperties.put(property.getKey(), property.getValue());
            }
        }

        return fetchedProperties;

    }

    @Override
    public void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException {
        checkThrowError();
       this.properties.put(name,value);
    }

    /**
     * This method simulates a deletion failure for a given key.
     *
     * @param key         The property key to simulate a failure on.
     * @param shouldThrow If true, deletion of the key will throw an exception.
     */
    public void setThrowOnDeleteForKey(String key, boolean shouldThrow) {
        if (shouldThrow) {
            keysThrowOnDelete.add(key);
        } else {
            keysThrowOnDelete.remove(key);
        }
    }

    @Override
    public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
        checkThrowError();
        if (keysThrowOnDelete.contains(name)) {
            throw new ConfigurationPropertyStoreException("Simulated deletion failure for key: " + name);
        }
        this.properties.remove(namespaceInput+"."+name);
    }

    @Override
    public Map<String, String> getAllProperties() {
        return this.properties;
    }

    @Override
    public String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        
        throw new UnsupportedOperationException("Unimplemented method 'reportPropertyVariants'");
    }

    @Override
    public String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes) {
        
        throw new UnsupportedOperationException("Unimplemented method 'reportPropertyVariantsString'");
    }

    @Override
    public List<String> getCPSNamespaces() {
        ArrayList<String> namespaces = new ArrayList<String>();
        for( Map.Entry<String,String> entry : properties.entrySet() ) {
            String[] parts = entry.getKey().split("[.]");
            String ns = parts[0];
            namespaces.add(ns);
        }
        return namespaces;
    }

    public void setThrowError(boolean throwError) {
        this.throwError = throwError;
    }


    private void checkThrowError() throws ConfigurationPropertyStoreException {
        if (throwError) {
            throw new ConfigurationPropertyStoreException("Simulating a CPS failure!");
        }
    }

    @Override
    public void deletePrefixedProperties(@NotNull String prefix) throws ConfigurationPropertyStoreException {

        Map<String, String> propertiesToRemove = getPrefixedProperties(prefix);
        for(Map.Entry<String, String> property : propertiesToRemove.entrySet()) {
            this.properties.remove(property.getKey());
        }

    }

    @Override
    public void setProperties(Map<String, String> propertiesToSet) throws ConfigurationPropertyStoreException {
        this.properties.putAll(propertiesToSet);
    }
}
