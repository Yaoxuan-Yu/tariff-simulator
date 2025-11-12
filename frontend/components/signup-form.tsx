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

const GOOGLE_LOGO_URL = "https://www.svgrepo.com/show/475656/google-color.svg"
const MINIMUM_PASSWORD_LENGTH = 6
const REDIRECT_TO_LOGIN_DELAY = 5000
const ALLOWED_FILE_TYPES = ".pdf,.doc,.docx,.jpg,.jpeg,.png"
const STORAGE_BUCKET_NAME = "admin-proofs"

export function SignupForm({ onSignup, onSwitchToLogin }: SignupFormProps) {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [userRole, setUserRole] = useState("user")
  const [adminProof, setAdminProof] = useState("")
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([])
  const [message, setMessage] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const clearMessage = () => {
    setMessage("")
  }

  const isAdminRole = (): boolean => {
    return userRole === "admin"
  }

  const validatePasswordMatch = (): boolean => {
    if (password !== confirmPassword) {
      setMessage("Passwords do not match")
      return false
    }
    return true
  }

  const validatePasswordLength = (): boolean => {
    if (password.length < MINIMUM_PASSWORD_LENGTH) {
      setMessage(`Password must be at least ${MINIMUM_PASSWORD_LENGTH} characters long`)
      return false
    }
    return true
  }

  const validateAdminRequirements = (): boolean => {
    if (isAdminRole()) {
      if (!adminProof.trim() && uploadedFiles.length === 0) {
        setMessage("Please provide either written justification or upload documents for admin access")
        return false
      }
    }
    return true
  }

  const validateFormInputs = (): boolean => {
    return validatePasswordMatch() && validatePasswordLength() && validateAdminRequirements()
  }

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

  const generateUniqueFileName = (userId: string, file: File): string => {
    const fileExtension = file.name.split('.').pop()
    const timestamp = Date.now()
    const randomString = Math.random().toString(36).substring(7)
    return `${userId}/${timestamp}_${randomString}.${fileExtension}`
  }

  const uploadSingleFile = async (userId: string, file: File): Promise<string | null> => {
    const fileName = generateUniqueFileName(userId, file)
    
    const { data, error } = await supabase.storage
      .from(STORAGE_BUCKET_NAME)
      .upload(fileName, file)

    if (error) {
      console.error('File upload error:', error)
      return null
    }

    if (data) {
      const { data: { publicUrl } } = supabase.storage
        .from(STORAGE_BUCKET_NAME)
        .getPublicUrl(fileName)
      
      return publicUrl
    }

    return null
  }

  const uploadFilesToSupabase = async (userId: string): Promise<string[]> => {
    const uploadedUrls: string[] = []
    
    for (const file of uploadedFiles) {
      const fileUrl = await uploadSingleFile(userId, file)
      if (fileUrl) {
        uploadedUrls.push(fileUrl)
      }
    }

    return uploadedUrls
  }

  const createUserProfile = async (userId: string, fileUrls: string[]) => {
    const { error: dbError } = await supabase
      .from('user_profiles')
      .insert({
        user_id: userId,
        email: email,
        role: userRole,
        admin_proof_text: isAdminRole() ? adminProof : null,
        admin_proof_files: isAdminRole() ? fileUrls : null,
        created_at: new Date().toISOString(),
        approved_at: isAdminRole() ? new Date().toISOString() : null,
      })

    return dbError
  }

  const getSuccessMessage = (): string => {
    if (isAdminRole()) {
      return "Account created successfully! You have been granted admin access. Please check your email to verify your account."
    }
    return "Account created successfully! Please check your email to verify your account."
  }

  const handleSuccessfulSignup = (message: string) => {
    setMessage(message)
    setTimeout(() => {
      onSwitchToLogin()
    }, REDIRECT_TO_LOGIN_DELAY)
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    clearMessage()
    setIsLoading(true)

    if (!validateFormInputs()) {
      setIsLoading(false)
      return
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
        
        if (isAdminRole() && uploadedFiles.length > 0) {
          fileUrls = await uploadFilesToSupabase(data.user.id)
        }

        const dbError = await createUserProfile(data.user.id, fileUrls)

        if (dbError) {
          console.error('Database error:', dbError)
          setMessage("Account created but role assignment failed. Please contact support.")
          setIsLoading(false)
          return
        }

        const successMsg = getSuccessMessage()
        handleSuccessfulSignup(successMsg)
      }
    } catch (err) {
      setMessage("An unexpected error occurred")
    } finally {
      setIsLoading(false)
    }
  }

  const getRedirectUrl = (): string => {
    return `${window.location.origin}/`
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

  const getSubmitButtonText = (): string => {
    return isLoading ? "Creating account..." : "Sign Up"
  }

  const getFileSizeInKB = (file: File): string => {
    return (file.size / 1024).toFixed(1)
  }

  const isSuccessMessage = (): boolean => {
    return message.includes("successfully")
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
            <Alert variant={isSuccessMessage() ? "default" : "destructive"} className="mb-4">
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

            {isAdminRole() && (
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
                      accept={ALLOWED_FILE_TYPES}
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
                              ({getFileSizeInKB(file)} KB)
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
              {getSubmitButtonText()}
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