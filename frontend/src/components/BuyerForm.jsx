import { useEffect, useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Stack,
} from '@mui/material';
import { buyerApi } from '../api/client';
import { useToast } from '../context/ToastContext';

const empty = { name: '', mobile: '', address: '', openingBalance: '' };

export default function BuyerForm({ open, onClose, onSaved }) {
  const { notify } = useToast();
  const [form, setForm] = useState(empty);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) setForm(empty);
  }, [open]);

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const submit = async () => {
    if (!form.name.trim()) {
      notify('Buyer name is required', 'warning');
      return;
    }
    setSaving(true);
    try {
      const res = await buyerApi.create({
        ...form,
        openingBalance: Number(form.openingBalance) || 0,
      });
      notify(res.message || 'Buyer created', 'success');
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
      <DialogTitle>New buyer</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Buyer name" value={form.name} onChange={set('name')} fullWidth required />
          <TextField label="Mobile number" value={form.mobile} onChange={set('mobile')} fullWidth />
          <TextField label="Address" value={form.address} onChange={set('address')} multiline minRows={2} fullWidth />
          <TextField
            label="Opening balance" type="number" value={form.openingBalance}
            onChange={set('openingBalance')} inputProps={{ step: '0.01' }} fullWidth
            helperText="Positive = buyer owes you money"
          />
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={submit} disabled={saving}>
          {saving ? 'Saving…' : 'Create buyer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
