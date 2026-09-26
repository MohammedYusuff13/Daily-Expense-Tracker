import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Box, Typography, Tabs, Tab, Card, CardContent, Grid, TextField, MenuItem,
  Stack, Button, Chip, Divider, Table, TableBody, TableCell, TableHead, TableRow,
} from '@mui/material';
import BackupIcon from '@mui/icons-material/Backup';
import UploadIcon from '@mui/icons-material/UploadFile';
import DownloadIcon from '@mui/icons-material/Download';

import TransactionTable from '../components/TransactionTable';
import { reportApi, buyerApi, dataApi } from '../api/client';
import { useToast } from '../context/ToastContext';
import { formatCurrency } from '../utils/format';

const TABS = ['Daily', 'Weekly', 'Monthly', 'Buyer-wise', 'Outstanding'];

export default function Reports() {
  const { notify } = useToast();
  const [tab, setTab] = useState(0);
  const [report, setReport] = useState(null);
  const [outstanding, setOutstanding] = useState([]);
  const [buyers, setBuyers] = useState([]);
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [month, setMonth] = useState(new Date().toISOString().slice(0, 7));
  const [buyer, setBuyer] = useState('');
  const fileRef = useRef();

  useEffect(() => { buyerApi.list().then(setBuyers).catch(() => {}); }, []);

  const load = useCallback(() => {
    if (tab === 0) reportApi.daily(date).then(setReport).catch((e) => notify(e.message, 'error'));
    else if (tab === 1) reportApi.weekly(date).then(setReport).catch((e) => notify(e.message, 'error'));
    else if (tab === 2) reportApi.monthly(month).then(setReport).catch((e) => notify(e.message, 'error'));
    else if (tab === 3 && buyer) reportApi.buyer(buyer).then(setReport).catch((e) => notify(e.message, 'error'));
    else if (tab === 4) reportApi.outstanding().then(setOutstanding).catch((e) => notify(e.message, 'error'));
  }, [tab, date, month, buyer, notify]);

  useEffect(load, [load]);

  const doBackup = async () => {
    try { const r = await dataApi.backup(); notify(r.message, 'success'); }
    catch (e) { notify(e.message, 'error'); }
  };

  const doImport = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const r = await dataApi.importFile(file);
      notify(r.message, 'success');
      window.dispatchEvent(new Event('data-changed'));
      load();
    } catch (err) {
      notify(err.message, 'error');
    } finally {
      e.target.value = '';
    }
  };

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={1} sx={{ mb: 2 }}>
        <Typography variant="h4">Reports</Typography>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<BackupIcon />} onClick={doBackup}>Backup</Button>
          <Button variant="outlined" startIcon={<UploadIcon />} onClick={() => fileRef.current.click()}>Import</Button>
          <Button variant="outlined" startIcon={<DownloadIcon />} href={dataApi.exportUrl}>Export</Button>
          <input ref={fileRef} type="file" accept=".xlsx" hidden onChange={doImport} />
        </Stack>
      </Stack>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" sx={{ mb: 2 }}>
        {TABS.map((t) => <Tab key={t} label={t} />)}
      </Tabs>

      <Card elevation={0} sx={{ mb: 2, border: (t) => `1px solid ${t.palette.divider}` }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            {(tab === 0 || tab === 1) && (
              <Grid item xs={12} sm={4}>
                <TextField type="date" label={tab === 1 ? 'Any day in week' : 'Date'} value={date}
                  onChange={(e) => setDate(e.target.value)} InputLabelProps={{ shrink: true }} fullWidth size="small" />
              </Grid>
            )}
            {tab === 2 && (
              <Grid item xs={12} sm={4}>
                <TextField type="month" label="Month" value={month}
                  onChange={(e) => setMonth(e.target.value)} InputLabelProps={{ shrink: true }} fullWidth size="small" />
              </Grid>
            )}
            {tab === 3 && (
              <Grid item xs={12} sm={4}>
                <TextField select label="Buyer" value={buyer} onChange={(e) => setBuyer(e.target.value)} fullWidth size="small">
                  <MenuItem value="">Select a buyer</MenuItem>
                  {buyers.map((b) => <MenuItem key={b.name} value={b.name}>{b.name}</MenuItem>)}
                </TextField>
              </Grid>
            )}
          </Grid>
        </CardContent>
      </Card>

      {tab === 4 ? (
        <OutstandingTable rows={outstanding} />
      ) : report ? (
        <Box>
          <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" gap={1}>
            <Chip color="success" label={`Income ${formatCurrency(report.totalIncome)}`} />
            <Chip color="error" label={`Expense ${formatCurrency(report.totalExpense)}`} />
            <Chip color="primary" label={`Net ${formatCurrency(report.net)}`} />
            <Chip variant="outlined" label={report.period} />
          </Stack>
          <TransactionTable rows={report.transactions} compact />
        </Box>
      ) : (
        <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
          {tab === 3 ? 'Pick a buyer to see their report.' : 'No data for this period.'}
        </Typography>
      )}
    </Box>
  );
}

function OutstandingTable({ rows }) {
  if (!rows.length) {
    return <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>Everyone is settled up.</Typography>;
  }
  const total = rows.reduce((s, r) => s + r.outstanding, 0);
  return (
    <Card elevation={0} sx={{ border: (t) => `1px solid ${t.palette.divider}` }}>
      <CardContent>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Buyer</TableCell>
              <TableCell>Mobile</TableCell>
              <TableCell align="right">Outstanding</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((r) => (
              <TableRow key={r.buyerName} hover>
                <TableCell>{r.buyerName}</TableCell>
                <TableCell>{r.mobile || '—'}</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700, color: 'warning.main' }}>
                  {formatCurrency(r.outstanding)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <Divider sx={{ my: 1 }} />
        <Stack direction="row" justifyContent="space-between">
          <Typography variant="h6">Total outstanding</Typography>
          <Typography variant="h6" color="warning.main">{formatCurrency(total)}</Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}
