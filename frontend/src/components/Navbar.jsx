import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Navbar() {
  const { isAuthenticated, isAdmin, email, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <Link to="/">Volunteer Manager</Link>
      </div>
      <div className="nav-links">
        <Link to="/">Events</Link>
        {isAuthenticated ? (
          <>
            <Link to="/profile">Profile</Link>
            <Link to="/hours">My Hours</Link>
            {isAdmin && <Link to="/admin/events/new">Create Event</Link>}
            <span className="nav-user">{email}</span>
            <button onClick={handleLogout} className="logout-btn">
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
