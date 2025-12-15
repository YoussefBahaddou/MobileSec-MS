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
    useTheme,
    Card,
    CardContent,
    CircularProgress,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    IconButton,
    Fade,
    Slide
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import CodeIcon from '@mui/icons-material/Code';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import ErrorIcon from '@mui/icons-material/Error';
import SecurityIcon from '@mui/icons-material/Security';
import BugReportIcon from '@mui/icons-material/BugReport';
import DownloadIcon from '@mui/icons-material/Download';
import CloseIcon from '@mui/icons-material/Close';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';

import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import { createReport, downloadReportById, getFixSuggestion } from '../services/api';
import { useSnackbar } from 'notistack';

export default function ReportPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const theme = useTheme();
    const { enqueueSnackbar } = useSnackbar();

    const { apkResult, secretResult, cryptoResult, scanDate } = location.state || {};
    const [scanId] = useState(apkResult?.scan_id || null);
    const hasSavedRef = useRef(false);

    const secretFindings = useMemo(() =>
        Array.isArray(secretResult) ? secretResult : (secretResult?.findings || []),
        [secretResult]);

    const cryptoFindings = useMemo(() =>
        Array.isArray(cryptoResult) ? cryptoResult : (cryptoResult?.findings || []),
        [cryptoResult]);

    const ensureReportExists = async () => {
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
        }
    };

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

    const handleDownload = async (format) => {
        try {
            const { blob, filename } = await downloadReportById(scanId, format);
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (e) {
            console.warn("Download failed, attempting to restore report...", e);
            enqueueSnackbar('Report not found. Resaving...', { variant: 'info' });
            const saved = await ensureReportExists();
            if (saved) {
                try {
                    const { blob, filename } = await downloadReportById(scanId, format);
                    const url = window.URL.createObjectURL(blob);
                    const link = document.createElement('a');
                    link.href = url;
                    link.setAttribute('download', filename);
                    document.body.appendChild(link);
                    link.click();
                    link.remove();
                } catch (retryError) {
                    enqueueSnackbar(`Failed to download ${format}`, { variant: 'error' });
                }
            } else {
                enqueueSnackbar('Failed to restore report', { variant: 'error' });
            }
        }
    };

    const handleDownloadPdf = () => handleDownload('PDF');
    const handleDownloadSarif = () => handleDownload('SARIF');

    const [fixModalOpen, setFixModalOpen] = useState(false);
    const [currentFix, setCurrentFix] = useState(null);
    const [loadingFix, setLoadingFix] = useState(false);

    // Slide Transition
    const Transition = React.forwardRef(function Transition(props, ref) {
        return <Slide direction="up" ref={ref} {...props} />;
    });

    const handleGetFix = async (checkName, status) => {
        setLoadingFix(true);
        // Map check name to Issue ID
        const issueMap = {
            'Debuggable': 'ANDROID_DEBUGGABLE',
            'Allow Backup': 'ANDROID_ALLOW_BACKUP',
            'Cleartext Traffic': 'ANDROID_CLEARTEXT_TRAFFIC'
        };

        const issueId = issueMap[checkName] || 'UNKNOWN_ISSUE';

        try {
            const fix = await getFixSuggestion(issueId, `Fix issue: ${checkName}`);
            setCurrentFix(fix);
            setFixModalOpen(true);
        } catch (e) {
            enqueueSnackbar("Failed to get suggestion.", { variant: 'error' });
        } finally {
            setLoadingFix(false);
        }
    };

    const handleCopyCode = () => {
        if (currentFix?.codeFix) {
            navigator.clipboard.writeText(currentFix.codeFix);
            enqueueSnackbar("Code copied to clipboard!", { variant: 'success' });
        }
    };

    if (!apkResult) {
        return (
            <Container sx={{ mt: 4, textAlign: 'center' }}>
                <Typography variant="h5" color="text.secondary">No report data found.</Typography>
                <CircularProgress style={{ marginTop: "20px" }} />
            </Container>
        );
    }

    // --- ANALYSIS LOGIC ---
    const manifestRisks = [];
    if (apkResult.manifest?.isDebuggable || apkResult.manifest?.is_debuggable) manifestRisks.push({ label: 'Debuggable', val: true, severity: 'High', desc: 'App is Debuggable' });
    if (apkResult.manifest?.isAllowBackup || apkResult.manifest?.allow_backup) manifestRisks.push({ label: 'Allow Backup', val: true, severity: 'Medium', desc: 'Backup Allowed' });
    if (apkResult.manifest?.isUsesCleartextTraffic || apkResult.manifest?.uses_cleartext_traffic) manifestRisks.push({ label: 'Cleartext Traffic', val: true, severity: 'High', desc: 'Cleartext Traffic Allowed' });

    const secretRisks = secretFindings.map(f => ({ type: 'Secret', severity: 'Critical', desc: f.type, match: f.match }));
    const cryptoRisks = cryptoFindings.map(f => ({ type: 'Crypto', severity: 'High', desc: f.description, rule: f.ruleId }));

    const allRisks = [...manifestRisks, ...secretRisks, ...cryptoRisks];

    // Counts
    const criticalCount = allRisks.filter(r => r.severity === 'Critical').length;
    const highCount = allRisks.filter(r => r.severity === 'High').length;
    const mediumCount = allRisks.filter(r => r.severity === 'Medium').length;
    const totalIssues = allRisks.length;

    let securityScore = 100 - (criticalCount * 20) - (highCount * 10) - (mediumCount * 5);
    if (securityScore < 0) securityScore = 0;

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

            {/* HEADER */}
            <Paper elevation={3} style={{ padding: "30px", marginBottom: "30px", borderLeft: "6px solid #3498db" }}>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <div>
                        <Typography variant="h3" style={{ fontWeight: "bold", color: "#2c3e50" }}>
                            Security Assessment Report
                        </Typography>
                        <Stack direction="row" spacing={2} mt={1}>
                            <Typography variant="subtitle1" style={{ color: "#7f8c8d" }}>Scan ID: {scanId}</Typography>
                            <Chip label={`Score: ${securityScore}`} color={securityScore > 80 ? "success" : "error"} />
                        </Stack>
                    </div>
                </Box>
            </Paper>

            <Grid container spacing={4}>
                {/* LEFT COLUMN: CHARTS */}
                <Grid item xs={12} md={4}>
                    <Stack spacing={3}>

                        {/* 0. ML MALWARE SCORE (XGBoost God Mode) */}
                        {/* Support both old 'security_score' and new 'ai_security_score' */}
                        {(apkResult.manifest?.ai_security_score || apkResult.manifest?.security_score) && (() => {
                            const ml = apkResult.manifest.ai_security_score || apkResult.manifest.security_score;
                            // Normalization: XGBoost uses 'probability'/'label', old used 'score'/'risk_label'
                            const score = ml.probability !== undefined ? ml.probability : ml.score;
                            const label = ml.label || ml.risk_label;
                            const color = ml.color || (score > 70 ? '#f44336' : (score > 30 ? '#ff9800' : '#4caf50'));

                            return (
                                <Paper variant="outlined" sx={{ p: 3, borderRadius: 3, bgcolor: '#f8f9fa', textAlign: 'center' }}>
                                    <Stack direction="row" alignItems="center" justifyContent="center" spacing={1} mb={2}>
                                        <BugReportIcon sx={{ color: color }} />
                                        <Typography variant="h6" fontWeight="bold">ML Risk (XGBoost)</Typography>
                                    </Stack>

                                    <Box sx={{ height: 200, position: 'relative' }}>
                                        <ResponsiveContainer width="100%" height="100%">
                                            <PieChart>
                                                <Pie
                                                    data={[
                                                        { value: score, color: color },
                                                        { value: 100 - score, color: '#e0e0e0' }
                                                    ]}
                                                    cx="50%"
                                                    cy="70%"
                                                    startAngle={180}
                                                    endAngle={0}
                                                    innerRadius={60}
                                                    outerRadius={80}
                                                    dataKey="value"
                                                    paddingAngle={5}
                                                >
                                                    <Cell fill={color} />
                                                    <Cell fill="#e0e0e0" />
                                                </Pie>
                                            </PieChart>
                                        </ResponsiveContainer>
                                        <Box sx={{ position: 'absolute', top: '60%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
                                            <Typography variant="h4" fontWeight="bold" sx={{ color: color }}>
                                                {score}%
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary" fontWeight="bold">
                                                {label}
                                            </Typography>
                                        </Box>
                                    </Box>

                                    <Typography variant="body2" color="text.secondary" display="block" mt={-4} gutterBottom>
                                        {ml.details}
                                    </Typography>

                                    {/* Contributors (Only if present - Old Model) */}
                                    {ml.contributors && ml.contributors.length > 0 && (
                                        <Box mt={2}>
                                            <Typography variant="caption" fontWeight="bold" display="block" mb={1}>RISK FACTORS:</Typography>
                                            <Box display="flex" justifyContent="center" flexWrap="wrap" gap={1}>
                                                {ml.contributors.map((perm, idx) => (
                                                    <Chip
                                                        key={idx}
                                                        label={perm.split('.').pop()}
                                                        size="small"
                                                        color="error"
                                                        variant="outlined"
                                                    />
                                                ))}
                                            </Box>
                                        </Box>
                                    )}
                                </Paper>
                            );
                        })()}

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

                {/* RIGHT COLUMN: FINDINGS */}
                <Grid item xs={12} md={8}>
                    <Stack spacing={3}>

                        {/* 1. MANIFEST ANALYSIS with FIX BUTTONS */}
                        <Card>
                            <CardContent>
                                <Typography variant="h5" style={{ marginBottom: "20px", borderBottom: "2px solid #ecf0f1", paddingBottom: "10px" }}>
                                    1. Manifest Analysis
                                </Typography>
                                <TableContainer>
                                    <Table>
                                        <TableHead style={{ backgroundColor: "#34495e" }}>
                                            <TableRow>
                                                <TableCell style={{ color: "white" }}>Status</TableCell>
                                                <TableCell style={{ color: "white" }}>Check</TableCell>
                                                <TableCell style={{ color: "white" }}>Risk</TableCell>
                                                <TableCell style={{ color: "white" }}>Action</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {[
                                                { label: "Debuggable", val: (apkResult.manifest?.isDebuggable || apkResult.manifest?.is_debuggable || apkResult.manifest?.debuggable), risk: "App is debuggable." },
                                                { label: "Allow Backup", val: (apkResult.manifest?.isAllowBackup || apkResult.manifest?.allow_backup || apkResult.manifest?.allowBackup), risk: "Backups allowed." },
                                                { label: "Cleartext Traffic", val: (apkResult.manifest?.isUsesCleartextTraffic || apkResult.manifest?.uses_cleartext_traffic || apkResult.manifest?.usesCleartextTraffic), risk: "Cleartext allowed." }
                                            ].map((row) => (
                                                <TableRow key={row.label}>
                                                    <TableCell>
                                                        {row.val ? (
                                                            <Chip label="FAIL" style={{ backgroundColor: "#e74c3c", color: "white", fontWeight: "bold" }} />
                                                        ) : (
                                                            <Chip label="PASS" style={{ backgroundColor: "#27ae60", color: "white", fontWeight: "bold" }} />
                                                        )}
                                                    </TableCell>
                                                    <TableCell><strong>{row.label}</strong></TableCell>
                                                    <TableCell>{row.val ? row.risk : "Check passed."}</TableCell>
                                                    <TableCell>
                                                        {row.val ? (
                                                            <Button
                                                                variant="outlined"
                                                                size="small"
                                                                color="primary"
                                                                onClick={() => handleGetFix(row.label, 'FAIL')}
                                                            >
                                                                ⚡ Fix It
                                                            </Button>
                                                        ) : (
                                                            <Typography variant="caption" color="textSecondary">No Actions</Typography>
                                                        )}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </TableContainer>
                            </CardContent>
                        </Card>

                        {/* 2. SECRETS */}
                        <Paper variant="outlined" sx={{ p: 0, borderRadius: 3, overflow: 'hidden' }}>
                            <Box sx={{ p: 2, bgcolor: 'error.lighter', borderBottom: '1px solid', borderColor: 'divider' }}>
                                <Stack direction="row" alignItems="center" spacing={1}>
                                    <ErrorIcon color="error" />
                                    <Typography variant="h6" fontWeight="bold">Hardcoded Secrets ({secretRisks.length})</Typography>
                                </Stack>
                            </Box>
                            <TableContainer>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Type</TableCell>
                                            <TableCell>Evidence</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {secretRisks.length > 0 ? secretRisks.map((row, i) => (
                                            <TableRow key={i} hover>
                                                <TableCell><Chip label="CRITICAL" color="error" size="small" /> {row.desc}</TableCell>
                                                <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{row.match}</TableCell>
                                            </TableRow>
                                        )) : (
                                            <TableRow><TableCell colSpan={2} align="center">No secrets found.</TableCell></TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Paper>

                    </Stack>
                </Grid>
            </Grid>

            {/* FIX SUGGESTION MODAL */}
            <Dialog
                open={fixModalOpen}
                TransitionComponent={Transition}
                keepMounted
                onClose={() => setFixModalOpen(false)}
                aria-describedby="fix-dialog-slide-description"
                maxWidth="md"
                fullWidth
            >
                <DialogTitle sx={{
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1
                }}>
                    <AutoFixHighIcon />
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
                        {currentFix?.title || "Smart Fix Suggestion"}
                    </Typography>
                    <IconButton onClick={() => setFixModalOpen(false)} sx={{ color: 'white' }}>
                        <CloseIcon />
                    </IconButton>
                </DialogTitle>
                <DialogContent sx={{ mt: 2 }}>
                    <Box sx={{ my: 2 }}>
                        <Typography variant="subtitle1" fontWeight="bold" gutterBottom color="primary">
                            Analysis & Recommendation
                        </Typography>
                        <Typography variant="body1" color="text.secondary" paragraph>
                            {currentFix?.explanation}
                        </Typography>

                        <Typography variant="subtitle1" fontWeight="bold" gutterBottom color="primary" sx={{ mt: 3 }}>
                            Recommended Fix
                        </Typography>
                        <Paper variant="outlined" sx={{
                            p: 2,
                            bgcolor: '#1e1e1e',
                            color: '#d4d4d4',
                            fontFamily: 'monospace',
                            borderRadius: 2,
                            position: 'relative',
                            overflow: 'auto'
                        }}>
                            <pre style={{ margin: 0 }}>
                                {currentFix?.codeFix}
                            </pre>
                            <Box sx={{ position: 'absolute', top: 8, right: 8 }}>
                                <Chip
                                    label={currentFix?.type === 'STATIC_RULE' ? "Verified Rule" : "AI Generated"}
                                    color={currentFix?.type === 'STATIC_RULE' ? "success" : "warning"}
                                    size="small"
                                    sx={{ mr: 1, opacity: 0.8 }}
                                />
                            </Box>
                        </Paper>
                    </Box>
                </DialogContent>
                <DialogActions sx={{ p: 2, bgcolor: '#f5f5f5' }}>
                    <Button
                        startIcon={<ContentCopyIcon />}
                        onClick={handleCopyCode}
                        variant="outlined"
                    >
                        Copy Code
                    </Button>
                    <Button
                        onClick={() => setFixModalOpen(false)}
                        variant="contained"
                        color="primary"
                    >
                        Review & Apply
                    </Button>
                </DialogActions>
            </Dialog>

        </Container>
    );
}

// Subcomponents
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
