"use client"

import React, { useState } from 'react'
import supabase from "@/lib/supabaseClient"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"

interface LoginFormProps {
  onLogin: (user: any) => void
  onSwitchToSignup: () => void
}

const GOOGLE_LOGO_URL = "https://www.svgrepo.com/show/475656/google-color.svg"

export function LoginForm({ onLogin, onSwitchToSignup }: LoginFormProps) {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [message, setMessage] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const clearMessage = () => {
    setMessage("")
  }

  const clearFormFields = () => {
    setEmail("")
    setPassword("")
  }

  const handleAuthError = (error: any) => {
    setMessage(error.message)
    clearFormFields()
  }

  const handleAuthSuccess = (user: any) => {
    if (user) {
      onLogin(user)
    }
  }

  const getRedirectUrl = (): string => {
    return `${window.location.origin}/`
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    clearMessage()
    setIsLoading(true)

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password,
      })

      if (error) {
        handleAuthError(error)
        return
      }

      handleAuthSuccess(data?.user)
    } catch (err) {
      setMessage("An unexpected error occurred")
    } finally {
      setIsLoading(false)
    }
  }

  const handleGoogleSignIn = async () => {
    clearMessage()
    setIsLoading(true)
    
    try {
      const { error } = await supabase.auth.signInWithOAuth({ 
        provider: 'google', 
        options: {
          redirectTo: getRedirectUrl()
        }
      })
      
      if (error) {
        setMessage(error.message)
      }
    } catch (err) {
      setMessage("An unexpected error occurred")
    } finally {
      setIsLoading(false)
    }
  }

  const getLoadingButtonText = (): string => {
    return isLoading ? "Signing in..." : "Sign In"
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-slate-900">TariffWise</CardTitle>
          <CardDescription>Welcome back!</CardDescription>
        </CardHeader>
        <CardContent>
          {message && (
            <Alert variant="destructive" className="mb-4">
              <AlertDescription>{message}</AlertDescription>
            </Alert>
          )}

          <Button 
            type="button" 
            onClick={handleGoogleSignIn} 
            disabled={isLoading}
            className="w-full mb-4 bg-white hover:bg-gray-50 text-gray-900 border border-gray-300"
          >
            <img 
              src={GOOGLE_LOGO_URL} 
              alt="Google" 
              className="w-5 h-5 mr-2" 
            />
            Sign in with Google
          </Button>

          <div className="relative mb-4">
            <div className="absolute inset-0 flex items-center">
              <span className="w-full border-t" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-white px-2 text-muted-foreground">or</span>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="email" className="text-sm font-medium text-slate-700">
                Email
              </label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                required
                className="w-full"
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="password" className="text-sm font-medium text-slate-700">
                Password
              </label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
                className="w-full"
              />
            </div>

            <Button 
              type="submit" 
              className="w-full bg-accent hover:bg-accent/90 text-accent-foreground" 
              disabled={isLoading}
            >
              {getLoadingButtonText()}
            </Button>
          </form>

          <div className="mt-4 text-center text-sm text-slate-600">
            <span>Don't have an account? </span>
            <button 
              onClick={onSwitchToSignup}
              className="text-accent hover:text-accent/90 font-medium"
            >
              Sign up
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}