import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client, { API_ORIGIN } from '../api/client';
import { Calendar } from 'lucide-react';

export default function EventsPage() {
  const [events, setEvents] = useState([]);
  const [skills, setSkills] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedSkill, setSelectedSkill] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchSkills() {
      try {
        const response = await client.get('/skills');
        setSkills(response.data);
      } catch (err) {
        console.error('Error fetching skills:', err);
      }
    }
    fetchSkills();
  }, []);

  useEffect(() => {
    async function fetchEvents() {
      setLoading(true);
      setError(null);
      try {
        const params = {
          page: currentPage,
          size: 5,
        };
        if (selectedStatus) params.status = selectedStatus;
        if (selectedSkill) params.skill = selectedSkill;

        const response = await client.get('/events', { params });
        const data = response.data;
        setEvents(data.content || []);
        setTotalPages(data.totalPages || 0);
      } catch (err) {
        const msg = err.response?.data?.message || 'Failed to fetch events';
        setError(msg);
      } finally {
        setLoading(false);
      }
    }
    fetchEvents();
  }, [selectedStatus, selectedSkill, currentPage]);

  const handleStatusChange = (e) => {
    setSelectedStatus(e.target.value);
    setCurrentPage(0);
  };

  const handleSkillChange = (e) => {
    setSelectedSkill(e.target.value);
    setCurrentPage(0);
  };

  return (
    <div className="container">
      <h1>Events</h1>

      <div className="filters-bar">
        <div className="filter-group">
          <label htmlFor="status-filter">Status:</label>
          <select
            id="status-filter"
            value={selectedStatus}
            onChange={handleStatusChange}
          >
            <option value="">All Statuses</option>
            <option value="UPCOMING">Upcoming</option>
            <option value="ONGOING">Ongoing</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="skill-filter">Required Skill:</label>
          <select
            id="skill-filter"
            value={selectedSkill}
            onChange={handleSkillChange}
          >
            <option value="">All Skills</option>
            {skills.map((skill) => (
              <option key={skill.id} value={skill.name}>
                {skill.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <p className="loading-state">Loading events...</p>
      ) : error ? (
        <p className="error-state">{error}</p>
      ) : events.length === 0 ? (
        <p className="empty-state">No events found matching the criteria.</p>
      ) : (
        <div className="events-list">
          {events.map((event) => (
             <div key={event.id} className="event-card">
               <div className="event-card-image-container">
                 {event.imageUrl ? (
                   <img
                     src={`${API_ORIGIN}${event.imageUrl}`}
                     alt={event.title}
                     className="event-card-image"
                   />
                 ) : (
                   <div className="event-card-image-placeholder">
                     <Calendar className="placeholder-icon" size={36} />
                   </div>
                 )}
               </div>
               <div className="event-card-body">
                 <div className="event-card-header">
                   <h2>{event.title}</h2>
                   <span className={`status-badge status-${event.status.toLowerCase()}`}>
                     {event.status}
                   </span>
                 </div>
                 <p className="event-location">
                   <strong>Location:</strong> {event.location}
                 </p>
                 <p className="event-desc">{event.description}</p>
                 <div className="event-card-actions">
                   <Link to={`/events/${event.id}`} className="view-details-btn">
                     View Shifts &amp; Details
                   </Link>
                 </div>
               </div>
             </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button
            onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
            disabled={currentPage === 0}
            className="pagination-btn"
          >
            Previous
          </button>
          <span className="pagination-info">
            Page {currentPage + 1} of {totalPages}
          </span>
          <button
            onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={currentPage >= totalPages - 1}
            className="pagination-btn"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
