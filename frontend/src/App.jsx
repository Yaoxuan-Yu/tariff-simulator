import React from 'react';
import {BrowserRouter, Routes, Route} from "react-router-dom";

import Login from './pages/Login';
import SignUp from './pages/SignUp';
import LandingPage from './pages/LandingPage';
import Home from './pages/Home';
import Wrapper from './pages/Wrapper';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/home" element={<Wrapper><Home /></Wrapper>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
