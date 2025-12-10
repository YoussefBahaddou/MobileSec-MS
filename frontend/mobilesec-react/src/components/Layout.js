import React from 'react';
import { Box, Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography, useTheme } from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SecurityIcon from '@mui/icons-material/Security';
import IntegrationInstructionsIcon from '@mui/icons-material/IntegrationInstructions';
import WifiIcon from '@mui/icons-material/Wifi';
import { useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

const drawerWidth = 260;

const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
    { text: 'Upload APK', icon: <CloudUploadIcon />, path: '/upload' },
    { text: 'Analysis Results', icon: <AssessmentIcon />, path: '/results' },
    { text: 'CI/CD Integration', icon: <IntegrationInstructionsIcon />, path: '/ci-integration' },
    { text: 'Network Inspector', icon: <WifiIcon />, path: '/network' },
];

const Layout = ({ children }) => {
    const theme = useTheme();
    const navigate = useNavigate();
    const location = useLocation();

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                        borderRight: 'none',
                        bgcolor: 'secondary.main',
                        color: 'white',
                    },
                }}
            >
                <Box sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                    <SecurityIcon sx={{ color: 'primary.main', fontSize: 32 }} />
                    <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: 1 }}>
                        MobileSec
                    </Typography>
                </Box>

                <List sx={{ px: 2 }}>
                    {menuItems.map((item) => {
                        const active = location.pathname === item.path || (item.path === '/results' && location.pathname.startsWith('/results'));
                        return (
                            <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
                                <ListItemButton
                                    onClick={() => navigate(item.path)}
                                    sx={{
                                        borderRadius: 2,
                                        bgcolor: active ? 'rgba(255, 107, 53, 0.15)' : 'transparent',
                                        color: active ? 'primary.main' : 'grey.400',
                                        '&:hover': {
                                            bgcolor: 'rgba(255, 255, 255, 0.05)',
                                            color: 'white',
                                        },
                                    }}
                                >
                                    <ListItemIcon sx={{ color: 'inherit', minWidth: 40 }}>
                                        {item.icon}
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={item.text}
                                        primaryTypographyProps={{ fontWeight: active ? 600 : 400 }}
                                    />
                                    {active && (
                                        <Box
                                            component={motion.div}
                                            layoutId="activeIndicator"
                                            sx={{ width: 4, height: 4, borderRadius: '50%', bgcolor: 'primary.main' }}
                                        />
                                    )}
                                </ListItemButton>
                            </ListItem>
                        );
                    })}
                </List>

                <Box sx={{ mt: 'auto', p: 3 }}>
                    <Typography variant="caption" sx={{ color: 'grey.600' }}>
                        v1.0.0 • MobileSec-MS
                    </Typography>
                </Box>
            </Drawer>

            <Box component="main" sx={{ flexGrow: 1, p: 4, width: `calc(100% - ${drawerWidth}px)` }}>
                <motion.div
                    key={location.pathname}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, ease: "easeOut" }}
                >
                    {children}
                </motion.div>
            </Box>
        </Box>
    );
};

export default Layout;
