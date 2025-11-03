"use client"
import React, { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import supabase from "@/lib/supabaseClient"

interface TariffCalculatorFormProps {
  onCalculationComplete: (results: any) => void
  tariffSource: "global" | "user"
}

export function TariffCalculatorForm({ onCalculationComplete, tariffSource }: TariffCalculatorFormProps) {
  const [formData, setFormData] = useState({
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

  // Load initial data based on tariff source
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession()
        const token = session?.access_token
        const authHeaders = {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }

        if (tariffSource === "global") {
          // Load global products and countries
          const productsResponse = await fetch('http://localhost:8080/api/products', { headers: authHeaders })
          const productsData = await productsResponse.json()
          setProducts(Array.isArray(productsData) ? productsData : [])
          
          const countriesResponse = await fetch('http://localhost:8080/api/countries', { headers: authHeaders })
          const countriesData = await countriesResponse.json()
          setCountries(Array.isArray(countriesData) ? countriesData : [])
        } else {
          // Load user-defined tariffs
          const res = await fetch('http://localhost:8080/api/tariff-definitions/user', { headers: authHeaders })
          const data = await res.json()
          if (data.success && Array.isArray(data.data)) {
            setUserTariffs(data.data)
            // Extract unique products and countries from user tariffs
            const uniqueProducts = Array.from(new Set(data.data.map((t: any) => t.product).filter(Boolean)))
            const uniqueCountries = Array.from(new Set([
              ...data.data.map((t: any) => t.exportingFrom).filter(Boolean),
              ...data.data.map((t: any) => t.importingTo).filter(Boolean)
            ]))
            setProducts(uniqueProducts as string[])
            setCountries(uniqueCountries as string[])
          } else {
            setUserTariffs([])
            setProducts([])
            setCountries([])
          }
        }
      } catch (error) {
        console.error('Error loading initial data:', error)
        setProducts([])
        setCountries([])
        setUserTariffs([])
      }
    }
    
    // Reset form when tariff source changes
    setFormData({
      product: "",
      brand: "",
      exportingFrom: "",
      importingTo: "",
      quantity: "1",
      customCost: "",
      calculationDate: new Date().toISOString().split("T")[0],
    })
    setAvailableBrands([])
    
    loadInitialData()
  }, [tariffSource])

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    setError("")

    if (field === "product") {
      const loadBrands = async () => {
        try {
          const { data: { session } } = await supabase.auth.getSession()
          const token = session?.access_token
          const authHeaders = {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          }

          const response = await fetch(`http://localhost:8080/api/brands?product=${encodeURIComponent(value)}`, { headers: authHeaders })
          const brandsData = await response.json()
          setAvailableBrands(Array.isArray(brandsData) ? brandsData : [])
          
          // Auto-select if only one brand available
          if (Array.isArray(brandsData) && brandsData.length === 1 && brandsData[0]?.brand) {
            setFormData((prev) => ({ ...prev, brand: brandsData[0].brand }))
          }
        } catch (error) {
          console.error('Error loading brands:', error)
          setAvailableBrands([])
        }
      }
      loadBrands()
      setFormData((prev) => ({ ...prev, brand: "" }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsLoading(true)

    if (
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

    if (Number.parseFloat(formData.quantity) <= 0) {
      setError("Quantity must be greater than 0")
      setIsLoading(false)
      return
    }

    try {
      const params = new URLSearchParams({
        product: formData.product,
        brand: formData.brand,
        exportingFrom: formData.exportingFrom,
        importingTo: formData.importingTo,
        quantity: formData.quantity,
        ...(formData.customCost && { customCost: formData.customCost }),
        ...(tariffSource === "user" ? { mode: "user" } : {})
      })
      
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
        onCalculationComplete(result.data)
      } else {
        setError(result.error || "Calculation failed")
      }
    } catch (err) {
      console.error("Calculation error:", err)
      setError("An unexpected error occurred during calculation")
    } finally {
      setIsLoading(false)
    }
  }

  // Get filtered lists based on tariff source with safety checks
  const getFilteredProducts = (): string[] => {
    if (tariffSource === "user") {
      if (!Array.isArray(userTariffs) || userTariffs.length === 0) return []
      const uniqueProducts = Array.from(
        new Set(
          userTariffs
            .map((t: any) => t?.product)
            .filter((p): p is string => Boolean(p))
        )
      )
      return uniqueProducts
    }
    return Array.isArray(products) ? products : []
  }

  const getFilteredExportingCountries = (): string[] => {
    if (tariffSource === "user") {
      if (!Array.isArray(userTariffs) || userTariffs.length === 0) return []
      const filtered = userTariffs
        .filter((t: any) => t && (!formData.product || t.product === formData.product))
        .map((t: any) => t?.exportingFrom)
        .filter((c): c is string => Boolean(c))
      return Array.from(new Set(filtered))
    }
    return Array.isArray(countries) ? countries : []
  }

  const getFilteredImportingCountries = (): string[] => {
    if (tariffSource === "user") {
      if (!Array.isArray(userTariffs) || userTariffs.length === 0) return []
      const filtered = userTariffs
        .filter(
          (t: any) =>
            t &&
            (!formData.product || t.product === formData.product) &&
            (!formData.exportingFrom || t.exportingFrom === formData.exportingFrom)
        )
        .map((t: any) => t?.importingTo)
        .filter((c): c is string => Boolean(c))
      return Array.from(new Set(filtered))
    }
    return Array.isArray(countries) ? countries : []
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl font-semibold text-foreground">
          {tariffSource === "global" ? "Global Tariff Calculator" : "Simulator Calculator"}
        </CardTitle>
        <CardDescription>
          {tariffSource === "global"
            ? "Calculate costs using official global tariffs."
            : "Calculate costs using your simulated tariffs."}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground">Product</label>
              <Select value={formData.product} onValueChange={(value) => handleInputChange("product", value)}>
                <SelectTrigger className="w-full h-10">
                  <SelectValue placeholder="Select a product" />
                </SelectTrigger>
                <SelectContent>
                  {getFilteredProducts().map((product) => (
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
                  {getFilteredExportingCountries().map((country) => (
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
                  {getFilteredImportingCountries().map((country) => (
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