import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { User, Post } from '../../types';
import { authService } from '../../services/authService';
import { postService } from '../../services/postService';
import PostCard from '../posts/PostCard';
import './Search.css';

const Search: React.FC = () => {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';
  const [activeTab, setActiveTab] = useState<'users' | 'posts' | 'hashtags'>('users');
  const [users, setUsers] = useState<User[]>([]);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(false);
  const [sortBy, setSortBy] = useState<'relevance' | 'recent' | 'popular'>('relevance');

  useEffect(() => {
    if (!query) return;

    const search = async () => {
      setLoading(true);
      setUsers([]);
      setPosts([]);
      try {
        if (activeTab === 'users') {
          const res = await authService.searchUsers(query.replace('#', ''));
          setUsers(res.data.data);
        } else if (activeTab === 'posts') {
          const res = await postService.searchPosts(query);
          setPosts(res.data.data?.content || []);
        } else if (activeTab === 'hashtags') {
          const hashtag = query.replace('#', '');
          const res = await postService.getPostsByHashtag(hashtag);
          setPosts(res.data.data?.content || []);
        }
      } catch {
        // Search failed silently - UI shows empty state
      } finally {
        setLoading(false);
      }
    };

    search();
  }, [query, activeTab]);

  const sortedPosts = [...posts].sort((a, b) => {
    if (sortBy === 'recent') return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    if (sortBy === 'popular') return b.likesCount - a.likesCount;
    return 0;
  });

  return (
    <div className="search-container">
      <h2 className="search-title">Search: "{query}"</h2>

      <div className="search-tabs">
        <button className={activeTab === 'users' ? 'active' : ''} onClick={() => setActiveTab('users')}>Users</button>
        <button className={activeTab === 'posts' ? 'active' : ''} onClick={() => setActiveTab('posts')}>Posts</button>
        <button className={activeTab === 'hashtags' ? 'active' : ''} onClick={() => setActiveTab('hashtags')}>Hashtags</button>
      </div>

      {(activeTab === 'posts' || activeTab === 'hashtags') && (
        <div className="search-filters">
          <label>Sort by:</label>
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value as 'relevance' | 'recent' | 'popular')}>
            <option value="relevance">Relevance</option>
            <option value="recent">Most Recent</option>
            <option value="popular">Most Popular</option>
          </select>
        </div>
      )}

      {loading && <div className="loading">Searching...</div>}

      {!loading && activeTab === 'users' && (
        <div className="search-results">
          {users.length === 0 ? (
            <p className="no-results">No users found</p>
          ) : (
            users.map((user) => (
              <Link key={user.id} to={`/profile/${user.username}`} className="user-result">
                <div className="user-avatar-small">
                  {user.profilePicture ? (
                    <img src={user.profilePicture} alt={user.username} />
                  ) : (
                    <div className="avatar-placeholder-small">{user.fullName?.[0] || user.username?.[0] || '?'}</div>
                  )}
                </div>
                <div className="user-result-info">
                  <span className="user-result-username">{user.username}</span>
                  <span className="user-result-name">{user.fullName}</span>
                </div>
              </Link>
            ))
          )}
        </div>
      )}

      {!loading && (activeTab === 'posts' || activeTab === 'hashtags') && (
        <div className="search-results">
          {sortedPosts.length === 0 ? (
            <p className="no-results">No posts found</p>
          ) : (
            sortedPosts.map((post) => <PostCard key={post.id} post={post} />)
          )}
        </div>
      )}
    </div>
  );
};

export default Search;
