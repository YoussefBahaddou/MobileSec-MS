import React, { useEffect, useState } from 'react';
import {
    Box,
    Card,
    CardContent,
    Container,
    Grid,
    IconButton,
    Paper,
    Stack,
    Typography,
    useTheme,
    Button,
    Chip,
    CircularProgress
} from '@mui/material';
import {
    Security as SecurityIcon,
    BugReport as BugReportIcon,
    Warning as WarningIcon,
    CheckCircle as CheckCircleIcon,
    ArrowForward as ArrowForwardIcon,
    CloudUpload as CloudUploadIcon,
    Assessment as AssessmentIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { getDashboardStats } from '../services/api';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

const StatCard = ({ title, value, icon, color, delay }) => (
    <Card
        elevation={0}
        component={motion.div}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay }}
        sx={{
            height: '100%',
            borderRadius: 4,
            border: '1px solid',
            borderColor: 'divider',
            background: `linear-gradient(135deg, #fff 0%, ${color}10 100%)`,
            position: 'relative',
            overflow: 'hidden'
        }}
    >
        <Box
            sx={{
                position: 'absolute',
                right: -20,
                top: -20,
                opacity: 0.1,
                color: color,
                transform: 'rotate(15deg)'
            }}
        >
            {React.cloneElement(icon, { sx: { fontSize: 120 } })}
        </Box>
        <CardContent sx={{ position: 'relative', zIndex: 1 }}>
            <Stack spacing={2}>
                <Box
                    sx={{
                        width: 48,
                        height: 48,
                        borderRadius: 3,
                        bgcolor: `${color}20`,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: color
                    }}
                >
                    {icon}
                </Box>
                <Box>
                    <Typography variant="h3" fontWeight="700" color="text.primary">
                        {value}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" fontWeight="500">
                        {title}
                    </Typography>
                </Box>
            </Stack>
        </CardContent>
    </Card>
);

const DashboardPage = () => {
    const theme = useTheme();
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await getDashboardStats();
                console.log('Dashboard stats received:', data);
                if (data) {
                    setStats(data);
                } else {
                    setError('No data received from server.');
                }
            } catch (error) {
                console.error('Failed to fetch dashboard stats', error);
                setError('Failed to load dashboard data. Please try again later.');
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Container maxWidth="md" sx={{ py: 8 }}>
                <Paper elevation={0} sx={{ p: 4, textAlign: 'center', borderRadius: 4, border: '1px solid', borderColor: 'error.light', bgcolor: '#FFF5F5' }}>
                    <Typography variant="h5" color="error" gutterBottom fontWeight="700">
                        Oops! Something went wrong.
                    </Typography>
                    <Typography variant="body1" color="text.secondary" paragraph>
                        {error}
                    </Typography>
                    <Button variant="outlined" color="error" onClick={() => window.location.reload()}>
                        Retry
                    </Button>
                </Paper>
            </Container>
        );
    }

    if (!stats) return null;

    const safeStats = {
        totalScans: stats.totalScans || 0,
        highRiskCount: stats.highRiskCount || 0,
        mediumRiskCount: stats.mediumRiskCount || 0,
        lowRiskCount: stats.lowRiskCount || 0,
        recentScans: Array.isArray(stats.recentScans) ? stats.recentScans : []
    };

    const pieData = [
        { name: 'High Risk', value: safeStats.highRiskCount, color: theme.palette.error.main },
        { name: 'Medium Risk', value: safeStats.mediumRiskCount, color: theme.palette.warning.main },
        { name: 'Low Risk', value: safeStats.lowRiskCount, color: theme.palette.success.main },
    ].filter(d => d.value > 0);

    return (
        <Container maxWidth="xl">
            <Stack spacing={4}>
                {/* Header */}
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                    <Box>
                        <Typography variant="h4" fontWeight="800" gutterBottom>
                            Security Overview
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            Welcome back! Here's what's happening with your mobile apps.
                        </Typography>
                    </Box>
                    <Button
                        variant="contained"
                        size="large"
                        startIcon={<CloudUploadIcon />}
                        onClick={() => navigate('/upload')}
                        sx={{ borderRadius: 3, px: 4, py: 1.5, textTransform: 'none', fontWeight: 600 }}
                    >
                        New Scan
                    </Button>
                </Stack>

                {/* Stats Grid */}
                <Grid container spacing={3}>
                    <Grid item xs={12} sm={6} md={3}>
                        <StatCard
                            title="Total Scans"
                            value={safeStats.totalScans}
                            icon={<AssessmentIcon />}
                            color={theme.palette.primary.main}
                            delay={0.1}
                        />
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                        <StatCard
                            title="High Risk Apps"
                            value={safeStats.highRiskCount}
                            icon={<BugReportIcon />}
                            color={theme.palette.error.main}
                            delay={0.2}
                        />
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                        <StatCard
                            title="Medium Risk Apps"
                            value={safeStats.mediumRiskCount}
                            icon={<WarningIcon />}
                            color={theme.palette.warning.main}
                            delay={0.3}
                        />
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                        <StatCard
                            title="Low Risk Apps"
                            value={safeStats.lowRiskCount}
                            icon={<CheckCircleIcon />}
                            color={theme.palette.success.main}
                            delay={0.4}
                        />
                    </Grid>
                </Grid>

                <Grid container spacing={3}>
                    {/* Recent Activity */}
                    <Grid item xs={12} md={8}>
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                borderRadius: 4,
                                border: '1px solid',
                                borderColor: 'divider',
                                height: '100%'
                            }}
                        >
                            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
                                <Typography variant="h6" fontWeight="700">
                                    Recent Scans
                                </Typography>
                                <Button
                                    endIcon={<ArrowForwardIcon />}
                                    onClick={() => navigate('/results')}
                                    sx={{ textTransform: 'none' }}
                                >
                                    View All
                                </Button>
                            </Stack>
                            <Stack spacing={2}>
                                {safeStats.recentScans.length > 0 ? (
                                    safeStats.recentScans.map((scan) => (
                                        <Paper
                                            key={scan.id}
                                            elevation={0}
                                            onClick={() => navigate(`/results/${scan.id}`)}
                                            sx={{
                                                p: 2,
                                                bgcolor: 'grey.50',
                                                borderRadius: 3,
                                                cursor: 'pointer',
                                                transition: 'all 0.2s',
                                                '&:hover': {
                                                    bgcolor: 'grey.100',
                                                    transform: 'translateX(4px)'
                                                }
                                            }}
                                        >
                                            <Stack direction="row" justifyContent="space-between" alignItems="center">
                                                <Stack direction="row" spacing={2} alignItems="center">
                                                    <Box
                                                        sx={{
                                                            width: 40,
                                                            height: 40,
                                                            borderRadius: 2,
                                                            bgcolor: 'white',
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            justifyContent: 'center',
                                                            boxShadow: 1
                                                        }}
                                                    >
                                                        <SecurityIcon color="primary" />
                                                    </Box>
                                                    <Box>
                                                        <Typography variant="subtitle2" fontWeight="600">
                                                            {scan.packageName}
                                                        </Typography>
                                                        <Typography variant="caption" color="text.secondary">
                                                            {new Date(scan.createdAt).toLocaleDateString()} • v{scan.versionName}
                                                        </Typography>
                                                    </Box>
                                                </Stack>
                                                <Chip
                                                    label={scan.riskLevel}
                                                    size="small"
                                                    color={
                                                        scan.riskLevel === 'HIGH' || scan.riskLevel === 'CRITICAL' ? 'error' :
                                                            scan.riskLevel === 'MEDIUM' ? 'warning' : 'success'
                                                    }
                                                    sx={{ fontWeight: 600, borderRadius: 2 }}
                                                />
                                            </Stack>
                                        </Paper>
                                    ))
                                ) : (
                                    <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 4 }}>
                                        No recent scans found.
                                    </Typography>
                                )}
                            </Stack>
                        </Paper>
                    </Grid>

                    {/* Risk Distribution Chart */}
                    <Grid item xs={12} md={4}>
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                borderRadius: 4,
                                border: '1px solid',
                                borderColor: 'divider',
                                height: '100%',
                                display: 'flex',
                                flexDirection: 'column'
                            }}
                        >
                            <Typography variant="h6" fontWeight="700" mb={3}>
                                Risk Distribution
                            </Typography>
                            <Box sx={{ flexGrow: 1, minHeight: 300, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                {pieData.length > 0 ? (
                                    <ResponsiveContainer width="100%" height="100%">
                                        <PieChart>
                                            <Pie
                                                data={pieData}
                                                innerRadius={60}
                                                outerRadius={80}
                                                paddingAngle={5}
                                                dataKey="value"
                                            >
                                                {pieData.map((entry, index) => (
                                                    <Cell key={`cell-${index}`} fill={entry.color} />
                                                ))}
                                            </Pie>
                                            <Tooltip />
                                            <Legend verticalAlign="bottom" height={36} />
                                        </PieChart>
                                    </ResponsiveContainer>
                                ) : (
                                    <Typography variant="body2" color="text.secondary">
                                        No risk data available to display.
                                    </Typography>
                                )}
                            </Box>
                        </Paper>
                    </Grid>
                </Grid>
            </Stack>
        </Container>
    );
};

export default DashboardPage;
