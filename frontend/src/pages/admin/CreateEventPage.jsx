import { useState, useEffect } from 'react';
import client from '../../api/client';

export default function CreateEventPage() {
  // Event state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [createdEvent, setCreatedEvent] = useState(null);

  // Shift state
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [capacity, setCapacity] = useState('');
  const [selectedSkills, setSelectedSkills] = useState([]);
  const [skillsList, setSkillsList] = useState([]);
  const [addedShifts, setAddedShifts] = useState([]);
  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [shiftError, setShiftError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Image upload state
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [imageUploadSuccess, setImageUploadSuccess] = useState(false);
  const [imageUploadError, setImageUploadError] = useState(null);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
      setPreviewUrl(URL.createObjectURL(file));
      setImageUploadError(null);
      setImageUploadSuccess(false);
    }
  };

  const handleImageUpload = async (e) => {
    e.preventDefault();
    if (uploadingImage) return; // Guard against duplicate calls
    if (!selectedFile || !createdEvent) return;

    setUploadingImage(true);
    setImageUploadError(null);
    setImageUploadSuccess(false);

    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      // Omit manual Content-Type header so Axios generates the correct boundary parameter
      await client.post(`/events/${createdEvent.id}/image`, formData);

      setImageUploadSuccess(true);
      setSelectedFile(null);
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
        setPreviewUrl(null);
      }
    } catch (err) {
      console.error('Failed to upload image:', err);
      setImageUploadError(err.response?.data?.message || 'Failed to upload image');
    } finally {
      setUploadingImage(false);
    }
  };

  const handleCreateAnotherEvent = () => {
    setCreatedEvent(null);
    setSelectedFile(null);
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    }
    setImageUploadSuccess(false);
    setImageUploadError(null);
    setTitle('');
    setDescription('');
    setLocation('');
    setAddedShifts([]);
  };

  useEffect(() => {
    async function fetchSkills() {
      try {
        const response = await client.get('/skills');
        setSkillsList(response.data || []);
      } catch (err) {
        console.error('Failed to load skills list:', err);
      }
    }
    fetchSkills();
  }, []);

  const handleEventSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMessage(null);
    try {
      const response = await client.post('/events', {
        title,
        description,
        location,
      });
      setCreatedEvent(response.data);
      setSuccessMessage(`Event "${response.data.title}" created successfully! Now add some shifts.`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create event');
    } finally {
      setLoading(false);
    }
  };

  const handleSkillCheckboxChange = (skillName) => {
    setSelectedSkills((prev) =>
      prev.includes(skillName)
        ? prev.filter((s) => s !== skillName)
        : [...prev, skillName]
    );
  };

  const handleShiftSubmit = async (e) => {
    e.preventDefault();
    if (!createdEvent) return;

    setLoading(true);
    setShiftError(null);
    try {
      // Convert datetime-local values (YYYY-MM-DDTHH:MM) to ISO instant string (e.g. YYYY-MM-DDTHH:MM:SSZ)
      const isoStartTime = new Date(startTime).toISOString();
      const isoEndTime = new Date(endTime).toISOString();

      const response = await client.post(`/events/${createdEvent.id}/shifts`, {
        startTime: isoStartTime,
        endTime: isoEndTime,
        capacity: parseInt(capacity, 10),
        requiredSkills: selectedSkills,
      });

      setAddedShifts((prev) => [...prev, response.data]);
      setSuccessMessage('Shift added successfully!');

      // Reset shift form inputs
      setStartTime('');
      setEndTime('');
      setCapacity('');
      setSelectedSkills([]);
    } catch (err) {
      setShiftError(err.response?.data?.message || 'Failed to add shift');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container small-container">
      <h1>Create Event (Admin)</h1>

      {!createdEvent ? (
        <form onSubmit={handleEventSubmit} className="admin-form">
          <h2>Event Details</h2>
          {error && <p className="error-message">{error}</p>}

          <div className="form-group">
            <label htmlFor="title">Event Title</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              rows="4"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="location">Location</label>
            <input
              type="text"
              id="location"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              required
            />
          </div>

          <button type="submit" disabled={loading} className="submit-btn">
            {loading ? 'Creating...' : 'Create Event'}
          </button>
        </form>
      ) : (
        <div className="shifts-creator-flow">
          {successMessage && <p className="success-message">{successMessage}</p>}

          <div className="event-summary-block">
            <h3>Event: {createdEvent.title}</h3>
            <p><strong>Location:</strong> {createdEvent.location}</p>
            <p>{createdEvent.description}</p>
          </div>

          <div className="image-upload-card">
            <h3>Event Flyer / Image (Optional)</h3>
            {imageUploadError && <p className="error-message">{imageUploadError}</p>}
            {imageUploadSuccess && <p className="success-message">Image uploaded successfully!</p>}
            
            {!imageUploadSuccess && (
              <div className="image-upload-controls">
                <div className="form-group" style={{ marginBottom: '1rem' }}>
                  <input
                    type="file"
                    id="event-image-input"
                    accept="image/*"
                    onChange={handleFileChange}
                    style={{ display: 'none' }}
                  />
                  <label htmlFor="event-image-input" className="submit-btn secondary-btn" style={{ display: 'inline-block', cursor: 'pointer', textAlign: 'center' }}>
                    {selectedFile ? 'Change Selected Image' : 'Select Image File'}
                  </label>
                  {selectedFile && <span style={{ marginLeft: '10px', fontSize: '0.9rem', color: 'var(--text)' }}>{selectedFile.name}</span>}
                </div>
                
                {previewUrl && (
                  <div className="image-preview-container" style={{ margin: '1rem 0', maxWidth: '300px', borderRadius: 'var(--radius-md)', overflow: 'hidden', border: '1px solid var(--border)' }}>
                    <img src={previewUrl} alt="Preview" style={{ width: '100%', display: 'block', objectFit: 'cover' }} />
                  </div>
                )}
                
                {selectedFile && (
                  <button
                    type="button"
                    onClick={handleImageUpload}
                    disabled={uploadingImage}
                    className="submit-btn"
                  >
                    {uploadingImage ? 'Uploading Image...' : 'Upload Image'}
                  </button>
                )}
              </div>
            )}
          </div>

          {addedShifts.length > 0 && (
            <div className="added-shifts-list">
              <h4>Added Shifts</h4>
              <ul>
                {addedShifts.map((shift, idx) => (
                  <li key={shift.id || idx}>
                    <strong>Shift {idx + 1}:</strong>{' '}
                    {new Date(shift.startTime).toLocaleString()} - {new Date(shift.endTime).toLocaleString()}{' '}
                    (Capacity: {shift.capacity})
                    {shift.requiredSkills && shift.requiredSkills.length > 0 && (
                      <span> | Skills: {shift.requiredSkills.join(', ')}</span>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          )}

          <form onSubmit={handleShiftSubmit} className="admin-form">
            <h2>Add Shift to Event</h2>
            {shiftError && <p className="error-message">{shiftError}</p>}

             <div className="form-group">
               <label htmlFor="startTime">Start Date &amp; Time</label>
               <input
                 type="datetime-local"
                 id="startTime"
                 value={startTime}
                 onChange={(e) => setStartTime(e.target.value)}
                 onClick={(e) => e.target.showPicker()}
                 required
               />
             </div>
 
             <div className="form-group">
               <label htmlFor="endTime">End Date &amp; Time</label>
               <input
                 type="datetime-local"
                 id="endTime"
                 value={endTime}
                 onChange={(e) => setEndTime(e.target.value)}
                 onClick={(e) => e.target.showPicker()}
                 required
               />
             </div>

            <div className="form-group">
              <label htmlFor="capacity">Capacity (Volunteers needed)</label>
              <input
                type="number"
                id="capacity"
                min="1"
                value={capacity}
                onChange={(e) => setCapacity(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label>Required Skills</label>
              <div className="skills-checkboxes">
                {skillsList.length === 0 ? (
                  <p className="form-help-text">No skills found. Create some skills to populate this.</p>
                ) : (
                  skillsList.map((skill) => (
                    <label key={skill.id} className="checkbox-label">
                      <input
                        type="checkbox"
                        checked={selectedSkills.includes(skill.name)}
                        onChange={() => handleSkillCheckboxChange(skill.name)}
                      />
                      <span>{skill.name}</span>
                    </label>
                  ))
                )}
              </div>
            </div>

            <button type="submit" disabled={loading} className="submit-btn secondary-btn">
              {loading ? 'Adding...' : 'Add Shift'}
            </button>
          </form>

          <button
            onClick={handleCreateAnotherEvent}
            className="submit-btn"
            style={{ marginTop: '20px' }}
          >
            Create Another Event
          </button>
        </div>
      )}
    </div>
  );
}
