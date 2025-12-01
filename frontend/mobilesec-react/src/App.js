import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import UploadPage from './pages/UploadPage';
import ResultsPage from './pages/ResultsPage';
import DetailPage from './pages/DetailPage';
import DashboardPage from './pages/DashboardPage';
import CIIntegrationPage from './pages/CIIntegrationPage';
import './App.css';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/results" element={<ResultsPage />} />
          <Route path="/results/:id" element={<DetailPage />} />
          <Route path="/ci-integration" element={<CIIntegrationPage />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
