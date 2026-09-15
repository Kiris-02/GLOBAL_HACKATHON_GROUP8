import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import ProfileView from './components/ProfileView';
import JobMatchingView from './components/JobMatchingView';
import ResumeAuditView from './components/ResumeAuditView';
import AiCoachChatView from './components/AiCoachChatView';
import { fetchCurrentProfile, DEFAULT_PROFILE, getApiBase, setCustomBackendUrl } from './api';
import { CheckCircle2, AlertTriangle, Link as LinkIcon, RefreshCw } from 'lucide-react';
import './App.css';

export default function App() {
  const [activeTab, setActiveTab] = useState('matching');
  const [profile, setProfile] = useState(DEFAULT_PROFILE);
  const [isBackendConnected, setIsBackendConnected] = useState(false);
  const [backendInput, setBackendInput] = useState('');
  const [toast, setToast] = useState(null);

  const loadProfileData = () => {
    fetchCurrentProfile()
      .then((data) => {
        setProfile(data);
        setIsBackendConnected(true);
      })
      .catch((err) => {
        console.warn('Backend currently unreachable, using default profile:', err.message);
        setIsBackendConnected(false);
        setProfile((prev) => prev || DEFAULT_PROFILE);
      });
  };

  useEffect(() => {
    loadProfileData();
  }, []);

  const handleConnectBackend = () => {
    if (!backendInput.trim()) return;
    setCustomBackendUrl(backendInput.trim());
    showToast('Saved backend URL! Reconnecting...');
    loadProfileData();
  };

  const showToast = (message) => {
    setToast(message);
    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  return (
    <div className="app-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} isConnected={isBackendConnected} />

      {!isBackendConnected && (
        <div style={{
          background: 'rgba(239, 68, 68, 0.08)',
          borderBottom: '1px solid rgba(239, 68, 68, 0.25)',
          padding: '0.6rem 1.25rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '0.75rem',
          fontSize: '0.82rem'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#fca5a5' }}>
            <AlertTriangle size={15} color="#f87171" />
            <span>
              Backend server not yet reached at <code>{getApiBase()}</code>. Running in offline preview mode.
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', flexWrap: 'wrap' }}>
            <input
              type="text"
              placeholder="Paste Render Backend URL (e.g. https://aicareer-backend-xxxx.onrender.com)"
              value={backendInput}
              onChange={(e) => setBackendInput(e.target.value)}
              style={{
                padding: '0.35rem 0.65rem',
                borderRadius: '6px',
                border: '1px solid #334155',
                background: '#0f172a',
                color: '#fff',
                fontSize: '0.78rem',
                minWidth: '280px'
              }}
            />
            <button
              onClick={handleConnectBackend}
              style={{
                padding: '0.35rem 0.75rem',
                borderRadius: '6px',
                background: '#2563eb',
                color: '#fff',
                border: 'none',
                fontSize: '0.78rem',
                cursor: 'pointer',
                fontWeight: 600
              }}
            >
              Connect
            </button>
            <button
              onClick={loadProfileData}
              title="Retry connection"
              style={{
                padding: '0.35rem 0.5rem',
                borderRadius: '6px',
                background: '#1e293b',
                color: '#cbd5e1',
                border: '1px solid #334155',
                fontSize: '0.78rem',
                cursor: 'pointer'
              }}
            >
              <RefreshCw size={13} />
            </button>
          </div>
        </div>
      )}

      <main className="main-content">
        {activeTab === 'profile' && (
          <ProfileView
            profile={profile}
            setProfile={setProfile}
            onGoToMatching={() => setActiveTab('matching')}
            showToast={showToast}
          />
        )}

        {activeTab === 'matching' && (
          <JobMatchingView profile={profile} showToast={showToast} />
        )}

        {activeTab === 'audit' && (
          <ResumeAuditView profile={profile} />
        )}

        {activeTab === 'chat' && (
          <AiCoachChatView profile={profile} />
        )}
      </main>

      {/* Toast Notification */}
      {toast && (
        <div className="toast">
          <CheckCircle2 size={18} color="#10b981" />
          <span>{toast}</span>
        </div>
      )}
    </div>
  );
}
