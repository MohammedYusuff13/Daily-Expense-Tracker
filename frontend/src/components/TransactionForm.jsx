import { useEffect, useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField,
  MenuItem, Stack, Autocomplete,
} from '@mui/material';
import { transactionApi, buyerApi } from '../api/client';
import { useToast } from '../context/ToastContext';

const today = () => new Date().toISOString().slice(0, 10);

const emptyForm = (type = 'DEBIT') => ({
  date: today(),
  buyerName: '',
  type,
  amount: '',
  description: '',
  paymentMode: 'CASH',
});

/**
 * Reusable add/edit dialog. `presetType` fixes the CREDIT/DEBIT toggle for the
 * quick-add flows (Income vs Expense). `existing` switches it to edit mode.
 */
export default function TransactionForm({ open, onClose, onSaved, presetType, existing }) {
  const { notify } = useToast();
  const [form, setForm] = useState(emptyForm(presetType));
  const [buyers, setBuyers] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      buyerApi.list().then(setBuyers).catch(() => setBuyers([]));
      if (existing) {
        setForm({
          date: existing.date,
          buyerName: existing.buyerName || '',
          type: existing.type,
          amount: existing.amount,
          description: existing.description || '',
          paymentMode: existing.paymentMode || 'CASH',
        });
      } else {
        setForm(emptyForm(presetType || 'DEBIT'));
      }
    }
  }, [open, existing, presetType]);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async () => {
    if (!form.amount || Number(form.amount) <= 0) {
      notify('Enter an amount greater than zero', 'warning');
      return;
    }
    setSaving(true);
    try {
      const payload = { ...form, amount: Number(form.amount) };
      const res = existing
        ? await transactionApi.update(existing.id, payload)
        : await transactionApi.create(payload);
      notify(res.message || 'Saved', 'success');
      onSaved?.();
      onClose();
    } catch (err) {
      notify(err.message, 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>
        {existing ? 'Edit transaction' : form.type === 'CREDIT' ? 'Add income' : 'Add expense'}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Date" type="date" value={form.date} onChange={set('date')}
            InputLabelProps={{ shrink: true }} fullWidth
          />
          <Autocomplete
            freeSolo
            options={buyers.map((b) => b.name)}
            value={form.buyerName}
            onChange={(_, v) => setForm((f) => ({ ...f, buyerName: v || '' }))}
            onInputChange={(_, v) => setForm((f) => ({ ...f, buyerName: v }))}
            renderInput={(params) => (
              <TextField {...params} label="Buyer (optional)" placeholder="Leave blank for personal" />
            )}
          />
          <TextField select label="Type" value={form.type} onChange={set('type')} fullWidth>
            <MenuItem value="CREDIT">Credit (money in)</MenuItem>
            <MenuItem value="DEBIT">Debit (money out)</MenuItem>
          </TextField>
          <TextField
            label="Amount" type="number" value={form.amount} onChange={set('amount')}
            inputProps={{ min: 0, step: '0.01' }} fullWidth
          />
          <TextField
            select label="Payment mode" value={form.paymentMode} onChange={set('paymentMode')} fullWidth
          >
            <MenuItem value="CASH">Cash</MenuItem>
            <MenuItem value="UPI">UPI</MenuItem>
            <MenuItem value="BANK">Bank</MenuItem>
          </TextField>
          <TextField
            label="Description" value={form.description} onChange={set('description')}
            multiline minRows={2} fullWidth
          />
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={submit} disabled={saving}>
          {saving ? 'Saving…' : existing ? 'Save changes' : 'Add'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
