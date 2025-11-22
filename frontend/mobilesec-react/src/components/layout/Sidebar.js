import PropTypes from 'prop-types';
import { Fragment } from 'react';
import {
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import TableViewIcon from '@mui/icons-material/TableView';
import DashboardIcon from '@mui/icons-material/Dashboard';
import { Link as RouterLink, useLocation } from 'react-router-dom';

const drawerWidth = 260;

const navItems = [
  {
    label: 'Dashboard',
    to: '/',
    icon: <DashboardIcon fontSize="small" />,
  },
  {
    label: 'Upload APK',
    to: '/upload',
    icon: <CloudUploadIcon fontSize="small" />,
  },
  {
    label: 'Results',
    to: '/results',
    icon: <TableViewIcon fontSize="small" />,
  },
];

function Sidebar({ mobileOpen, onDrawerToggle }) {
  const theme = useTheme();
  const isMdUp = useMediaQuery(theme.breakpoints.up('md'));
  const location = useLocation();

  const drawerContent = (
    <Fragment>
      <Toolbar sx={{ px: 3 }}>
        <Typography variant="h6" fontWeight={700} color="primary.main">
          MobileSec-MS
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {navItems.map((item) => {
          const selected = location.pathname === item.to || location.pathname.startsWith(`${item.to}/`);
          return (
            <ListItemButton
              key={item.to}
              component={RouterLink}
              to={item.to}
              selected={selected}
              onClick={!isMdUp ? onDrawerToggle : undefined}
              sx={{
                borderRadius: 2,
                mx: 1.5,
                my: 0.5,
              }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          );
        })}
      </List>
    </Fragment>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
      aria-label="Navigation"
    >
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
          },
        }}
      >
        {drawerContent}
      </Drawer>
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: drawerWidth,
          },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
}

Sidebar.propTypes = {
  mobileOpen: PropTypes.bool.isRequired,
  onDrawerToggle: PropTypes.func.isRequired,
};

export default Sidebar;
