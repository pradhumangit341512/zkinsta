import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Post, TrendingHashtag } from '../../types';
import { postService } from '../../services/postService';
import { trendingService } from '../../services/trendingService';
import PostCard from '../posts/PostCard';
import './Trending.css';

const Trending: React.FC = () => {
  const [trendingPosts, setTrendingPosts] = useState<Post[]>([]);
  const [trendingHashtags, setTrendingHashtags] = useState<TrendingHashtag[]>([]);
  const [timeFilter, setTimeFilter] = useState<'all' | '24h' | '7d'>('all');
  const [loading, setLoading] = useState(true);

  const loadTrending = useCallback(async () => {
    setLoading(true);
    try {
      const [postsRes, hashtagsRes] = await Promise.all([
        timeFilter === 'all'
          ? postService.getTrendingPosts(0, 20)
          : postService.getRecentTrendingPosts(0, 20, timeFilter),
        trendingService.getTrendingHashtags(10),
      ]);
      setTrendingPosts(postsRes.data.data?.content || []);
      setTrendingHashtags(hashtagsRes.data.data || []);
    } catch {
      // Trending loading failed silently - UI shows empty state
    } finally {
      setLoading(false);
    }
  }, [timeFilter]);

  useEffect(() => {
    loadTrending();
  }, [loadTrending]);

  return (
    <div className="trending-container">
      <div className="trending-sidebar">
        <h3>Trending Hashtags</h3>
        <div className="hashtag-list">
          {trendingHashtags.map((tag) => (
            <Link key={tag.id} to={`/search?q=%23${tag.hashtag}`} className="trending-hashtag">
              <span className="tag-name">#{tag.hashtag}</span>
              <span className="tag-count">{tag.postCount} posts</span>
            </Link>
          ))}
          {trendingHashtags.length === 0 && <p className="no-data">No trending hashtags</p>}
        </div>
      </div>

      <div className="trending-main">
        <div className="trending-header">
          <h2>Trending Posts</h2>
          <div className="time-filters">
            <button className={timeFilter === 'all' ? 'active' : ''} onClick={() => setTimeFilter('all')}>All Time</button>
            <button className={timeFilter === '24h' ? 'active' : ''} onClick={() => setTimeFilter('24h')}>24 Hours</button>
            <button className={timeFilter === '7d' ? 'active' : ''} onClick={() => setTimeFilter('7d')}>7 Days</button>
          </div>
        </div>

        {loading && <div className="loading">Loading trending content...</div>}

        {!loading && trendingPosts.length === 0 && (
          <p className="no-data">No trending posts right now</p>
        )}

        {!loading && trendingPosts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>
    </div>
  );
};

export default Trending;
