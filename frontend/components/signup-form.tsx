"use client"

import React, { useState } from 'react'
import supabase from "@/lib/supabaseClient"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Upload, X, FileText } from "lucide-react"

interface SignupFormProps {
  onSignup: (user: any) => void
  onSwitchToLogin: () => void
}

export function SignupForm({ onSignup, onSwitchToLogin }: SignupFormProps) {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [userRole, setUserRole] = useState("user")
  const [adminProof, setAdminProof] = useState("")
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([])
  const [message, setMessage] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files
    if (files) {
      const newFiles = Array.from(files)
      setUploadedFiles(prev => [...prev, ...newFiles])
    }   
  }

  const removeFile = (index: number) => {
    setUploadedFiles(prev => prev.filter((_, i) => i !== index))
  }

  const uploadFilesToSupabase = async (userId: string) => {
    const uploadedUrls: string[] = []
    
    for (const file of uploadedFiles) {
      const fileExt = file.name.split('.').pop()
      const fileName = `${userId}/${Date.now()}_${Math.random().toString(36).substring(7)}.${fileExt}`
      
      const { data, error } = await supabase.storage
        .from('admin-proofs')
        .upload(fileName, file)

      if (error) {
        console.error('File upload error:', error)
        continue
      }

      if (data) {
        const { data: { publicUrl } } = supabase.storage
          .from('admin-proofs')
          .getPublicUrl(fileName)
        
        uploadedUrls.push(publicUrl)
      }
    }

    return uploadedUrls
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setMessage("")
    setIsLoading(true)

    // Validate passwords match
    if (password !== confirmPassword) {
      setMessage("Passwords do not match")
      setIsLoading(false)
      return
    }

    // Validate password length
    if (password.length < 6) {
      setMessage("Password must be at least 6 characters long")
      setIsLoading(false)
      return
    }

    // Validate admin requirements
    if (userRole === "admin") {
      if (!adminProof.trim() && uploadedFiles.length === 0) {
        setMessage("Please provide either written justification or upload documents for admin access")
        setIsLoading(false)
        return
      }
    }

    try {
      const { data, error } = await supabase.auth.signUp({
        email: email,
        password: password,
      })

      if (error) {
        setMessage(error.message)
        setIsLoading(false)
        return
      }

      if (data?.user) {
        let fileUrls: string[] = []
        
        // Upload files if admin role
        if (userRole === "admin" && uploadedFiles.length > 0) {
          fileUrls = await uploadFilesToSupabase(data.user.id)
        }

        // Store user role and admin request in database
        const { error: dbError } = await supabase
          .from('user_profiles')
          .insert({
            user_id: data.user.id,
            email: email,
            role: userRole, // Auto-approve: 'admin' or 'user'
            admin_proof_text: userRole === "admin" ? adminProof : null,
            admin_proof_files: userRole === "admin" ? fileUrls : null,
            created_at: new Date().toISOString(),
            approved_at: userRole === "admin" ? new Date().toISOString() : null,
          })

        if (dbError) {
          console.error('Database error:', dbError)
          setMessage("Account created but role assignment failed. Please contact support.")
          setIsLoading(false)
          return
        }

        if (userRole === "admin") {
          setMessage("Account created successfully! You have been granted admin access. Please check your email to verify your account.")
        } else {
          setMessage("Account created successfully! Please check your email to verify your account.")
        }
        
        setTimeout(() => {
          onSwitchToLogin()
        }, 5000)
      }
    } catch (err) {
      setMessage("An unexpected error occurred")
    } finally {
      setIsLoading(false)
    }
  }

  const handleGoogleSignIn = async () => {
    setMessage("")
    setIsLoading(true)
    
    try {
      const { error } = await supabase.auth.signInWithOAuth({ 
        provider: 'google', 
        options: {
          redirectTo: `${window.location.origin}/`
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

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold text-slate-900">TariffWise</CardTitle>
          <CardDescription>Create your account</CardDescription>
        </CardHeader>
        <CardContent>
          {message && (
            <Alert variant={message.includes("successfully") ? "default" : "destructive"} className="mb-4">
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
              src="https://www.svgrepo.com/show/475656/google-color.svg" 
              alt="Google" 
              className="w-5 h-5 mr-2" 
            />
            Sign up with Google
          </Button>

          <div className="relative mb-4">
            <div className="absolute inset-0 flex items-center">
              <span className="w-full border-t" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-white px-2 text-muted-foreground">or</span>
            </div>
          </div>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="role">Account Type *</Label>
              <Select value={userRole} onValueChange={setUserRole}>
                <SelectTrigger>
                  <SelectValue placeholder="Select account type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="user">User</SelectItem>
                  <SelectItem value="admin">Admin</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email *</Label>
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
              <Label htmlFor="password">Password *</Label>
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

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm Password *</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm your password"
                required
                className="w-full"
              />
            </div>

            {userRole === "admin" && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="adminProof">Admin Access Justification</Label>
                  <Textarea
                    id="adminProof"
                    value={adminProof}
                    onChange={(e) => setAdminProof(e.target.value)}
                    placeholder="Please provide a detailed justification for why you need admin access (e.g., company role, responsibilities, etc.)"
                    className="w-full min-h-[100px]"
                  />
                  <p className="text-xs text-muted-foreground">Provide either written justification or upload documents below</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="documents">Supporting Documents (Optional)</Label>
                  <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-gray-400 transition-colors">
                    <input
                      id="documents"
                      type="file"
                      multiple
                      onChange={handleFileUpload}
                      className="hidden"
                      accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                    />
                    <label htmlFor="documents" className="cursor-pointer">
                      <Upload className="mx-auto h-8 w-8 text-gray-400 mb-2" />
                      <p className="text-sm text-gray-600">
                        Click to upload documents
                      </p>
                      <p className="text-xs text-gray-500 mt-1">
                        PDF, DOC, DOCX, JPG, PNG (Max 10MB each)
                      </p>
                    </label>
                  </div>

                  {uploadedFiles.length > 0 && (
                    <div className="mt-3 space-y-2">
                      {uploadedFiles.map((file, index) => (
                        <div
                          key={index}
                          className="flex items-center justify-between bg-gray-50 rounded-lg p-2"
                        >
                          <div className="flex items-center space-x-2">
                            <FileText className="h-4 w-4 text-gray-500" />
                            <span className="text-sm text-gray-700 truncate max-w-[200px]">
                              {file.name}
                            </span>
                            <span className="text-xs text-gray-500">
                              ({(file.size / 1024).toFixed(1)} KB)
                            </span>
                          </div>
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={() => removeFile(index)}
                            className="text-red-500 hover:text-red-700 hover:bg-red-50"
                          >
                            <X className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <Alert>
                  <AlertDescription className="text-sm">
                    Admin accounts are automatically approved. You'll have full admin access after email verification.
                  </AlertDescription>
                </Alert>
              </>
            )}

            <Button 
              onClick={handleSubmit}
              className="w-full bg-accent hover:bg-accent/90 text-accent-foreground" 
              disabled={isLoading}
            >
              {isLoading ? "Creating account..." : "Sign Up"}
            </Button>
          </div>

          <div className="mt-4 text-center text-sm text-slate-600">
            <span>Already have an account? </span>
            <button 
              onClick={onSwitchToLogin}
              className="text-accent hover:text-accent/90 font-medium"
            >
              Sign in
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
