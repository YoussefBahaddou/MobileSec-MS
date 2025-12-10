# MobileSec-MS Microservices Architecture

Based on the project specifications, here is the detailed breakdown of the 7 microservices, their technologies, roles, and functions.

## 1. APKScanner
[cite_start]**Role:** Disassembles and analyzes APK files to extract the manifest, permissions, and endpoints[cite: 16].
**Technologies:**
* [cite_start]Python (Androguard, Apktool) 
* [cite_start]**Database:** SQLite (explicitly mentioned for metadata storage) 
**Function:**
* [cite_start]Identifies exported components[cite: 18].
* [cite_start]Detects dangerous permissions[cite: 18].
* [cite_start]Checks for flags like `debuggable`, `allowBackup`, and `cleartextTrafficPermitted`[cite: 18].

## 2. SecretHunter
[cite_start]**Role:** Searches for exposed secrets within the source code or application resources[cite: 20].
**Technologies:**
* [cite_start]GitLeaks [cite: 21]
* [cite_start]Custom Regex (Regular Expressions) [cite: 21]
* [cite_start]YARA Rules [cite: 21]
**Function:**
* [cite_start]Detects API keys and OAuth tokens[cite: 22].
* [cite_start]Finds hardcoded passwords and other sensitive data[cite: 22].

## 3. CryptoCheck
[cite_start]**Role:** Verifies the correct usage of cryptographic APIs[cite: 24].
**Technologies:**
* [cite_start]SAST (Static Application Security Testing) for Java/Kotlin [cite: 25]
* [cite_start]Codified rules based on CWE (Common Weakness Enumeration) [cite: 25]
**Function:**
* [cite_start]Detects bad usage of algorithms like AES/ECB[cite: 26].
* [cite_start]Identifies absence of padding or weaknesses in random number generation[cite: 26].
* [cite_start]Flags usage of weak hashing algorithms like MD5/SHA1[cite: 26].

## 4. NetworkInspector
[cite_start]**Role:** Intercepts network communications via a Man-in-the-Middle (MITM) proxy[cite: 28].
**Technologies:**
* [cite_start]mitmproxy [cite: 29]
* [cite_start]Docker sandbox for Android (AVD - Android Virtual Device) [cite: 29]
**Function:**
* [cite_start]Analyzes HTTPS/TLS configurations[cite: 30].
* [cite_start]Inspects HTTP headers and certificate management[cite: 30].
* [cite_start]Detects potential data leaks over the network[cite: 30].

## 5. ReportGen
[cite_start]**Role:** Aggregates results from other services and generates reports in multiple formats[cite: 32].
**Technologies:**
* [cite_start]Node.js [cite: 33]
* [cite_start]Puppeteer [cite: 33]
**Function:**
* [cite_start]Generates detailed sheets of detected flaws[cite: 34].
* [cite_start]Provides corrective recommendations[cite: 34].
* [cite_start]Outputs reports in PDF, JSON, and SARIF formats[cite: 32].

## 6. FixSuggest
[cite_start]**Role:** Provides suggestions for code fixes or configurations to apply[cite: 36].
**Technologies:**
* [cite_start]MASVS Rules Model converted to YAML [cite: 37]
* [cite_start]Patch generation logic [cite: 37]
**Function:**
* [cite_start]Proposes concrete actions (e.g., setting `android:exported="false"`)[cite: 38].
* [cite_start]Suggests activating Proguard or isolating `signingConfig`[cite: 38].

## 7. CIConnector
[cite_start]**Role:** Handles integration with CI/CD pipelines (GitHub Actions / GitLab CI)[cite: 40].
**Technologies:**
* [cite_start]Docker CLI [cite: 41]
* [cite_start]YAML Plugins [cite: 41]
**Function:**
* [cite_start]Enables automatic scanning activation at every build[cite: 42].
* [cite_start]Ensures fluid integration into the DevSecOps cycle[cite: 46].