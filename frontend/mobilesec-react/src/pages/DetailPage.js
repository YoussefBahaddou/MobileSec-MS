import { useEffect, useMemo, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Paper,
  Stack,
  Typography,
  useTheme,
  Divider,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import DownloadIcon from '@mui/icons-material/Download';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import LockOpenOutlinedIcon from '@mui/icons-material/LockOpenOutlined';
import FlagOutlinedIcon from '@mui/icons-material/FlagOutlined';
import AppsOutlinedIcon from '@mui/icons-material/AppsOutlined';
import AndroidIcon from '@mui/icons-material/Android';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import { useSnackbar } from 'notistack';
import { useNavigate, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { downloadReportById, getResultById } from '../services/api';
import RiskChart from '../components/RiskChart';
import PermissionsHeatmap from '../components/PermissionsHeatmap';

const riskColorMap = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'error',
  UNKNOWN: 'default',
};

const reportFormats = ['JSON', 'SARIF', 'PDF'];

function DetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const theme = useTheme();

  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [downloadingFormat, setDownloadingFormat] = useState(null);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        setLoading(true);
        const data = await getResultById(id);
        setAnalysis(data);
      } catch (error) {
        console.error(error);
        enqueueSnackbar('Unable to fetch analysis details.', { variant: 'error' });
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchDetails();
    }
  }, [enqueueSnackbar, id]);

  const metadata = analysis?.metadata ?? {};

  const riskReasons = useMemo(
    () => (Array.isArray(analysis?.riskReasons) ? analysis.riskReasons.filter(Boolean) : []),
    [analysis]
  );

  const enrichedLists = useMemo(() => {
    const permissions = Array.isArray(analysis?.permissions)
      ? analysis.permissions.filter(Boolean)
      : [];
    const manifestFlags = Array.isArray(analysis?.manifestFlags)
      ? analysis.manifestFlags.filter(Boolean)
      : [];
    const exportedComponents = Array.isArray(analysis?.exportedComponents)
      ? analysis.exportedComponents.map((item, index) => {
        if (item && typeof item === 'object') {
          return {
            name: item.name ?? `Component ${index + 1}`,
            type: item.type ?? 'Unknown',
          };
        }
        return {
          name: String(item ?? `Component ${index + 1}`),
          type: 'Unknown',
        };
      })
      : [];

    return {
      permissions,
      manifestFlags,
      exportedComponents,
    };
  }, [analysis]);

  const triggerDownload = (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  const handleDownloadReport = async (format) => {
    if (!id) return;

    try {
      setDownloadingFormat(format);
      const { blob, filename } = await downloadReportById(id, format);
      triggerDownload(blob, filename);
      enqueueSnackbar(`${format} report downloaded successfully.`, { variant: 'success' });
    } catch (error) {
      console.error(error);
      enqueueSnackbar(`Failed to download ${format} report.`, { variant: 'error' });
    } finally {
      setDownloadingFormat(null);
    }
  };

  if (loading) {
    return (
      <Stack spacing={2} alignItems="center" justifyContent="center" sx={{ py: 8, minHeight: '60vh' }}>
        <CircularProgress size={60} thickness={4} />
        <Typography variant="h6" color="text.secondary" component={motion.div} animate={{ opacity: [0.5, 1, 0.5] }} transition={{ repeat: Infinity, duration: 1.5 }}>
          Loading Analysis Details...
        </Typography>
      </Stack>
    );
  }

  if (!analysis) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Stack spacing={3} alignItems="center">
          <Typography variant="h5" color="error">
            Analysis Not Found
          </Typography>
          <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
            Return to Dashboard
          </Button>
        </Stack>
      </Container>
    );
  }

  return (
    <Stack spacing={4}>
      {/* Header Section */}
      <Stack
        direction={{ xs: 'column', md: 'row' }}
        justifyContent="space-between"
        alignItems={{ xs: 'flex-start', md: 'center' }}
        spacing={2}
        component={motion.div}
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5 }}
      >
        <Stack direction="row" spacing={2} alignItems="center">
          <IconButton onClick={() => navigate(-1)} sx={{ bgcolor: 'background.paper', boxShadow: 1 }}>
            <ArrowBackIcon />
          </IconButton>
          <Box>
            <Stack direction="row" spacing={1} alignItems="center">
              <AndroidIcon color="primary" fontSize="large" />
              <Typography variant="h4" sx={{ background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`, backgroundClip: 'text', textFillColor: 'transparent', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                {analysis.packageName ?? 'Unknown Package'}
              </Typography>
            </Stack>
            <Stack direction="row" spacing={2} alignItems="center" sx={{ mt: 0.5, ml: 5 }}>
              <Chip label={`v${analysis.versionName ?? 'N/A'}`} size="small" sx={{ bgcolor: 'background.paper', fontWeight: 600 }} />
              <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <CalendarTodayIcon fontSize="inherit" /> {new Date(analysis.createdAt).toLocaleString()}
              </Typography>
            </Stack>
          </Box>
        </Stack>

        <Stack direction="row" spacing={1}>
          {reportFormats.map((format) => (
            <Button
              key={format}
              variant={format === 'PDF' ? 'contained' : 'outlined'}
              color={format === 'PDF' ? 'primary' : 'inherit'}
              startIcon={downloadingFormat === format ? <CircularProgress size={20} color="inherit" /> : <DownloadIcon />}
              onClick={() => handleDownloadReport(format)}
              disabled={downloadingFormat !== null}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              {format}
            </Button>
          ))}
        </Stack>
      </Stack>

      {/* Charts & Summary Section */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <RiskChart riskLevel={analysis.riskLevel} />
        </Grid>
        <Grid item xs={12} md={4}>
          <PermissionsHeatmap permissions={enrichedLists.permissions} />
        </Grid>
        <Grid item xs={12} md={4}>
          <Paper
            elevation={0}
            sx={{
              height: 300,
              p: 3,
              borderRadius: 4,
              bgcolor: 'secondary.main',
              color: 'white',
              boxShadow: theme.shadows[4],
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
              position: 'relative',
              overflow: 'hidden'
            }}
            component={motion.div}
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.1 }}
          >
            <Box sx={{ position: 'absolute', top: -20, right: -20, opacity: 0.1 }}>
              <VerifiedUserIcon sx={{ fontSize: 150 }} />
            </Box>

            <Typography variant="h6" gutterBottom fontWeight="600" sx={{ opacity: 0.9 }}>
              Security Scorecard
            </Typography>
            <Divider sx={{ bgcolor: 'rgba(255,255,255,0.1)', mb: 2 }} />

            <Stack spacing={2.5}>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Typography variant="body2" sx={{ opacity: 0.7 }}>Overall Risk</Typography>
                <Chip
                  label={analysis.riskLevel}
                  color={riskColorMap[analysis.riskLevel]}
                  sx={{ fontWeight: 'bold', px: 1 }}
                />
              </Box>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Typography variant="body2" sx={{ opacity: 0.7 }}>Permissions</Typography>
                <Typography fontWeight="600" variant="h6">{enrichedLists.permissions.length}</Typography>
              </Box>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Typography variant="body2" sx={{ opacity: 0.7 }}>Components</Typography>
                <Typography fontWeight="600" variant="h6">{enrichedLists.exportedComponents.length}</Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid>
      </Grid>

      {/* Detailed Lists Section */}
      <Grid container spacing={3}>
        {/* Secrets & Crypto Issues */}
        {(analysis.secrets?.length > 0 || analysis.cryptoIssues?.length > 0) && (
          <Grid item xs={12}>
            <Stack spacing={3}>
              {analysis.secrets?.length > 0 && (
                <Card elevation={0} sx={{ borderRadius: 4, border: `1px solid ${theme.palette.error.light}`, bgcolor: '#FFF5F5' }}>
                  <CardContent>
                    <Stack direction="row" spacing={1} alignItems="center" mb={2}>
                      <WarningAmberOutlinedIcon color="error" />
                      <Typography variant="h6" fontWeight={600} color="error.dark">
                        Hardcoded Secrets Detected
                      </Typography>
                    </Stack>
                    <Grid container spacing={2}>
                      {analysis.secrets.map((secret, idx) => (
                        <Grid item xs={12} key={idx}>
                          <Paper elevation={0} sx={{ p: 2, bgcolor: 'white', borderRadius: 2, border: '1px solid rgba(0,0,0,0.05)' }}>
                            <Typography variant="body2" fontFamily="monospace" color="error.main">
                              {secret}
                            </Typography>
                          </Paper>
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </Card>
              )}

              {analysis.cryptoIssues?.length > 0 && (
                <Card elevation={0} sx={{ borderRadius: 4, border: `1px solid ${theme.palette.warning.light}`, bgcolor: '#FFF9F0' }}>
                  <CardContent>
                    <Stack direction="row" spacing={1} alignItems="center" mb={2}>
                      <WarningAmberOutlinedIcon color="warning" />
                      <Typography variant="h6" fontWeight={600} color="warning.dark">
                        Cryptographic Issues
                      </Typography>
                    </Stack>
                    <Grid container spacing={2}>
                      {analysis.cryptoIssues.map((issue, idx) => (
                        <Grid item xs={12} key={idx}>
                          <Paper elevation={0} sx={{ p: 2, bgcolor: 'white', borderRadius: 2, border: '1px solid rgba(0,0,0,0.05)' }}>
                            <Typography variant="body2" fontFamily="monospace" color="warning.dark">
                              {issue}
                            </Typography>
                          </Paper>
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </Card>
              )}
            </Stack>
          </Grid>
        )}

        {/* Risk Reasons */}
        {riskReasons.length > 0 && (
          <Grid item xs={12}>
            <Card elevation={0} sx={{ borderRadius: 4, border: `1px solid ${theme.palette.warning.light}`, bgcolor: '#FFF9F0' }}>
              <CardContent>
                <Stack direction="row" spacing={1} alignItems="center" mb={2}>
                  <WarningAmberOutlinedIcon color="warning" />
                  <Typography variant="h6" fontWeight={600} color="warning.dark">
                    Security Risks Detected
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  {riskReasons.map((reason, idx) => (
                    <Grid item xs={12} sm={6} md={4} key={idx}>
                      <Paper elevation={0} sx={{ p: 2, bgcolor: 'white', borderRadius: 2, display: 'flex', alignItems: 'center', gap: 1.5, border: '1px solid rgba(0,0,0,0.05)' }}>
                        <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'warning.main', flexShrink: 0 }} />
                        <Typography variant="body2" fontWeight={500}>{reason}</Typography>
                      </Paper>
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Permissions */}
        <Grid item xs={12} md={6}>
          <Card elevation={0} sx={{ height: '100%', borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" mb={2}>
                <LockOpenOutlinedIcon color="primary" />
                <Typography variant="h6" fontWeight={600}>
                  Permissions ({enrichedLists.permissions.length})
                </Typography>
              </Stack>
              <Box sx={{ maxHeight: 400, overflowY: 'auto', pr: 1 }}>
                {enrichedLists.permissions.length > 0 ? (
                  <List dense>
                    {enrichedLists.permissions.map((perm, idx) => (
                      <ListItem key={idx} divider={idx !== enrichedLists.permissions.length - 1}>
                        <ListItemText
                          primary={perm}
                          primaryTypographyProps={{ variant: 'body2', fontFamily: 'monospace', color: 'text.primary' }}
                        />
                      </ListItem>
                    ))}
                  </List>
                ) : (
                  <Typography variant="body2" color="text.secondary">No permissions requested.</Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Exported Components & Flags */}
        <Grid item xs={12} md={6}>
          <Stack spacing={3} height="100%">
            <Card elevation={0} sx={{ flex: 1, borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
              <CardContent>
                <Stack direction="row" spacing={1} alignItems="center" mb={2}>
                  <FlagOutlinedIcon color="secondary" />
                  <Typography variant="h6" fontWeight={600}>
                    Manifest Flags
                  </Typography>
                </Stack>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {enrichedLists.manifestFlags.length > 0 ? (
                    enrichedLists.manifestFlags.map((flag, idx) => (
                      <Chip key={idx} label={flag} size="small" variant="outlined" color="secondary" sx={{ borderRadius: 1 }} />
                    ))
                  ) : (
                    <Typography variant="body2" color="text.secondary">No flags set.</Typography>
                  )}
                </Box>
              </CardContent>
            </Card>

            <Card elevation={0} sx={{ flex: 2, borderRadius: 4, border: '1px solid', borderColor: 'divider' }}>
              <CardContent>
                <Stack direction="row" spacing={1} alignItems="center" mb={2}>
                  <AppsOutlinedIcon color="info" />
                  <Typography variant="h6" fontWeight={600}>
                    Exported Components ({enrichedLists.exportedComponents.length})
                  </Typography>
                </Stack>
                <Box sx={{ maxHeight: 250, overflowY: 'auto', pr: 1 }}>
                  {enrichedLists.exportedComponents.length > 0 ? (
                    <List dense>
                      {enrichedLists.exportedComponents.map((comp, idx) => (
                        <ListItem key={idx} divider={idx !== enrichedLists.exportedComponents.length - 1}>
                          <ListItemText
                            primary={comp.name}
                            secondary={comp.type}
                            primaryTypographyProps={{ variant: 'body2', fontWeight: 500 }}
                            secondaryTypographyProps={{ variant: 'caption', color: 'info.main' }}
                          />
                        </ListItem>
                      ))}
                    </List>
                  ) : (
                    <Typography variant="body2" color="text.secondary">No exported components found.</Typography>
                  )}
                </Box>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>

      {/* Raw Metadata */}
      <Card elevation={0} sx={{ borderRadius: 4, bgcolor: 'grey.50', border: '1px solid', borderColor: 'divider' }}>
        <CardContent>
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>
            Raw Metadata
          </Typography>
          <Box
            component="pre"
            sx={{
              p: 2,
              borderRadius: 2,
              bgcolor: 'secondary.main',
              color: 'grey.100',
              overflowX: 'auto',
              fontSize: '0.75rem',
              m: 0,
              fontFamily: 'monospace'
            }}
          >
            {JSON.stringify(metadata, null, 2)}
          </Box>
        </CardContent>
      </Card>
    </Stack>
  );
}

export default DetailPage;
