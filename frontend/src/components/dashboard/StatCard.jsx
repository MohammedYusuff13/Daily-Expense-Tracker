import { Card, CardContent, Box, Typography, alpha } from '@mui/material';

/**
 * Colored summary card. `accent` is any theme/hex color; the card paints a
 * soft tint of it as the background and a solid chip behind the icon.
 */
export default function StatCard({ label, value, icon, accent = '#0f766e' }) {
  return (
    <Card
      sx={{
        height: '100%',
        background: (t) =>
          `linear-gradient(135deg, ${alpha(accent, t.palette.mode === 'dark' ? 0.25 : 0.12)}, ${alpha(
            accent,
            t.palette.mode === 'dark' ? 0.08 : 0.04
          )})`,
        border: (t) => `1px solid ${alpha(accent, 0.25)}`,
      }}
      elevation={0}
    >
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box
          sx={{
            width: 48, height: 48, borderRadius: 2.5, flexShrink: 0,
            display: 'grid', placeItems: 'center',
            bgcolor: accent, color: '#fff',
          }}
        >
          {icon}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="body2" color="text.secondary" noWrap>
            {label}
          </Typography>
          <Typography variant="h5" sx={{ fontWeight: 800 }} noWrap>
            {value}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
}
