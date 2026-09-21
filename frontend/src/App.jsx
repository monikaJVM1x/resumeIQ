import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import UploadResume from './components/UploadResume';
import ScoreCard from './components/ScoreCard';
import ScoreBreakdown from './components/ScoreBreakdown';
import ResumeProfile from './components/ResumeProfile';
import RoleMatches from './components/RoleMatches';
import SkillGaps from './components/SkillGaps';
import JobMatches from './components/JobMatches';
import Suggestions from './components/Suggestions';
import ErrorBoundary from './components/ErrorBoundary';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Profile from './pages/Profile';
import { AuthProvider, useAuth } from './context/AuthContext';
import { analyzeResume } from './services/api';

function Dashboard() {
  const [analysisResult, setAnalysisResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [loadingStep, setLoadingStep] = useState('');
  const { user, logout } = useAuth();

  const handleAnalyze = async (file) => {
    setIsLoading(true);
    setError(null);
    
    // Simulate some loading steps for UX
    const steps = [
      'Extracting resume content...',
      'Understanding your profile...',
      'Calculating resume score...',
      'Matching suitable roles...',
      'Finding relevant jobs...'
    ];
    
    let stepIndex = 0;
    setLoadingStep(steps[0]);
    const stepInterval = setInterval(() => {
      stepIndex++;
      if (stepIndex < steps.length) {
        setLoadingStep(steps[stepIndex]);
      }
    }, 1200);

    try {
      const result = await analyzeResume(file);
      setAnalysisResult(result);
    } catch (err) {
      if (err.message.includes('401')) {
        setError('Authentication session expired. Please logout and login again.');
      } else {
        setError(err.message || 'Unable to analyze your resume. Please check the file and make sure the backend server is running, then try again.');
      }
    } finally {
      clearInterval(stepInterval);
      setIsLoading(false);
    }
  };

  return (
    <div className="app-container">
      <header className="app-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 2rem' }}>
        <div>
          <h1 style={{ margin: 0 }}>ResumeIQ</h1>
          <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-muted)' }}>AI Resume Analyzer & Job Matcher</p>
        </div>
        
        {user && (
          <div 
            onClick={() => window.location.href = '/profile'}
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer', padding: '0.5rem', borderRadius: '8px', transition: 'background-color 0.2s' }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'var(--background)'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            {user.photoURL ? (
              <img src={user.photoURL} alt="Profile" style={{ width: '32px', height: '32px', borderRadius: '50%' }} />
            ) : (
              <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
                {(user.displayName || user.email || 'U')[0].toUpperCase()}
              </div>
            )}
            <span style={{ fontSize: '0.9rem', fontWeight: 500, color: 'var(--text-secondary)' }}>
              {user.displayName || user.email}
            </span>
          </div>
        )}
      </header>

      <main className="app-main">
        {!analysisResult ? (
          <div className="landing-view">
            <UploadResume onAnalyze={handleAnalyze} isLoading={isLoading} />
            
            {isLoading && (
              <div className="loading-state">
                <div className="spinner"></div>
                <p className="loading-text">{loadingStep}</p>
              </div>
            )}

            {error && (
              <div className="error-state">
                <h3>Analysis Failed</h3>
                <p>{error}</p>
              </div>
            )}
          </div>
        ) : (
          <div className="dashboard-view">
            <div className="dashboard-header">
              <h2>Analysis Complete</h2>
              <button className="reset-btn" onClick={() => setAnalysisResult(null)}>
                Analyze Another Resume
              </button>
            </div>

            <ErrorBoundary onReset={() => setAnalysisResult(null)}>
              <div className="dashboard-grid">
                <div className="dashboard-left">
                  <ScoreCard score={analysisResult.score.overall_score} />
                  <RoleMatches matches={analysisResult.role_matches} />
                  <JobMatches jobs={analysisResult.job_matches} />
                </div>
                
                <div className="dashboard-right">
                  <ResumeProfile profile={analysisResult.profile} />
                  <ScoreBreakdown categories={analysisResult.score.categories} />
                  <SkillGaps gaps={analysisResult.skill_gaps} />
                  <Suggestions suggestions={analysisResult.suggestions} />
                </div>
              </div>
            </ErrorBoundary>
          </div>
        )}
      </main>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route 
            path="/profile" 
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
