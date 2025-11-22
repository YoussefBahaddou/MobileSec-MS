import { useCallback, useState } from 'react';
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
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import ShieldOutlinedIcon from '@mui/icons-material/ShieldOutlined';
import { useSnackbar } from 'notistack';
import { motion, AnimatePresence } from 'framer-motion';
import { uploadApk } from '../services/api';

function UploadPage() {
  const { enqueueSnackbar } = useSnackbar();
  const theme = useTheme();
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState(null);

  const resetState = useCallback(() => {
    setSelectedFile(null);
    setProgress(0);
    setIsUploading(false);
    setResult(null);
  }, []);

  const handleFileSelect = useCallback(
    (file) => {
      if (!file) return;
      if (!file.name.endsWith('.apk')) {
        enqueueSnackbar('Please select an APK file.', { variant: 'warning' });
        return;
      }
      setSelectedFile(file);
      setResult(null);
    },
    [enqueueSnackbar]
  );

  const handleDrop = useCallback(
    (event) => {
      event.preventDefault();
      setIsDragging(false);
      const file = event.dataTransfer.files?.[0];
      handleFileSelect(file);
    },
    [handleFileSelect]
  );

  const handleUpload = async () => {
    if (!selectedFile) return;

    try {
      setIsUploading(true);
      setProgress(0);
      const data = await uploadApk(selectedFile, (progressEvent) => {
        if (!progressEvent.total) return;
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        setProgress(percent);
      });
      setResult(data);
      enqueueSnackbar('Analysis complete!', { variant: 'success' });
    } catch (error) {
      console.error(error);
      enqueueSnackbar('Upload failed. Please try again.', { variant: 'error' });
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <Grid container spacing={4} justifyContent="center">
      <Grid item xs={12} md={8}>
        <Stack spacing={4}>
          <Box textAlign="center">
            <Typography variant="h3" gutterBottom sx={{ background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`, backgroundClip: 'text', textFillColor: 'transparent', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Security Analysis
            </Typography>
            <Typography variant="h6" color="text.secondary" fontWeight="400">
              Upload your Android APK for instant vulnerability scanning.
            </Typography>
          </Box>

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
              p: 6,
              textAlign: 'center',
              bgcolor: isDragging ? 'rgba(255, 107, 53, 0.05)' : 'background.paper',
              transition: 'all 0.3s ease',
              cursor: 'pointer',
              position: 'relative',
              overflow: 'hidden'
            }}
          >
            <input
              type="file"
              accept=".apk"
              style={{ display: 'none' }}
              id="apk-upload-input"
              onChange={(e) => handleFileSelect(e.target.files?.[0])}
            />
            <label htmlFor="apk-upload-input" style={{ width: '100%', height: '100%', display: 'block' }}>
              <Stack spacing={3} alignItems="center">
                <Box
                  sx={{
                    width: 80,
                    height: 80,
                    borderRadius: '50%',
                    bgcolor: 'rgba(255, 107, 53, 0.1)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <CloudUploadIcon sx={{ fontSize: 40, color: 'primary.main' }} />
                </Box>
                <Box>
                  <Typography variant="h5" fontWeight="600" gutterBottom>
                    Drag & Drop your APK
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    or <span style={{ color: theme.palette.primary.main, fontWeight: 600 }}>browse files</span>
                  </Typography>
                </Box>
                <Chip label="Supports .apk files" size="small" sx={{ bgcolor: 'background.default' }} />
              </Stack>
            </label>
          </Paper>

          <AnimatePresence>
            {selectedFile && (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
              >
                <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
                  <CardContent>
                    <Stack spacing={3}>
                      <Stack direction="row" alignItems="center" spacing={2}>
                        <Box
                          sx={{
                            width: 48,
                            height: 48,
                            borderRadius: 2,
                            bgcolor: 'primary.main',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'white'
                          }}
                        >
                          <InsertDriveFileIcon />
                        </Box>
                        <Box flexGrow={1}>
                          <Typography variant="subtitle1" fontWeight="600">
                            {selectedFile.name}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB
                          </Typography>
                        </Box>
                        {!isUploading && !result && (
                          <IconButton onClick={resetState} color="error">
                            <DeleteOutlineIcon />
                          </IconButton>
                        )}
                      </Stack>

                      {isUploading && (
                        <Box>
                          <Stack direction="row" justifyContent="space-between" mb={1}>
                            <Typography variant="caption" fontWeight="600" color="primary">Uploading & Analyzing...</Typography>
                            <Typography variant="caption" color="text.secondary">{progress}%</Typography>
                          </Stack>
                          <LinearProgress
                            variant="determinate"
                            value={progress}
                            sx={{ height: 8, borderRadius: 4, bgcolor: 'rgba(255, 107, 53, 0.1)' }}
                          />
                        </Box>
                      )}

                      {!isUploading && !result && (
                        <Button
                          variant="contained"
                          size="large"
                          onClick={handleUpload}
                          fullWidth
                          sx={{ py: 1.5 }}
                        >
                          Start Security Analysis
                        </Button>
                      )}
                    </Stack>
                  </CardContent>
                </Card>
              </motion.div>
            )}
          </AnimatePresence>

          <AnimatePresence>
            {result && (
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
              >
                <Card
                  elevation={0}
                  sx={{
                    bgcolor: result.riskLevel === 'HIGH' ? '#FFF5F5' : '#F0FDF4',
                    border: '1px solid',
                    borderColor: result.riskLevel === 'HIGH' ? 'error.light' : 'success.light'
                  }}
                >
                  <CardContent>
                    <Stack spacing={3} alignItems="center" textAlign="center" py={2}>
                      {result.riskLevel === 'HIGH' ? (
                        <ErrorOutlineIcon color="error" sx={{ fontSize: 64 }} />
                      ) : (
                        <CheckCircleOutlineIcon color="success" sx={{ fontSize: 64 }} />
                      )}

                      <Box>
                        <Typography variant="h5" fontWeight="700" gutterBottom>
                          Analysis Complete
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                          Risk Level Assessed: <strong style={{ color: result.riskLevel === 'HIGH' ? theme.palette.error.main : theme.palette.success.main }}>{result.riskLevel}</strong>
                        </Typography>
                      </Box>

                      <Stack direction="row" spacing={2}>
                        <Button variant="outlined" onClick={resetState}>
                          Analyze Another
                        </Button>
                        <Button variant="contained" onClick={() => window.location.href = `/results/${result.id}`}>
                          View Full Report
                        </Button>
                      </Stack>
                    </Stack>
                  </CardContent>
                </Card>
              </motion.div>
            )}
          </AnimatePresence>
        </Stack>
      </Grid>
    </Grid>
  );
}

export default UploadPage;
