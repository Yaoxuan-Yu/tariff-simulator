"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { MultiSelect } from "@/components/multi-select"
import { Badge } from "@/components/ui/badge"
import { Loader2, ArrowUpDown, TrendingUp } from "lucide-react"
import {
  Bar,
  BarChart,
  Line,
  LineChart,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"
import supabase from "@/lib/supabaseClient"

const API_BASE_URL = "http://localhost:8080/api"

type TradeMode = "import" | "export"
type TimeFilterType = "year-range" | "specific-year" | "quarter" | "month"
type ChartType = "bar" | "line"

interface ComparisonFilters {
  product: string
  tradeMode: TradeMode
  primaryCountry: string
  comparisonCountries: string[]
  timeFilterType: TimeFilterType
  startYear: string
  endYear: string
  specificYear: string
  quarter: string
  month: string
}

interface ComparisonResult {
  country: string
  tariffRate: number
  effectiveDate: string
  product: string
  tradeDirection: string
}

const CURRENT_YEAR = new Date().getFullYear()
const YEARS = Array.from({ length: 20 }, (_, i) => (CURRENT_YEAR - i).toString())
const QUARTERS = ["Q1", "Q2", "Q3", "Q4"]
const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
]

export function TariffCompare() {
  const [filters, setFilters] = useState<ComparisonFilters>({
    product: "",
    tradeMode: "import",
    primaryCountry: "",
    comparisonCountries: [],
    timeFilterType: "specific-year",
    startYear: CURRENT_YEAR.toString(),
    endYear: CURRENT_YEAR.toString(),
    specificYear: CURRENT_YEAR.toString(),
    quarter: "Q1",
    month: "January",
  })

  const [products, setProducts] = useState<string[]>([])
  const [countries, setCountries] = useState<string[]>([])
  const [results, setResults] = useState<ComparisonResult[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState("")
  const [chartType, setChartType] = useState<ChartType>("bar")
  const [hasCompared, setHasCompared] = useState(false)

  const getAuthToken = async (): Promise<string> => {
    const {
      data: { session },
    } = await supabase.auth.getSession()
    return session?.access_token || ""
  }

  const createAuthHeaders = (token: string): Record<string, string> => ({
    Authorization: token ? `Bearer ${token}` : "",
    "Content-Type": "application/json",
  })

  const fetchDataWithAuth = async (endpoint: string): Promise<any> => {
    const token = await getAuthToken()
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: createAuthHeaders(token),
      credentials: "include",
    })
    return response.json()
  }

  useEffect(() => {
    loadInitialData()
  }, [])

  const loadInitialData = async () => {
    try {
      const productsData = await fetchDataWithAuth("/products")
      setProducts(Array.isArray(productsData) ? productsData : [])

      const countriesData = await fetchDataWithAuth("/countries")
      setCountries(Array.isArray(countriesData) ? countriesData : [])
    } catch (err) {
      console.error("Error loading initial data:", err)
    }
  }

  const handleFilterChange = (field: keyof ComparisonFilters, value: any) => {
    setFilters((prev) => ({ ...prev, [field]: value }))
    setError("")
  }

  const validateFilters = (): boolean => {
    if (!filters.product) {
      setError("Please select a product")
      return false
    }
    if (!filters.primaryCountry) {
      setError("Please select a primary country")
      return false
    }
    if (filters.comparisonCountries.length === 0) {
      setError("Please select at least one comparison country")
      return false
    }
    if (filters.comparisonCountries.includes(filters.primaryCountry)) {
      setError("Primary country cannot be in comparison countries")
      return false
    }
    if (filters.timeFilterType === "year-range") {
      const start = Number.parseInt(filters.startYear)
      const end = Number.parseInt(filters.endYear)
      if (start > end) {
        setError("Start year must be before or equal to end year")
        return false
      }
    }
    return true
  }

  const buildComparisonPayload = () => {
    const payload: any = {
      product: filters.product,
      tradeMode: filters.tradeMode,
      primaryCountry: filters.primaryCountry,
      comparisonCountries: filters.comparisonCountries,
      timeFilter: {
        type: filters.timeFilterType,
      },
    }

    if (filters.timeFilterType === "year-range") {
      payload.timeFilter.startYear = Number.parseInt(filters.startYear)
      payload.timeFilter.endYear = Number.parseInt(filters.endYear)
    } else if (filters.timeFilterType === "specific-year") {
      payload.timeFilter.year = Number.parseInt(filters.specificYear)
    } else if (filters.timeFilterType === "quarter") {
      payload.timeFilter.year = Number.parseInt(filters.specificYear)
      payload.timeFilter.quarter = filters.quarter
    } else if (filters.timeFilterType === "month") {
      payload.timeFilter.year = Number.parseInt(filters.specificYear)
      payload.timeFilter.month = filters.month
    }

    return payload
  }

  const handleCompare = async () => {
    if (!validateFilters()) return

    setIsLoading(true)
    setError("")

    try {
      const token = await getAuthToken()
      const payload = buildComparisonPayload()

      const response = await fetch(`${API_BASE_URL}/tariff/compare`, {
        method: "POST",
        headers: createAuthHeaders(token),
        credentials: "include",
        body: JSON.stringify(payload),
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: `HTTP ${response.status}` }))
        setError(errorData.error || "Comparison failed")
        return
      }

      const data = await response.json()
      setResults(data.results || [])
      setHasCompared(true)
    } catch (err) {
      console.error("Comparison error:", err)
      setError("An unexpected error occurred during comparison")
    } finally {
      setIsLoading(false)
    }
  }

  const getAvailableCountriesForComparison = (): { label: string; value: string }[] => {
    return countries.filter((c) => c !== filters.primaryCountry).map((c) => ({ label: c, value: c }))
  }

  const isCompareButtonDisabled = (): boolean => {
    return !filters.product || !filters.primaryCountry || filters.comparisonCountries.length === 0 || isLoading
  }

  const getPrimaryCountryRole = (): string => {
    return filters.tradeMode === "import" ? "Importer" : "Exporter"
  }

  const getComparisonCountriesRole = (): string => {
    return filters.tradeMode === "import" ? "Exporters" : "Importers"
  }

  const getChartData = () => {
    return results.map((result) => ({
      country: result.country,
      tariffRate: result.tariffRate,
      label: `${result.country} (${result.tariffRate}%)`,
    }))
  }

  const renderChart = () => {
    const chartData = getChartData()

    if (chartData.length === 0) {
      return (
        <div className="h-[400px] flex items-center justify-center text-muted-foreground">
          No data to display. Click "Compare" to see results.
        </div>
      )
    }

    if (chartType === "bar") {
      return (
        <ResponsiveContainer width="100%" height={400}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="country" />
            <YAxis label={{ value: "Tariff Rate (%)", angle: -90, position: "insideLeft" }} />
            <Tooltip />
            <Legend />
            <Bar dataKey="tariffRate" fill="hsl(var(--chart-1))" name="Tariff Rate (%)" />
          </BarChart>
        </ResponsiveContainer>
      )
    } else {
      return (
        <ResponsiveContainer width="100%" height={400}>
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="country" />
            <YAxis label={{ value: "Tariff Rate (%)", angle: -90, position: "insideLeft" }} />
            <Tooltip />
            <Legend />
            <Line
              type="monotone"
              dataKey="tariffRate"
              stroke="hsl(var(--chart-1))"
              strokeWidth={2}
              name="Tariff Rate (%)"
            />
          </LineChart>
        </ResponsiveContainer>
      )
    }
  }

  const renderResultsTable = () => {
    if (results.length === 0) {
      return (
        <div className="text-center text-muted-foreground py-8">
          No comparison results yet. Configure filters and click "Compare" to see data.
        </div>
      )
    }

    return (
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b">
              <th className="text-left py-3 px-4 font-semibold">Country</th>
              <th className="text-left py-3 px-4 font-semibold">Role</th>
              <th className="text-left py-3 px-4 font-semibold">Tariff Rate</th>
              <th className="text-left py-3 px-4 font-semibold">Effective Date</th>
            </tr>
          </thead>
          <tbody>
            {results.map((result, idx) => (
              <tr key={idx} className="border-b hover:bg-muted/50">
                <td className="py-3 px-4 font-medium">
                  {result.country}
                  {result.country === filters.primaryCountry && (
                    <Badge variant="secondary" className="ml-2">
                      Primary
                    </Badge>
                  )}
                </td>
                <td className="py-3 px-4">
                  {result.country === filters.primaryCountry
                    ? getPrimaryCountryRole()
                    : getComparisonCountriesRole().slice(0, -1)}
                </td>
                <td className="py-3 px-4 font-semibold text-chart-1">{result.tariffRate}%</td>
                <td className="py-3 px-4 text-muted-foreground">{result.effectiveDate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ArrowUpDown className="h-5 w-5" />
            Tariff Comparison Filters
          </CardTitle>
          <CardDescription>Compare tariff rates for a single product across multiple countries</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Product & Trade Mode */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Product</label>
              <Select value={filters.product} onValueChange={(value) => handleFilterChange("product", value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a product" />
                </SelectTrigger>
                <SelectContent>
                  {products.map((product) => (
                    <SelectItem key={product} value={product}>
                      {product}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Trade Context</label>
              <Select
                value={filters.tradeMode}
                onValueChange={(value) => handleFilterChange("tradeMode", value as TradeMode)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="import">Import (Primary country imports)</SelectItem>
                  <SelectItem value="export">Export (Primary country exports)</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Primary Country & Comparison Countries */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Primary Country ({getPrimaryCountryRole()})</label>
              <Select
                value={filters.primaryCountry}
                onValueChange={(value) => handleFilterChange("primaryCountry", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select primary country" />
                </SelectTrigger>
                <SelectContent>
                  {countries.map((country) => (
                    <SelectItem key={country} value={country}>
                      {country}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Comparison Countries ({getComparisonCountriesRole()})</label>
              <MultiSelect
                options={getAvailableCountriesForComparison()}
                selected={filters.comparisonCountries}
                onChange={(value) => handleFilterChange("comparisonCountries", value)}
                placeholder="Select countries to compare"
              />
            </div>
          </div>

          {/* Time Filter */}
          <div className="space-y-4">
            <label className="text-sm font-medium">Time Period</label>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <Select
                value={filters.timeFilterType}
                onValueChange={(value) => handleFilterChange("timeFilterType", value as TimeFilterType)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="year-range">Year Range</SelectItem>
                  <SelectItem value="specific-year">Specific Year</SelectItem>
                  <SelectItem value="quarter">Quarter</SelectItem>
                  <SelectItem value="month">Month</SelectItem>
                </SelectContent>
              </Select>

              {filters.timeFilterType === "year-range" && (
                <>
                  <Select value={filters.startYear} onValueChange={(value) => handleFilterChange("startYear", value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Start year" />
                    </SelectTrigger>
                    <SelectContent>
                      {YEARS.map((year) => (
                        <SelectItem key={year} value={year}>
                          {year}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Select value={filters.endYear} onValueChange={(value) => handleFilterChange("endYear", value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="End year" />
                    </SelectTrigger>
                    <SelectContent>
                      {YEARS.map((year) => (
                        <SelectItem key={year} value={year}>
                          {year}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </>
              )}

              {filters.timeFilterType === "specific-year" && (
                <Select
                  value={filters.specificYear}
                  onValueChange={(value) => handleFilterChange("specificYear", value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {YEARS.map((year) => (
                      <SelectItem key={year} value={year}>
                        {year}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}

              {filters.timeFilterType === "quarter" && (
                <>
                  <Select
                    value={filters.specificYear}
                    onValueChange={(value) => handleFilterChange("specificYear", value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Year" />
                    </SelectTrigger>
                    <SelectContent>
                      {YEARS.map((year) => (
                        <SelectItem key={year} value={year}>
                          {year}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Select value={filters.quarter} onValueChange={(value) => handleFilterChange("quarter", value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {QUARTERS.map((q) => (
                        <SelectItem key={q} value={q}>
                          {q}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </>
              )}

              {filters.timeFilterType === "month" && (
                <>
                  <Select
                    value={filters.specificYear}
                    onValueChange={(value) => handleFilterChange("specificYear", value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Year" />
                    </SelectTrigger>
                    <SelectContent>
                      {YEARS.map((year) => (
                        <SelectItem key={year} value={year}>
                          {year}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Select value={filters.month} onValueChange={(value) => handleFilterChange("month", value)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {MONTHS.map((month) => (
                        <SelectItem key={month} value={month}>
                          {month}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </>
              )}
            </div>
          </div>

          {/* Compare Button */}
          <Button onClick={handleCompare} disabled={isCompareButtonDisabled()} className="w-full" size="lg">
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Comparing...
              </>
            ) : (
              <>
                <TrendingUp className="mr-2 h-4 w-4" />
                Compare Tariffs
              </>
            )}
          </Button>
        </CardContent>
      </Card>

      {/* Results Section */}
      {hasCompared && (
        <>
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Comparison Results</CardTitle>
                  <CardDescription>Tariff rates for {filters.product} across selected countries</CardDescription>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant={chartType === "bar" ? "default" : "outline"}
                    size="sm"
                    onClick={() => setChartType("bar")}
                  >
                    Bar Chart
                  </Button>
                  <Button
                    variant={chartType === "line" ? "default" : "outline"}
                    size="sm"
                    onClick={() => setChartType("line")}
                  >
                    Line Chart
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent>{renderChart()}</CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Detailed Comparison Table</CardTitle>
              <CardDescription>Side-by-side tariff comparison with effective dates</CardDescription>
            </CardHeader>
            <CardContent>{renderResultsTable()}</CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
