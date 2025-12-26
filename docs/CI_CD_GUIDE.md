# MobileSec-MS CI/CD Integration Guide

This guide explains how to integrate MobileSec-MS security scanning into your automated build pipelines (GitHub Actions, Jenkins, GitLab CI).

## Prerequisites

1.  **MobileSec-MS Running**: Ensure the platform is deployed and accessible from your CI runner.
2.  **API Key**: Obtain your API Key from the **CI/CD Integration** page in the frontend.

## API Endpoint

- **URL**: `POST http://<gateway-host>:8083/api/ci/scan`
- **Headers**: `X-API-KEY: <your-api-key>`
- **Body**: `multipart/form-data` with `file=@path/to/app.apk`

## Integration Examples

### 1. cURL (Shell Script)

Use this snippet in any shell-based CI environment.

```bash
#!/bin/bash

API_URL="http://localhost:8083/api/ci/scan"
API_KEY="ms-ci-secret-key-123"
APK_PATH="./app/build/outputs/apk/release/app-release.apk"

echo "Uploading APK for Security Scan..."
RESPONSE=$(curl -s -X POST "$API_URL" \
  -H "X-API-KEY: $API_KEY" \
  -F "file=@$APK_PATH")

echo "Scan Response: $RESPONSE"

# Check if passed
if [[ $RESPONSE == *"passed":false* ]]; then
  echo "❌ Security Scan Failed! High risks detected."
  exit 1
else
  echo "✅ Security Scan Passed."
fi
```

### 2. GitHub Actions

Add this step to your `.github/workflows/main.yml`.

```yaml
- name: MobileSec Security Scan
  run: |
    response=$(curl -s -X POST "http://mobilesec-gateway:8083/api/ci/scan" \
      -H "X-API-KEY: ${{ secrets.MOBILESEC_API_KEY }}" \
      -F "file=@app/release/app.apk")
    
    if [[ $response == *"passed":false* ]]; then
      echo "Security Scan Failed"
      exit 1
    fi
```

### 3. Jenkins

Add this stage to your `Jenkinsfile`.

```groovy
stage('Security Scan') {
    steps {
        script {
            def response = sh(script: """
                curl -s -X POST "http://mobilesec-gateway:8083/api/ci/scan" \
                -H "X-API-KEY: ${MOBILESEC_API_KEY}" \
                -F "file=@app-release.apk"
            """, returnStdout: true).trim()
            
            if (response.contains('"passed":false')) {
                error("Security Scan Failed")
            }
        }
    }
}
```
