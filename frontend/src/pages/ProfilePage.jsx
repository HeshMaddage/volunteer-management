import { useState, useEffect } from 'react';
import client from '../api/client';

export default function ProfilePage() {
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [bio, setBio] = useState('');
  const [skillsText, setSkillsText] = useState('');
  const [joinDate, setJoinDate] = useState('');

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    async function fetchProfile() {
      setLoading(true);
      setError(null);
      try {
        const response = await client.get('/volunteers/me');
        const data = response.data;
        setFullName(data.fullName || '');
        setPhone(data.phone || '');
        setAddress(data.address || '');
        setBio(data.bio || '');
        setJoinDate(data.joinDate || '');
        setSkillsText((data.skills || []).join(', '));
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load profile');
      } finally {
        setLoading(false);
      }
    }
    fetchProfile();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccessMessage(null);

    // Split skills by comma, trim whitespace, remove empty items
    const skillsArray = skillsText
      .split(',')
      .map((skill) => skill.trim())
      .filter((skill) => skill.length > 0);

    try {
      const response = await client.put('/volunteers/me', {
        fullName,
        phone,
        address,
        bio,
        skills: skillsArray,
      });
      const data = response.data;
      setFullName(data.fullName || '');
      setPhone(data.phone || '');
      setAddress(data.address || '');
      setBio(data.bio || '');
      setSkillsText((data.skills || []).join(', '));
      setSuccessMessage('Profile updated successfully!');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p className="loading-state">Loading profile...</p>;
  if (error && !fullName) return <p className="error-state">{error}</p>;

  return (
    <div className="container small-container">
      <h1>My Profile</h1>
      {successMessage && <p className="success-message">{successMessage}</p>}
      {error && <p className="error-message">{error}</p>}

      <form onSubmit={handleSubmit} className="profile-form">
        <div className="form-group">
          <label htmlFor="fullName">Full Name</label>
          <input
            type="text"
            id="fullName"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="phone">Phone Number</label>
          <input
            type="text"
            id="phone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="address">Address</label>
          <input
            type="text"
            id="address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="bio">Bio</label>
          <textarea
            id="bio"
            rows="4"
            value={bio}
            onChange={(e) => setBio(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label htmlFor="skills">Skills (comma-separated)</label>
          <input
            type="text"
            id="skills"
            placeholder="e.g. Communication, Event Planning, Java, React"
            value={skillsText}
            onChange={(e) => setSkillsText(e.target.value)}
          />
          <p className="form-help-text">Type skills separated by commas.</p>
        </div>

        {joinDate && (
          <div className="form-group-info">
            <p><strong>Member Since:</strong> {new Date(joinDate).toLocaleDateString()}</p>
          </div>
        )}

        <button type="submit" disabled={saving} className="submit-btn">
          {saving ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </div>
  );
}
