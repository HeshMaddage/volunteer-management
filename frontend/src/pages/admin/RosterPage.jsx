import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import client from '../../api/client';

export default function RosterPage() {
  const { eventId } = useParams();
  const [roster, setRoster] = useState([]);
  const [eventTitle, setEventTitle] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionError, setActionError] = useState(null);

  const fetchRosterAndEvent = useCallback(async () => {
    setLoading(true);
    setError(null);
    setActionError(null);
    try {
      const [rosterRes, eventRes] = await Promise.all([
        client.get(`/events/${eventId}/roster`),
        client.get(`/events/${eventId}`).catch(() => null), // fail gracefully if event details fail
      ]);
      setRoster(rosterRes.data || []);
      if (eventRes) {
        setEventTitle(eventRes.data.title);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load event roster');
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  useEffect(() => {
    fetchRosterAndEvent();
  }, [fetchRosterAndEvent]);

  const handleUpdateStatus = async (registrationId, newStatus) => {
    setActionError(null);
    try {
      await client.patch(`/registrations/${registrationId}/attendance`, {
        status: newStatus,
      });
      // Refresh roster data
      const rosterRes = await client.get(`/events/${eventId}/roster`);
      setRoster(rosterRes.data || []);
    } catch (err) {
      const msg = err.response?.data?.message || `Failed to update status to ${newStatus}`;
      setActionError(msg);
    }
  };

  if (loading) return <p className="loading-state">Loading roster...</p>;
  if (error) return <p className="error-state">{error}</p>;

  return (
    <div className="container">
      <div className="roster-header">
        <h1>Event Roster</h1>
        {eventTitle && <h2>Event: {eventTitle}</h2>}
        <Link to={`/events/${eventId}`} className="back-link">
          &larr; Back to Event Details
        </Link>
      </div>

      {actionError && <p className="error-message">{actionError}</p>}

      <div className="roster-section">
        {roster.length === 0 ? (
          <p className="empty-state">No volunteers registered for this event yet.</p>
        ) : (
          <table className="roster-table">
            <thead>
              <tr>
                <th>Volunteer Name</th>
                <th>Shift ID</th>
                <th>Status</th>
                <th>Registered At</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {roster.map((registration) => (
                <tr key={registration.registrationId}>
                  <td>{registration.volunteerName}</td>
                  <td>Shift #{registration.shiftId}</td>
                  <td>
                    <span className={`status-badge status-${registration.status.toLowerCase()}`}>
                      {registration.status}
                    </span>
                  </td>
                  <td>{new Date(registration.registeredAt).toLocaleString()}</td>
                  <td>
                    {registration.status === 'REGISTERED' ? (
                      <div className="roster-actions">
                        <button
                          onClick={() =>
                            handleUpdateStatus(registration.registrationId, 'ATTENDED')
                          }
                          className="roster-btn btn-attended"
                        >
                          Mark Attended
                        </button>
                        <button
                          onClick={() =>
                            handleUpdateStatus(registration.registrationId, 'NO_SHOW')
                          }
                          className="roster-btn btn-noshow"
                        >
                          Mark No-Show
                        </button>
                      </div>
                    ) : (
                      <span className="roster-action-done">No pending actions</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
