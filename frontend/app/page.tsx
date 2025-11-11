"use client"

import { useEffect, useState } from "react"
import supabase from "@/lib/supabaseClient"
import { LoginForm } from "@/components/login-form"
import { SignupForm } from "@/components/signup-form"
import { TariffCalculatorForm } from "@/components/tariff-calculator-form"
import { ResultsTable } from "@/components/results-table"
import { TariffDefinitionsTable } from "@/components/tariff-definitions-table"
import { ExportCartWithHistory } from "@/components/export-cart-with-history"
import { SimulatorCalculator } from "@/components/simulator-calculator"
import { AdminDashboard } from "@/components/admin-dashboard"
import { TariffTrendsVisualization } from "@/components/tariff-trends-visualization"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { LogOut, User, History, Shield } from "lucide-react"

type AuthView = "login" | "signup"
type DashboardView = "dashboard" | "global-tariffs" | "simulator-tariffs" | "cart" | "history" | "admin"
type CalculatorMode = "global" | "simulator"

const API_BASE_URL = "http://localhost:8080/api"
const EMPTY_CART_STATUS = 204

export default function Home() {
  const [user, setUser] = useState<any>(null)
  const [authView, setAuthView] = useState<AuthView>("login")
  const [calculationResults, setCalculationResults] = useState<any>(null)
  const [currentView, setCurrentView] = useState<DashboardView>("dashboard")
  const [calculatorMode, setCalculatorMode] = useState<CalculatorMode>("global")
  const [sessionHistory, setSessionHistory] = useState<any[]>([])
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
    setCartCount(0)
  }

  const getAuthToken = async (): Promise<string> => {
    const { data: { session } } = await supabase.auth.getSession()
    return session?.access_token || ""
  }

  const createAuthHeaders = (token: string): Record<string, string> => ({
    "Authorization": token ? `Bearer ${token}` : "",
    "Content-Type": "application/json"
  })

  const parseCartResponse = async (response: Response): Promise<number> => {
    if (response.status === EMPTY_CART_STATUS || !response.ok) {
      return 0
    }

    const contentType = response.headers.get("content-type")
    if (!contentType || !contentType.includes("application/json")) {
      return 0
    }

    try {
      const cartData = await response.json()
      return Array.isArray(cartData) ? cartData.length : 0
    } catch (error) {
      console.error("Error parsing cart data:", error)
      return 0
    }
  }

  const fetchCartCount = async () => {
    try {
      const token = await getAuthToken()
      const response = await fetch(`${API_BASE_URL}/export-cart`, {
        method: "GET",
        headers: createAuthHeaders(token),
        credentials: "include"
      })

      const count = await parseCartResponse(response)
      setCartCount(count)
    } catch (err) {
      console.error("Error fetching cart count:", err)
      setCartCount(0)
    }
  }

  const fetchCalculationHistory = async () => {
    try {
      const token = await getAuthToken()
      const response = await fetch(`${API_BASE_URL}/tariff/history`, {
        method: "GET",
        headers: createAuthHeaders(token),
        credentials: "include"
      })

      if (response.ok) {
        const historyData = await response.json()
        setSessionHistory(Array.isArray(historyData) ? historyData : [])
      }
    } catch (err) {
      console.error("Error fetching history:", err)
    }
  }

  const fetchUserProfile = async (userId: string) => {
    const { data: profile } = await supabase
      .from("user_profiles")
      .select("role")
      .eq("user_id", userId)
      .single()

    return profile?.role
  }

  const resolveUserRole = async (sessionUser: any) => {
    const metadataRole = (sessionUser.app_metadata?.role || sessionUser.user_metadata?.role)
    if (metadataRole) {
      return String(metadataRole).toLowerCase()
    }

    const profileRole = await fetchUserProfile(sessionUser.id)
    return (profileRole || "user").toLowerCase()
  }

  const initializeUser = async (sessionUser: any) => {
    const role = await resolveUserRole(sessionUser)
    const name = sessionUser.user_metadata?.full_name || sessionUser.email?.split("@")[0] || "User"

    setUser({
      ...sessionUser,
      role,
      name
    })

    await Promise.all([fetchCartCount(), fetchCalculationHistory()])
  }

  useEffect(() => {
    const init = async () => {
      const { data } = await supabase.auth.getSession()
      if (data.session?.user) {
        await initializeUser(data.session.user)
      }
    }
    init()

    const { data: subscription } = supabase.auth.onAuthStateChange(async (_event, session) => {
      if (session?.user) {
        await initializeUser(session.user)
      } else {
        setUser(null)
        setSessionHistory([])
        setCartCount(0)
      }
    })

    return () => {
      subscription.subscription?.unsubscribe()
    }
  }, [])

  const generateCalculationId = (): string => {
    const timestamp = Date.now()
    const random = Math.random().toString(36).substr(2, 9)
    return `calc_${timestamp}_${random}`
  }

  const wrapCalculationWithMetadata = (results: any) => {
    return {
      data: results,
      calculationId: results?.calculationId || generateCalculationId(),
      calculationDate: results?.calculationDate || new Date().toISOString()
    }
  }

  const handleCalculationComplete = async (results: any) => {
    const calculationWithId = wrapCalculationWithMetadata(results)
    setCalculationResults(calculationWithId)
    await fetchCalculationHistory()
  }

  const handleAddToCart = async () => {
    await fetchCartCount()
    return true
  }

  const handleClearHistory = async () => {
    try {
      const token = await getAuthToken()
      const response = await fetch(`${API_BASE_URL}/tariff/history`, {
        method: "DELETE",
        headers: createAuthHeaders(token),
        credentials: "include"
      })

      if (response.ok) {
        setSessionHistory([])
      }
    } catch (err) {
      console.error("Error clearing history:", err)
    }
  }

  const navigateToHistory = async () => {
    setCurrentView("history")
    await fetchCalculationHistory()
  }

  const navigateToCart = async () => {
    setCurrentView("cart")
    await fetchCartCount()
  }

  const renderAuthView = () => {
    if (authView === "login") {
      return <LoginForm onLogin={handleLogin} onSwitchToSignup={() => setAuthView("signup")} />
    }
    return <SignupForm onSignup={handleSignup} onSwitchToLogin={() => setAuthView("login")} />
  }

  const getNavButtonClasses = (view: DashboardView): string => {
    const baseClasses = "px-3 py-2 text-sm font-medium rounded-md"
    const activeClasses = "bg-slate-100 text-slate-900"
    const inactiveClasses = "text-slate-600 hover:text-slate-900"
    return `${baseClasses} ${currentView === view ? activeClasses : inactiveClasses}`
  }

  const getUserDisplayName = (): string => {
    return user?.name || "User"
  }

  const getUserRoleLabel = (): string => {
    return user?.role === "admin" ? "Admin" : "User"
  }

  const renderUserRoleIcon = () => {
    return user?.role === "admin"
      ? <Shield className="h-4 w-4 text-accent" />
      : <User className="h-4 w-4" />
  }

  const getCalculationProductName = (): string => {
    return calculationResults?.data?.product || calculationResults?.product || "selected products"
  }

  const getCalculationExportingFrom = (): string => {
    return calculationResults?.data?.exportingFrom || calculationResults?.exportingFrom || "various countries"
  }

  const getCalculationImportingTo = (): string => {
    return calculationResults?.data?.importingTo || calculationResults?.importingTo || "destination countries"
  }

  const getTariffSource = (): "global" | "user" => {
    return calculatorMode === "global" ? "global" : "user"
  }

  const renderHistoryView = () => (
    <>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-slate-900 mb-2">Session History</h2>
          <p className="text-slate-600">View your recent calculations and quickly revisit important results.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" size="sm" onClick={() => setCurrentView("dashboard")}>
            Back to Dashboard
          </Button>
          <Button variant="outline" size="sm" onClick={fetchCalculationHistory}>
            Refresh
          </Button>
          <Button variant="destructive" size="sm" onClick={handleClearHistory}>
            Clear History
          </Button>
        </div>
      </div>

      {sessionHistory.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            No calculations found in your history yet. Run a calculation to populate this list.
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {sessionHistory.map((entry: any, index) => {
            const entryId = entry.id || entry.calculationId || `${index}`
            const breakdown = Array.isArray(entry.breakdown) ? entry.breakdown : []
            const totalCost = entry.totalCost ?? entry.summary?.totalCost

            return (
              <Card key={entryId}>
                <CardHeader className="space-y-1">
                  <CardTitle className="text-lg font-semibold text-slate-900">
                    {entry.product || "Unknown Product"}
                  </CardTitle>
                  <CardDescription>
                    {entry.exportingFrom || "Origin"} → {entry.importingTo || "Destination"} ·{" "}
                    {entry.calculationDate ? new Date(entry.calculationDate).toLocaleString() : "Date unknown"}
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3 text-sm">
                  <div className="flex flex-wrap gap-4 text-slate-600">
                    <span>Quantity: {entry.quantity ?? entry.summary?.quantity ?? "N/A"}</span>
                    <span>Unit: {entry.unit ?? entry.summary?.unit ?? "N/A"}</span>
                    <span>Tariff Type: {entry.tariffType ?? "N/A"}</span>
                    {typeof totalCost === "number" && (
                      <span className="font-medium text-slate-900">
                        Total Cost: ${totalCost.toFixed(2)}
                      </span>
                    )}
                  </div>
                  {breakdown.length > 0 && (
                    <div className="border rounded-md p-3 bg-muted/30">
                      <p className="text-xs uppercase tracking-wide text-muted-foreground mb-2">Breakdown</p>
                      <ul className="space-y-2">
                        {breakdown.map((item: any, idx: number) => (
                          <li key={idx} className="flex flex-wrap justify-between text-sm text-slate-700">
                            <span>{item.description || "Line item"}</span>
                            <span className="font-medium">
                              {item.rate ? `${item.rate} · ` : ""}
                              {typeof item.amount === "number" ? `$${item.amount.toFixed(2)}` : ""}
                            </span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </>
  )

  if (!user) {
    return renderAuthView()
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
                  className={getNavButtonClasses("dashboard")}
                >
                  Dashboard
                </button>
                <button
                  onClick={() => setCurrentView("global-tariffs")}
                  className={getNavButtonClasses("global-tariffs")}
                >
                  Tariff Definitions
                </button>
                <button
                  onClick={() => setCurrentView("simulator-tariffs")}
                  className={getNavButtonClasses("simulator-tariffs")}
                >
                  Tariff Simulator
                </button>
                <button
                  onClick={navigateToCart}
                  className={getNavButtonClasses("cart")}
                >
                  Export Cart ({cartCount})
                </button>
                {user.role === "admin" && (
                  <button
                    onClick={() => setCurrentView("admin")}
                    className={getNavButtonClasses("admin")}
                  >
                    Admin
                  </button>
                )}
              </nav>
            </div>

            <div className="flex items-center space-x-4">
              <Button
                onClick={navigateToHistory}
                variant="outline"
                size="sm"
                className="flex items-center space-x-2 bg-transparent"
              >
                <History className="h-4 w-4" />
                <span>Session History ({sessionHistory.length})</span>
              </Button>
              <div className="flex items-center space-x-2 text-sm text-slate-600">
                {renderUserRoleIcon()}
                <span>{getUserDisplayName()}</span>
                <span className="text-xs bg-slate-100 px-2 py-1 rounded-full">
                  {getUserRoleLabel()}
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
              <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div>
                  <h2 className="text-3xl font-bold text-slate-900 mb-2">Tariff Calculator</h2>
                  <p className="text-slate-600">Calculate import costs using global or simulated tariffs.</p>
                </div>
                <div className="flex items-center space-x-3">
                  <label className="text-sm font-medium text-slate-700">Calculator Mode:</label>
                  <Select
                    value={calculatorMode}
                    onValueChange={(value) => setCalculatorMode(value as CalculatorMode)}
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
                <div className="bg-gradient-to-r from-purple-50 to-blue-50 border-2 border-purple-200 rounded-lg p-4 mt-4">
                  <div className="flex items-center space-x-2 mb-1">
                    <div className="h-3 w-3 bg-purple-500 rounded-full animate-pulse"></div>
                    <h3 className="text-base font-semibold text-purple-900">Simulator Mode Active</h3>
                  </div>
                  <p className="text-sm text-purple-700">
                    Using simulated tariffs. Define your tariffs in the Simulator Tariffs tab or run manual simulations below.
                  </p>
                </div>
              )}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
              <TariffCalculatorForm
                onCalculationComplete={handleCalculationComplete}
                tariffSource={getTariffSource()}
              />

              <Card>
                <CardHeader>
                  <CardTitle className="text-xl font-semibold text-slate-900">Historical Trends</CardTitle>
                  <CardDescription>
                    Visualize tariff changes over time for {getCalculationProductName()} from {getCalculationExportingFrom()} to {getCalculationImportingTo()}.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <TariffTrendsVisualization />
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
                <h2 className="text-2xl font-bold text-purple-900">Tariff Simulator</h2>
              </div>
              <p className="text-purple-700">
                Experiment with tariff scenarios by defining simulated tariff rules or running manual simulations. These do not affect global data.
              </p>
            </div>
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
              <TariffDefinitionsTable userRole={user.role} simulatorMode={true} />
              <SimulatorCalculator onCartCountChange={fetchCartCount} />
            </div>
          </>
        )}

        {currentView === "cart" && (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Export Cart & Session History</h2>
              <p className="text-slate-600">Manage your cart and view session calculations. Add items from history to your cart for export.</p>
            </div>
            <ExportCartWithHistory onCartCountChange={fetchCartCount} />
          </>
        )}

        {currentView === "history" && renderHistoryView()}

        {currentView === "admin" && user.role === "admin" && (
          <AdminDashboard />
        )}
      </main>
    </div>
  )
}

