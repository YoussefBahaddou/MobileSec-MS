
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Typography, Box, Alert, IconButton, InputAdornment, LinearProgress } from '@mui/material';
import { motion } from 'framer-motion';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import LockIcon from '@mui/icons-material/Lock';
import EmailIcon from '@mui/icons-material/Email';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import { CyberInput, CyberButton, GlassCard, GridBackground } from '../components/CyberComponents';

const RegisterPage = () => {
    const { signUp } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState(null);
    const [msg, setMsg] = useState(null);
    const [loading, setLoading] = useState(false);

    // Password Strength Logic
    const getStrength = (pass) => {
        let strength = 0;
        if (pass.length > 5) strength += 25;
        if (pass.length > 8) strength += 25;
        if (/[A-Z]/.test(pass)) strength += 25;
        if (/[0-9]/.test(pass)) strength += 25;
        return strength;
    };

    const strength = getStrength(password);

    const getStrengthColor = (s) => {
        if (s <= 25) return 'error';
        if (s <= 50) return 'warning';
        if (s <= 75) return 'info';
        return 'success';
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            setLoading(false);
            return;
        }

        try {
            const { error, data } = await signUp({ email, password });
            if (error) throw error;
            if (data?.user && data?.session) {
                navigate('/dashboard');
            } else {
                setMsg("Registration Successful! Please check your email to confirm.");
            }
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
                <Box textAlign="center" mb={5}>
                    <motion.div
                        initial={{ scale: 0.8, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ duration: 0.8 }}
                    >
                        <Typography variant="overline" sx={{ color: '#FF6B35', letterSpacing: 4, fontWeight: 'bold' }}>
                            Join The Network
                        </Typography>
                        <Typography variant="h4" fontWeight="800" sx={{ color: 'white', letterSpacing: 2 }}>
                            SYSTEM REGISTRATION
                        </Typography>
                    </motion.div>
                </Box>

                {/* Register Card */}
                <motion.div
                    initial={{ y: 50, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{ duration: 0.5, delay: 0.2 }}
                >
                    <GlassCard>
                        <Box sx={{ textAlign: 'center', mb: 4 }}>
                            <Box
                                sx={{
                                    width: 60,
                                    height: 60,
                                    borderRadius: '50%',
                                    bgcolor: 'rgba(255, 107, 53, 0.1)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    mx: 'auto',
                                    border: '1px solid rgba(255, 107, 53, 0.3)'
                                }}
                            >
                                <HowToRegIcon sx={{ fontSize: 30, color: '#FF6B35' }} />
                            </Box>
                        </Box>

                        {error && <Alert severity="error" variant="filled" sx={{ mb: 3, bgcolor: 'rgba(211, 47, 47, 0.2)' }}>{error}</Alert>}
                        {msg && <Alert severity="success" variant="filled" sx={{ mb: 3 }}>{msg}</Alert>}

                        <Box component="form" onSubmit={handleRegister} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
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

                            <Box>
                                <CyberInput
                                    label="Create Access Code"
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
                                {/* Password Strength Meter */}
                                <Box sx={{ mt: 1, px: 0.5 }}>
                                    <LinearProgress
                                        variant="determinate"
                                        value={strength}
                                        color={getStrengthColor(strength)}
                                        sx={{ height: 4, borderRadius: 2, bgcolor: 'rgba(255,255,255,0.1)' }}
                                    />
                                    <Typography variant="caption" sx={{ color: 'grey.500', display: 'flex', justifyContent: 'flex-end', mt: 0.5 }}>
                                        Strength
                                    </Typography>
                                </Box>
                            </Box>

                            <CyberInput
                                label="Confirm Access Code"
                                type={showPassword ? 'text' : 'password'}
                                fullWidth
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <LockIcon />
                                        </InputAdornment>
                                    ),
                                }}
                            />

                            <CyberButton type="submit" fullWidth disabled={loading}>
                                {loading ? 'Initializing...' : 'Initialize Account'}
                            </CyberButton>
                        </Box>

                        <Box mt={3} textAlign="center">
                            <Typography variant="body2" sx={{ color: 'grey.500' }}>
                                Already Registered?{' '}
                                <Link to="/login" style={{ color: '#FF6B35', textDecoration: 'none', fontWeight: 'bold' }}>
                                    Access System
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

export default RegisterPage;
