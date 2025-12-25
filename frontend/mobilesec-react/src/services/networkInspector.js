const BASE_URL = '/api/network';

const parseJsonOrError = async (response) => {
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Network Inspector request failed');
  }
  return response.json();
};

export const startAndroidSandbox = async () => {
  const response = await fetch(`${BASE_URL}/avd/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  });
  return parseJsonOrError(response);
};

export const startNetworkScan = async (scanId) => {
  const response = await fetch(`${BASE_URL}/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ scan_id: scanId })
  });
  return parseJsonOrError(response);
};

export const stopNetworkScan = async () => {
  const response = await fetch(`${BASE_URL}/stop`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  });
  return parseJsonOrError(response);
};

export const fetchNetworkFindings = async (scanId) => {
  const response = await fetch(`${BASE_URL}/${scanId}/results`);
  return parseJsonOrError(response);
};
