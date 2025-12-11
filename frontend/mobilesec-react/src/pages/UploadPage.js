import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  IconButton,
  LinearProgress,
  Stack,
  Typography,
  useTheme,
  Paper,
  Chip,
  List,
  Alert,
  Stepper,
  Step,
  StepLabel,
  StepContent
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AndroidIcon from '@mui/icons-material/Android';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useSnackbar } from 'notistack';
import { motion, AnimatePresence } from 'framer-motion';
import { uploadApk, extractStrings, scanSecrets, scanCrypto } from '../services/api';

function UploadPage() {
  const { enqueueSnackbar } = useSnackbar();
  const theme = useTheme();
  const navigate = useNavigate();

  // State
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isScanning, setIsScanning] = useState(false);

  // Pipeline Results
  const [apkResult, setApkResult] = useState(null);
  const [secretResult, setSecretResult] = useState(null);
  const [cryptoResult, setCryptoResult] = useState(null);

  // Progress tracking: 0=Idle, 1=Manifest, 2=Strings, 3=Secrets/Crypto, 4=Done
  const [activeStep, setActiveStep] = useState(0);

  const resetState = useCallback(() => {
    setSelectedFile(null);
    setIsScanning(false);
    setActiveStep(0);
    setApkResult(null);
    setSecretResult(null);
    setCryptoResult(null);
  }, []);

  const handleFileSelect = useCallback((file) => {
    if (!file) return;
    if (!file.name.endsWith('.apk')) {
      enqueueSnackbar('Please select an APK file for automated analysis.', { variant: 'warning' });
      return;
    }
    setSelectedFile(file);
    resetData();
  }, [enqueueSnackbar]);

  const resetData = () => {
    setApkResult(null);
    setSecretResult(null);
    setCryptoResult(null);
    setActiveStep(0);
  }

  const handleDrop = useCallback((event) => {
    event.preventDefault();
    setIsDragging(false);
    const file = event.dataTransfer.files?.[0];
    handleFileSelect(file);
  }, [handleFileSelect]);

  const runAutoScan = async () => {
    if (!selectedFile) return;

    setIsScanning(true);
    setActiveStep(0);

    try {
      // Step 1: APK Manifest Analysis
      enqueueSnackbar('Starting APK Analysis...', { variant: 'info' });
      const manifestData = await uploadApk(selectedFile, (p) => { });
      setApkResult(manifestData);
      setActiveStep(1);

      // Step 2: Extract Strings for Content Analysis
      enqueueSnackbar('Extracting content for deep scan...', { variant: 'info' });
      const stringData = await extractStrings(selectedFile, (p) => { });
      const content = stringData.content || "";
      setActiveStep(2);

      // Step 3: Run Secret Hunter & Crypto Check (Targeting Extracted Strings)
      enqueueSnackbar('Scanning for Secrets & Weak Crypto...', { variant: 'info' });

      // Parallel execution with individual error handling
      let secrets = null;
      let crypto = null;

      try {
        secrets = await scanSecrets(content, null);
      } catch (e) {
        console.error("Secret Hunter Failed", e);
        enqueueSnackbar('Secret Hunter service failed', { variant: 'error' });
      }

      try {
        crypto = await scanCrypto(content, null);
      } catch (e) {
        console.error("Crypto Check Failed", e);
        enqueueSnackbar('Crypto Check service failed', { variant: 'error' });
      }

      setSecretResult(secrets);
      setCryptoResult(crypto);

      setActiveStep(3); // Done
      enqueueSnackbar('Automated Analysis Complete!', { variant: 'success' });

    } catch (error) {
      console.error("Scan Failed:", error);
      enqueueSnackbar('Automated Scan failed. Check console.', { variant: 'error' });
    } finally {
      setIsScanning(false);
    }
  };

  const renderFindings = (findings, title) => {
    if (!findings || findings.length === 0) return <Alert severity="success">No issues found in {title}.</Alert>;
    return (
      <Stack spacing={1}>
        <Alert severity="warning">Found {findings.length} issues in {title}</Alert>
        <Box sx={{ maxHeight: 200, overflowY: 'auto' }}>
          {findings.map((f, i) => (
            <Paper key={i} variant="outlined" sx={{ p: 1, my: 0.5, bgcolor: 'grey.50' }}>
              <Typography variant="caption" fontWeight="bold" color="error">{f.type || f.ruleId}</Typography>
              <br />
              <Typography variant="caption">{f.match || f.description}</Typography>
            </Paper>
          ))}
        </Box>
      </Stack>
    );
  };

  return (
    <Grid container spacing={4} justifyContent="center">
      <Grid item xs={12} md={10}>
        <Stack spacing={4}>
          <Box textAlign="center">
            <Typography variant="h3" gutterBottom sx={{ background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`, backgroundClip: 'text', textFillColor: 'transparent', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Automated Security Pipeline
            </Typography>
            <Typography variant="h6" color="text.secondary">
              One-Click Analysis: APK Manifest • AWS/API Secrets • Weak Crypto
            </Typography>
          </Box>

          {/* UPLOAD AREA */}
          {!isScanning && activeStep === 0 && !selectedFile && (
            <Paper
              elevation={0}
              component={motion.div}
              whileHover={{ scale: 1.01 }}
              onDrop={handleDrop}
              onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
              onDragLeave={() => setIsDragging(false)}
              sx={{
                border: '3px dashed',
                borderColor: isDragging ? 'primary.main' : 'divider',
                borderRadius: 4,
                p: 8,
                textAlign: 'center',
                bgcolor: isDragging ? 'rgba(255, 107, 53, 0.05)' : 'background.paper',
                cursor: 'pointer'
              }}
            >
              <input
                type="file"
                accept=".apk"
                style={{ display: 'none' }}
                id="auto-scan-input"
                onChange={(e) => handleFileSelect(e.target.files?.[0])}
              />
              <label htmlFor="auto-scan-input" style={{ width: '100%', height: '100%', display: 'block' }}>
                <Stack spacing={2} alignItems="center">
                  <AndroidIcon sx={{ fontSize: 64, color: 'primary.main' }} />
                  <Typography variant="h5" fontWeight="bold">Drag & Drop APK to Auto-Scan</Typography>
                  <Button variant="contained" size="large" sx={{ pointerEvents: 'none' }}>Browse Files</Button>
                </Stack>
              </label>
            </Paper>
          )}

          {/* PROGRESS & RESULTS */}
          {(isScanning || activeStep > 0 || selectedFile) && (
            <Paper elevation={2} sx={{ p: 4, borderRadius: 3 }}>
              <Stack spacing={3}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <Typography variant="h5">Analysis Pipeline</Typography>
                  <Button onClick={resetState} variant="outlined">New Scan</Button>
                </Stack>

                <Stepper activeStep={activeStep} orientation="vertical">

                  {/* STEP 1: APK MANIFEST */}
                  <Step expanded={true}>
                    <StepLabel error={apkResult?.manifest?.is_debuggable}>
                      APK Structure & Manifest Analysis
                    </StepLabel>
                    <StepContent>
                      {apkResult ? (
                        <Box sx={{ mt: 2, mb: 1 }}>
                          <Typography variant="subtitle2">Package: {apkResult.manifest.package_name}</Typography>
                          <Stack direction="row" spacing={1} mt={1}>
                            <Chip label={`Debuggable: ${apkResult.manifest.is_debuggable}`}
                              color={apkResult.manifest.is_debuggable ? "error" : "success"} size="small" />
                            <Chip label={`Allow Backup: ${apkResult.manifest.allow_backup}`}
                              color={apkResult.manifest.allow_backup ? "warning" : "success"} size="small" />
                          </Stack>
                        </Box>
                      ) : <Typography color="text.secondary">Waiting...</Typography>}
                    </StepContent>
                  </Step>

                  {/* STEP 2: EXTRACTION */}
                  <Step expanded={activeStep >= 1}>
                    <StepLabel>Decompilation & String Extraction</StepLabel>
                    <StepContent>
                      {activeStep >= 2 ? (
                        <Alert severity="success">Extracted strings from DEX classes successfully.</Alert>
                      ) : isScanning && activeStep === 1 ? <LinearProgress /> : null}
                    </StepContent>
                  </Step>

                  {/* STEP 3: DEEP SCAN */}
                  <Step expanded={activeStep >= 2}>
                    <StepLabel>Secret Hunter & Crypto Check</StepLabel>
                    <StepContent>
                      {activeStep === 3 ? (
                        <Grid container spacing={2}>
                          <Grid item xs={12} md={6}>
                            <Typography variant="subtitle2" gutterBottom>Secret Findings</Typography>
                            {renderFindings(secretResult?.findings, "SecretHunter")}
                          </Grid>
                          <Grid item xs={12} md={6}>
                            <Typography variant="subtitle2" gutterBottom>Crypto Findings</Typography>
                            {renderFindings(cryptoResult?.findings, "CryptoCheck")}
                          </Grid>
                        </Grid>
                      ) : isScanning && activeStep >= 2 ? <LinearProgress color="secondary" /> : null}
                    </StepContent>
                  </Step>
                </Stepper>

                {activeStep === 0 && selectedFile && (
                  <Button onClick={runAutoScan} variant="contained" size="large" fullWidth>
                    Start Auto-Analysis on {selectedFile.name}
                  </Button>
                )}

                {activeStep === 3 && (
                  <Button
                    onClick={() => navigate('/report', { state: { apkResult, secretResult, cryptoResult, scanDate: new Date().toLocaleString() } })}
                    variant="contained"
                    color="success"
                    size="large"
                    fullWidth
                    startIcon={<CheckCircleOutlineIcon />}
                  >
                    View & Print Full Report
                  </Button>
                )}
              </Stack>
            </Paper>
          )}

        </Stack>
      </Grid>
    </Grid>
  );
}

export default UploadPage;
