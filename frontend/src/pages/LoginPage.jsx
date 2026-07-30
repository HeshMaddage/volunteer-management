import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { AtSign, Lock, Eye, EyeOff, Heart, ArrowLeft } from 'lucide-react';
import volunteersImage from '../assets/volunteers_login.png';
import './LoginPage.css';

const GoogleIcon = () => (
  <svg className="social-icon" viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg">
    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
  </svg>
);

const AppleIcon = () => (
  <svg className="social-icon" viewBox="0 0 24 24" width="18" height="18" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
    <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 4.17c.66-.81 1.11-1.93.99-3.06-1 .04-2.22.67-2.94 1.51-.64.73-1.2 1.87-1.05 2.98 1.12.09 2.27-.58 3-.143"/>
  </svg>
);

export default function LoginPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [rememberMe, setRememberMe] = useState(false);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setIsLoading(true);
        try {
            await login(email, password);
            navigate('/');
        } catch (err) {
            setError('Invalid email or password');
        } finally {
            setIsLoading(false);
        }
    };

    const handleSocialLogin = (platform) => {
        alert(`${platform} login is a demo placeholder. Please use your standard volunteer credentials to sign in.`);
    };

    const handleForgotPassword = (e) => {
        e.preventDefault();
        alert('Password recovery feature is under development. Please contact the administrator to reset your password.');
    };

    return (
        <div className="login-page-container">
            {/* Left Panel: Volunteer Image Banner */}
            <div className="login-image-panel" style={{ backgroundImage: `url(${volunteersImage})` }}>
                <div className="login-image-overlay" />
                <div className="login-image-text">
                    <h2>Make a Difference Today</h2>
                    <p>Join our passionate community of volunteers. Log in to track your service hours, explore upcoming community events, and coordinate with coordinators.</p>
                </div>
            </div>

            {/* Right Panel: Styled Login Card */}
            <div className="login-form-panel">
                <div className="login-card">
                    {/* Header */}
                    <div className="login-header">
                        <Link to="/" className="login-header-logo">
                            <Heart fill="currentColor" size={20} />
                            <span>Volunteer Manager</span>
                        </Link>
                        <h1>Sign In</h1>
                        <p>Welcome back! Please enter your credentials.</p>
                    </div>

                    {/* Back to Home helper button */}
                    <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', textDecoration: 'none', color: 'var(--color-text-muted)', fontSize: '0.85rem', marginBottom: '1.25rem', fontWeight: 500 }} className="back-link">
                        <ArrowLeft size={14} /> Back to Home
                    </Link>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="login-form">
                        {/* Email Field */}
                        <div className="login-form-group">
                            <label htmlFor="email">Email</label>
                            <div className="input-wrapper">
                                <AtSign size={16} className="input-icon-left" />
                                <input
                                    type="email"
                                    id="email"
                                    className="input-field"
                                    placeholder="Enter your Email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div className="login-form-group">
                            <label htmlFor="password">Password</label>
                            <div className="input-wrapper">
                                <Lock size={16} className="input-icon-left" />
                                <input
                                    type={showPassword ? "text" : "password"}
                                    id="password"
                                    className="input-field"
                                    placeholder="Enter your Password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                                <button
                                    type="button"
                                    className="input-icon-right"
                                    onClick={() => setShowPassword(!showPassword)}
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                >
                                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                                </button>
                            </div>
                        </div>

                        {/* Remember Me & Forgot Password */}
                        <div className="form-options">
                            <label className="checkbox-container">
                                <input
                                    type="checkbox"
                                    checked={rememberMe}
                                    onChange={(e) => setRememberMe(e.target.checked)}
                                />
                                <span>Remember me</span>
                            </label>
                            <a href="#" onClick={handleForgotPassword} className="forgot-password-link">
                                Forgot password?
                            </a>
                        </div>

                        {/* Error Message */}
                        {error && <p className="error-message">{error}</p>}

                        {/* Sign In Button */}
                        <button type="submit" className="signin-btn" disabled={isLoading}>
                            {isLoading ? 'Signing In...' : 'Sign In'}
                        </button>
                    </form>

                    {/* Sign Up Prompt */}
                    <p className="signup-prompt">
                        Don't have an account? 
                        <Link to="/register">Sign Up</Link>
                    </p>

                    {/* Or With Divider */}
                    <div className="divider">Or With</div>

                    {/* Social Buttons */}
                    <div className="social-container">
                        <button type="button" className="social-btn" onClick={() => handleSocialLogin('Google')}>
                            <GoogleIcon />
                            <span>Google</span>
                        </button>
                        <button type="button" className="social-btn" onClick={() => handleSocialLogin('Apple')}>
                            <AppleIcon />
                            <span>Apple</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}