import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ShoppingCart } from "lucide-react"
import { useState } from "react"
import { Alert, AlertDescription } from "@/components/ui/alert"

interface ResultsTableProps {
  results: any
  onAddToCart: (calculation: any) => boolean
}

export function ResultsTable({ results, onAddToCart }: ResultsTableProps) {
  const [exportError, setExportError] = useState("")
  const [successMessage, setSuccessMessage] = useState("")
  const [debugInfo, setDebugInfo] = useState("")

  const handleAddingExport = async () => {
    try {
      setExportError("")
      setSuccessMessage("")
      setDebugInfo("")

      console.log('=== ADD TO CART DEBUG ===')
      console.log('Raw results:', results)

      if (!results) {
        setExportError("No calculation data available")
        return
      }

      // Try to add via callback first
      const callbackSuccess = onAddToCart(results)
      
      if (callbackSuccess) {
        setSuccessMessage("Calculation added to export cart successfully!")
        setTimeout(() => setSuccessMessage(""), 3000)
        return
      }

      // If callback didn't work, try direct API call for debugging
      console.log('Callback returned false, trying direct API call...')
      
      const data = results.data || results
      const payload = {
        calculationId: data.calculationId || `calc-${Date.now()}`,
        product: data.product,
        brand: data.brand,
        exportingFrom: data.exportingFrom,
        importingTo: data.importingTo,
        quantity: data.quantity,
        unit: data.unit,
        productCost: data.productCost,
        totalCost: data.totalCost,
        tariffType: data.tariffType,
        calculationDate: data.calculationDate || new Date().toISOString(),
        breakdown: data.breakdown
      }

      console.log('Sending payload:', payload)

      const response = await fetch('http://localhost:8080/api/export-cart/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(payload)
      })

      console.log('Response status:', response.status)
      console.log('Response headers:', Object.fromEntries(response.headers.entries()))

      const responseText = await response.text()
      console.log('Response body:', responseText)

      if (!response.ok) {
        setDebugInfo(`Status: ${response.status}, Body: ${responseText}`)
        throw new Error(`Server returned ${response.status}: ${responseText}`)
      }

      setSuccessMessage("Added via direct API call!")
      setTimeout(() => setSuccessMessage(""), 3000)

    } catch (error: any) {
      console.error("Error adding to cart:", error)
      setExportError(`Failed to add to cart: ${error.message}`)
      setDebugInfo(JSON.stringify({
        errorMessage: error.message,
        errorStack: error.stack,
        results: results
      }, null, 2))
    }
  }

  const data = results?.data || results
  
  if (!data || !data.breakdown) {
    return null
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle className="text-xl font-semibold text-foreground">Calculation Results</CardTitle>
          <CardDescription>Detailed breakdown of import costs and tariffs</CardDescription>
        </div>
        <Button
          onClick={handleAddingExport}
          variant="outline"
          size="sm"
          className="flex items-center gap-2 bg-transparent"
        >
          <ShoppingCart className="h-4 w-4" />
          Add To Export Cart
        </Button>
      </CardHeader>
      <CardContent>
        {exportError && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{exportError}</AlertDescription>
          </Alert>
        )}

        {successMessage && (
          <Alert className="mb-4">
            <AlertDescription>{successMessage}</AlertDescription>
          </Alert>
        )}

        {debugInfo && (
          <Alert className="mb-4 bg-yellow-50 border-yellow-200">
            <AlertDescription>
              <details>
                <summary className="cursor-pointer font-semibold">Debug Information</summary>
                <pre className="mt-2 text-xs overflow-auto max-h-40">{debugInfo}</pre>
              </details>
            </AlertDescription>
          </Alert>
        )}

        <div className="space-y-6">
          <div className="bg-muted p-4 rounded-lg">
            <h3 className="font-semibold text-foreground mb-2">Summary</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Product</p>
                <p className="font-medium">{data.product}</p>
                <p className="text-xs text-muted-foreground">Brand: {data.brand}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Route</p>
                <p className="font-medium">
                  {data.exportingFrom} → {data.importingTo}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Quantity</p>
                <p className="font-medium">
                  {data.quantity} x {data.unit}
                </p>
                <p className="text-xs text-muted-foreground">Total Product Cost: ${data.productCost.toFixed(2)}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Total Cost</p>
                <p className="font-bold text-foreground">${data.totalCost.toFixed(2)}</p>
                <p className="text-xs text-muted-foreground">{data.tariffType}</p>
              </div>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-4 font-semibold text-foreground">Description</th>
                  <th className="text-left py-3 px-4 font-semibold text-foreground">Type</th>
                  <th className="text-right py-3 px-4 font-semibold text-foreground">Rate</th>
                  <th className="text-right py-3 px-4 font-semibold text-foreground">Amount</th>
                </tr>
              </thead>
              <tbody>
                {data.breakdown.map((item: any, index: number) => (
                  <tr key={index} className="border-b border-border hover:bg-muted/50">
                    <td className="py-3 px-4 text-foreground">{item.description}</td>
                    <td className="py-3 px-4">
                      <Badge
                        variant="secondary"
                        className={
                          item.type?.toLowerCase() === "tariff"
                            ? "bg-accent text-accent-foreground"
                            : item.type?.toLowerCase() === "ahs"
                            ? "bg-primary text-primary-foreground"
                            : ""
                        }
                      >
                        {item.type}
                      </Badge>
                    </td>
                    <td className="py-3 px-4 text-right text-muted-foreground">{item.rate}</td>
                    <td className="py-3 px-4 text-right font-medium text-foreground">${item.amount.toFixed(2)}</td>
                  </tr>
                ))}
                <tr className="border-t-2 border-border bg-muted/50">
                  <td className="py-3 px-4 font-bold text-foreground" colSpan={3}>
                    Total Cost
                  </td>
                  <td className="py-3 px-4 text-right font-bold text-foreground">${data.totalCost.toFixed(2)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}