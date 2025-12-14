import React, { useEffect, useState, useMemo, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
    Box,
    Button,
    Container,
    Grid,
    Paper,
    Stack,
    Typography,
    Chip,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    useTheme
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import CodeIcon from '@mui/icons-material/Code';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import ErrorIcon from '@mui/icons-material/Error';
import SecurityIcon from '@mui/icons-material/Security';
import BugReportIcon from '@mui/icons-material/BugReport';

import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import { createReport, downloadReportById } from '../services/api';
import { useSnackbar } from 'notistack';

export default function ReportPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const theme = useTheme();
    const { enqueueSnackbar } = useSnackbar();

    // Destructure with default empty objects to prevent crashes
    const { apkResult, secretResult, cryptoResult, scanDate } = location.state || {};
    const [scanId] = useState(apkResult?.scan_id || null);
    const [isSaving, setIsSaving] = useState(false);
    const hasSavedRef = useRef(false);

    // Normalize findings (useMemo to prevent unstable dependencies)
    const secretFindings = useMemo(() =>
        Array.isArray(secretResult) ? secretResult : (secretResult?.findings || []),
        [secretResult]);

    const cryptoFindings = useMemo(() =>
        Array.isArray(cryptoResult) ? cryptoResult : (cryptoResult?.findings || []),
        [cryptoResult]);

    // Save report to backend on load
    // Save report to backend on load
    // Helper to ensure report exists on backend
    const ensureReportExists = async () => {
        setIsSaving(true);
        try {
            const payload = {
                scanId: apkResult.scan_id,
                manifest: apkResult.manifest,
                secrets: { findings: secretFindings },
                crypto: { findings: cryptoFindings }
            };
            await createReport(payload);
            return true;
        } catch (error) {
            console.error("Failed to save report", error);
            return false;
        } finally {
            setIsSaving(false);
        }
    };

    // Save report to backend on load (Initial sync)
    useEffect(() => {
        if (apkResult && scanId && !hasSavedRef.current) {
            hasSavedRef.current = true;
            ensureReportExists()
                .then(success => {
                    if (success) enqueueSnackbar('Report saved successfully', { variant: 'success' });
                    else enqueueSnackbar('Failed to save report to backend', { variant: 'error' });
                });
        }
    }, [scanId, apkResult, secretFindings, cryptoFindings, enqueueSnackbar]);

    // Robust Download Handler
    const handleDownload = async (format) => {
        const attemptDownload = async () => {
            const { blob, filename } = await downloadReportById(scanId, format);
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
        };

        try {
            await attemptDownload();
        } catch (e) {
            // If download fails (likely 404 due to backend restart), try Resaving
            console.warn("Download failed, attempting to restore report...", e);
            enqueueSnackbar('Report not found on server. Resaving...', { variant: 'info' });

            const saved = await ensureReportExists();
            if (saved) {
                try {
                    await attemptDownload();
                    enqueueSnackbar(`${format} downloaded successfully`, { variant: 'success' });
                } catch (retryError) {
                    enqueueSnackbar(`Failed to download ${format} after restore`, { variant: 'error' });
                }
            } else {
                enqueueSnackbar('Failed to restore report data to server', { variant: 'error' });
            }
        }
    };

    const handleDownloadPdf = () => handleDownload('PDF');
    const handleDownloadSarif = () => handleDownload('SARIF');

    if (!apkResult) {
        return (
            <Container sx={{ mt: 4, textAlign: 'center' }}>
                <Typography variant="h5" color="text.secondary">No report data found.</Typography>
                <Button onClick={() => navigate('/upload')} sx={{ mt: 2 }} variant="outlined">Start New Scan</Button>
            </Container>
        );
    }

    // --- ANALYSIS LOGIC ---
    const manifestRisks = [];
    if (apkResult.manifest?.is_debuggable) manifestRisks.push({ type: 'Manifest', severity: 'High', desc: 'App is Debuggable' });
    if (apkResult.manifest?.allow_backup) manifestRisks.push({ type: 'Manifest', severity: 'Medium', desc: 'Backup Allowed' });
    if (apkResult.manifest?.uses_cleartext_traffic) manifestRisks.push({ type: 'Manifest', severity: 'High', desc: 'Cleartext Traffic Allowed' });

    const secretRisks = secretFindings.map(f => ({ type: 'Secret', severity: 'Critical', desc: f.type, match: f.match }));
    const cryptoRisks = cryptoFindings.map(f => ({ type: 'Crypto', severity: 'High', desc: f.description, rule: f.ruleId }));

    const allRisks = [...manifestRisks, ...secretRisks, ...cryptoRisks];

    // Counts
    const criticalCount = allRisks.filter(r => r.severity === 'Critical').length;
    const highCount = allRisks.filter(r => r.severity === 'High').length;
    const mediumCount = allRisks.filter(r => r.severity === 'Medium').length;
    const totalIssues = allRisks.length;

    // Score Calculation (Simple)
    // Base 100. Deduct 20 for Critical, 10 for High, 5 for Medium.
    let securityScore = 100 - (criticalCount * 20) - (highCount * 10) - (mediumCount * 5);
    if (securityScore < 0) securityScore = 0;

    // Chart Data
    const pieData = [
        { name: 'Manifest', value: manifestRisks.length, color: theme.palette.info.main },
        { name: 'Secrets', value: secretRisks.length, color: theme.palette.error.main },
        { name: 'Crypto', value: cryptoRisks.length, color: theme.palette.warning.main },
    ].filter(d => d.value > 0);

    const severityData = [
        { name: 'Critical', count: criticalCount },
        { name: 'High', count: highCount },
        { name: 'Medium', count: mediumCount },
    ];



    const getSeverityColor = (sev) => {
        switch (sev) {
            case 'Critical': return 'error';
            case 'High': return 'error';
            case 'Medium': return 'warning';
            default: return 'success';
        }
    };

    return (
        <Container maxWidth="lg" sx={{ py: 4 }} id="report-content">

            {/* ACTION BAR */}
            <Stack direction="row" justifyContent="space-between" mb={4} className="no-print">
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/upload')}>Back to Scan</Button>
                <Stack direction="row" spacing={2}>
                    <Button variant="outlined" startIcon={<CodeIcon />} onClick={handleDownloadSarif}>Export SARIF</Button>
                    <Button variant="contained" startIcon={<PrintIcon />} onClick={handleDownloadPdf} color="primary">Export PDF</Button>
                </Stack>
            </Stack>

            {/* HEADER SECTION */}
            <Paper elevation={0} sx={{ p: 4, borderRadius: 4, mb: 4, border: '1px solid', borderColor: 'divider', background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)' }}>
                <Grid container alignItems="center" spacing={4}>
                    <Grid item xs={12} md={8}>
                        <Stack direction="row" spacing={2} alignItems="center">
                            <SecurityIcon sx={{ fontSize: 60, color: 'primary.main', opacity: 0.8 }} />
                            <Box>
                                <Typography variant="h3" fontWeight="800" sx={{ letterSpacing: '-1px' }}>
                                    {apkResult.manifest?.package_name || "Application"}
                                </Typography>
                                <Typography variant="h6" color="text.secondary">
                                    Security Assessment Report
                                </Typography>
                                <Stack direction="row" spacing={2} mt={1}>
                                    <Chip label={`Ver: ${apkResult.manifest?.version_code}`} size="small" sx={{ bgcolor: 'white' }} />
                                    <Chip label={scanDate || "Just Now"} size="small" sx={{ bgcolor: 'white' }} />
                                </Stack>
                            </Box>
                        </Stack>
                    </Grid>
                    <Grid item xs={12} md={4} textAlign="center">
                        <Box position="relative" display="inline-flex">
                            <CircularScore score={securityScore} />
                            <Box
                                sx={{
                                    top: 0,
                                    left: 0,
                                    bottom: 0,
                                    right: 0,
                                    position: 'absolute',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                }}
                            >
                                <Typography variant="h3" component="div" fontWeight="bold" color={securityScore > 80 ? 'success.main' : securityScore > 50 ? 'warning.main' : 'error.main'}>
                                    {securityScore}
                                </Typography>
                                <Typography variant="caption" component="div" color="text.secondary">
                                    SAFETY SCORE
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                </Grid>
            </Paper>

            {/* RISKS OVERVIEW CARDS */}
            <Grid container spacing={3} mb={4}>
                <Grid item xs={12} md={3}>
                    <RiskCard title="Total Issues" count={totalIssues} color={theme.palette.primary.main} icon={<BugReportIcon />} />
                </Grid>
                <Grid item xs={12} md={3}>
                    <RiskCard title="High Risks" count={highCount + criticalCount} color={theme.palette.error.main} icon={<ErrorIcon />} />
                </Grid>
                <Grid item xs={12} md={3}>
                    <RiskCard title="Medium Risks" count={mediumCount} color={theme.palette.warning.main} icon={<WarningIcon />} />
                </Grid>
                <Grid item xs={12} md={3}>
                    <RiskCard title="Passed Checks" count={100} color={theme.palette.success.main} icon={<CheckCircleIcon />} />
                </Grid>
            </Grid>

            <Grid container spacing={4}>

                {/* LEFT COLUMN: VISUALIZATIONS */}
                <Grid item xs={12} md={4}>
                    <Stack spacing={3}>
                        <Paper variant="outlined" sx={{ p: 3, borderRadius: 3, height: 350 }}>
                            <Typography variant="h6" fontWeight="bold" gutterBottom>Vulnerability Types</Typography>
                            <ResponsiveContainer width="100%" height="85%">
                                <PieChart>
                                    <Pie data={pieData} innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value">
                                        {pieData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                    <Legend verticalAlign="bottom" />
                                </PieChart>
                            </ResponsiveContainer>
                        </Paper>

                        <Paper variant="outlined" sx={{ p: 3, borderRadius: 3, height: 350 }}>
                            <Typography variant="h6" fontWeight="bold" gutterBottom>Severity Distribution</Typography>
                            <ResponsiveContainer width="100%" height="85%">
                                <BarChart data={severityData}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis />
                                    <Tooltip />
                                    <Bar dataKey="count" fill={theme.palette.primary.main} radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </Paper>
                    </Stack>
                </Grid>

                {/* RIGHT COLUMN: DETAILED FINDINGS */}
                <Grid item xs={12} md={8}>
                    <Stack spacing={3}>

                        {/* 1. SECRETS */}
                        <Paper variant="outlined" sx={{ p: 0, borderRadius: 3, overflow: 'hidden' }}>
                            <Box sx={{ p: 2, bgcolor: 'error.lighter', borderBottom: '1px solid', borderColor: 'divider' }}>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <ErrorIcon color="error" />
                                    <Typography variant="h6" fontWeight="bold">Hardcoded Secrets Detected ({secretRisks.length})</Typography>
                                </Stack>
                            </Box>
                            <TableContainer>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Vulnerability Type</TableCell>
                                            <TableCell>Evidence / Location</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {secretRisks.length > 0 ? secretRisks.map((row, i) => (
                                            <TableRow key={i} hover>
                                                <TableCell>
                                                    <Chip label="CRITICAL" color="error" size="small" sx={{ mr: 1, fontWeight: 'bold' }} />
                                                    {row.desc}
                                                </TableCell>
                                                <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem', bgcolor: 'grey.50' }}>
                                                    {row.match}
                                                </TableCell>
                                            </TableRow>
                                        )) : (
                                            <TableRow>
                                                <TableCell colSpan={2} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                                                    <CheckCircleIcon color="success" sx={{ mb: 1, fontSize: 40 }} />
                                                    <Typography>No leaked secrets found.</Typography>
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Paper>

                        {/* 2. CRYPTO */}
                        <Paper variant="outlined" sx={{ p: 0, borderRadius: 3, overflow: 'hidden' }}>
                            <Box sx={{ p: 2, bgcolor: 'warning.lighter', borderBottom: '1px solid', borderColor: 'divider' }}>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <WarningIcon color="warning" />
                                    <Typography variant="h6" fontWeight="bold">Cryptographic Issues ({cryptoRisks.length})</Typography>
                                </Stack>
                            </Box>
                            <TableContainer>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Rule ID</TableCell>
                                            <TableCell>Description</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {cryptoRisks.length > 0 ? cryptoRisks.map((row, i) => (
                                            <TableRow key={i} hover>
                                                <TableCell>
                                                    <Chip label="HIGH" color="error" size="small" variant="outlined" sx={{ mr: 1 }} />
                                                    <strong>{row.rule}</strong>
                                                </TableCell>
                                                <TableCell>{row.desc}</TableCell>
                                            </TableRow>
                                        )) : (
                                            <TableRow>
                                                <TableCell colSpan={2} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                                                    <CheckCircleIcon color="success" sx={{ mb: 1, fontSize: 40 }} />
                                                    <Typography>No weak crypto found.</Typography>
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Paper>

                        {/* 3. MANIFEST */}
                        <Paper variant="outlined" sx={{ p: 0, borderRadius: 3, overflow: 'hidden' }}>
                            <Box sx={{ p: 2, bgcolor: 'info.lighter', borderBottom: '1px solid', borderColor: 'divider' }}>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <SecurityIcon color="info" />
                                    <Typography variant="h6" fontWeight="bold">Manifest Configuration ({manifestRisks.length})</Typography>
                                </Stack>
                            </Box>
                            <TableContainer>
                                <Table>
                                    <TableBody>
                                        {manifestRisks.length > 0 ? manifestRisks.map((row, i) => (
                                            <TableRow key={i}>
                                                <TableCell>
                                                    <Chip label={row.severity.toUpperCase()} color={getSeverityColor(row.severity)} size="small" sx={{ mr: 2 }} />
                                                    {row.desc}
                                                </TableCell>
                                            </TableRow>
                                        )) : (
                                            <TableRow>
                                                <TableCell align="center" sx={{ py: 4, color: 'text.secondary' }}>
                                                    <CheckCircleIcon color="success" sx={{ mb: 1, fontSize: 40 }} />
                                                    <Typography>Manifest checks passed safely.</Typography>
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Paper>

                    </Stack>
                </Grid>
            </Grid>
        </Container>
    );
}

// --- SUBCOMPONENTS ---

function RiskCard({ title, count, color, icon }) {
    return (
        <Paper elevation={0} sx={{ p: 3, borderRadius: 3, border: '1px solid', borderColor: `${color}40`, bgcolor: `${color}08`, height: '100%' }}>
            <Stack spacing={2}>
                <Box sx={{ color: color, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    {icon}
                    <Typography variant="h3" fontWeight="bold">{count}</Typography>
                </Box>
                <Typography variant="subtitle2" color="text.secondary" fontWeight="bold">{title.toUpperCase()}</Typography>
            </Stack>
        </Paper>
    );
}

function CircularScore({ score }) {
    // Basic SVG circle
    const radius = 50;
    const stroke = 8;
    const normalizedRadius = radius - stroke * 2;
    const circumference = normalizedRadius * 2 * Math.PI;
    const strokeDashoffset = circumference - (score / 100) * circumference;

    let color = '#f44336';
    if (score > 50) color = '#ff9800';
    if (score > 80) color = '#4caf50';

    return (
        <div style={{ position: 'relative', width: 120, height: 120 }}>
            <svg height="120" width="120" style={{ transform: 'rotate(-90deg)' }}>
                <circle
                    stroke="#e6e6e6"
                    strokeWidth={stroke}
                    fill="transparent"
                    r={normalizedRadius}
                    cx="60"
                    cy="60"
                />
                <circle
                    stroke={color}
                    strokeDasharray={circumference + ' ' + circumference}
                    style={{ strokeDashoffset, transition: 'stroke-dashoffset 0.5s ease-in-out' }}
                    strokeWidth={stroke}
                    strokeLinecap="round"
                    fill="transparent"
                    r={normalizedRadius}
                    cx="60"
                    cy="60"
                />
            </svg>
        </div>
    );
}
