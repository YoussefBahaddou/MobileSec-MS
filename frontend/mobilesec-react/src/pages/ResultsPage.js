import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
  useTheme,
  Tooltip,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import VisibilityIcon from '@mui/icons-material/Visibility';
import ShieldOutlinedIcon from '@mui/icons-material/ShieldOutlined';
import SearchIcon from '@mui/icons-material/Search';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { listResults } from '../services/api';

const riskColorMap = {
  LOW: 'success',
  MEDIUM: 'warning',
  HIGH: 'error',
  UNKNOWN: 'default',
};

function ResultsPage() {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const theme = useTheme();

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  const fetchResults = async (p, s) => {
    try {
      setLoading(true);
      const data = await listResults(p, s);
      if (data && Array.isArray(data.content)) {
        setResults(data.content);
        setTotalElements(data.totalElements);
      } else if (Array.isArray(data)) {
        setResults(data);
        setTotalElements(data.length);
      }
    } catch (error) {
      console.error(error);
      enqueueSnackbar('Failed to load analysis results.', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResults(page, rowsPerPage);
  }, [page, rowsPerPage, enqueueSnackbar]);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <Stack spacing={4}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="h4" gutterBottom sx={{ background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`, backgroundClip: 'text', textFillColor: 'transparent', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Analysis Dashboard
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage and review your security analysis reports.
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={() => fetchResults(page, rowsPerPage)}
          sx={{ borderRadius: 2, px: 3, py: 1 }}
        >
          Refresh
        </Button>
      </Stack>

      <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
            <CircularProgress size={40} thickness={4} />
          </Box>
        ) : (
          <>
            <TableContainer>
              <Table sx={{ minWidth: 650 }}>
                <TableHead sx={{ bgcolor: 'grey.50' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600, color: 'text.secondary' }}>ID</TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'text.secondary' }}>Application</TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'text.secondary' }}>Version</TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'text.secondary' }}>Risk Level</TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'text.secondary' }}>Date</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 600, color: 'text.secondary' }}>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.length > 0 ? (
                    results.map((row, index) => (
                      <TableRow
                        key={row.id}
                        hover
                        component={motion.tr}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: index * 0.05 }}
                        sx={{
                          cursor: 'pointer',
                          '&:last-child td, &:last-child th': { border: 0 },
                          transition: 'background-color 0.2s'
                        }}
                        onClick={() => navigate(`/results/${row.id}`)}
                      >
                        <TableCell sx={{ color: 'text.secondary' }}>#{row.id}</TableCell>
                        <TableCell>
                          <Stack direction="row" spacing={2} alignItems="center">
                            <Box sx={{ p: 1, borderRadius: 1, bgcolor: 'primary.light', color: 'white' }}>
                              <ShieldOutlinedIcon fontSize="small" />
                            </Box>
                            <Typography variant="body2" fontWeight={600}>
                              {row.packageName}
                            </Typography>
                          </Stack>
                        </TableCell>
                        <TableCell>{row.versionName ?? 'N/A'}</TableCell>
                        <TableCell>
                          <Chip
                            label={row.riskLevel}
                            color={riskColorMap[row.riskLevel]}
                            size="small"
                            sx={{ fontWeight: 700, minWidth: 80 }}
                          />
                        </TableCell>
                        <TableCell>{new Date(row.createdAt).toLocaleDateString()}</TableCell>
                        <TableCell align="right">
                          <Tooltip title="View Details">
                            <IconButton
                              color="primary"
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/results/${row.id}`);
                              }}
                            >
                              <VisibilityIcon />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 8 }}>
                        <Stack spacing={2} alignItems="center">
                          <SearchIcon sx={{ fontSize: 48, color: 'text.disabled' }} />
                          <Typography variant="body1" color="text.secondary">
                            No analysis results found.
                          </Typography>
                          <Button variant="outlined" onClick={() => navigate('/')}>
                            Start New Analysis
                          </Button>
                        </Stack>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              rowsPerPageOptions={[5, 10, 25]}
              component="div"
              count={totalElements}
              rowsPerPage={rowsPerPage}
              page={page}
              onPageChange={handleChangePage}
              onRowsPerPageChange={handleChangeRowsPerPage}
            />
          </>
        )}
      </Card>
    </Stack>
  );
}

export default ResultsPage;
