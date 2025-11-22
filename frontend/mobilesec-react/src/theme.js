import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#FF6B35', // Vibrant Orange
      light: '#FF9B74',
      dark: '#C43E00',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#2D3142', // Dark Grey/Navy
      light: '#585B6B',
      dark: '#00081B',
      contrastText: '#FFFFFF',
    },
    background: {
      default: '#F4F5F7', // Soft Grey/White
      paper: '#FFFFFF',
    },
    text: {
      primary: '#2D3142',
      secondary: '#777E90',
    },
    success: {
      main: '#00BFA6',
    },
    warning: {
      main: '#FFC107',
    },
    error: {
      main: '#FF4D4D',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: { fontWeight: 700 },
    h2: { fontWeight: 700 },
    h3: { fontWeight: 700 },
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0px 4px 12px rgba(255, 107, 53, 0.2)',
          },
        },
        containedPrimary: {
          background: 'linear-gradient(45deg, #FF6B35 30%, #FF8E53 90%)',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0px 4px 20px rgba(0, 0, 0, 0.05)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          borderRadius: 8,
        },
      },
    },
  },
});

export default theme;
