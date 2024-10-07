import { ReactElement } from 'react';
import {
  Paper,
  Table,
  TableRow,
  TableBody,
  TableCell,
  TableHead,
  Typography,
  TableContainer,
} from '@mui/material';
import { productTableRows } from 'data/product-data';
import ProductListRow from './ProductListRow';
import SimpleBar from 'simplebar-react';

const TopExpense = (): ReactElement => {
  return (
    <Paper sx={{ p: { xs: 4, sm: 8 }, height: 1 }}>
      <Typography variant="h4" color="common.white" mb={6}>
        Top Expense Categories
      </Typography>
      <TableContainer component={SimpleBar}>
        <Table sx={{ minWidth: 440 }}>
          <TableHead>
            <TableRow>
              <TableCell align="left">#</TableCell>
              <TableCell align="left">Category</TableCell>
              <TableCell align="left">Frequency</TableCell>
              <TableCell align="center">Amount Spent</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {productTableRows.map((product) => (
              <ProductListRow key={product.id} productList={product} />
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
};

export default TopExpense;
