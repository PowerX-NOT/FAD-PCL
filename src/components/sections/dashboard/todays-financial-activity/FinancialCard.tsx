import { ReactElement } from 'react';
import { Box, Stack, Typography } from '@mui/material';
import Image from 'components/base/Image';
import { financialItem } from 'data/financial-data';

const FinancialCard = ({ financialItem }: { financialItem: financialItem }): ReactElement => {
  return (
    <Stack gap={6} p={5} borderRadius={4} height={1} bgcolor="background.default">
      <Image src={financialItem.icon} alt={financialItem.subtitle} width={26} height={26} />
      <Box>
        <Typography variant="h4" color="common.white" mb={4}>
          {financialItem.title}
        </Typography>
        <Typography variant="body1" color="text.secondary" mb={2}>
          {financialItem.subtitle}
        </Typography>
        <Typography variant="body2" color={financialItem.color} lineHeight={1.25}>
          +{financialItem.increment}% from yesterday
        </Typography>
      </Box>
    </Stack>
  );
};

export default FinancialCard;
