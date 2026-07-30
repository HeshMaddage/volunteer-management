import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import client from '../api/client';

export default function EventDetailPage() {
  const { eventId } = useParams();
  const { isAuthenticated, isAdmin } = useAuth();

  const [event, setEvent] = useState(null);
  const [shifts, setShifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [registerMessage, setRegisterMessage] = useState(null);
  const [registerError, setRegisterError] = useState({});
  const [cancelling, setCancelling] = useState(false);

  const fetchEventAndShifts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [eventRes, shiftsRes] = await Promise.all([
        client.get(`/events/${eventId}`),
        client.get(`/events/${eventId}/shifts`),
      ]);
      setEvent(eventRes.data);
      setShifts(shiftsRes.data || []);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to load event details';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  useEffect(() => {
    fetchEventAndShifts();
  }, [fetchEventAndShifts]);

  const handleRegister = async (shiftId) => {
    setRegisterMessage(null);
    setRegisterError((prev) => ({ ...prev, [shiftId]: null }));
    try {
      await client.post(`/shifts/${shiftId}/register`);
      setRegisterMessage(`Successfully registered for the shift!`);
      // Refresh the shifts to get updated registeredCount
      const shiftsRes = await client.get(`/events/${eventId}/shifts`);
      setShifts(shiftsRes.data || []);
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed';
      setRegisterError((prev) => ({ ...prev, [shiftId]: msg }));
    }
  };

  const handleCancelEvent = async () => {
    if (!window.confirm('Are you sure you want to cancel this event?')) return;
    setCancelling(true);
    try {
      await client.patch(`/events/${eventId}/cancel`);
      // Refresh event details
      await fetchEventAndShifts();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel event');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <p className="loading-state">Loading event details...</p>;
  if (error) return <p className="error-state">{error}</p>;
  if (!event) return <p className="empty-state">Event not found.</p>;

  return (
    <div className="container">
      <div className="event-detail-header">
        <h1>{event.title}</h1>
        <div className="header-badges">
          <span className={`status-badge status-${event.status.toLowerCase()}`}>
            {event.status}
          </span>
          {isAdmin && (
            <Link to={`/admin/events/${eventId}/roster`} className="admin-roster-link">
              Manage Roster
            </Link>
          )}
        </div>
      </div>

      <div className="event-info-panel">
        <p><strong>Location:</strong> {event.location}</p>
        <p><strong>Created:</strong> {new Date(event.createdAt).toLocaleString()}</p>
        <p className="event-description-text">{event.description}</p>

        {isAdmin && event.status !== 'CANCELLED' && (
          <button
            onClick={handleCancelEvent}
            disabled={cancelling}
            className="cancel-event-btn"
          >
            {cancelling ? 'Cancelling...' : 'Cancel Event'}
          </button>
        )}
      </div>

      <div className="shifts-section">
        <h2>Available Shifts</h2>
        {registerMessage && <p className="success-message">{registerMessage}</p>}
        {shifts.length === 0 ? (
          <p className="empty-state">No shifts scheduled for this event.</p>
        ) : (
          <div className="shifts-list">
            {shifts.map((shift) => {
              const isFull = shift.registeredCount >= shift.capacity;
              const formattedStart = new Date(shift.startTime).toLocaleString();
              const formattedEnd = new Date(shift.endTime).toLocaleString();

              return (
                <div key={shift.id} className="shift-card">
                  <div className="shift-details">
                    <p><strong>Time:</strong> {formattedStart} - {formattedEnd}</p>
                    <div className="shift-capacity-container">
                      <span className="shift-capacity-label"><strong>Capacity:</strong> {shift.registeredCount} / {shift.capacity} slots claimed</span>
                      <div className="shift-capacity-bar-track">
                        <div
                          className={`shift-capacity-bar-fill ${isFull ? 'full' : ''}`}
                          style={{ width: `${Math.min(100, (shift.registeredCount / shift.capacity) * 100)}%` }}
                        ></div>
                      </div>
                    </div>
                    {shift.requiredSkills && shift.requiredSkills.length > 0 && (
                      <p>
                        <strong>Required Skills:</strong>{' '}
                        {shift.requiredSkills.map((s, idx) => (
                          <span key={idx} className="skill-tag">{s}</span>
                        ))}
                      </p>
                    )}
                  </div>
                  <div className="shift-actions">
                    {isAuthenticated ? (
                      <button
                        onClick={() => handleRegister(shift.id)}
                        disabled={isFull || event.status === 'CANCELLED' || event.status === 'COMPLETED'}
                        className="register-btn"
                      >
                        {isFull ? 'Shift Full' : 'Register'}
                      </button>
                    ) : (
                      <Link to="/login" className="login-to-register-link">
                        Login to Register
                      </Link>
                    )}
                    {registerError[shift.id] && (
                      <p className="shift-error">{registerError[shift.id]}</p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
