import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import UploadPage from './pages/UploadPage';
import ResultsPage from './pages/ResultsPage';
import DetailPage from './pages/DetailPage';
import DashboardPage from './pages/DashboardPage';
import CIIntegrationPage from './pages/CIIntegrationPage';
import NetworkPage from './pages/NetworkPage';
import ReportPage from './pages/ReportPage';
import './App.css';

import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

function App() {
  return (
    <AuthProvider>
      <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            path="/*"
            element={
              <Layout>
                <Routes>
                  <Route path="/" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
                  <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
                  <Route path="/upload" element={<PrivateRoute><UploadPage /></PrivateRoute>} />
                  <Route path="/results" element={<PrivateRoute><ResultsPage /></PrivateRoute>} />
                  <Route path="/results/:id" element={<PrivateRoute><DetailPage /></PrivateRoute>} />
                  <Route path="/ci-integration" element={<PrivateRoute><CIIntegrationPage /></PrivateRoute>} />
                  <Route path="/network" element={<PrivateRoute><NetworkPage /></PrivateRoute>} />
                  <Route path="/report/:scanId?" element={<PrivateRoute><ReportPage /></PrivateRoute>} />
                </Routes>
              </Layout>
            }
          />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
