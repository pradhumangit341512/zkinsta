import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [failedAttempts, setFailedAttempts] = useState(0);
  const [lockoutTimer, setLockoutTimer] = useState(0);

  useEffect(() => {
    if (lockoutTimer > 0) {
      const interval = setInterval(() => {
        setLockoutTimer((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(interval);
    } else if (lockoutTimer === 0 && failedAttempts >= 3) {
      setFailedAttempts(0);
    }
  }, [lockoutTimer, failedAttempts]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (lockoutTimer > 0) {
      setError(`Too many failed attempts. Please wait ${lockoutTimer} seconds.`);
      return;
    }
    try {
      const res = await authService.login(form);
      const data = res.data.data;
      await login(data.token);
      navigate('/');
    } catch (err: any) {
      const newAttempts = failedAttempts + 1;
      setFailedAttempts(newAttempts);
      if (newAttempts >= 3) {
        setLockoutTimer(60);
        setError('Too many failed login attempts. Please wait for 1 minute before trying again.');
      } else {
        setError(err.response?.data?.message || 'Invalid username or password');
      }
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-wrapper">
        <div className="auth-card">
          <h1 className="auth-logo">Instagram</h1>

          {error && <div className="auth-error">{error}</div>}
          {lockoutTimer > 0 && (
            <div className="lockout-timer">
              Account locked. Try again in: <strong>{lockoutTimer}s</strong>
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-form">
            <input name="username" placeholder="Phone number, username, or email" value={form.username} onChange={handleChange} required />
            <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
            <button type="submit" className="auth-btn" disabled={lockoutTimer > 0 || !form.username || !form.password}>
              Log In
            </button>
          </form>

          <div className="auth-divider">
            <span>OR</span>
          </div>

          <Link to="/forgot-password" className="forgot-link">Forgot password?</Link>
        </div>

        <div className="auth-switch-card">
          Don't have an account? <Link to="/register">Sign up</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
