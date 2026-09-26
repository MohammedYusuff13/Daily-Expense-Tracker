import { useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent, IconButton, Box, Typography,
  ButtonBase, alpha,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import PaymentsIcon from '@mui/icons-material/Payments';
import PersonAddIcon from '@mui/icons-material/PersonAddAlt1';

import TransactionForm from './TransactionForm';
import BuyerForm from './BuyerForm';

const ACTIONS = [
  { key: 'expense', label: 'Expense', hint: 'Money going out', icon: <TrendingDownIcon />, accent: '#dc2626' },
  { key: 'income', label: 'Income', hint: 'Money coming in', icon: <TrendingUpIcon />, accent: '#16a34a' },
  { key: 'payment', label: 'Buyer Payment', hint: 'Record a credit from a buyer', icon: <PaymentsIcon />, accent: '#0f766e' },
  { key: 'buyer', label: 'New Buyer', hint: 'Add a customer', icon: <PersonAddIcon />, accent: '#f59e0b' },
];

/**
 * The popup opened by the floating (+) button. Picking an action closes this
 * chooser and opens the matching form.
 */
export default function AddSpeedDial({ open, onClose }) {
  const [flow, setFlow] = useState(null); // 'expense' | 'income' | 'payment' | 'buyer'

  const pick = (key) => {
    onClose();
    setFlow(key);
  };

  const closeFlow = () => setFlow(null);

  return (
    <>
      <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          What would you like to add?
          <IconButton onClick={onClose} size="small"><CloseIcon /></IconButton>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5, pb: 1 }}>
            {ACTIONS.map((a) => (
              <ButtonBase
                key={a.key}
                onClick={() => pick(a.key)}
                sx={{
                  p: 2, borderRadius: 3, textAlign: 'left', width: '100%',
                  flexDirection: 'column', alignItems: 'flex-start', gap: 1,
                  border: (t) => `1px solid ${alpha(a.accent, 0.3)}`,
                  bgcolor: (t) => alpha(a.accent, t.palette.mode === 'dark' ? 0.15 : 0.07),
                  transition: 'transform .12s ease',
                  '&:hover': { transform: 'translateY(-2px)' },
                }}
              >
                <Box sx={{
                  width: 40, height: 40, borderRadius: 2, display: 'grid',
                  placeItems: 'center', bgcolor: a.accent, color: '#fff',
                }}>
                  {a.icon}
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 700 }}>{a.label}</Typography>
                  <Typography variant="caption" color="text.secondary">{a.hint}</Typography>
                </Box>
              </ButtonBase>
            ))}
          </Box>
        </DialogContent>
      </Dialog>

      {/* Expense / Income / Buyer payment all use the transaction form with a preset type. */}
      <TransactionForm
        open={flow === 'expense'}
        presetType="DEBIT"
        onClose={closeFlow}
        onSaved={() => window.dispatchEvent(new Event('data-changed'))}
      />
      <TransactionForm
        open={flow === 'income'}
        presetType="CREDIT"
        onClose={closeFlow}
        onSaved={() => window.dispatchEvent(new Event('data-changed'))}
      />
      <TransactionForm
        open={flow === 'payment'}
        presetType="CREDIT"
        onClose={closeFlow}
        onSaved={() => window.dispatchEvent(new Event('data-changed'))}
      />
      <BuyerForm
        open={flow === 'buyer'}
        onClose={closeFlow}
        onSaved={() => window.dispatchEvent(new Event('data-changed'))}
      />
    </>
  );
}
