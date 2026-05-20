import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { postService } from '../../services/postService';
import MediaEditor from '../media/MediaEditor';
import './Posts.css';

const CreatePost: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [caption, setCaption] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [editedMediaUrl, setEditedMediaUrl] = useState('');
  const [filterName, setFilterName] = useState('normal');
  const [mediaType, setMediaType] = useState<'IMAGE' | 'VIDEO' | 'TEXT'>('IMAGE');
  const [privacy, setPrivacy] = useState<'PUBLIC' | 'FRIENDS_ONLY' | 'PRIVATE'>('PUBLIC');
  const [hashtagInput, setHashtagInput] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [uploadProgress, setUploadProgress] = useState('');
  const [showEditor, setShowEditor] = useState(false);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      setError('File size must be under 10MB');
      return;
    }

    const isImage = file.type.startsWith('image/');
    const isVideo = file.type.startsWith('video/');

    if (!isImage && !isVideo) {
      setError('Please select an image or video file');
      return;
    }

    setError('');
    setSelectedFile(file);
    setMediaType(isVideo ? 'VIDEO' : 'IMAGE');
    setEditedMediaUrl('');
    setFilterName('normal');

    const reader = new FileReader();
    reader.onload = () => setPreviewUrl(reader.result as string);
    reader.readAsDataURL(file);
  };

  const handleRemoveMedia = () => {
    setSelectedFile(null);
    setPreviewUrl('');
    setEditedMediaUrl('');
    setFilterName('normal');
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleEditorSave = (editedUrl: string, filter: string) => {
    setEditedMediaUrl(editedUrl);
    setFilterName(filter);
    setShowEditor(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (mediaType !== 'TEXT' && !selectedFile) {
      setError('Please select a photo or video');
      return;
    }

    const hashtags = hashtagInput
      .split(/[,\s#]+/)
      .filter((t) => t.trim().length > 0)
      .map((t) => t.trim().toLowerCase());

    setSubmitting(true);
    try {
      let mediaUrl = '';

      if (selectedFile && mediaType !== 'TEXT') {
        setUploadProgress('Uploading media...');
        const uploadRes = await postService.uploadMedia(selectedFile);
        mediaUrl = uploadRes.data.data.mediaUrl;
        setUploadProgress('');
      }

      await postService.createPost({
        caption,
        mediaUrl,
        mediaType,
        privacy,
        hashtags,
        filter: filterName !== 'normal' ? filterName : undefined,
      });
      setSuccess('Post published successfully!');
      setTimeout(() => navigate('/'), 1500);
    } catch (err: unknown) {
      setUploadProgress('');
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setError(axiosErr.response?.data?.message || 'Failed to create post');
    } finally {
      setSubmitting(false);
    }
  };

  const displayUrl = editedMediaUrl || previewUrl;

  return (
    <div className="create-post-container">
      <div className="create-post-card">
        <h2>Create New Post</h2>

        {error && <div className="auth-error">{error}</div>}
        {success && <div className="auth-success">{success}</div>}

        <form onSubmit={handleSubmit} className="create-post-form">
          <div className="form-group">
            <label>Media Type</label>
            <select value={mediaType} onChange={(e) => {
              setMediaType(e.target.value as 'IMAGE' | 'VIDEO' | 'TEXT');
              handleRemoveMedia();
            }}>
              <option value="IMAGE">Image</option>
              <option value="VIDEO">Video</option>
              <option value="TEXT">Text Only</option>
            </select>
          </div>

          {mediaType !== 'TEXT' && (
            <div className="form-group">
              <label>{mediaType === 'IMAGE' ? 'Photo' : 'Video'}</label>
              {!selectedFile ? (
                <div
                  className="file-drop-zone"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <div className="file-drop-icon">
                    {mediaType === 'IMAGE' ? (
                      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8e8e8e" strokeWidth="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2" />
                        <circle cx="8.5" cy="8.5" r="1.5" />
                        <path d="M21 15l-5-5L5 21" />
                      </svg>
                    ) : (
                      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#8e8e8e" strokeWidth="1.5">
                        <polygon points="5 3 19 12 5 21 5 3" />
                      </svg>
                    )}
                  </div>
                  <p className="file-drop-text">
                    Tap to select {mediaType === 'IMAGE' ? 'a photo' : 'a video'}
                  </p>
                  <p className="file-drop-hint">JPEG, PNG, GIF, WebP, MP4 (max 10MB)</p>
                </div>
              ) : (
                <div className="media-preview">
                  {mediaType === 'IMAGE' && (
                    <>
                      <img src={displayUrl} alt="Preview" />
                      {editedMediaUrl && (
                        <span className="filter-badge">Filter: {filterName}</span>
                      )}
                    </>
                  )}
                  {mediaType === 'VIDEO' && (
                    <video src={displayUrl} controls style={{ maxWidth: '100%', maxHeight: '300px' }} />
                  )}
                  <div className="media-preview-actions">
                    {mediaType === 'IMAGE' && previewUrl && (
                      <button type="button" className="edit-media-btn" onClick={() => setShowEditor(true)}>
                        Edit Photo
                      </button>
                    )}
                    <button type="button" className="remove-media-btn" onClick={handleRemoveMedia}>
                      Remove
                    </button>
                  </div>
                </div>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept={mediaType === 'IMAGE' ? 'image/*' : 'video/*'}
                onChange={handleFileSelect}
                style={{ display: 'none' }}
              />
            </div>
          )}

          <div className="form-group">
            <label>Caption</label>
            <textarea
              placeholder="Write a caption..."
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              rows={3}
            />
          </div>

          <div className="form-group">
            <label>Hashtags (comma or space separated)</label>
            <input
              type="text"
              placeholder="travel, food, nature"
              value={hashtagInput}
              onChange={(e) => setHashtagInput(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Privacy</label>
            <select value={privacy} onChange={(e) => setPrivacy(e.target.value as 'PUBLIC' | 'FRIENDS_ONLY' | 'PRIVATE')}>
              <option value="PUBLIC">Public</option>
              <option value="FRIENDS_ONLY">Friends Only</option>
              <option value="PRIVATE">Private</option>
            </select>
          </div>

          {uploadProgress && <div className="upload-progress">{uploadProgress}</div>}

          <button type="submit" className="auth-btn" disabled={submitting}>
            {submitting ? 'Publishing...' : 'Share Post'}
          </button>
        </form>
      </div>

      {showEditor && previewUrl && (
        <MediaEditor
          imageUrl={previewUrl}
          onSave={handleEditorSave}
          onCancel={() => setShowEditor(false)}
        />
      )}
    </div>
  );
};

export default CreatePost;
