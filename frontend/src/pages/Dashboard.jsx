import { useCallback, useEffect, useState } from 'react';
import { Grid, Typography, Box, Card, CardContent, CircularProgress } from '@mui/material';
import WalletIcon from '@mui/icons-material/AccountBalanceWallet';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import GroupsIcon from '@mui/icons-material/Groups';

import StatCard from '../components/dashboard/StatCard';
import { IncomeExpensePie, MonthlyTrend } from '../components/dashboard/Charts';
import TransactionTable from '../components/TransactionTable';
import { dashboardApi } from '../api/client';
import { useDataChanged } from '../hooks/useDataChanged';
import { formatCurrency } from '../utils/format';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    dashboardApi.get().then(setData).finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);
  useDataChanged(load);

  if (loading) {
    return <Box sx={{ display: 'grid', placeItems: 'center', py: 10 }}><CircularProgress /></Box>;
  }
  if (!data) return null;

  const cards = [
    { label: 'Total Balance', value: formatCurrency(data.currentBalance), icon: <WalletIcon />, accent: '#0f766e' },
    { label: "Today's Income", value: formatCurrency(data.todayIncome), icon: <TrendingUpIcon />, accent: '#16a34a' },
    { label: "Today's Expense", value: formatCurrency(data.todayExpense), icon: <TrendingDownIcon />, accent: '#dc2626' },
    { label: 'Outstanding Credit', value: formatCurrency(data.outstandingCredit), icon: <GroupsIcon />, accent: '#f59e0b' },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Dashboard</Typography>

      <Grid container spacing={2} sx={{ mb: 1 }}>
        {cards.map((c) => (
          <Grid item xs={6} md={3} key={c.label}>
            <StatCard {...c} />
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={2} sx={{ mb: 1, mt: 0 }}>
        <Grid item xs={12} md={5}>
          <IncomeExpensePie income={data.totalCredit} expense={data.totalDebit} />
        </Grid>
        <Grid item xs={12} md={7}>
          <MonthlyTrend data={data.monthlyTrend} />
        </Grid>
      </Grid>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <SectionCard title="Today's transactions">
            <TransactionTable rows={data.todayTransactions} compact />
          </SectionCard>
        </Grid>
        <Grid item xs={12} md={6}>
          <SectionCard title="Recent transactions">
            <TransactionTable rows={data.recentTransactions} compact />
          </SectionCard>
        </Grid>
      </Grid>
    </Box>
  );
}

function SectionCard({ title, children }) {
  return (
    <Card elevation={0} sx={{ border: (t) => `1px solid ${t.palette.divider}` }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>{title}</Typography>
        {children}
      </CardContent>
    </Card>
  );
}
