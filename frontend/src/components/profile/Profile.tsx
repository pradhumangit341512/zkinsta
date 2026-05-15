import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { User, Post, FollowCount } from '../../types';
import { authService } from '../../services/authService';
import { postService } from '../../services/postService';
import { getMediaUrl } from '../../services/api';
import { followService } from '../../services/followService';
import { useAuth } from '../../context/AuthContext';
import PostCard from '../posts/PostCard';
import './Profile.css';

const Profile: React.FC = () => {
  const { username } = useParams<{ username: string }>();
  const { user: currentUser, refreshProfile } = useAuth();
  const [profileUser, setProfileUser] = useState<User | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [followCounts, setFollowCounts] = useState<FollowCount | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({ fullName: '', bio: '' });
  const [loading, setLoading] = useState(true);
  const [followLoading, setFollowLoading] = useState(false);
  const [viewMode, setViewMode] = useState<'grid' | 'feed'>('grid');

  const isOwnProfile = currentUser?.username === username;

  const loadProfile = useCallback(async () => {
    if (!username) return;
    setLoading(true);
    try {
      const profileRes = await authService.getProfileByUsername(username);
      const user = profileRes.data.data;
      setProfileUser(user);
      setEditForm({ fullName: user.fullName, bio: user.bio || '' });

      const [postsRes, countsRes] = await Promise.all([
        postService.getPostsByUser(user.id),
        followService.getFollowCounts(user.id),
      ]);
      setPosts(postsRes.data.data);
      setFollowCounts(countsRes.data.data);

      if (currentUser && !isOwnProfile) {
        const followRes = await followService.isFollowing(user.id);
        setIsFollowing(followRes.data.data);
      }
    } catch (err) {
      console.error('Failed to load profile', err);
    } finally {
      setLoading(false);
    }
  }, [username, currentUser, isOwnProfile]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const handleFollow = async () => {
    if (!profileUser || followLoading) return;
    setFollowLoading(true);
    const prevIsFollowing = isFollowing;
    const prevCounts = followCounts;
    try {
      if (isFollowing) {
        setIsFollowing(false);
        setFollowCounts((prev) => prev ? { ...prev, followersCount: prev.followersCount - 1 } : prev);
        await followService.unfollowUser(profileUser.id);
      } else {
        setIsFollowing(true);
        setFollowCounts((prev) => prev ? { ...prev, followersCount: prev.followersCount + 1 } : prev);
        await followService.followUser(profileUser.id, profileUser.username);
      }
    } catch {
      setIsFollowing(prevIsFollowing);
      setFollowCounts(prevCounts);
    } finally {
      setFollowLoading(false);
    }
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await authService.updateProfile(editForm);
      await refreshProfile();
      setIsEditing(false);
      loadProfile();
    } catch {}
  };

  const handleDeletePost = (postId: number) => {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
  };

  if (loading) return <div className="loading">Loading profile...</div>;
  if (!profileUser) return <div className="loading">User not found</div>;

  return (
    <div className="profile-container">
      <div className="profile-header">
        <div className="profile-avatar">
          {profileUser.profilePicture ? (
            <img src={profileUser.profilePicture} alt={profileUser.username} />
          ) : (
            <div className="avatar-placeholder">{profileUser.fullName?.[0] || profileUser.username?.[0] || '?'}</div>
          )}
        </div>

        <div className="profile-info">
          <div className="profile-top">
            <h2 className="profile-username">{profileUser.username}</h2>
            {isOwnProfile ? (
              <button onClick={() => setIsEditing(!isEditing)} className="edit-profile-btn">
                {isEditing ? 'Cancel' : 'Edit profile'}
              </button>
            ) : (
              <button
                onClick={handleFollow}
                disabled={followLoading}
                className={`follow-btn ${isFollowing ? 'following' : ''}`}
              >
                {followLoading ? '...' : isFollowing ? 'Following' : 'Follow'}
              </button>
            )}
          </div>

          <div className="profile-stats">
            <span><strong>{posts.length}</strong> posts</span>
            <span><strong>{followCounts?.followersCount || 0}</strong> followers</span>
            <span><strong>{followCounts?.followingCount || 0}</strong> following</span>
          </div>

          <div className="profile-bio">
            <h3>{profileUser.fullName}</h3>
            {profileUser.bio && <p>{profileUser.bio}</p>}
          </div>
        </div>
      </div>

      {isEditing && (
        <form onSubmit={handleUpdateProfile} className="edit-form">
          <input placeholder="Full Name" value={editForm.fullName}
            onChange={(e) => setEditForm((prev) => ({ ...prev, fullName: e.target.value }))} />
          <textarea placeholder="Bio" value={editForm.bio} rows={3}
            onChange={(e) => setEditForm((prev) => ({ ...prev, bio: e.target.value }))} />
          <button type="submit" className="auth-btn">Save</button>
        </form>
      )}

      <div className="profile-posts">
        <div className="posts-tab-bar">
          <div className="posts-title" onClick={() => setViewMode('grid')} style={{ cursor: 'pointer', borderTopColor: viewMode === 'grid' ? '#f5f5f5' : 'transparent' }}>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <rect x="0" y="0" width="7" height="7" /><rect x="8.5" y="0" width="7" height="7" /><rect x="17" y="0" width="7" height="7" />
              <rect x="0" y="8.5" width="7" height="7" /><rect x="8.5" y="8.5" width="7" height="7" /><rect x="17" y="8.5" width="7" height="7" />
              <rect x="0" y="17" width="7" height="7" /><rect x="8.5" y="17" width="7" height="7" /><rect x="17" y="17" width="7" height="7" />
            </svg>
            POSTS
          </div>
        </div>

        {posts.length === 0 ? (
          <p className="no-posts">No posts yet</p>
        ) : viewMode === 'grid' ? (
          <div className="profile-posts-grid">
            {posts.map((post) => (
              <div key={post.id} className="profile-post-thumb" onClick={() => setViewMode('feed')}>
                {post.mediaUrl && post.mediaType === 'IMAGE' ? (
                  <img src={getMediaUrl(post.mediaUrl)} alt={post.caption} />
                ) : (
                  <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, background: '#1a1a1a', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#a8a8a8', fontSize: 12, padding: 8, textAlign: 'center' }}>
                    {post.caption?.substring(0, 60) || (post.mediaType === 'VIDEO' ? 'Video' : 'Post')}
                  </div>
                )}
                <div className="thumb-overlay">
                  <span className="thumb-stat">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="#fff"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" /></svg>
                    {post.likesCount}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div>
            <button onClick={() => setViewMode('grid')} style={{ background: 'none', border: 'none', color: '#0095f6', padding: '12px 0', cursor: 'pointer', fontSize: 14, fontWeight: 600 }}>
              Back to grid
            </button>
            {posts.map((post) => (
              <PostCard key={post.id} post={post} onDelete={() => handleDeletePost(post.id)} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile;
