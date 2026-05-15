import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({
    fullName: '', email: '', username: '', password: '', confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [passwordMatch, setPasswordMatch] = useState(true);
  const [usernameStatus, setUsernameStatus] = useState<'idle' | 'checking' | 'available' | 'taken'>('idle');
  const [emailStatus, setEmailStatus] = useState<'idle' | 'checking' | 'available' | 'taken'>('idle');

  useEffect(() => {
    if (!form.username || form.username.length < 3) {
      setUsernameStatus('idle');
      return;
    }
    setUsernameStatus('checking');
    const timer = setTimeout(async () => {
      try {
        const res = await authService.checkUsername(form.username);
        setUsernameStatus(res.data.data ? 'available' : 'taken');
      } catch {
        setUsernameStatus('idle');
      }
    }, 400);
    return () => clearTimeout(timer);
  }, [form.username]);

  useEffect(() => {
    if (!form.email || !form.email.includes('@')) {
      setEmailStatus('idle');
      return;
    }
    setEmailStatus('checking');
    const timer = setTimeout(async () => {
      try {
        const res = await authService.checkEmail(form.email);
        setEmailStatus(res.data.data ? 'available' : 'taken');
      } catch {
        setEmailStatus('idle');
      }
    }, 400);
    return () => clearTimeout(timer);
  }, [form.email]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (name === 'confirmPassword' || name === 'password') {
      const pwd = name === 'password' ? value : form.password;
      const cpwd = name === 'confirmPassword' ? value : form.confirmPassword;
      setPasswordMatch(cpwd === '' || pwd === cpwd);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (usernameStatus === 'taken') {
      setError('Username is already taken');
      return;
    }
    if (emailStatus === 'taken') {
      setError('Email is already registered');
      return;
    }
    try {
      const res = await authService.register(form);
      const data = res.data.data;
      await login(data.token);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  const getStatusIcon = (status: string) => {
    if (status === 'checking') return <span className="field-status checking">...</span>;
    if (status === 'available') return <span className="field-status available" style={{ color: '#58c322' }}>&#10003;</span>;
    if (status === 'taken') return <span className="field-status taken" style={{ color: '#ed4956' }}>&#10007;</span>;
    return null;
  };

  return (
    <div className="auth-page">
      <div className="auth-wrapper">
        <div className="auth-card">
          <h1 className="auth-logo">Instagram</h1>
          <p className="auth-subtitle">Sign up to see photos and videos from your friends.</p>

          {error && <div className="auth-error">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-with-status">
              <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
              {getStatusIcon(emailStatus)}
            </div>
            {emailStatus === 'taken' && <span className="field-hint taken">Email is already registered</span>}

            <input name="fullName" placeholder="Full Name" value={form.fullName} onChange={handleChange} required />

            <div className="input-with-status">
              <input name="username" placeholder="Username" value={form.username} onChange={handleChange} required />
              {getStatusIcon(usernameStatus)}
            </div>
            {usernameStatus === 'available' && <span className="field-hint available">Username is available</span>}
            {usernameStatus === 'taken' && <span className="field-hint taken">Username is already taken</span>}

            <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
            <input name="confirmPassword" type="password" placeholder="Confirm Password" value={form.confirmPassword} onChange={handleChange} required />
            {!passwordMatch && <span className="password-mismatch">Passwords do not match</span>}
            <button type="submit" className="auth-btn">Sign up</button>
          </form>
        </div>

        <div className="auth-switch-card">
          Have an account? <Link to="/login">Log in</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
