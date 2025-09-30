"use client"
import React, { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import supabase from "@/lib/supabaseClient"

interface TariffCalculatorFormProps {
  onCalculationComplete: (results: any) => void
}
export function TariffCalculatorForm({ onCalculationComplete }: TariffCalculatorFormProps) {
  const [formData, setFormData] = useState({
    tariffSource: "global", // Added tariff source selection
    userTariffId: "",
    product: "",
    brand: "",
    exportingFrom: "",
    importingTo: "",
    quantity: "1",
    customCost: "",
    calculationDate: new Date().toISOString().split("T")[0],
  })
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [availableBrands, setAvailableBrands] = useState<any[]>([])
  const [products, setProducts] = useState<string[]>([])
  const [countries, setCountries] = useState<string[]>([])
  const [userTariffs, setUserTariffs] = useState<any[]>([])
  // Load initial data from backend API
  React.useEffect(() => {
    const loadInitialData = async () => {
      try {
        // Get Supabase JWT token for all API endpoints
        const { data: { session } } = await supabase.auth.getSession()
        const token = session?.access_token
        const authHeaders = {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }

        // Get products from backend API
        const productsResponse = await fetch('http://localhost:8080/api/products', { headers: authHeaders })
        const productsData = await productsResponse.json()
        setProducts(productsData)
        
        // Get countries from backend API
        const countriesResponse = await fetch('http://localhost:8080/api/countries', { headers: authHeaders })
        const countriesData = await countriesResponse.json()
        setCountries(countriesData)
      } catch (error) {
        console.error('Error loading initial data from backend:', error)
        setProducts([])
        setCountries([])
      }
    }
    loadInitialData()
  }, [])
  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    setError("")
    if (field === "product") {
      const loadBrands = async () => {
        try {
          // Get Supabase JWT token for API endpoint
          const { data: { session } } = await supabase.auth.getSession()
          const token = session?.access_token
          const authHeaders = {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          }

          // Get brands from backend API
          const response = await fetch(`http://localhost:8080/api/brands?product=${encodeURIComponent(value)}`, { headers: authHeaders })
          const brandsData = await response.json()
          setAvailableBrands(brandsData)
          // If there's only one brand for the selected product, auto-select it
          if (Array.isArray(brandsData) && brandsData.length === 1 && brandsData[0]?.brand) {
            setFormData((prev) => ({ ...prev, brand: brandsData[0].brand }))
          }
        } catch (error) {
          console.error('Error loading brands from backend:', error)
          setAvailableBrands([])
        }
      }
      loadBrands()
      setFormData((prev) => ({ ...prev, brand: "" })) // Reset brand selection
    }
    if (field === "tariffSource") {
      if (value === "user") {
        const loadUserTariffs = async () => {
          try {
            // Get Supabase JWT token for API endpoint
            const { data: { session } } = await supabase.auth.getSession()
            const token = session?.access_token
            const authHeaders = {
              'Authorization': token ? `Bearer ${token}` : '',
              'Content-Type': 'application/json'
            }

            const res = await fetch('http://localhost:8080/api/tariff-definitions/user', { headers: authHeaders })
            const data = await res.json()
            if (data.success && data.data) {
              setUserTariffs(data.data)
            } else {
              setUserTariffs([])
            }
          } catch (e) {
            setUserTariffs([])
          }
        }
        loadUserTariffs()
      }
    }
  }
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsLoading(true)
    if (
      !formData.tariffSource ||
      !formData.product ||
      !formData.brand ||
      !formData.exportingFrom ||
      !formData.importingTo ||
      !formData.quantity
    ) {
      setError("Please fill in all required fields")
      setIsLoading(false)
      return
    }
    // No explicit userTariffId needed; backend will choose matching user-defined definition
    if (Number.parseFloat(formData.quantity) <= 0) {
      setError("Quantity must be greater than 0")
      setIsLoading(false)
      return
    }
    try {
      // Call backend API for calculation
      const params = new URLSearchParams({
        product: formData.product,
        brand: formData.brand,
        exportingFrom: formData.exportingFrom,
        importingTo: formData.importingTo,
        quantity: formData.quantity,
        ...(formData.customCost && { customCost: formData.customCost }),
        ...(formData.tariffSource === "user" ? { mode: "user" } : {})
      })
      
      // Get Supabase JWT token
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token

      const response = await fetch(`http://localhost:8080/api/tariff?${params}`, {
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }
      })
      const result = await response.json()
      
      if (result.success && result.data) {
        // Debug: Log the response to see the structure
        console.log('Backend response:', result)
        console.log('Data object:', result.data)
        // Pass the data object to the results table
        onCalculationComplete(result.data)
      } else {
        setError(result.error || "Calculation failed")
      }
    } catch (err) {
      console.log("Calculation error:", err) // Debug log
      setError("An unexpected error occurred during calculation")
    } finally {
      setIsLoading(false)
    }
  }
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl font-semibold text-foreground">Cost Calculator</CardTitle>
        <CardDescription>Calculate the total import cost for a product.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
          
          <div className="space-y-2">
            <label className="text-sm font-medium text-muted-foreground">Tariff Source</label>
            <Select value={formData.tariffSource} onValueChange={(value) => handleInputChange("tariffSource", value)}>
              <SelectTrigger className="w-full h-10">
                <SelectValue placeholder="Select tariff source" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="global">Global Tariffs</SelectItem>
                <SelectItem value="user">User Defined Tariffs</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Removed separate user-defined tariff dropdown per user request */}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Product</label>
              <Select value={formData.product} onValueChange={(value) => handleInputChange("product", value)}>
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select a product" />
                </SelectTrigger>
                <SelectContent>
                  {(formData.tariffSource === "user"
                    ? Array.from(new Set(
                        userTariffs.map((t: any) => t.product)
                      ))
                    : products
                  ).map((product: string) => (
                    <SelectItem key={product} value={product}>
                      {product}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Brand</label>
              <Select
                value={formData.brand}
                onValueChange={(value) => handleInputChange("brand", value)}
                disabled={!formData.product}
              >
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select a brand" />
                </SelectTrigger>
                <SelectContent>
                  {availableBrands.map((product) => (
                    <SelectItem key={`${product.brand}-${product.cost}`} value={product.brand}>
                      {product.brand} (${product.cost}/{product.unit})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
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
                  {(formData.tariffSource === "user"
                    ? Array.from(new Set(
                        userTariffs
                          .filter((t: any) => !formData.product || t.product === formData.product)
                          .map((t: any) => t.exportingFrom)
                      ))
                    : countries
                  ).map((country: string) => (
                    <SelectItem key={country} value={country}>
                      {country}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Importing To</label>
              <Select value={formData.importingTo} onValueChange={(value) => handleInputChange("importingTo", value)}>
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select country" />
                </SelectTrigger>
                <SelectContent>
                  {(formData.tariffSource === "user"
                    ? Array.from(new Set(
                        userTariffs
                          .filter(
                            (t: any) =>
                              (!formData.product || t.product === formData.product) &&
                              (!formData.exportingFrom || t.exportingFrom === formData.exportingFrom)
                          )
                          .map((t: any) => t.importingTo)
                      ))
                    : countries
                  ).map((country: string) => (
                    <SelectItem key={country} value={country}>
                      {country}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">
                Quantity {availableBrands.length > 0 && availableBrands[0]?.unit ? `(${availableBrands[0].unit})` : ""}
              </label>
              <Input
                type="number"
                value={formData.quantity}
                onChange={(e) => handleInputChange("quantity", e.target.value)}
                placeholder={
                  availableBrands.length > 0 && availableBrands[0]?.unit
                    ? `Enter amount in ${availableBrands[0].unit}`
                    : "1"
                }
                min="0"
                step="0.01"
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Custom Cost (USD)</label>
              <Input
                type="number"
                value={formData.customCost}
                onChange={(e) => handleInputChange("customCost", e.target.value)}
                placeholder="Override brand cost"
                min="0"
                step="0.01"
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