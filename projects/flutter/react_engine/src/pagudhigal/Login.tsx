import React, { useState, useEffect } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebase';
import { AuthLayout, AuthHeader, AuthInput, AuthButton } from './auth/AuthComponents';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (localStorage.getItem('elvanniril_theme') === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, []);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      await signInWithEmailAndPassword(auth, email, password);
      // Firebase auth state observer in Seyali.tsx will catch the login and render the app
    } catch (err: any) {
      console.error(err);
      setError('Invalid email or password. Please try again.');
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <AuthHeader
          title="Elvan Niril"
          subtitle="Sign in to your secure database"
        />

        <form onSubmit={handleLogin} style={{ width: '100%', maxWidth: '360px', marginTop: '32px' }}>
          <AuthInput
          label="Email Address"
          placeholder="Enter your email"
          value={email}
          onChange={(e: any) => setEmail(e.target.value)}
          type="email"
          required
        />

        <AuthInput
          label="Password"
          placeholder="Enter your password"
          value={password}
          onChange={(e: any) => setPassword(e.target.value)}
          type="password"
          error={error}
          required
        />

        <div style={{ height: '32px' }} />

        <AuthButton
          type="submit"
          loading={loading}
        >
          Sign In
        </AuthButton>
      </form>
      </div>
    </AuthLayout>
  );
}
