import { useState, useEffect } from 'react';
import client from '../api/client';

export default function MyHoursPage() {
  const [totalHours, setTotalHours] = useState(0);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchHours() {
      setLoading(true);
      setError(null);
      try {
        const response = await client.get('/volunteers/me/hours');
        setTotalHours(response.data.totalHours || 0);
        setHistory(response.data.history || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load volunteer hours');
      } finally {
        setLoading(false);
      }
    }
    fetchHours();
  }, []);

  if (loading) return <p className="loading-state">Loading hours history...</p>;
  if (error) return <p className="error-state">{error}</p>;

  return (
    <div className="container">
      <h1>My Volunteer Hours</h1>

      <div className="hours-summary-card">
        <h2>Total Hours Contributed</h2>
        <p className="total-hours-number">{totalHours.toFixed(1)}</p>
      </div>

      <div className="hours-history-section">
        <h2>History</h2>
        {history.length === 0 ? (
          <p className="empty-state">No hours logged yet. Start registering and attending shifts!</p>
        ) : (
          <table className="hours-table">
            <thead>
              <tr>
                <th>Event Title</th>
                <th>Hours Logged</th>
                <th>Date Logged</th>
              </tr>
            </thead>
            <tbody>
              {history.map((log) => (
                <tr key={log.id}>
                  <td>{log.eventTitle}</td>
                  <td>{log.hours.toFixed(1)}</td>
                  <td>{new Date(log.loggedAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
