"use client"
import React, { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import supabase from "@/lib/supabaseClient"

interface Currency {
  code: string
  name: string
  rate: number
  lastUpdated: string
}


interface TariffCalculatorFormProps {
  onCalculationComplete: (results: any) => void
  tariffSource: "global" | "user"
}

const API_BASE_URL = "http://localhost:8080/api"
const MINIMUM_QUANTITY = 0
const QUANTITY_STEP = 0.01
const INITIAL_QUANTITY = "1"

interface FormData {
  product: string
  brand: string
  exportingFrom: string
  importingTo: string
  quantity: string
  customCost: string
  calculationDate: string
  currency?: string  // added currency here
}

const createInitialFormData = (): FormData => ({
  product: "",
  brand: "",
  exportingFrom: "",
  importingTo: "",
  quantity: INITIAL_QUANTITY,
  customCost: "",
  calculationDate: new Date().toISOString().split("T")[0],
  currency: "USD" // default currency
})

export function TariffCalculatorForm({ onCalculationComplete, tariffSource }: TariffCalculatorFormProps) {
  const [formData, setFormData] = useState<FormData>(createInitialFormData())
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [availableBrands, setAvailableBrands] = useState<any[]>([])
  const [products, setProducts] = useState<string[]>([])
  const [countries, setCountries] = useState<string[]>([])
  const [userTariffs, setUserTariffs] = useState<any[]>([])
  const [availableCurrencies, setAvailableCurrencies] = useState<Currency[]>([]) // <-- currency state

  const clearError = () => {
    setError("")
  }

  const getAuthToken = async (): Promise<string> => {
    const { data: { session } } = await supabase.auth.getSession()
    return session?.access_token || ""
  }

  const createAuthHeaders = (token: string): Record<string, string> => ({
    'Authorization': token ? `Bearer ${token}` : '',
    'Content-Type': 'application/json'
  })

  const isGlobalTariffSource = (): boolean => {
    return tariffSource === "global"
  }

  const fetchDataWithAuth = async (endpoint: string): Promise<any> => {
    const token = await getAuthToken()
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: createAuthHeaders(token),
      credentials: 'include'
    })
    return response.json()
  }

  // --- Existing data loading logic remains unchanged ---
  const loadGlobalData = async () => {
    const productsData = await fetchDataWithAuth('/products')
    setProducts(Array.isArray(productsData) ? productsData : [])
    
    const countriesData = await fetchDataWithAuth('/countries')
    setCountries(Array.isArray(countriesData) ? countriesData : [])
  }

  const extractUniqueProducts = (tariffs: any[]): string[] => {
    const uniqueProducts = Array.from(
      new Set(tariffs.map((t: any) => t?.product).filter(Boolean))
    )
    return uniqueProducts as string[]
  }

  const extractUniqueCountries = (tariffs: any[]): string[] => {
    const exportingCountries = tariffs.map((t: any) => t?.exportingFrom).filter(Boolean)
    const importingCountries = tariffs.map((t: any) => t?.importingTo).filter(Boolean)
    const allCountries = [...exportingCountries, ...importingCountries]
    return Array.from(new Set(allCountries)) as string[]
  }

  
  const loadUserDefinedData = async () => {
    const data = await fetchDataWithAuth('/tariff-definitions/user')
    
    if (data.success && Array.isArray(data.data)) {
      setUserTariffs(data.data)
      setProducts(extractUniqueProducts(data.data))
      setCountries(extractUniqueCountries(data.data))
    } else {
      resetUserData()
    }
  }

  const resetUserData = () => {
    setUserTariffs([])
    setProducts([])
    setCountries([])
  }

  const loadInitialData = async () => {
    try {
      if (isGlobalTariffSource()) {
        await loadGlobalData()
      } else {
        await loadUserDefinedData()
      }
    } catch (error) {
      console.error('Error loading initial data:', error)
      resetUserData()
    }
  }

  const resetForm = () => {
    setFormData(createInitialFormData())
    setAvailableBrands([])
  }

const loadMockCurrencies = async () => {
  try {
    const response = await fetch("http://localhost:8089/currency")
    const data = await response.json()
    if (Array.isArray(data)) {
      setAvailableCurrencies(data)
      setFormData((prev) => ({ ...prev, currency: "USD" }))
    }
  } catch (err) {
    console.error("Failed to load currencies:", err)
  }
}

  useEffect(() => {
    resetForm()
    loadInitialData()
    loadMockCurrencies()
  }, [tariffSource])

  // --- Existing brand/product logic remains unchanged ---
  const loadBrandsForProduct = async (product: string) => {
    try {
      const brandsData = await fetchDataWithAuth(`/brands?product=${encodeURIComponent(product)}`)
      const brands = Array.isArray(brandsData) ? brandsData : []
      setAvailableBrands(brands)
    } catch (error) {
      console.error('Error loading brands:', error)
      setAvailableBrands([])
    }
  }

  const handleProductChange = (value: string) => {
    setFormData((prev) => ({ ...prev, product: value, brand: "" }))
    loadBrandsForProduct(value)
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    clearError()
    if (field === "product") handleProductChange(value)
  }

  // --- validation, calculation, and filtering logic remain unchanged ---
  const validateRequiredFields = (): boolean => {
    if (!formData.product || !formData.exportingFrom || !formData.importingTo || !formData.quantity) {
      setError("Please fill in all required fields")
      return false
    }
    return true
  }

  const validateQuantity = (): boolean => {
    if (Number.parseFloat(formData.quantity) <= MINIMUM_QUANTITY) {
      setError("Quantity must be greater than 0")
      return false
    }
    return true
  }

  const validateFormData = (): boolean => validateRequiredFields() && validateQuantity()

  const buildQueryParams = (): URLSearchParams => {
    const params = new URLSearchParams({
      product: formData.product,
      exportingFrom: formData.exportingFrom,
      importingTo: formData.importingTo,
      quantity: formData.quantity,
    })
    if (formData.customCost) params.append('customCost', formData.customCost)
    if (!isGlobalTariffSource()) params.append('mode', 'user')
    return params
  }

  const performCalculation = async (): Promise<any> => {
    const params = buildQueryParams()
    const token = await getAuthToken()
    const response = await fetch(`${API_BASE_URL}/tariff?${params}`, {
      method: 'GET',
      headers: createAuthHeaders(token),
      credentials: 'include'
    })
    return { response, token }
  }

  const handleCalculationError = async (response: Response) => {
    const errorData = await response.json().catch(() => ({ error: `HTTP ${response.status}` }))
    setError(errorData.error || "Calculation failed")
  }

  const fetchLatestCalculationId = async (token: string): Promise<{ id: string; date: string } | null> => {
    try {
      const historyResponse = await fetch(`${API_BASE_URL}/tariff/history`, {
        method: 'GET',
        headers: createAuthHeaders(token),
        credentials: 'include'
      })
      if (historyResponse.ok) {
        const historyData = await historyResponse.json()
        if (Array.isArray(historyData) && historyData.length > 0) {
          const latestCalculation = historyData[0]
          return { id: latestCalculation.id, date: latestCalculation.createdAt }
        }
      }
    } catch (err) {
      console.warn("Could not fetch calculation ID from history:", err)
    }
    return null
  }

  const attachCalculationMetadata = async (result: any, token: string) => {
    const calculationInfo = await fetchLatestCalculationId(token)
    if (calculationInfo) {
      result.data.calculationId = calculationInfo.id
      result.data.calculationDate = calculationInfo.date
    }
  }

  const handleCalculationSuccess = async (result: any, token: string) => {
    if (result.success && result.data) {
      await attachCalculationMetadata(result, token)
      onCalculationComplete(result.data)
    } else {
      setError(result.error || "Calculation failed")
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    clearError()
    setIsLoading(true)
    if (!validateFormData()) { setIsLoading(false); return }

    try {
      const { response, token } = await performCalculation()
      if (!response.ok) { await handleCalculationError(response); return }
      const result = await response.json()
      await handleCalculationSuccess(result, token)
    } catch (err) {
      console.error("Calculation error:", err)
      setError("An unexpected error occurred during calculation")
    } finally {
      setIsLoading(false)
    }
  }

  // --- currency dropdown display logic ---
  const getCurrencyLastUpdated = () => {
    const selectedCurrency = availableCurrencies.find(c => c.code === formData.currency)
    return selectedCurrency?.lastUpdated || "N/A"
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl font-semibold text-foreground">
          {isGlobalTariffSource() ? "Global Tariff Calculator" : "Simulator Calculator"}
        </CardTitle>
        <CardDescription>
          {isGlobalTariffSource() ? "Calculate costs using official global tariffs." : "Calculate costs using your simulated tariffs."}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Row 1: Product + Quantity + Calculation Date */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Product</label>
              <Select value={formData.product} onValueChange={(value) => handleInputChange("product", value)}>
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select a product" />
                </SelectTrigger>
                <SelectContent>
                  {products.map(product => (
                    <SelectItem key={product} value={product}>{product}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Quantity</label>
              <Input
                type="number"
                value={formData.quantity}
                onChange={(e) => handleInputChange("quantity", e.target.value)}
                placeholder="1"
                min={MINIMUM_QUANTITY}
                step={QUANTITY_STEP}
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Calculation Date</label>
              <Input
                type="date"
                value={formData.calculationDate}
                onChange={(e) => handleInputChange("calculationDate", e.target.value)}
              />
            </div>
          </div>

          {/* Row 2: Exporting + Importing */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Exporting From</label>
              <Select
                value={formData.exportingFrom}
                onValueChange={(value) => handleInputChange("exportingFrom", value)}
              >
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select country" />
                </SelectTrigger>
                <SelectContent>
                  {countries.map(country => (
                    <SelectItem key={country} value={country}>{country}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Importing To</label>
              <Select
                value={formData.importingTo}
                onValueChange={(value) => handleInputChange("importingTo", value)}
              >
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select country" />
                </SelectTrigger>
                <SelectContent>
                  {countries.map(country => (
                    <SelectItem key={country} value={country}>{country}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Row 3: Custom Cost + Currency */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Custom Cost (USD)</label>
              <Input
                type="number"
                value={formData.customCost}
                onChange={(e) => handleInputChange("customCost", e.target.value)}
                placeholder="Override brand cost"
                min={MINIMUM_QUANTITY}
                step={QUANTITY_STEP}
              />
            </div>

           <div className="space-y-2">
            <label className="text-sm font-medium text-muted-foreground">Currency</label>
            <Select
              value={formData.currency || ""}
              onValueChange={(value) => setFormData((prev) => ({ ...prev, currency: value }))}
            >
              <SelectTrigger className="w-full h-10">
                <SelectValue placeholder="Select currency" />
              </SelectTrigger>
              <SelectContent>
                {availableCurrencies.map((c) => (
                  <SelectItem key={c.code} value={c.code}>
                    {c.code} 
                    <span className="text-xs text-muted-foreground ml-2">{c.rate}</span>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          </div>

          {/* Row 4: Small span (Currency latest update) */}
          <div>
            <span className="text-xs text-muted-foreground">
              {availableCurrencies.find((c) => c.code === formData.currency)?.lastUpdated
                ? `Currency latest update: ${availableCurrencies.find((c) => c.code === formData.currency)?.lastUpdated}`
                : "Currency latest update: N/A"}
            </span>
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            className="w-full bg-accent hover:bg-accent/90 text-accent-foreground"
            disabled={isLoading}
          >
            {isLoading ? "Calculating..." : "Calculate Cost"}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
