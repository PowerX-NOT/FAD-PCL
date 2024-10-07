import { ReactElement } from 'react';
import { Box } from '@mui/material';

import FinancialFulfillment from 'components/sections/dashboard/financial-fulfilment/FinancialFulfillment';
import ExpenseInsights from 'components/sections/dashboard/expense-insights/ExpenseInsights';
import TodaysFinancial from 'components/sections/dashboard/todays-financial-activity/TodaysFinancial';
import TopExpense from 'components/sections/dashboard/top-expense/TopExpense';
import TrendingNow from 'components/sections/dashboard/trending-now/TrendingNow';
import Customers from 'components/sections/dashboard/customers/Customers';
import Financial from 'components/sections/dashboard/financial-overview/Financial';
import Level from 'components/sections/dashboard/level/Level';

const Dashboard = (): ReactElement => {
  return (
    <>
      <Box display="grid" gridTemplateColumns="repeat(12, 1fr)" gap={3.5}>
        <Box gridColumn={{ xs: 'span 12', '2xl': 'span 8' }} order={{ xs: 0 }}>
          <TodaysFinancial />
        </Box>
        <Box gridColumn={{ xs: 'span 12', lg: 'span 4' }} order={{ xs: 1, '2xl': 1 }}>
          <Level />
        </Box>
        <Box gridColumn={{ xs: 'span 12', lg: 'span 8' }} order={{ xs: 2, '2xl': 2 }}>
          <TopExpense />
        </Box>
        <Box
          gridColumn={{ xs: 'span 12', md: 'span 6', xl: 'span 4' }}
          order={{ xs: 3, xl: 3, '2xl': 3 }}
        >
          <FinancialFulfillment />
        </Box>
        <Box
          gridColumn={{ xs: 'span 12', md: 'span 6', xl: 'span 4' }}
          order={{ xs: 4, xl: 5, '2xl': 4 }}
        >
          <Financial />
        </Box>
        <Box gridColumn={{ xs: 'span 12', xl: 'span 8' }} order={{ xs: 5, xl: 4, '2xl': 5 }}>
          <ExpenseInsights />
        </Box>
        <Box
          gridColumn={{ xs: 'span 12', xl: 'span 8', '2xl': 'span 6' }}
          order={{ xs: 6, '2xl': 6 }}
        >
          <TrendingNow />
        </Box>
        <Box gridColumn={{ xs: 'span 12', '2xl': 'span 6' }} order={{ xs: 7 }}>
          <Customers />
        </Box>
      </Box>
    </>
  );
};

export default Dashboard;
