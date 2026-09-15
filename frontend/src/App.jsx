import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import ProfileView from './components/ProfileView';
import JobMatchingView from './components/JobMatchingView';
import ResumeAuditView from './components/ResumeAuditView';
import AiCoachChatView from './components/AiCoachChatView';
import { fetchCurrentProfile } from './api';
import { CheckCircle2 } from 'lucide-react';
import './App.css';

export default function App() {
  const [activeTab, setActiveTab] = useState('matching');
  const [profile, setProfile] = useState(null);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    fetchCurrentProfile()
      .then((data) => setProfile(data))
      .catch((err) => console.error('Error fetching profile:', err));
  }, []);

  const showToast = (message) => {
    setToast(message);
    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  return (
    <div className="app-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

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
