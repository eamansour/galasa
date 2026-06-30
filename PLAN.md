# Galasa OSGi Dependency Management Improvement Plan

## Overview
This plan addresses the manual transitive dependency management issues in Galasa's OSGi bundles and establishes automated dependency updates using Renovate/Dependabot.

---

## Phase 1: Audit and Document Current Wrapper Bundles
**Goal:** Understand what we have and identify quick wins

**Tasks:**
1. List all wrapper bundles in [`modules/wrapping/`](modules/wrapping/)
2. For each wrapper, document:
   - Primary dependency being wrapped
   - Number of transitive dependencies manually listed
   - Whether an OSGi-ready version exists in Maven Central
3. Identify wrappers that can be eliminated (already OSGi-ready)
4. Categorize by complexity (simple vs. complex dependency trees)

**Deliverable:** Markdown document with wrapper inventory and elimination candidates

---

## Phase 2: Simplify Wrapper Bundles (Remove Transitive Dependencies)
**Goal:** Let Maven Bundle Plugin handle transitive dependencies automatically

**Start with simplest wrappers first:**

### 2.1 Simplify `dev.galasa.wrapping.gson`
**Current state:** Manually includes `error_prone_annotations`

**New approach:**
```xml
<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
    </dependency>
    <!-- Remove error_prone_annotations - let BND handle it -->
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.felix</groupId>
            <artifactId>maven-bundle-plugin</artifactId>
            <configuration>
                <instructions>
                    <Bundle-SymbolicName>dev.galasa.wrapping.gson</Bundle-SymbolicName>
                    <!-- Only inline the main artifact -->
                    <Embed-Dependency>gson;inline=true</Embed-Dependency>
                    <!-- Auto-calculate imports, exclude gson packages -->
                    <Import-Package>!com.google.gson*,*;resolution:=optional</Import-Package>
                    <Export-Package>com.google.gson*</Export-Package>
                </instructions>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2.2 Simplify `dev.galasa.wrapping.httpclient5`
**Current state:** Manually includes httpcore5 and httpcore5-h2

**New approach:** Only declare the main dependency, let BND resolve imports

### 2.3 Tackle Complex Wrappers
For [`dev.galasa.wrapping.io.grpc.java`](modules/wrapping/dev.galasa.wrapping.io.grpc.java/pom.xml) and [`dev.galasa.wrapping.io.kubernetes.client-java`](modules/wrapping/dev.galasa.wrapping.io.kubernetes.client-java/pom.xml):
- Remove all transitive dependency declarations
- Use `<Import-Package>*;resolution:=optional</Import-Package>` to make imports optional
- Test thoroughly to ensure runtime resolution works

**Deliverable:** Updated wrapper POMs with simplified dependencies

---

## Phase 3: Consolidate Version Management in Gradle Platform
**Goal:** Single source of truth for all versions

### 3.1 Enhance `dev.galasa.platform/build.gradle`
- Ensure ALL dependency versions are defined here
- Remove version declarations from wrapper POMs
- Use `${project.parent.version}` for Galasa artifacts only

### 3.2 Update Wrapper Parent POM
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>dev.galasa</groupId>
            <artifactId>dev.galasa.platform</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 3.3 Remove Version Attributes from Dependencies
All wrapper POMs should reference dependencies without versions:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <!-- No version - comes from platform -->
</dependency>
```

**Deliverable:** All versions managed in platform, no version duplication

---

## Phase 4: Create Shared OSGi Bundles for Common Dependencies
**Goal:** Stop embedding, start sharing

### 4.1 Identify Commonly Used Dependencies
From analysis, these appear in multiple bundles:
- slf4j-api
- commons-io
- commons-lang3
- guava
- jackson-databind

### 4.2 Decision Matrix
For each common dependency:
- **If OSGi-ready exists:** Use it directly, don't wrap
- **If not OSGi-ready:** Create ONE shared wrapper bundle
- **Update consumers:** Change from Embed to Import-Package

### 4.3 Update Consumer Bundles
Instead of embedding, import:
```xml
<Import-Package>
    org.slf4j,
    org.apache.commons.io,
    *
</Import-Package>
```

**Deliverable:** Shared dependency bundles, reduced duplication

---

## Phase 5: Update bnd.bnd Files to Use Auto-Generation
**Goal:** Let BND analyze code and generate manifests

### 5.1 Simplify bnd.bnd Files
**Current:** [`dev.galasa.framework.docker.controller/bnd.bnd`](modules/framework/galasa-parent/dev.galasa.framework.docker.controller/bnd.bnd) manually lists 15+ JARs

**New approach:**
```
Bundle-Name: dev.galasa.framework.docker.controller
Export-Package: dev.galasa.framework.docker.controller*
Import-Package: *;resolution:=optional
-conditionalpackage: com.github.dockerjava.*,org.apache.hc.*
```

### 5.2 Use Conditional Packages
Instead of `-includeresource`, use `-conditionalpackage` to include only referenced classes

**Deliverable:** Simplified bnd.bnd files using BND's analysis

---

## Phase 6: Automate release.yaml Generation
**Goal:** Eliminate manual synchronization

### 6.1 Create Gradle Task
```gradle
task generateReleaseYaml {
    doLast {
        def platform = project(':dev.galasa.platform')
        def yaml = new File('modules/obr/release.yaml')
        
        // Generate YAML from platform constraints
        // Include version, obr, mvp, isolated flags
    }
}
```

### 6.2 Integrate into Build
- Run during build process
- Validate generated YAML matches expected format
- Update CI/CD to use generated file

**Deliverable:** Automated release.yaml generation from platform

---

## Phase 7: Configure Renovate/Dependabot for Automated Updates
**Goal:** Automated dependency updates

### 7.1 Create `.github/renovate.json`
```json
{
  "extends": ["config:base"],
  "packageRules": [
    {
      "matchManagers": ["gradle"],
      "matchFiles": ["modules/platform/dev.galasa.platform/build.gradle"],
      "groupName": "Galasa Platform Dependencies",
      "schedule": ["before 3am on Monday"]
    },
    {
      "matchManagers": ["maven"],
      "matchFiles": ["modules/wrapping/**/pom.xml"],
      "enabled": false,
      "description": "Versions managed by platform"
    }
  ],
  "gradle": {
    "enabled": true
  },
  "maven": {
    "enabled": true
  }
}
```

### 7.2 Configure Dependabot (Alternative)
```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/modules/platform/dev.galasa.platform"
    schedule:
      interval: "weekly"
    groups:
      platform-dependencies:
        patterns:
          - "*"
```

### 7.3 Test Automation
- Create test PR with Renovate
- Verify build passes
- Validate release.yaml regenerates correctly

**Deliverable:** Working automated dependency updates

---

## Phase 8: Test and Validate the New Approach
**Goal:** Ensure everything works

### 8.1 Unit Testing
- Build all wrapper bundles
- Verify OSGi metadata is correct
- Check bundle sizes (should be smaller)

### 8.2 Integration Testing
- Deploy to test environment
- Run existing test suites
- Verify no ClassNotFoundException or NoClassDefFoundError

### 8.3 Performance Testing
- Compare bundle resolution times
- Measure memory usage
- Validate startup times

### 8.4 Documentation
- Update developer documentation
- Create migration guide
- Document new dependency addition process

**Deliverable:** Validated, tested, documented solution

---

## Success Metrics

1. **Reduced Maintenance:** No manual transitive dependency tracking
2. **Smaller Bundles:** 50%+ reduction in wrapper bundle sizes
3. **Automated Updates:** Renovate/Dependabot PRs working
4. **Single Source of Truth:** All versions in platform only
5. **Faster Builds:** Reduced dependency resolution time

## Risk Mitigation

- **Incremental rollout:** Start with simple wrappers, validate before complex ones
- **Parallel testing:** Keep old wrappers until new ones validated
- **Rollback plan:** Git branches for easy reversion
- **Communication:** Update team on changes and new processes