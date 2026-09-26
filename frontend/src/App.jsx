import { useState } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar, Toolbar, Typography, IconButton, Box, Container,
  BottomNavigation, BottomNavigationAction, Paper, Fab, useMediaQuery, useTheme,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/SpaceDashboard';
import ReceiptIcon from '@mui/icons-material/ReceiptLong';
import PeopleIcon from '@mui/icons-material/Groups';
import AssessmentIcon from '@mui/icons-material/Assessment';
import AccountBalanceIcon from '@mui/icons-material/AccountBalanceWallet';
import AddIcon from '@mui/icons-material/Add';
import LightModeIcon from '@mui/icons-material/LightMode';
import DarkModeIcon from '@mui/icons-material/DarkMode';

import { useColorMode } from './context/ColorModeContext';
import Dashboard from './pages/Dashboard';
import Transactions from './pages/Transactions';
import Buyers from './pages/Buyers';
import BalanceSheet from './pages/BalanceSheet';
import Reports from './pages/Reports';
import AddSpeedDial from './components/AddSpeedDial';

const NAV = [
  { label: 'Home', icon: <DashboardIcon />, path: '/' },
  { label: 'Ledger', icon: <ReceiptIcon />, path: '/transactions' },
  { label: 'Buyers', icon: <PeopleIcon />, path: '/buyers' },
  { label: 'Balance', icon: <AccountBalanceIcon />, path: '/balance-sheet' },
  { label: 'Reports', icon: <AssessmentIcon />, path: '/reports' },
];

export default function App() {
  const { mode, toggle } = useColorMode();
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [dialogOpen, setDialogOpen] = useState(false);

  const currentIndex = Math.max(0, NAV.findIndex((n) => n.path === location.pathname));

  return (
    <Box sx={{ minHeight: '100vh', pb: isMobile ? 9 : 0 }}>
      <AppBar position="sticky" elevation={0} color="primary">
        <Toolbar>
          <AccountBalanceIcon sx={{ mr: 1.5 }} />
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 800 }}>
            ShopBook
          </Typography>
          {!isMobile &&
            NAV.map((n) => (
              <Typography
                key={n.path}
                onClick={() => navigate(n.path)}
                sx={{
                  mx: 1.5, cursor: 'pointer', fontWeight: 600, opacity: 0.9,
                  borderBottom: location.pathname === n.path ? '2px solid' : '2px solid transparent',
                }}
              >
                {n.label}
              </Typography>
            ))}
          <IconButton color="inherit" onClick={toggle} aria-label="Toggle color mode">
            {mode === 'light' ? <DarkModeIcon /> : <LightModeIcon />}
          </IconButton>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 3 }}>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/transactions" element={<Transactions />} />
          <Route path="/buyers" element={<Buyers />} />
          <Route path="/balance-sheet" element={<BalanceSheet />} />
          <Route path="/reports" element={<Reports />} />
        </Routes>
      </Container>

      {/* Floating add button, centered above the bottom nav on mobile. */}
      <Fab
        color="secondary"
        aria-label="Add entry"
        onClick={() => setDialogOpen(true)}
        sx={{
          position: 'fixed',
          bottom: isMobile ? 72 : 32,
          left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 1200,
          boxShadow: 6,
        }}
      >
        <AddIcon />
      </Fab>

      <AddSpeedDial open={dialogOpen} onClose={() => setDialogOpen(false)} />

      {isMobile && (
        <Paper
          elevation={8}
          sx={{ position: 'fixed', bottom: 0, left: 0, right: 0, zIndex: 1100 }}
        >
          <BottomNavigation
            showLabels
            value={currentIndex}
            onChange={(_, idx) => navigate(NAV[idx].path)}
          >
            {NAV.map((n) => (
              <BottomNavigationAction key={n.path} label={n.label} icon={n.icon} />
            ))}
          </BottomNavigation>
        </Paper>
      )}
    </Box>
  );
}
