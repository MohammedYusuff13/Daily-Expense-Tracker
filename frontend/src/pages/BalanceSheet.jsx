import { useCallback, useEffect, useState } from 'react';
import {
  Box, Typography, Card, CardContent, Grid, Button, Stack, Divider, CircularProgress,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import GridOnIcon from '@mui/icons-material/GridOn';

import { balanceSheetApi } from '../api/client';
import { useDataChanged } from '../hooks/useDataChanged';
import { formatCurrency, formatDate } from '../utils/format';

export default function BalanceSheet() {
  const [bs, setBs] = useState(null);

  const load = useCallback(() => { balanceSheetApi.get().then(setBs); }, []);
  useEffect(load, [load]);
  useDataChanged(load);

  if (!bs) return <Box sx={{ display: 'grid', placeItems: 'center', py: 10 }}><CircularProgress /></Box>;

  const lines = [
    { label: 'Opening balance', value: bs.openingBalance },
    { label: 'Total income', value: bs.totalIncome, color: 'success.main' },
    { label: 'Total expense', value: bs.totalExpense, color: 'error.main' },
    { label: 'Outstanding credit', value: bs.outstandingCredit, color: 'warning.main' },
  ];

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={1} sx={{ mb: 2 }}>
        <Typography variant="h4">Balance sheet</Typography>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<GridOnIcon />} href={balanceSheetApi.exportExcelUrl}>
            Excel
          </Button>
          <Button variant="outlined" startIcon={<PictureAsPdfIcon />} href={balanceSheetApi.exportPdfUrl}>
            PDF
          </Button>
        </Stack>
      </Stack>

      <Card elevation={0} sx={{ maxWidth: 560, border: (t) => `1px solid ${t.palette.divider}` }}>
        <CardContent>
          <Typography variant="caption" color="text.secondary">
            Generated {formatDate(bs.generatedOn)}
          </Typography>
          <Box sx={{ mt: 2 }}>
            {lines.map((l) => (
              <Stack key={l.label} direction="row" justifyContent="space-between" sx={{ py: 1 }}>
                <Typography color="text.secondary">{l.label}</Typography>
                <Typography sx={{ fontWeight: 600, color: l.color }}>{formatCurrency(l.value)}</Typography>
              </Stack>
            ))}
            <Divider sx={{ my: 1 }} />
            <Stack direction="row" justifyContent="space-between" sx={{ py: 1 }}>
              <Typography variant="h6">Current balance</Typography>
              <Typography variant="h6" color="primary">{formatCurrency(bs.currentBalance)}</Typography>
            </Stack>
          </Box>
        </CardContent>
      </Card>

      <Typography variant="body2" color="text.secondary" sx={{ mt: 2, display: 'flex', gap: 0.5 }}>
        <DownloadIcon fontSize="small" /> Exports reflect the live workbook, including every buyer worksheet.
      </Typography>
    </Box>
  );
}
