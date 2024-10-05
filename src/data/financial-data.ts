import totalSales from 'assets/images/todays-financial-activity/total-sales.png';
import totalOrder from 'assets/images/todays-financial-activity/total-order.png';
import productSold from 'assets/images/todays-financial-activity/product-sold.png';
import newCustomer from 'assets/images/todays-financial-activity/new-customer.png';

export interface FinancialItem {
  id?: number;
  icon: string;
  title: string;
  subtitle: string;
  increment: number;
  color: string;
}

const financialData: FinancialItem[] = [
  {
    id: 1,
    icon: totalSales,
    title: '$5k',
    subtitle: 'Total Income',
    increment: 10,
    color: 'warning.main',
  },
  {
    id: 2,
    icon: totalOrder,
    title: '500',
    subtitle: 'Total Expenses',
    increment: 8,
    color: 'primary.main',
  },
  {
    id: 3,
    icon: productSold,
    title: '9',
    subtitle: 'Transactions Completed',
    increment: 2,
    color: 'secondary.main',
  },
  {
    id: 4,
    icon: newCustomer,
    title: '12',
    subtitle: 'New Transactions',
    increment: 3,
    color: 'info.main',
  },
];

export default financialData;
