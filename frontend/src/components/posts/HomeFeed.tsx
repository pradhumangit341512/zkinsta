import React, { useState, useEffect, useCallback } from 'react';
import { Post } from '../../types';
import { postService } from '../../services/postService';
import PostCard from './PostCard';
import './Posts.css';

const HomeFeed: React.FC = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);

  const loadPosts = useCallback(async (pageNum: number) => {
    try {
      setLoading(true);
      const res = await postService.getPublicFeed(pageNum, 20);
      const data = res.data.data;
      if (data && data.content) {
        setPosts((prev) => pageNum === 0 ? data.content : [...prev, ...data.content]);
        setHasMore(data.number < data.totalPages - 1);
      }
    } catch {
      // Feed loading failed silently - UI shows empty state
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPosts(0);
  }, [loadPosts]);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    loadPosts(nextPage);
  };

  const handleDeletePost = (postId: number) => {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
  };

  return (
    <div className="feed-container">
      <div className="feed-content">
        {posts.length === 0 && !loading && (
          <div className="empty-feed">
            <h3>No posts yet</h3>
            <p>Follow people or create your first post!</p>
          </div>
        )}

        {posts.map((post) => (
          <PostCard key={post.id} post={post} onDelete={() => handleDeletePost(post.id)} />
        ))}

        {loading && <div className="loading">Loading posts...</div>}

        {hasMore && !loading && (
          <button onClick={handleLoadMore} className="load-more-btn">Load More</button>
        )}
      </div>
    </div>
  );
};

export default HomeFeed;
