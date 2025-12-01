import axios from 'axios';

const analysisClient = axios.create({
  baseURL: 'http://localhost:8083/api',
});

const reportClient = axios.create({
  baseURL: 'http://localhost:8083',
});

export const uploadApk = (file, onUploadProgress) => {
  const formData = new FormData();
  formData.append('file', file);

  return analysisClient
    .post('/analysis/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    })
    .then((response) => response.data);
};

export const listResults = (page = 0, size = 5) =>
  analysisClient
    .get('/analysis/results', {
      params: { page, size },
    })
    .then((response) => response.data);

export const getResultById = (id) =>
  analysisClient.get(`/analysis/results/${id}`).then((response) => response.data);

export const getDashboardStats = () =>
  analysisClient.get('/dashboard/stats').then((response) => response.data);

const REPORT_FILE_MAP = {
  JSON: {
    mimeType: 'application/json',
    filename: (id) => `analysis-${id}.report.json`,
  },
  SARIF: {
    mimeType: 'application/json',
    filename: (id) => `analysis-${id}.report.sarif.json`,
  },
  PDF: {
    mimeType: 'application/pdf',
    filename: (id) => `analysis-${id}.report.pdf`,
  },
};

export const downloadReportById = async (id, format) => {
  const formatConfig = REPORT_FILE_MAP[format];

  if (!formatConfig) {
    throw new Error(`Unsupported report format: ${format}`);
  }

  try {
    const config = {
      params: { format },
      headers: {
        Accept: format === 'PDF' ? 'application/pdf' : 'application/json',
      },
    };

    if (format === 'PDF') {
      config.responseType = 'blob';
    }

    const response = await reportClient.post(`/reports/by-id/${id}`, {}, config);

    let blob;
    let filename;

    if (format === 'PDF') {
      // Robustly handle Blob response
      blob = response.data instanceof Blob
        ? response.data
        : new Blob([response.data], { type: formatConfig.mimeType });

      // Try to extract filename from Content-Disposition header
      const contentDisposition = response.headers['content-disposition'];
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1];
        }
      }
    } else {
      // Handle JSON/SARIF
      const jsonPayload =
        typeof response.data === 'string'
          ? response.data
          : JSON.stringify(response.data, null, 2);
      blob = new Blob([jsonPayload], { type: formatConfig.mimeType });
    }

    // Fallback filename if extraction failed
    if (!filename) {
      filename = formatConfig.filename(id);
    }

    return { blob, filename };
  } catch (error) {
    console.error(`Download failed for format ${format}:`, error);
    throw error;
  }
};
