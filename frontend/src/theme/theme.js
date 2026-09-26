import { createTheme } from '@mui/material/styles';

// A calm teal + amber identity, distinct from the usual blue SaaS default.
const shared = {
  shape: { borderRadius: 14 },
  typography: {
    fontFamily: '"Plus Jakarta Sans", "Inter", system-ui, sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 700 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiCard: {
      styleOverrides: { root: { borderRadius: 16 } },
    },
    MuiButton: {
      styleOverrides: { root: { borderRadius: 10 } },
    },
  },
};

export const lightTheme = createTheme({
  ...shared,
  palette: {
    mode: 'light',
    primary: { main: '#0f766e' },
    secondary: { main: '#f59e0b' },
    success: { main: '#16a34a' },
    error: { main: '#dc2626' },
    background: { default: '#f4f6f8', paper: '#ffffff' },
  },
});

export const darkTheme = createTheme({
  ...shared,
  palette: {
    mode: 'dark',
    primary: { main: '#2dd4bf' },
    secondary: { main: '#fbbf24' },
    success: { main: '#4ade80' },
    error: { main: '#f87171' },
    background: { default: '#0b1220', paper: '#111a2b' },
  },
});
