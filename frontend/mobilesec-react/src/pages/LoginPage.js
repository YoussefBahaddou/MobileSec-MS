
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Typography, Box, Alert, IconButton, InputAdornment } from '@mui/material';
import { motion } from 'framer-motion';
import SecurityIcon from '@mui/icons-material/Security';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import LockIcon from '@mui/icons-material/Lock';
import EmailIcon from '@mui/icons-material/Email';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import { CyberInput, CyberButton, GlassCard, GridBackground } from '../components/CyberComponents';

const LoginPage = () => {
    const { signIn } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            const { error } = await signIn({ email, password });
            if (error) throw error;
            navigate('/dashboard');
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative', overflow: 'hidden' }}>
            <GridBackground />

            <Container maxWidth="xs" sx={{ position: 'relative', zIndex: 2 }}>

                {/* Branding Header */}
                <Box textAlign="center" mb={6}>
                    <motion.div
                        initial={{ scale: 0.8, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ duration: 0.8, ease: "easeOut" }}
                    >
                        <Box sx={{ position: 'relative', display: 'inline-block' }}>
                            <Box
                                component={motion.div}
                                animate={{
                                    boxShadow: ['0 0 0px rgba(255, 107, 53, 0)', '0 0 30px rgba(255, 107, 53, 0.6)', '0 0 0px rgba(255, 107, 53, 0)']
                                }}
                                transition={{ duration: 2, repeat: Infinity }}
                                sx={{
                                    width: 70,
                                    height: 70,
                                    borderRadius: '50%',
                                    bgcolor: 'rgba(255, 107, 53, 0.1)',
                                    border: '1px solid #FF6B35',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    mx: 'auto',
                                    mb: 2
                                }}
                            >
                                <SecurityIcon sx={{ fontSize: 36, color: '#FF6B35' }} />
                            </Box>
                        </Box>

                        <Typography variant="h3" fontWeight="800" sx={{ color: 'white', letterSpacing: 3, textTransform: 'uppercase' }}>
                            MobileSec
                        </Typography>
                        <Typography variant="overline" sx={{ color: '#FF6B35', letterSpacing: 4, fontWeight: 'bold' }}>
                            DevSecOps Suite
                        </Typography>
                    </motion.div>
                </Box>

                {/* Login Card */}
                <motion.div
                    initial={{ y: 50, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                >
                    <GlassCard>
                        <Typography variant="h5" fontWeight="600" align="center" sx={{ color: 'white', mb: 1 }}>
                            System Access
                        </Typography>
                        <Typography variant="body2" align="center" sx={{ color: 'grey.500', mb: 4 }}>
                            Enter your credentials to proceed
                        </Typography>

                        {error && (
                            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                                <Alert severity="error" variant="filled" sx={{ mb: 3, bgcolor: 'rgba(211, 47, 47, 0.2)', border: '1px solid #d32f2f' }}>{error}</Alert>
                            </motion.div>
                        )}

                        <Box component="form" onSubmit={handleLogin} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                            <CyberInput
                                label="Email Identity"
                                type="email"
                                fullWidth
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <EmailIcon />
                                        </InputAdornment>
                                    ),
                                }}
                            />

                            <CyberInput
                                label="Access Code"
                                type={showPassword ? 'text' : 'password'}
                                fullWidth
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <LockIcon />
                                        </InputAdornment>
                                    ),
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" sx={{ color: 'grey.500' }}>
                                                {showPassword ? <VisibilityOff /> : <Visibility />}
                                            </IconButton>
                                        </InputAdornment>
                                    ),
                                }}
                            />

                            <CyberButton type="submit" fullWidth disabled={loading}>
                                {loading ? 'Authenticating...' : 'Sign In'}
                            </CyberButton>
                        </Box>

                        <Box mt={3} textAlign="center">
                            <Typography variant="body2" sx={{ color: 'grey.500' }}>
                                New User?{' '}
                                <Link to="/register" style={{ color: '#FF6B35', textDecoration: 'none', fontWeight: 'bold' }}>
                                    Initialize Account
                                </Link>
                            </Typography>
                        </Box>
                    </GlassCard>
                </motion.div>

                {/* Footer Badge */}
                <Box mt={4} textAlign="center" display="flex" justifyContent="center" alignItems="center" gap={1}>
                    <VerifiedUserIcon sx={{ color: '#00C853', fontSize: 16 }} />
                    <Typography variant="caption" sx={{ color: 'grey.600', letterSpacing: 1 }}>
                        END-TO-END ENCRYPTED SESSION
                    </Typography>
                </Box>

            </Container>
        </Box>
    );
};

export default LoginPage;
