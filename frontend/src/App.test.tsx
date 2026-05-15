import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders Instagram brand', () => {
  render(<App />);
  const brandElement = screen.getByText(/Instagram/i);
  expect(brandElement).toBeInTheDocument();
});
