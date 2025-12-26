import PropTypes from 'prop-types';
import {
  AppBar,
  Box,
  IconButton,
  Toolbar,
  Typography,
  alpha,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import SecurityIcon from '@mui/icons-material/Security';

function Topbar({ onMenuClick }) {
  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        width: { md: `calc(100% - 260px)` },
        ml: { md: '260px' },
        backgroundColor: (theme) => alpha(theme.palette.background.paper, 0.85),
        backdropFilter: 'blur(8px)',
        borderBottom: (theme) => `1px solid ${alpha(theme.palette.divider, 0.3)}`,
        color: 'text.primary',
      }}
    >
      <Toolbar>
        <IconButton
          color="inherit"
          edge="start"
          onClick={onMenuClick}
          sx={{ mr: 2, display: { md: 'none' } }}
        >
          <MenuIcon />
        </IconButton>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <SecurityIcon color="primary" />
          <Typography variant="h6" fontWeight={600}>
            MobileSec-MS Dashboard
          </Typography>
        </Box>
      </Toolbar>
    </AppBar>
  );
}

Topbar.propTypes = {
  onMenuClick: PropTypes.func.isRequired,
};

export default Topbar;
