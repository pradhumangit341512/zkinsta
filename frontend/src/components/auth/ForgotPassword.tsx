import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { authService } from '../../services/authService';
import './Auth.css';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [step, setStep] = useState<'email' | 'reset' | 'done'>('email');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const handleRequestReset = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await authService.forgotPassword(email);
      setMessage('If an account exists with this email, a password reset link has been sent. Enter your reset token below.');
      setStep('reset');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Failed to send reset request');
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    try {
      await authService.resetPassword({ token, newPassword, confirmPassword });
      setStep('done');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Password reset failed');
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-wrapper">
        <div className="auth-card">
          <div style={{ margin: '12px 0 16px' }}>
            <svg width="96" height="96" viewBox="0 0 24 24" fill="none" stroke="#a8a8a8" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </div>
          <h2 className="auth-title">Trouble logging in?</h2>
          <p style={{ color: '#a8a8a8', fontSize: 14, marginBottom: 16, lineHeight: 1.5 }}>
            Enter your email and we'll send you a link to get back into your account.
          </p>

          {error && <div className="auth-error">{error}</div>}
          {message && <div className="auth-success">{message}</div>}

          {step === 'email' && (
            <form onSubmit={handleRequestReset} className="auth-form">
              <input type="email" placeholder="Email" value={email}
                onChange={(e) => setEmail(e.target.value)} required />
              <button type="submit" className="auth-btn">Send Reset Token</button>
            </form>
          )}

          {step === 'reset' && (
            <form onSubmit={handleResetPassword} className="auth-form">
              <input type="text" placeholder="Reset Token" value={token}
                onChange={(e) => setToken(e.target.value)} required />
              <input type="password" placeholder="New Password" value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)} required />
              <input type="password" placeholder="Confirm New Password" value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)} required />
              <button type="submit" className="auth-btn">Reset Password</button>
            </form>
          )}

          {step === 'done' && (
            <div>
              <p className="auth-success">Password reset successful!</p>
              <Link to="/login" className="auth-btn" style={{ display: 'block', textAlign: 'center', textDecoration: 'none', padding: '10px', marginTop: 12 }}>
                Back to Login
              </Link>
            </div>
          )}

          <div className="auth-divider">
            <span>OR</span>
          </div>

          <Link to="/register" style={{ color: '#f5f5f5', fontSize: 14, fontWeight: 600 }}>Create new account</Link>
        </div>

        <div className="auth-switch-card">
          <Link to="/login" style={{ color: '#f5f5f5', fontWeight: 600 }}>Back to login</Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
