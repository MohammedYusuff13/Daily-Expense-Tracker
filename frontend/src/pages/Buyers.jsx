import { useCallback, useEffect, useState } from 'react';
import {
  Box, Typography, Grid, Card, CardContent, TextField, Chip, Button, Stack,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import PersonAddIcon from '@mui/icons-material/PersonAddAlt1';
import PhoneIcon from '@mui/icons-material/Phone';
import PlaceIcon from '@mui/icons-material/Place';

import BuyerForm from '../components/BuyerForm';
import { buyerApi } from '../api/client';
import { useDataChanged } from '../hooks/useDataChanged';
import { formatCurrency } from '../utils/format';

export default function Buyers() {
  const [buyers, setBuyers] = useState([]);
  const [query, setQuery] = useState('');
  const [formOpen, setFormOpen] = useState(false);

  const load = useCallback(() => {
    buyerApi.search(query).then(setBuyers).catch(() => setBuyers([]));
  }, [query]);

  useEffect(load, [load]);
  useDataChanged(load);

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }} flexWrap="wrap" gap={1}>
        <Typography variant="h4">Buyers</Typography>
        <Button variant="contained" startIcon={<PersonAddIcon />} onClick={() => setFormOpen(true)}>
          New buyer
        </Button>
      </Stack>

      <TextField
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Search by name or mobile"
        fullWidth size="small" sx={{ mb: 2 }}
        InputProps={{ startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1 }} /> }}
      />

      <Grid container spacing={2}>
        {buyers.map((b) => {
          const owes = b.currentBalance > 0;
          return (
            <Grid item xs={12} sm={6} md={4} key={b.name}>
              <Card elevation={0} sx={{ height: '100%', border: (t) => `1px solid ${t.palette.divider}` }}>
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                    <Typography variant="h6">{b.name}</Typography>
                    <Chip
                      size="small"
                      color={owes ? 'warning' : 'success'}
                      label={owes ? 'Outstanding' : 'Settled'}
                    />
                  </Stack>
                  {b.mobile && (
                    <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', gap: 0.5, mt: 0.5 }}>
                      <PhoneIcon fontSize="inherit" /> {b.mobile}
                    </Typography>
                  )}
                  {b.address && (
                    <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', gap: 0.5 }}>
                      <PlaceIcon fontSize="inherit" /> {b.address}
                    </Typography>
                  )}
                  <Box sx={{ mt: 1.5 }}>
                    <Typography variant="caption" color="text.secondary">Current balance</Typography>
                    <Typography variant="h6" color={owes ? 'warning.main' : 'text.primary'}>
                      {formatCurrency(b.currentBalance)}
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
        {!buyers.length && (
          <Grid item xs={12}>
            <Typography color="text.secondary" sx={{ py: 6, textAlign: 'center' }}>
              No buyers yet. Add your first buyer to give them a dedicated ledger.
            </Typography>
          </Grid>
        )}
      </Grid>

      <BuyerForm open={formOpen} onClose={() => setFormOpen(false)} onSaved={load} />
    </Box>
  );
}
