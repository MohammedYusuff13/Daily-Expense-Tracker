import { useCallback, useEffect, useState } from 'react';
import {
  Box, Typography, Card, CardContent, Grid, TextField, MenuItem, Button,
  Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';

import TransactionTable from '../components/TransactionTable';
import TransactionForm from '../components/TransactionForm';
import { transactionApi, buyerApi } from '../api/client';
import { useDataChanged } from '../hooks/useDataChanged';
import { useToast } from '../context/ToastContext';

const emptyFilters = { q: '', from: '', to: '', buyer: '', type: '' };

export default function Transactions() {
  const { notify } = useToast();
  const [rows, setRows] = useState([]);
  const [buyers, setBuyers] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [editing, setEditing] = useState(null);
  const [confirm, setConfirm] = useState(null);

  const load = useCallback(() => {
    const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v));
    transactionApi.search(params).then(setRows).catch((e) => notify(e.message, 'error'));
  }, [filters, notify]);

  useEffect(() => { buyerApi.list().then(setBuyers).catch(() => {}); }, []);
  useEffect(load, [load]);
  useDataChanged(load);

  const set = (key) => (e) => setFilters((f) => ({ ...f, [key]: e.target.value }));

  const doDelete = async () => {
    try {
      const res = await transactionApi.remove(confirm.id);
      notify(res.message || 'Deleted', 'success');
      setConfirm(null);
      load();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Ledger</Typography>

      <Card elevation={0} sx={{ mb: 2, border: (t) => `1px solid ${t.palette.divider}` }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={3}>
              <TextField label="Search" value={filters.q} onChange={set('q')} fullWidth size="small"
                placeholder="Description or buyer" InputProps={{ startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1 }} /> }} />
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField label="From" type="date" value={filters.from} onChange={set('from')}
                InputLabelProps={{ shrink: true }} fullWidth size="small" />
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField label="To" type="date" value={filters.to} onChange={set('to')}
                InputLabelProps={{ shrink: true }} fullWidth size="small" />
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField select label="Buyer" value={filters.buyer} onChange={set('buyer')} fullWidth size="small">
                <MenuItem value="">All</MenuItem>
                {buyers.map((b) => <MenuItem key={b.name} value={b.name}>{b.name}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6} md={2}>
              <TextField select label="Type" value={filters.type} onChange={set('type')} fullWidth size="small">
                <MenuItem value="">All</MenuItem>
                <MenuItem value="CREDIT">Credit</MenuItem>
                <MenuItem value="DEBIT">Debit</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} md={1}>
              <Button startIcon={<ClearIcon />} onClick={() => setFilters(emptyFilters)} fullWidth>Clear</Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <TransactionTable
        rows={rows}
        onEdit={setEditing}
        onDelete={setConfirm}
      />

      <TransactionForm
        open={Boolean(editing)}
        existing={editing}
        onClose={() => setEditing(null)}
        onSaved={load}
      />

      <Dialog open={Boolean(confirm)} onClose={() => setConfirm(null)}>
        <DialogTitle>Delete transaction?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            This removes the entry from the workbook and updates the buyer balance. This cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirm(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={doDelete}>Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
