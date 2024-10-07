import { Chip, LinearProgress, TableCell, TableRow } from '@mui/material';
import { ProductList } from 'data/product-data';
import { ReactElement } from 'react';

const ProductListRow = ({ productList }: { productList: ProductList }): ReactElement => {
  return (
    <TableRow>
      <TableCell
        align="left"
        component="th"
        variant="head"
        scope="row"
        sx={{
          color: 'common.white',
          fontSize: 'body1.fontSize',
        }}
      >
        {productList.id}
      </TableCell>
      <TableCell
        align="left"
        sx={{
          whiteSpace: 'nowrap',
        }}
      >
        {productList.name}
      </TableCell>
      <TableCell align="left">
        <LinearProgress
          variant="determinate"
          color={productList.color}
          value={productList.sales}
          sx={{
            bgcolor: 'grey.900',
          }}
        />
      </TableCell>
      <TableCell align="center">
        <Chip
          label={`${productList.sales}%`}
          color={productList.color as any}
          variant="outlined"
          size="medium"
        />
      </TableCell>
    </TableRow>
  );
};

export default ProductListRow;
