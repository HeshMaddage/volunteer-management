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
            onClick={() => setCreatedEvent(null)}
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
