import { ReactElement } from 'react';
import { Box, Paper, Typography } from '@mui/material';
import financialData from 'data/financial-data';
import FinancialCard from './FinancialCard';

const TodaysFinancial = (): ReactElement => {
  return (
    <Paper sx={{ p: { xs: 4, sm: 8 }, height: 1 }}>
      <Typography variant="h4" color="common.white" mb={1.25}>
        Today's Financial Activity
      </Typography>
      <Typography variant="subtitle2" color="text.disabled" mb={6}>
        Financial Summary
      </Typography>
      <Box display="grid" gridTemplateColumns="repeat(12, 1fr)" gap={{ xs: 4, sm: 6 }}>
        {financialData.map((financialItem) => (
          <Box key={financialItem.id} gridColumn={{ xs: 'span 12', sm: 'span 6', lg: 'span 3' }}>
            <FinancialCard financialItem={financialItem} />
          </Box>
        ))}
      </Box>
    </Paper>
  );
};

export default TodaysFinancial;
