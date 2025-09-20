import React, { useState } from 'react';
import supabase from "../helper/supabaseClient";
import { Link, useNavigate } from "react-router-dom";

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");

    const { data, error } = await supabase.auth.signInWithPassword({
      email: email,
      password: password,
    });

    if (error) {
      setMessage(error.message);
      setEmail("");
      setPassword("");
      return;
    }

    if (data) {
      navigate("/home");
      return null;
    }
  };

  const handleGoogleSignIn = async () => {
    setMessage("");
    const { error } = await supabase.auth.signInWithOAuth({ 
      provider: 'google', 
      options: {
        redirectTo: 'http://localhost:5173/home'
      }
    });
    redirectTo: 'http://localhost:5173/home' // hardcoded for now 
    if (error) {
      setMessage(error.message);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h2 className="login-title">Login</h2>
            <p className="signup-subtitle">Welcome back!</p>
        </div>

        {message && (
          <div className="message error">
            {message}
          </div>
        )}

        <button type="button" className="google-signin-button" onClick={handleGoogleSignIn} style={{marginBottom: '1rem', width: '100%'}}>
          <img src="https://www.svgrepo.com/show/475656/google-color.svg" alt="Google" style={{width: '1.2em', verticalAlign: 'middle', marginRight: '0.5em'}} />
          Sign in with Google
        </button>

        <div className="divider">
          <span>or</span>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
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
            Sign In
          </button>
        </form>

        <div className="signup-link-container">
          <span className="signup-text">Don't have an account?</span>
          <Link to="/signup" className="signup-link">Sign up</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;