import React, { useState } from 'react';
import {
    Box,
    Button,
    Card,
    CardContent,
    Container,
    Divider,
    Grid,
    IconButton,
    Paper,
    Stack,
    Tab,
    Tabs,
    TextField,
    Typography,
    useTheme,
} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import IntegrationInstructionsIcon from '@mui/icons-material/IntegrationInstructions';
import TerminalIcon from '@mui/icons-material/Terminal';
import GitHubIcon from '@mui/icons-material/GitHub';
import { useSnackbar } from 'notistack';
import { motion } from 'framer-motion';

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
        </div>
    );
}

const CIIntegrationPage = () => {
    const theme = useTheme();
    const { enqueueSnackbar } = useSnackbar();
    const [tabValue, setTabValue] = useState(0);

    // Hardcoded for MVP demonstration
    const apiKey = "ms-ci-secret-key-123";
    const apiUrl = "http://localhost:8083/api/ci/scan";

    const handleCopy = (text) => {
        navigator.clipboard.writeText(text);
        enqueueSnackbar('Copied to clipboard!', { variant: 'success' });
    };

    const handleChange = (event, newValue) => {
        setTabValue(newValue);
    };

    const curlSnippet = `curl -X POST "${apiUrl}" \\
  -H "X-API-KEY: ${apiKey}" \\
  -F "file=@app-release.apk"`;

    const githubActionSnippet = `name: Security Scan
on: [push]
jobs:
  mobile-sec-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build APK
        run: ./gradlew assembleRelease
      - name: Scan with MobileSec-MS
        run: |
          response=$(curl -s -X POST "${apiUrl}" \\
            -H "X-API-KEY: \${{ secrets.MOBILESEC_API_KEY }}" \\
            -F "file=@app/build/outputs/apk/release/app-release.apk")
          echo "Scan Response: $response"
          if [[ $response == *"passed":false* ]]; then
            echo "Security Scan Failed!"
            exit 1
          fi`;

    const jenkinsSnippet = `pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './gradlew assembleRelease'
            }
        }
        stage('Security Scan') {
            steps {
                script {
                    def response = sh(script: """
                        curl -s -X POST "${apiUrl}" \\
                        -H "X-API-KEY: ${apiKey}" \\
                        -F "file=@app/build/outputs/apk/release/app-release.apk"
                    """, returnStdout: true).trim()
                    
                    echo "Response: \${response}"
                    
                    if (response.contains('"passed":false')) {
                        error("Security Scan Failed! High risks detected.")
                    }
                }
            }
        }
    }
}`;

    return (
        <Container maxWidth="lg">
            <Stack spacing={4}>
                <Stack direction="row" spacing={2} alignItems="center">
                    <IntegrationInstructionsIcon fontSize="large" color="primary" />
                    <Typography variant="h4" fontWeight={700}>
                        CI/CD Integration
                    </Typography>
                </Stack>

                <Typography variant="body1" color="text.secondary">
                    Integrate MobileSec-MS directly into your build pipeline to automatically scan APKs for vulnerabilities.
                    Use the API Key below to authenticate your requests.
                </Typography>

                {/* API Key Section */}
                <Card elevation={0} sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
                    <CardContent>
                        <Typography variant="h6" gutterBottom fontWeight={600}>
                            Your API Key
                        </Typography>
                        <Stack direction="row" spacing={2} alignItems="center">
                            <TextField
                                fullWidth
                                value={apiKey}
                                InputProps={{
                                    readOnly: true,
                                    sx: { fontFamily: 'monospace', bgcolor: 'grey.50' }
                                }}
                            />
                            <Button
                                variant="contained"
                                startIcon={<ContentCopyIcon />}
                                onClick={() => handleCopy(apiKey)}
                                sx={{ minWidth: 120, height: 56 }}
                            >
                                Copy
                            </Button>
                        </Stack>
                        <Typography variant="caption" color="warning.main" sx={{ mt: 1, display: 'block' }}>
                            ⚠️ Keep this key secret. Do not commit it to version control.
                        </Typography>
                    </CardContent>
                </Card>

                {/* Integration Snippets */}
                <Paper elevation={0} sx={{ borderRadius: 4, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
                    <Box sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'grey.50' }}>
                        <Tabs value={tabValue} onChange={handleChange} aria-label="integration tabs">
                            <Tab icon={<TerminalIcon />} iconPosition="start" label="cURL" />
                            <Tab icon={<GitHubIcon />} iconPosition="start" label="GitHub Actions" />
                            <Tab icon={<IntegrationInstructionsIcon />} iconPosition="start" label="Jenkins" />
                        </Tabs>
                    </Box>

                    <TabPanel value={tabValue} index={0}>
                        <Box sx={{ position: 'relative' }}>
                            <IconButton
                                sx={{ position: 'absolute', top: 0, right: 0, color: 'white' }}
                                onClick={() => handleCopy(curlSnippet)}
                            >
                                <ContentCopyIcon />
                            </IconButton>
                            <Box
                                component="pre"
                                sx={{
                                    p: 3,
                                    borderRadius: 2,
                                    bgcolor: '#1e1e1e',
                                    color: '#d4d4d4',
                                    overflowX: 'auto',
                                    fontFamily: 'monospace',
                                    m: 0
                                }}
                            >
                                {curlSnippet}
                            </Box>
                        </Box>
                    </TabPanel>

                    <TabPanel value={tabValue} index={1}>
                        <Box sx={{ position: 'relative' }}>
                            <IconButton
                                sx={{ position: 'absolute', top: 0, right: 0, color: 'white' }}
                                onClick={() => handleCopy(githubActionSnippet)}
                            >
                                <ContentCopyIcon />
                            </IconButton>
                            <Box
                                component="pre"
                                sx={{
                                    p: 3,
                                    borderRadius: 2,
                                    bgcolor: '#1e1e1e',
                                    color: '#d4d4d4',
                                    overflowX: 'auto',
                                    fontFamily: 'monospace',
                                    m: 0
                                }}
                            >
                                {githubActionSnippet}
                            </Box>
                        </Box>
                    </TabPanel>

                    <TabPanel value={tabValue} index={2}>
                        <Box sx={{ position: 'relative' }}>
                            <IconButton
                                sx={{ position: 'absolute', top: 0, right: 0, color: 'white' }}
                                onClick={() => handleCopy(jenkinsSnippet)}
                            >
                                <ContentCopyIcon />
                            </IconButton>
                            <Box
                                component="pre"
                                sx={{
                                    p: 3,
                                    borderRadius: 2,
                                    bgcolor: '#1e1e1e',
                                    color: '#d4d4d4',
                                    overflowX: 'auto',
                                    fontFamily: 'monospace',
                                    m: 0
                                }}
                            >
                                {jenkinsSnippet}
                            </Box>
                        </Box>
                    </TabPanel>
                </Paper>
            </Stack>
        </Container>
    );
};

export default CIIntegrationPage;
