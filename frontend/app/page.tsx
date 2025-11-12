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
import { TariffTrendsVisualization } from "@/components/tariff-trends-visualization"
import { TariffCompare } from "@/components/tariff-compare"
import { TradeInsights } from "@/components/trade-insights"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { LogOut, User, Settings, Shield, ShoppingCart } from "lucide-react"
import { Badge } from "@/components/ui/badge"

type AuthView = "login" | "signup"
type DashboardView = "dashboard" | "global-tariffs" | "simulator-tariffs" | "tariff-compare" | "trade-insights"
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
  const [exportCart, setExportCart] = useState<any[]>([])
  const [cartCount, setCartCount] = useState<number>(0)
  const [isExportSheetOpen, setIsExportSheetOpen] = useState(false)
  const [isHistorySheetOpen, setIsHistorySheetOpen] = useState(false)

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

  const getAuthToken = async (): Promise<string> => {
    const {
      data: { session },
    } = await supabase.auth.getSession()
    return session?.access_token || ""
  }

  const createAuthHeaders = (token: string): Record<string, string> => {
    return {
      Authorization: token ? `Bearer ${token}` : "",
      "Content-Type": "application/json",
    }
  }

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
    } catch (parseError) {
      console.error("Error parsing cart data:", parseError)
      return 0
    }
  }

  const fetchCartCount = async () => {
    try {
      const token = await getAuthToken()
      const response = await fetch(`${API_BASE_URL}/export-cart`, {
        method: "GET",
        headers: createAuthHeaders(token),
        credentials: "include",
      })

      const count = await parseCartResponse(response)
      setCartCount(count)
    } catch (err) {
      console.error("Error fetching cart count:", err)
      setCartCount(0)
    }
  }

  const fetchUserProfile = async (userId: string) => {
    const { data: profile } = await supabase.from("user_profiles").select("role").eq("user_id", userId).single()

    return profile?.role || "user"
  }

  const createUserObject = (sessionUser: any, role: string) => {
    return {
      ...sessionUser,
      role,
      name: sessionUser.email?.split("@")[0] || "User",
    }
  }

  const initializeUser = async (sessionUser: any) => {
    const role = await fetchUserProfile(sessionUser.id)
    const userObject = createUserObject(sessionUser, role)
    setUser(userObject)
    await fetchCartCount()
  }

  useEffect(() => {
    const init = async () => {
      const { data } = await supabase.auth.getSession()
      if (data.session?.user) {
        await initializeUser(data.session.user)
      }
    }
    init()

    const { data: subscription } = supabase.auth.onAuthStateChange(async (event, session) => {
      if (session?.user) {
        await initializeUser(session.user)
      } else {
        setUser(null)
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
      calculationId: results.calculationId || generateCalculationId(),
      calculationDate: results.calculationDate || new Date().toISOString(),
    }
  }

  const fetchCalculationHistory = async () => {
    try {
      const token = await getAuthToken()
      const historyResponse = await fetch(`${API_BASE_URL}/tariff/history`, {
        method: "GET",
        headers: createAuthHeaders(token),
        credentials: "include",
      })

      if (historyResponse.ok) {
        const historyData = await historyResponse.json()
        setSessionHistory(Array.isArray(historyData) ? historyData : [])
      }
    } catch (err) {
      console.error("Error fetching history:", err)
    }
  }

  const handleCalculationComplete = async (results: any) => {
    console.log("Raw calculation results:", results)

    const calculationWithId = wrapCalculationWithMetadata(results)
    console.log("Processed calculation:", calculationWithId)

    setCalculationResults(calculationWithId)
    await fetchCalculationHistory()
  }

  const handleAddToCart = async (calculation: any) => {
    await fetchCartCount()
    return true
  }

  const handleRemoveFromCart = async (calculationId: string) => {
    await fetchCartCount()
  }

  const handleClearCart = async () => {
    await fetchCartCount()
  }

  const handleClearHistory = async () => {
    await fetchCalculationHistory()
  }

  const getUserDisplayName = (): string => {
    return user?.name || "User"
  }

  const getUserRoleLabel = (): string => {
    return user?.role === "admin" ? "Admin" : "User"
  }

  const renderUserRoleIcon = () => {
    return user?.role === "admin" ? <Shield className="h-4 w-4 text-accent" /> : <User className="h-4 w-4" />
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

  const renderAuthView = () => {
    if (authView === "login") {
      return <LoginForm onLogin={handleLogin} />
    } else if (authView === "signup") {
      return <SignupForm onSignup={handleSignup} />
    }
    return null
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
        <header className="bg-white shadow-sm border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center h-16">
              <div className="flex items-center space-x-8">
                <h1 className="text-2xl font-bold text-slate-900">TariffWise</h1>
              </div>
            </div>
          </div>
        </header>
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">{renderAuthView()}</main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <h1 className="text-2xl font-bold text-slate-900">TariffWise</h1>
              <nav className="flex space-x-4">
                <button onClick={() => setCurrentView("dashboard")} className={getNavButtonClasses("dashboard")}>
                  Dashboard
                </button>
                <button
                  onClick={() => setCurrentView("global-tariffs")}
                  className={getNavButtonClasses("global-tariffs")}
                >
                  Global Tariffs
                </button>
                <button
                  onClick={() => setCurrentView("simulator-tariffs")}
                  className={getNavButtonClasses("simulator-tariffs")}
                >
                  Simulator Tariffs
                </button>
                <button
                  onClick={() => setCurrentView("tariff-compare")}
                  className={getNavButtonClasses("tariff-compare")}
                >
                  Tariff Compare
                </button>
                <button
                  onClick={() => setCurrentView("trade-insights")}
                  className={getNavButtonClasses("trade-insights")}
                >
                  Trade Insights
                </button>
              </nav>
            </div>

            <div className="flex items-center space-x-4">
              <Sheet open={isExportSheetOpen} onOpenChange={setIsExportSheetOpen}>
                <SheetTrigger asChild>
                  <Button variant="outline" size="sm" className="relative flex items-center space-x-2 bg-transparent">
                    <ShoppingCart className="h-4 w-4" />
                    {cartCount > 0 && (
                      <Badge className="absolute -top-2 -right-2 h-5 w-5 flex items-center justify-center p-0 text-xs">
                        {cartCount}
                      </Badge>
                    )}
                  </Button>
                </SheetTrigger>
                <SheetContent side="right" className="w-full sm:max-w-3xl overflow-y-auto">
                  <SheetHeader>
                    <SheetTitle>Export Cart</SheetTitle>
                  </SheetHeader>
                  <div className="mt-6">
                    <ExportPage />
                  </div>
                </SheetContent>
              </Sheet>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" size="sm" className="flex items-center space-x-2 bg-transparent">
                    <Settings className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuLabel className="flex items-center space-x-2">
                    {renderUserRoleIcon()}
                    <div className="flex flex-col">
                      <span>{getUserDisplayName()}</span>
                      <span className="text-xs font-normal text-muted-foreground">{getUserRoleLabel()}</span>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={() => {
                      setIsHistorySheetOpen(true)
                      fetchCalculationHistory()
                    }}
                  >
                    Session History ({sessionHistory.length})
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="h-4 w-4 mr-2" />
                    Logout
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>

              <Sheet open={isHistorySheetOpen} onOpenChange={setIsHistorySheetOpen}>
                <SheetContent side="right" className="w-full sm:max-w-4xl overflow-y-auto">
                  <SheetHeader>
                    <SheetTitle>Session History</SheetTitle>
                  </SheetHeader>
                  <div className="mt-6">
                    <SessionHistoryPage />
                  </div>
                </SheetContent>
              </Sheet>
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
                  <Select value={calculatorMode} onValueChange={(value) => setCalculatorMode(value as CalculatorMode)}>
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
                tariffSource={getTariffSource()}
              />

              <Card>
                <CardHeader>
                  <CardTitle className="text-xl font-semibold text-slate-900">Historical Trends</CardTitle>
                  <CardDescription>
                    Visualize tariff changes over time for {getCalculationProductName()} from{" "}
                    {getCalculationExportingFrom()} to {getCalculationImportingTo()}.
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

        {currentView === "tariff-compare" && (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Tariff Compare</h2>
              <p className="text-slate-600">
                Compare tariff rates for a single product across multiple countries over time.
              </p>
            </div>
            <TariffCompare />
          </>
        )}

        {currentView === "trade-insights" && (
          <>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-slate-900 mb-2">Trade Insights</h2>
              <p className="text-slate-600">
                Discover trade-related news articles and official trade agreements relevant to your research.
              </p>
            </div>
            <TradeInsights />
          </>
        )}
      </main>
    </div>
  )
}

const getNavButtonClasses = (view: DashboardView): string => {
  const baseClasses = "px-3 py-2 text-sm font-medium rounded-md"
  const activeClasses = "bg-slate-100 text-slate-900"
  const inactiveClasses = "text-slate-600 hover:text-slate-900"
  return `${baseClasses} ${view === "dashboard" ? activeClasses : inactiveClasses}`
}
