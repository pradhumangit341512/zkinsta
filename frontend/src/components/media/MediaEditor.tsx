import React, { useState, useCallback } from 'react';
import Cropper, { Area } from 'react-easy-crop';
import { getCroppedImg, CroppedArea } from './cropUtils';
import './MediaEditor.css';

interface FilterPreset {
  name: string;
  label: string;
  brightness: number;
  contrast: number;
  saturate: number;
  sepia: number;
  grayscale: number;
  hueRotate: number;
}

const FILTER_PRESETS: FilterPreset[] = [
  { name: 'normal', label: 'Normal', brightness: 100, contrast: 100, saturate: 100, sepia: 0, grayscale: 0, hueRotate: 0 },
  { name: 'clarendon', label: 'Clarendon', brightness: 110, contrast: 120, saturate: 130, sepia: 0, grayscale: 0, hueRotate: 0 },
  { name: 'gingham', label: 'Gingham', brightness: 105, contrast: 90, saturate: 80, sepia: 10, grayscale: 0, hueRotate: 0 },
  { name: 'moon', label: 'Moon', brightness: 110, contrast: 110, saturate: 0, sepia: 0, grayscale: 100, hueRotate: 0 },
  { name: 'lark', label: 'Lark', brightness: 115, contrast: 90, saturate: 110, sepia: 5, grayscale: 0, hueRotate: 0 },
  { name: 'reyes', label: 'Reyes', brightness: 110, contrast: 85, saturate: 75, sepia: 22, grayscale: 0, hueRotate: 0 },
  { name: 'juno', label: 'Juno', brightness: 100, contrast: 115, saturate: 140, sepia: 0, grayscale: 0, hueRotate: 0 },
  { name: 'slumber', label: 'Slumber', brightness: 95, contrast: 90, saturate: 85, sepia: 15, grayscale: 0, hueRotate: 0 },
  { name: 'aden', label: 'Aden', brightness: 115, contrast: 90, saturate: 85, sepia: 20, grayscale: 0, hueRotate: 10 },
  { name: 'perpetua', label: 'Perpetua', brightness: 105, contrast: 100, saturate: 110, sepia: 0, grayscale: 0, hueRotate: 180 },
  { name: 'warm', label: 'Warm', brightness: 105, contrast: 100, saturate: 120, sepia: 30, grayscale: 0, hueRotate: 0 },
  { name: 'cool', label: 'Cool', brightness: 100, contrast: 105, saturate: 90, sepia: 0, grayscale: 0, hueRotate: 200 },
];

interface MediaEditorProps {
  imageUrl: string;
  onSave: (editedDataUrl: string, filterName: string) => void;
  onCancel: () => void;
}

const MediaEditor: React.FC<MediaEditorProps> = ({ imageUrl, onSave, onCancel }) => {
  const [activeTab, setActiveTab] = useState<'crop' | 'adjust' | 'filters'>('filters');
  const [saveError, setSaveError] = useState('');
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<CroppedArea | null>(null);

  // Adjustments
  const [brightness, setBrightness] = useState(100);
  const [contrast, setContrast] = useState(100);
  const [saturate, setSaturate] = useState(100);

  // Active filter
  const [activeFilter, setActiveFilter] = useState<FilterPreset>(FILTER_PRESETS[0]);

  const onCropComplete = useCallback((_: Area, croppedPixels: CroppedArea) => {
    setCroppedAreaPixels(croppedPixels);
  }, []);

  const getPreviewStyle = (): React.CSSProperties => {
    const f = activeFilter;
    return {
      filter: `brightness(${(brightness / 100) * (f.brightness / 100) * 100}%) contrast(${(contrast / 100) * (f.contrast / 100) * 100}%) saturate(${(saturate / 100) * (f.saturate / 100) * 100}%) sepia(${f.sepia}%) grayscale(${f.grayscale}%) hue-rotate(${f.hueRotate}deg)`,
    };
  };

  const handleSave = async () => {
    setSaveError('');
    try {
      const f = activeFilter;
      const finalBrightness = Math.round((brightness / 100) * (f.brightness / 100) * 100);
      const finalContrast = Math.round((contrast / 100) * (f.contrast / 100) * 100);
      const finalSaturate = Math.round((saturate / 100) * (f.saturate / 100) * 100);

      if (croppedAreaPixels) {
        const croppedImage = await getCroppedImg(
          imageUrl, croppedAreaPixels,
          finalBrightness, finalContrast, finalSaturate,
          f.sepia, f.grayscale, f.hueRotate
        );
        onSave(croppedImage, activeFilter.name);
      } else {
        // No crop, just apply filters via CSS (store filter name)
        onSave(imageUrl, activeFilter.name);
      }
    } catch (err) {
      setSaveError('Failed to apply edits. Check image URL or try again.');
      console.error('Failed to process image', err);
    }
  };

  const handleReset = () => {
    setBrightness(100);
    setContrast(100);
    setSaturate(100);
    setActiveFilter(FILTER_PRESETS[0]);
    setZoom(1);
    setCrop({ x: 0, y: 0 });
  };

  return (
    <div className="media-editor">
      <div className="editor-header">
        <button className="editor-cancel" onClick={onCancel}>Cancel</button>
        <h3>Edit Photo</h3>
        <button className="editor-save" onClick={handleSave}>Apply</button>
      </div>

      {saveError && <div className="auth-error" style={{ margin: '8px 16px' }}>{saveError}</div>}

      <div className="editor-canvas">
        {activeTab === 'crop' ? (
          <div className="crop-container">
            <Cropper
              image={imageUrl}
              crop={crop}
              zoom={zoom}
              aspect={1}
              onCropChange={setCrop}
              onZoomChange={setZoom}
              onCropComplete={onCropComplete}
              style={{
                mediaStyle: getPreviewStyle(),
              }}
            />
            <div className="zoom-control">
              <label>Zoom</label>
              <input
                type="range" min={1} max={3} step={0.1}
                value={zoom}
                onChange={(e) => setZoom(Number(e.target.value))}
              />
            </div>
          </div>
        ) : (
          <div className="preview-container">
            <img src={imageUrl} alt="Preview" style={getPreviewStyle()} />
          </div>
        )}
      </div>

      <div className="editor-tabs">
        <button className={activeTab === 'filters' ? 'active' : ''} onClick={() => setActiveTab('filters')}>
          Filters
        </button>
        <button className={activeTab === 'adjust' ? 'active' : ''} onClick={() => setActiveTab('adjust')}>
          Adjust
        </button>
        <button className={activeTab === 'crop' ? 'active' : ''} onClick={() => setActiveTab('crop')}>
          Crop
        </button>
        <button className="reset-btn" onClick={handleReset}>Reset</button>
      </div>

      <div className="editor-controls">
        {activeTab === 'filters' && (
          <div className="filter-grid">
            {FILTER_PRESETS.map((filter) => (
              <button
                key={filter.name}
                className={`filter-item ${activeFilter.name === filter.name ? 'active' : ''}`}
                onClick={() => setActiveFilter(filter)}
              >
                <div className="filter-thumbnail">
                  <img
                    src={imageUrl}
                    alt={filter.label}
                    style={{
                      filter: `brightness(${filter.brightness}%) contrast(${filter.contrast}%) saturate(${filter.saturate}%) sepia(${filter.sepia}%) grayscale(${filter.grayscale}%) hue-rotate(${filter.hueRotate}deg)`,
                    }}
                  />
                </div>
                <span>{filter.label}</span>
              </button>
            ))}
          </div>
        )}

        {activeTab === 'adjust' && (
          <div className="adjust-controls">
            <div className="adjust-slider">
              <label>Brightness: {brightness}%</label>
              <input type="range" min={50} max={150} value={brightness}
                onChange={(e) => setBrightness(Number(e.target.value))} />
            </div>
            <div className="adjust-slider">
              <label>Contrast: {contrast}%</label>
              <input type="range" min={50} max={150} value={contrast}
                onChange={(e) => setContrast(Number(e.target.value))} />
            </div>
            <div className="adjust-slider">
              <label>Saturation: {saturate}%</label>
              <input type="range" min={0} max={200} value={saturate}
                onChange={(e) => setSaturate(Number(e.target.value))} />
            </div>
          </div>
        )}

        {activeTab === 'crop' && (
          <div className="crop-info">
            <p>Drag to reposition. Use the slider or pinch to zoom.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default MediaEditor;
