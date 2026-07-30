import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Heart, Sun, Moon } from 'lucide-react';
import { useTheme } from '../theme/ThemeContext';

export default function Navbar() {
  const { isAuthenticated, isAdmin, email, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
          <Heart fill="currentColor" size={20} />
          <span>Volunteer Manager</span>
        </Link>
      </div>
      <div className="nav-links">
        <Link to="/events">Events</Link>
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
        <button
          onClick={toggleTheme}
          className="theme-toggle-btn"
          aria-label="Toggle theme"
        >
          {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
        </button>
      </div>
    </nav>
  );
}
