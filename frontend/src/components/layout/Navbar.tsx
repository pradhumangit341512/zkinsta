import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { notificationService } from '../../services/notificationService';
import { authService } from '../../services/authService';
import { trendingService } from '../../services/trendingService';
import { Notification, User, TrendingHashtag } from '../../types';
import './Navbar.css';

const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchQuery, setSearchQuery] = useState('');
  const [showSearch, setShowSearch] = useState(false);

  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const notificationRef = useRef<HTMLDivElement>(null);

  const [suggestedUsers, setSuggestedUsers] = useState<User[]>([]);
  const [suggestedTags, setSuggestedTags] = useState<TrendingHashtag[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const searchDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const searchContainerRef = useRef<HTMLDivElement>(null);

  const [showMore, setShowMore] = useState(false);
  const moreRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isAuthenticated) return;
    const fetchNotifications = async () => {
      try {
        const [notifRes, countRes] = await Promise.all([
          notificationService.getNotifications(0, 10),
          notificationService.getUnreadCount(),
        ]);
        setNotifications(notifRes.data.data?.content || []);
        setUnreadCount(countRes.data.data || 0);
      } catch {}
    };
    fetchNotifications();
    const intervalId = setInterval(fetchNotifications, 30000);
    return () => clearInterval(intervalId);
  }, [isAuthenticated]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (notificationRef.current && !notificationRef.current.contains(e.target as Node)) {
        setShowNotifications(false);
      }
      if (searchContainerRef.current && !searchContainerRef.current.contains(e.target as Node)) {
        setShowSearch(false);
        setShowSuggestions(false);
      }
      if (moreRef.current && !moreRef.current.contains(e.target as Node)) {
        setShowMore(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchQuery(value);
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    if (!value.trim()) {
      setSuggestedUsers([]);
      setSuggestedTags([]);
      setShowSuggestions(false);
      return;
    }
    searchDebounceRef.current = setTimeout(async () => {
      try {
        const [usersRes, tagsRes] = await Promise.all([
          authService.searchUsers(value.trim()),
          trendingService.searchHashtags(value.trim()),
        ]);
        setSuggestedUsers((usersRes.data.data || []).slice(0, 5));
        setSuggestedTags((tagsRes.data.data || []).slice(0, 5));
        setShowSuggestions(true);
      } catch {}
    }, 300);
  }, []);

  useEffect(() => {
    return () => { if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current); };
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setShowSuggestions(false);
    setShowSearch(false);
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.read) {
      try {
        await notificationService.markAsRead(notification.id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === notification.id ? { ...n, read: true } : n))
        );
        setUnreadCount((prev) => Math.max(0, prev - 1));
      } catch {}
    }
    setShowNotifications(false);
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch {}
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="sidebar">
      <div className="sidebar-inner">
        <Link to="/" className="sidebar-logo">
          <span className="logo-full">Instagram</span>
          <span className="logo-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="2" width="20" height="20" rx="5" />
              <circle cx="12" cy="12" r="5" />
              <circle cx="17.5" cy="6.5" r="1.5" fill="currentColor" stroke="none" />
            </svg>
          </span>
        </Link>

        <div className="sidebar-nav">
          <Link to="/" className={`sidebar-link${isActive('/') ? ' active' : ''}`}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill={isActive('/') ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            <span>Home</span>
          </Link>

          <div ref={searchContainerRef} className="sidebar-search-container">
            <button
              className={`sidebar-link${showSearch ? ' active' : ''}`}
              onClick={() => { setShowSearch(!showSearch); setShowNotifications(false); }}
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <span>Search</span>
            </button>

            {showSearch && (
              <div className="search-panel">
                <div className="search-panel-header">
                  <h3>Search</h3>
                </div>
                <form onSubmit={handleSearch} className="search-panel-form">
                  <input
                    type="text"
                    placeholder="Search"
                    value={searchQuery}
                    onChange={handleSearchChange}
                    onFocus={() => {
                      if (suggestedUsers.length || suggestedTags.length) setShowSuggestions(true);
                    }}
                    autoFocus
                  />
                  {searchQuery && (
                    <button type="button" className="search-clear" onClick={() => { setSearchQuery(''); setSuggestedUsers([]); setSuggestedTags([]); }}>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="#8e8e8e">
                        <circle cx="12" cy="12" r="10" />
                        <path d="M15 9l-6 6M9 9l6 6" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
                      </svg>
                    </button>
                  )}
                </form>
                <div className="search-panel-results">
                  {showSuggestions && (suggestedUsers.length > 0 || suggestedTags.length > 0) ? (
                    <>
                      {suggestedUsers.map((u) => (
                        <Link
                          key={u.id}
                          to={`/profile/${u.username}`}
                          className="search-result-item"
                          onClick={() => { setShowSearch(false); setSearchQuery(''); }}
                        >
                          <div className="search-result-avatar">
                            {u.profilePicture ? (
                              <img src={u.profilePicture} alt={u.username} />
                            ) : (
                              <div className="avatar-placeholder-xs">{u.fullName?.[0] || u.username?.[0]}</div>
                            )}
                          </div>
                          <div className="search-result-info">
                            <span className="search-result-username">{u.username}</span>
                            <span className="search-result-name">{u.fullName}</span>
                          </div>
                        </Link>
                      ))}
                      {suggestedTags.map((h) => (
                        <Link
                          key={h.id}
                          to={`/search?q=%23${h.hashtag}`}
                          className="search-result-item"
                          onClick={() => { setShowSearch(false); setSearchQuery(''); }}
                        >
                          <div className="search-result-avatar tag-icon">
                            <span>#</span>
                          </div>
                          <div className="search-result-info">
                            <span className="search-result-username">#{h.hashtag}</span>
                            <span className="search-result-name">{h.postCount} posts</span>
                          </div>
                        </Link>
                      ))}
                    </>
                  ) : (
                    <div className="search-panel-empty">
                      <span>Recent</span>
                      <p>No recent searches.</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          <Link to="/trending" className={`sidebar-link${isActive('/trending') ? ' active' : ''}`}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
            <span>Explore</span>
          </Link>

          {isAuthenticated && (
            <>
              <div ref={notificationRef} className="sidebar-notif-container">
                <button
                  className={`sidebar-link${showNotifications ? ' active' : ''}`}
                  onClick={() => { setShowNotifications(!showNotifications); setShowSearch(false); }}
                >
                  <div className="notif-icon-wrapper">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill={showNotifications ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                    </svg>
                    {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
                  </div>
                  <span>Notifications</span>
                </button>

                {showNotifications && (
                  <div className="notif-panel">
                    <div className="notif-panel-header">
                      <h3>Notifications</h3>
                      {unreadCount > 0 && <button onClick={handleMarkAllRead}>Mark all read</button>}
                    </div>
                    {notifications.length === 0 ? (
                      <p className="notif-empty">No notifications yet</p>
                    ) : (
                      notifications.map((n) => (
                        <div
                          key={n.id}
                          className={`notif-item${n.read ? '' : ' unread'}`}
                          onClick={() => handleNotificationClick(n)}
                        >
                          <span>{n.message}</span>
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>

              <Link to="/create-post" className={`sidebar-link${isActive('/create-post') ? ' active' : ''}`}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2" />
                  <line x1="12" y1="8" x2="12" y2="16" />
                  <line x1="8" y1="12" x2="16" y2="12" />
                </svg>
                <span>Create</span>
              </Link>

              <Link to={`/profile/${user?.username}`} className={`sidebar-link${location.pathname.startsWith('/profile') ? ' active' : ''}`}>
                <div className="sidebar-profile-pic">
                  {user?.profilePicture ? (
                    <img src={user.profilePicture} alt={user.username} />
                  ) : (
                    <div className="avatar-placeholder-xs">{user?.fullName?.[0] || user?.username?.[0] || '?'}</div>
                  )}
                </div>
                <span>Profile</span>
              </Link>
            </>
          )}

          {!isAuthenticated && (
            <>
              <Link to="/login" className={`sidebar-link${isActive('/login') ? ' active' : ''}`}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                  <polyline points="10 17 15 12 10 7" />
                  <line x1="15" y1="12" x2="3" y2="12" />
                </svg>
                <span>Log in</span>
              </Link>
            </>
          )}
        </div>

        <div ref={moreRef} className="sidebar-bottom">
          <button className="sidebar-link" onClick={() => setShowMore(!showMore)}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="3" y1="12" x2="21" y2="12" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <line x1="3" y1="18" x2="21" y2="18" />
            </svg>
            <span>More</span>
          </button>

          {showMore && (
            <div className="more-menu">
              {isAuthenticated && (
                <button onClick={handleLogout} className="more-menu-item logout">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  Log out
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Mobile bottom bar */}
      <div className="mobile-bottom-bar">
        <Link to="/" className={`mobile-nav-item${isActive('/') ? ' active' : ''}`}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill={isActive('/') ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" /><polyline points="9 22 9 12 15 12 15 22" /></svg>
        </Link>
        <Link to="/search" className={`mobile-nav-item${isActive('/search') ? ' active' : ''}`}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
        </Link>
        {isAuthenticated && (
          <>
            <Link to="/create-post" className={`mobile-nav-item${isActive('/create-post') ? ' active' : ''}`}>
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2" /><line x1="12" y1="8" x2="12" y2="16" /><line x1="8" y1="12" x2="16" y2="12" /></svg>
            </Link>
            <Link to="/trending" className={`mobile-nav-item${isActive('/trending') ? ' active' : ''}`}>
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" /></svg>
            </Link>
            <Link to={`/profile/${user?.username}`} className={`mobile-nav-item${location.pathname.startsWith('/profile') ? ' active' : ''}`}>
              <div className="mobile-profile-pic">
                {user?.profilePicture ? (
                  <img src={user.profilePicture} alt="" />
                ) : (
                  <div className="avatar-placeholder-xxs">{user?.fullName?.[0] || '?'}</div>
                )}
              </div>
            </Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
