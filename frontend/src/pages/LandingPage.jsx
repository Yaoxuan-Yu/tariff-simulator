import React from 'react';
import { Link } from 'react-router-dom';

function LandingPage() {
  return (
    <div className="landing-container">
      <div className="landing-content">
        <div className="landing-hero">
          <h1 className="landing-title">Tariff Simulator</h1>
          <p className="landing-subtitle">Presented by Group 5</p>
        </div>
        
        <div className="landing-buttons">
          <Link to="/signup" className="landing-button-primary">
            Get Started
          </Link>
          <Link to="/login" className="landing-button-secondary">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;