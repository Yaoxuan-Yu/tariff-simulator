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
import { LogOut, User, History } from "lucide-react"

export default function Home() {
  const [user, setUser] = useState<any>(null)
  const [calculationResults, setCalculationResults] = useState<any>(null)
  const [currentView, setCurrentView] = useState<"dashboard" | "tariffs" | "cart" | "history">("dashboard")
  const [authView, setAuthView] = useState<"login" | "signup">("login")
  const [sessionHistory, setSessionHistory] = useState<any[]>([])
  const [exportCart, setExportCart] = useState<any[]>([])

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

  useEffect(() => {
    const init = async () => {
      const { data } = await supabase.auth.getSession()
      if (data.session?.user) {
        setUser(data.session.user)
      }
    }
    init()

    const { data: subscription } = supabase.auth.onAuthStateChange((event, session) => {
      setUser(session?.user ?? null)
      
      if (event === 'SIGNED_IN' && session?.user) {
        setUser(session.user)
      }
    })

    return () => {
      subscription.subscription?.unsubscribe()
    }
  }, [])

  const handleCalculationComplete = (results: any) => {
    console.log('Raw calculation results:', results)
    
    // The TariffCalculatorForm passes result.data, which contains the actual calculation
    // Wrap it properly with metadata
    const calculationWithId = {
      data: results, // This is the actual calculation data from backend
      calculationId: `calc_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      calculationDate: new Date().toISOString()
    }
    
    console.log('Processed calculation:', calculationWithId)
    
    setCalculationResults(calculationWithId)
    
    // Automatically add to session history
    setSessionHistory(prev => [calculationWithId, ...prev])
  }

  const handleAddToCart = (calculation: any) => {
    // Check if already in cart
    const exists = exportCart.some(item => item.calculationId === calculation.calculationId)
    if (!exists) {
      setExportCart(prev => [...prev, calculation])
      return true
    }
    return false
  }

  const handleRemoveFromCart = (calculationId: string) => {
    setExportCart(prev => prev.filter(item => item.calculationId !== calculationId))
  }

  const handleClearCart = () => {
    setExportCart([])
  }

  const handleClearHistory = () => {
    setSessionHistory([])
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
                  onClick={() => setCurrentView("tariffs")}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentView === "tariffs" ? "bg-slate-100 text-slate-900" : "text-slate-600 hover:text-slate-900"
                  }`}
                >
                  Tariffs
                </button>
                <button
                  onClick={() => setCurrentView("cart")}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentView === "cart" ? "bg-slate-100 text-slate-900" : "text-slate-600 hover:text-slate-900"
                  }`}
                >
                  Export Cart ({exportCart.length})
                </button>
              </nav>
            </div>

            <div className="flex items-center space-x-4">
              <Button
                onClick={() => setCurrentView("history")}
                variant="outline"
                size="sm"
                className="flex items-center space-x-2 bg-transparent"
              >
                <History className="h-4 w-4" />
                <span>Session History ({sessionHistory.length})</span>
              </Button>
              <div className="flex items-center space-x-2 text-sm text-slate-600">
                <User className="h-4 w-4" />
                <span>{user.user_metadata?.full_name || user.email || 'User'}</span>
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
        {currentView === "dashboard" ? (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Dashboard</h2>
              <p className="text-slate-600">Calculate import costs and analyze historical trends.</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
              <TariffCalculatorForm onCalculationComplete={handleCalculationComplete} />

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
        ) : currentView === "tariffs" ? (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Tariff Definitions</h2>
              <p className="text-slate-600">View, manage, and define import tariffs and fees.</p>
            </div>
            <TariffDefinitionsTable />
          </>
        ) : currentView === "cart" ? (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Export Cart</h2>
              <p className="text-slate-600">Manage and export your selected calculations.</p>
            </div>
            <ExportPage 
              cartItems={exportCart} 
              onRemoveFromCart={handleRemoveFromCart}
              onClearCart={handleClearCart}
            />
          </>
        ) : (
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
            <SessionHistoryPage 
              historyItems={sessionHistory}
              onAddToCart={handleAddToCart}
              onClearHistory={handleClearHistory}
            />
          </>
        )}
      </main>
    </div>
  )
}