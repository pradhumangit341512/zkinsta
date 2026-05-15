import React, { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Post, Comment } from '../../types';
import { postService } from '../../services/postService';
import { getMediaUrl } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import './Posts.css';

interface PostCardProps {
  post: Post;
  onDelete?: () => void;
  onUpdate?: (post: Post) => void;
}

const PostCard: React.FC<PostCardProps> = ({ post, onDelete }) => {
  const { user } = useAuth();
  const [liked, setLiked] = useState(post.likedByCurrentUser);
  const [likesCount, setLikesCount] = useState(post.likesCount);
  const [viewsCount, setViewsCount] = useState(post.viewsCount);
  const [commentsCount, setCommentsCount] = useState(post.commentsCount || 0);
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);
  const [likeAnimating, setLikeAnimating] = useState(false);
  const videoRef = useRef<HTMLVideoElement>(null);
  const cardRef = useRef<HTMLDivElement>(null);
  const viewRecorded = useRef(false);

  // Comments state
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentText, setCommentText] = useState('');
  const [loadingComments, setLoadingComments] = useState(false);
  const [submittingComment, setSubmittingComment] = useState(false);

  // Record view when post scrolls into viewport
  useEffect(() => {
    if (!user || viewRecorded.current) return;
    const el = cardRef.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !viewRecorded.current) {
          viewRecorded.current = true;
          postService.recordView(post.id)
            .then((res) => {
              if (res.data.data) setViewsCount(res.data.data.viewsCount);
            })
            .catch(() => {});
        }
      },
      { threshold: 0.5 }
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [post.id, user]);

  const handleLike = async () => {
    const prevLiked = liked;
    const prevCount = likesCount;
    try {
      if (liked) {
        setLiked(false);
        setLikesCount((prev) => prev - 1);
        await postService.unlikePost(post.id);
      } else {
        setLiked(true);
        setLikesCount((prev) => prev + 1);
        setLikeAnimating(true);
        setTimeout(() => setLikeAnimating(false), 600);
        await postService.likePost(post.id);
      }
    } catch {
      setLiked(prevLiked);
      setLikesCount(prevCount);
    }
  };

  const lastTap = useRef(0);
  const handleDoubleTap = () => {
    const now = Date.now();
    if (now - lastTap.current < 300 && !liked) {
      handleLike();
    }
    lastTap.current = now;
  };

  const handleDelete = async () => {
    try {
      await postService.deletePost(post.id);
      onDelete?.();
    } catch {}
  };

  const loadComments = async () => {
    setLoadingComments(true);
    try {
      const res = await postService.getComments(post.id, 0, 50);
      if (res.data.data?.content) {
        setComments(res.data.data.content);
      }
    } catch {}
    setLoadingComments(false);
  };

  const handleToggleComments = () => {
    if (!showComments) {
      loadComments();
    }
    setShowComments(!showComments);
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim() || submittingComment) return;
    setSubmittingComment(true);
    try {
      const res = await postService.addComment(post.id, commentText.trim());
      if (res.data.data) {
        setComments((prev) => [res.data.data, ...prev]);
        setCommentsCount((prev) => prev + 1);
      }
      setCommentText('');
    } catch {}
    setSubmittingComment(false);
  };

  const handleDeleteComment = async (commentId: number) => {
    try {
      await postService.deleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
      setCommentsCount((prev) => Math.max(0, prev - 1));
    } catch {}
  };

  const timeAgo = (date: string) => {
    const diff = Date.now() - new Date(date).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d`;
    const weeks = Math.floor(days / 7);
    return `${weeks}w`;
  };

  const formatCount = (count: number): string => {
    if (count >= 1000000) return `${(count / 1000000).toFixed(1)}M`;
    if (count >= 1000) return `${(count / 1000).toFixed(1)}K`;
    return count.toString();
  };

  return (
    <div className="post-card" ref={cardRef}>
      <div className="post-header">
        <div className="post-avatar">
          <div className="post-avatar-placeholder">{post.username?.[0]?.toUpperCase() || '?'}</div>
        </div>
        <Link to={`/profile/${post.username}`} className="post-username">
          {post.username}
        </Link>
        <span className="post-time">{timeAgo(post.createdAt)}</span>
        {user?.id === post.userId && (
          <div className="post-actions-menu">
            <button onClick={() => setShowConfirmDelete(true)} className="delete-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="1" /><circle cx="19" cy="12" r="1" /><circle cx="5" cy="12" r="1" />
              </svg>
            </button>
          </div>
        )}
      </div>

      <div className="post-media-wrapper" onClick={handleDoubleTap}>
        {post.mediaType === 'IMAGE' && post.mediaUrl && (
          <div className="post-media">
            <img src={getMediaUrl(post.mediaUrl)} alt={post.caption} />
          </div>
        )}
        {post.mediaType === 'VIDEO' && post.mediaUrl && (
          <div className="post-media">
            <video ref={videoRef} controls src={getMediaUrl(post.mediaUrl)} />
          </div>
        )}
      </div>

      <div className="post-actions">
        <button
          onClick={handleLike}
          className={`action-btn ${liked ? 'liked' : ''} ${likeAnimating ? 'like-animate' : ''}`}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill={liked ? '#ed4956' : 'none'} stroke={liked ? '#ed4956' : 'currentColor'} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
        </button>
        <button onClick={handleToggleComments} className="action-btn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        </button>
      </div>

      <div className="post-content">
        <div className="post-stats">
          <span className="likes-count">{formatCount(likesCount)} likes</span>
          <span className="post-stats-dot">&middot;</span>
          <span className="views-count">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
            {formatCount(viewsCount)}
          </span>
        </div>

        {post.caption && (
          <p className="post-caption">
            <Link to={`/profile/${post.username}`} className="caption-username">{post.username}</Link>
            {post.caption}
          </p>
        )}

        {post.hashtags && post.hashtags.length > 0 && (
          <div className="post-hashtags">
            {post.hashtags.map((tag) => (
              <Link key={tag} to={`/search?q=%23${tag}`} className="hashtag">#{tag}</Link>
            ))}
          </div>
        )}

        {commentsCount > 0 && !showComments && (
          <button className="view-comments-btn" onClick={handleToggleComments}>
            View all {commentsCount} comment{commentsCount !== 1 ? 's' : ''}
          </button>
        )}
      </div>

      {/* Comments Section */}
      {showComments && (
        <div className="comments-section">
          {user && (
            <form onSubmit={handleAddComment} className="comment-form">
              <input
                type="text"
                placeholder="Add a comment..."
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                maxLength={2000}
              />
              <button
                type="submit"
                disabled={!commentText.trim() || submittingComment}
                className="comment-post-btn"
              >
                Post
              </button>
            </form>
          )}

          {loadingComments && <div className="comments-loading">Loading comments...</div>}

          <div className="comments-list">
            {comments.map((comment) => (
              <div key={comment.id} className="comment-item">
                <div className="comment-body">
                  <Link to={`/profile/${comment.username}`} className="comment-username">
                    {comment.username}
                  </Link>
                  <span className="comment-text">{comment.text}</span>
                </div>
                <div className="comment-meta">
                  <span className="comment-time">{timeAgo(comment.createdAt)}</span>
                  {user?.id === comment.userId && (
                    <button
                      onClick={() => handleDeleteComment(comment.id)}
                      className="comment-delete-btn"
                    >
                      Delete
                    </button>
                  )}
                </div>
              </div>
            ))}
            {!loadingComments && comments.length === 0 && (
              <div className="no-comments">No comments yet</div>
            )}
          </div>
        </div>
      )}

      {showConfirmDelete && (
        <div className="confirm-overlay">
          <div className="confirm-dialog">
            <p>Delete post?</p>
            <div className="confirm-actions">
              <button onClick={handleDelete} className="confirm-delete">Delete</button>
              <button onClick={() => setShowConfirmDelete(false)} className="confirm-cancel">Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PostCard;
