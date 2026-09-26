import {
  Table, TableBody, TableCell, TableHead, TableRow, Chip, IconButton,
  Box, Typography, TableContainer, Paper,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import { formatCurrency, formatDate } from '../utils/format';

export default function TransactionTable({ rows = [], onEdit, onDelete, compact = false }) {
  if (!rows.length) {
    return (
      <Box sx={{ py: 6, textAlign: 'center' }}>
        <Typography color="text.secondary">No transactions to show yet.</Typography>
      </Box>
    );
  }

  return (
    <TableContainer component={Paper} elevation={0} sx={{ border: (t) => `1px solid ${t.palette.divider}` }}>
      <Table size={compact ? 'small' : 'medium'}>
        <TableHead>
          <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Buyer</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Mode</TableCell>
            <TableCell align="right">Amount</TableCell>
            {!compact && <TableCell align="right">Actions</TableCell>}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((t) => {
            const credit = t.type === 'CREDIT';
            return (
              <TableRow key={t.id} hover>
                <TableCell>{formatDate(t.date)}</TableCell>
                <TableCell>{t.buyerName || '—'}</TableCell>
                <TableCell sx={{ maxWidth: 220 }}>
                  <Typography noWrap variant="body2">{t.description || '—'}</Typography>
                </TableCell>
                <TableCell>
                  <Chip label={t.paymentMode} size="small" variant="outlined" />
                </TableCell>
                <TableCell align="right" sx={{ fontWeight: 700, color: credit ? 'success.main' : 'error.main' }}>
                  {credit ? '+' : '−'}{formatCurrency(t.amount)}
                </TableCell>
                {!compact && (
                  <TableCell align="right">
                    <IconButton size="small" onClick={() => onEdit?.(t)}><EditIcon fontSize="small" /></IconButton>
                    <IconButton size="small" color="error" onClick={() => onDelete?.(t)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                )}
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
