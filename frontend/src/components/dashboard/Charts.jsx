import { Card, CardContent, Typography, Box, useTheme } from '@mui/material';
import {
  PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip,
  ResponsiveContainer, Legend, CartesianGrid,
} from 'recharts';

export function IncomeExpensePie({ income = 0, expense = 0 }) {
  const theme = useTheme();
  const data = [
    { name: 'Income', value: income },
    { name: 'Expense', value: expense },
  ];
  const colors = [theme.palette.success.main, theme.palette.error.main];
  const empty = income === 0 && expense === 0;

  return (
    <Card elevation={0} sx={{ height: '100%', border: (t) => `1px solid ${t.palette.divider}` }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>Income vs Expense</Typography>
        {empty ? (
          <EmptyChart message="Add a transaction to see the split" />
        ) : (
          <Box sx={{ height: 260 }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={data} dataKey="value" nameKey="name" innerRadius={60} outerRadius={90} paddingAngle={2}>
                  {data.map((_, i) => <Cell key={i} fill={colors[i]} />)}
                </Pie>
                <Tooltip formatter={(v) => `₹${Number(v).toLocaleString('en-IN')}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Box>
        )}
      </CardContent>
    </Card>
  );
}

export function MonthlyTrend({ data = [] }) {
  const theme = useTheme();
  const empty = data.every((d) => d.income === 0 && d.expense === 0);

  return (
    <Card elevation={0} sx={{ height: '100%', border: (t) => `1px solid ${t.palette.divider}` }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>Monthly trend</Typography>
        {empty ? (
          <EmptyChart message="No monthly activity yet" />
        ) : (
          <Box sx={{ height: 260 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data}>
                <CartesianGrid strokeDasharray="3 3" stroke={theme.palette.divider} />
                <XAxis dataKey="month" fontSize={12} />
                <YAxis fontSize={12} />
                <Tooltip formatter={(v) => `₹${Number(v).toLocaleString('en-IN')}`} />
                <Legend />
                <Bar dataKey="income" name="Income" fill={theme.palette.success.main} radius={[4, 4, 0, 0]} />
                <Bar dataKey="expense" name="Expense" fill={theme.palette.error.main} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Box>
        )}
      </CardContent>
    </Card>
  );
}

function EmptyChart({ message }) {
  return (
    <Box sx={{ height: 260, display: 'grid', placeItems: 'center' }}>
      <Typography color="text.secondary">{message}</Typography>
    </Box>
  );
}
