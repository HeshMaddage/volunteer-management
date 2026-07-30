import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import EventsPage from './pages/EventsPage';
import EventDetailPage from './pages/EventDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import MyHoursPage from './pages/MyHoursPage';
import CreateEventPage from './pages/admin/CreateEventPage';
import RosterPage from './pages/admin/RosterPage';
import LandingPage from './pages/LandingPage';
import './App.css';

function AppContent() {
  const location = useLocation();
  const isLandingPage = location.pathname === '/';
  const isLoginPage = location.pathname === '/login';

  return (
    <>
      {!isLandingPage && !isLoginPage && <Navbar />}
      <main style={{ padding: (isLandingPage || isLoginPage) ? '0' : '20px', flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/events/:eventId" element={<EventDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected Volunteer Routes */}
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/hours"
            element={
              <ProtectedRoute>
                <MyHoursPage />
              </ProtectedRoute>
            }
          />

          {/* Protected Admin-only Routes */}
          <Route
            path="/admin/events/new"
            element={
              <ProtectedRoute adminOnly>
                <CreateEventPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/events/:eventId/roster"
            element={
              <ProtectedRoute adminOnly>
                <RosterPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

