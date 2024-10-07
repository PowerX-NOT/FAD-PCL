import { LinearProgressProps } from '@mui/material';

export interface ProductList {
  id?: string;
  name: string;
  color: LinearProgressProps['color'];
  sales: number;
}

export const productTableRows: ProductList[] = [
  {
    id: '01',
    name: 'Tuition Fees',
    color: 'warning',
    sales: 85,
  },
  {
    id: '02',
    name: 'Housing/Accommodation',
    color: 'primary',
    sales: 70,
  },
  {
    id: '03',
    name: 'Books & Supplies',
    color: 'info',
    sales: 60,
  },
  {
    id: '04',
    name: 'Food & Groceries',
    color: 'secondary',
    sales: 45,
  },
];
