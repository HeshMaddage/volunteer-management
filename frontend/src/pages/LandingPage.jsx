import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useTheme } from '../theme/ThemeContext';
import {
  Heart,
  UserPlus,
  Search,
  Clock,
  UserCheck,
  Calendar,
  MousePointerClick,
  Activity,
  Users,
  SlidersHorizontal,
  ChevronDown,
  LogOut,
  User,
  Menu,
  X,
  Sun,
  Moon
} from 'lucide-react';
import './LandingPage.css';

export default function LandingPage() {
  return (
    <div className="landing-page-root">
      <LandingNavbar />
      <HeroSection />
      <HowItWorksSection />
      <FeaturesGridSection />
      <StatsStripSection />
      <CtaSection />
      <LandingFooter />
    </div>
  );
}

// Sticky Nav Bar Component
function LandingNavbar() {
  const { isAuthenticated, email, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  // Scroll effect to add shadow to navbar
  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 20) {
        setScrolled(true);
      } else {
        setScrolled(false);
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogoutClick = () => {
    logout();
    setDropdownOpen(false);
    setMobileMenuOpen(false);
    navigate('/');
  };

  return (
    <nav className={`lp-navbar ${scrolled ? 'scrolled' : ''}`}>
      <div className="lp-nav-brand">
        <Link to="/" className="lp-logo-link">
          <Heart className="lp-logo-icon" fill="currentColor" size={24} />
          <span>Volunex</span>
        </Link>
      </div>

      <button
        className="lp-nav-menu-toggle"
        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        aria-label="Toggle menu"
      >
        {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
      </button>

      <div className={`lp-nav-links ${mobileMenuOpen ? 'active' : ''}`}>
        <Link
          to="/events"
          className="lp-nav-link"
          onClick={() => setMobileMenuOpen(false)}
        >
          Browse Events
        </Link>

        <div className="lp-nav-actions">
          {isAuthenticated ? (
            <div className="lp-user-menu" ref={dropdownRef}>
              <button
                className="lp-user-trigger"
                onClick={() => setDropdownOpen(!dropdownOpen)}
              >
                <User size={16} />
                <span>{email?.split('@')[0]}</span>
                <ChevronDown size={14} />
              </button>

              {dropdownOpen && (
                <div className="lp-user-dropdown">
                  <div className="lp-user-info">Logged in as {email}</div>
                  <Link
                    to="/events"
                    className="lp-dropdown-link"
                    onClick={() => {
                      setDropdownOpen(false);
                      setMobileMenuOpen(false);
                    }}
                  >
                    Dashboard
                  </Link>
                  <Link
                    to="/profile"
                    className="lp-dropdown-link"
                    onClick={() => {
                      setDropdownOpen(false);
                      setMobileMenuOpen(false);
                    }}
                  >
                    My Profile
                  </Link>
                  <Link
                    to="/hours"
                    className="lp-dropdown-link"
                    onClick={() => {
                      setDropdownOpen(false);
                      setMobileMenuOpen(false);
                    }}
                  >
                    My Hours
                  </Link>
                  <button onClick={handleLogoutClick} className="lp-dropdown-btn">
                    <LogOut size={16} />
                    <span>Logout</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              {/* <Link
                to="/login"
                className="lp-btn lp-btn-login"
                onClick={() => setMobileMenuOpen(false)}
              >
                Login
              </Link> */}
              <Link
                to="/login"
                className="lp-btn lp-btn-primary"
                onClick={() => setMobileMenuOpen(false)}
              >
                Sign Up
              </Link>
            </>
          )}
        </div>
        <button
          onClick={toggleTheme}
          className="lp-theme-toggle-btn"
          aria-label="Toggle theme"
          style={{
            background: 'none',
            border: '1px solid var(--color-border)',
            borderRadius: '50%',
            color: 'var(--color-text)',
            padding: '0.5rem',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            transition: 'all 0.2s ease',
            width: '38px',
            height: '38px',
            marginLeft: '1rem',
            alignSelf: 'center'
          }}
        >
          {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
        </button>
      </div>
    </nav>
  );
}

// 2. Hero Section Component
function HeroSection() {
  const { isAuthenticated } = useAuth();

  return (
    <section className="lp-section lp-hero">
      <div className="lp-hero-content">
        <div className="lp-hero-tagline">
          <Heart size={14} fill="currentColor" />
          <span>Empowering Local Communities</span>
        </div>
        <h1 className="lp-hero-heading">
          Where Passion Meets Purpose. <span>Simplify Volunteering.</span>
        </h1>
        <p className="lp-hero-text">
          Connect with local opportunities, align shifts with your skills, and
          automatically track your impact. Built for active volunteers and organizers
          who want to make a difference without the paperwork.
        </p>
        <div className="lp-hero-ctas">
          <Link to="/events" className="lp-btn lp-btn-secondary lp-hero-btn">
            Browse Opportunities
          </Link>
          <Link
            to={isAuthenticated ? '/events' : '/register'}
            className="lp-btn lp-btn-primary lp-hero-btn"
          >
            {isAuthenticated ? 'Go to Dashboard' : 'Get Started'}
          </Link>
        </div>
      </div>
      <div className="lp-hero-visual">
        <HeroIllustration />
      </div>
    </section>
  );
}

// 3. How It Works Section Component
function HowItWorksSection() {
  return (
    <section className="lp-section lp-section-bg-cream">
      <div className="lp-section-title-wrapper">
        <span className="lp-section-label">Process</span>
        <h2 className="lp-section-title">How It Works</h2>
        <p className="lp-section-subtitle">
          Getting involved in your community is quick, structured, and entirely online.
        </p>
      </div>

      <div className="lp-steps-grid">
        <div className="lp-step-card">
          <div className="lp-step-icon-box">
            <UserPlus size={28} />
          </div>
          <span className="lp-step-number">Step 01</span>
          <h3 className="lp-step-heading">Create a Profile</h3>
          <p className="lp-step-description">
            Sign up in seconds and specify your unique skills, experience, and interests.
          </p>
        </div>

        <div className="lp-step-card">
          <div className="lp-step-icon-box">
            <Search size={28} />
          </div>
          <span className="lp-step-number">Step 02</span>
          <h3 className="lp-step-heading">Find Open Shifts</h3>
          <p className="lp-step-description">
            Browse upcoming events and register instantly for open slots matching your availability.
          </p>
        </div>

        <div className="lp-step-card">
          <div className="lp-step-icon-box">
            <Clock size={28} />
          </div>
          <span className="lp-step-number">Step 03</span>
          <h3 className="lp-step-heading">Auto Hour Crediting</h3>
          <p className="lp-step-description">
            Check-in at the venue. Your volunteer hours are automatically computed and saved on approval.
          </p>
        </div>
      </div>
    </section>
  );
}

// 4. Features Grid Component
function FeaturesGridSection() {
  return (
    <section className="lp-section">
      <div className="lp-section-title-wrapper">
        <span className="lp-section-label">Capabilities</span>
        <h2 className="lp-section-title">Designed for Real Impact</h2>
        <p className="lp-section-subtitle">
          Every tool you need to register, schedule, and verify community efforts, backed by design-level checks.
        </p>
      </div>

      <div className="lp-features-grid">
        <div className="lp-feature-card">
          <div className="lp-feature-icon-wrapper">
            <UserCheck size={22} />
          </div>
          <h3 className="lp-feature-heading">Skills Matching</h3>
          <p className="lp-feature-text">
            Add skills to your volunteer profile. Shift roles require matching skills, ensuring you can contribute effectively.
          </p>
        </div>

        <div className="lp-feature-card">
          <div className="lp-feature-icon-wrapper">
            <Calendar size={22} />
          </div>
          <h3 className="lp-feature-heading">Capacity & Shift Management</h3>
          <p className="lp-feature-text">
            Organizers create concrete shifts with max capacities. Prevents overcrowding and distributes shifts evenly.
          </p>
        </div>

        <div className="lp-feature-card">
          <div className="lp-feature-icon-wrapper">
            <MousePointerClick size={22} />
          </div>
          <h3 className="lp-feature-heading">One-Click Registration</h3>
          <p className="lp-feature-text">
            Claim open shifts with a single click. Our database enforces limits to prevent double-bookings or duplicate signs.
          </p>
        </div>

        <div className="lp-feature-card">
          <div className="lp-feature-icon-wrapper">
            <Activity size={22} />
          </div>
          <h3 className="lp-feature-heading">Automated Hour Logs</h3>
          <p className="lp-feature-text">
            Hours are automatically tracked and logged to your profile when your attendance is verified by event administrators.
          </p>
        </div>

        <div className="lp-feature-card">
          <div className="lp-feature-icon-wrapper">
            <Users size={22} />
          </div>
          <h3 className="lp-feature-heading">Roster Attendance Control</h3>
          <p className="lp-feature-text">
            Administrators manage volunteer rosters, register emergency help, and record attendance directly from their dashboard.
          </p>
        </div>

        <div className="lp-feature-card">
          <div className="lp-feature-icon-wrapper">
            <SlidersHorizontal size={22} />
          </div>
          <h3 className="lp-feature-heading">Multi-Attribute Filters</h3>
          <p className="lp-feature-text">
            Quickly filter opportunities by status or target skill sets. Navigate through pages to find the event that fits.
          </p>
        </div>
      </div>
    </section>
  );
}

// 5. Stats Strip Component
function StatsStripSection() {
  return (
    <section className="lp-stats-strip">
      <div className="lp-stats-container">
        <div className="lp-stat-box">
          <span className="lp-stat-title">Integrity Guaranteed</span>
          <span className="lp-stat-desc">Zero double-bookings by design</span>
        </div>
        <div className="lp-stat-box">
          <span className="lp-stat-title">Real-Time Verification</span>
          <span className="lp-stat-desc">Auto-credited volunteer hours</span>
        </div>
        <div className="lp-stat-box">
          <span className="lp-stat-title">Matching System</span>
          <span className="lp-stat-desc">Skills-aligned event rosters</span>
        </div>
      </div>
    </section>
  );
}

// 6. Call-to-action Banner Component
function CtaSection() {
  return (
    <section className="lp-cta-banner">
      <div className="lp-cta-content">
        <h2 className="lp-cta-title">Ready to Make an Impact?</h2>
        <p className="lp-cta-text">
          Join a growing community dedicated to transparent, structured coordination.
          Create your account today, match your skills to actual needs, and track your history.
        </p>
        <Link to="/register" className="lp-btn lp-cta-btn">
          Create Free Account
        </Link>
      </div>
    </section>
  );
}

// 7. Footer Component
function LandingFooter() {
  return (
    <footer className="lp-footer">
      <div className="lp-footer-container">
        <div className="lp-footer-brand">
          <div className="lp-footer-title">
            <Heart size={20} fill="#ea580c" color="#ea580c" />
            <span>Volunteer Manager</span>
          </div>
          <p className="lp-footer-tagline">
            A secure and streamlined platform designed to connect people with purpose and automate operational overhead.
          </p>
        </div>
        <div className="lp-footer-links-col">
          <h4 className="lp-footer-col-title">Navigation</h4>
          <ul className="lp-footer-links">
            <li>
              <Link to="/events" className="lp-footer-link">
                Browse Events
              </Link>
            </li>
            <li>
              <Link to="/login" className="lp-footer-link">
                Login
              </Link>
            </li>
            <li>
              <Link to="/register" className="lp-footer-link">
                Register
              </Link>
            </li>
          </ul>
        </div>
      </div>
      <div className="lp-footer-bottom">
        <p className="lp-footer-copyright">
          &copy; {new Date().getFullYear()} Volunteer Manager. Created for structured nonprofit operational coordination.
        </p>
      </div>
    </footer>
  );
}

// Inline Vector Illustration for Hero
function HeroIllustration() {
  return (
    <svg
      className="lp-hero-svg-wrapper"
      viewBox="0 0 500 500"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        {/* Soft Teal Gradient for Background Glow */}
        <radialGradient id="glow" cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
          <stop offset="0%" stopColor="#f0fdfa" stopOpacity="0.8" />
          <stop offset="100%" stopColor="#fdfbf7" stopOpacity="0" />
        </radialGradient>

        {/* Teal Primary Gradient */}
        <linearGradient id="tealGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#14b8a6" />
          <stop offset="100%" stopColor="#115e59" />
        </linearGradient>

        {/* Orange Accent Gradient */}
        <linearGradient id="orangeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#ffedd5" />
          <stop offset="100%" stopColor="#ea580c" />
        </linearGradient>

        {/* Coral Secondary Gradient */}
        <linearGradient id="coralGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#f97316" />
          <stop offset="100%" stopColor="#b91c1c" />
        </linearGradient>
      </defs>

      {/* Background Glow */}
      <circle cx="250" cy="250" r="230" fill="url(#glow)" />

      {/* Abstract Grid Elements */}
      <line x1="80" y1="250" x2="420" y2="250" stroke="#e7e5e4" strokeWidth="1" strokeDasharray="5 5" />
      <line x1="250" y1="80" x2="250" y2="420" stroke="#e7e5e4" strokeWidth="1" strokeDasharray="5 5" />
      <circle cx="250" cy="250" r="120" stroke="#e7e5e4" strokeWidth="1.5" />
      <circle cx="250" cy="250" r="180" stroke="#e7e5e4" strokeWidth="1" strokeDasharray="4 4" />

      {/* Connecting Network Node 1 (Teal Central Hub) */}
      <g filter="drop-shadow(0 10px 15px rgba(17, 94, 89, 0.25))">
        <rect x="200" y="200" width="100" height="100" rx="20" fill="url(#tealGrad)" />
        <path d="M235 250H265M250 235V265" stroke="#ffffff" strokeWidth="4" strokeLinecap="round" />
      </g>

      {/* Floating Card 1: Shift Capacity / Clock */}
      <g filter="drop-shadow(0 8px 20px rgba(0, 0, 0, 0.06))">
        <rect x="310" y="110" width="130" height="70" rx="14" fill="#ffffff" stroke="#e7e5e4" strokeWidth="1.5" />
        <circle cx="345" cy="145" r="16" fill="var(--lp-primary-light)" />
        {/* Clock Symbol */}
        <circle cx="345" cy="145" r="9" stroke="var(--lp-primary)" strokeWidth="2" />
        <path d="M345 140V145H350" stroke="var(--lp-primary)" strokeWidth="2" strokeLinecap="round" />

        {/* Fake text lines */}
        <rect x="372" y="136" width="50" height="6" rx="3" fill="#a8a29e" />
        <rect x="372" y="148" width="30" height="6" rx="3" fill="#d6d3d1" />
      </g>

      {/* Floating Card 2: Skill Verified Checkmark */}
      <g filter="drop-shadow(0 8px 20px rgba(0, 0, 0, 0.06))">
        <rect x="60" y="300" width="140" height="70" rx="14" fill="#ffffff" stroke="#e7e5e4" strokeWidth="1.5" />
        <circle cx="95" cy="335" r="16" fill="#ffedd5" />
        {/* Checkmark Symbol */}
        <path d="M90 335L93 338L100 331" stroke="var(--lp-accent)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />

        {/* Fake text lines */}
        <rect x="122" y="326" width="60" height="6" rx="3" fill="#ea580c" opacity="0.8" />
        <rect x="122" y="338" width="40" height="6" rx="3" fill="#d6d3d1" />
      </g>

      {/* Supporting Decorative Shapes */}
      {/* Sparkles / Stars */}
      <path d="M120 150L122 158L130 160L122 162L120 170L118 162L110 160L118 158L120 150Z" fill="url(#orangeGrad)" />
      <path d="M380 340L381.5 346L387.5 347.5L381.5 349L380 355L378.5 349L372.5 347.5L378.5 346L380 340Z" fill="url(#tealGrad)" />

      {/* Circle Nodes with Icons */}
      <circle cx="250" cy="70" r="10" fill="var(--lp-primary)" />
      <circle cx="410" cy="270" r="8" fill="var(--lp-accent)" />
      <circle cx="90" cy="210" r="12" fill="#e2e8f0" />

      {/* Dynamic Swirly/Connector Line */}
      <path d="M102 210C160 210 180 150 230 110" stroke="var(--lp-primary)" strokeWidth="2" strokeLinecap="round" opacity="0.3" />
      <path d="M360 180C340 220 380 290 402 270" stroke="var(--lp-accent)" strokeWidth="1.5" strokeLinecap="round" opacity="0.3" />
    </svg>
  );
}
