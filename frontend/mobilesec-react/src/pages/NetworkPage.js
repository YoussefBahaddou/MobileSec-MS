import React, { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    Paper,
    Button,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Chip,
    Box,
    CircularProgress,
    Alert
} from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import StopIcon from '@mui/icons-material/Stop';
import RefreshIcon from '@mui/icons-material/Refresh';

const NetworkPage = () => {
    const [scanId, setScanId] = useState(null);
    const [isScanning, setIsScanning] = useState(false);
    const [findings, setFindings] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Poll for status or just check local state? Local state is fine for now.
    // Ideally, we'd persist scan ID or fetch "current status" from backend.

    const startScan = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('/api/network/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({})
            }); // Gateway forwards to 8087
            if (!response.ok) {
                const text = await response.text();
                throw new Error(`Failed: ${response.status} ${response.statusText} - ${text}`);
            }
            const data = await response.json();
            setScanId(data.scan_id);
            setIsScanning(true);
        } catch (err) {
            console.error('Scan Error:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const stopScan = async () => {
        setLoading(true);
        try {
            await fetch('/api/network/stop', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({})
            });
            setIsScanning(false);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchResults = async () => {
        if (!scanId) return;
        try {
            const response = await fetch(`/api/network/${scanId}/results`);
            if (!response.ok) throw new Error('Failed to fetch findings');
            const data = await response.json();
            setFindings(data);
        } catch (err) {
            setError(err.message);
        }
    };

    useEffect(() => {
        let interval;
        if (isScanning && scanId) {
            fetchResults();
            interval = setInterval(fetchResults, 2000); // Poll every 2s
        }
        return () => clearInterval(interval);
    }, [isScanning, scanId]);

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom>
                Dynamic Network Inspector
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Paper sx={{ p: 2, mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                <Button
                    variant="contained"
                    color="primary"
                    startIcon={isScanning ? <CircularProgress size={20} color="inherit" /> : <PlayArrowIcon />}
                    onClick={startScan}
                    disabled={isScanning || loading}
                >
                    {isScanning ? 'Scanning...' : 'Start New Scan'}
                </Button>

                <Button
                    variant="contained"
                    color="error"
                    startIcon={<StopIcon />}
                    onClick={stopScan}
                    disabled={!isScanning || loading}
                >
                    Stop Scan
                </Button>

                <Button
                    variant="outlined"
                    startIcon={<RefreshIcon />}
                    onClick={fetchResults}
                    disabled={!scanId}
                >
                    Refresh Results
                </Button>

                {scanId && (
                    <Chip label={`Session ID: ${scanId}`} variant="outlined" />
                )}
            </Paper>

            <Typography variant="h6" gutterBottom>
                Captured Traffic & Findings
            </Typography>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Method</TableCell>
                            <TableCell>URL</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Analysis</TableCell>
                            <TableCell>Timestamp</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {findings.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} align="center">No traffic captured yet. Start a scan and trigger network activity.</TableCell>
                            </TableRow>
                        ) : (
                            findings.map((finding) => (
                                <TableRow key={finding.id}>
                                    <TableCell>
                                        <Chip label={finding.method} size="small" color={finding.method === 'GET' ? 'primary' : 'secondary'} />
                                    </TableCell>
                                    <TableCell sx={{ wordBreak: 'break-all' }}>{finding.url}</TableCell>
                                    <TableCell>{finding.status_code}</TableCell>
                                    <TableCell>
                                        {finding.vulnerability_type ? (
                                            <Chip label={finding.vulnerability_type} color="error" size="small" />
                                        ) : (
                                            <Chip label="Secure" color="success" size="small" />
                                        )}
                                    </TableCell>
                                    <TableCell>{new Date(finding.created_at).toLocaleTimeString()}</TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );
};

export default NetworkPage;
