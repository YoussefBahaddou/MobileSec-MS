
import { styled } from '@mui/material/styles';
import { TextField, Button, Box } from '@mui/material';

// --- Theme Constants ---
const PRIMARY_COLOR = '#FF6B35'; // Neon Orange
const PRIMARY_GLOW = 'rgba(255, 107, 53, 0.5)';
const BG_DARK = '#0a0a0a';
const CARD_BG = 'rgba(20, 20, 20, 0.6)';

// --- Styled Components ---

export const CyberInput = styled(TextField)({
    '& .MuiOutlinedInput-root': {
        backgroundColor: 'rgba(0, 0, 0, 0.3)',
        borderRadius: '4px',
        color: '#e0e0e0',
        transition: 'all 0.3s ease-in-out',
        '& fieldset': {
            borderColor: 'rgba(255, 255, 255, 0.1)',
        },
        '&:hover fieldset': {
            borderColor: 'rgba(255, 107, 53, 0.3)',
        },
        '&.Mui-focused': {
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            boxShadow: `0 0 15px ${PRIMARY_GLOW}`,
            '& fieldset': {
                borderColor: PRIMARY_COLOR,
                borderWidth: '1px',
            },
        },
    },
    '& .MuiInputLabel-root': {
        color: '#888',
        '&.Mui-focused': {
            color: PRIMARY_COLOR,
        },
    },
    '& .MuiInputAdornment-root': {
        color: '#666',
    }
});

export const CyberButton = styled(Button)({
    background: `linear-gradient(90deg, ${PRIMARY_COLOR} 0%, #FF8E53 100%)`,
    border: 0,
    borderRadius: 4,
    boxShadow: `0 0 20px ${PRIMARY_GLOW}`,
    color: '#000',
    height: 48,
    padding: '0 30px',
    fontWeight: 700,
    letterSpacing: '1px',
    textTransform: 'uppercase',
    transition: 'all 0.3s ease',
    '&:hover': {
        background: `linear-gradient(90deg, #FF8E53 0%, ${PRIMARY_COLOR} 100%)`,
        boxShadow: `0 0 30px ${PRIMARY_GLOW}, 0 0 10px ${PRIMARY_COLOR}`,
        transform: 'translateY(-2px)',
    },
});

export const GlassCard = styled(Box)({
    background: CARD_BG,
    backdropFilter: 'blur(20px)',
    border: '1px solid rgba(255, 255, 255, 0.05)',
    borderRadius: '16px',
    padding: '40px',
    boxShadow: '0 20px 50px rgba(0,0,0,0.5)',
    position: 'relative',
    overflow: 'hidden',
    '&::before': {
        content: '""',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        height: '2px',
        background: `linear-gradient(90deg, transparent, ${PRIMARY_COLOR}, transparent)`,
        opacity: 0.8,
    }
});

export const GridBackground = styled(Box)({
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    zIndex: -1,
    backgroundColor: BG_DARK,
    backgroundImage: `
        linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px)
    `,
    backgroundSize: '40px 40px',
    '&::after': {
        content: '""',
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        background: 'radial-gradient(circle at 50% 50%, rgba(255, 107, 53, 0.1), transparent 70%)',
    }
});
