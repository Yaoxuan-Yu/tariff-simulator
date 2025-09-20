import React, { useState } from 'react';
import supabase from "../helper/supabaseClient";
import { Link, useNavigate } from "react-router-dom";
import '../App.css';

function SignUp() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setIsError(false);

    const { data, error } = await supabase.auth.signUp({
      email: email,
      password: password,
    });

    if (error) {
      setMessage(error.message);
      setIsError(true);
      return;
    }

    if (data) {
      setMessage("Please click the link in your email to verify your email address!");
      setIsError(false);
    }

    setEmail("");
    setPassword("");
  };

  // Google sign-up handler
  const handleGoogleSignUp = async () => {
    setMessage("");
    setIsError(false);
    const { error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: 'http://localhost:5173/home'
      }
    });
    if (error) {
      setMessage(error.message);
      setIsError(true);
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-card">
        <div className="signup-header">
          <h2 className="signup-title">Create Account</h2>
          <p className="signup-subtitle">Enter your details to get started</p>
        </div>

        {message && (
          <div className={`message ${isError ? 'error' : 'success'}`}>
            {message}
          </div>
        )}

        <button type="button" className="google-signin-button" onClick={handleGoogleSignUp}>
          <img src="https://www.svgrepo.com/show/475656/google-color.svg" alt="Google" />
          Sign up with Google
        </button>

        <div className="divider">
          <span>or</span>
        </div>

        <form onSubmit={handleSubmit} className="signup-form">
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email</label>
            <input
              id="email"
              className="form-input"
              onChange={(e) => setEmail(e.target.value)}
              value={email}
              type="email"
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input
              id="password"
              className="form-input"
              onChange={(e) => setPassword(e.target.value)}
              value={password}
              type="password"
              placeholder="Enter your password"
              required
            />
          </div>

          <button type="submit" className="submit-button">
            Create Account
          </button>
        </form>

        <div className="login-link-container">
          <span className="login-text">Already have an account?</span>
          <Link to="/login" className="login-link">Sign in</Link>
        </div>
      </div>
    </div>
  );
}

export default SignUp;