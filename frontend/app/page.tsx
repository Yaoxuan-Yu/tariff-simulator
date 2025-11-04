"use client"

import { useEffect, useState } from "react"
import supabase from "@/lib/supabaseClient"
import { LoginForm } from "@/components/login-form"
import { SignupForm } from "@/components/signup-form"
import { TariffCalculatorForm } from "@/components/tariff-calculator-form"
import { ResultsTable } from "@/components/results-table"
import { TariffDefinitionsTable } from "@/components/tariff-definitions-table"
import { ExportPage } from "@/components/export-page"
import { SessionHistoryPage } from "@/components/session-history"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { LogOut, User, History, Shield } from "lucide-react"

export default function Home() {
  const [user, setUser] = useState<any>(null)
  const [authView, setAuthView] = useState<"login" | "signup">("login") // ADD THIS LINE
  const [calculationResults, setCalculationResults] = useState<any>(null)
  const [currentView, setCurrentView] = useState<"dashboard" | "global-tariffs" | "simulator-tariffs" | "cart" | "history">("dashboard")
  const [calculatorMode, setCalculatorMode] = useState<"global" | "simulator">("global")
  const [sessionHistory, setSessionHistory] = useState<any[]>([])
  const [exportCart, setExportCart] = useState<any[]>([])
  const [cartCount, setCartCount] = useState<number>(0)

  const handleLogin = (userData: any) => {
    setUser(userData)
  }

  const handleSignup = (userData: any) => {
    setUser(userData)
  }

  const handleLogout = async () => {
    await supabase.auth.signOut()
    setUser(null)
    setCalculationResults(null)
    setSessionHistory([])
    setExportCart([])
  }

  // Fetch cart count from backend
  const fetchCartCount = async () => {
    try {
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token
      
      const response = await fetch('http://localhost:8080/api/export-cart', {
        method: 'GET',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      })
      
      if (response.status === 204 || !response.ok) {
        // Empty cart or error
        setCartCount(0)
      } else if (response.ok) {
        // Check if response has content before parsing
        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
          try {
            const cartData = await response.json()
            setCartCount(Array.isArray(cartData) ? cartData.length : 0)
          } catch (parseError) {
            console.error("Error parsing cart data:", parseError)
            setCartCount(0)
          }
        } else {
          // No JSON content
          setCartCount(0)
        }
      }
    } catch (err) {
      console.error("Error fetching cart count:", err)
      setCartCount(0)
    }
  }

  useEffect(() => {
    const init = async () => {
      const { data } = await supabase.auth.getSession()
      if (data.session?.user) {
        // Fetch user role from database
        const { data: profile } = await supabase
          .from('user_profiles')
          .select('role')
          .eq('user_id', data.session.user.id)
          .single()
        
        setUser({
          ...data.session.user,
          role: profile?.role || 'user',
          name: data.session.user.email?.split('@')[0] || 'User'
        })
        
        // Fetch cart count
        fetchCartCount()
      }
    }
    init()

    const { data: subscription } = supabase.auth.onAuthStateChange(async (event, session) => {
      if (session?.user) {
        // Fetch user role from database
        const { data: profile } = await supabase
          .from('user_profiles')
          .select('role')
          .eq('user_id', session.user.id)
          .single()
        
        setUser({
          ...session.user,
          role: profile?.role || 'user',
          name: session.user.email?.split('@')[0] || 'User'
        })
        
        // Fetch cart count
        fetchCartCount()
      } else {
        setUser(null)
        setCartCount(0)
      }
    })

    return () => {
      subscription.subscription?.unsubscribe()
    }
  }, [])

  const handleCalculationComplete = async (results: any) => {
    console.log('Raw calculation results:', results)
    
    // The TariffCalculatorForm already fetches the calculation ID from backend history
    // Wrap it properly with metadata
    const calculationWithId = {
      data: results, // This is the actual calculation data from backend
      calculationId: results.calculationId || `calc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      calculationDate: results.calculationDate || new Date().toISOString()
    }
    
    console.log('Processed calculation:', calculationWithId)
    
    setCalculationResults(calculationWithId)
    
    // Fetch updated history from backend
    try {
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token
      
      const historyResponse = await fetch('http://localhost:8080/api/tariff/history', {
        method: 'GET',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      })
      
      if (historyResponse.ok) {
        const historyData = await historyResponse.json()
        setSessionHistory(Array.isArray(historyData) ? historyData : [])
      }
    } catch (err) {
      console.error("Error fetching history:", err)
    }
  }

  const handleAddToCart = async (calculation: any) => {
    // The ResultsTable component now handles adding to cart directly via API
    // This callback is kept for compatibility but the actual work is done in ResultsTable
    // Refresh cart count after adding
    await fetchCartCount()
    return true
  }

  const handleRemoveFromCart = async (calculationId: string) => {
    // ExportPage handles removal via API, just refresh count here
    await fetchCartCount()
  }

  const handleClearCart = async () => {
    // ExportPage handles clearing via API, just refresh count here
    await fetchCartCount()
  }

  const handleClearHistory = async () => {
    // Refresh history from backend
    try {
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token
      
      const historyResponse = await fetch('http://localhost:8080/api/tariff/history', {
        method: 'GET',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      })
      
      if (historyResponse.ok) {
        const historyData = await historyResponse.json()
        setSessionHistory(Array.isArray(historyData) ? historyData : [])
      }
    } catch (err) {
      console.error("Error fetching history:", err)
    }
  }

  if (!user) {
    if (authView === "login") {
      return <LoginForm onLogin={handleLogin} onSwitchToSignup={() => setAuthView("signup")} />
    } else {
      return <SignupForm onSignup={handleSignup} onSwitchToLogin={() => setAuthView("login")} />
    }
  }

  
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <h1 className="text-2xl font-bold text-slate-900">TariffWise</h1>
              <nav className="flex space-x-4">
                <button
                  onClick={() => setCurrentView("dashboard")}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentView === "dashboard" ? "bg-slate-100 text-slate-900" : "text-slate-600 hover:text-slate-900"
                  }`}
                >
                  Dashboard
                </button>
                <button
                  onClick={() => setCurrentView("global-tariffs")}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentView === "global-tariffs"
                      ? "bg-slate-100 text-slate-900"
                      : "text-slate-600 hover:text-slate-900"
                  }`}
                >
                  Global Tariffs
                </button>
                <button
                  onClick={() => setCurrentView("simulator-tariffs")}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentView === "simulator-tariffs"
                      ? "bg-slate-100 text-slate-900"
                      : "text-slate-600 hover:text-slate-900"
                  }`}
                >
                  Simulator Tariffs
                </button>
                <button
                  onClick={() => {
                    setCurrentView("cart")
                    fetchCartCount() // Refresh cart count when navigating to cart
                  }}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentView === "cart" ? "bg-slate-100 text-slate-900" : "text-slate-600 hover:text-slate-900"
                  }`}
                >
                  Export Cart ({cartCount})
                </button>
              </nav>
            </div>

            <div className="flex items-center space-x-4">
              <Button
                onClick={async () => {
                  setCurrentView("history")
                  // Refresh history count when navigating to history
                  try {
                    const { data: { session } } = await supabase.auth.getSession()
                    const token = session?.access_token
                    
                    const historyResponse = await fetch('http://localhost:8080/api/tariff/history', {
                      method: 'GET',
                      headers: {
                        'Authorization': token ? `Bearer ${token}` : '',
                        'Content-Type': 'application/json'
                      },
                      credentials: 'include'
                    })
                    
                    if (historyResponse.ok) {
                      const historyData = await historyResponse.json()
                      setSessionHistory(Array.isArray(historyData) ? historyData : [])
                    }
                  } catch (err) {
                    console.error("Error fetching history:", err)
                  }
                }}
                variant="outline"
                size="sm"
                className="flex items-center space-x-2 bg-transparent"
              >
                <History className="h-4 w-4" />
                <span>Session History ({sessionHistory.length})</span>
              </Button>
              <div className="flex items-center space-x-2 text-sm text-slate-600">
                {user.role === "admin" ? <Shield className="h-4 w-4 text-accent" /> : <User className="h-4 w-4" />}
                <span>{user.name}</span>
                <span className="text-xs bg-slate-100 px-2 py-1 rounded-full">
                  {user.role === "admin" ? "Admin" : "User"}
                </span>
              </div>
              <Button
                onClick={handleLogout}
                variant="outline"
                size="sm"
                className="flex items-center space-x-2 bg-transparent"
              >
                <LogOut className="h-4 w-4" />
                <span>Logout</span>
              </Button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {currentView === "dashboard" && (
          <>
            <div className="mb-8">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-3xl font-bold text-slate-900 mb-2">Tariff Calculator</h2>
                  <p className="text-slate-600">Calculate import costs using global or simulated tariffs.</p>
                </div>
                <div className="flex items-center space-x-3">
                  <label className="text-sm font-medium text-slate-700">Calculator Mode:</label>
                  <Select
                    value={calculatorMode}
                    onValueChange={(value) => setCalculatorMode(value as "global" | "simulator")}
                  >
                    <SelectTrigger className="w-[180px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="global">Global Mode</SelectItem>
                      <SelectItem value="simulator">Simulator Mode</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {calculatorMode === "simulator" && (
                <div className="bg-gradient-to-r from-purple-50 to-blue-50 border-2 border-purple-200 rounded-lg p-4 mb-6">
                  <div className="flex items-center space-x-2 mb-1">
                    <div className="h-3 w-3 bg-purple-500 rounded-full animate-pulse"></div>
                    <h3 className="text-base font-semibold text-purple-900">Simulator Mode Active</h3>
                  </div>
                  <p className="text-sm text-purple-700">
                    Using simulated tariffs. Define your tariffs in the Simulator Tariffs tab.
                  </p>
                </div>
              )}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
              <TariffCalculatorForm
                onCalculationComplete={handleCalculationComplete}
                tariffSource={calculatorMode === "global" ? "global" : "user"}
              />

              <Card>
                <CardHeader>
                  <CardTitle className="text-xl font-semibold text-slate-900">Historical Data</CardTitle>
                  <CardDescription>
                    Showing total import cost trends over time for {calculationResults?.data?.product || calculationResults?.product || "selected products"}{" "}
                    from {calculationResults?.data?.exportingFrom || calculationResults?.exportingFrom || "various countries"} to{" "}
                    {calculationResults?.data?.importingTo || calculationResults?.importingTo || "destination countries"}.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-center h-48 bg-slate-50 rounded-lg">
                    <p className="text-slate-500">
                      {calculationResults
                        ? "Historical data visualization would appear here"
                        : "No historical data available for this selection."}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </div>

            {calculationResults && (
              <div className="mb-8">
                <ResultsTable results={calculationResults} onAddToCart={handleAddToCart} />
              </div>
            )}
          </>
        )}

        {currentView === "global-tariffs" && (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Global Tariff Definitions</h2>
              <p className="text-slate-600">
                {user.role === "admin"
                  ? "View and manage global tariffs. As an admin, you can edit existing tariffs."
                  : "View official global tariff rates for all country pairs and products."}
              </p>
            </div>
            <TariffDefinitionsTable userRole={user.role} simulatorMode={false} />
          </>
        )}

        {currentView === "simulator-tariffs" && (
          <>
            <div className="bg-gradient-to-r from-purple-50 to-blue-50 border-2 border-purple-200 rounded-lg p-6 mb-6">
              <div className="flex items-center space-x-2 mb-2">
                <div className="h-3 w-3 bg-purple-500 rounded-full animate-pulse"></div>
                <h2 className="text-2xl font-bold text-purple-900">Simulator Tariffs</h2>
              </div>
              <p className="text-purple-700">
                Define temporary tariffs for testing different scenarios. These tariffs are only available in Simulator
                Mode and do not affect global data.
              </p>
            </div>
            <TariffDefinitionsTable userRole={user.role} simulatorMode={true} />
          </>
        )}

        {currentView === "cart" && (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Export Cart</h2>
              <p className="text-slate-600">Manage and export your selected calculations.</p>
            </div>
            <ExportPage />
          </>
        )}

        {currentView === "history" && (
          <>
            <div className="mb-8 flex items-center justify-between">
              <div>
                <h2 className="text-3xl font-bold text-slate-900 mb-2">Session History</h2>
                <p className="text-slate-600">View and manage your calculation history.</p>
              </div>
              <Button
                onClick={() => setCurrentView("dashboard")}
                variant="outline"
                size="sm"
              >
                Back to Dashboard
              </Button>
            </div>
            <SessionHistoryPage />
          </>
        )}
      </main>
    </div>
  )
}
