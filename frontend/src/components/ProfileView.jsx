import React, { useState } from 'react';
import { 
  UploadCloud, FileText, CheckCircle2, User, Globe, 
  MapPin, Plus, X, Save, ArrowRight, Loader2 
} from 'lucide-react';
import { saveProfile, uploadCvFile, resetSampleProfile } from '../api';

export default function ProfileView({ profile, setProfile, onGoToMatching, showToast }) {
  const [loading, setLoading] = useState(false);
  const [newSkill, setNewSkill] = useState('');
  const [newRole, setNewRole] = useState('');
  const [newLocation, setNewLocation] = useState('');

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setLoading(true);
    try {
      const updated = await uploadCvFile(file);
      setProfile(updated);
      showToast('Resume parsed and profile updated successfully! 🎉');
    } catch (err) {
      alert(err.message || 'Error parsing resume file');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const updated = await saveProfile(profile);
      setProfile(updated);
      showToast('Profile saved successfully! 💾');
    } catch (err) {
      alert(err.message || 'Error saving profile');
    } finally {
      setLoading(false);
    }
  };

  const handleLoadSample = async (type) => {
    setLoading(true);
    try {
      const updated = await resetSampleProfile(type);
      setProfile(updated);
      showToast(`Switched to sample profile: ${type}`);
    } catch (err) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  const addSkill = () => {
    if (!newSkill.trim()) return;
    const current = profile.skills || [];
    if (!current.includes(newSkill.trim())) {
      setProfile({ ...profile, skills: [...current, newSkill.trim()] });
    }
    setNewSkill('');
  };

  const removeSkill = (s) => {
    setProfile({
      ...profile,
      skills: (profile.skills || []).filter((item) => item !== s),
    });
  };

  const addRole = () => {
    if (!newRole.trim()) return;
    const current = profile.targetRoles || [];
    if (!current.includes(newRole.trim())) {
      setProfile({ ...profile, targetRoles: [...current, newRole.trim()] });
    }
    setNewRole('');
  };

  const removeRole = (r) => {
    setProfile({
      ...profile,
      targetRoles: (profile.targetRoles || []).filter((item) => item !== r),
    });
  };

  const addLocation = () => {
    if (!newLocation.trim()) return;
    const current = profile.targetLocations || [];
    if (!current.includes(newLocation.trim())) {
      setProfile({ ...profile, targetLocations: [...current, newLocation.trim()] });
    }
    setNewLocation('');
  };

  const removeLocation = (loc) => {
    setProfile({
      ...profile,
      targetLocations: (profile.targetLocations || []).filter((item) => item !== loc),
    });
  };

  if (!profile) {
    return <div style={{ textAlign: 'center', padding: '3rem' }}>Loading profile information...</div>;
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-text">
          <h1>Career Profile & Resume Upload</h1>
          <p>
            Personalize your professional identity, core tech stack, and set career goals domestically or overseas.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.65rem', flexWrap: 'wrap' }}>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => handleLoadSample('senior-backend')}
            disabled={loading}
          >
            👨‍💻 Sample: Senior Backend (Relocation)
          </button>
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => handleLoadSample('frontend-react')}
            disabled={loading}
          >
            🎨 Sample: Frontend React (Remote US)
          </button>
          <button
            className="btn btn-primary btn-sm"
            onClick={onGoToMatching}
          >
            Find Matching Jobs <ArrowRight size={14} />
          </button>
        </div>
      </div>

      <div className="grid-2">
        {/* Left Column: CV Upload & Quick Summary */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="glass-card">
            <div className="card-title-row">
              <div className="card-title">
                <UploadCloud size={20} color="#818cf8" />
                <span>AI Resume Extractor</span>
              </div>
            </div>

            <label className="dropzone">
              <input
                type="file"
                accept=".pdf,.txt,.doc,.docx"
                onChange={handleFileUpload}
                style={{ display: 'none' }}
                disabled={loading}
              />
              <div className="dropzone-icon">
                {loading ? <Loader2 className="animate-spin" size={24} /> : <FileText size={24} />}
              </div>
              <div>
                <strong style={{ color: '#ffffff', display: 'block', marginBottom: '0.2rem' }}>
                  {loading ? 'Extracting resume details...' : 'Drag & Drop your Resume or Click to Browse'}
                </strong>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Supports PDF and TXT. AI automatically extracts technical skills, roles, and experience.
                </span>
              </div>
            </label>
          </div>

          <div className="glass-card">
            <div className="card-title-row">
              <div className="card-title">
                <Globe size={20} color="#06b6d4" />
                <span>International & Visa Preferences</span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <label style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={profile.willingToRelocate || false}
                  onChange={(e) => setProfile({ ...profile, willingToRelocate: e.target.checked })}
                  style={{ marginTop: '0.25rem', width: '18px', height: '18px', accentColor: 'var(--accent-primary)' }}
                />
                <div>
                  <strong style={{ color: '#ffffff', fontSize: '0.9rem' }}>
                    Willing to Relocate Abroad (Relocation Ready)
                  </strong>
                  <p style={{ fontSize: '0.785rem', color: 'var(--text-secondary)' }}>
                    Enable this option so AI prioritizes jobs in Singapore, Germany, Japan with Visa Sponsorship & Relocation assistance.
                  </p>
                </div>
              </label>

              <div className="form-group">
                <label className="form-label">Preferred Work Arrangement</label>
                <select
                  className="form-control"
                  value={profile.targetWorkType || 'ANY'}
                  onChange={(e) => setProfile({ ...profile, targetWorkType: e.target.value })}
                >
                  <option value="ANY">Flexible (All arrangements)</option>
                  <option value="REMOTE">Remote (100% Work from Anywhere)</option>
                  <option value="HYBRID">Hybrid</option>
                  <option value="ONSITE">Onsite</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Target Countries / Markets</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="e.g., Singapore, Germany, Remote Global..."
                    value={newLocation}
                    onChange={(e) => setNewLocation(e.target.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addLocation(); }}}
                  />
                  <button type="button" className="btn btn-secondary btn-sm" onClick={addLocation}>
                    <Plus size={16} />
                  </button>
                </div>
                <div className="tag-container">
                  {(profile.targetLocations || []).map((loc) => (
                    <span key={loc} className="skill-tag">
                      <MapPin size={12} color="#06b6d4" />
                      {loc}
                      <span className="skill-tag-remove" onClick={() => removeLocation(loc)}>
                        <X size={12} />
                      </span>
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Editable Profile Form */}
        <form onSubmit={handleSave} className="glass-card">
          <div className="card-title-row">
            <div className="card-title">
              <User size={20} color="#818cf8" />
              <span>Personal & Professional Profile</span>
            </div>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <Loader2 className="animate-spin" size={16} /> : <Save size={16} />}
              Save Profile
            </button>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                className="form-control"
                value={profile.fullName || ''}
                onChange={(e) => setProfile({ ...profile, fullName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Contact Email</label>
              <input
                type="email"
                className="form-control"
                value={profile.email || ''}
                onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 0.8fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Current Title / Role</label>
              <input
                type="text"
                className="form-control"
                value={profile.currentTitle || ''}
                onChange={(e) => setProfile({ ...profile, currentTitle: e.target.value })}
                placeholder="e.g., Senior Backend Engineer"
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Years of Experience</label>
              <input
                type="number"
                step="0.5"
                className="form-control"
                value={profile.yearsOfExperience || ''}
                onChange={(e) => setProfile({ ...profile, yearsOfExperience: parseFloat(e.target.value) || 0 })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Education / Degree</label>
              <input
                type="text"
                className="form-control"
                value={profile.education || ''}
                onChange={(e) => setProfile({ ...profile, education: e.target.value })}
                placeholder="e.g., B.S. in Computer Science"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Languages & Proficiency</label>
              <input
                type="text"
                className="form-control"
                value={profile.languages || ''}
                onChange={(e) => setProfile({ ...profile, languages: e.target.value })}
                placeholder="e.g., English (Fluent), Japanese (N3)"
              />
            </div>
          </div>

          {/* Skills Management */}
          <div className="form-group">
            <label className="form-label">Core Technical Skills</label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                type="text"
                className="form-control"
                placeholder="Add skill (e.g., Docker, Kubernetes, React, Redis)..."
                value={newSkill}
                onChange={(e) => setNewSkill(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addSkill(); }}}
              />
              <button type="button" className="btn btn-secondary btn-sm" onClick={addSkill}>
                <Plus size={16} /> Add
              </button>
            </div>
            <div className="tag-container" style={{ marginTop: '0.75rem' }}>
              {(profile.skills || []).map((skill) => (
                <span key={skill} className="skill-tag">
                  {skill}
                  <span className="skill-tag-remove" onClick={() => removeSkill(skill)}>
                    <X size={12} />
                  </span>
                </span>
              ))}
            </div>
          </div>

          {/* Target Roles */}
          <div className="form-group">
            <label className="form-label">Target Next Roles</label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <input
                type="text"
                className="form-control"
                placeholder="e.g., Cloud Solutions Architect, Tech Lead..."
                value={newRole}
                onChange={(e) => setNewRole(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addRole(); }}}
              />
              <button type="button" className="btn btn-secondary btn-sm" onClick={addRole}>
                <Plus size={16} /> Add
              </button>
            </div>
            <div className="tag-container" style={{ marginTop: '0.75rem' }}>
              {(profile.targetRoles || []).map((role) => (
                <span key={role} className="skill-tag">
                  {role}
                  <span className="skill-tag-remove" onClick={() => removeRole(role)}>
                    <X size={12} />
                  </span>
                </span>
              ))}
            </div>
          </div>

          {/* Bio */}
          <div className="form-group">
            <label className="form-label">Professional Summary & Bio</label>
            <textarea
              className="form-control"
              value={profile.bio || ''}
              onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
              placeholder="Brief summary of your project experience, architectural expertise, and career aspirations..."
            />
          </div>
        </form>
      </div>
    </div>
  );
}
